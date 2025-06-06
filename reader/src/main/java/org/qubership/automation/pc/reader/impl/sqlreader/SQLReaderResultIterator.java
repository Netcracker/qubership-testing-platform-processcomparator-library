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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.qubership.automation.pc.models.Table.TableRow;

/**
 * Iterator implementation for traversing rows in a {@link SQLReaderResult}.
 *
 * <p>
 * Provides sequential access to each {@link TableRow} contained within the
 * result set. This iterator does not support removal of elements and is
 * intended for read-only iteration.
 * </p>
 */
public class SQLReaderResultIterator implements Iterator<TableRow> {
    private final SQLReaderResult readerResult;
    private int cursor = 0;
    
    public SQLReaderResultIterator(SQLReaderResult readerResult) {
        this.readerResult = readerResult;
    }

    @Override
    public boolean hasNext() {
        return cursor < this.readerResult.rows.size();
    }

    @Override
    public TableRow next() {
        if (this.hasNext()) {
            TableRow current = this.readerResult.rows.get(cursor);
            cursor++;
            return current;
        }
        throw new NoSuchElementException();        
    }
    
}
