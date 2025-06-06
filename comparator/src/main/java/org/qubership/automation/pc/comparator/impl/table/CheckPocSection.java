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

package org.qubership.automation.pc.comparator.impl.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CheckPocSection {

    public String erTable = "";
    public CheckPocSectionType cfgType;
    public String cfgName = "";
    public String colName = "";
    public Map<String, String> relations = new HashMap<>();
    public Map<String, String> columns = new HashMap<>();
    public List<ReplacementSpecification> replacements = new ArrayList<>();
    public Map<String, FilterSpecification> filters = new HashMap<>(); // Named filters
    public List<String> displayColumnEr = new ArrayList<>();
    public List<String> displayColumnAr = new ArrayList<>();

    public CheckPocSection() {
    }

    public CheckPocSection(List<String> configRows, CheckPocSectionType configType) {
        this.cfgType = configType;
        for (String s : configRows) {
            s = s.trim();
            if (s.isEmpty()) {
                continue; // skip empty rows
            }
            int i = s.indexOf("=");
            if (i == -1) {
                continue; // skip; invalid format
            } else {
                String rowType = s.substring(0, i).trim().toUpperCase();
                String cfgItem = s.substring(i + 1).trim();
                switch (rowType) {
                    case "TABLE":
                        this.erTable = cfgItem;
                        break;
                    case "NAME":
                        this.cfgName = cfgItem;
                        break;
                    case "COLNAME":
                        this.colName = cfgItem;
                        break;
                    case "RELATION":
                        this.relations.putAll(parsePairs(cfgItem));
                        break;
                    case "COLUMNS":
                        this.columns.putAll(parsePairs(cfgItem));
                        break;
                    case "FILTER":
                        this.filters.putAll(parseFilterPairs(cfgItem));
                        break;
                    case "DISPLAYCOLUMNSER":
                        displayColumnEr.addAll(parseArray(cfgItem));
                        break;
                    case "DISPLAYCOLUMNSAR":
                        displayColumnAr.addAll(parseArray(cfgItem));
                        break;
                    default:
                        if (CheckPocSectionType.REPLACE.equals(this.cfgType)) {
                            this.replacements.addAll(parseReplacement(s));
                        }
                }
            }
        }
    }

    private List<String> parseArray(String values) {
        return Arrays.stream(values.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    // String format: comma-delimited pairs of key = value
    private Map<String, String> parsePairs(String cfgItem) {
        cfgItem = cfgItem.replaceAll("\\\\,", "\\u002C");
        String[] pairs = cfgItem.split(",");
        Map<String, String> keyVals = new HashMap<>();

        for (int i = 0; i < pairs.length; i++) {
            String[] keys = splitPairsBy(pairs[i], "=", "\"");
            //String[] keys = pairs[i].split("=");
            if (keys.length != 2) {
                continue; // invalid format
            } else {
                String key = keys[0];
                String val = keys[1];
                if (key.isEmpty() || val.isEmpty()) {
                    continue; // invalid format
                } else {
                    keyVals.put(clearValue(key), clearValue(val));
                }
            }
        }
        return keyVals;
    }

    private String[] splitPairsBy(String pairs, String splitter, String escaper) {
        List<String> finalPairs = new LinkedList<>();
        char[] charPairs = pairs.toCharArray();
        String key = null;
        String value = null;
        for (int i = 0; i < charPairs.length; i++) {
            if (charPairs[i] == ' ' || pairs.substring(i, i + splitter.length()).equals(splitter)) {
                continue;
            }
            StringBuilder var = new StringBuilder();
            if (pairs.substring(i, i + escaper.length()).equals(escaper)) {
                i += escaper.length();
                while (i < charPairs.length && !pairs.substring(i, i + escaper.length()).equals(escaper)) {
                    var.append(charPairs[i]);
                    i++;
                }
            } else {
                while (i < charPairs.length && !pairs.substring(i, i + splitter.length()).equals(splitter)) {
                    var.append(charPairs[i]);
                    i++;
                }
            }
            if (key == null) {
                key = new String(var).trim();
            } else {
                value = new String(var).trim();
                finalPairs.add(key);
                finalPairs.add(value);
                key = null;
                value = null;
            }
        }
        String[] stockArr = new String[finalPairs.size()];
        return finalPairs.toArray(stockArr);
    }

    // String format:
    //  1) Simple format - comma-delimited pairs of key = value:
    //      name1=value1,name2=value2,name3=value3
    //  2) In case of LOVs - some values are <Lists of values>:
    //      name1=(value11;value12;value13),name2=(value21;value22;value23),name3=value3
    // Addition at 14.03.2017:
    //  3) [Column name] operand Value(s)
    //      where:
    //          - [Column name] - name of ar/er column, must be surrounded with [] if contains spaces
    //          - operand - one of { '=' , '<>' , '<'  , '>' , '<=' , '>=' }
    //          - Value(s) - single fixed value
    //                          OR er-table column name in [] for example [Offering (German)]
    //                          OR list of values in () delimited with ; for example (0;1;5;10)
    private Map<String, FilterSpecification> parseFilterPairs(String cfgItem) {
        String[] pairs = cfgItem.split(",");
        Map<String, FilterSpecification> keyVals = new HashMap<>();

        for (int i = 0; i < pairs.length; i++) {
            // At the beginning there were really 'pairs' of key=value.
            // But now string format is more complex (see comments above)
            String[] keys = pairs[i].split("=|<|>|like|unlike");

            if (keys.length < 2) {
                continue; // invalid format; maybe we should throw an exception here...
            } else {
                String tail = pairs[i].substring(keys[0].length()).trim(); // tail consists of <operand> and <value(s)>
                String key = keys[0].trim(); // this is column name optionally surrounded with []
                if (key.startsWith("[") && key.endsWith("]")) {
                    key = key.substring(1, key.length() - 1).trim();
                }
                if (key.isEmpty()) {
                    continue; // invalid format
                }
                String curOperand = getComparisonOperand(tail);
                if (curOperand.isEmpty()) {
                    continue; // invalid format; may be we should throw an exception here...
                } else {
                    tail = tail.substring(curOperand.length()).trim(); // currently tail consists of <value(s)> only
                    if (tail.isEmpty()) {
                        continue; // invalid format
                    } else {
                        List<String> lov = new ArrayList<>();
                        if (tail.startsWith("(") && tail.endsWith(")")) {
                            // This is semicolon-delimited list of values
                            String[] valueItems = tail.substring(1, tail.length() - 1).split(";");
                            for (String s : valueItems) {
                                s = clearValue(s);
                            }
                            lov.addAll(Arrays.asList(valueItems));
                        } else {
                            lov.add(clearValue(tail));
                        }
                        FilterSpecification spec = new FilterSpecification();
                        spec.comparisonOperand = curOperand;
                        spec.lov = lov;
                        keyVals.put(key, spec);
                    }
                }
            }
        }
        return keyVals;
    }

    private String clearValue(String value) {
        String val = value.trim().replaceAll("\\\\u002C", ",");
        if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith("\"") && val.endsWith("\""))) {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }

    private String getComparisonOperand(String str) {
        for (ComparisonOperand operand: ComparisonOperand.values()) {
            if (str.startsWith(operand.getSymbols())) {
                return operand.getSymbols();
            }
        }
        return "";
    }

    public class ReplacementSpecification {

        public String searchString;
        public String replaceString;
        public boolean replaceEmpty;

        public ReplacementSpecification() {
        }

        public ReplacementSpecification(String searchString, String replaceString) {
            this.searchString = searchString;
            this.replaceString = replaceString;
            this.replaceEmpty = (searchString.equalsIgnoreCase("[empty]"));
        }
    }

    public class FilterSpecification {

        public String comparisonOperand;
        public List<String> lov;
    }

    private List<ReplacementSpecification> parseReplacement(String cfgItem) {
        String[] pairs = cfgItem.split("=");
        List<ReplacementSpecification> keyVals = new ArrayList<>();

        if (pairs.length == 2) {
            String key = clearValue(pairs[0]);
            if (!key.isEmpty()) {
                keyVals.add(new ReplacementSpecification(key, clearValue(pairs[1])));
            }
        }
        return keyVals;
    }
}
