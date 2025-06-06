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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.EnumUtils;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.helpers.db.Storage;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reader implementation for extracting data from SQL data sources defined via configuration.
 *
 * <p>
 * This class supports parameterized SQL queries that are executed against configured data sources.
 * It primarily supports reading data in "simple" mode, where SQL query results are aggregated into
 * {@link DataList} structures.
 * </p>
 *
 * <p>
 * The reader uses input parameters to substitute macros within SQL scripts and returns the output as
 * a list of named data values. Each result column becomes a {@link Data} object with appropriate
 * content type detection.
 * </p>
 *
 * <p>
 * Process-mode reading is not supported and will result in an {@link UnsupportedOperationException}.
 * </p>
 *
 * @see IReader
 * @see SQLReaderConfiguration
 * @see DataList
 * @see Data
 */
public class SQLListReader implements IReader {

    private final String nullValue = "[null]";
    // Old variant: Pattern.compile("\\{(.*?)\\}");
    private final Pattern inputParametersPattern = Pattern.compile("\\{([a-zA-Z0-9_\\.\\x20]+)\\}");
    private SQLReaderConfiguration configuration;

    private final Logger log = LoggerFactory.getLogger(SQLListReader.class);

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        setLocalConfiguration(configuration);
        return read(false);
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String testConnection(Map<String,String> parameters) throws ReaderException {
        Storage storage
                = new Storage(parameters.get("connectionString"), parameters.get("dbUser"), parameters.get("dbPass"));
        try (Connection conn = storage.getConnection()) {
            return JSONUtils.statusMessage(10000, "Success!").toString();
        } catch (Exception ex) {
            throw new ReaderException(ResponseMessages.msg(20211, ex.getMessage()));
        }
    }

