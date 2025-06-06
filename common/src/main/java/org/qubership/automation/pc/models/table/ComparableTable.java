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

package org.qubership.automation.pc.models.table;

import java.util.ArrayList;
import java.util.List;

import org.qubership.automation.pc.models.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ComparableTable {

    private String name;
    private List<ComparableTableHeader> headers;
    private List<ComparableTableRow> rows;

    private ComparableTable(String name, List<ComparableTableHeader> headers, List<ComparableTableRow> rows) {
        this.name = name;
        this.headers = headers;
        this.rows = rows;
    }

    public static ComparableTable fromTable(Table table) {
        if (table == null) {
            return null;
        }
        List<ComparableTableHeader> headers = new ArrayList<>(table.headers.size());
        for (int headerIdx = 0; headerIdx < table.headers.size(); headerIdx++) {
            headers.add(new ComparableTableHeader(headerIdx, table.headers.get(headerIdx)));
        }
        List<ComparableTableRow> newRows = new ArrayList<>(table.rows.size());
        for (int rowIdx = 0; rowIdx < table.rows.size(); rowIdx++) {
            List<ComparableTableCell> cells = new ArrayList<>();
            Table.TableRow currRow = table.rows.get(rowIdx);
            for (int colIdx = 0; colIdx < currRow.size(); colIdx++) {
                ComparableTableCell newCell = new ComparableTableCell(rowIdx, colIdx, currRow.get(colIdx));
                cells.add(newCell);
            }
            newRows.add(new ComparableTableRow(rowIdx, cells));
        }
        return new ComparableTable(table.name, headers, newRows);
    }

}
