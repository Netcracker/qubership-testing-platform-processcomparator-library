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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.qubership.automation.pc.core.helpers.db.Storage;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.core.interfaces.IValueConverterValue;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.qubership.automation.pc.models.Table;
import org.qubership.automation.pc.models.Table.TableRow;
import org.qubership.automation.pc.reader.impl.sqlreader.SQLReaderResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Reader implementation for executing SQL-based scripts and extracting structured or process-oriented data.
 *
 * <p>
 * The {@code SQLReader} handles data extraction from various SQL data sources using parameterized scripts.
 * It supports simple and process-style reading modes, allows dynamic SQL generation with input parameters,
 * and provides post-processing features such as data type conversions, grouping steps, and key-value transformations.
 * </p>
 *
 * <p>
 * This class can interpret special input parameters passed via URL strings, handle field type configurations,
 * and convert SQL query results into internal {@link Data} representations with rich metadata.
 * </p>
 *
 * <p>
 * The configuration for the reader is provided via {@link SQLReaderConfiguration}, which contains scripts,
 * input/output parameters, and data source settings.
 * </p>
 *
 * <p><b>Note:</b> Some legacy functionality, such as parameter substitution using local methods,
 * is marked as deprecated.
 * Prefer utility methods from {@code ScriptUtils} where available.</p>
 */
public class SQLReader implements IReader {

    public static final String STEP_NAME_COLUMN = "stepNameField";
    public static final String PROCESS_NAME_COLUMN = "processNameField";

    public static final String URL_TO_ISL_PARAMETER = "URLtoISL";
    public static final String URLKEYS_PARAMETER = "URLkeys";

    public static final String STEP_GROUP_COLUMN = "stepGroupField";

    public static final String KEY_VALUE_CONVERT_MODE_PARAMETER = "keyValueConvert";
    public static final String FIRST_CHILD_AS_PARENT_VALUE_PARAMETER = "childAsParentValue";

    public enum KeyValueConvertMode {
        OFF, FROM, TO
    }

    public static final KeyValueConvertMode KEY_VALUE_CONVERT_DEFAULT = KeyValueConvertMode.OFF;
    public static final boolean FIRST_CHILD_AS_PARENT_VALUE_DEFAULT = false;

    public static final String DEFAULT_STEP_NAME = "Step_%d";
    public static final String DEFAULT_CSV_DATA_NAME = "CSV";
    public static final String DEFAULT_TABLE_DATA_NAME = "Query result";

    private final String nullValue = "[null]";
    private final String nullValueEncoded = DataContentConverter.fromString(nullValue);
    // Old variant: Pattern.compile("\\{(.*?)\\}");
    private final Pattern inputParametersPattern = Pattern.compile("\\{([a-zA-Z0-9_\\.\\x20]+)\\}");
    private SQLReaderConfiguration configuration;

