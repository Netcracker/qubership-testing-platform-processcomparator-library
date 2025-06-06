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

package org.qubership.automation.pc.core.interfaces;

import java.util.List;

import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.exceptions.DataStorageException;
import org.qubership.automation.pc.data.Data;

/**
 * Interface for managing in-memory or persistent data storage.
 * </p>
 * Provides operations to retrieve, filter, add, and remove {@link Data} objects
 * based on internal identifiers or content type. Implementations may store data
 * in memory, databases, or other backends.
 */
public interface IDataStorage {
    Data getData(String internalId) throws DataStorageException;

    List<Data> getAllData() throws DataStorageException;

    List<Data> getDataListByContentType(DataContentType contentType) throws DataStorageException;    
    
    void addData(Data data) throws DataStorageException;

    void removeData(String internalId) throws DataStorageException;    
}
