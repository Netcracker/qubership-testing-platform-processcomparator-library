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


import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ComparatorUtilsTest {

    @Test
    public void applyErSubstitutionRule_canApplyRule() {
        String erWithVariables = "It is a ${test} with ${count} parameters which can be replaced by ${ruleName} rule.";
        String expectedER = "It is a ER with 3 parameters which can be replaced by erSubstitution rule.";
        List<String> erSubstitutionValue = new ArrayList<>();
        erSubstitutionValue.add("test:ER, count:3,");
        erSubstitutionValue.add("ruleName:erSubstitution");

        String result = ComparatorUtils.applyErSubstitutionRule(erWithVariables, erSubstitutionValue);

        Assertions.assertEquals(expectedER, result);
    }
}
