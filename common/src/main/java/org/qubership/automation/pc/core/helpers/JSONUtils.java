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

package org.qubership.automation.pc.core.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;

/**
 * Utility class for working with JSON data and JSONPath representations.
 * </p>
 * Provides methods to construct status messages in JSON format,
 * convert object paths to JSONPath strings, and simplify JSONPath expressions.
 */
public class JSONUtils {

    public static JsonObject statusMessage(int statusCode) {
        return statusMessage(statusCode, "");
    }

    public static JsonObject statusMessage(int statusCode, String message) {
        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("statusCode", statusCode);        
        if (!message.isEmpty()) {
            message = message.replace("\"", "\\\"");
            resultObject.addProperty("statusMessage", message);
        }        
        return resultObject;
    }

    public static String listToJsonPath(List<Object> path) {
        StringBuilder result = new StringBuilder(path.isEmpty() ? "" : "$");
        for (Object element : path) {
            result.append("[")
                    .append(element instanceof Integer ? element : "'" + element + "'")
                    .append("]");
        }
        return result.toString();
    }

    public static String listToJsonPath(Stack<String> path) {
        List<Object> list = new ArrayList<>(path);
        return listToJsonPath(list);
    }

    public static String convertJsonPathToSimplePath(String jsonPath) {
        if (StringUtils.isNotBlank(jsonPath)) {
            jsonPath = jsonPath.startsWith("$") ? jsonPath.replaceFirst("\\$", "") : jsonPath;
            jsonPath = jsonPath.replaceAll("'", "").replaceAll("\\]", "").replaceAll("\\[", "/");
        }
        return jsonPath;
    }
}
