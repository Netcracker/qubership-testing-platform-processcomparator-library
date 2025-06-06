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
 * Specifies the format or structure of the data being compared.
 * </p>
 * Indicates how the data should be interpreted, processed, or displayed.
 * <ul>
 *   <li>{@code PRIMITIVES} – Basic data types such as strings, numbers, booleans, etc.</li>
 *   <li>{@code XML} – XML-formatted data.</li>
 *   <li>{@code MASKED_XML} – XML data with masked or redacted content.</li>
 *   <li>{@code JSON} – JSON-formatted data.</li>
 *   <li>{@code CSV} – Comma-separated values.</li>
 *   <li>{@code EXCEL} – Microsoft Excel spreadsheet data.</li>
 *   <li>{@code PLAIN_TEXT} – Unstructured plain text.</li>
 *   <li>{@code FULL_TEXT} – Full-text documents, potentially large and formatted.</li>
 *   <li>{@code BITMAP} – Bitmap or binary image data.</li>
 *   <li>{@code TASK_LIST} – Structured task or action list.</li>
 *   <li>{@code XSD} – XML Schema Definition documents.</li>
 *   <li>{@code TABLE} – Tabular data not bound to a specific format like CSV or Excel.</li>
 * </ul>
 */
public enum DataContentType {
    PRIMITIVES,
    XML,
    MASKED_XML,
    JSON,
    CSV,
    EXCEL,
    PLAIN_TEXT,
    FULL_TEXT,
    BITMAP,
    TASK_LIST,
    XSD,
    TABLE
}
