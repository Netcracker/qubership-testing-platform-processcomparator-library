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

import static org.qubership.automation.pc.comparator.impl.PlainTextComparator.IGNORE_IDENTICAL;
import static org.qubership.automation.pc.comparator.impl.PlainTextComparator.ignoreIdentical;
import static org.qubership.automation.pc.core.helpers.TextHelpers.escapeHtmlEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.comparator.impl.FullTextComparator;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.CheckRegexpRule;
import org.qubership.automation.pc.models.HighlighterNode;
import org.qubership.automation.pc.models.HighlighterResult;

/**
 * Utility class responsible for generating syntax-highlighted HTML representations of
 * plain or full text differences for visual comparison.
 *
 * <p>
 * This class takes structured diff information (typically generated during comparison of
 * textual data) and produces decorated outputs that highlight modifications, additions,
 * deletions, and unchanged lines, using custom HTML and CSS classes.
 * </p>
 *
 * <p>
 * Supports multiple comparison strategies, including line-by-line (plain text) and
 * character/block-based (full text) diffs. It also applies optional transformation rules
 * like regular expression replacements and exclusion blocks before comparison.
 * </p>
 *
 * <p>
 * The result is encapsulated in a {@link HighlighterResult} object containing separate
 * and/or merged visual representations for expected and actual inputs.
 * </p>
 *
 * <p>This class is stateless in behavior but uses internal static fields for configuration during execution.</p>
 */
public class BuildColoredText {

    // rule: name = "replaceRegexp", value =  regexpStr==replaceStr
    private static final String REPLACE_REGEXP = "replaceRegexp";
    // rule: name = "replaceRegexp", value =  regexpStr==replaceStr
    private static final String REPLACE_REGEXP_FULL_TEXT = "replaceRegexpFullText";
    // Needed for rule "replaceRegexp", value = delimiter between regexpStr and replaceStr
    private static final String REPLACE_DELIMITER = "==";

    private static boolean sortErAr;
    private static List<CheckRegexpRule> listReplaceRegexpRule;
    private static List<CheckRegexpRule> listReplaceRegexpRuleFullText;

    public static HighlighterResult highlight(List<DiffMessage> differences, String er, String ar) {
        List<String> erList = TextHelpers.stringToList(er);
        List<String> arList = TextHelpers.stringToList(ar);
        return highlightText(er, ar, differences, erList, arList);
    }

    public static HighlighterResult highlightPlainText(List<DiffMessage> differences,
                                                       String er,
                                                       String ar,
                                                       Map<String, List<String>> rules) throws ComparatorException {
        getConfigurationParameters(rules);

        List<String> erList = TextHelpers.stringToList(er);
        List<String> arList = TextHelpers.stringToList(ar);
        TextHelpers.processRule_ExcludeTextBlocks(erList, arList, rules);

        return highlightText(er, ar, differences, erList, arList);
    }

    private static HighlighterResult highlightText(String er,
                                                   String ar,
                                                   List<DiffMessage> differences,
                                                   List<String> erList,
                                                   List<String> arList) {
        HighlighterNode erMessage = new HighlighterNode();
        if (StringUtils.isBlank(er)) {
            erMessage.setValue("$$$root$$$");
            addNewHighlighterNode(erMessage, "", "IDENTICAL",0);
        } else {
            erMessage = processDifferences(differences, erList, false);
        }
        HighlighterNode arMessage = new HighlighterNode();
        if (StringUtils.isBlank(ar)) {
            arMessage.setValue("$$$root$$$");
            addNewHighlighterNode(arMessage, "", "IDENTICAL",0);
        } else {
            arMessage = processDifferences(differences, arList, true);
        }
        HighlighterResult resultMap = new HighlighterResult();
        resultMap.setEr(erMessage);
        resultMap.setAr(arMessage);
        return resultMap;
    }

    public static HighlighterResult highlightFullText(List<DiffMessage> differences,
                                                      String er,
                                                      String ar,
                                                      Map<String, List<String>> rules) throws ComparatorException {
        getConfigurationParameters(rules);

        final List<String> erList
                = TextHelpers.stringToList(FullTextComparator.replaceRegexpFullText(er, listReplaceRegexpRuleFullText));
        final List<String> arList
                = TextHelpers.stringToList(FullTextComparator.replaceRegexpFullText(ar, listReplaceRegexpRuleFullText));

        TextHelpers.processRule_ExcludeTextBlocks(erList, arList, rules);
        if (sortErAr) {
            Collections.sort(erList);
            Collections.sort(arList);
        }

        HighlighterNode erMessage = new HighlighterNode();
        if (StringUtils.isBlank(er)) {
            erMessage.setValue("$$$root$$$");
            addNewHighlighterNode(erMessage, "", "IDENTICAL",0);
        } else {
            erMessage = processFullTextDifferences(differences, erList, false);
        }
        HighlighterNode arMessage = new HighlighterNode();
        if (StringUtils.isBlank(ar)) {
            arMessage.setValue("$$$root$$$");
            addNewHighlighterNode(arMessage, "", "IDENTICAL",0);
        } else {
            arMessage = processFullTextDifferences(differences, arList, true);
        }

        HighlighterResult resultMap = new HighlighterResult();
        resultMap.setEr(erMessage);
        resultMap.setAr(arMessage);
        return resultMap;
    }

