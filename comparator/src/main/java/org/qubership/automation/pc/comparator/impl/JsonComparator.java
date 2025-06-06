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

import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_CHANGE_DIFF_RESULT;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_CHECK_ARRAY;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_DIFF_SUMMARY_TEMPLATE;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_DISABLE_TYPE_CHECK_IF_REGEXP;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_FIND_ER_IN_AR;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_IGNORE_ARRAY_ELEMENTS_ORDER;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_IGNORE_EXTRA;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_IGNORE_PROPERTIES;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_IGNORE_PROPERTIES_V2;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_IGNORE_VALUE;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_KEYS_CASE_INSENSITIVE;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_MANDATORY_ATTRIBUTE;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_OBJECT_PRIMARY_KEY;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_OBJECT_PRIMARY_KEY_V2;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_READ_BY_PATH;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_SAVE_DIFF_VALUE;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_VALIDATE_AS_SIMPLE_SCHEMA;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_VALIDATE_SCHEMA;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.comparator.impl.json.SimpleJsonSchemaValidator;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.JsonDiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.utils.JsonComparatorUtils;
import org.qubership.automation.pc.core.utils.jsondiff.JsonDiffTuned;
import org.qubership.automation.pc.models.ChangeDiffResultRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.base.Strings;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

/**
 * A comparator implementation for comparing two JSON structures with extended features.
 *
 * <p>
 * This class supports configurable JSON diff logic, including:
 * <ul>
 *   <li>Selective path comparison using JSONPath</li>
 *   <li>Property filtering with support for case-insensitive keys</li>
 *   <li>Schema validation using JSON Schema (Draft-7 and V2 format)</li>
 *   <li>Inline regular expression checks within expected JSON values</li>
 *   <li>Dynamic change of diff result types based on custom rules</li>
 * </ul>
 *
 * <p>
 * It returns a list of {@link DiffMessage} objects that describe the differences
 * between the expected and actual JSON documents.
 * </p>
 *
 * <p>
 * Used in automated testing or data verification scenarios where flexible and
 * robust comparison logic is required.
 * </p>
 *
 * @see AbstractComparator
 * @see DiffMessage
 * @see JsonDiffTuned
 */
public class JsonComparator extends AbstractComparator {

    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonDiffTuned jsonDiffTuned = new JsonDiffTuned();
    private List<FilterObjectProperty> ignorePropertiesList;
    private List<String>  checkArrayList;
    private List<String>  mandatoryAttributeList;
    private List<String> ignorePropertiesV2List;
    private List<String> ignoreValue;

    private String diffSummaryTemplate;
    private boolean ignoreExtra;
    private boolean keysCaseInsensitive;
    private boolean saveDiffValue;

    private static final Logger log = LoggerFactory.getLogger(JsonComparator.class);

    private static final String DIFF_MACROS_ER_PATH = "ERPATH";
    private static final String DIFF_MACROS_AR_PATH = "ARPATH";
    private static final String DIFF_MACROS_VALUE = "VALUE";
    private static final String DIFF_MACROS_SUMMARY = "SUMMARY";
    private static final String DIFF_MACROS_ER_JSON_PATH = "ERJSONPATH";
    private static final String DIFF_MACROS_AR_JSON_PATH = "ARJSONPATH";

