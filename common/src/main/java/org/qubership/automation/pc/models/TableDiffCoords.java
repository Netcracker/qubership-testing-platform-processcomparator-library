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

import org.qubership.automation.pc.compareresult.ResultType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TableDiffCoords {

    private ResultType result;
    private String checkName;
    private String tableName;
    private String colName;
    private String reportMessage;
    private int rowId = -1;
    private int colId = -1;

    public TableDiffCoords(String coords, ResultType result) throws NumberFormatException {
        this.result = result;
        String[] items = coords.split("/");
        if (items.length > 0) {
            this.checkName = items[0].trim();
            if (items.length > 1) {
                this.tableName = items[1].trim();
                if (items.length > 2) {
                    rowId = Integer.parseInt(items[2].trim());
                    if (items.length > 3) {
                        try {
                            colId = Integer.parseInt(items[3].trim());
                        } catch (Exception ex) {
                            colId = -1;
                        }
                        if (items.length > 4) {
                            this.colName = items[4].trim();
                            if (items.length > 5) {
                                this.reportMessage = items[5].trim();
                            }
                        }
                    }
                }
            }
        }
    }
}
