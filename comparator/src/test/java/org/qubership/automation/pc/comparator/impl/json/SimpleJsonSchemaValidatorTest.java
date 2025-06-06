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

package org.qubership.automation.pc.comparator.impl.json;

import static org.qubership.automation.pc.comparator.impl.json.SimpleJsonSchemaValidator.validateDocument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class SimpleJsonSchemaValidatorTest {

    @Test
    public void simpleJsonSchemaValidation_givenObjectInSchemaAndStringWithTheSameNameInAr_returnModified()
            throws ComparatorException {
        String ar = "{\n" +
                "    \"obj\":\"string\"\n" +
                "}";
        String schema = "{\n" +
                "    \"obj\": {\n" +
                "        \"inner\":\"123\"\n" +
                "    }\n" +
                "}";

        List<DiffMessage> diffs = validateDocument(ar, schema);

        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).getResult(), ResultType.MODIFIED);
    }

    @Test
    public void simpleJsonSchemaValidation_givenObjectInSchemaAndEmptyAr_returnMissed() throws ComparatorException {
        String ar = "{}";
        String schema = "{\n" +
                "    \"obj\": {\n" +
                "        \"inner\":\"123\"\n" +
                "    }\n" +
                "}";

        List<DiffMessage> diffs = validateDocument(ar, schema);

        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).getResult(), ResultType.MISSED);
    }

    @Test
    public void simpleJsonSchemaValidation_givenEmptyArraysInTheSchemaAndAr_returnIdenticalWithoutExceptions()
            throws ComparatorException {
        String ar = "{\n" +
                "    \"array\":[]\n" +
                "}";
        String schema = ar;

        List<DiffMessage> diffs = validateDocument(ar, schema);

        assertTrue(diffs.isEmpty());
    }

    @Test
    public void simpleJsonSchemaValidation_givenNotEmptySchemaArrayWithMinValue1AndEmptyArrayInAr_returnModifiedDiff()
            throws ComparatorException {
        String ar = "{\n" +
                "    \"array\":[]\n" +
                "}";
        String schema = "{\n" +
                "    \"array[1..5]\":[1]\n" +
                "}";

        List<DiffMessage> diffs = validateDocument(ar, schema);

        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).getResult(), ResultType.MODIFIED);
    }

    @Test
    public void simpleJsonSchemaValidation_givenNotEmptySchemaArrayWithMaxValue1AndArrayContaining2ElementsInAr_returnModifiedDiff()
            throws ComparatorException {
        String ar = "{\n" +
                "    \"array\":[1, 1]\n" +
                "}";
        String schema = "{\n" +
                "    \"array[0..1]\":[1]\n" +
                "}";

        List<DiffMessage> diffs = validateDocument(ar, schema);

        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).getResult(), ResultType.MODIFIED);
    }

    @Test
    public void simpleJsonSchemaValidation_givenEmptySchemaArrayAndArrayContaining1ElementInAr_returnExtraDiff()
            throws ComparatorException {
        String ar = "{\n" +
                "    \"array\":[1]\n" +
                "}";
        String schema = "{\n" +
                "    \"array\":[]\n" +
                "}";

        List<DiffMessage> diffs = validateDocument(ar, schema);

        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).getResult(), ResultType.EXTRA);
    }

    @Test
    public void simpleJsonSchemaValidation_givenEmptySchemaArrayWithMin0Max1ValuesAndArrayContaining1ElementInAr_returnExtraDiff()
            throws ComparatorException {
        String ar = "{\n" +
                "    \"array\":[1]\n" +
                "}";
        String schema = "{\n" +
                "    \"array[0..1]\":[]\n" +
                "}";

        List<DiffMessage> diffs = validateDocument(ar, schema);

        assertEquals(diffs.size(), 1);
        assertEquals(diffs.get(0).getResult(), ResultType.EXTRA);
    }
}