    public List<DiffMessage> compare(String er, String ar, Parameters parameters) throws ComparatorException {
        List<DiffMessage> result = new ArrayList<>();
        String messageForExceptionIfPathNotExist = "Error while parsing input message %s. The path '%s' "
                + "specified in parameter 'readByPath' does not exist.";
        String messageForExceptionIfObjectIsNull = "The transmitted %s are NULL.";
        String arContent = Optional.ofNullable(ar).orElseThrow(() -> new ComparatorException(
                String.format(messageForExceptionIfObjectIsNull, "ar"), 20000));
        JsonNode jsonNodeER = null;
        JsonNode jsonNodeAR = null;
        try {
            parameters = Optional.ofNullable(parameters).orElse(new Parameters());
            //.orElseThrow(() -> new ComparatorException("The transmitted parameters are NULL."));
            List<String> parameterReadByPathValue = PARAMETER_READ_BY_PATH.getValue(parameters);
            keysCaseInsensitive = PARAMETER_KEYS_CASE_INSENSITIVE.getValue(parameters);
            String parameterValidateSchemaValue = PARAMETER_VALIDATE_SCHEMA.getValue(parameters);
            jsonNodeAR = readByPath(ar, parameterReadByPathValue, keysCaseInsensitive)
                    .orElseThrow(() -> new ComparatorException(String.format(messageForExceptionIfPathNotExist, "ar",
                            parameterReadByPathValue.get(0)), 20000));
            if (!parameterValidateSchemaValue.isEmpty()) {
                if (!arContent.isEmpty()) {
                    result = validateJsonSchema(jsonNodeAR, parameterValidateSchemaValue);
                } else {
                    result.add(new DiffMessage().setOrderId(1).setDescription("").setResult(ResultType.FAILED));
                }
            } else if (((Boolean)(PARAMETER_FIND_ER_IN_AR.getValue(parameters))).booleanValue()) {
                //set explicit cost of boolean to run unit-tests
                result = findErInAr(er, ar);
            } else if (((Boolean)PARAMETER_VALIDATE_AS_SIMPLE_SCHEMA.getValue(parameters)).booleanValue()) {
                //set explicit cost of boolean to run unit-tests
                jsonNodeER = readByPath(er, parameterReadByPathValue, keysCaseInsensitive)
                        .orElseThrow(()
                                -> new ComparatorException(String.format(messageForExceptionIfPathNotExist, "er",
                                parameterReadByPathValue.get(0)), 20000));
                result = validateBySimpleSchema(jsonNodeER.toString(), jsonNodeAR.toString());
            } else {
                jsonNodeER = readByPath(er, parameterReadByPathValue, keysCaseInsensitive)
                        .orElseThrow(()
                                -> new ComparatorException(String.format(messageForExceptionIfPathNotExist, "er",
                                parameterReadByPathValue.get(0)), 20000));
                Map<String, String> mergedObjectPrimaryKey = mergeObjectPrimaryKeysVersion(parameters);
                if (keysCaseInsensitive
                        && Objects.nonNull(mergedObjectPrimaryKey)
                        && !mergedObjectPrimaryKey.isEmpty()) {
                    mergedObjectPrimaryKey = mergedObjectPrimaryKey.entrySet().stream()
                            .collect(Collectors.toMap(
                                entry -> entry.getKey().toLowerCase(),
                                entry -> entry.getValue().toLowerCase())
                    );
                }
                JsonNode comparisonResult = jsonDiffTuned.asJson(jsonNodeER, jsonNodeAR,
                        PARAMETER_IGNORE_ARRAY_ELEMENTS_ORDER.getValue(parameters),
                        PARAMETER_DISABLE_TYPE_CHECK_IF_REGEXP.getValue(parameters),
                        mergedObjectPrimaryKey);
                ignorePropertiesList = PARAMETER_IGNORE_PROPERTIES.<List<String>>getValue(parameters).stream()
                        .filter(str -> !StringUtils.isBlank(str))
                        .filter(str -> !str.trim().replace("/", "").trim().isEmpty())
                        .map(str -> keysCaseInsensitive ? str.toLowerCase() : str)
                        .map(str -> new JsonComparator.FilterObjectProperty(str.trim()))
                        .collect(Collectors.toList());
                checkArrayList = PARAMETER_CHECK_ARRAY.getValue(parameters);
                mandatoryAttributeList = PARAMETER_MANDATORY_ATTRIBUTE.getValue(parameters);
                ignorePropertiesV2List = PARAMETER_IGNORE_PROPERTIES_V2.getValue(parameters);
                diffSummaryTemplate = PARAMETER_DIFF_SUMMARY_TEMPLATE.getValue(parameters);
                ignoreExtra = PARAMETER_IGNORE_EXTRA.getValue(parameters);
                ignoreValue = PARAMETER_IGNORE_VALUE.getValue(parameters);
                saveDiffValue = PARAMETER_SAVE_DIFF_VALUE.getValue(parameters);
                if (keysCaseInsensitive) {
                    checkArrayList = JsonComparatorUtils.jsonPathsToLowercase(checkArrayList);
                    mandatoryAttributeList = JsonComparatorUtils.jsonPathsToLowercase(mandatoryAttributeList);
                    ignorePropertiesV2List = JsonComparatorUtils.jsonPathsToLowercase(ignorePropertiesV2List);
                    ignoreValue = JsonComparatorUtils.jsonPathsToLowercase(ignoreValue);
                }
                try {
                    log.debug("[Json comparator] formDiffMessages");
                    result = formDiffMessages(comparisonResult, jsonNodeER, jsonNodeAR);
                } catch (ReaderException ex) {
                    throw new ComparatorException("Error while parsing Diff Template", ex);
                }
            }
        } catch (IOException ex) {
            throw new ComparatorException(
                    String.format("Error while parsing input message %s. Probably it is not valid JSON. %s",
                            jsonNodeAR == null ? "ar" : "er", ex.getMessage()), 20000);
        }
        log.debug("[Json comparator] changeDiffResults");
        changeDiffResults(parameters, result, jsonNodeER, jsonNodeAR);
        return result;
    }

