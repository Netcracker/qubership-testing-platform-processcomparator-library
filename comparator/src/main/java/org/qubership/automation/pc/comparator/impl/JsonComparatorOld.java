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

package org.qubership.automation.pc.comparator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.utils.jsondiff.JsonDiffTuned;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * JsonComparatorOld provides a flexible and extensible comparator for JSON data.
 * </p>
 * This comparator supports a variety of comparison modes including:
 * <ul>
 *     <li>Ignoring array element order</li>
 *     <li>Using object primary keys to match elements</li>
 *     <li>JSONPath-based comparison subtrees</li>
 *     <li>Schema validation of actual results (AR)</li>
 *     <li>Regular expression matching within expected result (ER)</li>
 *     <li>Ignoring specified properties or entire objects</li>
 * </ul>
 *
 * </p>Configuration parameters include:
 * <ul>
 *     <li>{@code ignoreArraysOrder} — ignore order of array elements</li>
 *     <li>{@code objectPrimaryKey} — mapping of object paths to their primary key fields</li>
 *     <li>{@code readByPath} — JSONPath expressions to extract sub-nodes for comparison</li>
 *     <li>{@code ignoreProperties} — paths to ignore in comparison</li>
 *     <li>{@code validateSchema} — a JSON schema string to validate actual results</li>
 *     <li>{@code disableTypeCheckIfRegexp} — disables type check if ER uses regex</li>
 *     <li>{@code findERInAR} — checks if ER exists as substring inside AR</li>
 * </ul>
 * </p>
 *
 * <p>This class is backward-compatible and intended primarily for legacy use.
 * New implementations are encouraged to use more modern comparators if available.</p>
 *
 * @see AbstractComparator
 * @see JsonDiffTuned
 * @see Parameters
 * @see DiffMessage
 */
public class JsonComparatorOld extends AbstractComparator {

    // true / false (default)
    public static final String PARAMETER_NAME_IGNORE_ARRAY_ELEMENTS_ORDER = "ignoreArraysOrder";
    // Value is like "/contactMethods/type" where "/contactMethods" - xpath to object,
    // "type" - primary key property of the object
    public static final String PARAMETER_NAME_OBJECT_PRIMARY_KEY = "objectPrimaryKey";
    // Value is like "$.store.book[*].author" or "$.store.book[?(@.price > 22)].title"
    // or "$.store" (see https://www.leveluplunch.com/java/examples/parse-json-elements-with-jsonpath/ etc.)
    public static final String PARAMETER_NAME_READ_BY_PATH = "readByPath";
    // Value is like "contactMethods/type" (To ignore property 'type' of 'contactMethods' objects)
    // or "contactMethods" (To totally ignore 'contactMethods' objects)
    // or "*/type" (To ignore property 'type' of all objects)
    public static final String PARAMETER_NAME_IGNORE_PROPERTIES = "ignoreProperties";
    // Value is String representation of jsonSchema. er and ar are validated separately via the schema
    public static final String PARAMETER_NAME_VALIDATE_SCHEMA = "validateSchema";
    public static final String PARAMETER_NAME_DIFF_SUMMARY_TEMPLATE = "diffSummaryTemplate";
    public static final String PARAMETER_DISABLE_TYPE_CHECK_IF_REGEXP = "disableTypeCheckIfRegexp";
    //New rule for CloudBSS
    public static final String PARAMETER_FIND_ER_IN_AR = "findERInAR";

    public static final String DIFF_MACROS_ER_PATH = "ERPATH";
    public static final String DIFF_MACROS_AR_PATH = "ARPATH";
    public static final String DIFF_MACROS_VALUE = "VALUE";
    public static final String DIFF_MACROS_SUMMARY = "SUMMARY";
    public static final String DEFAULT_DIFF_SUMMARY_TEMPLATE = "{" + DIFF_MACROS_SUMMARY + "}";

    private boolean disableTypeCheckIfRegexp = true;
    private boolean ignoreArraysOrder = false;
    private Map<String, String> objectPrimaryKeysMap;
    private List<String> readByPath;
    private List<String> ignoreProperties;
    private JsonDiffTuned jsonDiffTuned = new JsonDiffTuned();
    private List<FilterObjectProperty> ignorePropertiesList;
    private String schemaContent;
    private String diffSummaryTemplate;
    private boolean ignoreExtra = false;
    private boolean findEeInAr = false;