    private static String diffGetText(DiffMessage diff, Boolean isActual) {
        return isActual ? diff.getActual() : diff.getExpected();
    }

    /* Used for highlighting compareResults for: PLAIN_TEXT, PRIMITIVES, TASK_LIST */
    /* 1st version ( processDifferencesOld() ) assumed that diffs are in line numbers order.
            It was correct for PLAIN_TEXT. And it's correct for PLAIN_TEXT now.
            But order of diffs for PRIMITIVES, TASK_LIST are not guarantied.
        Thats why 2nd version ( processDifferences() ) makes resultmsg in different way */
    private static HighlighterNode processDifferences(List<DiffMessage> differences,
                                                      List<String> ear,
                                                      Boolean isActual) {
        String diffText;  // "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<pre>";
        String diffResult;
        Map<Integer, String> coloured = new HashMap<>();
        Map<Integer, Integer> emptyLineNumbers = new HashMap<>();
        int earSize = ear.size();

        for (DiffMessage diff : differences) {
            // Actual / Expected in format: N1-N2 where N1,N2 - line numbers
            diffText = diffGetText(diff, isActual);
            if (/*diff.getResult() == ResultType.SKIPPED ||*/diffText.isEmpty()) {
                continue;
            }

            DiffCoords diffCoords = new DiffCoords(diffText);
            if (diffCoords.invalidCoordsFormat) {
                continue; // Or we can throw exception in that case
            }
            if (diffCoords.emptyRows) {
                if (emptyLineNumbers.containsKey(diffCoords.startRow)) {
                    emptyLineNumbers.put(diffCoords.startRow, emptyLineNumbers.get(diffCoords.startRow)
                            + diffCoords.emptyRowCount);
                } else {
                    emptyLineNumbers.put(diffCoords.startRow, diffCoords.emptyRowCount);
                }
                continue;
            }

            if (diffCoords.startRow >= earSize) {
                continue;
            }

            diffResult = diff.getResult().toString();
            for (int i = diffCoords.startRow; i <= Math.min(diffCoords.endRow, earSize - 1); i++) {
                coloured.put(i, diffResult + "#" + diff.getOrderId());
            }
        }
        HighlighterNode rootNode = new HighlighterNode();
        rootNode.setValue("$$$root$$$");
        int emptyRows = 0;
        for (int i = 0; i <= earSize; i++) {

            if (emptyLineNumbers.containsKey(i)) { 
                String es = "";
                for (int idx = 0; idx < emptyLineNumbers.get(i); idx++) {
                    es = "<span class=\"EMPTY_ROW\">          </span><br>";
                    addNewHighlighterNode(rootNode, es, "EMPTY_ROW",i + emptyRows);
                    emptyRows++;
                }
                // addNewHighlighterNode(rootNode, es, "EMPTY_ROW");
            }

            // replacements are needed in order to show XML/HTML tags
            // if content-type is in (PRIMITIVES, TASK_LIST, PLAIN_TEXT) but content is really XML or HTML
            if (i < earSize) {
                ear.set(i, ear.get(i) + "\n");
                String s = coloured.get(i);
                if (s == null) {
                    if (!ignoreIdentical) {
                        addNewHighlighterNode(rootNode,
                                escapeHtmlEntities(ear.get(i)), "IDENTICAL", i + emptyRows);
                        ///ear.get(i)
                        // .replace("<", "&lt;").replace(">", "&gt;") + "<br>";
                    }
                } else {
                    int k = s.indexOf("#");
                    String value = paintDifference(Integer.parseInt(s.substring(k + 1)), s.substring(0, k),
                            ear.get(i), true);
                    addNewHighlighterNode(rootNode, value, s.substring(0, k),i + emptyRows);

                }
            }
        }
        return rootNode;
    }