    private void changeDiffResults(Parameters parameters, List<DiffMessage> diffMessages, JsonNode jsonNodeER,
                                   JsonNode jsonNodeAR) {
        List<ChangeDiffResultRule> changeDiffResultRules = PARAMETER_CHANGE_DIFF_RESULT.getValue(parameters);
        boolean keysCaseInsensitive = PARAMETER_KEYS_CASE_INSENSITIVE.getValue(parameters);
        log.debug("[changeDiffResults] start");
        for (ChangeDiffResultRule rule : changeDiffResultRules) {
            diffMessages.stream()
                    .filter(JsonDiffMessage.class::isInstance)
                    .map(JsonDiffMessage.class::cast)
                    .filter(diff -> diff.getResult() == rule.getOldResult()
                            && ((StringUtils.isNotBlank(diff.getExpectedJsonPath())
                            && validateJsonNodeByPaths(jsonNodeER, diff.getExpectedJsonPath(),
                            rule.getPath(), keysCaseInsensitive))
                            || (StringUtils.isNotBlank(diff.getActualJsonPath())
                            && validateJsonNodeByPaths(jsonNodeAR, diff.getActualJsonPath(),
                            rule.getPath(), keysCaseInsensitive))))
                    .forEach(diff -> diff.setResult(rule.getNewResult()));
        }
        log.debug("[changeDiffResults] end");
    }

    private boolean validateJsonNodeByPaths(JsonNode node, String expected, String toFind,
                                            boolean keysCaseInsensitive) {
        String path = keysCaseInsensitive ? JsonComparatorUtils.jsonPathToLowercase(toFind) : toFind;
        try {
            return findPathListForJsonPath(node.toString(), path).contains(expected);
        } catch (InvalidPathException e) {
            return false;
        }
    }

    private List<String> findPathListForJsonPath(String json, String jsonPath) throws InvalidPathException {
        return JsonPath.using(Configuration.builder().options(Option.AS_PATH_LIST, Option.SUPPRESS_EXCEPTIONS).build())
                .parse(json)
                .read(jsonPath);
    }

    private List<DiffMessage> findErInAr(String er, String ar) throws ComparatorException {
        List<DiffMessage> result = new ArrayList<>();
        String erWithoutWhitespaces = null;
        String arWithoutWhitespaces = null;
        try {
            erWithoutWhitespaces = objectMapper.readValue(trimJson(er), JsonNode.class)
                    .toString().replaceAll("\\s", "");
            arWithoutWhitespaces = objectMapper.readValue(trimJson(ar), JsonNode.class)
                    .toString().replaceAll("\\s", "");
        } catch (IOException ex) {
            throw new ComparatorException(
                    String.format("Error while parsing input message %s. Probably it is not valid JSON. %s",
                            erWithoutWhitespaces == null ? "er" : "ar", ex.getMessage()), 20000);
        }
        if (!StringUtils.contains(arWithoutWhitespaces, erWithoutWhitespaces)) {
            result.add(new DiffMessage(1, er, ar, ResultType.FAILED));
        }
        return result;
    }

