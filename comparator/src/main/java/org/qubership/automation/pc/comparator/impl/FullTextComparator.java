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

import static org.qubership.automation.pc.core.helpers.TextHelpers.escapeHtmlEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.helpers.StringHelper;
import org.qubership.automation.pc.core.helpers.TextHelpers;
import org.qubership.automation.pc.core.utils.DiffMatchPatch;
import org.qubership.automation.pc.models.CheckRegexpRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffRow;
import difflib.DiffRowGenerator;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * A full-featured comparator for plain text comparison with advanced rules and configurable behavior.
 *
 * <p>
 * This comparator allows rich customization during text comparison including:
 * <ul>
 *     <li>Line-by-line or full-text comparison modes</li>
 *     <li>Case-insensitive matching</li>
 *     <li>Exclusion of blank lines or unchanged rows</li>
 *     <li>Sorting of compared inputs</li>
 *     <li>RegExp-based transformations and filtering (e.g., ignore or replace text)</li>
 *     <li>Custom rules to mark content as passed or failed based on matching patterns</li>
 * </ul>
 *
 * <p>Designed to support highly customizable and structured text comparisons often used
 * in data validation, file verification, or automated test frameworks.
 * Comparison results are returned as a list of {@link DiffMessage} objects with precise
 * descriptions and positions of differences.</p>
 *
 * <p>This class extends {@code AbstractComparator} and uses several helper utilities
 * and rule-based processors to enhance flexibility and accuracy.</p>
 */
public class FullTextComparator extends AbstractComparator {
    public static final String SKIP_BLANK = "skip.blank";
    // rule: name = "skip.blank", value = "true" or "false"
    public static final String IGNORE_CHANGED = "ignoreChanged";
    // rule: name = "failIfTextContains", value = any string
    public static final String FAIL_IF_TEXT_CONTAINS = "failIfTextContains";
    public static final String SORT_ER_AR  = "sortErAr";
    public static final String IGNORE_IDENTICAL  = "ignoreIdentical";
    // rule: name = "ignoreCase", value = "true" or "false" (default) - Ignore case while comparison ( = "true")
    public static final String IGNORE_CASE = "ignoreCase";
    // rule: name = "mappingRegexp", value =  regexp
    // Not implemented: OR ROW#=rownumbers,REGEX=regexp OR MATCH=regexp1,REGEX=regexp2
    public static final String MAPPING_REGEXP = "mappingRegexp";
    // rule: name = "ignoreRegexp", value =  regexp
    public static final String IGNORE_REGEXP = "ignoreRegexp";
    // rule: name = "replaceRegexp", value =  regexpStr==replaceStr
    public static final String REPLACE_REGEXP = "replaceRegexp";
    public static final String REPLACE_REGEXP_FULL_TEXT = "replaceRegexpFullText";
    // Needed for rule "replaceRegexp", value = delimiter between regexpStr and replaceStr
    public static final String REPLACE_DELIMITER = "==";

    // Two rules which are applied to the whole er/ar not to each row of them
    // If these rules are set, er & ar both are checked via these rules. No comparison between er/ar is performed.
    // If both er/ar matches any of 'successIfMatch'-regexps, result of comparison is set to IDENTICAL
    // If both er/ar matches any of 'failIfMatch'-regexps, result of comparison is set to MODIFIED
    // 'successIfMatch'-rule has higher priority than 'failIfMatch'-rule.
    public static final String SUCCESS_IF_MATCH = "successIfMatch"; // rule: name = "successIfMatch", value = regexp
    public static final String FAIL_IF_MATCH = "failIfMatch";    // rule: name = "failIfMatch", value = regexp

    public static boolean ignoreIdentical;

    // rule: name = "singleRowMode", value = "true" or "false" (default - false)
    public static final String SINGLE_ROW_MODE = "singleRowMode";

    private List<CheckRegexpRule> listCheckRegexpRule;
    private List<CheckRegexpRule> listReplaceRegexpRule;
    private List<CheckRegexpRule> listReplaceRegexpRuleFullText;
    private List<CheckRegexpRule> listSuccessIfMatchRule;
    private List<CheckRegexpRule> listFailIfMatchRule;

