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

import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;

/**
 * Enumeration representing JSON node types as defined in JSON schemas and
 * commonly encountered during JSON parsing or validation.
 * </p>
 * Each enum constant corresponds to a basic JSON type such as {@code object}, {@code array},
 * {@code string}, etc., and is mapped internally to a {@link JsonToken} to facilitate
 * compatibility with Jackson's streaming API.
 * </p>
 * This enum is typically used to determine the type of a {@link JsonNode} and is useful
 * in schema validation, type-checking, or dynamic processing of JSON content.
 * </p>
 * <h2>Supported Node Types:</h2>
 * <ul>
 *   <li>{@link #ARRAY} – JSON arrays (e.g., {@code [1, 2, 3]}).</li>
 *   <li>{@link #BOOLEAN} – JSON booleans ({@code true}, {@code false}).</li>
 *   <li>{@link #INTEGER} – JSON integers (e.g., {@code 42}).</li>
 *   <li>{@link #NUMBER} – JSON numbers including decimals (e.g., {@code 3.14}).</li>
 *   <li>{@link #NULL} – JSON null value.</li>
 *   <li>{@link #OBJECT} – JSON objects (e.g., {@code {"key": "value"}}).</li>
 *   <li>{@link #STRING} – JSON strings (e.g., {@code "text"}).</li>
 * </ul>
 *
 * @see com.fasterxml.jackson.core.JsonToken
 * @see com.fasterxml.jackson.databind.JsonNode
 * @see #getNodeType(JsonNode)
 */
enum NodeType {
    /**
     * Array nodes.
     */
    ARRAY("array"),
    /**
     * Boolean nodes.
     */
    BOOLEAN("boolean"),
    /**
     * Integer nodes.
     */
    INTEGER("integer"),
    /**
     * Number nodes (ie, decimal numbers).
     */
    NULL("null"),
    /**
     * Object nodes.
     */
    NUMBER("number"),
    /**
     * Null nodes.
     */
    OBJECT("object"),
    /**
     * String nodes.
     */
    STRING("string");

    /**
     * The name for this type, as encountered in a JSON schema.
     */
    private final String name;

    private static final Map<JsonToken, NodeType> TOKEN_MAP
            = new EnumMap<JsonToken, NodeType>(JsonToken.class);

    static {
        TOKEN_MAP.put(JsonToken.START_ARRAY, ARRAY);
        TOKEN_MAP.put(JsonToken.VALUE_TRUE, BOOLEAN);
        TOKEN_MAP.put(JsonToken.VALUE_FALSE, BOOLEAN);
        TOKEN_MAP.put(JsonToken.VALUE_NUMBER_INT, INTEGER);
        TOKEN_MAP.put(JsonToken.VALUE_NUMBER_FLOAT, NUMBER);
        TOKEN_MAP.put(JsonToken.VALUE_NULL, NULL);
        TOKEN_MAP.put(JsonToken.START_OBJECT, OBJECT);
        TOKEN_MAP.put(JsonToken.VALUE_STRING, STRING);

    }

    NodeType(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static NodeType getNodeType(final JsonNode node) {
        final JsonToken token = node.asToken();
        final NodeType ret = TOKEN_MAP.get(token);
        Preconditions.checkNotNull(ret, "unhandled token type " + token);
        return ret;
    }
    
}
