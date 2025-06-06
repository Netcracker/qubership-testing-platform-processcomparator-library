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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import org.qubership.automation.pc.models.table.ComparableTable;

public class ComparableTableTest {

    private Gson gson;

    @BeforeEach
    public void setUp() throws Exception {
        gson = new Gson();
    }

    @Test
    public void comparableTable_fromTable_returnsCorrectModel() throws IOException {
        Table commonTable = gson.fromJson(getJsonFromFile("src/test/resources/table/test_table.json"),
                Table.class);
        ComparableTable expectedTable = gson.fromJson(getJsonFromFile("src/test/resources/table/comparable_table.json"),
                ComparableTable.class);

        ComparableTable actualTable = ComparableTable.fromTable(commonTable);

        Assertions.assertEquals(expectedTable, actualTable);
    }

    private String getJsonFromFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }
}