    protected List<DataList> read(boolean isProcess) throws ReaderException {
        List<DataList> resultList = new ArrayList<>();

        //read parameters for each DataSource
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            //create storage for data source
            Storage storage = getStorage(dataSource);
            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());

            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }
            List<Data> dataRecords = new ArrayList<>();
            for (SQLReaderConfiguration.Script script : configuration.getScripts()) {
                String sourceName = script.sourceName;
                boolean sourceFounded = false;
                String[] sourceNames = null;
                if (sourceName.contains(",")) {
                    sourceNames = sourceName.split(",");
                } else {
                    sourceNames = new String[1];
                    sourceNames[0] = sourceName;
                }
                boolean diffNameTypes = false;
                boolean itsNegativeNames = false;
                boolean dsFound = false;
                for (String name : sourceNames) {
                    if (name.startsWith("!")) {
                        itsNegativeNames = true;
                    } else if ((name.equals("*") || name.isEmpty()) && sourceNames.length == 1) {
                        dsFound = true;
                        break;
                    } else if (itsNegativeNames) {
                        diffNameTypes = true;
                        break;
                    }
                    String tempName = (itsNegativeNames) ? "!" + dataSource.getName() : dataSource.getName();
                    if (tempName.equals(name)) {
                        dsFound = true;
                        break;
                    }
                }
                if (diffNameTypes) {
                    log.error(ResponseMessages.msg(20206));
                    throw new ReaderException(ResponseMessages.msg(20206));
                }
                if (dsFound) {
                    if (itsNegativeNames) {
                        break;
                    }
                } else {
                    break;
                }

                //get parameterized script
                String sqlScript
                        = ScriptUtils.prepareParameterizedScript(script.script, configuration.getInputParameters());
                try (PreparedStatement prStatement = storage.getConnection().prepareStatement(sqlScript,
                        ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                    try (ResultSet resultSet = prStatement.executeQuery()) {
                        ResultSetMetaData rsMetaData = resultSet.getMetaData();
                        int columnsCount = rsMetaData.getColumnCount();
                        // Filling data content map with results of the query
                        String content;
                        HashMap<String, String> contentMap = new HashMap<>();
                        String columnName;
                        while (resultSet.next()) {
                            for (int i = 0; i < columnsCount; i++) {
                                content = "";
                                columnName = rsMetaData.getColumnName(i + 1);
                                if (resultSet.getString(columnName) == null) {
                                    content = nullValue;
                                } else {
                                    content = resultSet.getString(columnName);
                                }
                                if (contentMap.containsKey(columnName)) {
                                    String currContent = contentMap.get(columnName);
                                    currContent += "\r\n" + content;
                                    contentMap.put(columnName, currContent);
                                } else {
                                    contentMap.put(columnName, content);
                                }
                            }
                        }
                        // Creating datas
                        for (int i = 0; i < columnsCount; i++) {
                            columnName = rsMetaData.getColumnName(i + 1);
                            // Filling data with basic parameters
                            if (contentMap.get(columnName) != null) {
                                Data data = new Data();
                                data.setName(columnName);
                                data.setContentType(DataContentType.PRIMITIVES);
                                String columnContentType = null;
                                Map<String, String> fieldTypes = new HashMap<>();
                                if (script.fieldTypes.containsKey("*")) {
                                    fieldTypes = script.fieldTypes.get("*");
                                }
                                if (fieldTypes.containsKey("*")) {
                                    columnContentType = fieldTypes.get("*");
                                } else {
                                    columnContentType = fieldTypes.get(columnName);
                                }
                                if (!EnumUtils.isValidEnum(DataContentType.class, columnContentType)
                                        || columnContentType == null) {
                                    data.setContentType(DataContentType.PLAIN_TEXT);
                                } else {
                                    data.setContentType(DataContentType.valueOf(columnContentType));
                                }
                                data.setDataType(DataType.SIMPLE);
                                data.setExternalId("0");
                                data.setTimeStamp(new Date());
                                // Old behaviour: do NOT encode PRIMITIVES
                                data.setContent(DataContentConverter.fromString(contentMap.get(columnName)));
                                dataRecords.add(data);
                            }
                        }
                    }
                } catch (SQLException ex) {
                    log.error(ResponseMessages.msg(20205, ex.getMessage()));
                    throw new ReaderException(ResponseMessages.msg(20205, ex.getMessage()));
                } finally {
                    try {
                        storage.close();
                    } catch (SQLException ex) {
                        log.error(ResponseMessages.msg(20205, ex.getMessage()));
                        throw new ReaderException(ResponseMessages.msg(20205, ex.getMessage()));
                    }
                }
            }
            dataList.setDatas(dataRecords);
            resultList.add(dataList);
        }
        return resultList;
    }

    @Deprecated // Should use methods from org.qubership.automation.pc.core.helpers.ScriptUtils instead
    protected String prepareParameterizedScript(String sqlScript) throws ReaderException {

        //Parse macroses in Script
        Matcher matcher = inputParametersPattern.matcher(sqlScript);
        while (matcher.find()) {
            String fullParameterName = matcher.group(1);
            //cause parameter can have ID for identify parameter, we need check it
            String[] explodedParameterName = fullParameterName.split(".");
            String parameterName = (explodedParameterName.length >= 2) ? explodedParameterName[0] : fullParameterName;
            String parameterId = (explodedParameterName.length >= 2) ? explodedParameterName[1] : null;

            //Prepare Input Parameters and replace it
            String parameterValue = "";
            boolean parameterFound = false;
            for (SQLReaderConfiguration.InputParameter inputParameter : configuration.getInputParameters()) {
                if (inputParameter.name.equals(parameterName)) {
                    if (parameterId != null) {
                        if (inputParameter.id.equals(parameterId)) {
                            parameterValue = inputParameter.value;
                            parameterFound = true;
                            break;
                        }
                    } else {
                        parameterValue = inputParameter.value;
                        parameterFound = true;
                        break;
                    }
                }
            }
            if (!parameterFound) {
                log.error(ResponseMessages.msg(20207, fullParameterName));
                throw new ReaderException(ResponseMessages.msg(20207, fullParameterName));
            }
            if (parameterValue.isEmpty()) {
                log.error(ResponseMessages.msg(20208, fullParameterName));
                throw new ReaderException(ResponseMessages.msg(20208, fullParameterName));
            }
            String parameterMask = matcher.group(0);
            sqlScript = sqlScript.replace(parameterMask, parameterValue); // Old valiant: "'" + parameterValue + "'"
        }
        return sqlScript;
    }

    protected void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

    protected Storage getStorage(SQLDataSource dataSource) {
        Storage storage = new Storage(dataSource.getConnectionString(), dataSource.getUser(), dataSource.getPassword());
        return storage;
    }

}
