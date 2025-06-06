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

package org.qubership.automation.pc.core.utils;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

public class JsonComparatorUtilsTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void keysToLowercase_givenSimpleJsonObject_canConvertKeyToLowercase() {
        JsonNode node = mapper.createObjectNode()
                .set("ParameterName", new TextNode("Value"));

        JsonComparatorUtils.keysToLowercase(node);

        assertEquals(1, node.size(), "The number of elements should not change");
        assertTrue(node.has("parametername"), "The element name should change to lower case");
        assertEquals("Value", node.get("parametername").textValue(), "The value should not change");
    }

    @Test
    public void keysToLowercase_givenJsonWithDuplicatedKeys_canRemoveDuplicateNodesExceptTheLastOnes()
            throws IOException {
        String originalString = "{\"name\":\"1\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NAME\":\"3\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NamE\":[{\n"
                + "        \"obj\":\"1\",\n"
                + "        \"OBJ\":\"3\"\n"
                + "    },{\n"
                + "        \"Obj\":\"4\",\n"
                + "        \"obj\":\"5\"\n"
                + "    }]\n"
                + "}";
        JsonNode node = mapper.readTree(originalString);
        String expectedResult = "{\"name\":[{\"obj\":\"3\"},{\"obj\":\"5\"}]}";

        JsonComparatorUtils.keysToLowercase(node);

        String result = node.toString();
        assertEquals(expectedResult, result, "The result should contain keys in lowercase. The value must "
                + "be taken from the last of all duplicates.");
    }

    @Test
    public void jsonPathToLowercase_givenDifferentJsonPaths_canConvertAllKeysToLowercaseExceptExpressions() {
        List<String> jsonPaths = Arrays.asList(
                "$..book[?(@.author =~ /.*REES/i)]",
                 "$..BOOk.Book2[?(@.author =~ /.*R$EES/i)]",
                 "$..boOk[?(@.author =~ /.?*@REES/i)]",
                 "$..book[?(@.price <= $['expensive'])]",
                 "$..BOOK.book.book.*[?(@.pRice >= $['eXPENSive'])]",
                 "$.bOok[?(@.pRice == 'EXPEN$SI VE?qwe')]",
                 "$..bOok[?(@.pRice=='EXPEN SI VE')]",
                 "$..Book[?(@.pRice >=$[ 'eXPENSive' ])]",
                 "$.store.book[?(@.price < 10)]",
                 "$.store.book[*].author",
                 "$..book[-2:]"
        );

        List<String> expectedPaths = Arrays.asList(
                "$..book[?(@.author =~ /.*REES/i)]",
                "$..book.book2[?(@.author =~ /.*R$EES/i)]",
                "$..book[?(@.author =~ /.?*@REES/i)]",
                "$..book[?(@.price <= $['expensive'])]",
                "$..book.book.book.*[?(@.price >= $['expensive'])]",
                "$.book[?(@.price == 'EXPEN$SI VE?qwe')]",
                "$..book[?(@.price=='EXPEN SI VE')]",
                "$..book[?(@.price >=$[ 'expensive' ])]",
                "$.store.book[?(@.price < 10)]",
                "$.store.book[*].author",
                "$..book[-2:]"
        );

        List<String> results =
                jsonPaths.stream().map(JsonComparatorUtils::jsonPathToLowercase).collect(Collectors.toList());

        assertEquals(expectedPaths, results, "All paths must have keys converted to lowercase.");
    }

    @Test
    public void readJsonNodeFromString_givenJsonStringAndJsonPathAndKeyCaseInsensitive_canExtractContent()
            throws IOException {
        String originalString = "{\"name\":\"1\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NAME\":\"3\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NamE\":[{\n"
                + "        \"obj\":\"1\",\n"
                + "        \"OBJ\":\"3\"\n"
                + "    },{\n"
                + "        \"Obj\":\"4\",\n"
                + "        \"obj\":\"5\"\n"
                + "    }]\n"
                + "}";
        String jsonPath = "$.Name.*.oBj";
        JsonNode expectedNode = mapper.createArrayNode()
                .add("3")
                .add("5");

        Optional<JsonNode> result = JsonComparatorUtils.readJsonNodeFromString(originalString, jsonPath, true);

        assertTrue(result.isPresent(), "Node should be present in the result");
        assertEquals(expectedNode, result.get(), "Expected array node with 2 strings: '3', '5'");
    }
}