    private Optional<JsonNode> readByPath(String content, List<String> parameterReadByPathValue,
                                          boolean keysCaseInsensitive) throws IOException {

        String trimContent = trimJson(content);
        String jsonPath = Objects.isNull(parameterReadByPathValue) || parameterReadByPathValue.isEmpty()
                        ? "" : parameterReadByPathValue.get(0);
        return JsonComparatorUtils.readJsonNodeFromString(trimContent, jsonPath, keysCaseInsensitive);
    }

    private String trimJson(String srcJson) {
        String result = "{}";
        if (!StringUtils.isBlank(srcJson)) {
            int startObject = srcJson.indexOf("{");
            int startArray = srcJson.indexOf("[");
            if (startObject >= 0 && startArray >= 0) {
                if (startObject < startArray) {
                    result = StringUtils.substring(srcJson, startObject, srcJson.lastIndexOf("}") + 1);
                } else {
                    result = StringUtils.substring(srcJson, startArray, srcJson.lastIndexOf("]") + 1);
                }
            } else if (startArray < 0 && startObject >= 0) {
                result = StringUtils.substring(srcJson, startObject, srcJson.lastIndexOf("}") + 1);
            } else if (startArray >= 0) {
                result = StringUtils.substring(srcJson, startArray, srcJson.lastIndexOf("]") + 1);
            }
        }
        return result.isEmpty() ? "{}" : result;
    }

    private List<DiffMessage> validateJsonSchema(JsonNode jsonNode, String schema) throws ComparatorException {
        Set<ValidationMessage> resultValidation = new HashSet<>();
        AtomicInteger count = new AtomicInteger();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        try {
            JsonSchema jsonSchema = factory.getSchema(schema);
            switch (jsonNode.getNodeType()) {
                case ARRAY:
                case OBJECT: {
                    resultValidation = jsonSchema.validate(jsonNode);
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + jsonNode.getNodeType());
            }
        } catch (JsonSchemaException e) {
            throw new ComparatorException(e.getMessage(), e);
        }
        List<DiffMessage> diffMessageList = new ArrayList<>(resultValidation.size());
        Iterator<ValidationMessage> iterator = resultValidation.iterator();
        int missedCount = 0;
        while (iterator.hasNext()) {
            ValidationMessage validationMessage = iterator.next();
            DiffMessage diffMessage = new DiffMessage().setExpected("")
                        .setActual(jsonPathToTreePath(validationMessage.getPath()))
                        .setDescription(validationMessage.getMessage())
                        .setOrderId(count.incrementAndGet());
            switch (validationMessage.getType()) {
                case "additionalProperties":
                    diffMessage.setResult(ResultType.EXTRA);
                    diffMessage.setActual(diffMessage.getActual().replaceFirst("/(?>$)","")
                            + "/" + validationMessage.getArguments()[0]);
                    break;
                case "pattern":
                case "minLength":
                case "maxLength":
                case "maximum":
                case "minimum":
                    diffMessage.setResult(ResultType.SIMILAR);
                    break;
                case "required":
                    diffMessage.setResult(ResultType.MISSED);
                    if (validationMessage.getArguments().length > 0) {
                        diffMessage.setActualValue(validationMessage.getArguments()[0]);
                    } else {
                        diffMessage.setActualValue("missed " + missedCount++);
                    }
                    break;
                default:
                    diffMessage.setResult(ResultType.MODIFIED);
            }
            diffMessageList.add(diffMessage);
        }
        return diffMessageList;
    }

    public static String jsonPathToTreePath(String jsonPath) {
        if (!jsonPath.startsWith("$")) {
            return jsonPath;
        }
        return jsonPath
                .replaceAll("[\\$\\.\\[\\]]", "/")
                .replaceAll("//", "/")
                .replaceAll("(?<!^)/$","");

    }
    //WITHE -------------------------------------------------------------------------------------------------

