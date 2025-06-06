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

package org.qubership.automation.pc.core.utils.jsondiff;

import static java.lang.Math.min;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.core.helpers.JSONUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Copied from package com.flipkart.zjsonpatch; public final class JsonDiff In order to tune comparison of arrays.
 */
public final class JsonDiffTuned {

    public final EncodePathFunction encodePathFunction = new EncodePathFunction();
    private boolean ignoreArrayElementsOrder = false;
    private Map<String, String> objectPrimaryKeysMap = new HashMap<>();

    private boolean disableTypeCheckIfRegexp = true;

    private final class EncodePathFunction implements Function<Object, String> {

        @Override
        public String apply(Object object) {
            String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
            return path.replaceAll("~", "~0").replaceAll("/", "~1");
        }
    }

    public JsonNode asJson(final JsonNode source, final JsonNode target, boolean ignoreOrder,
                           boolean disableTypeCheck, Map<String, String> keysMap) {
        objectPrimaryKeysMap.putAll(checkSlashInKey(keysMap));
        ignoreArrayElementsOrder = ignoreOrder;
        disableTypeCheckIfRegexp = disableTypeCheck;
        return asJson(source, target);
    }

    public JsonNode asJson(final JsonNode source, final JsonNode target) {
        final List<Diff> diffs = new ArrayList<>();
        List<Object> path = new LinkedList<>();
        /**
         * generating diffs in the order of their occurrence
         */
        generateDiffs(diffs, path, source, target);
        /**
         * Merging remove & add to move operation
         */
        //compactDiffs(diffs);
        return getJsonNodes(diffs);
    }

