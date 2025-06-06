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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

public class JsonComparatorUtils {

    /**
     * Convert all keys(fields) to lowercase.
     * If an object contains keys with the same lowercase spelling,
     * then only the last one will remain, all the others will be deleted.
     * @param node {@link JsonNode} that may contain keys in different cases.
     *     After execution, the node will contain keys only in lowercase.
     */
    public static void keysToLowercase(JsonNode node) {
        if (Objects.isNull(node) || !node.isContainerNode()) {
            return;
        }
        if (JsonNodeType.OBJECT.equals(node.getNodeType())) {
            Iterator<String> fieldIterator = node.fieldNames();
            Set<String> fieldsToRemove = new HashSet<>();
            Map<String, String> lowercaseNames = new LinkedHashMap<>();
            while (fieldIterator.hasNext()) {
                String field = fieldIterator.next();
                if (lowercaseNames.containsKey(field.toLowerCase())) {
                    fieldsToRemove.add(lowercaseNames.get(field.toLowerCase()));
                }
                lowercaseNames.put(field.toLowerCase(), field);
            }
            lowercaseNames.forEach((lowercaseName, originalName) -> {
                if (!lowercaseName.equals(originalName)) {
                    ((ObjectNode) node).set(lowercaseName, node.get(originalName));
                    fieldsToRemove.remove(lowercaseName);
                    fieldsToRemove.add(originalName);
                }
            });
            ((ObjectNode) node).remove(fieldsToRemove);
        }
        node.iterator().forEachRemaining(JsonComparatorUtils::keysToLowercase);
    }

    /**
     * Replace key names in json path with lowercase.
     * Does not change expressions corresponding to values.
     * @param jsonPath json path that may contain keys written in different cases.
     * @return jsonPath with keys in lowercase.
     */
    public static String jsonPathToLowercase(String jsonPath) {
        if (Objects.isNull(jsonPath)) {
            return null;
        }
        StringBuilder lowercasePath = new StringBuilder(jsonPath);
        Matcher m = Pattern.compile("[$@][.\\[][^\\(\\)?=<>~\\\\\\/]*").matcher(jsonPath);
        while (m.find()) {
            lowercasePath.replace(m.start(), m.end(), m.group().toLowerCase());
        }
        return lowercasePath.toString();
    }

    /**
     * Replace key names in all json paths with lowercase.
     * @param paths collection of json paths.
     * @return {@link List} of json path with replaced keys.
     */
    public static List<String> jsonPathsToLowercase(Collection<String> paths) {
        return paths.stream()
                .map(JsonComparatorUtils::jsonPathToLowercase)
                .collect(Collectors.toList());
    }

    /**
     * Search for content in a string by a given path.
     * @param jsonContent original string in json format.
     * @param jsonPath jsonPath for reading. If missing, the entire contents will be returned.
     * @param keysCaseInsensitive determines whether to ignore the case. If true, all keys are converted to lowercase.
     * @return search result as optional of JsonNode.
     * @throws IOException If there are any problems with json parsing.
     */
    public static Optional<JsonNode> readJsonNodeFromString(String jsonContent, String jsonPath,
                                                            boolean keysCaseInsensitive) throws IOException {
        Optional<JsonNode> result;
        ObjectMapper objectMapper = new ObjectMapper();
        if (keysCaseInsensitive) {
            JsonNode json = objectMapper.readTree(jsonContent);
            JsonComparatorUtils.keysToLowercase(json);
            if (!Strings.isNullOrEmpty(jsonPath)) {
                jsonPath = JsonComparatorUtils.jsonPathToLowercase(jsonPath);
                json = JsonPath.using(Configuration.defaultConfiguration()
                                .jsonProvider(new JacksonJsonNodeJsonProvider())
                                .addOptions(Option.SUPPRESS_EXCEPTIONS))
                        .parse(json).read(jsonPath);
            }
            result = Optional.ofNullable(json);
        } else if (Strings.isNullOrEmpty(jsonPath)) {
            result = Optional.ofNullable(objectMapper.readTree(jsonContent));
        } else {
            Configuration conf = Configuration
                    .defaultConfiguration()
                    .jsonProvider(new JacksonJsonNodeJsonProvider())
                    /*Option.ALWAYS_RETURN_LIST,*/
                    .addOptions(Option.SUPPRESS_EXCEPTIONS);
            result = Optional.ofNullable(JsonPath.using(conf).parse(jsonContent).read(jsonPath));
        }
        return result;
    }
}
