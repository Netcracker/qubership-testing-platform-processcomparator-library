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

package org.qubership.automation.pc.core.enums;

/**
 * Specifies the type of data reader used to retrieve input for comparison.
 * <p>
 * Each reader corresponds to a specific source or format of data.
 * <ul>
 *   <li>{@code SQLReader} – Reads data from a SQL database.</li>
 *   <li>{@code ITFReader} – Reads from an ITF (Interface Test Format) source.</li>
 *   <li>{@code SQLListReader} – Executes and reads from a list of SQL queries.</li>
 *   <li>{@code CLIReader} – Retrieves data through command-line interface output.</li>
 *   <li>{@code ExcelFileReader} – Parses and reads data from Excel files.</li>
 *   <li>{@code BuildInfoReader} – Extracts information about software builds.</li>
 *   <li>{@code NCObjectReader} – Reads objects from a network component or system.</li>
 *   <li>{@code FTPReader} – Retrieves files or data from an FTP server.</li>
 *   <li>{@code FileReader} – Reads raw file data.</li>
 *   <li>{@code DnRReader} – Reads data from a "Dump and Replay" source.</li>
 *   <li>{@code CassandraReader} – Reads from a Cassandra NoSQL database.</li>
 *   <li>{@code VCSReader} – Retrieves data from a version control system.</li>
 *   <li>{@code RESTReader} – Accesses data from a RESTful web service.</li>
 * </ul>
 */
public enum ReaderType {
    SQLReader,
    ITFReader,
    SQLListReader,
    CLIReader,
    ExcelFileReader,
    BuildInfoReader,
    NCObjectReader,
    FTPReader,
    FileReader,
    DnRReader,
    CassandraReader,
    VCSReader,
    RESTReader
}
