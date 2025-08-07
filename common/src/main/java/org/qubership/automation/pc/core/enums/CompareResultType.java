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
 * Defines the type of comparison result based on the context or data structure.
 * <p>
 * Used to distinguish different comparison scenarios or levels of granularity.
 * <ul>
 *   <li>{@code SIMPLE} – Basic comparison of standalone values or entities.</li>
 *   <li>{@code PROCESS} – Comparison of complex process models or workflows.</li>
 *   <li>{@code TESTCASE} – Comparison result specific to test cases.</li>
 *   <li>{@code STEP} – Comparison of individual steps within a process or test case.</li>
 * </ul>
 */
public enum CompareResultType {
    SIMPLE,
    PROCESS,
    TESTCASE,
    STEP
}
