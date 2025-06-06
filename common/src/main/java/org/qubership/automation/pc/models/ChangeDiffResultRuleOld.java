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

package org.qubership.automation.pc.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.EnumUtils;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class ChangeDiffResultRuleOld {

    private String action; // "change", "ignore"
    private ResultType oldResult;
    private ResultType  newResult;
    private List<String> xpaths;
    private List<XPathExpression> xpathsCompiled;

    public ChangeDiffResultRuleOld() {
        action = "";
        oldResult = null;
        newResult = null;
        xpaths = new ArrayList();
        xpathsCompiled = new ArrayList();
    }

    public ChangeDiffResultRuleOld(String parAction, String parOldResult, String parNewResult,
                                   List<String> parXPaths) throws ComparatorException {
        this();
        if (parXPaths.isEmpty()) {
            return;
        }
        switch (parAction.trim().toLowerCase()) {
            case "change":
                if (!EnumUtils.isValidEnum(ResultType.class, parOldResult)
                        || !EnumUtils.isValidEnum(ResultType.class, parNewResult)) {
                    return;
                }
                action = "change";
                oldResult = ResultType.valueOf(parOldResult);
                newResult = ResultType.valueOf(parNewResult);
                break;
            case "ignore":
                action = "ignore";
                break;
            default:
                return;
        }
        setXPaths(parXPaths, XPathFactory.newInstance().newXPath());
    }

    //TODO not used
    public ChangeDiffResultRuleOld(String parAction, ResultType parOldResult, ResultType parNewResult,
                                   List<String> parXPaths) throws ComparatorException {
        this();
        if (parXPaths.isEmpty()) {
            return;
        }
        switch (parAction.trim().toLowerCase()) {
            case "change":
                action = "change";
                oldResult = parOldResult;
                newResult = parNewResult;
                break;
            case "ignore":
                action = "ignore";
                break;
            default:
                return;
        }
        setXPaths(parXPaths, XPathFactory.newInstance().newXPath());
    }

    // Rule string can be like
    //  SIMILAR=MODIFIED=//*[local-name()='productionPlan']
    //      or
    //  ignore=//*[local-name()='characteristicID']
    //      or
    //  //*[local-name()='Subscription-ID']
    // Thats why we should firstly determine the type of row.
    //  If row consists of xpath (not starts with "ignore=" or smth. like "SIMILAR=MODIFIED="
    //  then one of previous rows should start with "ignore=" or smth. like "SIMILAR=MODIFIED="

    //    SIMILAR=IDENTICAL=/start_dtm
    public static ChangeDiffResultRuleOld checkRule(String ruleItem) throws ComparatorException {
        int idx = ruleItem.indexOf("=");
        String ruleAction;
        String oldRes;
        String newRes;
        if (idx >= 1) {
            String part1 = ruleItem.substring(0, idx).trim().toLowerCase();
            String tail1 = ruleItem.substring(idx + 1);
            if (part1.equals("ignore")) {
                ruleAction = part1;
                return new ChangeDiffResultRuleOld(ruleAction, "", "",
                        Arrays.asList(tail1.split("\r\n|\n|\r")));
            } else {
                // change Result from old to new
                ruleAction = "change";
                oldRes = part1.toUpperCase();
                int idx1 = tail1.indexOf("=");
                if (idx1 >= 1) { // Otherwise - invalid rule syntax
                    newRes = tail1.substring(0, idx1).trim().toUpperCase();
                    String tail2 = tail1.substring(idx1 + 1);
                    if (EnumUtils.isValidEnum(ResultType.class, oldRes)
                            && EnumUtils.isValidEnum(ResultType.class, newRes)) {
                        return new ChangeDiffResultRuleOld(ruleAction, oldRes, newRes,
                                Arrays.asList(tail2.split("\r\n|\n|\r")));
                    }
                }
            }
        }
        return null;
    }

    public void addXPaths(List<String> parXPaths) throws ComparatorException {
        this.setXPaths(parXPaths, XPathFactory.newInstance().newXPath());
    }

    private void setXPaths(List<String> parXPaths, XPath xpath) throws ComparatorException {
        for (String item : parXPaths) {
            if (item.isEmpty()) {
                continue;
            }
            try {
                xpathsCompiled.add(xpath.compile(item));
                xpaths.add(item);
            } catch (XPathExpressionException ex) {
                throw new ComparatorException(" Xpath = " + item.replace("\"", "`")
                        .replace("'", "`") + "; " + ex.getMessage(), 20004);
            }
        }
    }
}
