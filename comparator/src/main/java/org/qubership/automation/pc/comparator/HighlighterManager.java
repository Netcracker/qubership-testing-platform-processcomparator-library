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

package org.qubership.automation.pc.comparator;

import static org.qubership.automation.pc.comparator.ComparatorManager.COMPARE_AS;
import static org.qubership.automation.pc.comparator.ComparatorManager.PARAMETER_ER_SUBSTITUTION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.helpers.BuildColoredCsv;
import org.qubership.automation.pc.core.helpers.BuildColoredJson;
import org.qubership.automation.pc.core.helpers.BuildColoredTable;
import org.qubership.automation.pc.core.helpers.BuildColoredText;
import org.qubership.automation.pc.core.helpers.BuildColoredXML;
import org.qubership.automation.pc.core.helpers.BuildColoredXsdXML;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.helpers.XmlHelpers;
import org.qubership.automation.pc.core.utils.ComparatorUtils;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.models.HighlighterResult;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class HighlighterManager {

    private static final String PARAMETER_NAME_CHECK_CONTEXT = "checkContext";

    public List<HighlighterResult> highlight(JsonArray arrayContext) {
        List<HighlighterResult> resultList = new ArrayList<>();
        for (int hlDiffIndex = 0; hlDiffIndex < arrayContext.size(); hlDiffIndex++) {
            resultList.add(highlightContextObject(arrayContext.get(hlDiffIndex).getAsJsonObject()));
        }
        return resultList;
    }

    public HighlighterResult highlightContextObject(JsonObject jsonObject) {
        HighlighterResult resultMap = new HighlighterResult();
        if (!jsonObject.has("er")
                || !jsonObject.has("ar")
                || !jsonObject.has("diffs")) {
            resultMap.setStatusCode("20000");
            resultMap.setStatusMessage("All fields (er[Base64 Encoded String], "
                    + "ar[Base64 Encoded String], diffs[Array]) required!");
            return resultMap;
        }
        if (jsonObject.has("summaryResult") && jsonObject.has("summaryMessage")) {
            String summaryResult = jsonObject.get("summaryResult").getAsString();
            if (summaryResult.equals("ERROR")) {
                resultMap.setStatusCode("20000");
                resultMap.setStatusMessage(jsonObject.get("summaryMessage").getAsString());
                return resultMap;
            }
        }

        List<DiffMessage> differs = new ArrayList<>();
        if (jsonObject.get("diffs").isJsonArray()) {
            Gson gson = new Gson();
            differs = gson.fromJson(jsonObject.get("diffs"), new TypeToken<List<DiffMessage>>() {
            }.getType());
        }

        Map<String, List<String>> rules = new HashMap<>();
        JsonArray rulesArray = new JsonArray();
        if (jsonObject.has("rules")) {
            rulesArray = jsonObject.getAsJsonArray("rules");
            for (int i = 0; i < rulesArray.size(); i++) {
                JsonObject jsRule = rulesArray.get(i).getAsJsonObject();
                if (!jsRule.has("name") || !jsRule.has("value")) {
                    continue; // Incorrect rule format; don't throw exception
                }
                String ruleName = jsRule.get("name").getAsString().trim();
                String ruleValue = jsRule.get("value").getAsString().trim();
                if (ruleName.isEmpty() || ruleValue.isEmpty()) {
                    continue; // Empty rule; don't throw exception
                }

                List<String> valuesList = rules.get(ruleName);
                if (valuesList == null) {
                    valuesList = new ArrayList<>();
                }
                valuesList.add(ruleValue);
                rules.put(ruleName, valuesList);
            }
        }

        String contentType = "PRIMITIVES";
        if (jsonObject.has("contentType")) {
            contentType = jsonObject.get("contentType").getAsString();
        }
        if (rules.containsKey(COMPARE_AS)) {
            String newContentType = rules.get(COMPARE_AS).get(0);
            if (EnumUtils.isValidEnum(DataContentType.class, newContentType)) {
                contentType = newContentType;
            }
        }
        if (jsonObject.has("transformOnly") && contentType.equals("XML")) {
            contentType = "XML2Transform";
        }
        if (jsonObject.has("readMessage")) {
            resultMap.setReadMessage(jsonObject.get("readMessage").getAsString());
        }
        resultMap = highlightContent(
                differs,
                jsonObject.get("er").getAsString(),
                jsonObject.get("ar").getAsString(),
                rules,
                contentType,
                true,
                true
        );
        resultMap.setRules(rulesArray);
        return resultMap;
    }

    public HighlighterResult highlightContent(List<DiffMessage> differs,
                                              String er,
                                              String ar,
                                              Parameters configuration,
                                              String contentType,
                                              boolean encoded,
                                              boolean encodeResults) {
        return highlightContent(differs, er, ar, configuration.toMap(), contentType, encoded, encodeResults);
    }

    public HighlighterResult highlightContent(List<DiffMessage> differs,
                                              String er,
                                              String ar,
                                              Map<String, List<String>> rules,
                                              String contentType,
                                              boolean encoded,
                                              boolean encodeResults) {
        HighlighterResult resultMap = new HighlighterResult();
        try {
            String decodedER = (er == null) ? "" : er;
            if (!decodedER.isEmpty() && encoded) {
                decodedER = DataContentConverter.toString(decodedER);
            }
            String decodedAR = (ar == null) ? "" : ar;
            if (!decodedAR.isEmpty() && encoded) {
                decodedAR = DataContentConverter.toString(decodedAR);
            }
            decodedER = erSubstitutionRule(decodedER, rules);
            switch (contentType) {
                case "MASKED_XML":
                case "XML":
                    // Old variant (still working fine, but without rules): resultMap
                    // = BuildColoredXML.highlight(differs, XmlHelpers.cleanCommentsAndStartXML(decodedER),
                    // XmlHelpers.cleanCommentsAndStartXML(decodedAR));
                    resultMap = BuildColoredXML.highlightByRules(differs, decodedER, decodedAR, rules);
                    break;
                case "XML2Transform":
                    resultMap = BuildColoredXML.transformByRules(differs,
                            XmlHelpers.cleanCommentsAndStartXML(decodedER),
                            XmlHelpers.cleanCommentsAndStartXML(decodedAR), rules);
                    break;
                case "FULL_TEXT":
                    resultMap = BuildColoredText.highlightFullText(differs, decodedER, decodedAR, rules);
                    break;
                case "TASK_LIST":
                case "PRIMITIVES":
                    resultMap = BuildColoredText.highlight(differs, decodedER, decodedAR);
                    break;
                case "PLAIN_TEXT":
                    resultMap = BuildColoredText.highlightPlainText(differs, decodedER, decodedAR, rules);
                    break;
                case "XSD":
                    resultMap = BuildColoredXsdXML.highlight(differs, decodedER, decodedAR);
                    break;
                case "JSON":
                    // Old variant (still working fine, but without rules): resultMap
                    // = BuildColoredJson.highlight(differs, decodedER, decodedAR);
                    resultMap = BuildColoredJson.highlightByRules(differs, decodedER, decodedAR, rules);
                    break;
                case "CSV":
                    resultMap = BuildColoredCsv.highlight(differs, decodedER, decodedAR, rules);
                    break;
                case "TABLE":
                    resultMap = BuildColoredTable.highlight(differs, decodedER, decodedAR, rules);
                    break;
                default:
                    break;
            }
            //            resultMap = checkContext(resultMap, rules);
            if (encodeResults) {
                resultMap.getEr().encodeNode();
                resultMap.getAr().encodeNode();
                if (resultMap.getCombined() != null) {
                    resultMap.getCombined().encodeNode();
                }
            }
            return resultMap;
        } catch (Exception ex) {
            if (resultMap == null || resultMap.getEr() == null || resultMap.getAr() == null) {
                resultMap.setStatusCode("20000");
                // In case of java.lang.NullPointerException ex.getMessage() returns null
                resultMap.setStatusMessage(ResponseMessages.msg(-1, (ex.getMessage() == null)
                        ? ex.toString() : ex.getMessage()));
            } else {
                // Maybe we should set code to 20000
                resultMap.setStatusCode("10000");
                // In case of java.lang.NullPointerException ex.getMessage() returns null
                resultMap.setStatusMessage("Difference highlighting is done but exception(s) happen: "
                        + ResponseMessages.msg(-1, (ex.getMessage() == null) ? ex.toString() : ex.getMessage()));
            }
            return resultMap;
        }
    }

    private String erSubstitutionRule(String er, Map<String, List<String>> rules) {
        if (rules.containsKey(PARAMETER_ER_SUBSTITUTION)) {
            List<String> ruleValue = rules.get(PARAMETER_ER_SUBSTITUTION);
            return ComparatorUtils.applyErSubstitutionRule(er, ruleValue);
        }
        return er;
    }

    private Map<String, String> checkContext(Map<String, String> resultMap, Map<String, List<String>> rules) {
        if (resultMap.isEmpty() || !resultMap.containsKey("er") || !resultMap.containsKey("ar")) {
            return resultMap;
        } else {
            List<String> contextValues = rules.get(PARAMETER_NAME_CHECK_CONTEXT);
            if (contextValues != null) {
                String ar = resultMap.get("ar");
                for (String contextValue : contextValues) {
                    String[] singleContextValues = contextValue.split("\n|\r|\r\n");
                    for (String str : singleContextValues) {
                        if (str.isEmpty()) {
                            continue;
                        }
                        ar = ar.replaceAll(str, "<span class=\"IDENTICAL\">" + str + "</span>");
                    }
                }
                resultMap.put("ar", ar);
            }
            return resultMap;
        }
    }

}
