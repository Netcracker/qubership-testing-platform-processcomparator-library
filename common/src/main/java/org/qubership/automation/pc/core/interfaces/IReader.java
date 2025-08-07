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
import java.util.Map;

import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.data.DataList;

/**
 * Interface defining a contract for reading input data from various sources.
 * <p>
 * Implementations are responsible for retrieving and structuring data
 * in formats suitable for comparison or processing.
 */
public interface IReader {
    List<DataList> readSimple(Object configuration) throws ReaderException;

    List<DataList> readProcess(Object configuration) throws ReaderException;

    String testConnection(Map<String,String> parameters) throws ReaderException;
}
