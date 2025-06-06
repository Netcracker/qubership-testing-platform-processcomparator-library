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

import java.io.IOException;
import java.io.InputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration.InputParameter;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration.Script;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.helpers.db.Storage;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.qubership.automation.pc.models.Table;
import org.qubership.automation.pc.models.TablesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.pjfanning.xlsx.StreamingReader;
import com.google.gson.Gson;
import com.jcraft.jsch.ChannelSftp;

/**
 * {@code ExcelFileReader} implements the {@link IReader} interface and is used for reading data from Excel files.
 *
 * <p>
 * It supports reading both tabular data and key-value configuration pairs from specified Excel sheet ranges.
 * Various configuration parameters are supported such as sheet name, cell range, headers, delimiters,
 * and number rounding.
 * </p>
 *
 * <h3>Supported Configuration Parameters:</h3>
 * <ul>
 *     <li><b>FILEPATH</b> – path to the Excel file (required)</li>
 *     <li><b>SHEETNAME</b> – sheet name(s), comma-separated if multiple</li>
 *     <li><b>HEADERNAME</b> – column names to include</li>
 *     <li><b>RANGE</b> – cell range in Excel notation (e.g., A1:C10)</li>
 *     <li><b>DELIMITER</b> – delimiter to join multiple values</li>
 *     <li><b>TABLENAME</b> – name of the resulting table (if omitted, sheet name is used)</li>
 *     <li><b>COLUMNS</b> – filters columns listed with the pipe symbol (|)</li>
 *     <li><b>ROUNDNUMBER</b> – whether to round numeric values to 4 decimal places (default is true)</li>
 * </ul>
 *
 * <p>Configuration script format: {@code File=path.xlsx;Sheet=Sheet1;Range=A1:C10;Delimiter=;}</p>
 *
 * <p>The class also supports parameterized scripts using placeholders like <code>{parameter}</code>.</p>
 *
 * <h3>Main Features:</h3>
 * <ul>
 *     <li>Reads Excel data based on provided configuration</li>
 *     <li>Merges tables from multiple sheets</li>
 *     <li>Converts data to CSV or JSON (via Gson)</li>
 *     <li>Handles cells of different data types (numbers, strings, dates, formulas)</li>
 *     <li>Issues warnings for invalid formats (e.g., number stored as text)</li>
 * </ul>
 *
 * @see IReader
 * @see SQLReaderConfiguration
 * @see Data
 * @see DataList
 */
public class ExcelFileReader implements IReader {
    public static final String[] CONFIG_ITEMS = {
            "FILEPATH", "SHEETNAME", "HEADERNAME", "RANGE", "DELIMITER", "TABLENAME", "COLUMNS", "ROUNDNUMBER"
    };

    private final String nullValue = "[null]";
    // Old variant: Pattern.compile("\\{(.*?)\\}");
    private final Pattern inputParametersPattern = Pattern.compile("\\{([a-zA-Z0-9_\\.\\x20]+)\\}");
    private SQLReaderConfiguration configuration;

