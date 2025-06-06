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

package org.qubership.automation.pc.models;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * A specialized list for holding {@link Table} objects, with utility methods for JSON deserialization.
 * </p>
 * This class extends {@link ArrayList} to represent a collection of tables and provides support for
 * creating such a list from various JSON formats. It supports:
 * <ul>
 *   <li>A plain JSON array of tables</li>
 *   <li>An object with a {@code "tables"} array field</li>
 *   <li>An object with {@code "headers"} and {@code "rows"} fields representing a single table</li>
 * </ul>
 * </p>
 * This flexibility ensures compatibility with legacy and variant JSON formats (e.g. SQL query results).
 */
public class TablesList extends ArrayList<Table> {

    public static TablesList getTableListFromJson(String jsonString) {
        Gson gson = new Gson();
        JsonElement jsElem = new JsonParser().parse(jsonString);
        TablesList tableList = null;
        if (jsElem.isJsonArray()) {
            tableList = gson.fromJson(jsonString, TablesList.class);
        } else if (jsElem.isJsonObject() && jsElem.getAsJsonObject().has("tables")) {
            tableList = gson.fromJson(jsElem.getAsJsonObject().getAsJsonArray("tables"), TablesList.class);
            // magic due to structure changes of TablesList class
        } else if (jsElem.isJsonObject() && jsElem.getAsJsonObject().has("headers")
                && jsElem.getAsJsonObject().has("rows")) { // for SQL Reader with TABLE type of val params
            tableList = new TablesList();
            Table table = gson.fromJson(jsElem, Table.class); 
            if (StringUtils.isBlank(table.name)) {
                table.name = "Query result";
            }            
            tableList.add(table);
        }
        return tableList;
    }

}