    private List<DiffMessage> formDiffMessages(JsonNode comparisonResult,
                                               JsonNode jsonNodeER,
                                               JsonNode jsonNodeAR) throws ReaderException {
        Set<String> ignorePathsER = new HashSet<>();
        Set<String> ignorePathsAR = new HashSet<>();
        Set<String> ignoreValuesER = new HashSet<>();
        Set<String> ignoreValuesAR = new HashSet<>();
        Set<String> checkPathsER = new HashSet<>();
        Set<String> checkPathsAR = new HashSet<>();
        for (String path: ignorePropertiesV2List) {
            try {
                ignorePathsER.addAll(findPathListForJsonPath(jsonNodeER.toString(), path));
                ignorePathsAR.addAll(findPathListForJsonPath(jsonNodeAR.toString(), path));
            } catch (InvalidPathException e) {
                log.warn("Invalid Path for rule 'ignorePropertiesV2': {}", path);
            }
        }
        for (String path: ignoreValue) {
            try {
                ignoreValuesER.addAll(findPathListForJsonPath(jsonNodeER.toString(), path));
                ignoreValuesAR.addAll(findPathListForJsonPath(jsonNodeAR.toString(), path));
            } catch (InvalidPathException e) {
                log.warn("Invalid Path for rule 'ignoreValue': {}", path);
            }
        }
        for (String path: checkArrayList) {
            try {
                checkPathsER.addAll(findPathListForJsonPath(jsonNodeER.toString(), path));
                checkPathsAR.addAll(findPathListForJsonPath(jsonNodeAR.toString(), path));
            } catch (InvalidPathException e) {
                log.warn("Invalid Path for rule 'checkArray': {}", path);
            }
        }
        Set<String> mandatoryPathsER = new HashSet<>();
        Set<String> mandatoryPathsAR = new HashSet<>();
        for (String path: mandatoryAttributeList) {
            try {
                mandatoryPathsER.addAll(findPathListForJsonPath(jsonNodeER.toString(), path));
                mandatoryPathsAR.addAll(findPathListForJsonPath(jsonNodeAR.toString(), path));
            } catch (InvalidPathException e) {
                log.warn("Invalid Path for rule 'mandatoryAttribute': {}", path);
            }
        }

        List<DiffMessage> diffMessages = new ArrayList<>();
        int diffCounter = 1;
        for (int i = 0; i < comparisonResult.size(); i++) {
            JsonNode comparisonNode = comparisonResult.get(i);
            String operation = comparisonNode.get("op").toString().replaceAll("\"", "").toLowerCase();
            log.debug("[formDiffMessages] start {}", comparisonNode);
            Map<String, String> macroses = getMacroses(comparisonNode);
            String erPath = macroses.get(DIFF_MACROS_ER_PATH);
            String arPath = macroses.get(DIFF_MACROS_AR_PATH);
            String erJsonPath = macroses.get(DIFF_MACROS_ER_JSON_PATH);
            String arJsonPath = macroses.get(DIFF_MACROS_AR_JSON_PATH);
            JsonDiffMessage diffMessage;
            if (!ignoreDifference(macroses) && mandatoryAttribute(macroses, mandatoryPathsER, mandatoryPathsAR)
                    && !ignorePropertiesByJsonPath(macroses, ignorePathsER, ignorePathsAR, checkPathsER, checkPathsAR)
                    && !ignoreValuesByJsonPath(macroses, operation, ignoreValuesER, ignoreValuesAR)) {
                switch (operation) {
                    case "replace":
                        if (erPath.equals("/") || arPath.equals("/")) {
                            log.debug("[formDiffMessages] prepareParameterizedScript");
                            macroses.put(DIFF_MACROS_SUMMARY, "er and ar root nodes have different types.");
                            diffMessage = new JsonDiffMessage(diffCounter, erPath, arPath, ResultType.MODIFIED,
                                    ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses),
                                    erJsonPath, arJsonPath);
                        } else {
                            macroses.put(DIFF_MACROS_SUMMARY, "Node values are different.");
                            diffMessage = new JsonDiffMessage(diffCounter, erPath, arPath, ResultType.SIMILAR,
                                    "Node values are different.", erJsonPath, arJsonPath);
                            log.debug("[formDiffMessages] checkRegexpResult {}" , diffMessage);
                            Optional<ResultType> checkRegexpResult = checkRegexp(jsonNodeER.at(erPath), jsonNodeAR,
                                    arPath);
                            if (checkRegexpResult.isPresent()) {
                                diffMessage.setResult(checkRegexpResult.get());
                                macroses.put(DIFF_MACROS_SUMMARY, "Result is changed due to inline-regexp checking.");
                            }
                            log.debug("[formDiffMessages] prepareParameterizedScript1 {}", checkRegexpResult);
                            diffMessage.setDescription(ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate,
                                    macroses));
                        }
                        break;
                    case "type_not_matched":
                        macroses.put(DIFF_MACROS_SUMMARY, "Nodes have different types.");
                        log.debug("[formDiffMessages] prepareParameterizedScript2");
                        diffMessage = new JsonDiffMessage(diffCounter, erPath, arPath, ResultType.MODIFIED,
                                ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses),
                                erJsonPath, arJsonPath);
                        break;
                    case "add":
                        macroses.put(DIFF_MACROS_SUMMARY, "ar has extra node(s).");
                        log.debug("[formDiffMessages] prepareParameterizedScript3");
                        diffMessage = new JsonDiffMessage(diffCounter, "", arPath, ResultType.EXTRA,
                                ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses),
                                "", arJsonPath);
                        if (ignoreExtra) {
                            diffMessage.setResult(ResultType.IDENTICAL);
                        }
                        break;
                    case "remove":
                        macroses.put(DIFF_MACROS_SUMMARY, "er node is missed.");
                        log.debug("[formDiffMessages] prepareParameterizedScript4");
                        diffMessage = new JsonDiffMessage(diffCounter, erPath, "", ResultType.MISSED,
                                ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses),
                                erJsonPath, "");
                        break;
                    case "move":
                        erPath = comparisonNode.get("from").toString().replaceAll("\"", "");
                        macroses.put(DIFF_MACROS_ER_PATH, erPath);
                        macroses.put(DIFF_MACROS_SUMMARY, "er and ar nodes have different types and/or structure.");
                        log.debug("[formDiffMessages] prepareParameterizedScript4");
                        diffMessage = new JsonDiffMessage(diffCounter, erPath, arPath, ResultType.MODIFIED,
                                ScriptUtils.prepareParameterizedScript(this.diffSummaryTemplate, macroses),
                                erJsonPath, arJsonPath);
                        break;
                    default:
                        continue;
                }
                if (saveDiffValue) {
                    String expectedValue = Strings.isNullOrEmpty(diffMessage.getExpected()) ? "" :
                            jsonNodeER.at(diffMessage.getExpected()).toString();
                    String actualValue = Strings.isNullOrEmpty(diffMessage.getActual()) ? "" :
                            jsonNodeAR.at(diffMessage.getActual()).toString();
                    diffMessage.setExpectedValue(expectedValue);
                    diffMessage.setActualValue(actualValue);
                }
                diffMessages.add(diffMessage);
            }
            diffCounter++;
        }
        log.debug("[formDiffMessages] end");
        return diffMessages;
    }

    private boolean mandatoryAttribute(Map<String, String> macroses,
                                       Set<String> mandatoryPathsER,
                                       Set<String> mandatoryPathsAR) {
        String expected = macroses.get(DIFF_MACROS_ER_JSON_PATH);
        String actual = macroses.get(DIFF_MACROS_AR_JSON_PATH);
        if (!mandatoryAttributeList.isEmpty()) {
            return mandatoryPathsER.contains(expected) || mandatoryPathsAR.contains(actual);
        }
        return true;
    }

    private Map<String, String> getMacroses(JsonNode comparisonNode) {
        String erPath = comparisonNode.get("path").toString().replaceAll("\"", "");
        String arPath;
        if (comparisonNode.has("from")) {
            arPath = comparisonNode.get("from").toString().replaceAll("\"", "");
        } else {
            arPath = erPath;
        }
        String erJsonPath = comparisonNode.get("controlJsonPath").toString().replaceAll("\"", "");
        String arJsonPath;
        if (comparisonNode.has("testJsonPath")) {
            arJsonPath = comparisonNode.get("testJsonPath").toString().replaceAll("\"", "");
        } else {
            arJsonPath = erJsonPath;
        }
        String value = comparisonNode.get("value").textValue();
        // Prepare Diff Template Macroses
        Map<String, String> macroses = new HashMap<>();
        macroses.put(DIFF_MACROS_ER_PATH, erPath);
        macroses.put(DIFF_MACROS_AR_PATH, arPath);
        macroses.put(DIFF_MACROS_VALUE, value);
        macroses.put(DIFF_MACROS_ER_JSON_PATH, erJsonPath);
        macroses.put(DIFF_MACROS_AR_JSON_PATH, arJsonPath);
        return macroses;
    }

    private boolean ignoreDifference(Map<String, String> macroses) {
        String expected = macroses.get(DIFF_MACROS_ER_PATH);
        String actual = macroses.get(DIFF_MACROS_AR_PATH);
        long startTime = System.currentTimeMillis();
        for (JsonComparator.FilterObjectProperty item : ignorePropertiesList) {
            if (expected.matches(item.filterStr) || actual.matches(item.filterStr)) {
                log.debug("[End ignore difference] with true result. "
                                + "Expected:{}, actual:{}, time(ms):{}, list item: {}",
                        expected, actual, System.currentTimeMillis() - startTime, item.filterStr);
                return true;
            }
        }
        log.debug("[End ignore difference] with false result. Expected:{}, actual:{}, time(ms):{}, "
                        + "ignore properties list:{}", expected, actual, System.currentTimeMillis() - startTime,
                ignorePropertiesList.stream().map(item -> item.filterStr).collect(Collectors.toList()));
        return false;
    }

    private boolean ignorePropertiesByJsonPath(Map<String, String> macroses,
                                               Set<String> ignorePathsER,
                                               Set<String> ignorePathsAR,
                                               Set<String> checkPathsER,
                                               Set<String> checkPathsAR) {
        String expected = macroses.get(DIFF_MACROS_ER_JSON_PATH);
        String actual = macroses.get(DIFF_MACROS_AR_JSON_PATH);
        if (ignorePathsER.contains(expected) || ignorePathsAR.contains(actual)) {
            if (checkPathsER.contains(expected) || checkPathsAR.contains(actual)) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean ignoreValuesByJsonPath(Map<String, String> macroses, String diffResult,
                                           Set<String> ignorePathsER, Set<String> ignorePathsAR) {
        String expected = macroses.get(DIFF_MACROS_ER_JSON_PATH);
        String actual = macroses.get(DIFF_MACROS_AR_JSON_PATH);
        if ("replace".equals(diffResult) || "type_not_matched".equals(diffResult) || "move".equals(diffResult)) {
            if (ignorePathsER.contains(expected) || ignorePathsAR.contains(actual)) {
                return true;
            }
        }
        return false;
    }

    private Optional<ResultType> checkRegexp(JsonNode erNode, JsonNode arDoc, String arPath) {
        Optional<ResultType> resultType = Optional.empty();
        log.debug("[checkRegexp] start");
        if (erNode.getNodeType().equals(JsonNodeType.STRING)) {
            String erValue = erNode.textValue();
            if (erValue.startsWith("regexp:")) {
                JsonNode arNode = arDoc.at(arPath);
                String arValue = null;
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
                        log.warn("Unexpected node type for regexp: {}", arNode.getNodeType());
                        break;
                }
                try {
                    log.debug("[checkRegexp] matches");
                    if (arValue != null && arValue.matches(erValue.substring(7))) {
                        resultType = Optional.of(ResultType.IDENTICAL);
                    } else {
                        resultType = Optional.of(ResultType.MODIFIED);
                    }
                } catch (PatternSyntaxException ignore) {
                    log.warn("[checkRegexp] PatternSyntaxException {}", ignore);
                }
            }
        }
        return resultType;
    }

    private Map<String, String> mergeObjectPrimaryKeysVersion(Parameters parameters) {
        HashMap objectPrimaryKeys = PARAMETER_OBJECT_PRIMARY_KEY.getValue(parameters);
        objectPrimaryKeys.putAll(PARAMETER_OBJECT_PRIMARY_KEY_V2.getValue(parameters));
        return objectPrimaryKeys;
    }

    private class FilterObjectProperty {

        private String obj;
        private String property;
        private String filterStr;
        private Pattern filterPattern;

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

    private List<DiffMessage> validateBySimpleSchema(String er, String ar) throws ComparatorException {
        return SimpleJsonSchemaValidator.validateDocument(ar, er);
    }
}
