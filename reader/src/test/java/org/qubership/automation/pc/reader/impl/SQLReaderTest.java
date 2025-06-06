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

package org.qubership.automation.pc.reader.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.models.Table;
import org.qubership.automation.pc.models.TablesList;

public class SQLReaderTest {

    SQLReader reader = new SQLReader();

    @Test
    public void convertResultToSimpleData_givenCsvParameter_saveInCsvFormat() {
        SQLReaderConfiguration configuration = new SQLReaderConfiguration();
        SQLReaderConfiguration.Script script = configuration.new Script();
        HashMap<String, String> fieldTypes = new HashMap<>();
        fieldTypes.put("*", "CSV");
        script.fieldTypes.put("*", fieldTypes);
        configuration.setScripts(Arrays.asList(script));
        reader.setLocalConfiguration(configuration);
        TablesList tablesList = new TablesList();
        Table table = new Table("CSV");
        table.headers.addAll(Arrays.asList("Header1", "Header2"));
        Table.TableRow row1 = new Table.TableRow();
        row1.addAll(Arrays.asList("r1c1", "r1c2"));
        table.rows.add(row1);
        tablesList.add(table);
        String expectedData = "\"Header1\",\"Header2\"\n"
                + "\"r1c1\",\"r1c2\"\n";

        List<Data> dataList = reader.convertResultToSimpleData(table.headers, table, script.fieldTypes);

        Assertions.assertEquals(expectedData, DataContentConverter.toString(dataList.get(0)));
    }
}