    private static HighlighterNode processFullTextDifferences(List<DiffMessage> differences,
                                                              List<String> ear,
                                                              Boolean isActual) {
        String diffText; // "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>";
        Map<Integer, Integer> emptyLineNumbers = new HashMap<>();
        int earSize = ear.size();

        for (CheckRegexpRule rule : listReplaceRegexpRule) {
            for (int k = 0; k < rule.regexps.size(); k++) {
                String regexp = rule.regexps.get(k);
                String replacement = rule.replacements.get(k);
                for (int j = 0; j < earSize; j++) {
                    String line = ear.get(j).replaceAll(regexp, replacement);
                    ear.set(j, line);
                }
            }
        }
        // replacements are needed in order to show XML/HTML tags if content-type is
        // in (PRIMITIVES, TASK_LIST, PLAIN_TEXT) but content is really XML or HTML
        //  Suddenly it's considered that command:
        //      List<DiffRow> rows = dfg.generateDiffRows( erList, arList);
        //  1. escapes "<" into "&lt;" and ">" into "&gt;"
        //  2. replaces "\t" to "    "
        //  Thats why column coords are related not to original BUT to transformed row!!!
        for (int i = 0; i < earSize; i++) {
            ear.set(i, escapeHtmlEntities(ear.get(i)) + "\n");
        }

        Map<Integer, String> statusList = new HashMap<>();
        for (DiffMessage diff : differences) {
            // Actual / Expected in format: "N" or "N:K1-K2,K3-K4,K5-K6,..."
            // where N - line number, Ki - position in line (starts with 0)
            diffText = diffGetText(diff, isActual);
            if (/*diff.getResult() == ResultType.SKIPPED ||*/diffText.isEmpty()) {
                continue;
            }

            DiffCoords diffCoords = new DiffCoords(diffText);
            if (diffCoords.invalidCoordsFormat) {
                continue; // Or we can throw exception in that case
            }
            if (diffCoords.emptyRows) {
                if (emptyLineNumbers.containsKey(diffCoords.startRow)) {
                    emptyLineNumbers.put(diffCoords.startRow, emptyLineNumbers.get(diffCoords.startRow)
                            + diffCoords.emptyRowCount);
                } else {
                    emptyLineNumbers.put(diffCoords.startRow, diffCoords.emptyRowCount);
                }
                continue;
            }

            if (diffCoords.startRow >= earSize) {
                continue;
            }

            if (diffCoords.intervals.isEmpty()) {
                for (int i = diffCoords.startRow; i <= Math.min(diffCoords.endRow, earSize - 1); i++) {
                    ear.set(i, paintDifference(diff.getOrderId(), diff.getResult().toString(), ear.get(i), false));
                }
            } else {
                String s = ear.get(diffCoords.startRow);
                String res = "";
                int curCol = 0;
                int len = s.length();
                for (Interval item : diffCoords.intervals) {
                    int startCol = Math.min(item.start, len - 1);
                    int endCol = Math.min(item.end, len - 1);

                    if (curCol < startCol) {
                        res = res + s.substring(curCol, startCol);
                    }
                    curCol = endCol + 1;
                    res = res + paintDifference(diff.getOrderId(), diff.getResult().toString(), s.substring(startCol,
                            curCol), false);
                    if (curCol >= len) {
                        break;
                    }
                }
                res = res + s.substring(curCol);
                ear.set(diffCoords.startRow, res);
            }
            statusList.put(diffCoords.startRow, diff.getResult().toString());
        }

        HighlighterNode rootNode = new HighlighterNode();
        rootNode.setValue("$$$root$$$");
        int emptyRows = 0;
        for (int i = 0; i <= earSize; i++) {
            // Show empty lines (for missed / extra lines on the other side)
            if (emptyLineNumbers.containsKey(i)) {
                String es = "";
                for (int idx = 0; idx < emptyLineNumbers.get(i); idx++) {
                    es = "<span class=\"EMPTY_ROW\">          </span><br>";
                    addNewHighlighterNode(rootNode, es, "EMPTY_ROW",i + emptyRows);
                    emptyRows++;
                }
            }
            if (i < earSize) {
                String status = "IDENTICAL";
                if (statusList.get(i) != null) {
                    status = statusList.get(i);
                }
                addNewHighlighterNode(rootNode, ear.get(i).replace("    ", "\t"),
                        status,i + emptyRows);
            }
        }
        return rootNode;
    }

    private static void addNewHighlighterNode(HighlighterNode rootNode, String value, String status, int rowNumber) {
        HighlighterNode node = new HighlighterNode();
        node.setParent(rootNode);
        node.setRowNumber(rowNumber + 1);
        rootNode.getChildren().add(node);
        node.setValue(value);
        node.setValidationStatus(status);
    }

