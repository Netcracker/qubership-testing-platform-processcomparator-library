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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.qubership.automation.pc.core.exceptions.ValueConverterException;
import org.qubership.automation.pc.core.interfaces.IValueConverter;
import org.qubership.automation.pc.core.interfaces.IValueConverterValue;
import org.qubership.automation.pc.models.Table;
import org.qubership.automation.pc.models.Table.TableRow;

/*
 *  Convert TextValue which contains CSV to TABLE type.
 */
public class CsvConverterLegacy implements IValueConverter {

    public static final String PROP_DELIMETER = "delimeter";
    public static final String PROP_FIRST_ROW_IS_COLUMNS = "firstRowIsColumns";

    public static final String PROP_DELIMETER_DEFAULT = ";";
    public static final boolean PROP_FIRST_ROW_IS_COLUMNS_DEFAULT = true;

    private String delimiter = PROP_DELIMETER_DEFAULT;
    private boolean firstRowIsColumns = PROP_FIRST_ROW_IS_COLUMNS_DEFAULT;

    @Override
    public IValueConverterValue process(String inputValue,
                                        Map<String, String> parameters) throws ValueConverterException {
        // Reading properties
        if (parameters.containsKey(PROP_DELIMETER) && parameters.get(PROP_DELIMETER) != null) {
            this.delimiter = parameters.get(PROP_DELIMETER);
        }
        if (parameters.containsKey(PROP_FIRST_ROW_IS_COLUMNS) && parameters.get(PROP_FIRST_ROW_IS_COLUMNS) != null) {
            this.firstRowIsColumns = parameters.get(PROP_FIRST_ROW_IS_COLUMNS).equals("true");
        }
        Table resultTable = new Table("CSV");
        resultTable.rows = new ArrayList<>();
        IValueConverterValue resultValue = new CsvConverterValue();
        String[] csvLines = inputValue.split("\n");
        int rowNum = 1;
        for (String csvLine : csvLines) {
            String[] splittedLine = csvLine.split(this.delimiter);
            boolean addRow = true;
            if (rowNum == 1) {
                List<String> headers;
                if (this.firstRowIsColumns) {
                    headers = Arrays.asList(splittedLine);
                    addRow = false;
                } else {
                    headers = new ArrayList<>();
                    for (int i = 0; i < splittedLine.length; i++) {
                        headers.add("Column_" + i);
                    }
                }
                TableRow header = new TableRow();
                header.addAll(headers);
                resultTable.headers = header;
            }
            if (addRow) {
                TableRow csvRow = new TableRow();

                for (String colValue : splittedLine) {
                    csvRow.add(this.trimQuotes(colValue));
                }
                resultTable.rows.add(csvRow);
            }
            rowNum++;

        }
        resultValue.setValue(resultTable.toString());
        return resultValue;
    }

    private String trimQuotes(String inputString) {
        return inputString.replaceAll("^\"|\"$", "");
    }

    private int getRowLength(String[] splittedLine) {
        int result = 0;
        if (splittedLine != null) {
            result = splittedLine.length;
        }
        return result;
    }

}
