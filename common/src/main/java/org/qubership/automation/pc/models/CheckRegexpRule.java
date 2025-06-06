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

package org.qubership.automation.pc.models;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

/**
 * {@code CheckRegexpRule} defines a rule set for handling regular expressions
 * used in comparison processes. The rule may perform one of the following actions:
 * <ul>
 *     <li><b>check</b> – Evaluate content against specified patterns</li>
 *     <li><b>ignore</b> – Skip matching patterns during comparison</li>
 *     <li><b>replace</b> – Replace matches with predefined strings (applied <b>before</b> comparison)</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * When action is "replace", the number of regular expressions must match the number of replacement strings.
 * Patterns may also include inline flags using the format {@code /flags:FLAG1|FLAG2}, such as:
 * <pre>{@code
 * "someRegex/flags:CASE_INSENSITIVE|MULTILINE"
 * }</pre>
 *
 * <h3>Supported Pattern Flags:</h3>
 * <ul>
 *     <li>CANON_EQ</li>
 *     <li>CASE_INSENSITIVE</li>
 *     <li>COMMENTS</li>
 *     <li>MULTILINE</li>
 *     <li>DOTALL</li>
 *     <li>LITERAL</li>
 *     <li>UNICODE_CASE</li>
 *     <li>UNICODE_CHARACTER_CLASS</li>
 *     <li>UNIX_LINES</li>
 * </ul>
 *
 * @see java.util.regex.Pattern
 * @see ComparatorException
 */
public class CheckRegexpRule {
    public String action; // "check", "ignore", "replace" (replace is performed BEFORE comparison!!!)
    public List<String> regexps;
    public List<Pattern> regexpsCompiled;
    public List<String> replacements;

    public CheckRegexpRule() {
        action = "";
        regexps = new ArrayList();
        regexpsCompiled = new ArrayList();
        replacements = new ArrayList();
    }

    public CheckRegexpRule(String parAction, List<String> parRegexps,
                           List<String> parReplacements) throws ComparatorException {
        this();
        if (parRegexps.isEmpty()) {
            return;
        }
        switch (parAction) {
            case "check":
            case "ignore":
                action = parAction;
                break;
            case "replace":
                action = parAction;
                if (parRegexps.size() != parReplacements.size()) {
                    throw new ComparatorException(
                            "'replaceRegexp' rule format error. Should be: regexpStr==replacementStr", 20003);
                }
                replacements.addAll(parReplacements);
                break;
            default:
                return;
        }
        regexps.addAll(parRegexps);
        for (int k = 0; k < regexps.size(); k++) {
            String regstr = regexps.get(k);
            if (regstr.isEmpty()) {
                continue;
            }
            try {
                // Check if there are Regexp flags at the end of regexp string 
                //  (i.e. "....../flags:
                //  CANON_EQ|CASE_INSENSITIVE|COMMENTS|MULTILINE|DOTALL|LITERAL|UNICODE_CASE|UNIX_LINES" )
                int m = regstr.indexOf("/flags:");
                if (m == -1) {
                    regexpsCompiled.add(Pattern.compile(regstr));
                } else {
                    String reg = regstr.substring(0, m);
                    String flags = regstr.substring(m + 7);
                    String[] flagsArr = flags.trim().toUpperCase().split(" |\\||,|;");
                    if (flagsArr == null) {
                        regexpsCompiled.add(Pattern.compile(reg));
                    } else {
                        int intReFlags = 0;
                        for (int j = 0; j < flagsArr.length; j++) {
                            int h = checkFlag(flagsArr[j]);
                            if (h > 0) {
                                intReFlags = intReFlags | h;
                            }
                        }
                        if (intReFlags != 0) {
                            regexpsCompiled.add(Pattern.compile(reg, intReFlags));
                        } else {
                            regexpsCompiled.add(Pattern.compile(reg));
                        }
                    }
                }
            } catch (PatternSyntaxException ex) {
                throw new ComparatorException(" Regexp = " + regstr.replace("\"", "`")
                        .replace("'", "`") + "; " + ex.getMessage(), 20003);
            }
        }
    }

    private int checkFlag(String flag) {
        if (StringUtils.isBlank(flag)) {
            return 0;
        }
        switch (flag) {
            case "CANON_EQ":
                return Pattern.CANON_EQ;
            case "CASE_INSENSITIVE":
                return Pattern.CASE_INSENSITIVE;
            case "COMMENTS":
                return Pattern.COMMENTS;
            case "MULTILINE":
                return Pattern.MULTILINE;
            case "DOTALL":
                return Pattern.DOTALL;
            case "LITERAL":
                return Pattern.LITERAL;
            case "UNICODE_CASE":
                return Pattern.UNICODE_CASE;
            case "UNIX_LINES":
                return Pattern.UNIX_LINES;
            case "UNICODE_CHARACTER_CLASS":
                return Pattern.UNICODE_CHARACTER_CLASS;
            default:
                return 0;
        }
    }
}
