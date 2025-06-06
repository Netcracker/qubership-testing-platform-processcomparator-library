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

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.google.gson.Gson;

public class Table {

    public String name;
    public TableRow headers;
    public List<TableRow> rows;

    public static class TableRow extends ArrayList<String> {
        private transient Table parent;

        public void setParent(Table parent) {
            this.parent = parent;
        }

        public Table getParent() {
            return this.parent;
        }

        public String getValue(String columnName) {
            if (this.parent == null) {
                throw new NoSuchElementException("Parent for TableRow not set");
            }
            int columnIndex = this.parent.getColumnIndex(columnName);
            if (columnIndex >= this.size()) {
                throw new IndexOutOfBoundsException("Row contains less columns than in the header");
            } else if (columnIndex < 0) {
                throw new IndexOutOfBoundsException("Can't find a column name '"
                        + columnName + "' among table headers " + this.parent.headers.toString());
            }
            return this.get(columnIndex);
        }

        public Map<String, String> getPrimaryColumns(List<String> primaryKeys) {
            Map<String, String> result = new HashMap<>();
            primaryKeys.forEach(pk -> result.put(pk, getValue(pk)));
            return result;
        }
    }

    public Table() {
        this.headers = new Table.TableRow();
        this.rows = new ArrayList<>();
    }

    public Table(String name) {
        this();
        this.name = name;
    }

    public Table(String name, Table.TableRow headers) {
        this(name);
        this.headers.addAll(headers);
    }

    public Table(String name, Table.TableRow headers, List<Table.TableRow> rows) {
        this(name,headers);
        this.name = name;
        this.rows.addAll(rows);
    }

    @Override
    public String toString() {
        for (TableRow row : rows) {
            for (int i = 0; i < row.size(); i++) {
                if (row.get(i) == null) {
                    row.set(i, "null");
                }
            }
        }
        return new Gson().toJson(this);
    }

    public int getColumnCount() {
        return this.headers.size();
    }

    public int getColumnIndex(String columnName) {
        int columnIndex = -1;
        for (String column : headers) {
            columnIndex++;
            final boolean columnNameAreEquals = equalsIgnoreCase(column, columnName)
                    || equalsIgnoreCase(column.replaceAll(".+\\.", ""), columnName);
            if (columnNameAreEquals) {
                return columnIndex;
            }
        }
        return -1;
    }

    public String[] getColumnNamesAsArray() {
        return this.headers.toArray(new String[0]);
    }

    public String getValue(int rowIndex, String columnName) {
        if (rowIndex >= this.rows.size()) {
            throw new NoSuchElementException(String.format("TableRow with index %s doesn't exist", rowIndex));
        }
        TableRow tableRow = this.rows.get(rowIndex);
        int columnIndex = getColumnIndex(columnName);
        if (columnIndex < 0) {
            throw new NoSuchElementException(String.format("Columns with name '%s' not found", columnName));
        }
        return tableRow.get(columnIndex);
    }

    public boolean isConsistent() {
        return this.rows.stream().allMatch(row -> row.size() == this.headers.size());
    }

    public void expandTableToMaxRowSize(String defaultCellValue, String defaultHeaderPrefix) {
        if (isConsistent()) {
            return;
        }
        int maxRowSize = this.rows.stream().mapToInt(record -> record.size()).max().getAsInt();
        int newSize = Math.max(this.headers.size(), maxRowSize);
        changeColumnSize(newSize, defaultCellValue, defaultHeaderPrefix);
    }

    public void changeColumnSize(int newSize, String defaultCellValue, String defaultHeaderPrefix) {
        if (newSize < 0) {
            throw new IllegalArgumentException("new size can't be negative");
        }
        changeTableRowSize(this.headers, newSize, defaultHeaderPrefix, true);
        this.rows.forEach(row -> changeTableRowSize(row, newSize, defaultCellValue, false));
    }

    private void changeTableRowSize(TableRow row, int newSize, String defaultValue, boolean addUniqueIndexes) {
        if (row.size() < newSize) {
            int index = 0;
            while (newSize != row.size()) {
                String value = defaultValue;
                if (addUniqueIndexes) {
                    value += index++;
                    while (row.contains(value)) {
                        value = defaultValue + index++;
                    }
                }
                row.add(value);
            }
        } else if (row.size() > newSize) {
            while (newSize != row.size()) {
                row.remove(row.size() - 1);
            }
        }
    }

    /**
     * Parses JSON string into Table object.
     */
    public static Table fromString(String json) {
        Table table = new Gson().fromJson(json, Table.class);
        if (Objects.nonNull(table) && Objects.nonNull(table.rows)) {
            table.rows.forEach(row -> row.setParent(table));
        }
        return table;
    }

    public String toCsv() {
        StringBuilder csv = new StringBuilder();
        appendCsvRow(csv, this.headers);
        this.rows.forEach(row -> appendCsvRow(csv, row));
        return csv.toString();
    }

    private static void appendCsvRow(StringBuilder builder, List<String> row) {
        Iterator<String> rowIterator = row.iterator();
        while (rowIterator.hasNext()) {
            builder.append("\"")
                    .append(rowIterator.next().replaceAll("\"", "\"\""))
                    .append("\"");
            if (rowIterator.hasNext()) {
                builder.append(",");
            }
        }
        builder.append("\n");
    }

}
