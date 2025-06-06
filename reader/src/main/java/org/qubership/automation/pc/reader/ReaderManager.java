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

package org.qubership.automation.pc.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.qubership.automation.pc.configuration.ReaderConfiguration;
import org.qubership.automation.pc.core.ReaderFactory;
import org.qubership.automation.pc.core.enums.ReaderMode;
import org.qubership.automation.pc.core.exceptions.FactoryInstatiationException;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.exceptions.ReaderManagerException;
import org.qubership.automation.pc.core.exceptions.ReaderNotFoundException;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.DataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code ReaderManager} class coordinates data reading operations using
 * dynamic reader implementations.
 *
 * <p>
 * It supports executing readers in different modes (e.g., SIMPLE or PROCESS)
 * and handles validation, instantiation, and error handling for readers.
 * Readers are instantiated via {@link ReaderFactory} based on parameters
 * provided in the {@code ReaderConfiguration}.
 * </p>
 *
 * <p>
 * It also provides a method for testing reader connections based on the specified type and parameters.
 * </p>
 *
 * @see ReaderFactory
 * @see IReader
 * @see ReaderMode
 * @see ReaderConfiguration
 */
public class ReaderManager {
    
    public static final String PARAMETER_READER_TYPE = "readerType";
    public static final String PARAMETER_READER_MODE = "readerMode";
 
    private final Logger log = LoggerFactory.getLogger(ReaderManager.class);
    
    public List<DataList> read(Object readerConfiguration) throws ReaderManagerException {
        ReaderConfiguration globalConfiguration = (ReaderConfiguration) readerConfiguration;
        if (!globalConfiguration.getGlobal().has(PARAMETER_READER_MODE)
               || !EnumUtils.isValidEnum(ReaderMode.class,
                globalConfiguration.getGlobal().get(PARAMETER_READER_MODE))) {
            log.error(ResponseMessages.msg(20203,PARAMETER_READER_MODE));
            throw new ReaderManagerException(ResponseMessages.msg(20203,PARAMETER_READER_MODE));
        }
        if (!globalConfiguration.getGlobal().has(PARAMETER_READER_TYPE)) {
            log.error(ResponseMessages.msg(20203,PARAMETER_READER_TYPE));
            throw new ReaderManagerException(ResponseMessages.msg(20203,PARAMETER_READER_TYPE));
        }
        ReaderMode readerMode = ReaderMode.valueOf(globalConfiguration.getGlobal().get(PARAMETER_READER_MODE));
        
        try {
            IReader reader = ReaderFactory.getReader(globalConfiguration.getGlobal().get(PARAMETER_READER_TYPE));
            List<DataList> resultData = new ArrayList<>();
            if (readerMode == ReaderMode.SIMPLE) {
                resultData = reader.readSimple(readerConfiguration);
            } else if (readerMode == ReaderMode.PROCESS) {
                resultData = reader.readProcess(readerConfiguration);
            }
            return resultData;
        } catch (ReaderNotFoundException | FactoryInstatiationException | ReaderException ex) {
            throw new ReaderManagerException(ex);
        }
    }

    public String testConnection(String readerType, Map<String,String> parameters) throws ReaderManagerException {
        try {
            IReader reader = ReaderFactory.getReader(readerType);
            return reader.testConnection(parameters);
        } catch (ReaderNotFoundException | FactoryInstatiationException | ReaderException ex) {
            throw new ReaderManagerException(ex.getMessage(), ex);
        }
    }
    
}
