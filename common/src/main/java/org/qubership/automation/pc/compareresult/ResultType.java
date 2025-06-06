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

package org.qubership.automation.pc.compareresult;

/**
 * Represents the result type of comparison operation or data evaluation step.
 * </p>
 * Used to classify the outcome of individual comparisons or processing results.
 * <ul>
 *   <li>{@code HIDDEN} – Internal use only; set in XML highlighter to align MISSED/EXTRA nodes.</li>
 *   <li>{@code SUCCESS} – Operation completed successfully.</li>
 *   <li>{@code SKIPPED} – Step or item was intentionally skipped.</li>
 *   <li>{@code IGNORED} – Differences were ignored by configuration or rule.</li>
 *   <li>{@code IDENTICAL} – Data is exactly the same.</li>
 *   <li>{@code PASSED} – Comparison passed according to rules.</li>
 *   <li>{@code SIMILAR} – Data is similar but not identical.</li>
 *   <li>{@code BROKEN_STEP_INDEX} – Indicates an invalid or broken step reference.</li>
 *   <li>{@code CHANGED} – Significant change detected.</li>
 *   <li>{@code MODIFIED} – Minor or acceptable modification found.</li>
 *   <li>{@code AR_MISSED} – Actual result is missing.</li>
 *   <li>{@code ER_MISSED} – Expected result is missing.</li>
 *   <li>{@code EXTRA} – Extra data was found in actual result.</li>
 *   <li>{@code MISSED} – Expected data is missing in actual result.</li>
 *   <li>{@code FAILED} – Comparison failed.</li>
 *   <li>{@code ERROR} – Error occurred during processing.</li>
 * </ul>
 */
public enum ResultType {
    HIDDEN, // This result is ONLY set in XML highlighter on the opposite side of MISSED/EXTRA nodes (in order to align corresponding nodes vertically)
    SUCCESS,
    SKIPPED,
    IGNORED,
    IDENTICAL,
    PASSED,
    SIMILAR,
    BROKEN_STEP_INDEX,
    CHANGED,
    MODIFIED,
    AR_MISSED,
    ER_MISSED,
    EXTRA,
    MISSED,
    FAILED,
    ERROR
}
