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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.models.Table;

public class CheckColumnRule {

    public String columnName;
    public CheckColumnOperations type;
    public List<String> values;

    public CheckColumnRule(String columnName, CheckColumnOperations type, List<String> values) {
        this.columnName = columnName;
        this.type = type;
        this.values = values;
    }

    public static Optional<CheckColumnRule> buildObject(String rule) {
        Optional<CheckColumnRule> result = Optional.empty();
        String[] rawSplit = rule.split("=");
        if (rawSplit.length == 3) {
            try {
                result = Optional.of(new CheckColumnRule(rawSplit[0], CheckColumnOperations.valueOf(rawSplit[1]),
                        Arrays.asList(rawSplit[2].split(","))));
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(ex);
            }
        }
        return result;
    }

    public List<DiffMessage> processTable(Table table) {
        List<DiffMessage> diffs = new ArrayList<>();
        table.headers.stream().filter(header -> StringUtils.equalsIgnoreCase(header, columnName))
                .findFirst()
                .ifPresent(header -> {
                    int columnIndex = table.headers.indexOf(header);
                    table.rows.forEach(row -> {
                        int rowIndex = table.rows.indexOf(row);
                        if (!checkValue(row.get(columnIndex))) {
                            DiffMessage diff = new DiffMessage();
                            diff.setExpectedValue(type.toString() + " " + values);
                            diff.setActual("/" + table.name + "/" + rowIndex + "/" + columnIndex + "/"
                                    + table.headers.get(columnIndex));
                            diff.setActualValue(row.get(columnIndex));
                            diff.setResult(ResultType.MODIFIED);
                            diffs.add(diff);
                        }
                    });
                });
        return diffs;
    }

    public List<DiffMessage> processTable(ComparableTable table) {
        List<DiffMessage> diffs = new ArrayList<>();
        int columnIdx = table.getHeaders().stream()
                .filter(header -> columnName.equals(header.getName()))
                .findFirst()
                .map(header -> header.getIndex())
                .orElse(-1);
        if (columnIdx != -1) {
            table.getRows().forEach(row -> {
                ComparableTableCell cell = row.getCell(columnIdx);
                if (!checkValue(cell.getValue())) {
                    DiffMessage diff = new DiffMessage();
                    diff.setExpectedValue(type.toString() + " " + values);
                    diff.setActual(cell.getRow() + "-" + cell.getColumn());
                    diff.setActualValue(cell.getValue());
                    diff.setResult(ResultType.MODIFIED);
                    diffs.add(diff);
                }
            });
        }
        return diffs;
    }

    public List<DiffMessage> processTable(FatTable table) {
        List<DiffMessage> diffs = new ArrayList<>();
        if (table.getHeaders().contains(columnName)) {
            for (int rowIdx = 0; rowIdx < table.getRows().size(); rowIdx++) {
                Map<String, String> row = table.getRows().get(rowIdx);
                if (!checkValue(row.get(columnName))) {
                    DiffMessage diff = new DiffMessage();
                    diff.setExpectedValue(type.toString() + " " + values);
                    diff.setActual(table.getName() + "|" + rowIdx + "|" + columnName);
                    diff.setActualValue(row.get(columnName));
                    diff.setResult(ResultType.MODIFIED);
                    diffs.add(diff);
                }
            }
        }
        return diffs;
    }

    private boolean checkValue(String actualValue) {
        switch (type) {
            case EQUALS:
                return values.stream().anyMatch(rule -> StringUtils.equalsIgnoreCase(rule, actualValue));
            case NOT_EQUALS:
                return !values.stream().anyMatch(rule -> StringUtils.equalsIgnoreCase(rule, actualValue));
            default:
                return false;
        }
    }

}