    private boolean skipBlank;
    private boolean ignoreCase;
    private boolean singleRowMode;
    private boolean ignoreChanged;
    private boolean sortErAr;
    private boolean replaceFullText;
    private List<String> failText = new ArrayList<>();

    private final Logger log = LoggerFactory.getLogger(PlainTextComparator.class);

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        List<DiffMessage> differences = new ArrayList<>();
        try {
            getConfigurationParameters(configuration);

            if (!listSuccessIfMatchRule.isEmpty()) {
                differences.add(checkEntireValue(TextHelpers.skipCR(er), 1, listSuccessIfMatchRule, true, true));
                differences.add(checkEntireValue(TextHelpers.skipCR(ar), 2, listSuccessIfMatchRule, false, true));
                return differences;
            }
            if (!listFailIfMatchRule.isEmpty()) {
                differences.add(checkEntireValue(TextHelpers.skipCR(er), 1, listFailIfMatchRule, true, false));
                differences.add(checkEntireValue(TextHelpers.skipCR(ar), 2, listFailIfMatchRule, false, false));
                return differences;
            }
            final List<String> erList = TextHelpers.stringToList((ignoreCase)
                    ? replaceRegexpFullText(er).toLowerCase() : replaceRegexpFullText(er));
            final List<String> arList = TextHelpers.stringToList((ignoreCase)
                    ? replaceRegexpFullText(ar).toLowerCase() : replaceRegexpFullText(ar));

            TextHelpers.processRule_ExcludeTextBlocks(erList, arList, configuration);
            replaceRegexpByLine(erList);
            replaceRegexpByLine(arList);
            differences.addAll((singleRowMode) ? compareRowByRow(erList, arList) : compareAsPlainText(erList, arList));

            for (CheckRegexpRule rule : listCheckRegexpRule) {
                for (int k = 0; k < rule.regexpsCompiled.size(); k++) {
                    Pattern regexpCompiled = rule.regexpsCompiled.get(k);
                    String regexp = rule.regexps.get(k);
                    for (int j = 0; j < arList.size(); j++) {
                        if (!regexpCompiled.matcher(arList.get(j)).matches()) {
                            if (!rule.action.equals("ignore")) {
                                differences.add(new DiffMessage(0, "",
                                        TextHelpers.formatdiffCoords(j, 1),
                                        ResultType.MODIFIED,
                                        "ar row# " + (j + 1) + "doesn't match regexp: " + regexp)
                                );
                            }
                        } else {
                            if (rule.action.equals("ignore")) {
                                // 1st step: skip all other diffmessages for this row (set their results to SKIPPED)
                                skipDiffMessages(differences, j, false,
                                        "; is skipped because row mathes regexp: " + regexp);
                                // 2nd step: add IDENTICAL diffmessage for this row
                                differences.add(new DiffMessage(0, "",
                                        TextHelpers.formatdiffCoords(j, 1),
                                        ResultType.IDENTICAL)
                                ); // Empty description because this is actually not a difference
                            }
                        }
                    }
                    for (int j = 0; j < erList.size(); j++) {
                        if (!regexpCompiled.matcher(erList.get(j)).matches()) {
                            if (!rule.action.equals("ignore")) {
                                differences.add(new DiffMessage(0,
                                        TextHelpers.formatdiffCoords(j, 1),
                                        "",
                                        ResultType.MODIFIED,
                                        "er row# " + (j + 1) + "doesn't match regexp: " + regexp));
                            }
                        } else {
                            if (rule.action.equals("ignore")) {
                                // 1st step: skip all other diffmessages for this row (set their results to SKIPPED)
                                skipDiffMessages(differences, j, true,
                                        "; is skipped because row mathes regexp: " + regexp);
                                // 2nd step: add IDENTICAL diffmessage for this row
                                differences.add(new DiffMessage(0,
                                        TextHelpers.formatdiffCoords(j, 1),
                                        "",
                                        ResultType.IDENTICAL)
                                ); // Empty description because this is actually not a difference
                            }
                        }
                    }
                }
            }

            // Due to very comprehensive algorithm which can remove some diffMessages
            // by rules we need to renumerate them at the end of comparison
            for (int idx = 0; idx < differences.size(); idx++) {
                differences.get(idx).setOrderId(idx + 1);
            }

            return differences;
        } catch (Exception ex) {
            throw new ComparatorException(ex.toString() /* ex.getMessage()*/, 20002);
        }
    }

    private String replaceRegexpFullText(String text) {
        return replaceRegexpFullText(text, listReplaceRegexpRuleFullText);
    }

    public static String replaceRegexpFullText(String text, List<CheckRegexpRule> regexpRulesList) {
        for (CheckRegexpRule rule : regexpRulesList) {
            for (int k = 0; k < rule.regexps.size(); k++) {
                Pattern pattern = rule.regexpsCompiled.get(k);
                String replacement = rule.replacements.get(k);
                text = pattern.matcher(text).replaceAll(replacement);
            }
        }
        return text;
    }

    private void replaceRegexpByLine(List<String> textList) {
        replaceRegexpByLine(textList, listReplaceRegexpRule);
    }

    public static void replaceRegexpByLine(List<String> textList, List<CheckRegexpRule> regexpRulesList) {
        for (CheckRegexpRule rule : regexpRulesList) {
            for (int k = 0; k < rule.regexps.size(); k++) {
                String regexp = rule.regexps.get(k);
                String replacement = rule.replacements.get(k);
                for (int j = 0; j < textList.size(); j++) {
                    String line = textList.get(j).replaceAll(regexp, replacement);
                    textList.set(j, line);
                }
            }
        }
    }

    private DiffMessage checkEntireValue(String val, int orderId, List<CheckRegexpRule> listMatchRule,
                                         boolean isControl, boolean checkForSuccess) {
        DiffMessage diff = new DiffMessage();
        diff.setOrderId(orderId);
        boolean found = false;
        for (CheckRegexpRule rule : listMatchRule) {
            for (int k = 0; k < rule.regexpsCompiled.size(); k++) {
                Pattern regexpCompiled = rule.regexpsCompiled.get(k);
                String regexp = rule.regexps.get(k);
                if (!found) {
                    Matcher m = regexpCompiled.matcher(val);
                    try {
                        if (m.find()) {
                            found = true;
                            int nn1 = StringUtils.countMatches(val.substring(0, m.start()), '\n');
                            int nn2 = StringUtils.countMatches(val.substring(0, m.end()), '\n');
                            String coords = TextHelpers.formatdiffCoords(nn1, nn2 - nn1 + 1);
                            diff.setActual((isControl) ? "" : coords);
                            diff.setExpected((isControl) ? coords : "");
                            diff.setResult((checkForSuccess) ? ResultType.IDENTICAL : ResultType.MODIFIED);
                            diff.setDescription((checkForSuccess)
                                    ? "Success by '" + SUCCESS_IF_MATCH + "'-rule: " + ((isControl)
                                    ? "ER" : "AR") + " matches regexp. " + regexp
                                    : "Fail by '" + FAIL_IF_MATCH + "'-rule: " + ((isControl)
                                    ? "ER" : "AR") + " matches regexp. " + regexp);
                            return diff;
                        }
                    } catch (StackOverflowError e) {
                        String message = String.format("StackOverflowError when matching regexp %s with value %s",
                                StringHelper.trimToLength(regexpCompiled.toString(), 100),
                                StringHelper.trimToLength(val, 100));
                        log.error(message);
                        throw new RuntimeException(message);
                    }
                }
            }
        }
        int nn = StringUtils.countMatches(val, '\n');
        String coords = TextHelpers.formatdiffCoords(0, nn);
        diff.setActual((isControl) ? "" : coords);
        diff.setExpected((isControl) ? coords : "");
        diff.setResult((!checkForSuccess) ? ResultType.IDENTICAL : ResultType.MODIFIED);
        diff.setDescription((!checkForSuccess)
                ? "Success by '" + FAIL_IF_MATCH + "'-rule: " + ((isControl)
                ? "ER" : "AR") + " doesn't match any regexp." :
                "Fail by '" + SUCCESS_IF_MATCH + "'-rule: " + ((isControl)
                        ? "ER" : "AR") + " doesn't match any regexp.");
        return diff;
    }

    private List<DiffMessage> compareRowByRow(List<String> erList, List<String> arList) {
        List<DiffMessage> differences = new ArrayList<>();
        DiffMatchPatch dmp = new DiffMatchPatch();
        int diffCounter = 0;
        int erSize = erList.size();
        int arSize = arList.size();
        if (!ignoreChanged) {
            for (int k = 0; k < Math.min(erSize, arSize); k++) {
                if (!erList.get(k).equals(arList.get(k))) {
                    differences.add(compareRow(dmp, escapeHtmlEntities(erList.get(k)),
                            escapeHtmlEntities(arList.get(k)),
                            k, k, ++diffCounter));
                }
            }
        }
        if (erSize < arSize) {
            differences.add(new DiffMessage(++diffCounter,
                    TextHelpers.formatdiffEmptyLinesCoords(erSize, arSize - erSize),
                    TextHelpers.formatdiffCoords(erSize, arSize - erSize),
                    ResultType.EXTRA,
                    "At er row# " + erSize + ": ar row(s)## " + erSize + "-" + (arSize - 1) + " are inserted."));
        } else if (erSize > arSize) {
            differences.add(new DiffMessage(++diffCounter,
                    TextHelpers.formatdiffCoords(arSize, erSize - arSize),
                    TextHelpers.formatdiffEmptyLinesCoords(arSize, erSize - arSize),
                    ResultType.MISSED,
                    "At ar row# " + arSize + ": er row(s)## " + arSize + "-" + (erSize - 1) + " are deleted."));
        }
        return differences;
    }

    private List<DiffMessage> compareAsPlainText(List<String> erList, List<String> arList) {
        if (sortErAr) {
            Collections.sort(erList);
            Collections.sort(arList);
        }
        erList = erList.stream().map(erLine -> escapeHtmlEntities(erLine)).collect(Collectors.toList());
        arList = arList.stream().map(arLine -> escapeHtmlEntities(arLine)).collect(Collectors.toList());
        final Patch diff = DiffUtils.diff(erList, arList);
        int diffCounter = 0;
        List<DiffMessage> differences = new ArrayList<>();
        DiffRowGenerator dfg = null;
        for (Delta delta : (List<Delta>) diff.getDeltas()) {
            int erLinesCount = delta.getOriginal().getLines().size();
            int arLinesCount = delta.getRevised().getLines().size();
            int erPosition = delta.getOriginal().getPosition();
            int arPosition = delta.getRevised().getPosition();
            switch (delta.getType()) {
                case INSERT:
                    differences.add(new DiffMessage(++diffCounter,
                            TextHelpers.formatdiffEmptyLinesCoords(erPosition, arLinesCount),
                            TextHelpers.formatdiffCoords(arPosition, arLinesCount),
                            ResultType.EXTRA,
                            "At er row# " + erPosition + ": ar row(s)## " + arPosition + "-"
                                    + (arPosition - 1 + arLinesCount) + " are inserted."));
                    break;
                // Contrary to PlainTextComparator we should determine more detailed differences for 'CHANGE'-deltas...
                case CHANGE:
                    if (dfg == null) {
                        dfg = initDiffRowGenerator();
                    }
                    List<DiffMessage> addDiffs
                            = compareChanged(dfg, diffCounter, delta.getOriginal(), delta.getRevised());
                    if (!addDiffs.isEmpty()) {
                        diffCounter += addDiffs.size();
                        differences.addAll(addDiffs);
                    }
                    break;
                case DELETE:
                    differences.add(new DiffMessage(++diffCounter,
                            TextHelpers.formatdiffCoords(erPosition, erLinesCount),
                            TextHelpers.formatdiffEmptyLinesCoords(arPosition, erLinesCount),
                            ResultType.MISSED,
                            "At ar row# " + arPosition + ": er row(s)## " + erPosition + "-"
                                    + (erPosition - 1 + erLinesCount) + " are deleted."));
                    break;
                default:
                    log.warn(String.format("Unhandled delta type: %s", delta.getType()));
                    break;
            }
        }

        return differences;
    }

    private DiffRowGenerator initDiffRowGenerator() {
        DiffRowGenerator.Builder builder = new DiffRowGenerator.Builder();
        boolean sideBySide = true;              //default -> inline - Inherited from v.1

        builder.showInlineDiffs(!sideBySide);   // - Inherited from v.1
        builder.columnWidth(400/* 120 */);     // - Inherited from v.1
        builder.ignoreBlankLines(skipBlank);
        builder.ignoreWhiteSpaces(skipBlank);

        return builder.build();
    }

    private List<DiffMessage> compareChanged(DiffRowGenerator dfg, int parentDiffCounter, Chunk infoOriginal,
                                             Chunk infoRevised) {
        List<DiffMessage> differences = new ArrayList<>();
        DiffMessage extraDiffmsg = null;
        List<DiffRow> rows = new ArrayList<>();
        int erLinesSize = infoOriginal.getLines().size();
        int arLinesSize = infoRevised.getLines().size();
        int counter
                = parentDiffCounter;
        int erPosition = infoOriginal.getPosition();
        int arPosition = infoRevised.getPosition();

        if (erLinesSize == arLinesSize) {
            if (!ignoreChanged) {
                rows = dfg.generateDiffRows(infoOriginal.getLines(), infoRevised.getLines());
            }
        } else if (erLinesSize > arLinesSize) {
            List<String> trimmedSide = new ArrayList<>();
            for (int k = 0; k < arLinesSize; k++) {
                trimmedSide.add((String) (infoOriginal.getLines().get(k)));
            }
            rows = dfg.generateDiffRows(trimmedSide, infoRevised.getLines());
            extraDiffmsg = new DiffMessage(++counter,
                    TextHelpers.formatdiffCoords(erPosition + arLinesSize, erLinesSize - arLinesSize),
                    TextHelpers.formatdiffEmptyLinesCoords(arPosition + arLinesSize,
                            erLinesSize - arLinesSize),
                    ResultType.MISSED);
            extraDiffmsg.setDescription("er row(s)## " + extraDiffmsg.getExpected() + " are MISSED.");
        } else {
            List<String> trimmedSide = new ArrayList<>();
            for (int k = 0; k < erLinesSize; k++) {
                trimmedSide.add((String) (infoRevised.getLines().get(k)));
            }
            rows = dfg.generateDiffRows(infoOriginal.getLines(), trimmedSide);
            extraDiffmsg = new DiffMessage(++counter,
                    TextHelpers.formatdiffEmptyLinesCoords(erPosition + erLinesSize,
                            arLinesSize - erLinesSize),
                    TextHelpers.formatdiffCoords(arPosition + erLinesSize, arLinesSize - erLinesSize),
                    ResultType.EXTRA);
            extraDiffmsg.setDescription("ar row(s)## " + extraDiffmsg.getActual() + " are EXTRA.");
        }

        DiffMatchPatch dmp = new DiffMatchPatch();
        int cnt = 0;
        for (final DiffRow diffRow : rows) {
            differences.add(compareRow(dmp, diffRow.getOldLine(), diffRow.getNewLine(), erPosition + cnt,
                    arPosition + cnt, counter++));
            cnt++;
        }

        if (extraDiffmsg != null) {
            differences.add(extraDiffmsg);
        }
        return differences;
    }

    private DiffMessage compareRow(DiffMatchPatch dmp, String erRow, String arRow, int erPosition, int arPosition,
                                   int counter) {
        DiffMessage diffmsg = new DiffMessage();
        LinkedList<DiffMatchPatch.Diff> diffList = dmp.diffMain(erRow, arRow);
        double similarity = 100 - ((double) dmp.diffLevenshtein(diffList) / (Math.max(arRow.length(),
                erRow.length())) * 100);
        dmp.diffCleanupSemantic(diffList);
        boolean similar = (similarity < 20) ? false : true;

        if (similar) {
            diffmsg.setResult(ResultType.SIMILAR);
            diffmsg.setDescription("er row# " + (erPosition) + " is SIMILAR to ar row# " + (arPosition)
                    + " (has inline differences)");
            diffmsg.setActual(formatInlineArDiffMessage(arPosition, diffList));
            diffmsg.setExpected(formatInlineErDiffMessage(erPosition, diffList));
        } else {
            diffmsg.setResult(ResultType.MODIFIED);
            diffmsg.setDescription("er row# " + (erPosition) + " is replaced with ar row# " + (arPosition)
                    + " (there are too many inline differences)");
            diffmsg.setActual(TextHelpers.formatdiffCoords(arPosition, 1));
            diffmsg.setExpected(TextHelpers.formatdiffCoords(erPosition, 1));
        }
        diffmsg.setOrderId(counter);
        return diffmsg;
    }

    private void skipDiffMessages(List<DiffMessage> diffList, int rowId, boolean isControl, String addToDescription) {
        String diffStr;
        for (int i = 0; i < diffList.size(); i++) {
            DiffMessage diffMessage = diffList.get(i);
            diffStr = (isControl) ? diffMessage.getExpected() : diffMessage.getActual();
            if (diffStr.isEmpty()) {
                continue;
            }
            // It may be "N:m1-m2,m3-m4" or "N" or "N1-N2" or "N-emptyM".
            // We need to extract rows range specification from it and test if the range contains rowId
            DiffCoords diffCoords = new DiffCoords(diffStr);
            if (diffCoords.invalidCoordsFormat) {
                continue;
            }

            if (!diffCoords.emptyRows) {
                if (diffCoords.startRow <= rowId && diffCoords.endRow >= rowId) {
                    diffMessage.setResult(ResultType.SKIPPED);
                    diffMessage.setDescription(diffMessage.getDescription() + addToDescription);
                }
            }
        }
    }

    private void getConfigurationParameters(Parameters configuration) throws ComparatorException {
        /* EXCLUDE_TEXT_BLOCKS & EXCLUDE_REPLACE_SYMBOL rules' values are
        get in TextHelpers.processRule_ExcludeTextBlocks(...) */

        skipBlank = configuration.getBooleanParameter(SKIP_BLANK, false);
        ignoreChanged = configuration.getBooleanParameter(IGNORE_CHANGED, false);
        /* skipBlank = true; */
        /* only for debug purposes */
        /* CHECKED: this functionality doesn't implemented in current version of DiffRowGenerator.Builder */

        String fullTextComparatorFailIfText = configuration.getParameter(FAIL_IF_TEXT_CONTAINS);
        if (fullTextComparatorFailIfText == null) {
            fullTextComparatorFailIfText = "";
        }
        if (!fullTextComparatorFailIfText.isEmpty()) {
            String[] failTextVariants = fullTextComparatorFailIfText.split(";");
            for (String s : failTextVariants) {
                if (!s.isEmpty()) {
                    failText.add(s);
                }
            }
        }

        ignoreIdentical = configuration.getBooleanParameter(IGNORE_IDENTICAL, false);
        ignoreCase = configuration.getBooleanParameter(IGNORE_CASE, false);
        singleRowMode = configuration.getBooleanParameter(SINGLE_ROW_MODE, false);
        sortErAr = configuration.getBooleanParameter(SORT_ER_AR, false);

        listCheckRegexpRule = new ArrayList<>();
        List<String> regexps = configuration.getParameters(MAPPING_REGEXP);
        if (regexps == null) {
            regexps = new ArrayList<>();
        }
        if (!regexps.isEmpty()) {
            listCheckRegexpRule.add(new CheckRegexpRule("check", regexps, new ArrayList<String>()));
        }

        regexps = configuration.getParameters(IGNORE_REGEXP);
        if (regexps == null) {
            regexps = new ArrayList<>();
        }
        if (!regexps.isEmpty()) {
            listCheckRegexpRule.add(new CheckRegexpRule("ignore", regexps, new ArrayList<String>()));
        }


        listReplaceRegexpRule = prepareReplaceRegexpRules(configuration, REPLACE_REGEXP);
        listReplaceRegexpRuleFullText = prepareReplaceRegexpRules(configuration, REPLACE_REGEXP_FULL_TEXT);

        listSuccessIfMatchRule = new ArrayList<>();
        regexps = configuration.getParameters(SUCCESS_IF_MATCH);
        if (regexps == null) {
            regexps = new ArrayList<>();
        }
        if (!regexps.isEmpty()) {
            listSuccessIfMatchRule.add(new CheckRegexpRule("check", regexps, new ArrayList<String>()));
        }

        listFailIfMatchRule = new ArrayList<>();
        regexps = configuration.getParameters(FAIL_IF_MATCH);
        if (regexps == null) {
            regexps = new ArrayList<>();
        }
        if (!regexps.isEmpty()) {
            listFailIfMatchRule.add(new CheckRegexpRule("check", regexps, new ArrayList<String>()));
        }
    }

    public static List<CheckRegexpRule> prepareReplaceRegexpRules(Parameters configuration,
                                                                  String ruleName) throws ComparatorException {
        return prepareReplaceRegexpRules(configuration.getParameters(ruleName));
    }

    public static List<CheckRegexpRule> prepareReplaceRegexpRules(List<String> regexps) throws ComparatorException {
        List<CheckRegexpRule> regexpRule = new ArrayList<>();
        if (regexps != null && !regexps.isEmpty()) {
            List<String> regStrs = new ArrayList<>();
            List<String> replStrs = new ArrayList<>();
            for (String line : regexps) {
                if (!(StringUtils.isBlank(line) || line.equals(REPLACE_DELIMITER))) {
                    String[] parts = line.split(REPLACE_DELIMITER, 2);
                    if (parts.length >= 1) {
                        if (!parts[0].isEmpty()) {
                            regStrs.add(parts[0]);
                            if (parts.length == 2) {
                                replStrs.add(parts[1]);
                            } else {
                                replStrs.add("");
                            }
                        }
                    }
                }
            }
            if (!regStrs.isEmpty()) {
                regexpRule.add(new CheckRegexpRule("replace", regStrs, replStrs));
            }
        }
        return regexpRule;
    }

    private String formatInlineArDiffMessage(int rowIndex, LinkedList<DiffMatchPatch.Diff> diffs) {
        String str = String.format("%s:", rowIndex);
        String cols = "";
        int pos = 0;
        for (DiffMatchPatch.Diff arDiff : diffs) {
            int textLength = arDiff.text.length();
            switch (arDiff.operation) {
                case INSERT:
                    cols = cols + (pos) + "-" + (pos - 1 + textLength) + ",";
                    break;
                case DELETE:
                    continue;
                default:
            }
            pos += textLength;
        }
        return (cols.isEmpty()) ? "" : str + cols;
    }

    private String formatInlineErDiffMessage(int rowIndex, LinkedList<DiffMatchPatch.Diff> diffs) {
        String str = String.format("%s:", rowIndex);
        String cols = "";
        int pos = 0;
        for (DiffMatchPatch.Diff erDiff : diffs) {
            int textLength = erDiff.text.length();
            switch (erDiff.operation) {
                case DELETE:
                    cols = cols + (pos) + "-" + (pos - 1 + textLength) + ",";
                    break;
                case INSERT:
                    continue;
                default:
            }
            pos += textLength;
        }
        return (cols.isEmpty()) ? "" : str + cols;
    }

    private Boolean failIfDiffContains(String er, String ar) {
        if (!failText.isEmpty()) {
            for (String text : failText) {
                if (ar.contains(text) || er.contains(text)) {
                    return true;
                }
            }
            return false;
        }
        return false;
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
        // Expected format is: "N1-N2,N3-N4,N5-N6" where N1-N2 etc. - column positions to highlight (including)
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
                    //  1) N-emptyM - means "INSERT M empty rows BEFORE N's row"; variant:
                    //                N-empty - equivalent to N-empty1
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
