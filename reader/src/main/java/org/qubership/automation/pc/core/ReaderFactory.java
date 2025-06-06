/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.automation.pc.core;

import java.util.HashMap;
import java.util.Map;

import org.qubership.automation.pc.core.exceptions.FactoryInstatiationException;
import org.qubership.automation.pc.core.exceptions.ReaderNotFoundException;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class responsible for creating instances of reader implementations based on their type.
 *
 * <p>
 * This factory follows the singleton pattern to ensure a single instance manages the
 * creation and lookup of supported {@link IReader} implementations.
 * </p>
 *
 * <p>
 * Reader types are dynamically instantiated using reflection based on the configured class path.
 * The factory also maintains a registry of known reader types mapped to their fully qualified class names.
 * </p>
 *
 * <p>
 * Usage of this factory allows abstraction from the concrete implementation details of various readers
 * (e.g., SQLReader, FTPReader, FileReader) and provides a centralized mechanism for instantiation.
 * </p>
 */
public class ReaderFactory {

    private final Logger log = LoggerFactory.getLogger(ReaderFactory.class);
    private final Map<String, ReaderInfo> readers = new HashMap<>();
    private final String packagePath = "org.qubership.automation.pc.reader.impl";

    private ReaderFactory() {
        fillReaders();
    }

    @Deprecated
    private void fillReaders() {
        readers.put("SQLReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.SQLReader"));
        readers.put("ITFReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.ITFReader"));
        readers.put("SQLListReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.SQLListReader"));
        readers.put("CLIReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.CLIReader"));
        readers.put("ExcelFileReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.ExcelFileReader"));
        readers.put("NCObjectReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.NCObjectReader"));
        readers.put("BuildInfoReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.BuildInfoReader"));
        readers.put("FTPReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.FTPReader"));
        readers.put("FileReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.FileReader"));
        readers.put("DnRReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.DnRReader"));
        readers.put("CassandraReader", new ReaderInfo("org.qubership.automation.pc.reader.impl.CassandraReader"));
    }

    private IReader getReaderByType(String readerType) throws ReaderNotFoundException, FactoryInstatiationException {
        try {
            Class<?> readerClass = Class.forName(packagePath + "." + readerType);
            return (IReader) readerClass.newInstance();
        } catch (ClassNotFoundException ex) {
            log.error(ResponseMessages.msg(20201, readerType));
            throw new ReaderNotFoundException(ResponseMessages.msg(20201, readerType));
        } catch (InstantiationException | IllegalAccessException ex) {
            log.error(ResponseMessages.msg(20202,
                    " ReaderType = " + readerType + " Error: " + ex.getMessage()));
            throw new FactoryInstatiationException(ResponseMessages.msg(20202,
                    " ReaderType = " + readerType + " Error: " + ex.getMessage()));
        }
    }

    private static ReaderFactory _instance;

    private static ReaderFactory getInstance() {
        if (_instance == null) {
            _instance = new ReaderFactory();
        }
        return _instance;
    }

    public static IReader getReader(String readerType) throws ReaderNotFoundException, FactoryInstatiationException {
        return getInstance().getReaderByType(readerType);
    }

    private class ReaderInfo {

        public String path;
        public Class<?> readerClass;

        public ReaderInfo() {
        }

        public ReaderInfo(String path) {
            this.path = path;
        }

        public Class<?> getReaderClass() throws ClassNotFoundException {
            if (this.readerClass == null) {
                if (this.path == null) {
                    return null;
                } else {
                    this.readerClass = Class.forName(this.path);
                }
            }
            return this.readerClass;
        }
    }

}
