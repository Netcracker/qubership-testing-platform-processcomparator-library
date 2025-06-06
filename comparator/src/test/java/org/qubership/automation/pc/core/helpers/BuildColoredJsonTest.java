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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.HighlighterResult;

//order is critical , because bug appeared in unix systems, not windows, and line breaks are defined in DefaultIndenter as static
//so firstly we are checking unix style , and then - all others (which does not matter)
@TestMethodOrder(MethodOrderer.MethodName.class)
public class BuildColoredJsonTest {

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

    @Test
    public void _1_highlightWithLineBreaksUnixStyle_line_breaks_should_be_exists() throws ComparatorException {
        System.setProperty("line.separator", "\n");
        checkLineBreaks();
    }

    @Test
    public void _2_highlightWithLineBreaksWindowsStyle_line_breaks_should_be_exists() throws ComparatorException {
        System.setProperty("line.separator", "\r\n");
        checkLineBreaks();
    }

    private void checkLineBreaks() throws ComparatorException {
        HighlighterResult highlightDiff = BuildColoredJson.highlight(Arrays.asList(
                new DiffMessage(1, "/locationIds", "", ResultType.MISSED, "ER node is missed."),
                new DiffMessage(2, "/sequenceNo", "", ResultType.MISSED, "ER node is missed."),
                new DiffMessage(3, "/customerId", "", ResultType.MISSED, "ER node is missed.")
                ),
                "{\n"
                        + "\"locationIds\": [\n"
                        + "\"9151077258452090975\"\n"
                        + "],\n"
                        + "\"sequenceNo\": \"0000000020\",\n"
                        + "\"customerId\": 9151074318513303653\n"
                        + "}",
                "{ }");

        assertEquals(highlightDiff.getEr().getValue(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>{<br>"
                + "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"ER node is missed.\">  "
                + "\"locationIds\" : [  \"9151077258452090975\"]</span>,<br>"
                + "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"ER node is missed.\">  "
                + "\"sequenceNo\" : \"0000000020\",</span><br>"
                + "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"ER node is missed.\">  \"customerId\" : 9151074318513303653</span><br>"
                + "}</pre>");
    }

    @Test
    public void highlightByRules_withDiffAndRule_extra() throws ComparatorException {
        HighlighterResult highlightDiff = BuildColoredJson.highlightByRules(Arrays.asList(
                        new DiffMessage(1, "/status", "/status", ResultType.SIMILAR, "Node values are different."),
                        new DiffMessage(2, "", "/testid", ResultType.EXTRA, "AR has extra node(s).")
                ),
                "{\n" +
                        "\"objectid\":\"3245435\",\n" +
                        "\"active\":\"true\",\n" +
                        "\"status\":\"Disabled\"\n" +
                        "}",
                "{\n" +
                        "\"objectid\":\"324543\",\n" +
                        "\"testid\":\"wl340\",\n" +
                        "\"active\":\"true\",\n" +
                        "\"status\":\"New\"\n" +
                        "}",
                Collections.singletonMap("ignoreValue", Arrays.asList("$.objectid"))
        );

        assertEquals(highlightDiff.getEr().getValue(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>{<br>  " +
                "\"objectid\" : \"3245435\",<br>  \"active\" : \"true\",<br>  \"status\" : <span data-block-id=\"pc-highlight-block\"" +
                " class=\"SIMILAR\" title=\"Similar property or object\">\"Disabled\"</span><br>}</pre>");
    }

    @Test
    public void highlight_DiffForArrayAndObjects_similarAndModified () throws ComparatorException {
        HighlighterResult highlightDiff = BuildColoredJson.highlight(Arrays.asList(
                        new DiffMessage(1, "/mode/0", "/mode/0", ResultType.MODIFIED, "Nodes have different types."),
                        new DiffMessage(2, "/mode/1", "/mode/1", ResultType.MODIFIED, "Nodes have different types."),
                        new DiffMessage(3, "/number/1", "/number/1", ResultType.SIMILAR, "Node values are different."),
                        new DiffMessage(4, "/object", "/object", ResultType.SIMILAR, "Node values are different."),
                        new DiffMessage(5, "/type", "/type", ResultType.SIMILAR, "Node values are different.")
                ),
                "{\"mode\":[\"on\",\"off\"],\n" +
                        "\"number\":[1,2],\n" +
                        "\"object\":{\"id\":1},\n" +
                        "\"type\":\"active\"\n" +
                        "}",
                "{\"mode\":[true,false],\n" +
                        "\"number\":[1,3],\n" +
                        "\"object\":[],\n" +
                        "\"type\":\"inactive\"\n" +
                        "}"
        );

        assertEquals(highlightDiff.getEr().getValue(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<pre>{<br>  \"mode\" : [ <span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" " +
                "title=\"Nodes have different types.\">\"on\", </span> <span data-block-id=\"pc-highlight-block\" " +
                "class=\"MODIFIED\" title=\"Nodes have different types.\">\"off\" </span> ]," +
                "<br>  \"number\" : [ 1, <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" " +
                "title=\"Node values are different.\"> 2 </span> ],<br>  \"object\" : <span data-block-id=\"pc-highlight-block\" " +
                "class=\"SIMILAR\" title=\"Similar property or object\">{\"id\":1}</span>," +
                "<br>  \"type\" : <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" title=\"Similar property or object\">" +
                "\"active\"</span><br>}</pre>");
    }

    @Test
    public void _1_highlight_withDiff_invalidJson() throws ComparatorException {
        HighlighterResult highlightDiff = BuildColoredJson.highlight(Arrays.asList(
                        new DiffMessage(1, "/", "/", ResultType.MODIFIED, "Nodes have different types.")
                ),
                "plain text",
                "[\"it\",\"is\",\"array\"]"
        );

        assertEquals(highlightDiff.getEr().getValue(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre><span " +
                "data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" title=\"Nodes have different types.\">{ }</span></pre>");
    }

    @Test
    public void _2_highlight_withDiff_invalidJson() throws ComparatorException {
        HighlighterResult highlightDiff = BuildColoredJson.highlight(Arrays.asList(
                        new DiffMessage(1, "/", "/", ResultType.MODIFIED, "Nodes have different types.")
                ),
                "",
                "{}"
        );

        assertEquals(highlightDiff.getEr().getValue(), "");
    }

    @Test
    public void _3_highlight_withoutDiff_invalidJsonWithException() throws ComparatorException {
        assertThrows(ComparatorException.class,
                () -> BuildColoredJson.highlight(Arrays.asList(), "test[{", ""));
    }

    @Test
    public void BuildColoredJsonTest_givenManyMissedAndOneSimilarDiffsInJsonArray_canHighlightAllDiffs() throws ComparatorException {
        String er = "[\n" +
                "\t{\n" + "\t\t\"externalId\": \"9141486121813616913\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9141994423313001761\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9151852501813424658\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9152454004413943789\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9154703692113460826\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9154746998813696119\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9155420147713221254\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9155732294913484198\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9156299990013367374\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9156371041313380173\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"externalId\": \"9157961934313394732\",\n"
                + "\t\t\"priority\":\"2\"\n" + "\t}\n"
                + "]";
        String ar = "[\n" +
                "    {\n" +
                "        \"externalId\":\"9157961934313394732\",\n" +
                "        \"priority\":\"1\"\n" +
                "    }\n" +
                "]";
        String expectedEr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>[ <span data-block-id=\"pc-highlight-block\" " +
                "class=\"MISSED\" title=\"null\">{<br>  \"externalId\" : \"9141486121813616913\"<br>}, " +
                "</span> <span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">" +
                "{<br>  \"externalId\" : \"9141994423313001761\"<br>}, </span> <span data-block-id=\"pc-highlight-block\"" +
                " class=\"MISSED\" title=\"null\">{<br>  \"externalId\" : \"9151852501813424658\"<br>}," +
                " </span> <span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">" +
                "{<br>  \"externalId\" : \"9152454004413943789\"<br>}, </span> <span data-block-id=\"pc-highlight-block\"" +
                " class=\"MISSED\" title=\"null\">{<br>  \"externalId\" : \"9154703692113460826\"<br>}, " +
                "</span> <span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">" +
                "{<br>  \"externalId\" : \"9154746998813696119\"<br>}, </span> <span data-block-id=\"pc-highlight-block\"" +
                " class=\"MISSED\" title=\"null\">{<br>  \"externalId\" : \"9155420147713221254\"<br>}," +
                " </span> <span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">" +
                "{<br>  \"externalId\" : \"9155732294913484198\"<br>}, </span> <span data-block-id=\"pc-highlight-block\" " +
                "class=\"MISSED\" title=\"null\">{<br>  \"externalId\" : \"9156299990013367374\"<br>}, " +
                "</span> <span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">" +
                "{<br>  \"externalId\" : \"9156371041313380173\"<br>}, </span> {<br>  \"externalId\" : \"9157961934313394732\"," +
                "<br>  \"priority\" : <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" " +
                "title=\"Similar property or object\">\"2\"</span><br>} ]</pre>";
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1, "/10/priority", "/0/priority", ResultType.SIMILAR));
        for (int i = 0; i < 10; i++) {
            diffs.add(new DiffMessage(i + 2, "/" + i, "", ResultType.MISSED));
        }

        HighlighterResult result = BuildColoredJson.highlight(diffs, er, ar);

        assertEquals(expectedEr, result.getEr().getValue());
    }

    @Test
    public void BuildColoredJsonTest_givenTwoMissedIdenticalObjects_canHighlightBothObjects() throws ComparatorException {
        String er = "[\n"
                + "    {\"a\":\"b\"},\n"
                + "    {\"a\":\"b\"}\n"
                + "]";
        String ar = "[]";
        String expectedEr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>[ "
                + "<span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">{<br>  \"a\" : \"b\"<br>}, "
                + "</span> <span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">"
                + "{<br>  \"a\" : \"b\"<br>} </span> ]</pre>";
        List<DiffMessage> diffs = new ArrayList<>();
        diffs.add(new DiffMessage(1, "/0", "", ResultType.MISSED));
        diffs.add(new DiffMessage(2, "/1", "", ResultType.MISSED));

        HighlighterResult result = BuildColoredJson.highlight(diffs, er, ar);

        assertEquals(expectedEr, result.getEr().getValue());
    }

    @Test
    public void buildColoredJson_givenKeysCaseInsensitive_canHighlightByLowerCasePaths() throws ComparatorException {
        String er = "{\n"
                + "\"name\":\"1\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NAME\":\"3\",\n"
                + "\"namE\":[{\n"
                + "    \"obj\":\"1\",\n"
                + "    \"OBJ\":2,\n"
                + "    \"MIssED\":\"val\",\n"
                + "    \"Status\":\"SimilaR value\"\n"
                + "    }]\n"
                + "}";
        String ar = "{\n"
                + "\"name\":\"1\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NAME\":\"3\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NamE\":[{\n"
                + "        \"obj\":\"1\",\n"
                + "        \"OBJ\":\"3\",\n"
                + "        \"StaTuS\":\"SiMiLAR value\"\n"
                + "    },{\n"
                + "        \"obj\":\"4\",\n"
                + "        \"Obj\":\"5\",\n"
                + "        \"Status\":\"Extra\"\n"
                + "    }]\n"
                + "}\n";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        List<DiffMessage> diffs = Arrays.asList(
                new DiffMessage(1, "/name/0/obj", "/name/0/obj", ResultType.MODIFIED),
                new DiffMessage(2, "/name/0/missed", "", ResultType.MISSED),
                new DiffMessage(3, "/name/0/status", "/name/0/status", ResultType.SIMILAR),
                new DiffMessage(4, "", "/name/1", ResultType.EXTRA)
        );
        String expectedEr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>{<br>  \"name\" : [ {<br>"
                + "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" title=\"null\">    \"obj\" : 2,</span>"
                + "<br><span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">    \"missed\" : \"val\",</span>"
                + "<br>    \"status\" : <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" title=\"Similar property or object\">"
                + "\"SimilaR value\"</span><br>  } ]<br>}</pre>";
        String expectedAr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>{<br>  \"name\" : [ {"
                + "<br><span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" title=\"null\">    \"obj\" : \"3\",</span>"
                + "<br>    \"status\" : <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\""
                + " title=\"Similar property or object\">\"SiMiLAR value\"</span>"
                + "<br>  }, <span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" title=\"null\">{<br>    \"obj\" : \"5\","
                + "<br>    \"status\" : \"Extra\"<br>  } </span> ]<br>}</pre>";

        HighlighterResult result = BuildColoredJson.highlightByRules(diffs, er, ar, params.toMap());

        assertEquals(expectedEr, result.getEr().getValue(), "ER should contain all the differences correctly highlighted");
        assertEquals(expectedAr, result.getAr().getValue(), "ER should contain all the differences correctly highlighted");
    }

    @Test
    public void buildColoredJson_givenReadByPathAndKeysCaseInsensitive_canExtractAndHighlightByPath() throws ComparatorException {
        String er = "{\n"
                + "\"name\":\"1\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NAME\":\"3\",\n"
                + "\"namE\":[{\n"
                + "    \"obj\":\"1\",\n"
                + "    \"OBJ\":2,\n"
                + "    \"Status\":\"true\"\n"
                + "},{\n"
                + "    \"obj\":3,\n"
                + "    \"OBJ\":\"3\",\n"
                + "    \"Status\":\"TruE\"\n"
                + "}]\n"
                + "}";
        String ar = "{\n"
                + "\"name\":\"1\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NAME\":\"3\",\n"
                + "\"Name\":\"2\",\n"
                + "\"NamE\":[{\n"
                + "        \"obj\":\"1\",\n"
                + "        \"OBJ\":\"3\",\n"
                + "        \"StaTuS\":\"TruE\"\n"
                + "    },{\n"
                + "        \"obj\":\"4\",\n"
                + "        \"Obj\":\"5\",\n"
                + "        \"Status\":\"false\"\n"
                + "    },\n"
                + "    {\n"
                + "        \"obj\":\"1\",\n"
                + "        \"OBJ\":5,\n"
                + "        \"StaTuS\":\"TruE\"\n"
                + "    }]\n"
                + "}\n";
        Parameters params = new Parameters();
        params.put("keysCaseInsensitive", "true");
        params.put("readByPath", "$.Name..*[?(@.Status == 'TruE')].obj");
        List<DiffMessage> diffs = Arrays.asList(
                new DiffMessage(1, "", "/1", ResultType.EXTRA)
        );
        String expectedEr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>[ \"3\" ]</pre>";
        String expectedAr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>[ \"3\", "
                + "<span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" title=\"null\">5 </span> ]</pre>";

        HighlighterResult result = BuildColoredJson.highlightByRules(diffs, er, ar, params.toMap());

        assertEquals(expectedEr, result.getEr().getValue(),
                "ER should contain only one element in an array without highlighting");
        assertEquals(expectedAr, result.getAr().getValue(),
                "AR should have one extra highlighted element");
    }

    @Test
    void replaceDefaultTitle_shouldFallbackToDefault_whenNoDescriptionProvided() {
        String input = "{<br>  \"name\" : [ {<br><span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\""
                + " title=\"#DEFAULT_TITLE#Modified property or object#END_DEFAULT_TITLE#\">"
                + "    \"obj\" : 2, \"#BV_TITLE_null_BV_TITLE#\",</span><br><span data-block-id=\"pc-highlight-block\""
                + " class=\"MISSED\" title=\"#DEFAULT_TITLE#Missed property or object#END_DEFAULT_TITLE#\">"
                + "    \"missed\" : \"val\", \"#BV_TITLE_null_BV_TITLE#\",</span><br>    \"status\" :"
                + " <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\""
                + " title=\"#DEFAULT_TITLE#Similar property or object#END_DEFAULT_TITLE#\">\"SimilaR value\"</span><br>  } ]<br>}";

        String expected = "{<br>  \"name\" : [ {<br>"
                + "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" title=\"null\">    \"obj\" : 2,</span>"
                + "<br><span data-block-id=\"pc-highlight-block\" class=\"MISSED\" title=\"null\">    \"missed\" : \"val\",</span>"
                + "<br>    \"status\" : <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\" title=\"Similar property or object\">"
                + "\"SimilaR value\"</span><br>  } ]<br>}";

        String actual = BuildColoredJson.replaceDefaultTitle(input);

        assertEquals(expected, actual);
    }

    @Test
    void replaceDefaultTitle_shouldReplaceTitleAndRemoveBVTag_whenDescriptionProvided() {
        String input = "{<br>  \"name\" : [ {<br><span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\""
                + " title=\"#DEFAULT_TITLE#Modified property or object#END_DEFAULT_TITLE#\">"
                + "    \"obj\" : \"3\", \"#BV_TITLE_null_BV_TITLE#\",</span><br>    "
                + "\"status\" : <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\""
                + " title=\"#DEFAULT_TITLE#Similar property or object#END_DEFAULT_TITLE#\">\"SiMiLAR value\"</span><br>  },"
                + " <span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" title=\"#DEFAULT_TITLE#Extra property or object#END_DEFAULT_TITLE#\">"
                + " \"#BV_TITLE_null_BV_TITLE#\", {<br>    \"obj\" : \"5\",<br>    \"status\" : \"Extra\"<br>  } </span> ]<br>}";

        String expected = "{<br>  \"name\" : [ {"
                + "<br><span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" title=\"null\">    \"obj\" : \"3\",</span>"
                + "<br>    \"status\" : <span data-block-id=\"pc-highlight-block\" class=\"SIMILAR\""
                + " title=\"Similar property or object\">\"SiMiLAR value\"</span>"
                + "<br>  }, <span data-block-id=\"pc-highlight-block\" class=\"EXTRA\" title=\"null\">{<br>    \"obj\" : \"5\","
                + "<br>    \"status\" : \"Extra\"<br>  } </span> ]<br>}";

        String actual = BuildColoredJson.replaceDefaultTitle(input);

        assertEquals(expected, actual);
    }

    @Test
    void replaceDefaultTitle_shouldLeaveSpanUnchanged_whenNoPlaceholdersPresent() {
        String input = "{<br>  \"name\" : [ {<br>"
                + "<span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\" title=\"Some plain title\">"
                + "    \"obj\" : 123,</span><br>    "
                + "\"status\" : \"OK\"<br>  } ]<br>}";

        String actual = BuildColoredJson.replaceDefaultTitle(input);

        assertEquals(input, actual);
    }
}