    private ResultType status = ResultType.IDENTICAL;

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        List<DiffMessage> diffMessages = new ArrayList<>();
        String erContent;
        String arContent;

        if (configuration.get(PARAMETER_NAME_VALIDATE_SCHEMA) != null && this.arHasInvalidJson(ar)) {
            diffMessages.add(new DiffMessage(1, er, ar, ResultType.FAILED));
            return diffMessages;
        }

        // Currently (20/02/2017) there are no actions to do if er or ar are empty
        // Thats why we simply return empty list of diffMessages in this case
        if (StringUtils.isBlank(er) && StringUtils.isBlank(ar)) {
            return diffMessages;
        } else {
            erContent = trimJson(er);
            arContent = trimJson(ar);
        }

        try {
            getConfigurationParameters(configuration);

            //New rule for CloudBSS
            if (findEeInAr) {
                // String prettyJsonString = "{ \"Hello\" : \"world\"}";
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNodeER = objectMapper.readValue(er, JsonNode.class);
                String newER = jsonNodeER.toString();
                JsonNode jsonNodeAR = objectMapper.readValue(er, JsonNode.class);
                String newAR = jsonNodeAR.toString();
                //String escapedAr="(.|\\n)*"+ StringEscapeUtils.escapeJson(jsonNodeAR.toString()) + "(.|\\n)*";
                //boolean matches = newER.matches(escapedAr);

                String erWithoutWhitespaces = newER.replaceAll("\\s", "");
                String arWithoutWhitespaces = newAR.replaceAll("\\s", "");

                boolean contains = erWithoutWhitespaces.contains(arWithoutWhitespaces);
                if (contains) {
                    status = ResultType.IDENTICAL;
                    return diffMessages;
                } else {
                    //int orderId, String expected, String actual, ResultType result
                    DiffMessage message = new DiffMessage(1, er, ar, ResultType.FAILED);
                    diffMessages.add(message);
                    status = ResultType.FAILED;
                    return diffMessages;
                }
            }

            JsonNode jsonNodeER;
            JsonNode jsonNodeAR;
            ObjectMapper mapper = new ObjectMapper();
            boolean isActual = false;
            try {
                if (readByPath.isEmpty()) {
                    jsonNodeER = mapper.readTree(erContent);
                    isActual = true;
                    jsonNodeAR = mapper.readTree(arContent);
                } else {
                    Configuration conf = Configuration
                            .defaultConfiguration()
                            .jsonProvider(new JacksonJsonNodeJsonProvider())
                            .addOptions(/*Option.ALWAYS_RETURN_LIST,*/Option.SUPPRESS_EXCEPTIONS);
                    jsonNodeER = JsonPath.using(conf).parse(erContent).read(readByPath.get(0));
                    isActual = true;
                    jsonNodeAR = JsonPath.using(conf).parse(arContent).read(readByPath.get(0));
                }
            } catch (Exception ex) {
                throw new ComparatorException("Error while parsing input message " + ((isActual)
                        ? "ar" : "er") + ". Probably it is not valid JSON. " + ex.getMessage(), 20000);
            }
            if (schemaContent.isEmpty()) {
                // er/ar comparison
                JsonNode comparisonResult = jsonDiffTuned.asJson(jsonNodeER, jsonNodeAR, ignoreArraysOrder,
                        disableTypeCheckIfRegexp, objectPrimaryKeysMap);
                return formDiffMessages(comparisonResult, jsonNodeER, jsonNodeAR);
            } else {
                Set<ValidationMessage> errors = getValidationErrors(jsonNodeAR, mapper);
                return formDiffMessages(errors, jsonNodeAR);
            }
        } catch (IndexOutOfBoundsException | IOException e) {
            throw new ComparatorException("Error while parsing input message. Probably it is not valid JSON.\n"
                    + e.getMessage(), e);
        } catch (ReaderException ex) {
            throw new ComparatorException("Error while parsing Diff Template");
        }
    }

    private Set<ValidationMessage> getValidationErrors(JsonNode jsonNodeAR, ObjectMapper mapper) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        JsonSchema schema = factory.getSchema(schemaContent);
        Set<ValidationMessage> errors = new HashSet<>() ;
        switch (jsonNodeAR.getNodeType()) {
            case ARRAY: {
                for (int i = 0; i < jsonNodeAR.size(); i++) {
                    JsonNode node = jsonNodeAR.get(i);
                    errors.addAll(schema.validate(node));
                }
                break;
            }
            case OBJECT: {
                errors = schema.validate(jsonNodeAR);
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + jsonNodeAR.getNodeType());
        }
        return errors;
    }

    private String trimJson(String srcJson) {
        if (StringUtils.isBlank(srcJson)) {
            return "{}";
        } else {
            int startPos;
            int endPos;
            int startObject = srcJson.indexOf("{");
            int startArray = srcJson.indexOf("[");
            if (startObject == -1 && startArray == -1) {
                return "{}"; // There is no Json here
            } else {
                if (startObject == -1) {
                    startPos = startArray;
                    endPos = srcJson.lastIndexOf("]");
                } else if (startArray == -1) {
                    startPos = startObject;
                    endPos = srcJson.lastIndexOf("}");
                } else {
                    if (startObject < startArray) {
                        startPos = startObject;
                        endPos = srcJson.lastIndexOf("}");
                    } else {
                        startPos = startArray;
                        endPos = srcJson.lastIndexOf("]");
                    }
                }
                if (endPos == -1) {
                    //return "{}"; // This Json is NOT valid, but ... let comparator throw an exception
                    return srcJson.substring(startPos);
                } else {
                    return srcJson.substring(startPos, endPos + 1);
                }
            }
        }
    }

    private void getConfigurationParameters(Parameters configuration) throws ComparatorException {
        ignoreArraysOrder
                = configuration.getBooleanParameter(PARAMETER_NAME_IGNORE_ARRAY_ELEMENTS_ORDER, ignoreArraysOrder);
        ignoreExtra
                = configuration.getBooleanParameter("ignoreExtra") != null
                ? configuration.getBooleanParameter("ignoreExtra") : false;
        objectPrimaryKeysMap = new HashMap<>();
        List<String> keys = configuration.getParameters(PARAMETER_NAME_OBJECT_PRIMARY_KEY);
        if (keys != null) {
            for (String key : keys) {
                if (!key.isEmpty()) {
                    int i = key.lastIndexOf("/");
                    if (i != -1) {
                        String xpath = key.substring(0, i).trim();
                        String pk = key.substring(i + 1).trim();
                        if (!pk.isEmpty() && !xpath.isEmpty()) {
                            objectPrimaryKeysMap.put(xpath, pk);
                        }
                    }
                }
            }
        }

        findEeInAr = configuration.getBooleanParameter(PARAMETER_FIND_ER_IN_AR, findEeInAr);

        readByPath = configuration.getParameters(PARAMETER_NAME_READ_BY_PATH);
        if (readByPath == null) {
            readByPath = new ArrayList<>();
        }
        ignoreProperties = configuration.getParameters(PARAMETER_NAME_IGNORE_PROPERTIES);
        if (ignoreProperties == null) {
            ignoreProperties = new ArrayList<>();
        }
        ignorePropertiesList = new ArrayList<>();
        for (String item : ignoreProperties) {
            if (checkFilterObjectPropertyStringFormat(item)) {
                ignorePropertiesList.add(new FilterObjectProperty(item.trim()));
            }
        }
        List<String> schemaLines = configuration.getParameters(PARAMETER_NAME_VALIDATE_SCHEMA);
        if (schemaLines == null || schemaLines.isEmpty()) {
            schemaContent = "";
        } else {
            schemaContent = StringUtils.join(schemaLines, '\n');
        }

        this.diffSummaryTemplate = DEFAULT_DIFF_SUMMARY_TEMPLATE;
        if (configuration.containsKey(PARAMETER_NAME_DIFF_SUMMARY_TEMPLATE)) {
            String newDiffTemplate = configuration.get(PARAMETER_NAME_DIFF_SUMMARY_TEMPLATE);
            if (!StringUtils.isEmpty(newDiffTemplate)) {
                this.diffSummaryTemplate = newDiffTemplate;
            }
        }

        if (configuration.containsKey(PARAMETER_DISABLE_TYPE_CHECK_IF_REGEXP)) {
            this.disableTypeCheckIfRegexp
                    = configuration.getBooleanParameter(PARAMETER_DISABLE_TYPE_CHECK_IF_REGEXP, true);
        }
    }

    private List<DiffMessage> formDiffMessages(JsonNode comparisonResult,
                                               JsonNode jsonNodeER,
                                               JsonNode jsonNodeAR) throws JsonProcessingException, ReaderException {
        List<DiffMessage> diffMessages = new ArrayList<>();
        int diffCounter = 1;
        int modified = 0;
        int similar = 0;

        for (int i = 0; i < comparisonResult.size(); i++) {
            JsonNode comparisonNode = comparisonResult.get(i);
            String erPath = comparisonNode.get("path").toString().replaceAll("\"", "");
            String arPath = erPath;

            if (comparisonNode.has("from")) {
                arPath = comparisonNode.get("from").toString().replaceAll("\"", "");
            }

            String value = comparisonNode.get("value").textValue();

            // Prepare Diff Template Macroses
            Map<String, String> macroses = new HashMap<>();
            macroses.put(DIFF_MACROS_ER_PATH, erPath);
            macroses.put(DIFF_MACROS_AR_PATH, arPath);
            macroses.put(DIFF_MACROS_VALUE, value);

            String operation = comparisonNode.get("op").toString().replaceAll("\"", "").toLowerCase();
            DiffMessage diffMessage;
            switch (operation) {
                case "replace":
                    if (erPath.equals("/") || arPath.equals("/")) {
                        macroses.put(DIFF_MACROS_SUMMARY, "er and ar root nodes have different types.");
                        diffMessage = new DiffMessage(diffCounter, erPath, arPath, ResultType.MODIFIED,
                                ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses));
                    } else {
                        macroses.put(DIFF_MACROS_SUMMARY, "Node values are different.");
                        diffMessage = new DiffMessage(diffCounter, erPath, arPath, ResultType.SIMILAR,
                                "Node values are different.");
                        ResultType checkRegexpResult = checkRegexp(jsonNodeER.at(erPath), jsonNodeAR, arPath);
                        if (checkRegexpResult != null) {
                            diffMessage.setResult(checkRegexpResult);
                            macroses.put(DIFF_MACROS_SUMMARY, "Result is changed due to inline-regexp checking.");
                        }
                        String parsedDescr = ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses);
                        diffMessage.setDescription(parsedDescr);
                    }
                    break;
                case "type_not_matched":
                    macroses.put(DIFF_MACROS_SUMMARY, "Nodes have different types.");
                    diffMessage = new DiffMessage(diffCounter, erPath, arPath, ResultType.MODIFIED,
                            ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses));
                    break;
                case "add":
                    macroses.put(DIFF_MACROS_SUMMARY, "ar has extra node(s).");
                    diffMessage = new DiffMessage(diffCounter, "", arPath, ResultType.EXTRA,
                            ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses));
                    if (ignoreExtra) {
                        diffMessage.setResult(ResultType.IDENTICAL);
                    }
                    break;
                case "remove":
                    macroses.put(DIFF_MACROS_SUMMARY, "er node is missed.");
                    diffMessage = new DiffMessage(diffCounter, erPath, "", ResultType.MISSED,
                            ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses));
                    break;
                case "move":
                    erPath = comparisonNode.get("from").toString().replaceAll("\"", "");
                    macroses.put(DIFF_MACROS_ER_PATH, erPath);
                    macroses.put(DIFF_MACROS_SUMMARY, "er and ar nodes have different types and/or structure.");
                    diffMessage = new DiffMessage(diffCounter, erPath, arPath, ResultType.MODIFIED,
                            ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses));
                    break;
                default:
                    continue;
            }

            if (ignoreDifference(diffMessage)) {
                continue;
            }

            diffMessages.add(diffMessage);
            switch (diffMessage.getResult()) {
                case EXTRA:
                case MISSED:
                case MODIFIED:
                    modified++;
                    break;
                case SIMILAR:
                    similar++;
                    break;
                default:
            }
            diffCounter++;
        }
        if (modified > 0) {
            status = ResultType.MODIFIED;
        } else if (similar > 0) {
            status = ResultType.SIMILAR;
        }
        return diffMessages;
    }

    // OverLoaded method formDiffMessages - currently only for json schema validation errors
    private List<DiffMessage> formDiffMessages(Set<ValidationMessage> errors, JsonNode jsonNodeAR) {
        List<DiffMessage> diffMessages = new ArrayList<>();
        int counter = 0;
        for (ValidationMessage err : errors) {
            DiffMessage diff = new DiffMessage();
            diff.setExpected("");
            diff.setActual(err.getPath().replace('$', '/'));
            /* This replacement is due to (temporary?)
            different syntax of json-path in case of json schema validation (rule 'validateSchema') */
            diff.setDescription(err.getMessage());
            diff.setOrderId(++counter);
            diff.setResult(ResultType.FAILED);
            diffMessages.add(diff);
        }

        return diffMessages;
    }

    private boolean ignoreDifference(DiffMessage diffMessage) {
        String expected = diffMessage.getExpected();
        String actual = diffMessage.getActual();
        for (FilterObjectProperty item : ignorePropertiesList) {
            if (expected.matches(item.filterStr) || actual.matches(item.filterStr)) {
                return true;
            }
        }
        return false;
    }

    private ResultType checkRegexp(JsonNode erNode, JsonNode arDoc, String arPath) {
        if (erNode.getNodeType().equals(JsonNodeType.STRING)) {
            String erValue = erNode.textValue();
            if (erValue.startsWith("regexp:")) {
                JsonNode arNode = arDoc.at(arPath);
                String arValue;
                switch (arNode.getNodeType()) {
                    case STRING:
                        arValue = arNode.textValue();
                        break;
                    case BOOLEAN:
                        boolean arBoolean = arNode.booleanValue();
                        arValue = (arBoolean) ? "true" : "false";
                        break;
                    case NUMBER:
                        arValue = arNode.numberValue().toString();
                        break;
                    case MISSING:
                        /* Missed value can't satisfy regexp */
                    case NULL:
                        /* NULL value can't satisfy regexp */
                    case OBJECT:
                        /* er node is textual but ar node is Object. JSON structure is changed */
                    case POJO:
                        /* er node is textual but ar node is POJO. JSON structure is changed */
                    case ARRAY:
                        /* Array can't satisfy regexp */
                    case BINARY:
                        /* It seems incorrect if we try to check binary value via regexp */
                    default:
                        return null;
                }
                try {
                    if (arValue.matches(erValue.substring(7))) {
                        return ResultType.IDENTICAL;
                    } else {
                        return ResultType.MODIFIED;
                    }
                } catch (PatternSyntaxException ex) {
                    // do nothing with error
                    return null;
                }
            } else {
                return null; // There is no regexp. DiffResult remains SIMILAR
            }
        } else {
            return null; // Regexp can be only in text node
        }
    }

    private boolean checkFilterObjectPropertyStringFormat(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        String s = str.trim();
        if (s.isEmpty()) {
            return false;
        }
        if (s.replace("/", "").trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private boolean arHasInvalidJson(final String ar) {
        boolean valid = true;
        try {
            final JsonParser parser = new ObjectMapper().getFactory().createParser(ar);
            while (parser.nextToken() != null) {
            }
            valid = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return valid;
    }

    private class FilterObjectProperty {

        public String obj;
        public String property;
        public String filterStr;
        public Pattern filterPattern;

        public FilterObjectProperty() {

        }

        public FilterObjectProperty(String obj, String property) {
            this.obj = (StringUtils.isBlank(obj)) ? "" : obj.trim();
            this.property = (StringUtils.isBlank(property)) ? "" : property.trim();
            setRegexp();
        }

        public FilterObjectProperty(String str) {
            // Value is like "contactMethods/type" (To ignore property 'type' of 'contactMethods' objects) 
            //  or "contactMethods" (To totally ignore 'contactMethods' objects) 
            //  or "/type" (To ignore property 'type' of all objects)
            int k = str.lastIndexOf("/");
            if (k == -1) {
                this.obj = str;
                this.property = "";
            } else if (k == 0) {
                this.obj = "";
                this.property = str.substring(1);
            } else {
                this.obj = str.substring(0, k);
                this.property = str.substring(k + 1);
            }
            setRegexp();
        }

        private void setRegexp() {
            if (this.obj.isEmpty() && !this.property.isEmpty()) {
                this.filterStr = ".*/" + this.property;
            } else if (!this.obj.isEmpty() && this.property.isEmpty()) {
                this.filterStr = "(.*/)*" + this.obj + "/.*";
            } else {
                this.filterStr = "(.*/)*" + this.obj + "[/0-9]*/" + this.property;
            }
            this.filterPattern = Pattern.compile(this.filterStr, Pattern.CASE_INSENSITIVE);
        }
    }
}
