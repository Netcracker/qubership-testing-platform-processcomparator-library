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

import static org.qubership.automation.pc.converters.CsvConverter.PROP_DELIMETER;
import static org.qubership.automation.pc.converters.CsvConverter.PROP_FIRST_ROW_IS_COLUMNS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.core.exceptions.ValueConverterException;
import org.qubership.automation.pc.models.Table;

public class CsvConverterTest {

    private CsvConverter converter = new CsvConverter();

    @Test
    public void csvConverter_givenCsv_canConvert() throws ValueConverterException {
        String inputValues = "\"12\",\"test\"\n"
                + "\"123.9\",\"Test\"\n"
                + "3,t";
        List<String> expectedHeaders = Arrays.asList("12", "test");
        List<List<String>> expectedRows = new ArrayList<>();
        expectedRows.add(Arrays.asList("123.9", "Test"));
        expectedRows.add(Arrays.asList("3", "t"));
        Map<String, String> properties = new HashMap<>();
        properties.put(PROP_DELIMETER, ",");
        properties.put(PROP_FIRST_ROW_IS_COLUMNS, "true");

        String result = converter.process(inputValues, properties).getValue();

        Table resultTable = Table.fromString(result);
        assertEquals("CSV", resultTable.name, "Name should be CSV");
        assertEquals(expectedHeaders, resultTable.headers, "First row is header");
        assertEquals(expectedRows, resultTable.rows, "Rows should be parsed correctly");
    }

    @Test
    public void csvConverter_givenCsvWithoutHeaders_canGenerateHeaders() throws ValueConverterException {
        String inputValues = "\"12\",\"23\"\n"
                            + "\"43\",\"54\"";
        String[] expectedHeaders = {"Column_0", "Column_1"};
        Map<String, String> properties = new HashMap<>();
        properties.put(PROP_DELIMETER, ",");
        properties.put(PROP_FIRST_ROW_IS_COLUMNS, "false");

        String result = converter.process(inputValues, properties).getValue();

        Table resultTable = Table.fromString(result);
        assertArrayEquals(expectedHeaders, resultTable.getColumnNamesAsArray(), "Headers should be generated as "
                + "Column_i");
    }

    @Test
    public void csvConverter_givenCsvWithDifferentSize_canGenerateHeaderByMaxSizeAndAppendRowsWithEmptyValues()
            throws ValueConverterException {
        String inputValues = "\"12\",\"23\"\n"
                            + "\"13\",\"56\",\"77\",\"89\"\n"
                            + "\"43\",\"54\",\"32\"";
        String[] expectedHeaders = {"Column_0", "Column_1", "Column_2", "Column_3"};
        Map<String, String> properties = new HashMap<>();
        properties.put(PROP_DELIMETER, ",");
        properties.put(PROP_FIRST_ROW_IS_COLUMNS, "false");

        String result = converter.process(inputValues, properties).getValue();

        Table resultTable = Table.fromString(result);
        assertArrayEquals(expectedHeaders, resultTable.getColumnNamesAsArray(),
                "The number of headers should be determined by the size of the maximum line (2nd row, size=4)");
        for (Table.TableRow row: resultTable.rows) {
            assertEquals(4, row.size(), "The size of each row should be the same as the header");
        }
        assertEquals("", resultTable.rows.get(0).getValue("Column_2"),
                "non-existent values are converted to an empty cell");
    }

    @Test
    public void csvConverter_givenCsvWithDifferentSizeAndFirstRowAsHeader_canAppendHeaderRowWithGeneratedValues()
            throws ValueConverterException {
        String inputValues = "\"12\",\"23\"\n"
                        + "\"13\",\"56\",\"77\",\"89\"\n"
                        + "\"43\",\"54\",\"32\"";
        String[] expectedHeaders = {"12", "23", "Column_0", "Column_1"};
        Map<String, String> properties = new HashMap<>();
        properties.put(PROP_DELIMETER, ",");
        properties.put(PROP_FIRST_ROW_IS_COLUMNS, "true");

        String result = converter.process(inputValues, properties).getValue();

        Table resultTable = Table.fromString(result);
        assertArrayEquals(expectedHeaders, resultTable.getColumnNamesAsArray(),
                "All values of the first row must be in the beginning of the header row. "
                        + "The header row must be padded with generated values up to the maximum row size."
                );
    }

    @Test
    public void csvConverter_givenFirstRowAsHeaderAndContainsDefaultValue_canGenerateNewHeadersWithDifferentIndexes()
            throws ValueConverterException {
        String inputValues = "\"Column_0\",\"23\",\"Column_1\"\n"
                + "\"13\",\"56\",\"77\",\"89\",\"132\"\n"
                + "\"43\",\"54\",\"32\"";
        String[] expectedHeaders = {"Column_0", "23", "Column_1", "Column_2", "Column_3"};
        Map<String, String> properties = new HashMap<>();
        properties.put(PROP_DELIMETER, ",");
        properties.put(PROP_FIRST_ROW_IS_COLUMNS, "true");

        String result = converter.process(inputValues, properties).getValue();

        Table resultTable = Table.fromString(result);
        assertArrayEquals(expectedHeaders, resultTable.getColumnNamesAsArray());
    }
}
