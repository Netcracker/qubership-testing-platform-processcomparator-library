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

package org.qubership.automation.pc.models.table;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import lombok.Data;

/**
 * Used in SVP tool. Called 'fat' because every row is map of {@code <header, value>}
 * so data is duplicated many times.
 */
@Data
public class FatTable {
    private String name;
    private List<String> headers;
    private List<Map<String, String>> rows;

    /**
     * Parses JSON string into FatTable object.
     */
    public static FatTable fromString(String json) {
        return new Gson().fromJson(json, FatTable.class);
    }

    public Map<String, String> getRow(int i) {
        return rows.get(i);
    }
}
