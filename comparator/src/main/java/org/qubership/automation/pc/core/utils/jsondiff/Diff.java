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

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a single difference (or "diff") between two JSON documents.
 *
 * <p>
 * A {@code Diff} instance encapsulates a single change operation (such as {@code add}, {@code remove},
 * {@code replace}, or {@code move}) as part of a JSON diffing or patching process. This class is typically
 * used in the context of JSON patch generation (RFC 6902).
 * </p>
 *
 * <p>
 * Each diff contains:
 * <ul>
 *   <li>An {@link Operation} indicating the type of change (e.g., {@code ADD}, {@code REMOVE}).</li>
 *   <li>A {@code path} describing where in the JSON structure the change should occur.</li>
 *   <li>An optional {@code value} representing the data to add or replace.</li>
 *   <li>An optional {@code toPath} used only for {@code MOVE} operations.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class is immutable except for the {@code toPath} field, which is only applicable in special cases.
 * </p>
 *
 * @see Operation
 * @see com.fasterxml.jackson.databind.JsonNode
 */
class Diff {
    private final Operation operation;
    private final List<Object> path;
    private final JsonNode value;
    private List<Object> toPath; //only to be used in move operation

    Diff(Operation operation, List<Object> path, JsonNode value) {
        this.operation = operation;
        this.path = path;
        this.value = value;
    }

    Diff(Operation operation, List<Object> fromPath, JsonNode value, List<Object> toPath) {
        this.operation = operation;
        this.path = fromPath;
        this.value = value;
        this.toPath = toPath;
    }

    public Operation getOperation() {
        return operation;
    }

    public List<Object> getPath() {
        return path;
    }

    public JsonNode getValue() {
        return value;
    }

    public static Diff generateDiff(Operation replace, List<Object> path, JsonNode target) {
        return new Diff(replace, path, target);
    }

    List<Object> getToPath() {
        return toPath;
    }
    
}
