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

package org.qubership.automation.pc.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TableTest {

    @Test
    public void isConsistent_givenTableWithDifferentRowSize_isNotConsistent() {
        Table table = new Table();
        table.headers.addAll(Arrays.asList("header1", "header2"));
        Table.TableRow tableRow1 = new Table.TableRow();
        tableRow1.addAll(Arrays.asList("r1c1", "r1c2"));
        Table.TableRow tableRow2 = new Table.TableRow();
        tableRow2.addAll(Arrays.asList("r2c1"));
        table.rows.addAll(Arrays.asList(tableRow1, tableRow2));

        Boolean isConsistent = table.isConsistent();

        assertFalse(isConsistent, "Second row size is different, table is not consistent");
    }

    @Test
    public void changeColumnSize_givenTableAndNewSize_canResizeTable() {
        Table table = new Table();
        table.headers.addAll(Arrays.asList("header0"));
        Table.TableRow tableRow1 = new Table.TableRow();
        tableRow1.addAll(Arrays.asList("r1c1", "r1c2", "r1c3", "r1c4"));
        Table.TableRow tableRow2 = new Table.TableRow();
        tableRow2.addAll(Arrays.asList("r2c1", "r2c2"));
        Table.TableRow tableRow3 = new Table.TableRow();
        tableRow3.addAll(Arrays.asList("r3c1"));
        Table.TableRow tableRow4 = new Table.TableRow();
        table.rows.addAll(Arrays.asList(tableRow1, tableRow2, tableRow3, tableRow4));
        List<String> expectedHeaderRow = Arrays.asList("header0", "header1");
        List<String> expectedRow1 = Arrays.asList("r1c1", "r1c2");
        List<String> expectedRow2 = Arrays.asList("r2c1", "r2c2");
        List<String> expectedRow3 = Arrays.asList("r3c1", "null");
        List<String> expectedRow4 = Arrays.asList("null", "null");

        table.changeColumnSize(2, "null", "header");

        assertEquals(4, table.rows.size(), "the number of lines should not change");
        assertEquals(expectedHeaderRow,table.headers, "default headers with unique indexes should be added");
        assertEquals(expectedRow1, table.rows.get(0), "values at the end should be removed");
        assertEquals(expectedRow2, table.rows.get(1), "shouldn't change");
        assertEquals(expectedRow3, table.rows.get(2), "default values should be added");
        assertEquals(expectedRow4, table.rows.get(3), "default values should be added");
    }

    @Test
    public void expandTableToMaxRowSize_givenTable_canResizeTableToMaxRowSize() {
        Table table = new Table();
        table.headers.addAll(Arrays.asList("header0"));
        Table.TableRow tableRow1 = new Table.TableRow();
        tableRow1.addAll(Arrays.asList("r1c1", "r1c2", "r1c3"));
        Table.TableRow tableRow2 = new Table.TableRow();
        tableRow2.addAll(Arrays.asList("r2c1"));
        table.rows.addAll(Arrays.asList(tableRow1, tableRow2));

        table.expandTableToMaxRowSize("", "");

        assertEquals(3, table.headers.size());
        assertEquals(3, table.rows.get(0).size());
        assertEquals(3, table.rows.get(1).size());
    }

    @Test
    public void convertToCsv_givenTable_canConvertToCsvFormat() {
        Table table = new Table();
        table.headers.addAll(Arrays.asList("header0", "header1"));
        Table.TableRow tableRow1 = new Table.TableRow();
        tableRow1.addAll(Arrays.asList("r1;c1", "r1,c2"));
        Table.TableRow tableRow2 = new Table.TableRow();
        tableRow2.addAll(Arrays.asList("r2\"c1", "r2'c2"));
        table.rows.addAll(Arrays.asList(tableRow1, tableRow2));
        String expectedCsv = "\"header0\",\"header1\"\n"
                + "\"r1;c1\",\"r1,c2\"\n"
                + "\"r2\"\"c1\",\"r2'c2\"\n";

        String csv = table.toCsv();

        assertEquals(csv, expectedCsv);
    }

}