    private final Logger log = LoggerFactory.getLogger(SQLReader.class);

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        try {
            setLocalConfiguration(configuration);
            return read(false);
        } catch (MalformedURLException ex) {
            log.error(ResponseMessages.msg(20209, ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20209, ex.getMessage()));
        } catch (UnsupportedEncodingException ex) {
            log.error(ResponseMessages.msg(20210, ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20210, ex.getMessage()));
        }
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        try {
            setLocalConfiguration(configuration);
            return read(true);
        } catch (MalformedURLException ex) {
            log.error(ResponseMessages.msg(20209, ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20209, ex.getMessage()));
        } catch (UnsupportedEncodingException ex) {
            log.error(ResponseMessages.msg(20210, ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20210, ex.getMessage()));
        }
    }

    @Override
    public String testConnection(Map<String, String> parameters) throws ReaderException {
        Storage storage
                = new Storage(parameters.get("connectionString"), parameters.get("dbUser"), parameters.get("dbPass"));
        try (Connection conn = storage.getConnection();) {
            return JSONUtils.statusMessage(10000, "Success!").toString();
        } catch (Exception ex) {
            throw new ReaderException(ResponseMessages.msg(20211, ex.getMessage()));
        }
    }

    protected List<DataList> read(boolean isProcess)
            throws ReaderException, MalformedURLException, UnsupportedEncodingException {
        List<DataList> resultList = new ArrayList<>();
        // Commented - get/set Locale - may be needed to setUp Oracle NLS settings - should be debugged first
        /*
        Locale jvmLocale = Locale.getDefault();
        Locale aLocale = new Builder().setLanguage("en").setScript("Cyrl").setRegion("US").build();
        Locale.setDefault(aLocale);
        jvmLocale = Locale.getDefault();
         */

        //read parameters for each DataSource
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            //PreProcessing of input parameters.
            List<InputParameter> listInputs = configuration.getInputParameters();
            listInputs.addAll(extractFromURL(dataSource.getDsParameters()));

            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());

            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }
            List<Data> dataRecords = new ArrayList<>();
            for (Script script : configuration.getScripts()) {
                String sourceName = script.sourceName;
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

                //create storage for data source
                Storage storage = getStorage(dataSource);

                //get parameterized script
                String sqlScript = ScriptUtils.prepareParameterizedScript(script.script, listInputs);
                try (
                        PreparedStatement prStatement = storage.getConnection().prepareStatement(sqlScript);
                        ResultSet resultSet = prStatement.executeQuery()
                ) {
                    SQLReaderResult resultTable = new SQLReaderResult(resultSet);
                    KeyValueConvertMode convertMode = KEY_VALUE_CONVERT_DEFAULT;
                    if (dataSource.getDsParameters().containsKey(KEY_VALUE_CONVERT_MODE_PARAMETER)
                            && EnumUtils.isValidEnum(KeyValueConvertMode.class,
                            dataSource.getDsParameters().get(KEY_VALUE_CONVERT_MODE_PARAMETER))) {
                        convertMode = KeyValueConvertMode.valueOf(
                                dataSource.getDsParameters().get(KEY_VALUE_CONVERT_MODE_PARAMETER));
                    }
                    Boolean firstChildAsParentValue = FIRST_CHILD_AS_PARENT_VALUE_DEFAULT;
                    if (dataSource.getDsParameters().containsKey(FIRST_CHILD_AS_PARENT_VALUE_PARAMETER)) {
                        firstChildAsParentValue = Boolean.valueOf(
                                dataSource.getDsParameters().get(FIRST_CHILD_AS_PARENT_VALUE_PARAMETER));
                    }
                    if (convertMode != KeyValueConvertMode.OFF) {
                        switch (convertMode) {
                            case FROM:
                                resultTable.convertKeyValueToColumns();
                                break;
                            case TO:
                                resultTable.convertToKeyValue();
                                break;
                            default:
                                // No action needed for unknown convert mode
                                break;
                        }
                    }
                    String[] columnNames = resultTable.getColumnNamesAsArray();
                    Iterator<TableRow> resultTableIterator = resultTable.iterator();
                    if (isProcess) {
                        //do for process
                        String stepColumnName;
                        String originalColumnName = dataSource.getDsParameters().containsKey(STEP_NAME_COLUMN)
                                ? dataSource.getDsParameters().get(STEP_NAME_COLUMN).trim() : null;
                        if (dataSource.getDsParameters().containsKey(STEP_GROUP_COLUMN)) {
                            stepColumnName = dataSource.getDsParameters().get(STEP_GROUP_COLUMN).trim();
                        } else {
                            stepColumnName = originalColumnName;
                        }
                        Data process = new Data();
                        process.setDataType(DataType.PROCESS);
                        process.setTimeStamp(new Date());
                        process.setInternalId(UUID.randomUUID().toString());
                        process.setChilds(new ArrayList<>());
                        int stepIndex = 0;
                        Map<String, Integer> stepsCount = new HashMap<>();
                        Boolean hasParentGroupStep = false;
                        boolean stepIsCounter = (stepColumnName == null);
                        while (resultTableIterator.hasNext()) {
                            stepIndex++;
                            Data step = new Data();
                            String stepName = (stepIsCounter)
                                    ? String.format(DEFAULT_STEP_NAME, stepIndex)
                                    : resultTable.getValue(stepIndex - 1, stepColumnName);
                            hasParentGroupStep = false;
                            if (stepsCount.containsKey(stepName)) {
                                if (!dataSource.getDsParameters().containsKey(STEP_GROUP_COLUMN)) {
                                    stepsCount.put(stepName, stepsCount.get(stepName) + 1);
                                    stepName = stepName + "/" + stepsCount.get(stepName);
                                } else {
                                    for (Data data : process.getChilds()) {
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
                                step.setChilds(new ArrayList<>());
                            }
                            String originalStepName = (stepIsCounter)
                                    ? String.format(DEFAULT_STEP_NAME, stepIndex)
                                    : resultTable.getValue(stepIndex - 1, originalColumnName);
                            Map<String, String> fieldTypes = new HashMap<>();
                            String maskedStepName
                                    = ScriptUtils.fieldTypesContainsStepName(script.fieldTypes, originalStepName);
                            if (maskedStepName != null) {
                                fieldTypes = script.fieldTypes.get(maskedStepName);
                            } else if (script.fieldTypes.containsKey("*")) {
                                fieldTypes = script.fieldTypes.get("*");
                            } else if (stepIsCounter) {
                                int counter = 1;
                                for (Map.Entry<String, Map<String, String>> entry : script.fieldTypes.entrySet()) {
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
                            TableRow currentRow = resultTableIterator.next();
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
                                    stepParameter.setContentType(
                                            DataContentType.valueOf(columnContentType.toUpperCase()));
                                }
                                String columnValue = currentRow.getValue(columnName);
                                if (columnName == null) {
                                    stepParameter.setContent(nullValueEncoded);
                                } else {
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
                                    if (columnValue == null) {
                                        stepParameter.setContent(nullValueEncoded);
                                    } else {
                                        // Old behaviour: do NOT encode PRIMITIVES
                                        // if (stepParameter.getContentType() != DataContentType.PRIMITIVES)
                                        stepParameter.setContent(DataContentConverter.fromString(columnValue));
                                    }
                                }
                                if (dataSource.getDsParameters().containsKey(STEP_GROUP_COLUMN)) {
                                    columnName
                                            = currentRow.getValue(dataSource.getDsParameters().get(STEP_NAME_COLUMN));
                                }
                                stepParameter.setName(columnName);
                                stepParameter.setDataType(DataType.SIMPLE);
                                stepParameter.setExternalId("0");
                                stepParameter.setTimeStamp(new Date());
                                step.getChilds().add(stepParameter);
                            }
                            if (firstChildAsParentValue && !step.getChilds().isEmpty()) {
                                Data firstChild = step.getChilds().get(0);
                                firstChild.setName(step.getName());
                                firstChild.setOrderNum(step.getOrderNum());
                                firstChild.setExternalId(step.getExternalId());
                                firstChild.setInternalId(step.getInternalId());
                                firstChild.setChilds(new ArrayList<>());
                                step = firstChild;
                            }
                            if (!hasParentGroupStep) {
                                process.getChilds().add(step);
                            }
                        }
                        dataRecords.add(process);
                    } else if (resultTableIterator.hasNext()) {
                        dataRecords.addAll(
                                convertResultToSimpleData(resultTableIterator.next(), resultTable, script.fieldTypes));
                    }
                } catch (Exception ex) {
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

    protected List<Data> convertResultToSimpleData(TableRow currentRow,
                                                   Table resultTable,
                                                   LinkedHashMap<String,Map<String,String>> fieldTypes) {
        List<Data> dataRecords = new ArrayList<>();
        String[] columnNames = resultTable.getColumnNamesAsArray();
        String contentType = fieldTypes.containsKey("*") ? fieldTypes.get("*").get("*") : null;
        if (DataContentType.TABLE.name().equalsIgnoreCase(contentType)) {
            resultTable.name = DEFAULT_TABLE_DATA_NAME;
            Data data = new Data(StringUtils.EMPTY, DataType.SIMPLE, "0", new Date());
            data.setContentType(DataContentType.TABLE);
            data.setContent(DataContentConverter.fromString(resultTable.toString()));
            dataRecords.add(data);
        } else if (DataContentType.CSV.name().equalsIgnoreCase(contentType)) {
            resultTable.name = DEFAULT_CSV_DATA_NAME;
            Data data = new Data(StringUtils.EMPTY, DataType.SIMPLE, "0", new Date());
            data.setContentType(DataContentType.CSV);
            data.setContent(DataContentConverter.fromString(resultTable.toCsv()));
            dataRecords.add(data);
        } else if (configuration.getOutputParameters().isEmpty()) {
            for (String columnName : columnNames) {
                Data data = new Data();
                data.setName(columnName);
                data.setContentType(DataContentType.PRIMITIVES);
                String columnContentType = null;
                Map<String, String> commonFieldTypes = new HashMap<>();
                if (fieldTypes.containsKey("*")) {
                    commonFieldTypes = fieldTypes.get("*");
                }
                if (commonFieldTypes.containsKey("*")) {
                    columnContentType = commonFieldTypes.get("*");
                } else {
                    columnContentType = commonFieldTypes.get(columnName);
                }
                if (!EnumUtils.isValidEnum(DataContentType.class, columnContentType) || columnContentType == null) {
                    data.setContentType(DataContentType.PLAIN_TEXT);
                } else {
                    data.setContentType(DataContentType.valueOf(columnContentType));
                }
                String columnValue = currentRow.getValue(columnName);
                if (columnValue == null) {
                    data.setContent(nullValueEncoded);
                } else {
                    // Old behaviour: do NOT encode PRIMITIVES
                    // if (stepParameter.getContentType() != DataContentType.PRIMITIVES)
                    data.setContent(DataContentConverter.fromString(columnValue));
                }
                data.setDataType(DataType.SIMPLE);
                data.setExternalId("0");
                data.setTimeStamp(new Date());
                dataRecords.add(data);
            }
        }
        for (OutputParameter outputParameter : configuration.getOutputParameters()) {
            Data data = new Data(outputParameter.name, DataType.SIMPLE, "0", new Date());
            data.setInternalId(UUID.randomUUID().toString());
            data.setContentType(outputParameter.contentType);
            if (currentRow.getValue(outputParameter.name) == null) {
                if (!outputParameter.defaultValue.isEmpty()) {
                    data.setContent(DataContentConverter.fromString(outputParameter.defaultValue));
                } else {
                    data.setContent(nullValueEncoded);
                }
            } else {
                // Maybe should be encoded too (see above)
                data.setContent(DataContentConverter.fromString(currentRow.getValue(outputParameter.name)));
            }
            dataRecords.add(data);
        }
        return dataRecords;
    }

    private List<InputParameter> extractFromURL(Map<String, String> dsParams)
            throws MalformedURLException, UnsupportedEncodingException {
        List<InputParameter> result = new ArrayList<>();
        boolean urlFound = false;
        boolean keysFound = false;
        String url = "";
        String keys = "";
        if (dsParams.containsKey(URL_TO_ISL_PARAMETER)) {
            url = dsParams.get(URL_TO_ISL_PARAMETER);
            urlFound = true;
        }
        if (!urlFound) {
            return result;
        }
        if (dsParams.containsKey(URLKEYS_PARAMETER)) {
            keys = dsParams.get(URLKEYS_PARAMETER);
            keysFound = true;
        }

        Map<String, List<String>> mapKeys = splitQuery(new URL(url));
        SQLReaderConfiguration.InputParameter inp = configuration.new InputParameter();
        if (!keysFound || keys.isEmpty()) {
            for (Map.Entry<String, List<String>> mapKey : mapKeys.entrySet()) {
                inp.name = mapKey.getKey();
                List<String> lst = mapKey.getValue();
                if (lst.isEmpty()) {
                    inp.value = "";
                } else {
                    // Use only 1st value here. Currently, there are no situations to process array values from URL
                    inp.value = lst.get(0);
                }
                result.add(inp);
            }
        } else {
            String[] keyList = keys.split(",");
            for (String str : keyList) {
                if (!str.isEmpty()) {
                    if (mapKeys.containsKey(str)) {
                        inp.name = str;
                        List<String> lst = mapKeys.get(str);
                        if (lst.isEmpty()) {
                            inp.value = "";
                        } else {
                            // Use only 1st value here.
                            // Currently, there are no situations to process array values from URL
                            inp.value = lst.get(0);
                        }
                        result.add(inp);
                    }
                }
            }
        }
        return result;
    }

    protected Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
        final Map<String, List<String>> query_pairs = new LinkedHashMap<>();
        final String[] pairs = url.getQuery().split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
            if (!query_pairs.containsKey(key)) {
                query_pairs.put(key, new LinkedList<String>());
            }
            final String value = idx > 0 && pair.length() > idx + 1
                    ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
            query_pairs.get(key).add(value);
        }
        return query_pairs;
    }

    @Deprecated // Should use methods from org.qubership.automation.pc.core.helpers.ScriptUtils instead
    protected String prepareParameterizedScript(String sqlScript,
                                                List<InputParameter> listInputs) throws ReaderException {
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
            for (InputParameter inputParameter : listInputs /*configuration.getInputParameters()*/) {
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
            sqlScript = sqlScript.replace(parameterMask, parameterValue);
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

    private JsonArray getTableHeaders(ResultSetMetaData rsMetaData, int columnsCount) throws SQLException {
        JsonArray headersArray = new JsonArray();
        for (int i = 1; i <= columnsCount; i++) {
            headersArray.add(new JsonPrimitive(rsMetaData.getColumnName(i)));
        }
        return headersArray;
    }

    private JsonArray getTableRows(ResultSet resultSet, int columnsCount) throws SQLException {
        JsonArray jsonArray = new JsonArray();
        do {
            JsonArray valueArray = new JsonArray();
            for (int i = 1; i <= columnsCount; i++) {
                String value = (resultSet.getString(i) != null) ? resultSet.getString(i) : "";
                valueArray.add(new JsonPrimitive(value));
            }
            jsonArray.add(valueArray);
        } while (resultSet.next());
        return jsonArray;
    }

    private List<Data> getDataRecords(JsonObject resultObject) {
        Gson gson = new Gson();
        Data data = new Data();
        data.setName(StringUtils.EMPTY);
        data.setContentType(DataContentType.TABLE);
        data.setContent(DataContentConverter.fromString(gson.toJson(resultObject)));
        data.setDataType(DataType.SIMPLE);
        data.setExternalId("0");
        data.setTimeStamp(new Date());
        List<Data> dataRecords = new ArrayList<>();
        dataRecords.add(data);
        return dataRecords;
    }

}
