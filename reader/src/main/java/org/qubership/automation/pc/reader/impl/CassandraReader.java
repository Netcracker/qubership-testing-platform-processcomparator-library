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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration.InputParameter;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration.OutputParameter;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration.Script;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.converters.Converter;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.exceptions.ValueConverterException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.core.interfaces.IValueConverterValue;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.qubership.automation.pc.models.Table.TableRow;
import org.qubership.automation.pc.reader.impl.cassandra.CassandraConnectionParams;
import org.qubership.automation.pc.reader.impl.cassandra.CassandraReaderResult;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link IReader} for accessing and reading data from a Cassandra database.
 *
 * <p>
 * This reader supports both simple and process modes of data extraction using CQL scripts,
 * and allows configuration via {@link SQLReaderConfiguration}. It supports parameterized scripts,
 * custom field type mappings, and integration with data structures used in the broader framework.
 * </p>
 *
 * <p>
 * Connections are created based on connection strings and credentials, with the ability to
 * validate connection settings via {@code testConnection}.
 * </p>
 */
@Slf4j
public class CassandraReader implements IReader {

    private SQLReaderConfiguration configuration;

    public static final String STEP_NAME_COLUMN = "stepNameField";
    public static final String STEP_GROUP_COLUMN = "stepGroupField";
    public static final String DEFAULT_STEP_NAME = "Step_%d";

    public static final String CONN_STRING_DELIMETER = "/";
    private final String nullValue = "null";
    private final String nullValueEncoded = DataContentConverter.fromString(nullValue);

