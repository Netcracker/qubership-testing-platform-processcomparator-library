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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

import difflib.Chunk;

/**
 * A utility class that provides helper methods for text manipulation and formatting
 * in the context of text-based comparisons.
 *
 * <p>
 * This class is used primarily by text comparators such as {@code FullTextComparator} and
 * {@code PlainTextComparator}, and provides functionality to:
 * <ul>
 *     <li>Convert multi-line strings to lists</li>
 *     <li>Format difference coordinates for reporting</li>
 *     <li>Apply text replacement rules based on user-defined "excludeText" configurations</li>
 *     <li>Escape HTML entities for safe rendering</li>
 * </ul>
 *
 * <p>
 * It supports rules like "excludeText" and "replaceSymbol", which define blocks or character
 * ranges within lines to be masked during comparisons, allowing for flexible and rule-driven diffs.
 * </p>
 *
 * <p>This class is stateless and cannot be instantiated.</p>
 */
public class TextHelpers {
    // Settings needed for 'excludeText' & 'replaceSymbol'
    // - rules processing in classes: PlainTextComparator.java & FullTextComparator.java
    private static final String EXCLUDE_REPLACE_SYMBOL  = "replaceSymbol";  // rule name
    // default value for EXCLUDE_REPLACE_SYMBOL rule
    private static final char   DEFAULT_REPLACE_SYMBOL  = '*';
    private static final String EXCLUDE_TEXT_BLOCKS     = "excludeText";    // rule name
    private static final int    DEFAULT_START_VALUE     = 1;

