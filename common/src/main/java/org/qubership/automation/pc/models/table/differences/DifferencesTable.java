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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.qubership.automation.pc.compareresult.ResultType;

import com.google.common.base.Strings;
import lombok.Data;

@Data
public class DifferencesTable {

    private String name;
    private List<String> headers = new ArrayList<>();
    private List<DifferencesRow> rows = new ArrayList<>();
    private ResultType result;

    public DifferencesRow getLastRow() {
        return Objects.isNull(rows) ? null : rows.get(rows.size() - 1);
    }

    public void addDelimiterRow(String name) {
        if (Objects.isNull(rows)) {
            rows = new ArrayList<>();
        }
        rows.add(new DifferencesRow(null, true, null, name));
    }

    public boolean hasDelimiters() {
        return this.getRows().stream().anyMatch(row -> row.isDelimiter());
    }

    public boolean hasComments() {
        return this.getRows().stream().anyMatch(row -> !row.isDelimiter() && !Strings.isNullOrEmpty(row.getComment()));
    }

    public void filterTable(List<String> allowedHeaders) {
        if (Objects.isNull(allowedHeaders)) {
            return;
        }
        if (allowedHeaders.isEmpty()) {
            headers.clear();
            rows.clear();
        }
        List<Integer> removeIndexes = new ArrayList<>();
        for (int i = headers.size() - 1; i >= 0; i--) {
            if (!allowedHeaders.contains(headers.get(i))) {
                removeIndexes.add(i);
            }
        }
        for (DifferencesRow row: rows) {
            if (Objects.isNull(row.getCells()) || row.getCells().size() != headers.size()) {
                continue;
            }
            for (Integer removeIndex: removeIndexes) {
                row.getCells().remove((int)removeIndex);
            }
        }
        for (Integer removeIndex: removeIndexes) {
            headers.remove((int)removeIndex);
        }
    }

}