    private static String paintDifference(int counter, String diffResult, String diffText, boolean escape) {
        //        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return "<span data-block-id=\"pc-highlight-block\""
                + " class=\"" + diffResult + "\">" + (escape ? escapeHtmlEntities(diffText) : diffText) + "</span>";
        //        return "<span data-block-id=\"pc-highlight-block\" id=\"anchor-" + uuid + "\" data-diff-name=\"Diff-"
        //                + counter + "\" data-toggle=\"popover\" title=\"Double click to copy a anchor link.\""
        //                + ((counter == 0) ? "" : " title=\"Difference #" + String.format("%d", counter) + "\" ")
        //                + " class=\"" + diffResult + "\">" + diffText + "</span>";
    }

    private static void getConfigurationParameters(Map<String, List<String>> rules) throws ComparatorException {
        ignoreIdentical = getIgnoreIdenticalRuleValueFromRules(rules);
        listReplaceRegexpRule = FullTextComparator.prepareReplaceRegexpRules(rules.get(REPLACE_REGEXP));
        listReplaceRegexpRuleFullText
                = FullTextComparator.prepareReplaceRegexpRules(rules.get(REPLACE_REGEXP_FULL_TEXT));
        sortErAr = getSortErArRuleValueFromRules(rules);
    }

    private static boolean getIgnoreIdenticalRuleValueFromRules(Map<String, List<String>> rules) {
        return rules.getOrDefault(IGNORE_IDENTICAL,
                Collections.singletonList("false")).stream().allMatch("true"::equals);
    }

    private static boolean getSortErArRuleValueFromRules(Map<String, List<String>> rules) {
        return rules.getOrDefault(FullTextComparator.SORT_ER_AR, Collections.singletonList("false"))
                .stream().allMatch("true"::equals);
    }

    private static class Interval {

        public int start;
        public int end;

        public Interval() {
        }

        public Interval(int start, int end) {
            if (start < 0) {
                this.start = 0;
            } else {
                this.start = start;
            }
            if (this.start > end) {
                this.end = this.start;
            } else {
                this.end = end;
            }
        }

    }

    private static class DiffCoords {

        public int startRow;
        public int endRow;
        public int rowCount;
        public int emptyRowCount; // in case of empty rows they are inserted BEFORE startRow!
        public boolean emptyRows = false;
        // Expected format is: "N1-N2,N3-N4,N5-N6" where N1-N2 etc - column positions to highlight (including)
        public String cols = "";
        public boolean invalidCoordsFormat = false;
        public String errorMessage = "";
        public List<Interval> intervals = new ArrayList<>();

        public DiffCoords() {
        }

        public DiffCoords(String coords) {
            try {
                int k = coords.indexOf(":");
                if (k == -1) {
                    // Coords specify row(s) not cols
                    this.cols = "";

                    // Rows specification can be: 
                    //  1) N-emptyM - means "INSERT M empty rows BEFORE N's row";
                    //  variant: N-empty - equivalent to N-empty1
                    //  2) N1-N2    - means rows from N1 to N2 (including)
                    //  3) N        - equivalent to N1-N1
                    getRowSpec(coords);
                } else {
                    getRowSpec(coords.substring(0, k));
                    getColSpec(coords.substring(k + 1));
                }
            } catch (Exception ex) {
                this.invalidCoordsFormat = true;
                this.errorMessage = this.errorMessage + ex.getMessage();
            }
        }

        private void getRowSpec(String coords) {
            int k = coords.indexOf("-");
            if (k == -1) {
                this.startRow = Integer.parseInt(coords);
                this.endRow = this.startRow;
                this.emptyRowCount = 0;
                this.emptyRows = false;
            } else {
                this.startRow = Integer.parseInt(coords.substring(0, k));
                String s = coords.substring(k + 1);
                if (s.startsWith("empty")) {
                    this.emptyRows = true;
                    this.endRow = this.startRow;
                    try {
                        this.emptyRowCount = Integer.parseInt(s.substring(5));
                    } catch (Exception ex) {
                        this.emptyRowCount = 1;
                    }
                } else {
                    this.endRow = Integer.parseInt(s);
                    this.emptyRowCount = 0;
                    this.emptyRows = false;
                }
            }
            this.rowCount = this.endRow - this.startRow + 1;
        }

        private void getColSpec(String coords) {
            this.cols = coords;
            List<String> colSplit = new ArrayList<>(Arrays.asList(coords.split("-|,")));
            if (!colSplit.isEmpty()) {
                for (int i = 0; i < colSplit.size(); i += 2) {
                    if ((i + 1) >= colSplit.size()) {
                        break;
                    }

                    int startCol = Integer.parseInt(colSplit.get(i));
                    int endCol = Integer.parseInt(colSplit.get(i + 1));
                    this.intervals.add(new Interval(startCol, endCol));
                }
            }
        }
    }
}
