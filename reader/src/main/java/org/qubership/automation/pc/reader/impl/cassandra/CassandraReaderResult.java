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

package org.qubership.automation.pc.reader.impl.cassandra;

import java.util.Iterator;
import java.util.List;

import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.models.Table;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public class CassandraReaderResult extends Table implements Iterable {

    private transient CodecRegistry codecRegistry;
    private final transient String nullValue = "null";

    public CassandraReaderResult() {
        super();
    }

    public CassandraReaderResult(ResultSet resultSet, CodecRegistry codecRegistry) throws ReaderException {
        this();
        this.codecRegistry = codecRegistry;
        fromResultSet(resultSet);
    }

    @Override
    public Iterator iterator() {
        return new CassandraReaderResultIterator(this);
    }

    private void fromResultSet(ResultSet resultSet) throws ReaderException {
        List<Definition> columns = resultSet.getColumnDefinitions().asList();
        for (Definition column : columns) {
            this.headers.add(column.getName());
        }
        if (!resultSet.isExhausted()) {
            for (Row row : resultSet) {
                TableRow resultRow = new TableRow();
                resultRow.setParent(this);
                for (Definition column : columns) {
                    String value = convertColumnValueToString(row, column.getName());
                    resultRow.add(value == null ? nullValue : value);
                }
                this.rows.add(resultRow);
            }
        }
    }

    private String convertColumnValueToString(Row row, String columnName) throws ReaderException {
        try {
            com.datastax.driver.core.DataType colType = row.getColumnDefinitions().getType(columnName);
            String result = codecRegistry.codecFor(colType).format(row.get(columnName,
                    codecRegistry.codecFor(colType).getJavaType()));
            if (result.startsWith("'") && result.endsWith("'")) {
                result = result.substring(0, result.length() - 1).substring(1);
            }
            return result;
        } catch (Exception ex) {
            throw new ReaderException("Error while trying to parse value into String", ex);
        }
    }
}
