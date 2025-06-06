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
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class TableComparatorTest extends AbstractComparatorTest {

    private TableComparator comparator;

    @BeforeEach
    public void setUp()  {
        comparator = new TableComparator();
    }

    @Test
    public void compare_onlyAr_checkColumn_notEquals_columnHasThisValue_oneDiffExpected()
            throws ComparatorException, IOException {
        String table = getStringFromFile("src/test/resources/table/test_table.json");
        Parameters params = new Parameters();
        params.put("checkColumn", "Status=NOT_EQUALS=FAILED");

        List<DiffMessage> diffs = comparator.compare("", table, params);

        Assertions.assertEquals(1, diffs.size());
    }

    @Test
    public void compare_onlyAr_checkColumn_notEquals_columnsDontHaveThisValue_noDiffs()
            throws ComparatorException, IOException {
        String table = getStringFromFile("src/test/resources/table/test_table.json");
        Parameters params = new Parameters();
        params.put("checkColumn", "Status=NOT_EQUALS=No Such Value");

        List<DiffMessage> diffs = comparator.compare("", table, params);

        Assertions.assertEquals(0, diffs.size());
    }

    @Test
    public void compare_onlyAr_checkColumn_equals_columnsDontHaveThisValue_oneDiff()
            throws ComparatorException, IOException {
        String table = getStringFromFile("src/test/resources/table/test_table.json");
        Parameters params = new Parameters();
        params.put("checkColumn", "Status=EQUALS=FAILED");

        List<DiffMessage> diffs = comparator.compare("", table, params);

        Assertions.assertEquals(1, diffs.size());
    }

    @Test
    public void compare_onlyAr_checkColumn_equals_columnsHaveThisValue_noDiffs()
            throws ComparatorException, IOException {
        String table = getStringFromFile("src/test/resources/table/test_table.json");
        Parameters params = new Parameters();
        params.put("checkColumn", "Start Date=EQUALS=01/01/1970");

        List<DiffMessage> diffs = comparator.compare("", table, params);

        Assertions.assertEquals(0, diffs.size());
    }

    @Test
    public void compare_tablesWithCheckPocRuleWithReplace_ModifiedResult()
            throws ComparatorException, IOException {

        String er = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithReplace/er.json");
        String ar = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithReplace/ar.json");
        String ruleValue = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithReplace/checkPocRule.txt");

        String ruleLines[] = ruleValue.split("\\r?\\n");
        Parameters params = new Parameters();
        Stream.of(ruleLines).forEach(line ->params.put("checkPOC", line));
        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        long modifiedDiffsCount = diffs.stream().filter(diff -> diff.getResult() == ResultType.MODIFIED).count();
        Assertions.assertEquals(3, diffs.size());
        Assertions.assertEquals(3, modifiedDiffsCount);
    }

    @Test
    public void compare_tablesWithCheckPocRuleWithErArFilter_ModifiedResult()
            throws ComparatorException, IOException {

        String er = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithFilter/er.json");
        String ar = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithFilter/ar.json");
        String ruleValue = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithFilter/checkPocRule.txt");

        String ruleLines[] = ruleValue.split("\\r?\\n");
        Parameters params = new Parameters();
        Stream.of(ruleLines).forEach(line ->params.put("checkPOC", line));
        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        long modifiedDiffsCount = diffs.stream().filter(diff -> diff.getResult() == ResultType.MODIFIED).count();
        Assertions.assertEquals(2, diffs.size());
        Assertions.assertEquals(1, modifiedDiffsCount);
    }

    @Test
    public void compare_tableWithCellAsRegexpRule_canValidateByRegularExpressionsInEr() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/cellAsRegexp/er.json");
        String arPositive = getStringFromFile("src/test/resources/table/cellAsRegexp/ar_positive.json");
        String arNegative = getStringFromFile("src/test/resources/table/cellAsRegexp/ar_negative.json");
        Parameters parameters = new Parameters();
        parameters.put("cellAsRegexp", "true");

        List<DiffMessage> diffsPos = comparator.compare(er, arPositive, parameters);
        List<DiffMessage> diffsNeg = comparator.compare(er, arNegative, parameters);

        Assertions.assertTrue(diffsPos.isEmpty(), "positive AR and ER have no differences");
        Assertions.assertEquals(3, diffsNeg.size(), "negative AR and ER have 3 differences");
    }

    @Test
    public void compare_tableWithIgnoreExtraRule_canChangeExtraToIdentical() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_row/smallTable.json");
        String ar = getStringFromFile("src/test/resources/table/extra_row/bigTable.json");
        Parameters parameters = new Parameters();
        parameters.put("ignoreExtra", "true");

        List<DiffMessage> diffsOriginal = comparator.compare(er, ar, new Parameters());
        List<DiffMessage> diffsWithRules = comparator.compare(er, ar, parameters);

        Assertions.assertEquals(1, diffsOriginal.size());
        Assertions.assertEquals(1, diffsWithRules.size());
        Assertions.assertEquals(ResultType.EXTRA, diffsOriginal.get(0).getResult());
        Assertions.assertEquals(ResultType.IDENTICAL, diffsWithRules.get(0).getResult());
    }

    @Test
    public void compare_tableWithIgnoreMissedRule_canChangeMissedToIdentical() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_row/bigTable.json");
        String ar = getStringFromFile("src/test/resources/table/extra_row/smallTable.json");
        Parameters parameters = new Parameters();
        parameters.put("ignoreMissed", "true");

        List<DiffMessage> diffsOriginal = comparator.compare(er, ar, new Parameters());
        List<DiffMessage> diffsWithRules = comparator.compare(er, ar, parameters);

        Assertions.assertEquals(1, diffsOriginal.size());
        Assertions.assertEquals(1, diffsWithRules.size());
        Assertions.assertEquals(ResultType.MISSED, diffsOriginal.get(0).getResult());
        Assertions.assertEquals(ResultType.IDENTICAL, diffsWithRules.get(0).getResult());
    }

    @Test
    public void compare_tableWithAmpersandsInName_returnPathsWithAmpersands() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/with_ampersand/er.json");
        String ar = getStringFromFile("src/test/resources/table/with_ampersand/ar.json");
        Parameters parameters = new Parameters();
        String expectedPath = "/Prices&Statuses/0/2/S & t & a & t & e &";

        List<DiffMessage> diffs = comparator.compare(er, ar, parameters);

        Assertions.assertEquals(1, diffs.size());
        Assertions.assertEquals(expectedPath, diffs.get(0).getActual());
        Assertions.assertEquals(expectedPath, diffs.get(0).getExpected());

    }
}