    /**
     * This method merge 2 diffs ( remove then add, or vice versa ) with same
     * value into one Move operation, all the core logic resides here only.
     */
    @Deprecated
    private void compactDiffs(List<Diff> diffs) {
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff1 = diffs.get(i);
            // if not remove OR add, move to next diff
            if (!(Operation.REMOVE.equals(diff1.getOperation())
                    || Operation.ADD.equals(diff1.getOperation()))) {
                continue;
            }
            for (int j = i + 1; j < diffs.size(); j++) {
                Diff diff2 = diffs.get(j);
                if (!diff1.getValue().equals(diff2.getValue())) {
                    continue;
                }
                Diff moveDiff = null;
                if (Operation.REMOVE.equals(diff1.getOperation())
                        && Operation.ADD.equals(diff2.getOperation())) {
                    computeRelativePath(diff2.getPath(), i + 1, j - 1, diffs);
                    moveDiff = new Diff(Operation.MOVE, diff1.getPath(), diff2.getValue(), diff2.getPath());
                } else if (Operation.ADD.equals(diff1.getOperation())
                        && Operation.REMOVE.equals(diff2.getOperation())) {
                    computeRelativePath(diff2.getPath(), i, j - 1, diffs); // diff1's add should also be considered
                    moveDiff = new Diff(Operation.MOVE, diff2.getPath(), diff1.getValue(), diff1.getPath());
                }
                if (moveDiff != null) {
                    diffs.remove(j);
                    diffs.set(i, moveDiff);
                    break;
                }
            }
        }
    }

    //Note : only to be used for arrays
    //Finds the longest common Ancestor ending at Array
    private void computeRelativePath(List<Object> path, int startIdx, int endIdx, List<Diff> diffs) {
        List<Integer> counters = new ArrayList<>();
        resetCounters(counters, path.size());
        for (int i = startIdx; i <= endIdx; i++) {
            Diff diff = diffs.get(i);
            //Adjust relative path according to #ADD and #Remove
            if (Operation.ADD.equals(diff.getOperation()) || Operation.REMOVE.equals(diff.getOperation())) {
                updatePath(path, diff, counters);
            }
        }
        updatePathWithCounters(counters, path);
    }

    private void resetCounters(List<Integer> counters, int size) {
        for (int i = 0; i < size; i++) {
            counters.add(0);
        }
    }

    private void updatePathWithCounters(List<Integer> counters, List<Object> path) {
        for (int i = 0; i < counters.size(); i++) {
            int value = counters.get(i);
            if (value != 0) {
                Integer currValue = Integer.parseInt(path.get(i).toString());
                path.set(i, String.valueOf(currValue + value));
            }
        }
    }

    private void updatePath(List<Object> path, Diff pseudo, List<Integer> counters) {
        //find longest common prefix of both the paths
        if (pseudo.getPath().size() <= path.size()) {
            int idx = -1;
            for (int i = 0; i < pseudo.getPath().size() - 1; i++) {
                if (pseudo.getPath().get(i).equals(path.get(i))) {
                    idx = i;
                } else {
                    break;
                }
            }
            if (idx == pseudo.getPath().size() - 2) {
                if (pseudo.getPath().get(pseudo.getPath().size() - 1) instanceof Integer) {
                    updateCounters(pseudo, pseudo.getPath().size() - 1, counters);
                }
            }
        }
    }

    private void updateCounters(Diff pseudo, int idx, List<Integer> counters) {
        if (Operation.ADD.equals(pseudo.getOperation())) {
            counters.set(idx, counters.get(idx) - 1);
        } else {
            if (Operation.REMOVE.equals(pseudo.getOperation())) {
                counters.set(idx, counters.get(idx) + 1);
            }
        }
    }

    private ArrayNode getJsonNodes(List<Diff> diffs) {
        JsonNodeFactory factory = JsonNodeFactory.instance;
        final ArrayNode patch = factory.arrayNode();
        for (Diff diff : diffs) {
            ObjectNode jsonNode = getJsonNode(factory, diff);
            patch.add(jsonNode);
        }
        return patch;
    }

    private ObjectNode getJsonNode(JsonNodeFactory factory, Diff diff) {
        ObjectNode jsonNode = factory.objectNode();
        jsonNode.put(Constants.OP, diff.getOperation().rfcName());
        jsonNode.put(Constants.PATH, getArrayNodeRepresentation(diff.getPath()));
        jsonNode.put(Constants.CONTROL_JSON_PATH, JSONUtils.listToJsonPath(diff.getPath()));
        if (diff.getToPath() != null && !diff.getToPath().isEmpty()) {
            jsonNode.put(Constants.FROM, getArrayNodeRepresentation(diff.getToPath())); // Added by KAG, 17/02/2017,
            jsonNode.put(Constants.TEST_JSON_PATH, JSONUtils.listToJsonPath(diff.getToPath()));
            // should be tested carefully
        }
        if (Operation.MOVE.equals(diff.getOperation())) {
            jsonNode.put(Constants.FROM, getArrayNodeRepresentation(diff.getPath())); //required {from} only in case
            // of Move Operation
            jsonNode.put(Constants.PATH, getArrayNodeRepresentation(diff.getToPath()));  // destination Path
            jsonNode.put(Constants.CONTROL_JSON_PATH, JSONUtils.listToJsonPath(diff.getToPath()));
            jsonNode.put(Constants.TEST_JSON_PATH, JSONUtils.listToJsonPath(diff.getPath()));
        }
        //if (!Operation.REMOVE.equals(diff.getOperation()) && !Operation.MOVE.equals(diff.getOperation())) { //
        // setting only for Non-Remove operation
        jsonNode.put(Constants.VALUE, diff.getValue().textValue());
        //}
        return jsonNode;
    }

    private String getArrayNodeRepresentation(List<Object> path) {
        return Joiner.on('/').appendTo(
                new StringBuilder().append('/'),
                Iterables.transform(path, encodePathFunction)
        ).toString();
    }

    private String getArrayNodeRepresentation(List<Object> path, Map<String, String> objectPrimaryKeysMap) {
        String strPath = Joiner.on('/').appendTo(
                new StringBuilder().append('/'),
                Iterables.transform(path, encodePathFunction)
        ).toString();
        return checkPrimaryKey(objectPrimaryKeysMap, strPath);
    }

    private void generateDiffs(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        final NodeType sourceType = NodeType.getNodeType(source);
        final NodeType targetType = NodeType.getNodeType(target);
        boolean isRequiredRegexpCheck = false;
        if (sourceType == NodeType.STRING && source.textValue() != null && source.textValue().startsWith("regexp:")) {
            isRequiredRegexpCheck = true;
        }
        if (sourceType != targetType && (!isRequiredRegexpCheck || !disableTypeCheckIfRegexp)) {
            diffs.add(Diff.generateDiff(Operation.TYPE_NOT_MATCHED, path, target));
        } else {
            if (!source.equals(target)) {
                if (sourceType == NodeType.ARRAY && targetType == NodeType.ARRAY) {
                    //both are arrays
                    //compareArray(diffs, path, source, target);
                    newCompareArray(diffs, path, source, path, target);
                } else if (sourceType == NodeType.OBJECT && targetType == NodeType.OBJECT) {
                    //both are json
                    compareObjects(diffs, path, source, path, target);
                } else {
                    //can be replaced
                    diffs.add(Diff.generateDiff(Operation.REPLACE, path, target));
                }
            }
        }
    }

    private void generateDiffs(List<Diff> diffs, List<Object> sourcePath, JsonNode source, List<Object> targetPath,
                               JsonNode target) {
        if (sourcePath.equals(targetPath)) {
            generateDiffs(diffs, sourcePath, source, target);
        } else {
            final NodeType sourceType = NodeType.getNodeType(source);
            final NodeType targetType = NodeType.getNodeType(target);
            boolean isRequiredRegexpCheck = false;
            if (sourceType == NodeType.STRING && source.textValue() != null && source.textValue().startsWith("regexp"
                    + ":")) {
                isRequiredRegexpCheck = true;
            }
            if (sourceType != targetType && (!isRequiredRegexpCheck || !disableTypeCheckIfRegexp)) {
                diffs.add(new Diff(Operation.TYPE_NOT_MATCHED, sourcePath, target, targetPath));
            } else {
                if (!source.equals(target)) {
                    if (sourceType == NodeType.ARRAY && targetType == NodeType.ARRAY) {
                        newCompareArray(diffs, sourcePath, source, targetPath, target);
                    } else if (sourceType == NodeType.OBJECT && targetType == NodeType.OBJECT) {
                        compareObjects(diffs, sourcePath, source, targetPath, target);
                    } else {
                        diffs.add(new Diff(Operation.REPLACE, sourcePath, target, targetPath));
                    }
                }
            }
        }
    }

    @Deprecated
    private void compareArray(List<Diff> diffs, List<Object> path, JsonNode source, JsonNode target) {
        List<JsonNode> lcs = getLcs(source, target);
        int srcIdx = 0;
        int targetIdx = 0;
        int lcsIdx = 0;
        int srcSize = source.size();
        int targetSize = target.size();
        int lcsSize = lcs.size();
        int pos = 0;
        while (lcsIdx < lcsSize) {
            JsonNode lcsNode = lcs.get(lcsIdx);
            JsonNode srcNode = source.get(srcIdx);
            JsonNode targetNode = target.get(targetIdx);
            if (lcsNode.equals(srcNode) && lcsNode.equals(targetNode)) { // Both are same as lcs node, nothing to do
                // here
                srcIdx++;
                targetIdx++;
                lcsIdx++;
                pos++;
            } else {
                if (lcsNode.equals(srcNode)) { // src node is same as lcs, but not targetNode
                    //addition
                    List<Object> currPath = getPath(path, pos);
                    diffs.add(Diff.generateDiff(Operation.ADD, currPath, targetNode));
                    pos++;
                    targetIdx++;
                } else if (lcsNode.equals(targetNode)) { //targetNode node is same as lcs, but not src
                    //removal,
                    List<Object> currPath = getPath(path, pos);
                    diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, srcNode));
                    srcIdx++;
                } else {
                    List<Object> currPath = getPath(path, pos);
                    //both are unequal to lcs node
                    generateDiffs(diffs, currPath, srcNode, targetNode);
                    srcIdx++;
                    targetIdx++;
                    pos++;
                }
            }
        }
        while ((srcIdx < srcSize) && (targetIdx < targetSize)) {
            JsonNode srcNode = source.get(srcIdx);
            JsonNode targetNode = target.get(targetIdx);
            List<Object> currPath = getPath(path, pos);
            generateDiffs(diffs, currPath, srcNode, targetNode);
            srcIdx++;
            targetIdx++;
            pos++;
        }
        pos = addRemaining(diffs, path, target, pos, targetIdx, targetSize);
        removeRemaining(diffs, path, pos, srcIdx, srcSize, source);
    }

    private void newCompareArray(List<Diff> diffs, List<Object> sourcePath, JsonNode source, List<Object> targetPath,
                                 JsonNode target) {
        HashSet<Integer> matchingTarget = new HashSet<>();
        HashSet<Integer> matchingSource = new HashSet<>();
        int srcSize = source.size();
        int targetSize = target.size();
        String strPath = getArrayNodeRepresentation(sourcePath, objectPrimaryKeysMap);
        boolean comparedWithPK = false;
        if (ignoreArrayElementsOrder) {
            for (int i = 0; i < srcSize; i++) {
                JsonNode srcNode = source.get(i);
                for (int j = 0; j < targetSize; j++) {
                    if (!matchingTarget.contains(j)) {
                        JsonNode targetNode = target.get(j);
                        if (srcNode.equals(targetNode)) {
                            matchingTarget.add(j);
                            matchingSource.add(i);
                            break;
                        }
                    }
                }
            }
            if (objectPrimaryKeysMap.containsKey(strPath)) {
                String pk = objectPrimaryKeysMap.get(strPath);
                comparedWithPK = true;
                // Loop through remaining source nodes and try to find corresponding target nodes
                // 'Corresponding' in this context means having property <pk> with equal value
                if (matchingSource.size() < srcSize) {
                    for (int i = 0; i < srcSize; i++) {
                        if (!matchingSource.contains(i)) {
                            JsonNode srcNode = source.get(i);
                            List<Object> srcPath = getPath(sourcePath, i);
                            Map<String, JsonNode> pkSrcNodes = new HashMap<>();
                            findNodesByPath(srcNode, pk, pkSrcNodes, false);
                            if (pkSrcNodes == null || pkSrcNodes.isEmpty()) {
                                diffs.add(Diff.generateDiff(Operation.REMOVE, srcPath, srcNode));
                                matchingSource.add(i);
                            } else {
                                for (int j = 0; j < targetSize; j++) {
                                    if (!matchingTarget.contains(j)) {
                                        JsonNode targetNode = target.get(j);
                                        Map<String, JsonNode> pkTgtNodes = new HashMap<>();
                                        findNodesByPath(targetNode, pk, pkTgtNodes, false);
                                        List<Object> tgtPath = getPath(targetPath, j);
                                        if (pkTgtNodes == null || pkSrcNodes.isEmpty()) {
                                            diffs.add(Diff.generateDiff(Operation.ADD, tgtPath, targetNode));
                                            matchingTarget.add(j);
                                        } else {
                                            if (pkSrcNodes.equals(pkTgtNodes)) {
                                                generateDiffs(diffs, srcPath, srcNode, tgtPath, targetNode);
                                                matchingTarget.add(j);
                                                matchingSource.add(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Special feature for the situation when there is only one array element in source array
            // and only one array element in target array
            // Assume they are correspond each other and we should perform more deep comparison
            if ((srcSize == 1 && targetSize == 1 && matchingSource.isEmpty() && matchingTarget.isEmpty())
                    || !comparedWithPK) {
                for (int i = 0; i < srcSize; i++) {
                    if (!matchingSource.contains(i)) {
                        for (int j = 0; j < targetSize; j++) {
                            if (!matchingTarget.contains(j)) {
                                JsonNode srcNode = source.get(i);
                                List<Object> srcPath = getPath(sourcePath, i);
                                JsonNode targetNode = target.get(j);
                                List<Object> tgtPath = getPath(targetPath, j);
                                generateDiffs(diffs, srcPath, srcNode, tgtPath, targetNode);
                                matchingTarget.add(j);
                                matchingSource.add(i);
                                break;
                            }
                        }
                    }
                }
            }
            if (matchingSource.size() < srcSize) {
                for (int i = 0; i < srcSize; i++) {
                    if (!matchingSource.contains(i)) {
                        JsonNode srcNode = source.get(i);
                        List<Object> currPath = getPath(sourcePath, i);
                        diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, srcNode));
                    }
                }
            }
            if (matchingTarget.size() < targetSize) {
                for (int i = 0; i < targetSize; i++) {
                    if (!matchingTarget.contains(i)) {
                        JsonNode targetNode = target.get(i);
                        List<Object> currPath = getPath(targetPath, i);
                        diffs.add(Diff.generateDiff(Operation.ADD, currPath, targetNode));
                    }
                }
            }
        } else {
            // 1st source element corresponds to 1st target element, 2nd - 2nd and so on
            int idx = 0;
            for (; idx < min(srcSize, targetSize); idx++) {
                JsonNode srcNode = source.get(idx);
                JsonNode targetNode = target.get(idx);
                if (!srcNode.equals(targetNode)) {
                    List<Object> currPath = getPath(sourcePath, idx);
                    generateDiffs(diffs, currPath, srcNode, targetNode);
                }
            }
            for (int i = idx; i < srcSize; i++) {
                JsonNode srcNode = source.get(i);
                List<Object> currPath = getPath(sourcePath, i);
                diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, srcNode));
            }
            for (int i = idx; i < targetSize; i++) {
                JsonNode targetNode = target.get(i);
                List<Object> currPath = getPath(sourcePath, i);
                diffs.add(Diff.generateDiff(Operation.ADD, currPath, targetNode));
            }
        }
    }

    public static JsonNode findNodeByPath(JsonNode src, String path) {
        if (path.contains("/")) {
            String target = StringUtils.substringBefore(path, "/");
            if (src.has(target) && src.get(target).getNodeType() == JsonNodeType.OBJECT) {
                return findNodeByPath(src.get(target), StringUtils.substringAfter(path, "/"));
            }
        } else if (src.has(path) && !src.get(path).isContainerNode()) {
            return src.get(path);
        }
        return null;
    }

    public static Map<String, JsonNode> findNodesByPath(JsonNode src,
                                                        String path,
                                                        Map<String, JsonNode> pkNodes,
                                                        Boolean isArrayAllowed) {
        // Expanded rule objectPrimaryKey to work with complex PKs
        String[] keys = path.split("&&");
        for (String key : keys) {
            if (key.contains("/")) {
                String[]  pathParts = key.split("/");
                String target = pathParts[0];
                if (src.has(target)) {
                    if (src.get(target).getNodeType() == JsonNodeType.OBJECT) {
                       findNodesByPath(src.get(target),
                               StringUtils.substringAfter(key, "/"), pkNodes, isArrayAllowed);
                    } else if (src.get(target).getNodeType() == JsonNodeType.ARRAY) {
                        if (pathParts[1].matches("^\\d+$")) {
                            findNodesByPath(src.get(target).get(Integer.parseInt(pathParts[1])),
                                    StringUtils.substringAfter(key, pathParts[1] + "/"), pkNodes,
                                    true);
                        } else  if (pathParts[1].equals("*")) {
                            findNodesByPath(getNodeForAsteriskPrimaryKey(src.get(target), pathParts[2]),
                                    StringUtils.substringBefore(pathParts[2], "="), pkNodes,
                                    true);
                        }
                    }
                }
            } else {
                if (src != null && src.has(key) && (!src.get(key).isContainerNode() || isArrayAllowed)
                        && src.get(key).getNodeType() != JsonNodeType.NULL) {
                    pkNodes.put(key, src.get(key));
                } else {
                    pkNodes.clear();
                    return  null;
                }
            }
        }
        return pkNodes;
    }

    private static JsonNode getNodeForAsteriskPrimaryKey(JsonNode src, String lastKeyPart) {
        JsonNode elementNode = null;
        if ((lastKeyPart).contains("=")) {
            Pattern p = Pattern.compile("(.*?)=['\"](.*?[^\\\\])['\"]");
            Matcher m = p.matcher(lastKeyPart);
            if (m.matches()) {
                elementNode = StreamSupport.stream(src.spliterator(), false)
                        .filter(jo -> jo.get(m.group(1)).asText().equals(m.group(2)))
                        .findFirst().orElse(null);
            }
        } else {
            elementNode = getNodeWithValuesOfDefinedProperty(src,lastKeyPart);
        }
        return elementNode;
    }


    private static JsonNode getNodeWithValuesOfDefinedProperty(JsonNode src, String key) {
        List<JsonNode> dataNodes = src.findValues(key);
        List<JsonNode> sortedDataNodes = dataNodes
                .stream()
                .sorted(Comparator.comparing(o -> o.asText()))
                .collect(Collectors.toList());
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode sortedArrayNode = mapper.createObjectNode().arrayNode().addAll(sortedDataNodes);
        JsonNode arrayNode = mapper.createObjectNode().set(key, sortedArrayNode);
        return arrayNode;
    }

    public String checkPrimaryKey(Map<String, String> keysMap, String strPath) {
        String result = strPath;
        boolean resultContains = keysMap.containsKey(strPath);
        if (!resultContains) {
            List<String> testPathL = new ArrayList<>(Arrays.asList(strPath.split("/")));
            for (String key : keysMap.keySet()) {
                List<String> keyL = new ArrayList<>(Arrays.asList(key.split("/")));
                if (keyL.size() == testPathL.size()) {
                    Map<Integer, String> mapAsterisk =
                            IntStream.range(0, keyL.size())
                                    .boxed()
                                    .filter(i -> keyL.get(i).equals("*"))
                                    .collect(Collectors.toMap(i -> i, keyL::get));
                    boolean checkEqualsKeyAndPath = IntStream.range(0, keyL.size())
                            .boxed().noneMatch(i -> !keyL.get(i).equals("*") && !keyL.get(i).equals(testPathL.get(i)));
                    if (checkEqualsKeyAndPath && mapAsterisk.size() > 0) {
                        boolean checkAsteriskInPath = mapAsterisk.keySet().stream()
                                .filter(index
                                        -> StringUtils.isNumeric(testPathL.get(index))).count() == mapAsterisk.size();
                        if (checkAsteriskInPath) {
                            mapAsterisk.keySet().forEach(index -> testPathL.set(index, keyL.get(index)));
                            if (keyL.equals(testPathL)) {
                                result = Joiner.on("/").join(testPathL);
                                break;
                            }
                        }
                    }
                }

            }
        }
        return result;
    }

    private Integer removeRemaining(List<Diff> diffs, List<Object> path, int pos, int srcIdx, int srcSize,
                                    JsonNode source) {
        while (srcIdx < srcSize) {
            // 2nd parameter = pos - because original zjson's Diff oriented to applying them to source in order to
            // make target
            //  Thats why array members are removed not from ORIGINAL source array but from CHANGED source array!!!
            //  (after all ADD operations are applied)
            //  It's suitable if we need to make target but INCORRECT if we only need to highlight differences -
            //  array indexes can be bigger than array size(!)
            // Original line is commented by KAG (17/02/2017) because reasons mentioned above - uncommented back -
            // new 'compareArray' method will be implemented
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(srcIdx)));
            srcIdx++;
        }
        return pos;
    }

    private Integer addRemaining(List<Diff> diffs, List<Object> path, JsonNode target, int pos, int targetIdx,
                                 int targetSize) {
        while (targetIdx < targetSize) {
            JsonNode jsonNode = target.get(targetIdx);
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.ADD, currPath, jsonNode.deepCopy()));
            pos++;
            targetIdx++;
        }
        return pos;
    }

    private void compareObjects(List<Diff> diffs, List<Object> sourcePath, JsonNode source, List<Object> targetPath,
                                JsonNode target) {
        Iterator<String> keysFromSrc = source.fieldNames();
        while (keysFromSrc.hasNext()) {
            String key = keysFromSrc.next();
            if (!target.has(key)) {
                //remove case
                List<Object> currPath = getPath(sourcePath, key);
                diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.get(key)));
                continue;
            }
            List<Object> srcPath = getPath(sourcePath, key);
            List<Object> tgtPath = getPath(targetPath, key);
            generateDiffs(diffs, srcPath, source.get(key), tgtPath, target.get(key));
        }
        Iterator<String> keysFromTarget = target.fieldNames();
        while (keysFromTarget.hasNext()) {
            String key = keysFromTarget.next();
            if (!source.has(key)) {
                //add case
                List<Object> currPath = getPath(targetPath, key);
                diffs.add(Diff.generateDiff(Operation.ADD, currPath, target.get(key)));
            }
        }
    }

    private List<Object> getPath(List<Object> path, Object key) {
        List<Object> toReturn = new ArrayList<Object>();
        toReturn.addAll(path);
        toReturn.add(key);
        return toReturn;
    }

    private List<JsonNode> getLcs(final JsonNode first, final JsonNode second) {
        Preconditions.checkArgument(first.isArray(), "LCS can only work on JSON arrays");
        Preconditions.checkArgument(second.isArray(), "LCS can only work on JSON arrays");
        return ListUtils.longestCommonSubsequence(Lists.newArrayList(first), Lists.newArrayList(second));
    }

    private Map<String, String> checkSlashInKey(Map<String, String> keyMap) {
        Map<String, String> resultMap = new HashMap<>();
        keyMap.entrySet().stream().forEach(map -> {
            if (map.getKey().startsWith("/")) {
                resultMap.put(map.getKey(), map.getValue());
            } else {
                resultMap.put("/" + map.getKey(), map.getValue());
            }
        });
        return resultMap;
    }
}