    private CodecRegistry cr;

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        return read(configuration, false);
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        return read(configuration, true);
    }

    @Override
    public String testConnection(Map<String, String> parameters) throws ReaderException {
        String connectionString = parameters.get("connectionString");
        String dbUser = parameters.get("dbUser");
        String dbPass = parameters.get("dbPass");
        log.debug("Testing connection to Cassandra: " + connectionString + ", " + dbUser);
        CassandraConnectionParams connectionParams = new CassandraConnectionParams(connectionString, dbUser, dbPass);
        try (Cluster cluster = prepareClusterFromConnectionString(connectionParams)) {
            log.debug("Cluster configuration: " + cluster.toString());
            try (Session session = cluster.connect(connectionParams.getKeyspace())) {
                log.debug("Connection test is successful!");
                return JSONUtils.statusMessage(10000, "Success!").toString();
            }
        } catch (Exception ex) {
            log.error("Test connection error: " + ex.getMessage(), ex);
            throw new ReaderException(ResponseMessages.msg(20211, ex.getMessage()));
        }
    }

    private void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

    public Cluster prepareClusterFromConnectionString(CassandraConnectionParams connectionParams) {
        Builder builder = Cluster.builder();
        for (String address : connectionParams.getAddresses()) {
            builder.addContactPoint(address);
        }
        if (connectionParams.getPort() != null) {
            builder.withPort(connectionParams.getPort());
        }
        return builder.withCredentials(connectionParams.getUser(), connectionParams.getPassword())
                .withoutJMXReporting()
                .withoutMetrics()
                .withRetryPolicy(new CustomRetryPolicy(5, 5, 5))
                .build();
    }

    private String convertColumnValueToString(Row row, String columnName) throws ReaderException {
        try {
            com.datastax.driver.core.DataType colType = row.getColumnDefinitions().getType(columnName);
            String result = cr.codecFor(colType).format(row.get(columnName, cr.codecFor(colType).getJavaType()));
            if (result.startsWith("'") && result.endsWith("'")) {
                result = result.substring(0, result.length() - 1).substring(1);
            }
            return result;
        } catch (Exception ex) {
            throw new ReaderException("Error while trying to parse value into String", ex);
        }
    }

    private String getKeyspaceFromConnectionString(String connectionString) {
        return connectionString.substring(connectionString.lastIndexOf(CONN_STRING_DELIMETER) + 1);
    }

    private List<DataList> read(Object config, boolean isProcess) throws ReaderException {
        log.debug("Reading data from Cassandra...");
        setLocalConfiguration(config);
        log.debug("Local configuration: " + config.toString());
        List<DataList> results = new ArrayList<>();
        //read parameters for each DataSource
        for (SQLDataSource dataSource : configuration.getDataSources()) {
            if (StringUtils.isNotBlank(dataSource.getConnectionString())) {
                CassandraConnectionParams connectionParams = new CassandraConnectionParams(
                        dataSource.getConnectionString(), dataSource.getUser(), dataSource.getPassword());
                try (Cluster cluster = prepareClusterFromConnectionString(connectionParams)) {
                    log.debug("Cluster configuration: " + cluster.toString());
                    this.cr = cluster.getConfiguration().getCodecRegistry();
                    try (Session session = cluster.connect(connectionParams.getKeyspace())) {
                        //PreProcessing of input parameters.
                        List<InputParameter> listInputs = configuration.getInputParameters();
                        // Creating data list
                        DataList dataList = new DataList();
                        dataList.setId((dataSource.getId().isEmpty())
                                ? UUID.randomUUID().toString() : dataSource.getId());
                        if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                            dataList.setName(dataSource.getConnectionString());
                        }
                        List<Data> dataRecords = new ArrayList<>();
                        // Processing scripts
                        for (Script script : configuration.getScripts()) {
                            boolean concatenateResult = false;
                            if (script.script.endsWith(";concat")) {
                                script.script = script.script.replace(";concat", "");
                                concatenateResult = true;
                            }
                            // Prepare script
                            String cqlScript = ScriptUtils.prepareParameterizedScript(script.script, listInputs);
                            log.debug("Executing script: " + cqlScript);
                            ResultSet resultSet = session.execute(cqlScript);
                            // Set headers
                            if (isProcess) {
                                // PROCESS PROCESS
                                String stepColumnName = null;
                                if (dataSource.getDsParameters().containsKey(STEP_GROUP_COLUMN)) {
                                    stepColumnName = dataSource.getDsParameters().get(STEP_GROUP_COLUMN).trim();
                                } else {
                                    stepColumnName = dataSource.getDsParameters().containsKey(STEP_NAME_COLUMN)
                                            ? dataSource.getDsParameters().get(STEP_NAME_COLUMN).trim() : null;
                                }
                                List<Definition> definintions = resultSet.getColumnDefinitions().asList();
                                String[] columnNames = new String[definintions.size()];
                                for (int i = 0; i < definintions.size(); i++) {
                                    columnNames[i]
                                            = resultSet.getColumnDefinitions().asList().get(i).getName().toLowerCase();
                                }
                                Data process = new Data();
                                process.setDataType(DataType.PROCESS);
                                process.setTimeStamp(new Date());
                                process.setInternalId(UUID.randomUUID().toString());
                                process.setChildren(new ArrayList<Data>());
                                int stepIndex = 0;
                                Map<String, Integer> stepsCount = new HashMap<>();
                                boolean stepIsCounter = (stepColumnName == null);
                                Boolean hasParentGroupStep = false;
                                // Processing rows
                                for (Row row : resultSet) {
                                    stepIndex++;
                                    Data step = new Data();
                                    String stepName = (stepIsCounter) ? String.format(DEFAULT_STEP_NAME, stepIndex)
                                            : convertColumnValueToString(row, stepColumnName);
                                    final String originalStepName = stepName;
                                    hasParentGroupStep = false;
                                    if (stepsCount.containsKey(stepName)) {
                                        if (!dataSource.getDsParameters().containsKey(STEP_GROUP_COLUMN)) {
                                            stepsCount.put(stepName, stepsCount.get(stepName) + 1);
                                            stepName = stepName + "/" + stepsCount.get(stepName);
                                        } else {
                                            for (Data data : process.getChildren()) {
                                                if (data.getName().equals(stepName)) {
                                                    step = data;
                                                    hasParentGroupStep = true;
                                                    break;
                                                }
                                            }
                                        }
                                    } else {
                                        stepsCount.put(stepName, 1);
                                    }
                                    if (!hasParentGroupStep) {
                                        step.setName(stepName);
                                        step.setTimeStamp(new Date());
                                        step.setOrderNum(stepIndex);
                                        step.setDataType(DataType.PROCESS_STEP);
                                        step.setInternalId(UUID.randomUUID().toString());
                                        step.setExternalId("0");
                                        step.setChildren(new ArrayList<Data>());
                                    }
                                    Map<String, String> fieldTypes = new HashMap<>();
                                    String maskedStepName = ScriptUtils
                                            .fieldTypesContainsStepName(script.fieldTypes, originalStepName);
                                    if (maskedStepName != null) {
                                        fieldTypes = script.fieldTypes.get(maskedStepName);
                                    } else if (script.fieldTypes.containsKey("*")) {
                                        fieldTypes = script.fieldTypes.get("*");
                                    } else if (stepIsCounter) {
                                        int counter = 1;
                                        for (Map.Entry<String, Map<String, String>> entry
                                                : script.fieldTypes.entrySet()) {
                                            if (counter == stepIndex) {
                                                fieldTypes = entry.getValue();
                                                break;
                                            }
                                        }
                                    } else if (script.fieldTypes.containsKey(originalStepName)) {
                                        fieldTypes = script.fieldTypes.get(originalStepName);
                                    }

                                    if (!fieldTypes.isEmpty()) {
                                        int counter = 0;
                                        columnNames = new String[fieldTypes.size()];
                                        for (Map.Entry<String, String> entry : fieldTypes.entrySet()) {
                                            columnNames[counter] = entry.getKey();
                                            counter++;
                                        }
                                    }

                                    for (String columnName : columnNames) {
                                        columnName = columnName.toLowerCase();
                                        Data stepParameter = new Data();
                                        stepParameter.setInternalId(UUID.randomUUID().toString());
                                        String columnContentType = null;
                                        if (!fieldTypes.containsKey(columnName)) {
                                            if (fieldTypes.containsKey("*")) {
                                                columnContentType = fieldTypes.get("*");
                                            }
                                        } else {
                                            columnContentType = fieldTypes.get(columnName);
                                        }
                                        if (!EnumUtils.isValidEnum(DataContentType.class, columnContentType)
                                                || columnContentType == null) {
                                            stepParameter.setContentType(DataContentType.PLAIN_TEXT);
                                        } else {
                                            stepParameter.setContentType(DataContentType.valueOf(
                                                    columnContentType.toUpperCase()));
                                        }
                                        if (convertColumnValueToString(row, columnName) == null) {
                                            stepParameter.setContent(nullValueEncoded);
                                        } else {
                                            String columnValue = convertColumnValueToString(row, columnName);
                                            if (columnContentType != null && columnContentType.startsWith("$")) {
                                                try {
                                                    IValueConverterValue convertedValue
                                                            = Converter.exec(columnValue, columnContentType);
                                                    stepParameter.setContentType(convertedValue.getType());
                                                    columnValue = convertedValue.getValue();
                                                } catch (ValueConverterException ex) {
                                                    throw new ReaderException(ex);
                                                }
                                            }
                                            // Old behaviour: do NOT encode PRIMITIVES
                                            // if (stepParameter.getContentType() != DataContentType.PRIMITIVES)
                                            stepParameter.setContent(DataContentConverter.fromString(columnValue));
                                        }
                                        if (dataSource.getDsParameters().containsKey(STEP_GROUP_COLUMN)) {
                                            columnName = convertColumnValueToString(
                                                    row, dataSource.getDsParameters().get(STEP_NAME_COLUMN));
                                        }
                                        stepParameter.setName(columnName);
                                        stepParameter.setDataType(DataType.SIMPLE);
                                        stepParameter.setExternalId("0");
                                        stepParameter.setTimeStamp(new Date());
                                        step.getChildren().add(stepParameter);
                                    }
                                    if (!hasParentGroupStep) {
                                        process.getChildren().add(step);
                                    }
                                }
                                dataRecords.add(process);
                            } else {
                                // PROCESS SIMPLE
                                CassandraReaderResult readerResult = new CassandraReaderResult(resultSet, this.cr);
                                Iterator<TableRow> resultIterator = readerResult.iterator();
                                Map<String, String> contentMap = new HashMap<>(readerResult.headers.size());
                                while (resultIterator.hasNext()) {
                                    TableRow currRow = resultIterator.next();
                                    for (String column : readerResult.headers) {
                                        if (contentMap.containsKey(column)) {
                                            String concatValue = contentMap.get(column).concat("\r\n"
                                                    + currRow.getValue(column));
                                            contentMap.put(column, concatValue);
                                        } else {
                                            contentMap.put(column, currRow.getValue(column));
                                        }
                                    }
                                    if (!concatenateResult) {
                                        break;
                                    }
                                }
                                if (script.fieldTypes.get("*").get("*").equals("TABLE")) {
                                    readerResult.name = "Query Result";
                                    Data data = new Data();
                                    data.setName(StringUtils.EMPTY);
                                    data.setContentType(DataContentType.TABLE);
                                    data.setContent(DataContentConverter.fromString(readerResult.toString()));
                                    data.setDataType(DataType.SIMPLE);
                                    data.setExternalId("0");
                                    data.setTimeStamp(new Date());
                                    dataRecords.add(data);
                                } else if (configuration.getOutputParameters().isEmpty()) {
                                    for (Map.Entry<String, String> resColumn : contentMap.entrySet()) {
                                        Data data = new Data();
                                        String columnName = resColumn.getKey();
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
                                        data.setContent(DataContentConverter.fromString(resColumn.getValue()));
                                        data.setDataType(DataType.SIMPLE);
                                        data.setExternalId("0");
                                        data.setTimeStamp(new Date());
                                        dataRecords.add(data);
                                    }
                                }
                                for (OutputParameter outputParameter : configuration.getOutputParameters()) {
                                    Data data = new Data();
                                    data.setInternalId(UUID.randomUUID().toString());
                                    data.setName(outputParameter.name);
                                    data.setContentType(outputParameter.contentType);
                                    String value = contentMap.get(outputParameter.name);
                                    if (nullValue == null) {
                                        if (!outputParameter.defaultValue.isEmpty()) {
                                            data.setContent(
                                                    DataContentConverter.fromString(outputParameter.defaultValue));
                                        } else {
                                            data.setContent(nullValueEncoded);
                                        }
                                    } else {
                                        data.setContent(DataContentConverter.fromString(value));
                                    }

                                    data.setDataType(DataType.SIMPLE);
                                    data.setExternalId("0");
                                    data.setTimeStamp(new Date());
                                    dataRecords.add(data);
                                }
                            }
                        }
                        dataList.setDatas(dataRecords);
                        results.add(dataList);
                    }
                } catch (Exception ex) {
                    log.error("Error while reading data from Cassandra: " + ex.getMessage(), ex);
                    throw new ReaderException(ex);
                }

            }
        }
        return results;
    }
}
