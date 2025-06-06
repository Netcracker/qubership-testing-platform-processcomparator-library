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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.comparator.impl.FullTextComparator;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.HighlighterNode;
import org.qubership.automation.pc.models.HighlighterResult;

public class BuildColoredTextTest {

    @Test
    public void highlightFullText_givenSortedErArRule_sortRows() throws ComparatorException {
        String er = "1\n" +
                    "9";
        String ar = "2\n" +
                    "3\n" +
                    "1\n" +
                    "4\n" +
                    "5\n" +
                    "9";
        Map<String, List<String>> rules = new HashMap<>();
        rules.put(FullTextComparator.SORT_ER_AR, Arrays.asList("true"));
        List<DiffMessage> differences = new ArrayList<>();
        differences.add(new DiffMessage(1, "1-empty4", "1-4", ResultType.EXTRA));
        List<String> expectedEr = Arrays.asList("1\n",
                "<span class=\"EMPTY_ROW\">          </span><br>",
                "<span class=\"EMPTY_ROW\">          </span><br>",
                "<span class=\"EMPTY_ROW\">          </span><br>",
                "<span class=\"EMPTY_ROW\">          </span><br>",
                "9\n");
        List<String> expectedAr = Arrays.asList("1\n",
                "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\">2\n</span>",
                "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\">3\n</span>",
                "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\">4\n</span>",
                "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\">5\n</span>",
                "9\n");

        HighlighterResult result = BuildColoredText.highlightFullText(differences, er, ar, rules);

        List<String> erValues = result.getEr().getChildren().stream()
                .sorted(Comparator.comparing(HighlighterNode::getRowNumber))
                .map(HighlighterNode::getValue).collect(Collectors.toList());
        List<String> arValues = result.getAr().getChildren().stream()
                .sorted(Comparator.comparing(HighlighterNode::getRowNumber))
                .map(HighlighterNode::getValue).collect(Collectors.toList());
        Assertions.assertEquals(expectedEr, erValues);
        Assertions.assertEquals(expectedAr, arValues);
    }

    @Test
    public void highlightFullText_givenValueWithHtmlEntities_canEscapeAndHighlight() throws ComparatorException {
        String er = "&reg;&<diff>&&amp;"; //normalized: &amp;reg;&amp;&lt;diff&gt;&amp;&amp;amp;
        String ar = "&reg;&<DIFF>&&amp;"; //normalized: &amp;reg;&amp;&lt;DIFF&gt;&amp;&amp;amp;
        DiffMessage diff = new DiffMessage(1, "0:18-21", "0:18-21,", ResultType.SIMILAR);
        String expectedEr = "&amp;reg;&amp;&lt;<span data-block-id=\"pc-highlight-block\" "
                + "class=\"SIMILAR\">diff</span>&gt;&amp;&amp;amp;\n";
        String expectedAr = "&amp;reg;&amp;&lt;<span data-block-id=\"pc-highlight-block\" "
                + "class=\"SIMILAR\">DIFF</span>&gt;&amp;&amp;amp;\n";

        HighlighterResult result =
                BuildColoredText.highlightFullText(Arrays.asList(diff), er, ar, new HashMap<>());

        String firstErRow = result.getEr().getChildren().get(0).getValue();
        String firstArRow = result.getAr().getChildren().get(0).getValue();
        Assertions.assertEquals(expectedEr, firstErRow);
        Assertions.assertEquals(expectedAr, firstArRow);
    }

    @Test
    public void highlightPlainText_givenValueWithHtmlEntities_canEscapeAndHighlight() throws ComparatorException {
        String er = "&reg;&<diff>&&amp;"; //normalized: &amp;reg;&amp;&lt;diff&gt;&amp;&amp;amp;
        String ar = "&reg;&<DIFF>&&amp;"; //normalized: &amp;reg;&amp;&lt;DIFF&gt;&amp;&amp;amp;
        DiffMessage diff = new DiffMessage(1, "0:18-21", "0:18-21,", ResultType.MODIFIED);
        String expectedEr = "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\">"
                + "&amp;reg;&amp;&lt;diff&gt;&amp;&amp;amp;\n</span>";
        String expectedAr = "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\">"
                + "&amp;reg;&amp;&lt;DIFF&gt;&amp;&amp;amp;\n</span>";

        HighlighterResult result =
                BuildColoredText.highlightPlainText(Arrays.asList(diff), er, ar, new HashMap<>());

        String firstErRow = result.getEr().getChildren().get(0).getValue();
        String firstArRow = result.getAr().getChildren().get(0).getValue();
        Assertions.assertEquals(expectedEr, firstErRow);
        Assertions.assertEquals(expectedAr, firstArRow);
    }
}
