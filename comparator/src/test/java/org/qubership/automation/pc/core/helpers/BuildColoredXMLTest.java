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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.HighlighterNode;
import org.qubership.automation.pc.models.HighlighterResult;

public class BuildColoredXMLTest {

    String erExtraMissedExample = "<array>\n" +
            "   <id6>equals</id6>\n" +
            "   <id1>equals</id1>\n" +
            "   <id2>missed</id2>\n" +
            "   <id8>missed</id8>\n" +
            "</array>";
   String arExtraMissedExample = "<array>\n" +
           "    <id1>equals</id1>\n" +
           "    <id3>extra</id3>\n" +
           "    <id6>equals</id6>\n" +
           "    <id5>extra</id5>\n" +
           "</array>";


    String erOfferingsExample = "<ContractSummary>\n" +
            "    <Offerings>\n" +
            "        <Type>Product</Type>\n" +
            "        <Name>Cable Access</Name>\n" +
            "        <BusinessDomains>Non-interactive Product</BusinessDomains>\n" +
            "    </Offerings>\n" +
            "    <Offerings>\n" +
            "        <Type>OneTimeService</Type>\n" +
            "        <Name>Satellite Access</Name>\n" +
            "        <BusinessDomains>New Business</BusinessDomains>\n" +
            "    </Offerings>\n" +
            "    <Offerings>\n" +
            "        <Type>Product</Type>\n" +
            "        <Name>Enlvement</Name>\n" +
            "        <BusinessDomains>OC: Skip Add OI from Business Case</BusinessDomains>\n" +
            "    </Offerings>\n" +
            "</ContractSummary>";
    String arOfferingsExample = "<ContractSummary>\n" +
            "    <Offerings>\n" +
            "        <Type>Product</Type>\n" +
            "        <Name>Enlvement</Name>\n" +
            "        <BusinessDomains>Skip Add OI from Business Case</BusinessDomains>\n" +
            "    </Offerings>\n" +
            "    <Offerings>\n" +
            "        <Type>Product</Type>\n" +
            "        <Name>Cable Access</Name>\n" +
            "        <BusinessDomains>Non-interactive Product</BusinessDomains>\n" +
            "    </Offerings>\n" +
            "    <Offerings>\n" +
            "        <Type>OneTimeService</Type>\n" +
            "        <Name>Satellite Access</Name>\n" +
            "        <BusinessDomains>KA</BusinessDomains>\n" +
            "    </Offerings>\n" +
            "</ContractSummary>";
    String sortByRuleOfferingsExample = "<xsl:template match=\"@*|node()\">\n" +
            "    <xsl:copy>\n" +
            "        <xsl:apply-templates select=\"@*|node()\"/>\n" +
            "    </xsl:copy>\n" +
            "</xsl:template>\n" +
            " \n" +
            "<xsl:template match=\"//*[local-name()='ContractSummary']\">\n" +
            "    <xsl:copy>   \n" +
            "        <xsl:apply-templates select=\"*[local-name()='Offerings']\">\n" +
            "            <xsl:sort select=\"*[local-name()='Name']\"/>          \n" +
            "        </xsl:apply-templates>\n" +
            "    </xsl:copy>\n" +
            "</xsl:template>";

    @Test
    public void xmlHighlightTest_xmlWithChildNodesAndDiffInAttribute_canHighlightAttributes() throws ComparatorException {
        String expectedErAttribute = "attr=\"<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >regexp:\\d{3}</span>";
        String expectedArAttribute = "attr=\"<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >no</span>";
        String er = "<nodes>\n" +
                "\t<node attr=\"regexp:\\d{3}\">\n" +
                "\t\t<innerNode> innerNodeText </innerNode>\n" +
                "\t</node>\n" +
                "</nodes>";
        String ar = "<nodes>\n" +
                "\t<node attr=\"no\">\n" +
                "\t\t<innerNode> innerNodeText </innerNode>\n" +
                "\t</node>\n" +
                "</nodes>";
        DiffMessage diffMessage = new DiffMessage(1, "/nodes[1]/node[1]/@attr",
                "/nodes[1]/node[1]/@attr", ResultType.MODIFIED);

        HighlighterResult result = BuildColoredXML.highlight(Arrays.asList(diffMessage), er, ar);
        HighlighterNode withDiffErNode = result.getEr().getChildren().get(0).getChildren().get(0);
        HighlighterNode withDiffArNode = result.getAr().getChildren().get(0).getChildren().get(0);

        Assertions.assertTrue(withDiffErNode.getValue().contains(expectedErAttribute),
                "Highlighted Er node with regexp should contain highlighted different");
        Assertions.assertTrue(withDiffArNode.getValue().contains(expectedArAttribute),
                "Highlighted Ar node with regexp should contain highlighted different");
    }

