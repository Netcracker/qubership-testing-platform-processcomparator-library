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

package org.qubership.automation.pc.core.utils.jsondiff;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * Enumeration representing different types of operations that can be performed
 * in a data or object transformation context.
 *
 * <p>
 * This enum includes standard operations such as add, remove, replace, move,
 * and a special case for type mismatches.
 *
 * <p>
 * Each operation has an associated RFC-compliant name, which can be used for
 * parsing or matching incoming operation descriptors in external formats (e.g., JSON Patch).
 * </p>
 * <h2>Defined Operations:</h2>
 * <ul>
 *   <li>{@code ADD} – Adds a new element or value.</li>
 *   <li>{@code REMOVE} – Removes an existing element.</li>
 *   <li>{@code REPLACE} – Replaces a value with a new one.</li>
 *   <li>{@code TYPE_NOT_MATCHED} – Indicates a mismatch in expected value type.</li>
 *   <li>{@code MOVE} – Moves a value from one location to another.</li>
 * </ul>
 *
 * @see #fromRfcName(String)
 * @see #rfcName()
 */
enum Operation {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    TYPE_NOT_MATCHED("type_not_matched"),
    MOVE("move");

    private static final  Map<String, Operation> OPS = ImmutableMap.of(
            ADD.rfcName, ADD,
            REMOVE.rfcName, REMOVE,
            REPLACE.rfcName, REPLACE,
            TYPE_NOT_MATCHED.rfcName, TYPE_NOT_MATCHED,
            MOVE.rfcName, MOVE
            );

    private String rfcName;

    Operation(String rfcName) {
        this.rfcName = rfcName;
    }

    public static Operation fromRfcName(String rfcName) {
        checkNotNull(rfcName, "rfcName cannot be null");
        return checkNotNull(OPS.get(rfcName.toLowerCase()), "unknown / unsupported operation %s", rfcName);
    }

    public String rfcName() {
        return this.rfcName;
    }
}
