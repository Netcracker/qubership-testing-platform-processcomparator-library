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

package org.qubership.automation.pc.core.helpers;

import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_KEYS_CASE_INSENSITIVE;
import static org.qubership.automation.pc.comparator.enums.JsonComparatorParameters.PARAMETER_READ_BY_PATH;
import static org.qubership.automation.pc.core.helpers.BuildColoredJson.IHighlighterTags.BLANK;
import static org.qubership.automation.pc.core.helpers.BuildColoredJson.IHighlighterTags.ENDTAG;
import static org.qubership.automation.pc.core.helpers.BuildColoredJson.IHighlighterTags.START_EXTRA_TAG;
import static org.qubership.automation.pc.core.helpers.BuildColoredJson.IHighlighterTags.START_MISSED_TAG;
import static org.qubership.automation.pc.core.helpers.BuildColoredJson.IHighlighterTags.START_MODIFIED_TAG;
import static org.qubership.automation.pc.core.helpers.BuildColoredJson.IHighlighterTags.START_SIMILAR_TAG;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.utils.JsonComparatorUtils;
import org.qubership.automation.pc.models.HighlighterNode;
import org.qubership.automation.pc.models.HighlighterResult;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A utility class responsible for highlighting differences between two JSON documents
 * based on a provided list of {@link DiffMessage} objects.
 *
 * <p>
 * This class generates an HTML representation of the differences using span tags
 * with appropriate CSS classes (e.g., EXTRA, MISSED, MODIFIED, SIMILAR) to visually
 * distinguish between changed, added, removed, or similar content.
 * </p>
 *
 * <p>
 * It supports advanced features such as customizable diff rules, partial highlighting,
 * and optional support for invalid or "dirty" JSON content that may contain trailing
 * or leading non-JSON text.
 * </p>
 *
 * <p>
 * Output is encapsulated in a {@link HighlighterResult}, which contains
 * formatted HTML for both the expected and actual JSON inputs.
 * </p>
 *
 * <p>
 * This class is primarily used in JSON comparison tools for user-friendly visualization of differences.
 * </p>
 */
public class BuildColoredJson {

    public static HighlighterResult highlightByRules(List<DiffMessage> differences, String er, String ar, Map<String,
            List<String>> rules) throws ComparatorException {
        Parameters parameters = Objects.isNull(rules) ? new Parameters() : new Parameters(rules);
        DirtyJson erDirtyJson = new DirtyJson(er);
        DirtyJson arDirtyJson = new DirtyJson(ar);
        String erMessage = (StringUtils.isBlank(er)) ? "" : processDifferences(differences, erDirtyJson.jsonText,
                false, parameters);
        String arMessage = (StringUtils.isBlank(ar)) ? "" : processDifferences(differences, arDirtyJson.jsonText,
                true, parameters);

        HighlighterNode erNode = new HighlighterNode();
        HighlighterNode arNode = new HighlighterNode();
        erNode.setValue(erMessage);
        erNode.setIsPlain(true);
        arNode.setValue(arMessage);
        arNode.setIsPlain(true);
        HighlighterResult resultMap = new HighlighterResult();
        resultMap.setEr(erNode);
        resultMap.setAr(arNode);
        return resultMap;
    }

    public static HighlighterResult highlight(List<DiffMessage> differences,
                                              String er,
                                              String ar) throws ComparatorException {
        return highlightByRules(differences, er, ar, Collections.emptyMap());
    }

    private static String diffGetText(DiffMessage diff, Boolean isActual) {
        return isActual ? diff.getActual() : diff.getExpected();
    }

    private static String processDifferences(List<DiffMessage> differences,
                                             String docEar, Boolean
                                                     isActualResult,
                                             Parameters parameters) throws ComparatorException {
        if (docEar.isEmpty()) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre></pre>";
        }

        List<String> parameterReadByPathValue = PARAMETER_READ_BY_PATH.getValue(parameters);
        String readByPath = Objects.isNull(parameterReadByPathValue) || parameterReadByPathValue.isEmpty()
                ? null : parameterReadByPathValue.get(0);
        boolean keysCaseInsensitive = PARAMETER_KEYS_CASE_INSENSITIVE.getValue(parameters);

