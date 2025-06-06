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

package org.qubership.automation.pc.converters;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.exceptions.ValueConverterException;
import org.qubership.automation.pc.core.interfaces.IValueConverter;
import org.qubership.automation.pc.core.interfaces.IValueConverterValue;
import org.qubership.automation.pc.models.Table;
import org.qubership.automation.pc.models.Table.TableRow;

import lombok.extern.slf4j.Slf4j;

/**
 *  Convert TextValue which contains CSV to TABLE type.
 */
@Slf4j
public class CsvConverter implements IValueConverter {

    public static final String PROP_DELIMETER = "delimeter";
    public static final String PROP_FIRST_ROW_IS_COLUMNS = "firstRowIsColumns";
    public static final String PROP_CSV_FORMAT = "format";
    public static final String PROP_COLUMN_PREFIX = "columnPrefix";
    public static final String PROP_TABLE_NAME = "tableName";

    public static final String PROP_DELIMETER_DEFAULT = ";";
    public static final boolean PROP_FIRST_ROW_IS_COLUMNS_DEFAULT = true;

    private String delimiter = PROP_DELIMETER_DEFAULT;
    private boolean firstRowIsColumns = PROP_FIRST_ROW_IS_COLUMNS_DEFAULT;
    private String columnPrefix = "Column_";
    private String tableName = "CSV";


    @Override
    public IValueConverterValue process(String inputValue,
                                        Map<String, String> parameters) throws ValueConverterException {
        readParameters(parameters);
        Table resultTable = new Table(tableName);
        Reader readerIn = new StringReader(inputValue);
        try (CSVParser parser = new CSVParser(readerIn, configureCSVFormat())) {
            if (Objects.nonNull(parser.getHeaderMap())) {
                resultTable.headers.addAll(parser.getHeaderMap().keySet());
            }
            Iterator<CSVRecord> recordIterator = parser.iterator();
            while (recordIterator.hasNext()) {
                TableRow row = new TableRow();
                Iterator<String> cellIterator = recordIterator.next().iterator();
                while (cellIterator.hasNext()) {
                    row.add(cellIterator.next());
                }
                resultTable.rows.add(row);
            }
        } catch (IOException e) {
            String errorMessage = "An error occurred while reading CSV. Message: " + e.getMessage();
            log.error(errorMessage);
            throw new ValueConverterException(errorMessage);
        }
        resultTable.expandTableToMaxRowSize("", columnPrefix);
        IValueConverterValue resultValue = new CsvConverterValue();
        resultValue.setType(DataContentType.TABLE);
        resultValue.setValue(resultTable.toString());
        return resultValue;
    }

    private void readParameters(Map<String, String> parameters) {
        if (parameters.containsKey(PROP_DELIMETER) && parameters.get(PROP_DELIMETER) != null) {
            this.delimiter = parameters.get(PROP_DELIMETER);
        }
        if (parameters.containsKey(PROP_FIRST_ROW_IS_COLUMNS) && parameters.get(PROP_FIRST_ROW_IS_COLUMNS) != null) {
            this.firstRowIsColumns = parameters.get(PROP_FIRST_ROW_IS_COLUMNS).equals("true");
        }
        if (parameters.containsKey(PROP_COLUMN_PREFIX) && parameters.get(PROP_COLUMN_PREFIX) != null) {
            this.columnPrefix = parameters.get(PROP_COLUMN_PREFIX);
        }
        if (parameters.containsKey(PROP_TABLE_NAME) && parameters.get(PROP_TABLE_NAME) != null) {
            this.columnPrefix = parameters.get(PROP_TABLE_NAME);
        }
    }

    private CSVFormat configureCSVFormat() {
        CSVFormat formatter =  CSVFormat.DEFAULT
                .withDelimiter(delimiter.charAt(0));
        if (firstRowIsColumns) {
            formatter = formatter.withFirstRecordAsHeader();
        }
        return formatter;
    }
}
