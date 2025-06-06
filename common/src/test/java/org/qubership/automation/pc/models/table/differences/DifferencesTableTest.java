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

package org.qubership.automation.pc.models.table.differences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.ResultType;

public class DifferencesTableTest {

    @Test
    public void filterTable() {
        DifferencesTable table = new DifferencesTable();
        table.setName("Table");
        table.getHeaders().addAll(Arrays.asList("first", "second", "third", "fourth"));
        DifferencesRow row1 = new DifferencesRow(new LinkedList<>(), false, null, null);
        row1.getCells().addAll(Arrays.asList(
                new DifferencesCell("row1_first", null),
                new DifferencesCell("row1_second", ResultType.MODIFIED),
                new DifferencesCell("row1_third", null),
                new DifferencesCell("row1_fourth", null)
                ));
        DifferencesRow row2 = new DifferencesRow(null, true, null, null);
        DifferencesRow row3 = new DifferencesRow(new LinkedList<>(), false, ResultType.EXTRA, null);
        row3.getCells().addAll(Arrays.asList(
                new DifferencesCell("row3_first", null),
                new DifferencesCell("row3_second", null),
                new DifferencesCell("row3_third", null),
                new DifferencesCell("row3_fourth", null)));
        table.getRows().addAll(Arrays.asList(row1, row2, row3));

        table.filterTable(Arrays.asList("first", "fourth"));

        assertEquals(3, table.getRows().size());
        assertEquals(2, table.getHeaders().size());
        assertEquals(2, table.getRows().get(0).getCells().size());
        assertEquals("row1_first", table.getRows().get(0).getCells().get(0).getValue());
        assertEquals("row1_fourth", table.getRows().get(0).getCells().get(1).getValue());
        assertTrue(table.getRows().get(1).isDelimiter());
        assertEquals(2, table.getRows().get(2).getCells().size());
        assertEquals("row3_first", table.getRows().get(2).getCells().get(0).getValue());
        assertEquals("row3_fourth", table.getRows().get(2).getCells().get(1).getValue());
    }
}