    public static List<String> stringToList(String str) {
        return (StringUtils.isBlank(str))
                ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(str.split("\\R")));
    }
    
    public static String skipCR(String str) {
        return str.replace("\r","");
    }
    
    // Following 4 method are shared by FullTextComparator & PlainTextComparator.
    // They format diff coords for 'whole-line'-differences
    public static String formatdiffCoords(Chunk deltaInfo) {
        return String.format("%s-%s", deltaInfo.getPosition(),
                deltaInfo.getPosition() - 1 + deltaInfo.getLines().size());
    }

    public static String formatdiffCoords(int position, int numberOfLines) {
        return String.format("%s-%s", position, position - 1 + numberOfLines);
    }

    public static String formatdiffEmptyLinesCoords(Chunk deltaInfo, int numberOfLines) {
        return String.format("%s-empty%s", deltaInfo.getPosition(), numberOfLines);
    }

    public static String formatdiffEmptyLinesCoords(int position, int numberOfLines) {
        return String.format("%s-empty%s", position, numberOfLines);
    }

    public static void processRule_ExcludeTextBlocks(List<String> erList,
                                                     List<String> arList,
                                                     Parameters configuration) throws ComparatorException {
        List<String> excludeTextBlocks = getParameter_excludeTextBlocks(configuration);
        char replaceSymbol = getParameter_excludeReplaceSymbol(configuration);
        
        processRule_ExcludeTextBlocks(erList, arList, excludeTextBlocks, replaceSymbol);
    }

    public static void processRule_ExcludeTextBlocks(List<String> erList,
                                                     List<String> arList,
                                                     Map<String, List<String>> rules) throws ComparatorException {
        List<String> excludeTextBlocks = getParameter_excludeTextBlocks(rules);
        char replaceSymbol = getParameter_excludeReplaceSymbol(rules);
        
        processRule_ExcludeTextBlocks(erList, arList, excludeTextBlocks, replaceSymbol);
    }

    public static void processRule_ExcludeTextBlocks(List<String> erList,
                                                     List<String> arList,
                                                     List<String> excludeTextBlocks,
                                                     char replaceSymbol) throws ComparatorException {
        for (String ruleStr : excludeTextBlocks) {
            String[] ruleMembers = ruleStr.toLowerCase().split("`|~|!|@|\"|#|$|%|^|\\&|\\*|\\_|\\+|'|\\.|"
                    + "<|>|\\?|:| |-|=|,|;|/|\\(|\\)|\\[|\\]|\\{|\\}|\t");
            int lineStart = -1;
            int lineEnd = -1;
            int cursorStart = -1;
            int cursorEnd = -1;
            boolean foundLines = false;
            boolean foundCursor = false;
            for (int i = 0 ; i < ruleMembers.length ; i++) {
                if (!foundLines) {
                    if (ruleMembers[i].equals("line")) {
                        if ((i + 1) < ruleMembers.length) {
                            lineStart = validateValue(getIntValue(ruleMembers[i + 1], DEFAULT_START_VALUE),
                                    DEFAULT_START_VALUE);
                        } else {
                            lineStart = DEFAULT_START_VALUE;
                        }
                        if ((i + 2) < ruleMembers.length) {
                            lineEnd = getIntValue(ruleMembers[i + 2], lineStart);
                        }
                        foundLines = true;
                    }
                }
                if (!foundCursor) {
                    if (ruleMembers[i].equals("cursor")) {
                        if ((i + 1) < ruleMembers.length) {
                            cursorStart = validateValue(getIntValue(ruleMembers[i + 1], DEFAULT_START_VALUE),
                                    DEFAULT_START_VALUE);
                        } else {
                            cursorStart = DEFAULT_START_VALUE;
                        }
                        if ((i + 2) < ruleMembers.length) {
                            cursorEnd = getIntValue(ruleMembers[i + 2], cursorStart);
                        }
                        foundCursor = true;
                    }
                }
                if (foundLines && foundCursor) {
                    break;
                }
            }

            if (!foundLines && !foundCursor) {
                throw new ComparatorException("Wrong parameters. \"line\" or \"cursor\" parameters must be defined. "
                        + "Example of correct syntax is: \"line:5-7,cursor:3-10\"", 20002);
            }

            if (foundLines && lineStart == -1 && lineEnd == -1) {
                throw new ComparatorException("Wrong parameters. \"start\" or \"end\" values for \"line\" "
                        + "parameter must be defined. Example of correct syntax is: \"line:5-7,cursor:3-10\"", 20002);
            }
            if (foundCursor && cursorStart == -1 && cursorEnd == -1) {
                throw new ComparatorException("Wrong parameters. \"start\" or \"end\" values for \"cursor\" "
                        + "parameter must be defined. Example of correct syntax is: \"line:5-7,cursor:3-10\"", 20002);
            }

            prepareLines(erList, lineStart, lineEnd == -1
                    ? erList.size() : lineEnd, cursorStart, cursorEnd, replaceSymbol);
            prepareLines(arList, lineStart, lineEnd == -1
                    ? arList.size() : lineEnd, cursorStart, cursorEnd, replaceSymbol);
        }
    }
    
    private static List<String> getParameter_excludeTextBlocks(Parameters configuration) {
        List<String> excludeTextBlocks = configuration.getParameters(EXCLUDE_TEXT_BLOCKS);
        return validateParameter_excludeTextBlocks(excludeTextBlocks);
    }
    
    private static List<String> getParameter_excludeTextBlocks(Map<String, List<String>> rules) {
        List<String> excludeTextBlocks = rules.get(EXCLUDE_TEXT_BLOCKS);
        return validateParameter_excludeTextBlocks(excludeTextBlocks);
    }

    private static List<String> validateParameter_excludeTextBlocks(List<String> excludeTextBlocks) {
        if (excludeTextBlocks == null) {
            excludeTextBlocks = new ArrayList<>();
        }
        return excludeTextBlocks;
    }
    
    private static char getParameter_excludeReplaceSymbol(Parameters configuration) throws ComparatorException {
        String replaceParam = configuration.getParameter(EXCLUDE_REPLACE_SYMBOL);
        if (replaceParam == null) {
            return DEFAULT_REPLACE_SYMBOL;
        } else {
            if (replaceParam.isEmpty()) {
                throw new ComparatorException("Blank value in parameter: " + EXCLUDE_REPLACE_SYMBOL);
            } else {
                return replaceParam.charAt(0);
            }
        }
    }
    
    private static char getParameter_excludeReplaceSymbol(Map<String, List<String>> rules) throws ComparatorException {
        List<String> replaceParam = rules.get(EXCLUDE_REPLACE_SYMBOL);
        if (replaceParam == null) {
            return DEFAULT_REPLACE_SYMBOL;
        } else {
            if (replaceParam.isEmpty() || replaceParam.get(0).isEmpty()) {
                throw new ComparatorException("Blank value in parameter: " + EXCLUDE_REPLACE_SYMBOL);
            } else {
                return replaceParam.get(0).charAt(0);
            }
        }
    }

    private static int validateValue(int value, int defaultValue) {
        if (value < defaultValue)   {
            return defaultValue;
        } else {
            return value;
        }
    }

    //  If cursorStart and cursorEnd both positive integers than replace each symbol at position
    //  in range [cursorStart; cursorEnd] with <symbol>
    //  If ( cursorEnd == -1 ) than replace the whole tail of the line (from position <cursorStart>)
    //  with single <symbol>
    //  Previous behaviour in that case was: set <cursorEnd> to actual
    //  length of the current line and perform common action
    //  Changed from the previous behaviour - by Alexander Kapustin, 2017-05-19
    private static void prepareLines(List<String> strings,
                                     int lineStart,
                                     int lineEnd,
                                     int cursorStart,
                                     int cursorEnd,
                                     char symbol) {
        char[] str;
        int curEnd;
        for (int currentLine = lineStart - 1; currentLine < lineEnd && currentLine < strings.size(); currentLine++) {
            if (cursorEnd == -1) {
                String s = strings.get(currentLine);
                str = s.substring(0, Math.min(cursorStart, s.length())).toCharArray();
                curEnd = str.length;
            } else {
                str = strings.get(currentLine).toCharArray();
                curEnd = cursorEnd;
            }
            for (int cursor = cursorStart - 1; cursor < Math.min(curEnd, str.length); cursor++) {
                str[cursor] = symbol;
            }
            strings.set(currentLine, new String(str));
        }
    }

    private static void prepareLines_oldBehaviour(List<String> strings,
                                                  int lineStart,
                                                  int lineEnd,
                                                  int cursorStart,
                                                  int cursorEnd,
                                                  char symbol) {
        for (int currentLine = lineStart - 1; currentLine < lineEnd && currentLine < strings.size(); currentLine++) {
            char[] str = strings.get(currentLine).toCharArray();
            if (cursorEnd == -1) {
                cursorEnd = str.length;
            }
            for (int cursor = cursorStart - 1; cursor < cursorEnd && cursor < str.length; cursor++) {
                str[cursor] = symbol;
            }
            strings.set(currentLine, new String(str));
        }
    }
    
    private static int getIntValue(String nodeValue, int defaultValue) throws ComparatorException {
        if (nodeValue == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(nodeValue);
        } catch (NumberFormatException e) {
            throw new ComparatorException("Failed to parse parameter value", e);
        }
    }

    public static String escapeHtmlEntities(String origin) {
        return origin
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\t", "    ");
    }

}