    private SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH); // "yyyyMMddHHmmss"
    private DecimalFormat decimalFormat = setCustomDecimalFormat();

    private final Logger log = LoggerFactory.getLogger(ExcelFileReader.class);
    private ChannelSftp channelSftp = null;

    private String filePath = "";             // Throw Error if null or empty
    // Name of table to store in JSON; 'sheetName' will be used if 'tableName' empty or missed
    private String tableName = "";
    private List<String> sheetName;            // Read sheets if null or empty
    private String rangeSpecification = "";   // Read the whole sheet if null or empty
    private List<String> headerName = new ArrayList<>();          // Read headers if null or empty
    // No delimiter (between cell values if it's needed to combine them) if null or empty
    private String delimiter = "";
    private List<String> colHeaders = new ArrayList<>(); // Column names to read delimited by "|"
    private Boolean roundNumber = true;         // Round numbers up to the 4th digit after comma (X.XXXX)

    // Later (may be) it will be implemented as separate ExcelContextReader
    // true - default value. false = read context values (name in 1st column, all other columns are combined into value)
    private boolean readTable = true;
    private List<String> sheetNameList = new ArrayList<>();

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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected List<DataList> read(boolean isProcess) throws ReaderException {
        List<DataList> resultList = new ArrayList<>();
        Workbook workbook = null;
        InputStream inputStream;

        //read parameters for each DataSource
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            DataList dataList = new DataList();
            dataList.setId(UUID.randomUUID().toString());
            dataList.setName("ExcelData");

            List<Data> dataRecords = new ArrayList<>();
            TablesList tableList = new TablesList();
            List<String> readWarnings = new ArrayList<>();
            for (Script script : configuration.getScripts()) {
                String[] scriptFragments = script.script.split("\n");
                int fragIdx = 0;
                for (String fragment : scriptFragments) {
                    inputStream = null;
                    try {
                        String excelSettings = ScriptUtils
                                .prepareParameterizedScript(fragment, configuration.getInputParameters());
                        parseExcelSettings(excelSettings, fragIdx);
                        FTPReader ftp = new FTPReader();
                        inputStream = ftp.getInputStreamForExcelData(dataSource, filePath);
                        getWorkbook(inputStream, filePath, dataRecords, tableList, readWarnings);
                    } catch (Exception ex) {
                        throw new ReaderException(ResponseMessages.msg(20301, ex.getMessage()));
                    } finally {
                        try {
                            if (inputStream != null) {
                                inputStream.close();
                            }
                        } catch (IOException ex) {
                            throw new ReaderException(ResponseMessages.msg(20301, ex.getMessage()));
                        }
                    }
                    fragIdx++;
                }
                if (readTable) {
                    dataRecords.addAll(convertTableListToDataList(tableList));
                }
            }
            dataList.setDatas(dataRecords);
            dataList.setReadWarnings(readWarnings);
            resultList.add(dataList);
        }
        return resultList;
    }

    protected List<Data> convertTableListToDataList(TablesList tablesList) {
        List<Data> dataRecords = new ArrayList<>();
        Table mergedTable = mergeTables(tablesList);
        boolean isCsvContentType = false;
        try {
            isCsvContentType = DataContentType.CSV.name()
                    .equalsIgnoreCase(configuration.getScripts().get(0).fieldTypes.get("*").get("*"));
        } catch (NullPointerException e) {
            throw new RuntimeException(e);
        }
        if (isCsvContentType) {
            dataRecords.addAll(tablesToCsv(mergedTable));
        } else {
            dataRecords.addAll(tablesToGson(mergedTable));
        }
        return dataRecords;
    }

    private List<Data> tablesToGson(Table table) {
        Gson gson = new Gson();
        Data data = new Data();
        data.setName(StringUtils.EMPTY);
        data.setContentType(DataContentType.TABLE);
        data.setContent(DataContentConverter.fromString(gson.toJson(table)));
        data.setDataType(DataType.SIMPLE);
        data.setExternalId("0");
        data.setTimeStamp(new Date());
        List<Data> dataRecords = new ArrayList<>();

        dataRecords.add(data);
        return dataRecords;
    }

    private List<Data> tablesToCsv(Table table) {
        Data data = new Data();
        data.setName(StringUtils.EMPTY);
        data.setContentType(DataContentType.CSV);
        data.setContent(DataContentConverter.fromString(table.toCsv()));
        data.setDataType(DataType.SIMPLE);
        data.setExternalId("0");
        data.setTimeStamp(new Date());
        List<Data> dataRecords = new ArrayList<>();
        dataRecords.add(data);
        return dataRecords;
    }

    private DecimalFormat setCustomDecimalFormat() {
        DecimalFormat df = new DecimalFormat("#.0000");
        df.setRoundingMode(RoundingMode.HALF_UP);
        DecimalFormatSymbols ds = df.getDecimalFormatSymbols();
        ds.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(ds);
        return df;
    }

    private Table mergeTables(TablesList tableList) {
        Table mergedTable = new Table();
        mergedTable.headers = new Table.TableRow();
        mergedTable.rows = new ArrayList<>();
        List<String> tableNames = new ArrayList<>();
        Map<Integer, String> mappedHeader = new HashMap<>();
        for (Table table : tableList) {
            tableNames.add(table.name);
            if (!headerName.isEmpty()) {
                for (int i = 0; i < table.headers.size(); i++) {
                    if (headerName.contains(table.headers.get(i))) {
                        mappedHeader.put(i, table.headers.get(i));
                        if (!mergedTable.headers.contains(table.headers.get(i))) {
                            mergedTable.headers.add(table.headers.get(i));
                        }
                    }
                }
                for (int y = 0; y < table.rows.size(); y++) {
                    Table.TableRow tblRow = new Table.TableRow();
                    Table.TableRow row = table.rows.get(y);
                    for (int a = 0; a < row.size(); a++) {
                        if (mappedHeader.containsKey(a)) {
                            tblRow.add(row.get(a));
                        }
                    }
                    mergedTable.rows.add(tblRow);
                }
            } else {
                for (String header : table.headers) {
                    if (!mergedTable.headers.contains(header)) {
                        mergedTable.headers.add(header);
                    }
                }
                mergedTable.rows.addAll(table.rows);
            }
            mappedHeader.clear();
        }
        mergedTable.name = tableNames.toString();
        return mergedTable;
    }

    private Table readTableFromRange(Sheet curSheet, CellRangeAddress rangeAddress, List readWarnings) {
        Table table = new Table();
        table.name = tableName;
        table.headers = new Table.TableRow();
        table.rows = new ArrayList<>();
        Table.TableRow tblRow;
        List<Integer> colIndexes = new ArrayList<>();
        boolean firstRow = true;
        for (Row row : curSheet) {
            if (row.getRowNum() >= rangeAddress.getFirstRow() && row.getRowNum() <= rangeAddress.getLastRow()) {
                tblRow = new Table.TableRow();
                if (firstRow) {
                    tblRow.add("Row#"); // 1st Column - for storing physical row numbers
                } else {
                    tblRow.add(String.valueOf(row.getRowNum() + 1));
                }

                int firstCol;
                int lastCol;
                if (rangeAddress.getLastColumn() == 0) { // whole sheet
                    firstCol = row.getFirstCellNum();
                    lastCol = row.getLastCellNum();
                } else {
                    firstCol = rangeAddress.getFirstColumn();
                    lastCol = rangeAddress.getLastColumn();
                }

                // Non-breaking space and new line should be replaced to ' '
                String badStr = createBadString();
                for (int idCol = firstCol; idCol <= lastCol; idCol++) {
                    String cellValue;
                    Cell curCell = row.getCell(idCol);
                    if (curCell != null) {
                        switch (curCell.getCellType()) {
                            case NUMERIC  /* 0 */:
                                if (DateUtil.isCellDateFormatted(curCell)) {
                                    Date dcellValue = curCell.getDateCellValue();
                                    cellValue = outputFormat.format(dcellValue);
                                } else {
                                    Boolean percentageFormat
                                            = curCell.getCellStyle().getDataFormatString().contains("%");
                                    Double dblVal = percentageFormat
                                            ? curCell.getNumericCellValue() * 100 : curCell.getNumericCellValue();
                                    if (dblVal != null && !dblVal.isNaN()) {
                                        int intVal = dblVal.intValue();
                                        if (dblVal.equals((double) intVal)) {
                                            cellValue = String.valueOf(intVal);
                                        } else {
                                            // cellValue = String.format("%.4f", dblVal);
                                            cellValue = roundNumber ? decimalFormat.format(dblVal) : dblVal.toString();
                                            if (cellValue.startsWith(".")) {
                                                cellValue = "0" + cellValue;
                                            } else if (cellValue.startsWith("-.")) {
                                                cellValue = "-0." + cellValue.substring(2);
                                            }
                                        }
                                        cellValue = percentageFormat ? cellValue + "%" : cellValue;
                                    } else {
                                        cellValue = "";
                                    }
                                }
                                break;
                            case STRING   /* 1 */:
                                cellValue = curCell.getStringCellValue().replaceAll(badStr, " ").trim();
                               if (cellValue.matches("-?\\d+(\\.\\d+)?")) {
                                    readWarnings.add("The value '"
                                            + cellValue
                                            + "' in the ar cell with row number="
                                            + (row.getRowNum() + 1)
                                            + " and column number="
                                            + (idCol + 1)
                                            + " is formatted as text (not as number).");
                                }
                                break;
                            case FORMULA  /* 2 */:
                                cellValue = curCell.getStringCellValue().replaceAll(badStr, " ").trim();
                                break;
                            case BLANK    /* 3 */:
                                cellValue = "";
                                break;
                            case BOOLEAN  /* 4 */:
                                cellValue = String.valueOf(curCell.getBooleanCellValue());
                                break;
                            case ERROR    /* 5 */:
                                cellValue = curCell.getStringCellValue();
                                break;
                            default:
                                cellValue = "";
                                break;
                        }
                    } else {
                        cellValue = "";
                    }
                    if (firstRow) {
                        cellValue = cellValue.trim();
                        if (!colHeaders.isEmpty()) {
                            if (cellValue.isEmpty() || !colHeaders.contains(cellValue)) {
                                continue;
                            } else {
                                colIndexes.add(idCol);
                            }
                        }
                    } else {
                        if (!colIndexes.isEmpty() && !colIndexes.contains((Integer) idCol)) {
                            continue;
                        }
                    }
                    tblRow.add(cellValue);
                }
                if (firstRow) {
                    table.headers.addAll(tblRow);
                    firstRow = false;
                    if (!colHeaders.isEmpty() && colIndexes.isEmpty()) {
                        // It seems that table & script settings don't correspond with it other.
                        // Read only headers and stop reading.
                        break;
                    }
                } else {
                    table.rows.add(tblRow);
                }
            }
        }
        return table;
    }

    private List<Data> readContextValuesFromRange(Sheet curSheet, CellRangeAddress rangeAddress, String delimiter) {
        List<Data> dataRecords = new ArrayList<>();
        for (int idRow = rangeAddress.getFirstRow(); idRow <= rangeAddress.getLastRow(); idRow++) {
            Row row = curSheet.getRow(idRow);
            if (row == null) {
                continue;
            }
            int firstCol;
            int lastCol;
            if (rangeAddress.getLastColumn() == 0) { // whole sheet
                firstCol = row.getFirstCellNum();
                lastCol = row.getLastCellNum();
            } else {
                firstCol = rangeAddress.getFirstColumn();
                lastCol = rangeAddress.getLastColumn();
            }

            String itemName = row.getCell(firstCol).getStringCellValue().trim();
            if (itemName.isEmpty()) {
                continue;
            }

            String itemValue = "";
            String cellValue;
            for (int idCol = firstCol + 1; idCol <= lastCol; idCol++) {
                cellValue = row.getCell(idCol).getStringCellValue().trim();
                if (cellValue.isEmpty()) {
                    continue;
                }
                if (itemValue.isEmpty()) {
                    itemValue = cellValue;
                } else {
                    itemValue = itemValue + delimiter + cellValue;
                }
            }

            // Add Data element
            Data data = new Data();
            data.setName(itemName);
            data.setContentType(DataContentType.PRIMITIVES);
            data.setContent((itemValue.isEmpty()) ? itemValue : DataContentConverter.fromString(itemValue));
            data.setDataType(DataType.SIMPLE);
            data.setExternalId("0");
            data.setTimeStamp(new Date());
            dataRecords.add(data);
        }
        return dataRecords;
    }

    protected void getWorkbook(InputStream inputStream,
                               String excelFilePath,
                               List<Data> dataRecords,
                               TablesList tableList,
                               List readWarnings) throws IOException {
        try (Workbook workbook = StreamingReader.builder()
                .rowCacheSize(1000)
                .bufferSize(8192)
                .open(inputStream)) {
            if (sheetName.isEmpty()) {
                sheetName.add(workbook.getSheetName(0));
                if (sheetName.isEmpty()) {
                    throw new ReaderException(ResponseMessages.msg(20301,
                            "Missed sheetName and ExcelBook doesn't have 1st sheet!"));
                }
            }
            for (String sheet : sheetName) {
                Sheet curSheet = workbook.getSheet(sheet);
                if (curSheet == null) {
                    throw new ReaderException(ResponseMessages.msg(20301,
                            "Error while reading sheet: " + sheetName + ". May be nonexistent sheet."));
                }
                CellRangeAddress rangeAddress;
                if (!rangeSpecification.isEmpty()) {
                    rangeAddress = CellRangeAddress.valueOf(rangeSpecification);
                } else {
                    rangeAddress = new CellRangeAddress(curSheet.getFirstRowNum(),
                            curSheet.getLastRowNum(), 0, 0);
                }
                tableName = curSheet.getSheetName();
                if (readTable) {
                    tableList.add(readTableFromRange(curSheet, rangeAddress, readWarnings));
                } else {
                    dataRecords.addAll(readContextValuesFromRange(curSheet, rangeAddress, delimiter));
                }
            }
        } catch (Exception ex) {
            throw new IOException("Error while opening file: " + excelFilePath + "\n" + ex.getMessage());
        }
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
            for (InputParameter inputParameter : configuration.getInputParameters()) {
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
        Storage storage = null; // Configuration is totally ignored now. All settings are in scripts
        return storage;
    }

    protected void parseExcelSettings(String settings, int counter) throws ReaderException {
        // Script string format is: File=<file>, Sheet=<Sheet>, Range=<Range>, delimiter=<delimiter>,
        // Columns=col1|col2|...|colN
        // For example: File=test.xls,Sheet=Page-1,Range=A1:C10,delimiter=;
        // Delimiter is used to delimit values if there are more than 2 columns in range
        // (1st left column makes name of data, other columns are concatenated to content of data)
        if (settings.isEmpty()) {
            throw new ReaderException(ResponseMessages.msg(20301, "Settings string is empty"));
        }
        String[] settingsList = settings.split(";");
        if (settingsList.length == 0) {
            throw new ReaderException(ResponseMessages.msg(20301,
                    "Incorrect format of Settings string.\nShould be: TableName=<tableName>, "
                            + "File=<file>, Sheet=<Sheet>, Range=<Range>, delimiter=<delimiter>"));
        }
        filePath = "";
        sheetName = new ArrayList<>();
        headerName = new ArrayList<>();
        tableName = "";
        rangeSpecification = "";
        delimiter = "";
        colHeaders = new ArrayList<>();
        roundNumber = true;
        for (int i = 0; i < settingsList.length; i++) {
            int k = settingsList[i].indexOf("=");
            if (k == -1) {
                continue;
            }
            String itemName = settingsList[i].substring(0, k).toUpperCase();
            String itemValue = settingsList[i].substring(k + 1);
            setValue(itemName, itemValue);
        }
        if (tableName.isEmpty()) {
            tableName = (sheetName.isEmpty()) ? "table" + counter : sheetName.get(0);
        }
        if (filePath.isEmpty()) {
            throw new ReaderException(ResponseMessages.msg(20301, "FilePath is empty"));
        }
    }

    private void setValue(String itemName, String itemValue) {
        for (int k = 0; k < CONFIG_ITEMS.length; k++) {
            if (itemName.equals(CONFIG_ITEMS[k])) {
                switch (k) {
                    case 0:
                        filePath = itemValue.trim();
                        break;
                    case 1:
                        sheetName = Arrays.asList(itemValue.split(","));
                        break;
                    case 2:
                        headerName = Arrays.asList(itemValue.split(","));
                        break;
                    case 3:
                        rangeSpecification = itemValue.trim();
                        break;
                    case 4:
                        delimiter = itemValue; // delimiter may be " "
                        break;
                    case 5:
                        tableName = itemValue.trim();
                        break;
                    case 6:
                        List<String> names = Arrays.asList(itemValue.split("\\|"));
                        if (!names.isEmpty()) {
                            for (String s : names) {
                                s = s.trim();
                                if (!s.isEmpty()) {
                                    colHeaders.add(s);
                                }
                            }
                        }
                        break;
                    case 7:
                        roundNumber = Boolean.valueOf(itemValue.trim());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private String createBadString() {
        Character [] nbsp = {(char) 160, (char) 10};  // empty and /n chars
        StringBuilder sb = new StringBuilder("(");
        Arrays.stream(nbsp).forEach((ch) -> sb.append(ch).append("|"));
        sb.deleteCharAt(sb.length() - 1).append(")");
        return sb.toString();
    }
}
