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

package org.qubership.automation.pc.reader.impl.sqlreader;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.models.Table;

/**
 * Represents a result table constructed from a SQL {@link ResultSet}.
 *
 * <p>
 * This class extends {@link Table} and provides functionality to convert
 * between different tabular representations, such as key-value and
 * columnar formats. It is also iterable for easy traversal of rows.
 * </p>
 *
 * <p>
 * The class supports two main transformations:
 * <ul>
 *   <li>{@code convertKeyValueToColumns()} – transforms key-value rows into a single row with named columns.</li>
 *   <li>{@code convertToKeyValue()} – transforms a single column-based row into multiple key-value rows.</li>
 * </ul>
 * </p>
 */
public class SQLReaderResult extends Table implements Iterable {

    private final transient  String nullValue = "[null]";
    private final transient String nullValueEncoded = DataContentConverter.fromString(nullValue);


    public SQLReaderResult() {
        super();
    }

    public SQLReaderResult(ResultSet resultSet) throws SQLException {
        this();
        fromResultSet(resultSet);
    }

    private void fromResultSet(ResultSet resultSet) throws SQLException {
         ResultSetMetaData rsMetaData = resultSet.getMetaData();
        int columnsCount = rsMetaData.getColumnCount();
        //build headers
        TableRow headerRow = new TableRow();
        headerRow.setParent(this);
        for (int columnIndex = 1; columnIndex <= columnsCount; columnIndex++) {
            headerRow.add(rsMetaData.getColumnName(columnIndex).toLowerCase());
        }
        this.headers = headerRow;
        while (resultSet.next()) {
            TableRow resultRow = new TableRow();
            resultRow.setParent(this);
            for (String columnName : this.headers) {
                if (resultSet.getString(columnName) == null) {
                    resultRow.add(null);
                } else {
                    String columnValue = resultSet.getString(columnName);
                    resultRow.add(columnValue);
                }
            }
            this.rows.add(resultRow);
        }
    }

    /**
     * Convert ResultTable from Key=Value view to Column=Value.
     *
     */
    public void convertKeyValueToColumns() {
        // also check rows
        if (this.rows.isEmpty()) {
            throw new NoSuchElementException("Cannot proceed convert because rows are empty");
        }
        TableRow newHeader = new TableRow();
        newHeader.setParent(this);
        TableRow newValueRow = new TableRow();
        newValueRow.setParent(this);
        for (TableRow tableRow : this.rows) {
            if (tableRow.size() < 2) {
                throw new NoSuchElementException("Row contains less columns than required for convert");
            }
            newHeader.add(tableRow.get(0));
            newValueRow.add(tableRow.get(1));
        }
        this.headers = newHeader;
        this.rows.clear();
        this.rows.add(newValueRow);
    }

    /**
     * Convert ResultTable from Column=Value view to Key=Value.
     *
     */
    public void convertToKeyValue() {
        // check if resulttable contains header
        if (this.headers.isEmpty()) {
            throw new NoSuchElementException("Cannot proceed convert because headers are empty");
        }
        // also check rows
        if (this.rows.isEmpty()) {
            throw new NoSuchElementException("Cannot proceed convert because rows are empty");
        }

        TableRow valueRow = this.rows.get(0);
        this.rows.clear();
        for (String columnName : this.headers) {
            String columnValue = valueRow.getValue(columnName);
            TableRow newValueRow = new TableRow();
            newValueRow.setParent(this);
            newValueRow.add(columnName);
            newValueRow.add(columnValue);
            this.rows.add(newValueRow);
        }

        TableRow newHeader = new TableRow();
        newHeader.setParent(this);
        newHeader.add("key");
        newHeader.add("value");

        this.headers = newHeader;
    }

    @Override
    public Iterator iterator() {
        return new SQLReaderResultIterator(this);
    }

}
