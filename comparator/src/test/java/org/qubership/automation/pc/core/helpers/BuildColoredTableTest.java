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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.comparator.impl.AbstractComparatorTest;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.HighlighterResult;

public class BuildColoredTableTest extends AbstractComparatorTest {

    @Test
    public void BuildColoredTable_TableWithUnixLineSeparators_TableIsCorrect() throws ComparatorException, IOException {
        String erPath = "src/test/resources/table/withLineSeparators/erUnixStyle.json";
        String arPath = "src/test/resources/table/withLineSeparators/arUnixStyle.json";
        CheckColoredTableWithLineSeparators(erPath, arPath);
    }

    @Test
    public void BuildColoredTable_TableWithWindowsLineSeparators_TableIsCorrect() throws ComparatorException, IOException {
        String erPath = "src/test/resources/table/withLineSeparators/erWindowsStyle.json";
        String arPath = "src/test/resources/table/withLineSeparators/arWindowsStyle.json";
        CheckColoredTableWithLineSeparators(erPath, arPath);
    }

    private void CheckColoredTableWithLineSeparators(String erPath, String arPath) throws IOException, ComparatorException {

        HighlighterResult highlightDiff = BuildColoredTable.highlight(Arrays.asList(
                new DiffMessage(1, "/Prices/0/0/PK", "/Prices/0/0/PK", ResultType.MODIFIED, "Cell [0, 0]: values are different"),
                new DiffMessage(2, "/Prices/1/0/PK", "/Prices/1/0/PK", ResultType.MODIFIED, "Cell [1, 0]: values are different"),
                new DiffMessage(3, "/Prices/2/0/PK", "/Prices/2/0/PK", ResultType.MODIFIED, "Cell [2, 0]: values are different")),
                getStringFromFile(erPath), getStringFromFile(arPath));
        // "<?xml version=\"1.0\" encoding=\"UTF-8\"?><h3>Prices</h3>"
        assertEquals("<table border=1 cellpadding=2 cellspacing=2><tr><th>PK</th><th>Status</th><th>State</th></tr>"
                + "<tr><td class=\"MODIFIED\">1</td><td>PASSED<br></td><td>FINISHED</td></tr>"
                + "<tr><td class=\"MODIFIED\">2</td><td>PASSED<br></td><td>FINISHED</td></tr>"
                + "<tr><td class=\"MODIFIED\">3</td><td>FAILED<br>FAILED</td><td>FINISHED</td></tr></table>",
                highlightDiff.getEr().getValue());
        // "<?xml version=\"1.0\" encoding=\"UTF-8\"?><h3>Prices</h3>"
        assertEquals("<table border=1 cellpadding=2 cellspacing=2><tr><th>PK</th><th>Status</th><th>State</th></tr>"
                + "<tr><td class=\"MODIFIED\">2</td><td>PASSED<br></td><td>FINISHED</td></tr>"
                + "<tr><td class=\"MODIFIED\">3</td><td>PASSED<br></td><td>FINISHED</td></tr>"
                + "<tr><td class=\"MODIFIED\">4</td><td>FAILED<br>FAILED</td><td>FINISHED</td></tr></table>",
                highlightDiff.getAr().getValue());
    }

