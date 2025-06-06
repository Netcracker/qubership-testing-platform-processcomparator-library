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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComparatorUtils {

    /**
     * Apply 'erSubstitution' rule to the Expected result (er, etalon).
     * It parses rule value and replaces all variable names with values in er.
     *
     * @param erContent er
     * @param ruleValue rule value. Pairs should be separated by ',' or new line and names should be separated
     *                  from values by ':'.
     *                  F.e. param1:value1,param2:value2,...
     * @return er in which the names of variables are replaced by their values.
     */
    public static String applyErSubstitutionRule(String erContent, List<String> ruleValue) {
        List<String> pairs = ruleValue
                .stream()
                .flatMap(row -> Arrays.stream(row.split(",")))
                .collect(Collectors.toList());
        if (pairs.isEmpty()) {
            return erContent;
        }
        Map<String,String> erSubstitutes = new HashMap<>();
        for (String s : pairs) {
            String[] parameterNameAndValue = s.split(":");
            String parameterName = parameterNameAndValue[0].trim();
            String parameterValue = parameterNameAndValue.length == 1 ? "" : parameterNameAndValue[1];
            erSubstitutes.put(parameterName, parameterValue);
        }
        for (String var : erSubstitutes.keySet()) {
            String regex = "${" + var + "}";
            if (erContent.contains(regex)) {
                erContent = erContent.replace(regex, erSubstitutes.get(var));
            }
        }
        return erContent;
    }

}