        try {
            JsonNode jsonNodeEar = JsonComparatorUtils.readJsonNodeFromString(docEar, readByPath,
                    keysCaseInsensitive).get();
            final JsonNode jsonNodeEarArray = JsonComparatorUtils.readJsonNodeFromString(docEar, readByPath,
                    keysCaseInsensitive).get();
            // 1st loop through differences - sort paths in ascending order
            List<DiffMessage> diffs = new ArrayList<>();
            for (DiffMessage diff : differences) {
                String diffPath = diffGetText(diff, isActualResult);
                if (diffPath.isEmpty() || diff.getResult() == ResultType.SKIPPED) {
                    continue;
                }
                DiffMessage newDiff = new DiffMessage(diff);
                if (isActualResult) {
                    // simplification. Sorting and highlighting below are based on .expected
                    newDiff.setExpected(diffPath);
                }
                diffs.add(newDiff);
            }

            Collections.sort(diffs, new Comparator<DiffMessage>() {
                @Override
                public int compare(DiffMessage df1, DiffMessage df2) {
                    String path1 = df1.getExpected();
                    String path2 = df2.getExpected();
                    if (path1.compareTo(path2) == 0) {
                        return 0;
                    }
                    while (!path1.isEmpty() && !path2.isEmpty()) {
                        String pathNode1 = path1.indexOf('/') != -1 ? path1.substring(0, path1.indexOf('/')) : path1;
                        path1 = path1.indexOf('/') != -1 ? path1.substring(path1.indexOf('/') + 1) : "";

                        String pathNode2 = path2.indexOf('/') != -1 ? path2.substring(0, path2.indexOf('/')) : path2;
                        path2 = path2.indexOf('/') != -1 ? path2.substring(path2.indexOf('/') + 1) : "";

                        if (pathNode1.compareTo(pathNode2) == 0) {
                            continue;
                        }
                        try {
                            if (Integer.parseInt(pathNode1) > Integer.parseInt(pathNode2)) {
                                return 1;
                            } else {
                                return -1;
                            }
                        } catch (NumberFormatException e) {
                            return pathNode1.compareTo(pathNode2);
                        }
                    }
                    return path1.isEmpty() ? -1 : 1;
                }
            });

            // Process differences in REVERSE order - because arrays' diffs are processed via inserting of elements.
            // That's why all LATER xpathes become incorrect
            boolean highlightRoot = false;

            String rootResult = "";
            String rootDescription = "";
            for (int diffIndex = diffs.size() - 1; diffIndex >= 0; diffIndex--) {
                DiffMessage diff = diffs.get(diffIndex);
                String diffPath = diff.getExpected();
                String diffResult = diff.getResult().toString();
                String diffDescription = diff.getDescription();
                boolean missedInRoot = diffPath.equals("/") && diff.getResult() == ResultType.MISSED;
                if (diffPath.equals("/") && !missedInRoot) {
                    highlightRoot = true;
                    rootResult = diffResult;
                    rootDescription = ((rootDescription.isEmpty()) ? "" : rootDescription + "; ")
                            + diff.getDescription();
                }
                // in case the handling of VALIDATE_AS_SIMPLE_SCHEMA result and diffs list is contains diffs with
                // similar expected jsonPath, we should skip highlight of these diff in er except one
                if (!isActualResult) {
                    // get diffs list with similar expected jsonPath
                    List<DiffMessage> sameDiffs = differences.stream()
                            .filter(diffMessage -> diffMessage.getExpected().equals(diff.getExpected()))
                            .collect(Collectors.toList());
                    if (sameDiffs.size() > 0) {
                        // if this diff is not last from similar diffs, then skip it
                        if (sameDiffs.stream()
                                .anyMatch(diffMessage -> diffMessage.getOrderId() < (diff.getOrderId()))) {
                            continue;
                        }
                        // if similar diffs are have difference result type, set MODIFIED type as common result type
                        // for similar diffs
                        if (!sameDiffs.stream()
                                .allMatch(diffMessage -> diffMessage.getResult().equals(diff.getResult()))) {
                            diff.setResult(ResultType.MODIFIED);
                        }
                    }
                }

                String titleWithDescription = "#BV_TITLE_" + diffDescription + "_BV_TITLE#";
                JsonNode jnArray = jsonNodeEarArray.at(diffPath);
                JsonNode jn = jsonNodeEar.at(diffPath);
                if (jn.isMissingNode() && !missedInRoot) {
                    continue;
                }
                String pathToLastNode = diffPath.substring(0, diffPath.lastIndexOf("/"));
                String lastNodeInPath = diffPath.substring(diffPath.lastIndexOf("/") + 1);
                switch (diff.getResult()) {
                    case SIMILAR:  // operation == "replace"
                        switch (jsonNodeEar.at(pathToLastNode).getNodeType()) {
                            case ARRAY:
                                ArrayNode an = ((ArrayNode) jsonNodeEar.at(pathToLastNode));
                                an.insert(Integer.parseInt(lastNodeInPath) + 1, "#EndArrayElementHighlight#");
                                an.insert(Integer.parseInt(lastNodeInPath) + 1, titleWithDescription);
                                an.insert(Integer.parseInt(lastNodeInPath),
                                        "#StartArrayElementHighlight" + diffResult + "#");
                                break;
                            default:
                                if (jn.isArray()) {
                                    ((ArrayNode) jn).add(titleWithDescription);
                                    ((ArrayNode) jn).add("#EndArrayHighlight#");
                                    ((ArrayNode) jn).insert(0, "#StartArrayHighlight" + diffResult + "#");
                                } else if (jn.isObject()) {
                                    ((ObjectNode) jsonNodeEar.at(pathToLastNode))
                                            .put(lastNodeInPath, "ObjectNode"
                                                    + diffResult + jn.toString() + "EndObjectNode");
                                } else {
                                    ((ObjectNode) jsonNodeEar.at(pathToLastNode))
                                            .put(lastNodeInPath, "TextNode"
                                                    + diffResult + jn.toString() + "EndTextNode");
                                }
                        }
                        break;
                    case MISSED:   // operation == "remove"
                        if (isActualResult) {
                            JsonNode missedNodesParent = missedInRoot ? jsonNodeEar : jsonNodeEar.at(diffPath);
                            switch (missedNodesParent.getNodeType()) {
                                case ARRAY:
                                    break;
                                default:
                                    ((ObjectNode) missedNodesParent).put(diff.getActualValue(),
                                            "_" + diffResult + "_" + " " + "_END_"
                                                    + diffResult + "_\"" + ", \"" + titleWithDescription);
                            }
                            break;
                        }
                        // fall through
                    case EXTRA:    // operation == "add"
                    case MODIFIED: // operation == "move"
                        switch (jsonNodeEar.at(pathToLastNode).getNodeType()) {
                            case ARRAY:
                                int i = Integer.parseInt(lastNodeInPath);
                                ArrayNode an = ((ArrayNode) jsonNodeEar.at(pathToLastNode));
                                an.insert(i + 1, "#EndArrayElementHighlight#");
                                an.insert(i, titleWithDescription);
                                an.insert(i, "#StartArrayElementHighlight" + diffResult + "#");
                                break;
                            default:
                                if (jn.isArray()) {
                                    ((ArrayNode) jn).add(titleWithDescription);
                                    ((ArrayNode) jn).add("#EndArrayHighlight#");
                                    ((ArrayNode) jn).insert(0, "#StartArrayHighlight" + diffResult + "#");
                                } else {
                                    ((ObjectNode) jsonNodeEar.at(pathToLastNode))
                                            .put(lastNodeInPath,"_" + diffResult + "_" + jn.toString() + "_END_"
                                                    + diffResult + "_\"" + ", \"" + titleWithDescription);
                                }
                        }
                        break;
                    default:
                        break;
                }
            }

            final String resultmsg = getObjectWriter()
                    .writeValueAsString(jsonNodeEar)
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");

            StringBuilder sb = new StringBuilder();
            if (highlightRoot) {
                sb.append(starthighlightTag(rootResult, rootDescription))
                        .append(resultmsg.replace("\\\"", "\""))
                        .append(ENDTAG);
            } else {
                for (String part : resultmsg.split("\n\r|\n|\r")) {
                    if (part.isEmpty()) {
                        sb.append("<br>");
                        continue;
                    }
                    part = part.replace("\\\"", "\"");

                    part = replaceTag(part, "EndObjectNode\"", ENDTAG, BLANK, BLANK);
                    part = replaceTag(part, "EndTextNode\"", ENDTAG, BLANK, BLANK);
                    // + toPrettyFormat(part)
                    part = replaceTag(part, "\"ObjectNodeSIMILAR", START_SIMILAR_TAG, BLANK, BLANK);
                    part = replaceTag(part, "\"TextNodeSIMILAR", START_SIMILAR_TAG, BLANK, BLANK);

                    part = replaceTag(part, "_END_MISSED_\"", BLANK, BLANK, ENDTAG);
                    part = replaceTag(part, "_END_MODIFIED_\"", BLANK, BLANK, ENDTAG);
                    part = replaceTag(part, "_END_EXTRA_\"", BLANK, BLANK, ENDTAG);
                    part = replaceTag(part, "_END_SIMILAR_\"", BLANK, BLANK, ENDTAG);

                    part = replaceTag(part, "\"_EXTRA_", BLANK, START_EXTRA_TAG, BLANK);
                    part = replaceTag(part, "\"_MISSED_", BLANK, START_MISSED_TAG, BLANK);
                    part = replaceTag(part, "\"_MODIFIED_", BLANK, START_MODIFIED_TAG, BLANK);
                    part = replaceTag(part, "\"_SIMILAR_", BLANK, START_SIMILAR_TAG, BLANK);

                    part = part.replace("\"#StartArrayElementHighlightEXTRA#\",", START_EXTRA_TAG);
                    part = part.replace("\"#StartArrayElementHighlightMISSED#\",", START_MISSED_TAG);
                    part = part.replace("\"#StartArrayElementHighlightMODIFIED#\",", START_MODIFIED_TAG);
                    part = part.replace("\"#StartArrayElementHighlightSIMILAR#\",", START_SIMILAR_TAG);
                    part = part.replace("\"#EndArrayElementHighlight#\",", ENDTAG);
                    part = part.replace("\"#EndArrayElementHighlight#\"", ENDTAG);
                    part = part.replace(", " + ENDTAG + " ]", " " + ENDTAG + " ]");

                    part = replaceTag(part, "\"#StartArrayHighlightEXTRA#\",", BLANK, START_EXTRA_TAG, BLANK);
                    part = replaceTag(part, "\"#StartArrayHighlightMISSED#\",", BLANK, START_MISSED_TAG, BLANK);
                    part = replaceTag(part, "\"#StartArrayHighlightMODIFIED#\",", BLANK, START_MODIFIED_TAG, BLANK);
                    part = replaceTag(part, "\"#StartArrayHighlightSIMILAR#\",", BLANK, START_SIMILAR_TAG, BLANK);

                    part = part.replace(", \"#EndArrayHighlight#\" ]", "]" + ENDTAG);
                    part = part.replace("\"#EndArrayHighlight#\" ]", "]" + ENDTAG);
                    sb.append(part);
                }
            }
            String finalOutput = replaceDefaultTitle(sb.toString());
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>" + finalOutput + "</pre>";
        } catch (IndexOutOfBoundsException | IOException e) {
            throw new ComparatorException("Error while parsing input message "
                    + (isActualResult ? "AR" : "ER") + ". Probably it is not valid JSON.", e);
        }
    }

    private static ObjectWriter getObjectWriter() {
        //to avoid errors in unix highlighting - use always windows structure with \r\n to show line breaks
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentObjectsWith(new DefaultIndenter().withLinefeed("\r\n"));

        return new ObjectMapper().writer(prettyPrinter);
    }

    private static String starthighlightTag(String result, String description) {
        return String.format("<span data-block-id=\"pc-highlight-block\" class=\"%s\" title=\"%s\">",
                (result.isEmpty()) ? "ERROR" : result, description);
    }

    private static String replaceTag(String str, String inTag, String outTag, String htmlStartTag, String htmlEndTag) {
        if (str.contains(inTag)) {
            return htmlStartTag + str.replace(inTag, outTag) + htmlEndTag;
        } else {
            return str;
        }
    }

    protected static String replaceDefaultTitle(String input) {
        Pattern spanPattern = Pattern.compile(
                "<span[^>]*?title=\"(#DEFAULT_TITLE#(.*?)#END_DEFAULT_TITLE#)\"[^>]*?>(.*?)</span>",
                Pattern.DOTALL);
        Pattern descriptionPattern = Pattern.compile("(?:,|)\\s\"#BV_TITLE_(.*?)_BV_TITLE#\"(?:,\\s|)");

        Matcher spanMatcher = spanPattern.matcher(input);
        StringBuffer result = new StringBuffer();

        while (spanMatcher.find()) {
            String newSpan = getString(spanMatcher, descriptionPattern);
            spanMatcher.appendReplacement(result, Matcher.quoteReplacement(newSpan));
        }
        spanMatcher.appendTail(result);
        return result.toString();
    }

    private static String getString(Matcher spanMatcher, Pattern descriptionPattern) {
        String fullSpan = spanMatcher.group(0);
        String fullTitlePlaceholder = spanMatcher.group(1);
        String defaultTitle = spanMatcher.group(2);

        Matcher descMatcher = descriptionPattern.matcher(fullSpan);
        String newTitle;
        String newSpan;
        if (descMatcher.find()) {
            String description = descMatcher.group(1);
            newTitle = description.isEmpty() ? defaultTitle : description;
            newSpan = fullSpan
                    .replace(fullTitlePlaceholder, newTitle)
                    .replace(descMatcher.group(0), "");
        } else {
            newSpan = fullSpan.replace(fullTitlePlaceholder, defaultTitle);
        }
        return newSpan;
    }

    protected interface IHighlighterTags {

        String BLANK = "";
        String ENDTAG = "</span>";
        String START_EXTRA_TAG = "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" "
                + "title=\"#DEFAULT_TITLE#Extra property or object#END_DEFAULT_TITLE#\">";
        String START_MISSED_TAG = "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" "
                + "title=\"#DEFAULT_TITLE#Missed property or object#END_DEFAULT_TITLE#\">";
        String START_MODIFIED_TAG = "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" "
                + "title=\"#DEFAULT_TITLE#Modified property or object#END_DEFAULT_TITLE#\">";
        String START_SIMILAR_TAG = "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" "
                + "title=\"#DEFAULT_TITLE#Similar property or object#END_DEFAULT_TITLE#\">";
    }

    private static class DirtyJson {

        public String startText = "";
        public String jsonText = "";
        public String endText = "";

        public DirtyJson() {
        }

        public DirtyJson(String srcJson) {
            if (StringUtils.isBlank(srcJson)) {
                this.jsonText = "{}";
            } else {
                int startPos;
                int endPos;
                int startObject = srcJson.indexOf("{");
                int startArray = srcJson.indexOf("[");
                if (startObject == -1 && startArray == -1) {
                    this.jsonText = "{}"; // There is no Json here
                    this.startText = srcJson;
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
                        // This Json is NOT valid, but ... let highlighter throw an exception
                        this.jsonText = srcJson.substring(startPos);
                        this.startText = srcJson.substring(0, startPos);
                    } else {
                        this.jsonText = srcJson.substring(startPos, endPos + 1);
                        this.startText = srcJson.substring(0, startPos);
                        this.endText = srcJson.substring(endPos + 1);
                    }
                }
            }
        }
    }

}
