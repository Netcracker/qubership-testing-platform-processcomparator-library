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

package org.qubership.automation.pc.reader.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code FileReader} is an implementation of the {@link IReader} interface designed to read data
 * from the local file system.
 *
 * <p>
 * This class supports both simple and process-based read modes, allowing it to either
 * execute pre-defined scripts or dynamically evaluate paths and file masks based on parameters.
 * It processes plain text files or directories and packages their content into {@code DataList} objects.
 *
 * <p>
 * The reader supports filtering files using regular expressions and optional transformations
 * before encapsulating the content in a structured data format for downstream processing.
 *
 * <p>
 * The expected configuration comes from {@link SQLReaderConfiguration} and the reader
 * assumes the connection string in the data source to be a file or directory path.
 * </p>
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Support for directory traversal and file mask filtering</li>
 *   <li>Reading file contents with character encoding support</li>
 *   <li>Script-based path and mask parameterization</li>
 *   <li>Encapsulation of read data into {@code DataList} and {@code Data} objects</li>
 *   <li>Support for transformation instructions via script metadata</li>
 * </ul>
 *
 * @see IReader
 * @see SQLReaderConfiguration
 * @see SQLDataSource
 * @see DataList
 * @see Data
 */
public class FileReader extends AbstractFileReader implements IReader {

    private String osPath = "";
    private SQLReaderConfiguration configuration;