    @Test
    public void buildColoredTable_tablesWithCheckPocRuleWithReplaceAndThreeModifiedDiffs_highlightDiffs()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithReplace/er.json");
        String ar = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithReplace/ar.json");
        String ruleValue = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithReplace/checkPocRule.txt");
        List<DiffMessage> differences = new ArrayList<>();
        differences.add(new DiffMessage(1, "Check#1/TableER/1/2/BooleanER/",
                "Check#1/TableAR/0/2/BooleanAR", ResultType.MODIFIED));
        differences.add(new DiffMessage(2, "Check#1/TableER/2/3/ReplaceColER/",
                "Check#1/TableAR/1/3/ReplaceColAR", ResultType.MODIFIED));
        differences.add(new DiffMessage(3, "Check#1/TableER/0/2/BooleanER/",
                "Check#1/TableAR/2/2/BooleanAR", ResultType.MODIFIED));
        String expectedErTable = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithReplace/"
                + "highlightedEr.html").replaceAll("\\R", "");
        String expectedArTable = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithReplace/"
                + "highlightedAr.html").replaceAll("\\R", "");

        HighlighterResult highlighterResult = BuildColoredTable.highlight(differences, er, ar);

        assertEquals(expectedErTable, highlighterResult.getEr().getValue());
        assertEquals(expectedArTable, highlighterResult.getAr().getValue());
    }

    @Test
    public void buildColoredTable_tablesWithCheckPocRuleWithErArFilter_highlightModifiedAndExtraDiffs()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithFilter/er.json");
        String ar = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithFilter/ar.json");
        String ruleValue = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithFilter"
                + "/checkPocRule.txt");
        List<DiffMessage> differences = new ArrayList<>();
        differences.add(new DiffMessage(1, "", "Check#1/TableAR/0", ResultType.EXTRA));
        differences.add(new DiffMessage(2, "Check#1/TableER/1/4/FilterLessER/",
                "Check#1/TableAR/1/4/ColumnForEr", ResultType.MODIFIED));
        String expectedErTable = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithFilter/"
                + "highlightedEr.html").replaceAll("\\R", "");
        String expectedArTable = getStringFromFile("src/test/resources/table/checkPocRule/checkPocWithFilter/"
                + "highlightedAr.html").replaceAll("\\R", "");

        HighlighterResult highlighterResult = BuildColoredTable.highlight(differences, er, ar);

        assertEquals(expectedErTable, highlighterResult.getEr().getValue());
        assertEquals(expectedArTable, highlighterResult.getAr().getValue());
    }

    @Test
    public void buildColoredTable_extraColumnInErAndTwoMissedDiffs_highlightMissedDiffsInEr()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_column_in_er/er.json");
        String ar = getStringFromFile("src/test/resources/table/extra_column_in_er/ar.json");
        List<DiffMessage> differences = new ArrayList<>();
        differences.add(new DiffMessage(1, "/Prices/0/7/New Column", "", ResultType.MISSED));
        differences.add(new DiffMessage(2, "/Prices/1/7/New Column", "", ResultType.MISSED));
        String expectedErTable = getStringFromFile("src/test/resources/table/extra_column_in_er/"
                + "highlightedEr.html").replaceAll("\\R", "");
        String expectedArTable = getStringFromFile("src/test/resources/table/extra_column_in_er/"
                + "highlightedAr.html").replaceAll("\\R", "");

        HighlighterResult highlighterResult = BuildColoredTable.highlight(differences, er, ar);

        assertEquals(expectedErTable, highlighterResult.getEr().getValue());
        assertEquals(expectedArTable, highlighterResult.getAr().getValue());
    }

    @Test
    public void buildColoredTable_extraColumnInArAndTwoExtraDiffs_highlightExtraRowsInAr()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_column_in_ar/er.json");
        String ar = getStringFromFile("src/test/resources/table/extra_column_in_ar/ar.json");
        List<DiffMessage> differences = new ArrayList<>();
        differences.add(new DiffMessage(1, "", "/Prices/0/7/New Column", ResultType.EXTRA));
        differences.add(new DiffMessage(2, "", "/Prices/1/7/New Column", ResultType.EXTRA));
        String expectedErTable = getStringFromFile("src/test/resources/table/extra_column_in_ar/"
                + "highlightedEr.html").replaceAll("\\R", "");
        String expectedArTable = getStringFromFile("src/test/resources/table/extra_column_in_ar/"
                + "highlightedAr.html").replaceAll("\\R", "");

        HighlighterResult highlighterResult = BuildColoredTable.highlight(differences, er, ar);

        assertEquals(expectedErTable, highlighterResult.getEr().getValue());
        assertEquals(expectedArTable, highlighterResult.getAr().getValue());
    }

    @Test
    public void buildColoredTable_withEmptyArAndEr_returnEmptyXml() throws ComparatorException {
        String expectedTable = "";

        HighlighterResult highlighterResult = BuildColoredTable.highlight(Collections.emptyList(), "", "");

        assertEquals(expectedTable, highlighterResult.getEr().getValue());
        assertEquals(expectedTable, highlighterResult.getAr().getValue());
    }

    @Test
    public void buildColoredTable_withModifiedDiffAndComment_highlightArAndErWithComment()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/simple_modified_difference/er.json");
        String ar = getStringFromFile("src/test/resources/table/simple_modified_difference/ar.json");
        List<DiffMessage> differences = new ArrayList<>();
        differences.add(new DiffMessage(1, "/Prices/0/1/Status/Extra Comment in ER",
                "/Prices/0/1/Status/Extra Comment in AR", ResultType.MODIFIED));
        String expectedErTable = getStringFromFile("src/test/resources/table/simple_modified_difference"
                + "/highlightedErWithComment.html").replaceAll("\\R", "");
        String expectedArTable = getStringFromFile("src/test/resources/table/simple_modified_difference"
                + "/highlightedArWithComment.html").replaceAll("\\R", "");

        HighlighterResult highlighterResult = BuildColoredTable.highlight(differences, er, ar);

        assertEquals(expectedErTable, highlighterResult.getEr().getValue());
        assertEquals(expectedArTable, highlighterResult.getAr().getValue());
    }

    @Test
    public void buildColoredTable_givenCheckPocRuleWithDisplayColumns_canDisplayGivenColumnsOnly() throws IOException, ComparatorException {
        String directory = "src/test/resources/table/checkPocRule/checkPocWithDisplayColumns/";
        String er = getStringFromFile(directory + "er.json");
        String ar = getStringFromFile(directory + "ar.json");
        String checkPocValue = getStringFromFile(directory+ "checkPocRule.txt");
        List<String> checkPocRows = Arrays.asList(checkPocValue.split("\\R"));
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("checkPOC", checkPocRows);
        List<DiffMessage> differences = new ArrayList<>();
        differences.add(new DiffMessage(1, "Check#1/TableER/1/2/BooleanER/",
                "Check#1/TableAR/0/2/BooleanAR", ResultType.MODIFIED));
        differences.add(new DiffMessage(2, "Check#1/TableER/2/3/ReplaceColER/",
                "Check#1/TableAR/1/3/ReplaceColAR", ResultType.MODIFIED));
        differences.add(new DiffMessage(3, "Check#1/TableER/0/2/BooleanER/",
                "Check#1/TableAR/2/2/BooleanAR", ResultType.MODIFIED));
        String expectedErTable = getStringFromFile(directory + "highlightedEr.html").replaceAll("\\R", "");
        String expectedArTable = getStringFromFile(directory + "highlightedAr.html").replaceAll("\\R", "");

        HighlighterResult highlighterResult = BuildColoredTable.highlight(differences, er, ar, rules);

        assertEquals(expectedErTable, highlighterResult.getEr().getValue());
        assertEquals(expectedArTable, highlighterResult.getAr().getValue());
    }

    @Test
    public void buildCploredTable_givenTablesWithIdenticalDifference_identicalDifferenceWillNotBeAddedToResult() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_row/smallTable.json");
        String ar = getStringFromFile("src/test/resources/table/extra_row/bigTable.json");
        DiffMessage diffMessage = new DiffMessage(1, "", "/Prices/1", ResultType.IDENTICAL);
        String expectedResults = "";

        HighlighterResult result = BuildColoredTable.highlight(Collections.singletonList(diffMessage), er, ar);

        assertEquals(expectedResults, result.getEr().getValue());
        assertEquals(expectedResults, result.getAr().getValue());
    }
}
