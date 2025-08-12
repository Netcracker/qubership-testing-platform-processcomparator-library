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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.qubership.automation.pc.reader.impl.dnr.parser.CiscoTxtFileParser;
import org.qubership.automation.pc.reader.impl.dnr.parser.DnRBlock;
import org.qubership.automation.pc.reader.impl.dnr.parser.IDnRParser;

/**
 * Reader implementation that processes structured text files according to device-specific formats
 * (e.g., Cisco) and extracts data blocks based on keyword configurations.
 *
 * <p>
 * The reader supports both simple and process modes and utilizes keyword-based filtering
 * to identify relevant data segments in the input files.
 * </p>
 *
 * <p>
 * Designed to work with {@link SQLReaderConfiguration} and integrates with
 * the Data/Process model used in comparison frameworks.
 * </p>
 */
public class DnRReader implements IReader {

    public static final String PATH_NAME = "path";
    public static final String DEVICE_FILE_TYPE = "deviceFileType";
    public static final String KEYWORDS = "keywords";

    private String osPath = "";
    private SQLReaderConfiguration configuration;

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

    protected List<DataList> read(boolean isProcess) throws ReaderException {
        List<DataList> resultList = new ArrayList<>();
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());
            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }
            String fullPath;            
            Map<String, String> params = dataSource.getDsParameters();
            getStorage(params.get("connectionString"));
            if (params.containsKey(PATH_NAME)) {
                fullPath = ((StringUtils.isBlank(osPath)) ? "" : osPath + "/") + params.get(PATH_NAME).trim();
            } else {
                fullPath = (StringUtils.isBlank(osPath)) ? "" : osPath;
            }
            List<DnRBlock> parsedFile = new ArrayList<>();
            if (params.containsKey(DEVICE_FILE_TYPE)) {
                String deviceType = params.get(DEVICE_FILE_TYPE).trim();
                parsedFile = readFile(fullPath, deviceType);
            }
            List<List<String>> fieldTypes = new ArrayList<>();
            String[] fieldTypeRows = {};
            if (params.containsKey(KEYWORDS)) {
                Pattern p = Pattern.compile("\\{(.*?)\\}");
                fieldTypeRows = params.get(KEYWORDS).split("\\n");

                for (String fieldTypeRow : fieldTypeRows) {
                    List<String> keywordSet = new ArrayList<>();
                    Matcher m = p.matcher(fieldTypeRow);
                    while (m.find()) {
                        keywordSet.add(m.group(1));
                    }
                    fieldTypes.add(keywordSet);
                }
            }
            List<DnRBlock> filteredParsedFile = null;
            if (parsedFile != null && !parsedFile.isEmpty() && !fieldTypes.isEmpty()) { //&& fieldTypes != null
                filteredParsedFile = findAllParentsByChildren(fieldTypes, parsedFile);
            }
            List<Data> dataRecords;
            if (isProcess) {
                dataRecords = (filteredParsedFile != null && !filteredParsedFile.isEmpty())
                        ? createProcessDataRecords(filteredParsedFile) : null;
            } else {
                dataRecords = (filteredParsedFile != null && !filteredParsedFile.isEmpty())
                        ? createSimpleDataRecords(filteredParsedFile) : null;
            }
            if (dataRecords !=  null) {
                dataList.setDatas(dataRecords);
                resultList.add(dataList);
            }
        }
        return resultList;
    }

    private List<DnRBlock> readFile(String filePath, String deviceType) throws ReaderException {
        List<DnRBlock> parsedFile = null;
        if (!filePath.isEmpty()) {
            File f = new File(filePath);
            if (f.exists() && f.canRead()) {
                InputStreamReader isr = null;
                BufferedReader bufferedReader = null;
                try {
                    isr = new InputStreamReader(new FileInputStream(filePath));
                    bufferedReader = new BufferedReader(isr);
                    IDnRParser parser = null;
                    switch (deviceType) {
                        case "Cisco":
                        default:
                            parser = new CiscoTxtFileParser();
                    }
                    parsedFile = parser.parse(bufferedReader);
                } catch (IOException ex) {
                    Logger.getLogger(DnRReader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return parsedFile;
    }

    private List<DnRBlock> findAllParentsByChildren(List<List<String>> fieldTypes, List<DnRBlock> parsedFile) {
        List<DnRBlock> filteredParsedFile = new ArrayList<>();
        for (List<String> keywordSet : fieldTypes) {
            for (DnRBlock block : parsedFile) {
                if (block.getLine().toLowerCase().contains(keywordSet.get(0).toLowerCase())) {
                    DnRBlock checkedBlock = block;
                    for (int i = 1; i < keywordSet.size(); i++) {
                        if (checkedBlock.getChildren() != null) {
                            checkedBlock = findKeywordInChildren(keywordSet.get(i), checkedBlock.getChildren());
                            if (checkedBlock == null) {
                                break;
                            }
                        }
                    }
                    if (checkedBlock != null) {
                        filteredParsedFile.add(block);
                    }
                }
            }
        }
        return filteredParsedFile;
    }

    private DnRBlock findKeywordInChildren(String keyword, List<DnRBlock> children) {
        for (DnRBlock block : children) {
            if (block.getLine().toLowerCase().contains(keyword.toLowerCase())) {
                return block;
            }
        }
        return null;
    }

    private List<Data> createProcessDataRecords(List<DnRBlock> filteredParsedFile) {
        Data process = new Data();
        process.setDataType(DataType.PROCESS);
        process.setTimeStamp(new Date());
        process.setInternalId(UUID.randomUUID().toString());
        process.setChilds(new ArrayList<Data>());
        int stepIndex = 0;
        for (DnRBlock parentBlock : filteredParsedFile) {
            stepIndex++;
            Data step = new Data();
            step.setName(parentBlock.getLine());
            step.setTimeStamp(new Date());
            step.setOrderNum(stepIndex);
            step.setDataType(DataType.PROCESS_STEP);
            step.setInternalId(UUID.randomUUID().toString());
            step.setExternalId("0");
            step.setChilds(new ArrayList<Data>());

            Data stepParameter = new Data();
            stepParameter.setInternalId(UUID.randomUUID().toString());
            stepParameter.setContentType(DataContentType.PLAIN_TEXT);
            String content = "";
            content = getContent(content, parentBlock);
            // Old behaviour: do NOT encode PRIMITIVES
            // if (stepParameter.getContentType() != DataContentType.PRIMITIVES)
            stepParameter.setContent(DataContentConverter.fromString(content));
            stepParameter.setName("value");
            stepParameter.setDataType(DataType.SIMPLE);
            stepParameter.setExternalId("0");
            stepParameter.setTimeStamp(new Date());
            step.getChilds().add(stepParameter);
            process.getChilds().add(step);
        }
        List<Data> dataRecords = new ArrayList<>();
        dataRecords.add(process);
        return dataRecords;

    }

    private List<Data> createSimpleDataRecords(List<DnRBlock> filteredParsedFile) {
        Data data = new Data();
        data.setName(filteredParsedFile.get(0).getLine());
        data.setContentType(DataContentType.PRIMITIVES);
        data.setContentType(DataContentType.PLAIN_TEXT);
        String content = "";
        for (DnRBlock block : filteredParsedFile) {
            content = content + getContent(content, block) + "\n";
        }
        // Old behaviour: do NOT encode PRIMITIVES
        // if (stepParameter.getContentType() != DataContentType.PRIMITIVES)
        data.setContent(DataContentConverter.fromString(content));
        data.setDataType(DataType.SIMPLE);
        data.setExternalId("0");
        data.setTimeStamp(new Date());
        List<Data> dataRecords = new ArrayList<>();
        dataRecords.add(data);
        return dataRecords;
    }

    private String getContent(String content, DnRBlock block) {
        if (block != null && block.getChildren() != null) {
            for (DnRBlock childBlock : block.getChildren()) {
                content = content + childBlock.getLine() + "\n";
                if (childBlock.getChildren() != null) {
                    content = getContent(content, childBlock);
                }
            }
        }
        return content;
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

    protected void getStorage(String connectionString) throws NumberFormatException {
        /* dataSource.getConnectionString() - returns connection string like "protocol:host:port" */
        /* In this case it contains only filepath !!! */
        if (StringUtils.isBlank(connectionString)) {
            osPath = "";
        } else {
            osPath = connectionString.trim();
        }
    }

    protected void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

}
