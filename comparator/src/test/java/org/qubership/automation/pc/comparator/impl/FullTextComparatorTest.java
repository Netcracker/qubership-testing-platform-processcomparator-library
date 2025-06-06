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
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class FullTextComparatorTest extends AbstractComparatorTest{

    private FullTextComparator comparator;

    public static final String LINE_SEPARATOR = "line.separator";

    private static String lineSeparatorBeforeTest;

    @BeforeAll
    public static void beforeClass() {
        lineSeparatorBeforeTest = System.getProperty("line.separator");
    }

    @AfterAll
    public static void tearDown() {
        System.setProperty(LINE_SEPARATOR, lineSeparatorBeforeTest);
    }

    @BeforeEach
    public void setUp() throws Exception {
        comparator = new FullTextComparator();
    }


    @Test
    public void compare_texts_after_replaceRegexpFullText_noDiffs() throws IOException, ComparatorException {
        System.setProperty(LINE_SEPARATOR, "\n");
        String er = getStringFromFile("src/test/resources/full_text/rules/replaceRegexpFullText/common_case_er.txt");
        String ar = getStringFromFile("src/test/resources/full_text/rules/replaceRegexpFullText/common_case_ar.txt");
        System.out.println("er from file: " + er);
        System.out.println("ar from file: " + ar);
        Parameters parameters = new Parameters();
        parameters.put("replaceRegexpFullText", "(\\d{15},(\\d{11}F{3}|F{11}),(\\d{15}|F{15}),F{15},F{15},F{15},F{15},F{15},F{15},F{15},F{15}(\\r\\n|\\n|))+==qwerty123");

        List<DiffMessage> diffs = comparator.compare(er, ar, parameters);
        for (DiffMessage diff: diffs){
            System.out.println("Expected: " + diff.getExpected());
            System.out.println("Actual: " + diff.getActual());
            System.out.println("Description: " + diff.getDescription());
            System.out.println("Result: " + diff.getResult());
        }
        Assertions.assertEquals(0, diffs.size());
    }

    @Test
    public void fullTextComparing_givenValuesWithHtmlEntities_canEscapeAndCompareInBothMode() throws ComparatorException {
        String er = "&reg;&<diff>&&amp;"; //normalized: &amp;reg;&amp;&lt;diff&gt;&amp;&amp;amp;
        String ar = "&reg;&<DIFF>&&amp;"; //normalized: &amp;reg;&amp;&lt;DIFF&gt;&amp;&amp;amp;
        Parameters rules = new Parameters();
        rules.put("singleRowMode", "true");
        String expectedDiffPath = "0:18-21,";

        List<DiffMessage> resultRegular = comparator.compare(er, ar, new Parameters());
        List<DiffMessage> resultWithSingleRowMode = comparator.compare(er, ar, rules);

        Assertions.assertEquals(1, resultRegular.size(), "Result should contain only 1 difference");
        Assertions.assertEquals(1, resultWithSingleRowMode.size(), "Result should contain only 1 difference");
        DiffMessage regularModeDiff = resultRegular.get(0);
        DiffMessage singleRowModeDiff = resultWithSingleRowMode.get(0);
        Assertions.assertEquals(ResultType.SIMILAR, regularModeDiff.getResult(), "Result should be SIMILAR");
        Assertions.assertEquals(ResultType.SIMILAR, singleRowModeDiff.getResult(), "Result should be SIMILAR");
        Assertions.assertEquals(expectedDiffPath, regularModeDiff.getExpected(), "Difference starts with 18 symbol (See normalized row)");
        Assertions.assertEquals(expectedDiffPath, singleRowModeDiff.getExpected(), "Difference starts with 18 symbol (See normalized row)");
        Assertions.assertEquals(expectedDiffPath, regularModeDiff.getActual(), "Difference starts with 18 symbol (See normalized row)");
        Assertions.assertEquals(expectedDiffPath, singleRowModeDiff.getActual(), "Difference starts with 18 symbol (See normalized row)");
    }

}
