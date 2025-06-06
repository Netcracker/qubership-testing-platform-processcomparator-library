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

import static java.lang.String.format;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.JsonDiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created at 4:16 PM on 2013-12-15.
 *
 * @author Russ Jackson (last updated by $Author: arjackson $)
 * @version $Revision: 49 $, $Date: 2014-08-10 10:32:13 -0500 (Sun, 10 Aug 2014) $
 */
public final class SimpleJsonSchemaValidator {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleJsonSchemaValidator.class);

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"
    );

    private static final Set<String> NATIVE_TYPES = new HashSet<>(
            Arrays.asList("int", "long", "date", "double", "enum", "bool", "string", "uuid")
    );

    private static final String MAX = "max";

    private static final String MIN = "min";

    private static final String REGEX = "regex";

    private static final String TYPE = "type";

    private static final String DOCUMENT = "document";

    /**
     * validate the given json document against the given schema.
     * </p>
     * use this method if you have a json document and the matching schema
     *
     * @param json   the document to be validated
     * @param schema the schema to use to validate the document
     * @throws ComparatorException thrown if the schema is invalid or if
     *                             the document doesn't match the schema
     */
    public static List<DiffMessage> validateDocument(JSONObject json, final JSONObject schema)
            throws ComparatorException {
        SimpleJsonSchemaValidator validator = new SimpleJsonSchemaValidator();
        return validator.validateDocTypeDocument(json, schema);
    }

    /**
     * validates the given document against the given schema;  both the document
     * and the schema will be converted to JSONObjects before further processing,
     * so the documents should be valid json as acceptable by JSONObject.
     * </p>
     * use this method if you have string representations of both the schema and
     * the document;
     *
     * @param json   string representing the json document to be validated
     * @param schema string representing the schema used to validate the
     *               given document
     * @throws ComparatorException thrown if either document fails to convert
     *                             into a JSONObject, if the schema is invalid, or if the json document doesn't
     *                             match the provided schema
     */
    public static List<DiffMessage> validateDocument(String json, String schema)
            throws ComparatorException {
        try {
            return validateDocument(new JSONObject(json), new JSONObject(schema));
        } catch (JSONException e) {
            throw new ComparatorException(e);
        }
    }

    /*
        strip any schema notation from the given attribute name
     */
    private String getRealAttributeName(String attribute) {
        if (attribute.contains("?")) {
            return attribute.split("[?]")[0];
        }
        if (attribute.contains("!")) {
            return attribute.split("[!]")[0];
        }
        return attribute.split("[\\[]")[0];
    }

    /*
        parse the given json attribute schema and return the elements as a
        mapping of key-value pairs
     */
    private Map<String, String> getValidations(Object schema) {
        Map<String, String> result = new HashMap<>();
        String[] attributes = String.valueOf(schema).split("[;]");
        for (String attribute : attributes) {
            String[] keyValue = attribute.split("[=]");
            if (keyValue.length != 2) {
                result.put("compareAsIs", keyValue[0]);
            } else {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }

    /*
        get maxItems and minItems of the given attribute.
        to define maxItems and minItems use the structure '[minItems..maxItems]' in the attribute name.
        E.g.:
        [0..1] - minItems=0, maxItems=1. 0 or 1 items
        [0..n] - minItems=0, maxItems=n. 0 or many items
        [1..1] - minItems=1, maxItems=1. only one item
     */
    private Pair<Integer, Integer> getMaxAndMinItems(String attribute) {
        Pattern regexp = Pattern.compile("\\[(\\d+)\\.\\.(\\d+|n)\\]");
        Matcher m = regexp.matcher(attribute);
        int minItems = isOptional(attribute) ? 0 : 1;
        int maxItems = Integer.MAX_VALUE;
        if (m.find()) {
            minItems = Integer.parseInt(m.group(1));
            if (!m.group(2).equals("n")) {
                maxItems = Integer.parseInt(m.group(2));
            }
        }
        return new ImmutablePair<>(minItems, maxItems);
    }

    /*
        determine whether the given attribute is optional;  optional attributes
        have a '?' in the name
     */
    private boolean isOptional(String attribute) {
        return attribute.contains("?");
    }

    /*
        validate the given JSONObject representing a data document against the
        given JSONObject representing a schema;  skipDoc should be set to true
        if the given objects represent the root of the json documents since the
        'document' header has its own validation rules
     */
    private void validateObject(JSONObject doc, JSONObject schema, Stack<Object> tracker,
                                Stack<Object> schemaTracker, List<DiffMessage> diffs)
            throws ComparatorException {
        try {
            JSONArray schemaChildren = ObjectUtils.defaultIfNull(schema.names(), new JSONArray());
            Set<String> schemaAttributes = new HashSet<>();

            /*
            iterate over all of the child attributes of the given schema object and
            match them against the given json data object
            */
            if (schemaChildren != null) {
                for (int i = 0; i < schemaChildren.length(); i++) {
                    String childAttribute = schemaChildren.getString(i);
                    // build a list of schema attributes so we can later check
                    // for extraneous attributes in the json data object
                    String realAttributeName = getRealAttributeName(childAttribute);
                    schemaAttributes.add(childAttribute);
                    tracker.push(realAttributeName);
                    schemaTracker.push(childAttribute);
                    validateAttribute(
                            doc,
                            schema,
                            childAttribute,
                            tracker,
                            schemaTracker,
                            diffs
                    );
                    tracker.pop();
                    schemaTracker.pop();
                }
            }

            /*
            iterate over all of the json data object attributes and make sure
            they are all in the schema object;  build a list of extraneous
            entries so we can report them later
            */
            JSONArray docChildren = ObjectUtils.defaultIfNull(doc.names(), new JSONArray());
            for (int i = 0; i < docChildren.length(); i++) {
                String docAttribute = docChildren.getString(i);
                Optional<String> schemaAttribute = schemaAttributes.stream()
                        .filter(s -> getRealAttributeName(s).equals(docAttribute)).findFirst();
                if (!schemaAttribute.isPresent()) {
                    tracker.push(docAttribute);
                    schemaTracker.push(schemaAttribute);
                    addDiff(diffs, format("ar has extra node(s): %s", docAttribute),
                            ResultType.EXTRA, tracker, schemaTracker);
                    tracker.pop();
                    schemaTracker.pop();
                }
            }
        } catch (JSONException e) {
            throw new ComparatorException("error processing object", e);
        }
    }

    /*
        validate the given document attribute as an object against the given schema
     */
    private void validateObject(JSONObject doc,
                                String docAttribute,
                                JSONObject schemaChild,
                                boolean optional, Stack<Object> tracker,
                                Stack<Object> schemaTracker, List<DiffMessage> diffs)
            throws ComparatorException {
        JSONObject docChild = null;
        try {
            // find the corresponding object in the document
            docChild = doc.getJSONObject(docAttribute);
            // validate the object against the schema;  set skipDoc to false
            // since we're past root object validation
            validateObject(docChild, schemaChild, tracker, schemaTracker, diffs);
        } catch (JSONException e) {
            if (!optional) {
                try {
                    doc.get(docAttribute);
                    addDiff(diffs, format("nodes '%s' have different type", docAttribute),
                            ResultType.MODIFIED, tracker, schemaTracker);
                } catch (JSONException ex) {
                    addDiff(diffs, format("required object '%s' is missing", docAttribute),
                            ResultType.MISSED, tracker, schemaTracker);
                    LOG.warn("Can't get object {} in ar. Required object is missing", docAttribute);
                }
            }
        }
    }

    private void addDiff(List<DiffMessage> diffs, String message, ResultType status,
                         Stack<Object> tracker, Stack<Object> schemaTracker) {
        diffs.add(new JsonDiffMessage(
                diffs.size() + 1,
                "/" + schemaTracker.stream().map(Object::toString).collect(Collectors.joining("/")),
                "/" + tracker.stream().map(Object::toString).collect(Collectors.joining("/")),
                status,
                message,
                JSONUtils.listToJsonPath(schemaTracker),
                JSONUtils.listToJsonPath(tracker)
        ));
    }

    /*
        validate the given JSONArray data against the given JSONArray representing
        the schema;  the schema array should contain one and only one element
        representing the type of data contained in the array and any constraints
        pertaining to that data
     */
    private void validateArray(JSONArray docArray, JSONArray schemaArray, Stack<Object> tracker,
                               Stack<Object> schemaTracker, List<DiffMessage> diffs)
            throws ComparatorException {

        Object objectSchema = null;
        try {
            objectSchema = schemaArray.get(0);
        } catch (JSONException e) {
            throw new ComparatorException("invalid schema array definition", e);
        }
        // check and validate the type contained in the JSONArray
        if (objectSchema instanceof JSONObject) {
            validateArrayObjects(docArray, tracker, schemaTracker, diffs, schemaArray);
        } else {
            if (objectSchema instanceof JSONArray) {
                schemaTracker.push(0);
                validateNestedArray(docArray, (JSONArray) objectSchema, tracker, schemaTracker, diffs);
                schemaTracker.pop();
            } else {
                validateArrayValues(docArray, objectSchema, tracker, schemaTracker, diffs, schemaArray);
            }
        }
    }

    /*
        validate the given document attribute as an array against the given schema
     */
    private void validateArray(JSONObject doc,
                               String docAttribute,
                               JSONArray schemaArray,
                               boolean optional, Stack<Object> tracker,
                               Stack<Object> schemaTracker, List<DiffMessage> diffs, int minItems, int maxItems)
            throws ComparatorException {
        JSONArray docArray = null;
        try {
            docArray = doc.getJSONArray(docAttribute);
            if (schemaArray.length() == 0) {
                for (int i = 0; i < docArray.length(); i++) {
                    tracker.push(i);
                    schemaTracker.push("NULL");
                    addDiff(diffs, "ar has extra item", ResultType.EXTRA, tracker, schemaTracker);
                    tracker.pop();
                    schemaTracker.pop();
                }
                return;
            }
            if (docArray.length() > maxItems) {
                addDiff(diffs, format("there must be a maximum of %s items in the '%s' array", maxItems, docAttribute),
                        ResultType.MODIFIED, tracker, schemaTracker);
            }
            if (docArray.length() < minItems) {
                addDiff(diffs, format("there must be a minimum of %s items in the '%s' array", minItems, docAttribute),
                        ResultType.MODIFIED, tracker, schemaTracker);
            }
            validateArray(docArray, schemaArray, tracker, schemaTracker, diffs);
        } catch (JSONException e) {
            if (!optional) {
                addDiff(diffs, format("required array '%s' is missing", docAttribute),
                        ResultType.MISSED, tracker, schemaTracker);
            }
        }
    }

    /*
        validate the values in the given array against the given array element schema
     */
    private void validateArrayValues(JSONArray docArray, Object schema, Stack<Object> tracker,
                                     Stack<Object> schemaTracker, List<DiffMessage> diffs, JSONArray schemaArray)
            throws ComparatorException {
        for (int i = 0; i < docArray.length(); i++) {
            try {
                tracker.push(i);
                if (schemaArray.length() > 1) {
                    schemaTracker.push(i);
                    schema = schemaArray.get(i);
                } else {
                    schemaTracker.push(0);
                }
                validateValue("", docArray.get(i), schema, diffs, tracker, schemaTracker);
                tracker.pop();
                schemaTracker.pop();
            } catch (JSONException e) {
                throw new ComparatorException(format("error processing json array %s",
                        JSONUtils.listToJsonPath(schemaTracker)), e);
            }
        }
    }

    /*
        validate a nested array against the given array schema
     */
    private void validateNestedArray(JSONArray docArray, JSONArray objectSchema, Stack<Object> tracker,
                                     Stack<Object> schemaTracker,
                                     List<DiffMessage> diffs)
            throws ComparatorException {
        for (int i = 0; i < docArray.length(); i++) {
            try {
                // validate each nested array element against the schema
                tracker.push(i);
                // tracker.push(format("[%d]", i));
                validateArray(docArray.getJSONArray(i), objectSchema, tracker, schemaTracker, diffs);
                tracker.pop();
            } catch (JSONException e) {
                throw new ComparatorException("invalid nested array in array", e);
            }
        }
    }

    @Data
    @RequiredArgsConstructor
    protected static class ValidationTemplate {

        private final String keyName;
        private final Object keyValue;
        private final int templateIndex;
        private final JSONObject template;
    }

    /*
        validate the objects in the given array against the given schema
     */
    private void validateArrayObjects(JSONArray docArray, Stack<Object> tracker, Stack<Object> schemaTracker,
                                      List<DiffMessage> diffs, JSONArray schemaArray)
            throws ComparatorException {
        // get templates to validate of array items
        List<ValidationTemplate> templates = new ArrayList<>();
        for (int schemaArrayIndex = 0; schemaArrayIndex < schemaArray.length(); schemaArrayIndex++) {
            try {
                JSONObject objectSchema = (JSONObject) schemaArray.get(schemaArrayIndex);
                JSONArray schemaChildren = objectSchema.names();
                for (int j = 0; j < schemaChildren.length(); j++) {
                    String attrName = schemaChildren.getString(j);
                    if (attrName.endsWith("!")) {
                        Object attrValue = objectSchema.get(attrName);
                        templates.add(new ValidationTemplate(attrName, attrValue, schemaArrayIndex, objectSchema));
                        break;
                    }
                }
            } catch (JSONException e) {
                throw new ComparatorException("invalid schema array definition", e);
            }
        }
        // validate each array items
        for (int docArrayIndex = 0; docArrayIndex < docArray.length(); docArrayIndex++) {
            try {
                tracker.push(docArrayIndex);
                JSONObject docObject = docArray.getJSONObject(docArrayIndex);

                // get suitable templates to validate this array item
                List<ValidationTemplate> suitableTemplates = new ArrayList<>();
                for (ValidationTemplate template : templates) {
                    String attrName = template.getKeyName();
                    Object attrValue = template.getKeyValue();

                    if (docObject.has(getRealAttributeName(attrName))) {
                        Object docValue = docObject.get(getRealAttributeName(attrName));
                        // need validate value according to validations from template, but without adding differences
                        // to diffs
                        List<DiffMessage> copyDiffs = new ArrayList<>(diffs);
                        validateValue(attrName, docValue, attrValue, copyDiffs, tracker, schemaTracker);
                        // if validation of value has differences, then this template not suitable for this array item
                        if (copyDiffs.equals(diffs)) {
                            suitableTemplates.add(template);
                        }
                    }
                }

                if (suitableTemplates.size() > 1) {
                    schemaTracker.push("-");
                    addDiff(diffs, format("multiple schema templates are found for array item '%s'", docArrayIndex),
                            ResultType.MODIFIED, tracker, schemaTracker);
                    tracker.pop();
                    schemaTracker.pop();
                    continue;
                }
                if (!templates.isEmpty() && suitableTemplates.isEmpty()) {
                    schemaTracker.push("-");
                    addDiff(diffs, format("schema template not found for array item '%s'", docArrayIndex),
                            ResultType.MODIFIED, tracker, schemaTracker);
                    tracker.pop();
                    schemaTracker.pop();
                    continue;
                }

                if (templates.isEmpty()) {
                    // if templates is absent, should validate array item :
                    //      - by schema array item with same index, if schema array contains multiple items
                    //      - by first schema array item, if schema array only one item
                    int schemaArrayIndex = schemaArray.length() > 1 ? docArrayIndex : 0;
                    schemaTracker.push(schemaArrayIndex);
                    if (schemaArray.opt(schemaArrayIndex) == null) {
                        addDiff(diffs, format("ar has extra item '%s'", docArrayIndex),
                                ResultType.EXTRA, tracker, schemaTracker);
                        tracker.pop();
                        schemaTracker.pop();
                        continue;
                    }
                    JSONObject objectSchema = schemaArray.getJSONObject(schemaArrayIndex);
                    validateObject(docObject, objectSchema, tracker, schemaTracker, diffs);
                } else {
                    int schemaArrayIndex = suitableTemplates.get(0).getTemplateIndex();
                    JSONObject objectSchema = suitableTemplates.get(0).getTemplate();
                    schemaTracker.push(schemaArrayIndex);
                    validateObject(docObject, objectSchema, tracker, schemaTracker, diffs);
                }
                tracker.pop();
                schemaTracker.pop();

            } catch (JSONException e) {
                    throw new ComparatorException("invalid object in array", e);
                }
        }
    }

    /*
        main entry point for attribute validation;  this is meant to be
        recursive;  determine whether the attribute is another object, an array,
        or a value and invoke the appropriate validation
     */
    private void validateAttribute(JSONObject doc,
                                   JSONObject schema,
                                   String attributeName, Stack<Object> tracker,
                                   Stack<Object> schemaTracker, List<DiffMessage> diffs)
            throws ComparatorException {
        Object objectAttribute = null;
        try {
            objectAttribute = schema.get(attributeName);
        } catch (JSONException e) {
            // this shouldn't happen, but just in case
            LOG.error("unexpected error", e);
            throw new ComparatorException(e);
        }
        Pair<Integer, Integer> items = getMaxAndMinItems(attributeName);
        int minItems = items.getKey();
        int maxItems = items.getValue();
        // strip out any meta-data from the attribute name
        String docAttribute = getRealAttributeName(attributeName);
        boolean optional = isOptional(attributeName);
        if (objectAttribute instanceof JSONObject) {
            validateObject(doc, docAttribute, (JSONObject) objectAttribute, optional, tracker, schemaTracker, diffs);
        } else if (objectAttribute instanceof JSONArray) {
            validateArray(doc, docAttribute, (JSONArray) objectAttribute, optional, tracker, schemaTracker, diffs,
                    minItems, maxItems);
        } else {
            try {
                Object value = doc.get(docAttribute);
                if (!optional || (null != value && !"null".equalsIgnoreCase(value.toString()))) {
                    validateValue(docAttribute, doc.get(docAttribute), objectAttribute, diffs, tracker, schemaTracker);
                }
            } catch (JSONException e) {
                if (!optional) {
                    addDiff(diffs, format("required attribute '%s' is missing", docAttribute),
                            ResultType.MISSED, tracker, schemaTracker);
                }
            }
        }
    }

    /*
        validate that the given attribute with the given value is a valid boolean
     */
    private void validateBoolean(String attribute, Object value, List<DiffMessage> diffs, Stack<Object> tracker,
                                 Stack<Object> schemaTracker) {
        if (!(value instanceof Boolean)) {
            addDiff(diffs, format("%s with value %s is not of type bool (boolean)", attribute, value),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
    }

    /*
        extract the date format type from the schema validations
     */
    private String getDateFormatType(Map<String, String> validations) {
        String result = validations.get("format");
        if (null == result) {
            // default format for date
            result = "date";
        }
        return result;
    }

    /*
        lookup the date format given the supplied type, or a default if the type
        doesn't map to a pre-defined value
     */
    private DateFormat getDateFormat(String formatType) {
        DateFormat result = null;
        switch (formatType) {
            case "date":
                result = new SimpleDateFormat("yyyy-MM-dd");
                break;
            case "datetime":
                result = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSS Z");
                break;
            case "time":
                result = new SimpleDateFormat("HHmm");
                break;
            default:
                // a custom date format was supplied
                result = new SimpleDateFormat(formatType);
                break;
        }
        return result;
    }

    /*
        validate the date against a validation minimum, if one exists
     */
    private void validateMinDate(String format,
                                 DateFormat df,
                                 Date date,
                                 String attribute,
                                 Map<String, String> validations, List<DiffMessage> diffs, Stack<Object> tracker,
                                 Stack<Object> schemaTracker) {
        // apply a minimum date validation if one is present
        String min = validations.get(MIN);
        if (null != min) {
            try {
                Date minDate = df.parse(min);
                if (date.before(minDate)) {
                    addDiff(diffs, format("%s value '%s' for '%s' is before min %1$s '%s'", format, df.format(date),
                            attribute, df.format(minDate)),
                            ResultType.MODIFIED, tracker, schemaTracker);
                }
            } catch (ParseException e) {
                addDiff(diffs, format("invalid min date schema definition '%s' for '%s", min, attribute),
                        ResultType.MODIFIED, tracker, schemaTracker);
            }
        }
    }

    /*
        validate the date against a validation maximum, if one exists
     */
    private void validateMaxDate(String format,
                                 DateFormat df,
                                 Date date,
                                 String attribute,
                                 Map<String, String> validations, List<DiffMessage> diffs, Stack<Object> tracker,
                                 Stack<Object> schemaTracker) {
        // apply a maximum date validation if one is present
        String max = validations.get(MAX);
        if (null != max) {
            try {
                Date maxDate = df.parse(max);
                if (date.after(maxDate)) {
                    addDiff(diffs, format("%s value '%s' for '%s' is after max %1$s '%s'", format, df.format(date),
                            attribute, df.format(maxDate)), ResultType.MODIFIED, tracker, schemaTracker);
                }
            } catch (ParseException e) {
                addDiff(diffs, format("invalid max date schema definition '%s' for '%s", max, attribute),
                        ResultType.MODIFIED, tracker, schemaTracker);
            }
        }
    }

    /*
        validate that the given attribute with the given value represents a
        valid date and meets any further validations provided in the given
        validation mapping
     */
    private void validateDate(String attribute,
                              Object value,
                              Map<String, String> validations, List<DiffMessage> diffs, Stack<Object> tracker,
                              Stack<Object> schemaTracker) {
        // dates are expected to be expressed as strings
        if (value instanceof String) {
            String format = getDateFormatType(validations);
            DateFormat df = getDateFormat(format);
            try {
                Date date = df.parse((String) value);
                validateMinDate(format, df, date, attribute, validations, diffs, tracker, schemaTracker);
                validateMaxDate(format, df, date, attribute, validations, diffs, tracker, schemaTracker);
            } catch (ParseException e) {
                // will be used for an example of what the expected date should look like
                Date date = new Date();
                addDiff(diffs, format("date value '%s' for '%s' does not match required format '%s'", value,
                        attribute,
                        df.format(date)), ResultType.MODIFIED, tracker, schemaTracker);
            }
        } else {
            addDiff(diffs, format("'%s' is a date and must be of type string; found %s", attribute, value),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
    }

    private List<DiffMessage> validateDocTypeDocument(JSONObject json, JSONObject schema) throws
            ComparatorException {
        List<DiffMessage> diffs = new ArrayList<>();
        Stack<Object> tracker = new Stack<>();
        Stack<Object> schemaTracker = new Stack<>();
        try {
            validateObject(json, schema, tracker, schemaTracker, diffs);
        } catch (ComparatorException e) {
            String path = unwind(tracker);
            LOG.error("Error at : " + path, e);
            throw (e);
        }
        return diffs;
    }

    private String unwind(Stack<Object> tracker) {
        StringBuilder sb = new StringBuilder();
        for (Object entry : tracker) {
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(entry);
        }
        return sb.toString();
    }

    /*
        validate double is in range, as/if defined
     */
    private void validateDouble(String attribute,
                                Object value,
                                Map<String, String> validations, List<DiffMessage> diffs, Stack<Object> tracker,
                                Stack<Object> schemaTracker)
            throws ComparatorException {
        Double dblVal = getDouble(attribute, value, diffs, tracker, schemaTracker);
        // min value validation
        Double minVal = getDouble(attribute, validations, MIN);
        if (null != minVal && dblVal < minVal) {
            addDiff(diffs, format("%s = %f is out of range, min = %f", attribute, dblVal, minVal),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
        // max value validation
        Double maxVal = getDouble(attribute, validations, MAX);
        if (null != maxVal && dblVal > maxVal) {
            addDiff(diffs, format("%s = %f is out of range, max = %f", attribute, dblVal, maxVal),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
    }

    /*
        validate enumerated value - case insensitive
     */
    private void validateEnum(String attribute,
                              Object value,
                              Map<String, String> validations, List<DiffMessage> diffs, Stack<Object> tracker,
                              Stack<Object> schemaTracker) {
        if (value instanceof String) {
            String enumValue = ((String) value).trim().toLowerCase();
            String values = validations.get("values");
            // parse the values and put them into a set
            Set<String> valueSet = new HashSet<>();
            if (null != values) {
                for (String enumEntry : values.split("[,]")) {
                    enumEntry = enumEntry.trim().toLowerCase();
                    if (enumEntry.length() > 0) {
                        valueSet.add(enumEntry);
                    }
                }
            }
            // make sure schema enumeration isn't empty and that value is in it
            if (valueSet.isEmpty()) {
                addDiff(diffs, format("enum schema for '%s' has no 'values'", attribute),
                        ResultType.MODIFIED, tracker, schemaTracker);
            } else if (!valueSet.contains(enumValue)) {
                addDiff(diffs, format("enum '%s' value '%s' is not in enumeration '%s'", attribute, enumValue, values),
                        ResultType.MODIFIED, tracker, schemaTracker);
            }
        } else {
            addDiff(diffs, format("'%s' is an enum and must be of type string; found %s", attribute, value),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
    }

    /*
        look up the validation rule using the given key and try to parse
        the resulting value as a double
     */
    private Double getDouble(String attribute,
                             Map<String, String> validations,
                             String key) throws ComparatorException {
        Double result = null;
        String value = validations.get(key);
        if (null != value) {
            try {
                result = Double.parseDouble(value);
            } catch (NumberFormatException nfe) {
                throw new ComparatorException(format("invalid schema : %s value of %s for %s", key, value, attribute),
                        nfe);
            }
        }
        return result;
    }

    /*
        try to obtain a double from the given value
     */
    private Double getDouble(String attribute, Object value, List<DiffMessage> diffs, Stack<Object> tracker,
                             Stack<Object> schemaTracker) {
        Double dblVal = null;
        if (value instanceof Double) {
            dblVal = (Double) value;
        } else if (value instanceof String) {
            try {
                dblVal = Double.parseDouble((String) value);
            } catch (NumberFormatException nfe) {
                dblVal = null;
            }
        } else if (value instanceof Long) {
            dblVal = (double) (Long) value;
        } else if (value instanceof Integer) {
            dblVal = (double) (Integer) value;
        }
        if (null == dblVal) {
            addDiff(diffs, format("'%s' with value '%s' is not a valid double", attribute, value),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
        return dblVal;
    }

    /*
        look up the validation rule using the given key and try to parse
        the resulting value as an int
     */
    private Integer getInt(String attribute,
                           Map<String, String> validations,
                           String key) throws ComparatorException {
        Integer result = null;
        String value = validations.get(key);
        if (null != value) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                throw new ComparatorException(
                        format(
                                "invalid schema : %s value of %s for %s",
                                key, value, attribute
                        ),
                        nfe
                );
            }
        }
        return result;
    }

    /*
        try to obtain an int from the given value
     */
    private Integer getInt(String attribute, Object value, List<DiffMessage> diffs,
                           Stack<Object> tracker, Stack<Object> schemaTracker) {
        Integer intVal = null;
        if (value instanceof Integer) {
            intVal = (Integer) value;
        } else if (value instanceof String) {
            try {
                intVal = Integer.parseInt((String) value);
            } catch (NumberFormatException nfe) {
                intVal = null;
            }
        }
        if (null == intVal) {
            addDiff(diffs, format("'%s' with value '%s' is not a valid integer", attribute, value),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
        return intVal;
    }

    /*
        validate that the given value satisfies the given validation rules for an int
     */
    private void validateInt(String attribute,
                             Object value,
                             Map<String, String> validations, List<DiffMessage> diffs,
                             Stack<Object> tracker, Stack<Object> schemaTracker)
            throws ComparatorException {
        Integer intVal = getInt(attribute, value, diffs, tracker, schemaTracker);
        // check min value
        Integer minVal = getInt(attribute, validations, MIN);
        if (null != minVal && intVal < minVal) {
            addDiff(diffs, format("%s = %d is out of range, min = %d", attribute, intVal, minVal),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
        // check max value
        Integer maxVal = getInt(attribute, validations, MAX);
        if (null != maxVal && intVal > maxVal) {
            addDiff(diffs, format("%s = %d is out of range, max = %d", attribute, intVal, maxVal),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
    }

    /*
        look up the validation rule using the given key and try to parse
        the resulting value as a long
     */
    private Long getLong(String attribute,
                         Map<String, String> validations,
                         String key) throws ComparatorException {
        Long result = null;
        String value = validations.get(key);
        if (null != value) {
            try {
                result = Long.parseLong(value);
            } catch (NumberFormatException nfe) {
                throw new ComparatorException(
                        format(
                                "invalid schema : %s value of %s for %s",
                                key, value, attribute
                        ),
                        nfe
                );
            }
        }
        return result;
    }

    /*
        try to obtain a long from the given value
     */
    private Long getLong(String attribute, Object value, List<DiffMessage> diffs, Stack<Object> tracker,
                         Stack<Object> schemaTracker) {
        Long longVal = null;
        if (value instanceof Long) {
            longVal = (Long) value;
        } else if (value instanceof Integer) {
            Integer intVal = getInt(attribute, value, diffs, tracker, schemaTracker);
            longVal = Long.valueOf(intVal);
        } else if (value instanceof String) {
            try {
                longVal = Long.parseLong((String) value);
            } catch (NumberFormatException nfe) {
                longVal = null;
            }
        }
        if (null == longVal) {
            addDiff(diffs, format("'%s' with value '%s' is not a valid long number", attribute, value),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
        return longVal;
    }

    /*
        validate that the given value satisfies the given validation rules for a long
     */
    private void validateLong(String attribute,
                              Object value,
                              Map<String, String> validations, List<DiffMessage> diffs, Stack<Object> tracker,
                              Stack<Object> schemaTracker)
            throws ComparatorException {
        Long longVal = getLong(attribute, value, diffs, tracker, schemaTracker);
        // check min
        Long minVal = getLong(attribute, validations, MIN);
        if (null != minVal && longVal < minVal) {
            addDiff(diffs, format("%s = %,d is out of range, min = %,d", attribute, longVal, minVal),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
        // check max
        Long maxVal = getLong(attribute, validations, MAX);
        if (null != maxVal && longVal > maxVal) {
            addDiff(diffs, format("%s = %,d is out of range, max = %,d", attribute, longVal, maxVal),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
    }

    /*
        validate that the given value is a string
     */
    private void validateString(String attribute,
                                Object value,
                                Map<String, String> validations, List<DiffMessage> diffs, Stack<Object> tracker,
                                Stack<Object> schemaTracker) {
        if (!(value instanceof String)) {
            addDiff(diffs, format("%s with value %s is not of type string", attribute, value),
                    ResultType.MODIFIED, tracker, schemaTracker);
            return;
        }
        String strVal = (String) value;
        if (null != validations && validations.size() > 0) {
            for (Map.Entry<String, String> entry : validations.entrySet()) {
                switch (entry.getKey().toLowerCase()) {
                    case MIN:
                        Integer minAttr = getInt(MIN, entry.getValue(), diffs, tracker, schemaTracker);
                        if (strVal.length() < minAttr) {
                            addDiff(diffs, format("attribute %s with value %s is below minimum length of %d", attribute,
                                    strVal, minAttr), ResultType.MODIFIED, tracker, schemaTracker);
                        }
                        break;
                    case MAX:
                        Integer maxAttr = getInt(MAX, entry.getValue(), diffs, tracker, schemaTracker);
                        if (strVal.length() > maxAttr) {
                            addDiff(diffs, format("attribute %s with value %s is above maximum length of %d", attribute,
                                    strVal, maxAttr), ResultType.MODIFIED, tracker, schemaTracker);
                        }
                        break;
                    case REGEX:
                        if (!strVal.matches(entry.getValue())) {
                            addDiff(diffs, format("attribute %s with value %s does not match regex %s", attribute,
                                    strVal, entry.getValue()), ResultType.MODIFIED, tracker, schemaTracker);
                        }
                        break;
                    default:
                        LOG.warn("Unknown validation key: {}", entry.getKey());
                        break;
                }
            }
        }
    }

    /*
        validate that the given value is a valid uuid
     */
    private void validateUuid(String attribute, Object value, List<DiffMessage> diffs, Stack<Object> tracker,
                              Stack<Object> schemaTracker) {
        if (value instanceof String) {
            Matcher matcher = UUID_PATTERN.matcher((String) value);
            if (!matcher.matches()) {
                addDiff(diffs, format("'%s' with value '%s' is not a valid uuid", attribute, value),
                        ResultType.MODIFIED, tracker, schemaTracker);
            }
        } else {
            addDiff(diffs, format("%s with value %s is not of type uuid", attribute, value),
                    ResultType.MODIFIED, tracker, schemaTracker);
        }
    }

    /*
        validate that the given value is of the expected type and that it
        satisfies any other validation rules specified
     */
    private void validateValue(String attribute, Object value, Object validation, List<DiffMessage> diffs,
                               Stack<Object> tracker, Stack<Object> schemaTracker)
            throws ComparatorException {
        Map<String, String> validations = new HashMap<>();
        if (validation instanceof String) {
            validations = getValidations(validation);
        }
        String type = validations.get(TYPE);
        if (null != type) {
            switch (type) {
                case "int":
                    validateInt(attribute, value, validations, diffs, tracker, schemaTracker);
                    break;
                case "long":
                    validateLong(attribute, value, validations, diffs, tracker, schemaTracker);
                    break;
                case "date":
                    validateDate(attribute, value, validations, diffs, tracker, schemaTracker);
                    break;
                case "double":
                    validateDouble(attribute, value, validations, diffs, tracker, schemaTracker);
                    break;
                case "enum":
                    validateEnum(attribute, value, validations, diffs, tracker, schemaTracker);
                    break;
                case "bool":
                    validateBoolean(attribute, value, diffs, tracker, schemaTracker);
                    break;
                case "uuid":
                    validateUuid(attribute, value, diffs, tracker, schemaTracker);
                    break;
                case "string":
                    validateString(attribute, value, validations, diffs, tracker, schemaTracker);
                    break;
                default:
                    throw new ComparatorException(format("unsupported attribute type '%s'", type));
            }
        } else {
            if (value.getClass().equals(validation.getClass())) {
                if (!value.equals(validation)) {
                    addDiff(diffs, "Node values are different", ResultType.SIMILAR, tracker, schemaTracker);
                }
            } else {
                addDiff(diffs, "Nodes have different types", ResultType.MODIFIED, tracker, schemaTracker);
            }
        }
    }
}