    @Test
    public void xmlHighlightTest_erAndArHaveCommentsAndIncorrectIndents_commentsRemovedAndIndentationCorrectedInHighlightedErAr() throws ComparatorException {
        String er = "<root>\n" +
                "    <!-- Comment-->\n" +
                "    <nextRowIsEmpty>text</nextRowIsEmpty>\n" +
                "    \n" +
                "                <incorrectIndent>text</incorrectIndent>\n" +
                "<!------------------------------------------>\n" +
                "</root>";
        String ar = "<root>\n" +
                "    <!-- Comment-->\n" +
                "    <nextRowIsEmpty>text</nextRowIsEmpty>\n" +
                "    \n" +
                "<incorrectIndent>text</incorrectIndent>\n" +
                "<!-- different comment -->\n" +
                "</root>";
        String expectedHighlightedErAr = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;root&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;nextRowIsEmpty&gt;text&lt;/nextRowIsEmpty&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;incorrectIndent&gt;text&lt;/incorrectIndent&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/root&gt;</div></div>";
        HighlighterResult result = BuildColoredXML.highlightByRules(Collections.emptyList(), er, ar, Collections.emptyMap());
        String highlightedER = result.getEr().getComposedValue(Arrays.asList(result.getEr()));
        String highlightedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr()));

        Assertions.assertEquals(highlightedER, highlightedAR, "After transformation er and ar should be equals");
        Assertions.assertEquals(expectedHighlightedErAr, highlightedER, "Highlighted er should be equal to expected");
    }

    @Test
    public void xmlHighlightTest_emptyErAndAr_returnEmptyErAndAr() throws ComparatorException {
        HighlighterResult result = BuildColoredXML.highlight(Collections.emptyList(), "\n\n", "      ");

        Assertions.assertTrue(result.getEr() != null && result.getEr().getChildren().isEmpty(),
                "Highlighted ER should be empty (not null and has no children)");
        Assertions.assertTrue(result.getAr() != null && result.getAr().getChildren().isEmpty(),
                "Highlighted ER should be empty (not null and has no children)");
    }



    @Test
    public void xmlHighlightByRulesTest_withoutRulesAndEqualsErArWithoutDiffs_canConvertToHtml() throws ComparatorException {
        String originalErAr = "<root>\n" +
                "    <emptyTag></emptyTag>\n" +
                "    <withAttr attribute=\"attr\">text</withAttr>\n" +
                "    <withChildElements>\n" +
                "        <child1>123</child1>\n" +
                "        <child2>\n" +
                "            <child level=\"2\">Child 2 Level</child>\n" +
                "        </child2>\n" +
                "    </withChildElements>\n" +
                "    <withRegexp>regexp:123</withRegexp>\n" +
                "</root>";
        String expectedHighlightedErAr = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;root&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;emptyTag&gt;&lt;/emptyTag&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;withAttr attribute=\"attr\"&gt;text&lt;/withAttr&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;withChildElements&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;child1&gt;123&lt;/child1&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;child2&gt;</div></div>" +
                "<div style=\"margin-left: 60px\"><div class=\"NORMAL\">&lt;child level=\"2\"&gt;Child 2 Level&lt;/child&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;/child2&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/withChildElements&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;withRegexp&gt;regexp:123&lt;/withRegexp&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/root&gt;</div></div>";

        HighlighterResult result = BuildColoredXML.highlightByRules(Collections.emptyList(), originalErAr, originalErAr, Collections.emptyMap());
        String highlightedER = result.getEr().getComposedValue(Arrays.asList(result.getEr()));
        String highlightedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr()));

        Assertions.assertEquals(highlightedER, highlightedAR, "Highlighted ER and AR should be equals");
        Assertions.assertEquals(expectedHighlightedErAr, highlightedER, "Highlighted ER should be equal to expected");
    }

    @Test
    public void xmlHighlightByRulesTest_abcSortRuleAndEqualsErArWithoutDiffs_canSort() throws ComparatorException {
        String originalErAr = "<array>\n" +
                "    <d z=\"2\" a=\"1\">must be fourth</d>\n" +
                "    <c>must be third</c>\n" +
                "    <b>must be second<innerElements>\n" +
                "            <s>2.2</s>\n" +
                "            <g>2.1</g></innerElements></b>\n" +
                "    <a>must be first</a>\n" +
                "</array>";
        String expectedHighlightedErAr = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;array&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;a&gt;must be first&lt;/a&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;b&gt;                 must be second                  </div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;innerElements&gt;</div></div>" +
                "<div style=\"margin-left: 60px\"><div class=\"NORMAL\">&lt;g&gt;2.1&lt;/g&gt;</div></div>" +
                "<div style=\"margin-left: 60px\"><div class=\"NORMAL\">&lt;s&gt;2.2&lt;/s&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;/innerElements&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/b&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;c&gt;must be third&lt;/c&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;d a=\"1\" z=\"2\"&gt;must be fourth&lt;/d&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/array&gt;</div></div>";
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("abcSort", Arrays.asList("true"));

        HighlighterResult result = BuildColoredXML.highlightByRules(Collections.emptyList(), originalErAr, originalErAr, rules);
        String highlightedER = result.getEr().getComposedValue(Arrays.asList(result.getEr()));
        String highlightedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr()));

        Assertions.assertEquals(expectedHighlightedErAr, highlightedER, "Highlighted ER should be equal to expected");
        Assertions.assertEquals(expectedHighlightedErAr, highlightedAR, "Highlighted AR should be equal to expected");
    }

    @Test
    public void xmlHighlightByRulesTest_abcSortRuleAndExtraMissedDiffs_canAddMissedToArAndExtraToErAndHighlightThemInGray() throws ComparatorException {
        String expectedHighlightedER = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;array&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;id1&gt;equals&lt;/id1&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"HIDDEN\" >&lt;id3&gt;extra&lt;/id3&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"HIDDEN\" >&lt;id5&gt;extra&lt;/id5&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" >&lt;id2&gt;missed&lt;/id2&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;id6&gt;equals&lt;/id6&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" >&lt;id8&gt;missed&lt;/id8&gt;</span></div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/array&gt;</div></div>";
        String expectedHighlightedAR = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;array&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;id1&gt;equals&lt;/id1&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" >&lt;id3&gt;extra&lt;/id3&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" >&lt;id5&gt;extra&lt;/id5&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"HIDDEN\" >&lt;id2&gt;missed&lt;/id2&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;id6&gt;equals&lt;/id6&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"HIDDEN\" >&lt;id8&gt;missed&lt;/id8&gt;</span></div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/array&gt;</div></div>";
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1, "/array[1]/id2[1]", "parent:/array[1]", ResultType.MISSED));
        diffs.add(new DiffMessage(2, "/array[1]/id8[1]", "parent:/array[1]", ResultType.MISSED));
        diffs.add(new DiffMessage(3, "parent:/array[1]", "/array[1]/id3[1]", ResultType.EXTRA));
        diffs.add(new DiffMessage(4, "parent:/array[1]", "/array[1]/id5[1]", ResultType.EXTRA));
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("abcSort", Arrays.asList("true"));

        HighlighterResult result = BuildColoredXML.highlightByRules(diffs, erExtraMissedExample,
                arExtraMissedExample, rules);
        String highlightedER = result.getEr().getComposedValue(Arrays.asList(result.getEr()));
        String highlightedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr()));

        Assertions.assertEquals(expectedHighlightedER, highlightedER, "Highlighted ER should be equal to expected");
        Assertions.assertEquals(expectedHighlightedAR, highlightedAR, "Highlighted AR should be equal to expected");
    }

    @Test
    public void xmlTransformByRulesTest_abcSortRuleAndExtraMissedDiffs_canTransform() throws ComparatorException {
        String expectedTransformedER =
                "<div style=\"margin-left: 0px\"><array>\n"
                + "    <id1>equals</id1>\n"
                + "    <id2>missed</id2>\n"
                + "    <id6>equals</id6>\n"
                + "    <id8>missed</id8>\n"
                + "</array>\n"
                + "</div>";
        String expectedTransformedAR =
                "<div style=\"margin-left: 0px\"><array>\n"
                + "    <id1>equals</id1>\n"
                + "    <id3>extra</id3>\n"
                + "    <id5>extra</id5>\n"
                + "    <id6>equals</id6>\n"
                + "</array>\n"
                + "</div>";
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1, "/array[1]/id2[1]", "parent:/array[1]", ResultType.MISSED));
        diffs.add(new DiffMessage(2, "/array[1]/id8[1]", "parent:/array[1]", ResultType.MISSED));
        diffs.add(new DiffMessage(3, "parent:/array[1]", "/array[1]/id3[1]", ResultType.EXTRA));
        diffs.add(new DiffMessage(4, "parent:/array[1]", "/array[1]/id5[1]", ResultType.EXTRA));
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("abcSort", Arrays.asList("true"));

        HighlighterResult result = BuildColoredXML.transformByRules(diffs, erExtraMissedExample,
                arExtraMissedExample, rules);
        String transformedER = result.getEr().getComposedValue(Arrays.asList(result.getEr())).replace("\r\n", "\n");
        String transformedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr())).replace("\r\n", "\n");

        Assertions.assertEquals(expectedTransformedER, transformedER, "Transformed ER should be equal to expected");
        Assertions.assertEquals(expectedTransformedAR, transformedAR, "Transformed AR should be equal to expected");
    }
    @Test
    public void xmlHighlightByRulesTest_abcSortRuleAndManyDiffs_canHighlightAllDiffs() throws ComparatorException {
        String er = "<root>\n" +
                "    <tag attr=\"1\">text</tag>\n" +
                "    <tag>\n" +
                "        <inner attr=\"re\">text</inner>\n" +
                "        <inner attr1=\"1\" attr2=\"2\"></inner>\n" +
                "            <r>qwerty</r>\n" +
                "    </tag>\n" +
                "    <mod></mod>\n" +
                "    <missed>missed</missed>\n" +
                "</root>";
        String ar = "<root>\n" +
                "    <tag attr=\"2\">another text</tag>\n" +
                "    <tag>\n" +
                "        <inner another=\"re\"></inner>\n" +
                "        <inner attr1=\"1\"></inner>\n" +
                "            <r>qwerty123</r>\n" +
                "    </tag>\n" +
                "    <mod>\n" +
                "        <inner></inner>\n" +
                "    </mod>\n" +
                "    <extra>extra</extra>\n" +
                "</root>";
        String expectedHighlightedER = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;root&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"HIDDEN\" >&lt;extra&gt;extra&lt;/extra&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" >&lt;missed&gt;missed&lt;/missed&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;mod&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"HIDDEN\" >&lt;inner&gt;&lt;/inner&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/mod&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;tag attr=\"" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >1</span>\"&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >text</span>&lt;/tag&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;tag&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >&lt;inner attr=\"" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >re</span>\"&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" >text</span>&lt;/inner&gt;</span></div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >&lt;inner attr1=\"" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >1</span>\" attr2=\"" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >2</span>\"&gt;&lt;/inner&gt;</span></div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;r&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >qwerty</span>&lt;/r&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/tag&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/root&gt;</div></div>";
        String expectedHighlightedAR = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;root&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" >&lt;extra&gt;extra&lt;/extra&gt;</span>" +
                "</div></div><div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"HIDDEN\" >&lt;missed&gt;missed&lt;/missed&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;mod&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" >&lt;inner&gt;&lt;/inner&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/mod&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;tag attr=\"" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >2</span>\"&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >another text</span>&lt;/tag&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;tag&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >&lt;inner another=\"" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >re</span>\"&gt;&lt;/inner&gt;</span></div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >&lt;inner attr1=\"" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" >1</span>\"&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"HIDDEN\" >text</span>&lt;/inner&gt;</span></div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;r&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >qwerty123</span>&lt;/r&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/tag&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/root&gt;</div></div>";
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1, "parent:/root[1]/mod[1]", "/root[1]/mod[1]/inner[1]", ResultType.EXTRA));
        diffs.add(new DiffMessage(2, "/root[1]/tag[2]/inner[2]/@attr1", "/root[1]/tag[2]/inner[1]", ResultType.MODIFIED));
        diffs.add(new DiffMessage(3, "/root[1]/tag[2]/inner[2]/@attr2", "/root[1]/tag[2]/inner[1]", ResultType.MODIFIED));
        diffs.add(new DiffMessage(4, "/root[1]/tag[2]/inner[2]", "/root[1]/tag[2]/inner[1]/@another", ResultType.MODIFIED));
        diffs.add(new DiffMessage(5, "/root[1]/tag[2]/inner[1]/@attr", "/root[1]/tag[2]/inner[2]", ResultType.MODIFIED));
        diffs.add(new DiffMessage(6, "/root[1]/tag[2]/inner[1]", "/root[1]/tag[2]/inner[2]/@attr1", ResultType.MODIFIED));
        diffs.add(new DiffMessage(7, "/root[1]/tag[2]/inner[1]/text()[1]", "parent:/root[1]/tag[2]/inner[2]", ResultType.MISSED));
        diffs.add(new DiffMessage(8, "/root[1]/tag[2]/r[1]/text()[1]", "/root[1]/tag[2]/r[1]/text()[1]", ResultType.SIMILAR));
        diffs.add(new DiffMessage(9, "/root[1]/tag[1]/@attr", "/root[1]/tag[1]/@attr", ResultType.SIMILAR));
        diffs.add(new DiffMessage(10, "/root[1]/tag[1]/text()[1]", "/root[1]/tag[1]/text()[1]", ResultType.SIMILAR));
        diffs.add(new DiffMessage(11, "/root[1]/missed[1]", "parent:/root[1]", ResultType.MISSED));
        diffs.add(new DiffMessage(12, "parent:/root[1]", "/root[1]/extra[1]", ResultType.EXTRA));
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("abcSort", Arrays.asList("true"));

        HighlighterResult result = BuildColoredXML.highlightByRules(diffs, er, ar, rules);
        String highlightedER = result.getEr().getComposedValue(Arrays.asList(result.getEr()));
        String highlightedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr()));

        Assertions.assertEquals(expectedHighlightedER, highlightedER, "Highlighted ER should be equal to expected");
        Assertions.assertEquals(expectedHighlightedAR, highlightedAR, "Highlighted AR should be equal to expected");
    }


    @Test
    public void xmlHighlightByRulesTest_sortByRuleWithEqualsErArAndWithoutDiffs_canTransformAndConvertToHtml() throws ComparatorException {
        String originalErAr = "<root>\n" +
                "    <otherElement>noNeedSort</otherElement>\n" +
                "    <needSortByText>some text</needSortByText>\n" +
                "    <needSortByAttrLabel label=\"asd\">text</needSortByAttrLabel>\n" +
                "    <needSortByText>another text</needSortByText>\n" +
                "    <needSortByAttrLabel label=\"abc\">text</needSortByAttrLabel>\n" +
                "    <otherElement>noNeedSort</otherElement>\n" +
                "    <needSortByText>different text</needSortByText>\n" +
                "    <needSortByText>123</needSortByText>\n" +
                "    <needSortByAttrLabel label=\"CAPS\">text</needSortByAttrLabel>\n" +
                "    <needSortByAttrLabel label=\"qwerty\">text</needSortByAttrLabel>\n" +
                "    <needSortByText>CAPS</needSortByText>\n" +
                "    <otherElement>noNeedSort</otherElement>\n" +
                "</root>";
        String expectedHighlightedErAr = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;root&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByText&gt;123&lt;/needSortByText&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByText&gt;another text&lt;/needSortByText&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByText&gt;CAPS&lt;/needSortByText&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByText&gt;different text&lt;/needSortByText&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByText&gt;some text&lt;/needSortByText&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByAttrLabel label=\"qwerty\"&gt;text&lt;/needSortByAttrLabel&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByAttrLabel label=\"CAPS\"&gt;text&lt;/needSortByAttrLabel&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByAttrLabel label=\"asd\"&gt;text&lt;/needSortByAttrLabel&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;needSortByAttrLabel label=\"abc\"&gt;text&lt;/needSortByAttrLabel&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;otherElement&gt;noNeedSort&lt;/otherElement&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;otherElement&gt;noNeedSort&lt;/otherElement&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;otherElement&gt;noNeedSort&lt;/otherElement&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/root&gt;</div></div>";
        String sortByRuleValue = "<xsl:template match=\"@*|node()\">\n" +
                "    <xsl:copy>" +
                "        <xsl:apply-templates select=\"@*\" />\n" +
                "        <xsl:apply-templates select=\"./*[name() = 'needSortByText']\">\n" +
                "            <xsl:sort select=\"text()\"/>\n" +
                "        </xsl:apply-templates>\n" +
                "        <xsl:apply-templates select=\"./*[name() = 'needSortByAttrLabel']\">\n" +
                "            <xsl:sort select=\"@label\" order=\"descending\"/>\n" +
                "        </xsl:apply-templates>\n" +
                "        <xsl:apply-templates select=\"node()[name() != 'needSortByText' and name() != 'needSortByAttrLabel']\"/>\n" +
                "    </xsl:copy>\n" +
                "</xsl:template>";
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("SortBy", Arrays.asList(sortByRuleValue));

        HighlighterResult result = BuildColoredXML.highlightByRules(Collections.emptyList(), originalErAr, originalErAr, rules);
        String highlightedER = result.getEr().getComposedValue(Arrays.asList(result.getEr()));
        String highlightedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr()));

        Assertions.assertEquals(expectedHighlightedErAr, highlightedER, "Highlighted ER should be equal to expected");
        Assertions.assertEquals(expectedHighlightedErAr, highlightedAR, "Highlighted AR should be equal to expected");
    }

    @Test
    public void xmlHighlightByRulesTest_sortByRuleWith2SimilarDiffs_canTransformAndHighlight() throws ComparatorException {
        String expectedHighlightedEr = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;ContractSummary&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Type&gt;Product&lt;/Type&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Name&gt;Cable Access&lt;/Name&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;BusinessDomains&gt;Non-interactive Product&lt;/BusinessDomains&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Type&gt;Product&lt;/Type&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Name&gt;Enlvement&lt;/Name&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;BusinessDomains&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >OC: Skip Add OI from Business Case</span>&lt;/BusinessDomains&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Type&gt;OneTimeService&lt;/Type&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Name&gt;Satellite Access&lt;/Name&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;BusinessDomains&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >New Business</span>&lt;/BusinessDomains&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/ContractSummary&gt;</div></div>";
        String expectedHighlightedAr = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;ContractSummary&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Type&gt;Product&lt;/Type&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Name&gt;Cable Access&lt;/Name&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;BusinessDomains&gt;Non-interactive Product&lt;/BusinessDomains&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Type&gt;Product&lt;/Type&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Name&gt;Enlvement&lt;/Name&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;BusinessDomains&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >Skip Add OI from Business Case</span>&lt;/BusinessDomains&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Type&gt;OneTimeService&lt;/Type&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;Name&gt;Satellite Access&lt;/Name&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;BusinessDomains&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >KA</span>&lt;/BusinessDomains&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/Offerings&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/ContractSummary&gt;</div></div>";;
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("SortBy", Arrays.asList(sortByRuleOfferingsExample));
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1, "/ContractSummary[1]/Offerings[2]/BusinessDomains[1]/text()[1]",
                "/ContractSummary[1]/Offerings[2]/BusinessDomains[1]/text()[1]", ResultType.SIMILAR));
        diffs.add(new DiffMessage(2, "/ContractSummary[1]/Offerings[3]/BusinessDomains[1]/text()[1]",
                "/ContractSummary[1]/Offerings[3]/BusinessDomains[1]/text()[1]", ResultType.SIMILAR));

        HighlighterResult result = BuildColoredXML.highlightByRules(diffs, erOfferingsExample, arOfferingsExample, rules);
        String highlightedER = result.getEr().getComposedValue(Arrays.asList(result.getEr()));
        String highlightedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr()));

        Assertions.assertEquals(expectedHighlightedEr, highlightedER, "Highlighted ER should be equal to expected");
        Assertions.assertEquals(expectedHighlightedAr, highlightedAR, "Highlighted AR should be equal to expected");
    }

    @Test
    public void xmlTransformByRulesTest_sortByRuleWith2SimilarDiffs_canTransform() throws ComparatorException {
        String expectedTransformedEr =
                "<div style=\"margin-left: 0px\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><ContractSummary>\n"
                + "    <Offerings>\n"
                + "        <Type>Product</Type>\n"
                + "        <Name>Cable Access</Name>\n"
                + "        <BusinessDomains>Non-interactive Product</BusinessDomains>\n"
                + "    </Offerings>\n"
                + "    <Offerings>\n"
                + "        <Type>Product</Type>\n"
                + "        <Name>Enlvement</Name>\n"
                + "        <BusinessDomains>OC: Skip Add OI from Business Case</BusinessDomains>\n"
                + "    </Offerings>\n"
                + "    <Offerings>\n"
                + "        <Type>OneTimeService</Type>\n"
                + "        <Name>Satellite Access</Name>\n"
                + "        <BusinessDomains>New Business</BusinessDomains>\n"
                + "    </Offerings>\n"
                + "</ContractSummary>\n"
                + "</div>";
        String expectedTransformedAr =
                "<div style=\"margin-left: 0px\"><?xml version=\"1.0\" encoding=\"UTF-8\"?><ContractSummary>\n"
                + "    <Offerings>\n"
                + "        <Type>Product</Type>\n"
                + "        <Name>Cable Access</Name>\n"
                + "        <BusinessDomains>Non-interactive Product</BusinessDomains>\n"
                + "    </Offerings>\n"
                + "    <Offerings>\n"
                + "        <Type>Product</Type>\n"
                + "        <Name>Enlvement</Name>\n"
                + "        <BusinessDomains>Skip Add OI from Business Case</BusinessDomains>\n"
                + "    </Offerings>\n"
                + "    <Offerings>\n"
                + "        <Type>OneTimeService</Type>\n"
                + "        <Name>Satellite Access</Name>\n"
                + "        <BusinessDomains>KA</BusinessDomains>\n"
                + "    </Offerings>\n"
                + "</ContractSummary>\n"
                + "</div>";
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("SortBy", Arrays.asList(sortByRuleOfferingsExample));
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1, "/ContractSummary[1]/Offerings[2]/BusinessDomains[1]/text()[1]",
                "/ContractSummary[1]/Offerings[2]/BusinessDomains[1]/text()[1]", ResultType.SIMILAR));
        diffs.add(new DiffMessage(2, "/ContractSummary[1]/Offerings[3]/BusinessDomains[1]/text()[1]",
                "/ContractSummary[1]/Offerings[3]/BusinessDomains[1]/text()[1]", ResultType.SIMILAR));

        HighlighterResult result = BuildColoredXML.transformByRules(diffs, erOfferingsExample, arOfferingsExample, rules);
        String transformedER = result.getEr().getComposedValue(Arrays.asList(result.getEr())).replace("\r\n", "\n");
        String transformedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr())).replace("\r\n", "\n");

        Assertions.assertEquals(expectedTransformedEr, transformedER, "Transformed ER should be equal to expected");
        Assertions.assertEquals(expectedTransformedAr, transformedAR, "Transformed AR should be equal to expected");
    }

    @Test
    public void xmlHighlightByRulesTest_excludeXpathRuleWithSimilarAndMissedDiffs_canTransformAndHighlight() throws ComparatorException {
        String er = "<array>\n" +
                "    <element>\n" +
                "        <id>9</id>\n" +
                "        <value>validate</value>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "        <id>4</id>\n" +
                "        <value>be sure to validate</value>\n" +
                "    </element>\n" +
                "        <element>\n" +
                "        <id>1</id>\n" +
                "        <value>notValidate</value>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "        <id>3</id>\n" +
                "        <value>validate</value>\n" +
                "    </element>\n" +
                "</array>";
        String ar = "<array>\n" +
                "    <element>\n" +
                "        <id>4</id>\n" +
                "        <value>validate</value>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "        <id>12</id>\n" +
                "        <value>notValidate</value>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "        <id>5</id>\n" +
                "        <value>notValidate</value>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "        <id>7</id>\n" +
                "        <value>notValidate</value>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "        <id>14</id>\n" +
                "        <value>validate</value>\n" +
                "    </element>\n" +
                "    <element>\n" +
                "        <id>9</id>\n" +
                "        <value>validate</value>\n" +
                "    </element>\n" +
                "</array>";
        String expectedHighlightedEr = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;array&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;9&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;validate&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;4&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >be sure to validate</span>&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" >&lt;element&gt;</span></div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" >&lt;id&gt;1&lt;/id&gt;</span></div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" >&lt;value&gt;notValidate&lt;/value&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">" +
                "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" >&lt;/element&gt;</span></div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;3&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;validate&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/array&gt;</div></div>";
        String expectedHighlightedAr = "<div style=\"margin-left: 0px\">$$$root$$$</div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;array&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;4&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;" +
                "<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >validate</span>&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;12&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;notValidate&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;5&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;notValidate&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;7&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;notValidate&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;14&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;validate&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;element&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;id&gt;9&lt;/id&gt;</div></div>" +
                "<div style=\"margin-left: 45px\"><div class=\"NORMAL\">&lt;value&gt;validate&lt;/value&gt;</div></div>" +
                "<div style=\"margin-left: 30px\"><div class=\"NORMAL\">&lt;/element&gt;</div></div>" +
                "<div style=\"margin-left: 15px\"><div class=\"NORMAL\">&lt;/array&gt;</div></div>";
        String excludeXpathRuleValue = "/array/*[local-name()='element'][id&gt;=10 or value='notValidate']";
        Map<String, List<String>> rules = new HashMap<>();
        rules.put("excludeXPath", Arrays.asList(excludeXpathRuleValue));
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1, "/array[1]/element[2]/value[1]/text()[1]",
                "/array[1]/element[1]/value[1]/text()[1]", ResultType.SIMILAR));
        diffs.add(new DiffMessage(2, "/array[1]/element[3]", "parent:/array[1]", ResultType.MISSED));

        HighlighterResult result = BuildColoredXML.highlightByRules(diffs, er, ar, rules);
        String highlightedER = result.getEr().getComposedValue(Arrays.asList(result.getEr()));
        String highlightedAR = result.getAr().getComposedValue(Arrays.asList(result.getAr()));

        Assertions.assertEquals(expectedHighlightedEr, highlightedER, "Highlighted ER should be equal to expected");
        Assertions.assertEquals(expectedHighlightedAr, highlightedAR, "Highlighted AR should be equal to expected");
    }

    @Test
    public void highlightXml_givenNodeWithTextAndChildren_canHighlightText() throws ComparatorException {
        String er = "<root attr='val'>\n"
                + "    someText\n"
                + "    <child attr=\"123\">1</child>\n"
                + "</root>";
        String ar = "<root attr='val'> another text\n"
                + "    <child attr=\"123\">1</child>\n"
                + "</root>";
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1,"/root[1]/text()[1]", "/root[1]/text()[1]", ResultType.SIMILAR));
        String expectedHighlightedEr = "<div class=\"NORMAL\">&lt;root attr=\"val\"&gt;<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" >     someText     </span></div>";
        String expectedHighlightedAr = "<div class=\"NORMAL\">&lt;root attr=\"val\"&gt;<span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" > another text     </span></div>";

        HighlighterResult result = BuildColoredXML.highlight(diffs, er, ar);

        String erRootNode = result.getEr().getChildren().get(0).getValue();
        String arRootNode = result.getAr().getChildren().get(0).getValue();
        Assertions.assertEquals(expectedHighlightedEr, erRootNode);
        Assertions.assertEquals(expectedHighlightedAr, arRootNode);
    }
}
