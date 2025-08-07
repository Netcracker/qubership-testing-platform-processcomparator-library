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
 * Defines the type of data being compared.
 * <p>
 * Used to indicate the structure or role of the data in the comparison process.
 * <ul>
 *   <li>{@code PROCESS} – Classic Process Comparator Data Model.</li>
 *   <li>{@code PROCESS_STEP} – Represents a single process step.</li>
 *   <li>{@code SIMPLE} – One data item corresponds to one record.
 *                        In the process model, used as a step parameter.</li>
 *   <li>{@code CONTEXT_PARAMETER} – Used only in {@code ProcessComparatorValidator} (class in BV),
 *                        within the {@code buildRequest} method, to provide additional information
 *                        to the {@code ComparatorManager}.</li>
 * </ul>
 */
public enum DataType {
    PROCESS,            // Classic Process Comparator Data Model
    PROCESS_STEP,       // Process Step
    SIMPLE,             // One Data - One Record. For Process Model using as step parameter
    CONTEXT_PARAMETER   // Set only in ProcessComparatorValidator (class in BV)
    // in 'buildRequest' method - in order to give extra information to ComparatorManager
}