    private final Logger log = LoggerFactory.getLogger(FileReader.class);

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        setLocalConfiguration(configuration);
        return read(false);
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        setLocalConfiguration(configuration);
        return read(true);
    }

    @Override
    public String testConnection(Map<String, String> parameters) throws ReaderException {
        getStorage(parameters.get("connectionString"));
        if (osPath.isEmpty()) {
            throw new ReaderException(ResponseMessages.msg(20501, "Specified path is empty!"));
        } else {
            try {
                File f = new File(osPath);
                if (f.exists()) {
                    if (f.canRead()) {
                        return JSONUtils.statusMessage(10000,
                                "Success (file/directory exists and can be read)!").toString();
                    } else {
                        throw new ReaderException(ResponseMessages.msg(20501,
                                "Specified path exists BUT cann't be read!"));
                    }
                } else {
                    throw new ReaderException(ResponseMessages.msg(20501,
                            "Specified path doesn't exist!"));
                }
            } catch (NullPointerException | SecurityException ex) {
                throw new ReaderException(ResponseMessages.msg(20501,
                        "Error while path checking: " + ex.getMessage()));
            }
        }
    }

    protected void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

    protected void getStorage(String connectionString) throws NumberFormatException {
        /* dataSource.getConnectionString() - returns connection string like "protocol:host:port" */
        /* In this case it contains only filepath !!! */
        if (StringUtils.isBlank(connectionString)) {
            osPath = "";
        } else {
            osPath = connectionString.trim();
        }
    }

    protected void getStorage(SQLDataSource dataSource) {
        if (dataSource != null) {
            getStorage(dataSource.getConnectionString());
        }
    }

    protected List<DataList> read(boolean isProcess) throws ReaderException {
        List<DataList> resultList = new ArrayList<>();
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            getStorage(dataSource);

            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());

            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }
            List<Data> dataRecords = new ArrayList<>();

            if (isProcess) {
                String fullPath;
                String mask = "";
                Map<String, String> params = dataSource.getDsParameters();
                if (params.containsKey(PATH_NAME_COLUMN)) {
                    fullPath = ((StringUtils.isBlank(osPath))
                            ? "" : osPath + "/") + params.get(PATH_NAME_COLUMN).trim();
                } else {
                    fullPath = (StringUtils.isBlank(osPath)) ? "" : osPath;
                }
                if (params.containsKey(MASK_NAME_COLUMN)) {
                    mask = params.get(MASK_NAME_COLUMN).trim();
                }
                if (params.containsKey(STEP_NAME_COLUMN)) {
                    childName = params.get(STEP_NAME_COLUMN).trim();
                    if (StringUtils.isBlank(childName)) {
                        childName = DEFAULT_STEP_NAME;
                    }
                } else {
                    childName = DEFAULT_STEP_NAME;
                }
                String convert = null;
                if (params.containsKey(CONVERT_PROP)) {
                    convert = params.get(CONVERT_PROP).trim();
                }

                Data process = new Data();
                process.setDataType(DataType.PROCESS);
                process.setTimeStamp(new Date());
                process.setInternalId(UUID.randomUUID().toString());
                process.setChilds(new ArrayList<Data>());
                process.getChilds().addAll(readFiles(isProcess,
                        ScriptUtils
                                .prepareParameterizedScript(fullPath, this.configuration.getInputParameters()),
                        ScriptUtils
                                .prepareParameterizedScript(mask, this.configuration.getInputParameters()), convert));
                dataRecords.add(process);
            } else {
                for (SQLReaderConfiguration.Script script : configuration.getScripts()) {
                    String cmdScript = ScriptUtils
                            .prepareParameterizedScript(script.script, this.configuration.getInputParameters());
                    String fullPath = "";
                    String mask = "";
                    String convert = null;
                    if (cmdScript.contains(MASK_NAME_COLUMN) || cmdScript.contains(PATH_NAME_COLUMN)) {
                        String[] items = cmdScript.split("\r\n|\n|\r");
                        for (int k = 0; k < items.length; k++) {
                            String s = items[k].trim();
                            if (!s.isEmpty()) {
                                if (s.startsWith(CONVERT_PROP + "=")) {
                                    convert = s.substring(CONVERT_PROP.length() + 1).trim();
                                } else if (s.startsWith(MASK_NAME_COLUMN + "=")) {
                                    mask = s.substring(MASK_NAME_COLUMN.length() + 1).trim();
                                } else if (s.startsWith(PATH_NAME_COLUMN + "=")) {
                                    fullPath = s.substring(PATH_NAME_COLUMN.length() + 1).trim();
                                }
                            }
                        }
                    } else {
                        fullPath = cmdScript;
                    }
                    fullPath = ((StringUtils.isBlank(osPath)) ? "" : osPath + "/") + fullPath;
                    dataRecords.addAll(readFiles(isProcess, fullPath, mask, convert));
                }
            }
            dataList.setDatas(dataRecords);
            resultList.add(dataList);
        }
        return resultList;
    }

    private List<Data> readFiles(boolean isProcess,
                                 String fullPath,
                                 final String mask,
                                 String convert) throws ReaderException {
        List<Data> dataRecords = new ArrayList<>();
        if (!fullPath.isEmpty()) {
            File f = new File(fullPath);
            if (f.exists() && f.canRead()) {
                if (f.isDirectory()) {
                    if (!fullPath.endsWith("/")) {
                        fullPath = fullPath + "/";
                    }
                    String[] listFileNames;
                    if (mask.isEmpty()) {
                        listFileNames = f.list();
                    } else {
                        listFileNames = f.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.matches(mask);
                            }
                        });
                    }
                    for (int k = 0; k < listFileNames.length; k++) {
                        File curFile = new File(fullPath + listFileNames[k]);
                        if (!curFile.isDirectory()) {
                            String contents = readFile(fullPath + listFileNames[k], StandardCharsets.UTF_8);
                            dataRecords.add(prepareContents(contents, convert, listFileNames[k], isProcess,mask));
                        }
                    }
                } else {
                    String contents = readFile(fullPath, StandardCharsets.UTF_8);
                    dataRecords.add(prepareContents(contents, convert, fullPath, isProcess,mask));
                }
            }
        }
        return dataRecords;
    }

    private String readFile(String filePath, Charset encoding) throws ReaderException {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(filePath));
            return new String(encoded, encoding);
        } catch (IOException ex) {
            log.error(ResponseMessages.msg(20002,
                    "Error while reading file: " + filePath + "\n" + ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20002,
                    "Error while reading file: " + filePath + "\n" + ex.getMessage()));
        }
    }

}
