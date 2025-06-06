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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.comparator.impl.table.CheckPocRule;
import org.qubership.automation.pc.comparator.impl.table.CheckPocSection;
import org.qubership.automation.pc.comparator.impl.table.CheckPocSectionType;
import org.qubership.automation.pc.comparator.impl.table.ComparisonOperand;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.exceptions.FailedToParseException;
import org.qubership.automation.pc.models.Table;
import org.qubership.automation.pc.models.TablesList;
import org.qubership.automation.pc.models.table.CheckColumnOperations;
import org.qubership.automation.pc.models.table.CheckColumnRule;

import lombok.extern.slf4j.Slf4j;

/**
 * Comparator implementation for validating tabular data (ER/AR tables).
 * </p>
 * This comparator supports flexible rules for comparing tables using various strategies:
 * <ul>
 *     <li>POC-based checks for rule-driven validation.</li>
 *     <li>Column-specific checks through user-defined rules.</li>
 *     <li>Row-by-row and cell-by-cell default comparison logic.</li>
 *     <li>Support for options like ignoring case, treating cells as regular expressions,
 *         and skipping extra/missing rows or cells.</li>
 * </ul>
 * </p>
 * Comparison behavior is dynamically configured using {@link Parameters} and
 * controlled via several pre-defined configuration flags.
 * </p>
 * Depending on configuration, the comparator can:
 * <ul>
 *     <li>Apply value replacements in the actual result (AR) table.</li>
 *     <li>Resolve aliases in the expected result (ER) table.</li>
 *     <li>Use key-based filtering, custom column validation, and order enforcement.</li>
 * </ul>
 * </p>
 * The comparator returns a list of {@link DiffMessage} objects indicating any
 * differences found between ER and AR tables.
 */
@Slf4j
public class TableComparator extends AbstractComparator {

    public static final String PARAMETER_NAME_CHECK_POC = "checkPOC";
    public static final String PARAMETER_NAME_IGNORE_CASE = "ignoreCase"; // true / false (default)
    public static final String PARAMETER_IGNORE_COLUMN_CASE = "ignoreColumnCase";
    public static final String PARAMETER_CHECK_COLUMN_VALUE = "checkColumn";
    public static final String PARAMETER_CELL_AS_REGEXP = "cellAsRegexp";
    public static final String PARAMETER_IGNORE_MISSED = "ignoreMissed";
    public static final String PARAMETER_IGNORE_EXTRA = "ignoreExtra";

    List<String> checkPocRules = new ArrayList<>();
    List<CheckPocSection> checkConfig = new ArrayList<>();
    List<CheckColumnRule> checkColumnRules = new ArrayList<>();
    boolean ignoreCase = false;
    boolean ignoreColumnCase = false;
    boolean cellAsRegexp = false;
    boolean ignoreMissed = false;
    boolean ignoreExtra = false;

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        String erContent = er;
        String arContent = ar;

        // Currently (20/02/2017) there are no actions to do if er or ar are empty
        // Thats why we simply return empty list of diffMessages in this case
        if (StringUtils.isBlank(er) && StringUtils.isBlank(ar)) {
            return new ArrayList<>();
        } else if (StringUtils.isBlank(er)) {
            erContent = "{}";
        } else if (StringUtils.isBlank(ar)) {
            arContent = "{}";
        }

        try {
            getConfigurationParameters(configuration);

            if (!checkConfig.isEmpty()) {
                return checkPoc(erContent, arContent, checkConfig);
            } else if (!checkColumnRules.isEmpty()) {
                // This rule was added to provide SVp tool required functionality. If it's present in list of
                // rules - others will be skipped
                // and table will be processed differently (only ar, not designed to work with highlighter)
                // Rule syntax <columnName>=<operation>=<expected_value> (Status=NOT_EQUALS=FAILED)
                List<DiffMessage> diffs = new ArrayList<>();
                for (CheckColumnRule rule : checkColumnRules) {
                    // TODO: This is a quick implementation. May be prone to instability
                    diffs.addAll(rule.processTable(TablesList.getTableListFromJson(arContent).get(0)));
                }
                return diffs;
            } else {
                return compareTablesByDefault(erContent, arContent);
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ComparatorException("Error while parsing input message(s). Probably it is not valid JSON.\n"
                    + e.getMessage(), e);
        }
    }

    private List<Table.TableRow> filterErRows(List<Table.TableRow> erRows, int colIndex, String operand,
                                              String filterValue, int[] filterErIndexes) {
        for (int i = 0; i < erRows.size(); ) {
            Table.TableRow row = erRows.get(i);
            String colValue = row.get(colIndex);
            switch (operand) {
                case "<>":
                    if (NumberUtils.isNumber(filterValue)) {
                        if (Integer.parseInt(colValue) == Integer.parseInt(filterValue)) {
                            erRows.remove(i);
                            updateFilterErRowIndexes(filterErIndexes, i);
                        } else {
                            i++;
                        }
                    } else if (colValue.equals(filterValue)) {
                        erRows.remove(i);
                        updateFilterErRowIndexes(filterErIndexes, i);
                    } else {
                        i++;
                    }
                    break;
                case "<":
                    if (Integer.parseInt(colValue) >= Integer.parseInt(filterValue)) {
                        erRows.remove(i);
                        updateFilterErRowIndexes(filterErIndexes, i);
                    } else {
                        i++;
                    }
                    break;
                case ">":
                    if (Integer.parseInt(colValue) <= Integer.parseInt(filterValue)) {
                        erRows.remove(i);
                        updateFilterErRowIndexes(filterErIndexes, i);
                    } else {
                        i++;
                    }
                    break;
                case "=":
                    if (NumberUtils.isNumber(filterValue)) {
                        if (Integer.parseInt(colValue) != Integer.parseInt(filterValue)) {
                            erRows.remove(i);
                            updateFilterErRowIndexes(filterErIndexes, i);
                        } else {
                            i++;
                        }
                    } else if (!colValue.equals(filterValue)) {
                        erRows.remove(i);
                        updateFilterErRowIndexes(filterErIndexes, i);
                    } else {
                        i++;
                    }
                    break;
                default:
                    i++; //operands >=, <=
            }
        }
        return erRows;
    }

    //create a map of row indexes of original and filtered erTable
    private int[] updateFilterErRowIndexes(int[] filterErIndexes, int i) {
        for (int j = i; j < filterErIndexes.length; j++) {
            filterErIndexes[j] = j + 1 < filterErIndexes.length ? filterErIndexes[j + 1] : -1;
        }
        return filterErIndexes;
    }

    private void getConfigurationParameters(Parameters configuration) throws ComparatorException {
        ignoreCase = configuration.getBooleanParameter(PARAMETER_NAME_IGNORE_CASE, ignoreCase);
        ignoreColumnCase = configuration.getBooleanParameter(PARAMETER_IGNORE_COLUMN_CASE, false);
        cellAsRegexp = configuration.getBooleanParameter(PARAMETER_CELL_AS_REGEXP, false);
        ignoreMissed = configuration.getBooleanParameter(PARAMETER_IGNORE_MISSED, false);
        ignoreExtra = configuration.getBooleanParameter(PARAMETER_IGNORE_EXTRA, false);
        List<String> rows = configuration.getParameters(PARAMETER_NAME_CHECK_POC);
        if (Objects.nonNull(rows) && !rows.isEmpty()) {
            CheckPocRule checkPocRule = new CheckPocRule(rows);
            checkConfig.addAll(checkPocRule.getCheckPocSections());
        }
        List<String> checkColumns = configuration.getParameters(PARAMETER_CHECK_COLUMN_VALUE);
        checkColumnRules = new ArrayList<>();
        if (checkColumns != null) {
            checkColumns.forEach(this::parseCheckColumnRule);
        }
    }

    /**
     * Parsing checkColumn rule from raw string into object.
     */
    private void parseCheckColumnRule(String rawString) {
        String[] rawSplit = rawString.split("=");
        if (rawSplit.length == 3) {
            try {
                checkColumnRules.add(new CheckColumnRule(rawSplit[0], CheckColumnOperations.valueOf(rawSplit[1]),
                        Arrays.asList(rawSplit[2].split(","))));
            } catch (IllegalArgumentException ex) {
                log.warn("[PC2] Tried to parse 'checkColumn' rule but failed! ", ex);
            }
        } else {
            log.warn("[PC2] Skipped 'checkColumn' rule {}", rawString);
        }
    }

    private List<DiffMessage> compareTablesByDefault(String jsonStringER,
                                                     String jsonStringAR) throws ComparatorException {
        int diffCounter = 1;
        try {
            List<DiffMessage> diffMessages = new ArrayList<>();
            // Initialize ar table description & rows
            TablesList arTables = TablesList.getTableListFromJson(jsonStringAR);
            if (arTables == null || arTables.isEmpty()) {
                throw new ComparatorException("ar table is missed! "
                        + "Please check source configuration and/or input files.");
            }
            //            else if (arTables.size() > 1) {
            //                throw new ComparatorException("ar should contain only one table!
            //                Please check source configuration and/or input files.");
            //            }

            TablesList erTables = TablesList.getTableListFromJson(jsonStringER);
            if (erTables == null || erTables.isEmpty()) {
                throw new ComparatorException("er tables are missed! "
                        + "Please check source configuration and/or input files.");
            }
            //            else if (erTables.size() > 1) {
            //                throw new ComparatorException("er should contain only one table!
            //                Please check source configuration and/or input files.");
            //            }
            for (int a = 0; a < Math.min(arTables.size(), erTables.size()); a++) {
                Table arTable = arTables.get(a);
                Table erTable = erTables.get(a);

                // How we should compare two tables without any configuration rules - it's a big question...
                // Currently, we only compare ROWS: 1st er-row with 1st ar-row, 2nd - 2nd and so on
                //  Cells are compared similar way: 1st - 1st, 2nd - 2nd, ...
                //      Cell values are compared as strings, for equality
                for (int i = 0; i < Math.min(erTable.rows.size(), arTable.rows.size()); i++) {
                    Table.TableRow erRow = erTable.rows.get(i);
                    Table.TableRow arRow = arTable.rows.get(i);
                    for (int k = 0; k < Math.min(erRow.size(), arRow.size()); k++) {
                        if (!equalsByRule(arRow.get(k), erRow.get(k))) {
                            // Cell values are different. Report error
                            diffMessages.add(new DiffMessage(diffCounter++,
                                    "" + "/" + erTable.name + "/" + i + "/" + k + "/" + erTable.headers.get(k),
                                    "" + "/" + arTable.name + "/" + i + "/" + k + "/" + arTable.headers.get(k),
                                    ResultType.MODIFIED,
                                    "Cell [" + i + ", " + k + "]: values are different"));
                        }
                    }
                    if (erRow.size() > arRow.size()) {
                        // Report error about extra er cells - once for the entire er-row's tail
                        diffMessages.add(new DiffMessage(diffCounter++,
                                "" + "/" + erTable.name + "/" + i + "/" + arRow.size() + "/"
                                        + erTable.headers.get(arRow.size()),
                                "",
                                ResultType.MISSED,
                                "er row# " + i + " has extra cells."));

                    } else if (erRow.size() < arRow.size()) {
                        // Report error about extra ar cells - once for the entire ar-row's tail
                        diffMessages.add(new DiffMessage(diffCounter++,
                                "",
                                "" + "/" + arTable.name + "/" + i + "/" + erRow.size() + "/"
                                        + arTable.headers.get(erRow.size()),
                                ResultType.EXTRA,
                                "ar row# " + i + " has extra cells."));
                    }
                }
                // Report error about extra ar rows - once for each extra row
                for (int i = erTable.rows.size(); i < arTable.rows.size(); i++) {
                    diffMessages.add(new DiffMessage(diffCounter++, "", "" + "/" + arTable.name
                            + "/" + i, ResultType.EXTRA, "ar row# " + i + " is extra."));
                }
                // Report error about extra er rows - once for each extra row
                for (int i = arTable.rows.size(); i < erTable.rows.size(); i++) {
                    diffMessages.add(new DiffMessage(diffCounter++, "" + "/" + erTable.name + "/"
                            + i, "", ResultType.MISSED, "er row# " + i + " is missed."));
                }
            }
            processIgnoreMissedAndExtraRules(diffMessages);
            return diffMessages;
        } catch (Exception ex) {
            throw new ComparatorException("Error while comparing er/ar tables. "
                    + "Please check er/ar tables and/or rules configuration.\n" + ex.getMessage(), ex);
        }
    }

    private List<DiffMessage> checkPoc(String jsonStringER,
                                       String jsonStringAR,
                                       List<CheckPocSection> checkConfig) throws ComparatorException {
        int diffCounter = 1;
        try {
            List<DiffMessage> diffMessages = new ArrayList<>();
            if (!checkConfig.isEmpty()) {
                // Initialize ar table description & rows
                TablesList arTables = TablesList.getTableListFromJson(jsonStringAR);
                if (arTables == null || arTables.isEmpty()) {
                    throw new ComparatorException("ar table is missed! "
                            + "Please check source configuration and/or input files.");
                } else if (arTables.size() > 1) {
                    throw new ComparatorException("ar should contain only one table! "
                            + "Please check source configuration and/or input files.");
                }
                Table arTable = arTables.get(0);

                TablesList erTables = TablesList.getTableListFromJson(jsonStringER);
                if (erTables == null || erTables.isEmpty()) {
                    throw new ComparatorException("er tables are missed! "
                            + "Please check source configuration and/or input files.");
                }
                Map<String, List<String>> aliases = new HashMap<>();

                // Make all replacements on the ar table...
                for (CheckPocSection item : checkConfig) {
                    if (CheckPocSectionType.REPLACE.equals(item.cfgType)) {
                        // May be replacement should be applied not to the whole table but to specified columns?
                        List<Integer> colNumbers = new ArrayList<>();
                        if (!item.colName.isEmpty()) {
                            String[] colNames = item.colName.split(",");
                            for (int m = 0; m < colNames.length; m++) {
                                String s = colNames[m].trim();
                                if (s.isEmpty()) {
                                    continue;
                                }
                                // Start index = 1 because column[0] contains physical row number.
                                // We should not spoil them (even in case when user configured such replacement)
                                for (int k = 1; k < arTable.headers.size(); k++) {
                                    if (s.equals(arTable.headers.get(k))) {
                                        colNumbers.add(k);
                                        break;
                                    }
                                }
                            }
                        }
                        for (int rowIndex = 0; rowIndex < arTable.rows.size(); rowIndex++) {
                            // Start index = 1 because column[0] contains physical row number. We should not spoil them
                            for (int columnIndex = 1; columnIndex < arTable.rows.get(rowIndex).size(); columnIndex++) {
                                if (!colNumbers.isEmpty()) {
                                    // colNumbers contains column indexes for replacement (see above)
                                    if (!colNumbers.contains(columnIndex)) {
                                        continue;
                                    }
                                }
                                String s = arTable.rows.get(rowIndex).get(columnIndex);
                                for (CheckPocSection.ReplacementSpecification entry : item.replacements) {
                                    if (s.isEmpty()) {
                                        if (entry.replaceEmpty
                                                && entry.replaceString.equals("$$$PC.TABLE.FIND_PREV$$$")) {
                                            // [empty] = "$$$PC.TABLE.FIND_PREV$$$"
                                            s = arTable.rows.get(rowIndex - 1).get(columnIndex);
                                        } else if (entry.replaceEmpty) {
                                            s = entry.replaceString;
                                            break;
                                        }
                                    } else if (!entry.replaceEmpty) {
                                        s = s.replace(entry.searchString, entry.replaceString);
                                    }
                                }
                                if (!s.equals(arTable.rows.get(rowIndex).get(columnIndex))) {
                                    arTable.rows.get(rowIndex).set(columnIndex, s);
                                }
                            }
                        }
                    }
                }

                // Prepare all aliases...
                int[] filterErIndexes = new int[0];
                boolean aliasesFiltered = false;
                for (CheckPocSection item : checkConfig) {
                    if (CheckPocSectionType.ALIAS.equals(item.cfgType)) {
                        List<String> colValues = getLookup(erTables, item.erTable, item.colName, item.filters);
                        aliases.put(item.cfgName, colValues);
                        //filter er table
                        if (!item.filters.isEmpty()) {
                            //create a default map of row indexes where a massive index
                            // = index in original er and value = index in filtered er
                            filterErIndexes = IntStream.range(0,
                                    getTableByName(erTables, item.erTable).rows.size()).toArray();
                            aliasesFiltered = true;
                            filterER(erTables, item, filterErIndexes);
                        }
                    }
                }

                Map<String, Integer> arTableHeaderIds = getHeadersMap(arTable);
                for (CheckPocSection item : checkConfig) {
                    if (CheckPocSectionType.CHECK.equals(item.cfgType)) {
                        // Find er table...
                        Table checkTable = getTableByName(erTables, item.erTable);
                        if (Objects.isNull(checkTable)) {
                            throw new ComparatorException("er table '" + item.erTable + "' is missed! "
                                    + "Please check source configuration.");
                        } else {
                            // Prepare HeadersMap for er-table
                            Map<String, Integer> checkTableHeaderIds = getHeadersMap(checkTable);

                            // Replace aliases to real values
                            // As a result of this action er-tables are changed.
                            // Maybe we should re-get them from er-input at the beginning of each checking?
                            int originalErSize = checkTable.rows.size();
                            boolean aliasesReplaced = replaceAliasesToValues(checkTable, aliases);

                            // Prepare FiltersMap for ar-table
                            Map<Integer, CheckPocSection.FilterSpecification> filterIds
                                    = getFiltersMap(item.filters, arTableHeaderIds);

                            for (Map.Entry entry : filterIds.entrySet()) {
                                CheckPocSection.FilterSpecification spec
                                        = (CheckPocSection.FilterSpecification) entry.getValue();
                                if (spec.lov.size() == 1
                                        && spec.lov.get(0).startsWith("[") && spec.lov.get(0).endsWith("]")) {
                                    List<String> localAliasValues = new ArrayList<>();
                                    localAliasValues.addAll(getLookup(erTables, item.erTable,
                                            spec.lov.get(0).substring(1, spec.lov.get(0).length() - 1).trim(),
                                            new HashMap<>()));
                                    if (!localAliasValues.isEmpty()) {
                                        spec.lov.clear();
                                        spec.lov.addAll(localAliasValues);
                                        entry.setValue(spec);
                                        //break; // only one Lookup is allowed here!
                                    }
                                }
                            }

                            // Prepare RelationsMaps...
                            List<Relation> relationList
                                    = prepareRelations(item.relations, arTableHeaderIds, checkTableHeaderIds);
                            List<Relation> columnsList
                                    = prepareRelations(item.columns, arTableHeaderIds, checkTableHeaderIds);

                            // Loop through ar rows (+apply filter)
                            List<Integer> arFilteredRowNumbers = new ArrayList<>();
                            List<Integer> matchingErRowNumbers = new ArrayList<>();

                            for (int i = 0; i < arTable.rows.size(); i++) {
                                if (checkFilter(arTable.rows.get(i), filterIds)) {
                                    //Saving rowNumbers to List for future use
                                    arFilteredRowNumbers.add(i);

                                    // Fill relation filter values for searching corresponding row in checkTable...
                                    for (Relation rel : relationList) {
                                        rel.arColValue = arTable.rows.get(i).get(rel.arColumnId);
                                    }

                                    //Searching corresponding row in checkTable
                                    int erRowIdx = searchErRow(checkTable, relationList, matchingErRowNumbers);
                                    if (erRowIdx == -1) {
                                        // Corresponding row doesn't exist. Report error
                                        diffMessages.add(
                                                new DiffMessage(
                                                        diffCounter++,
                                                        "",
                                                        item.cfgName
                                                                + "/"
                                                                +
                                                                arTable.name
                                                                + "/"
                                                                + i,
                                                        ResultType.EXTRA, "ar row# " + i + " is extra."));
                                    } else {
                                        matchingErRowNumbers.add(erRowIdx);

                                        // Check attributes...
                                        for (Relation col : columnsList) {
                                            col.arColValue = arTable.rows.get(i).get(col.arColumnId);
                                            col.erColValue = checkTable.rows.get(erRowIdx).get(col.erColumnId);
                                            //if( !col.arColValue.equals(col.erColValue) ) {
                                            if (!equalsByRule(col.arColValue, col.erColValue)) {
                                                // Attribute values are different. Report error
                                                String reportMessage = "";
                                                int originalRowId = erRowIdx;
                                                if (aliasesReplaced) {
                                                    if (erRowIdx >= originalErSize
                                                            || checkTable.headers.size()
                                                            < checkTable.rows.get(erRowIdx).size()) {
                                                        reportMessage = checkTable.rows
                                                                .get(erRowIdx)
                                                                .get(checkTable.headers.size());
                                                        int d = reportMessage.indexOf(" ");
                                                        originalRowId = Integer.parseInt(reportMessage.substring(0, d));
                                                        reportMessage = reportMessage.substring(d + 1);
                                                    }
                                                } else if (aliasesFiltered) {
                                                    originalRowId = filterErIndexes[erRowIdx];
                                                }
                                                diffMessages.add(new DiffMessage(diffCounter++,
                                                        item.cfgName + "/"
                                                                + checkTable.name + "/" + originalRowId + "/"
                                                                + col.erColumnId + "/"
                                                                + checkTable.headers.get(col.erColumnId)
                                                                + "/" + reportMessage,
                                                        item.cfgName + "/"
                                                                + arTable.name + "/" + i + "/"
                                                                + col.arColumnId + "/"
                                                                + arTable.headers.get(col.arColumnId),
                                                        ResultType.MODIFIED, reportMessage));
                                            }
                                        }
                                    }
                                }
                            }

                            // Last step of checking: if there are unmatched rows in checkTable - report error
                            if (matchingErRowNumbers.size() < checkTable.rows.size()) {
                                for (int i = 0; i < checkTable.rows.size(); i++) {
                                    if (!matchingErRowNumbers.contains((Integer) i)) {
                                        // report Error
                                        String reportMessage = "";
                                        int originalRowId = i;
                                        if (aliasesReplaced) {
                                            if (i >= originalErSize
                                                    || checkTable.headers.size() < checkTable.rows.get(i).size()) {
                                                reportMessage = checkTable.rows.get(i).get(checkTable.headers.size());
                                                int d = reportMessage.indexOf(" ");
                                                originalRowId = Integer.parseInt(reportMessage.substring(0, d));
                                                reportMessage = reportMessage.substring(d + 1);
                                            }
                                        } else if (aliasesFiltered) {
                                            originalRowId = filterErIndexes[originalRowId];
                                        }
                                        diffMessages.add(new DiffMessage(diffCounter++,
                                                item.cfgName + "/"
                                                        + checkTable.name + "/" + originalRowId + "///"
                                                        + reportMessage, "", ResultType.MISSED, reportMessage));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            processIgnoreMissedAndExtraRules(diffMessages);
            return diffMessages;
        } catch (Exception ex) {
            throw new ComparatorException("Error while comparing er/ar tables. "
                    + "Please check rules configuration.\n" + ex.getMessage(), ex);
        }
    }

    private void processIgnoreMissedAndExtraRules(List<DiffMessage> diffs) {
        if (ignoreMissed) {
            changeResultType(diffs, ResultType.MISSED, ResultType.IDENTICAL, "ignoreMissed rule");
        }
        if (ignoreExtra) {
            changeResultType(diffs, ResultType.EXTRA, ResultType.IDENTICAL, "ignoreExtra rule");
        }
    }

    private void changeResultType(List<DiffMessage> diffs, ResultType oldType, ResultType newType, String reason) {
        String message = "Result was changed from " + oldType + " to " + newType + "due to " + reason;
        diffs.stream()
                .filter(diff -> Objects.isNull(oldType) ? Objects.isNull(diff.getResult())
                        : oldType.equals(diff.getResult()))
                .forEach(diff -> {
                    diff.setResult(newType);
                    diff.setDescription(message);
                });
    }

    private boolean replaceAliasesToValues(Table checkTable, Map<String, List<String>> aliases) {
        List<Table.TableRow> addRows = new ArrayList<>();
        boolean result = false;
        for (int i = 0; i < checkTable.rows.size(); i++) {
            Table.TableRow thisRow = checkTable.rows.get(i);
            for (int k = 0; k < thisRow.size(); k++) {
                String cell = thisRow.get(k);
                if (aliases.containsKey(cell)) {
                    List<String> values = aliases.get(cell);
                    String reportReplace = i + " Column '" + checkTable.headers.get(k) + "': value '" + cell
                            + "' replaced with ";
                    boolean start = true;
                    for (String value : values) {
                        if (start) {
                            result = true; // return value (true means replacement(s) are really made)
                            start = false;
                            thisRow.set(k, value);
                            thisRow.add(reportReplace + "'" + value + "'");
                        } else {
                            Table.TableRow addRow = new Table.TableRow();
                            addRow.addAll(thisRow);
                            addRow.set(k, value);
                            addRow.set(thisRow.size() - 1, reportReplace + "'" + value + "'");
                            addRows.add(addRow);
                        }
                    }
                    break; // only one alias replacement allowed for row
                }
            }
        }
        checkTable.rows.addAll(addRows);
        return result;
    }

    private int searchErRow(Table checkTable, List<Relation> relationList, List<Integer> matchingErRowNumbers) {
        for (int i = 0; i < checkTable.rows.size(); i++) {
            if (!matchingErRowNumbers.contains((Integer) i)) {
                boolean found = true;
                for (Relation rel : relationList) {
                    //if( !rel.arColValue.equals(checkTable.rows.get(i).get(rel.erColumnId)) ) {
                    if (!equalsByRule(rel.arColValue, checkTable.rows.get(i).get(rel.erColumnId))) {
                        found = false;
                        break;
                    }
                }
                if (found) {
                    return i;
                }
            }
        }
        return -1;
    }

    private List<String> getLookup(List<Table> erTables, String erTable, String colName, Map<String,
            CheckPocSection.FilterSpecification> filters) {
        List<String> result = new ArrayList<>();

        Table lookupTable = getTableByName(erTables, erTable);
        if (lookupTable != null) {
            Map<String, Integer> headerIds = getHeadersMap(lookupTable);
            Map<Integer, CheckPocSection.FilterSpecification> filterIds = getFiltersMap(filters, headerIds);
            Integer idx = headerIds.get(colName);
            if (idx != null) {
                return getColumnValues(lookupTable, idx, filterIds, true);
            }
        }
        return result;
    }

    private Table getTableByName(List<Table> tables, String tableName) {
        for (Table tbl : tables) {
            if (tbl.name.equals(tableName)) {
                return tbl;
            }
        }
        return null;
    }

    private Map<String, Integer> getHeadersMap(Table lookupTable) {
        Map<String, Integer> headerIds = new HashMap<>();
        for (int i = 0; i < lookupTable.headers.size(); i++) {
            headerIds.put(lookupTable.headers.get(i), i);
        }
        return headerIds;
    }

    private Map<Integer, CheckPocSection.FilterSpecification> getFiltersMap(
            Map<String, CheckPocSection.FilterSpecification> filters, Map<String, Integer> headerIds) {
        Map<Integer, CheckPocSection.FilterSpecification> filterIds = new LinkedHashMap<>();
        for (Map.Entry entry : filters.entrySet()) {
            Integer i = headerIds.get(entry.getKey().toString());
            if (i != null) {
                CheckPocSection.FilterSpecification s = (CheckPocSection.FilterSpecification) entry.getValue();
                filterIds.put(i, s);
            }
        }
        return filterIds;
    }

    private List<Relation> prepareRelations(Map<String, String> relations, Map<String, Integer> arHeaderIds,
                                            Map<String, Integer> erHeaderIds) {
        List<Relation> result = new ArrayList<>();
        for (Map.Entry entry : relations.entrySet()) {
            String arKey = entry.getKey().toString();
            String erKey = entry.getValue().toString();
            Relation rel = new Relation();
            rel.arColName = arKey;
            rel.erColName = erKey;
            try {
                rel.arColumnId = findHeader(arHeaderIds, arKey);
                rel.erColumnId = findHeader(erHeaderIds, erKey);
            } catch (FailedToParseException ex) {
                throw new FailedToParseException(String.format("Can’t find columns specified in CheckPOC rule"
                        + " configuration: er-table %s, ar-table %s.", erKey, arKey));
            }
            result.add(rel);
        }
        return result;
    }

    private int findHeader(Map<String, Integer> headerIds, String key) {
        if (ignoreColumnCase) {
            for (String s : headerIds.keySet()) {
                if (StringUtils.equalsIgnoreCase(s, key)) {
                    return headerIds.get(s);
                }
            }
        } else if (headerIds.containsKey(key)) {
            return headerIds.get(key);
        }
        throw new FailedToParseException(String.format("Failed to find header %s. Please check configuration", key));
    }

    private List<String> getColumnValues(Table lookupTable,
                                         int idx,
                                         Map<Integer, CheckPocSection.FilterSpecification> filterIds,
                                         boolean distinct) {
        List<String> result = new ArrayList<>();

        for (Table.TableRow row : lookupTable.rows) {
            if (!checkFilter(row, filterIds)) {
                continue;
            }
            try {
                // May be because of this checking the type of result will be changed to... Hashset for example
                if (distinct && result.contains(row.get(idx))) {
                    continue;
                }
                result.add(row.get(idx));
            } catch (IndexOutOfBoundsException ex) {
                // Do nothing!
            }
        }
        return result;
    }

    private boolean checkFilter(Table.TableRow row, Map<Integer, CheckPocSection.FilterSpecification> filterIds) {
        if (filterIds.isEmpty()) {
            return true;
        } else {
            for (Map.Entry entry : filterIds.entrySet()) {
                //if( !row.get((Integer)entry.getKey()).equals(entry.getValue().toString() ) ) {
                CheckPocSection.FilterSpecification filterSpec
                        = (CheckPocSection.FilterSpecification) entry.getValue();
                if (!inLovByRule(row.get((Integer) entry.getKey()), filterSpec.comparisonOperand, filterSpec.lov)) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean equalsByRule(String str1, String str2) {
        if (cellAsRegexp) {
            return str1.matches(str2);
        } else if (ignoreCase) {
            return str1.equalsIgnoreCase(str2);
        } else {
            return str1.equals(str2);
        }
    }

    private boolean inLovByRule(String str, String operand, List<String> strList) {
        switch (ComparisonOperand.fromSymbols(operand)) {
            case EQUAL:
                return checkEqual(str, strList);
            case NOTEQUAL:
                return !checkEqual(str, strList);
            case LESS_OR_EQUAL:
                // this is unary operation; only 1st element of strList is compared
                return checkLessOrEqual(str, strList.get(0), false);
            case MORE_OR_EQUAL:
                // this is unary operation; only 1st element of strList is compared
                return !checkLessOrEqual(str, strList.get(0), true);
            case LESS:
                // this is unary operation; only 1st element of strList is compared
                return checkLessOrEqual(str, strList.get(0), true);
            case MORE:
                // this is unary operation; only 1st element of strList is compared
                return !checkLessOrEqual(str, strList.get(0), false);
            case LIKE:
                // this is unary operation; only 1st element of strList is compared
                return checkLike(str, strList.get(0));
            case UNLIKE:
                return !checkLike(str, strList.get(0));
            default:
                return false;
        }
    }

    private boolean checkEqual(String str, List<String> strList) {
        if (ignoreCase) {
            for (String s : strList) {
                if (str.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        } else {
            return strList.contains(str);
        }
    }

    private boolean checkLessOrEqual(String str1, String str2, boolean strictComparison) {
        // Special case of comparison - date comparison, dor example: ColumnValue < date('17.03.2015','dd.mm.yyyy')
        if (str2.matches("\\S+\\(.*\\)")) {
            String methodName = str2.substring(0, str2.indexOf("("));
            String params = str2.substring(str2.indexOf("(") + 1, str2.length() - 1);
            String[] paramsList = params.split(",");
            for (String s : paramsList) {
                s = clearValue(s);
            }

            try {
                Class<?> resourceClass
                        = Class.forName(
                                "org.qubership.automation.pc.comparator.impl.TableComparator.FilterFunctions");
                Object instance = resourceClass.newInstance();
                Method[] methods = resourceClass.getDeclaredMethods();
                for (Method method : methods) {
                    if (method.getName().equals(methodName)) {
                        String result2 = (method.getParameterTypes().length == 0)
                                ? (String) (method.invoke(instance))
                                : (String) (method.invoke(instance, paramsList.toString()));
                        paramsList[0] = str1;
                        String result1 = (method.getParameterTypes().length == 0)
                                ? (String) (method.invoke(instance))
                                : (String) (method.invoke(instance, paramsList.toString()));

                        int i = (ignoreCase) ? result1.compareToIgnoreCase(str2) : result2.compareTo(str2);
                        if (i < 0) {
                            return true;
                        } else if (i == 0) {
                            return !strictComparison;
                        } else {
                            return false;
                        }
                    }
                }
            } catch (ClassNotFoundException ex) {
                // Do nothing!
            } catch (InstantiationException ex) {
                // Do nothing!
            } catch (IllegalAccessException ex) {
                // Do nothing!
            } catch (IllegalArgumentException ex) {
                // Do nothing!
            } catch (InvocationTargetException ex) {
                // Do nothing!
            }
        }

        int i = (ignoreCase) ? str1.compareToIgnoreCase(str2) : str1.compareTo(str2);
        if (i < 0) {
            return true;
        } else if (i == 0) {
            return !strictComparison;
        } else {
            return false;
        }
    }

    private boolean checkLike(String str, String strTemplate) {
        final String regexpKeyword = "regexp:";
        if (strTemplate.startsWith(regexpKeyword)) {
            String regexpSubstring = strTemplate.substring(regexpKeyword.length(), strTemplate.length());
            return str.matches(regexpSubstring);
        } else {
            if (ignoreCase) {
                return str.toUpperCase().contains(strTemplate);
            } else {
                return str.contains(strTemplate);
            }
        }
    }

    private String clearValue(String value) {
        String val = value.trim().replaceAll("\\\\u002C", ",");
        if ((val.startsWith("'") && val.endsWith("'")) || (val.startsWith("\"") && val.endsWith("\""))) {
            return val.substring(1, val.length() - 1);
        }
        return val;
    }

    private void filterER(TablesList erTables, CheckPocSection item, int[] filterErIndexes) {
        Table erTable = erTables.stream()
                .filter(table -> table.name.equals(item.erTable))
                .findFirst()
                .get();
        for (Map.Entry<String, CheckPocSection.FilterSpecification> filter : item.filters.entrySet()) {
            CheckPocSection.FilterSpecification value = filter.getValue();
            erTable.rows = erTable.rows != null ? filterErRows(erTable.rows, erTable.getColumnIndex(filter.getKey()),
                    value.comparisonOperand, value.lov.get(0).equals("null")
                            ? "" : value.lov.get(0), filterErIndexes) : null;
        }
    }

    public static class FilterFunctions {

        public String date(String str, String mask) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(mask, Locale.ENGLISH); // "dd-MM-yyyy HH:mm:ss"
                Date thisDate = inputFormat.parse(str);
                SimpleDateFormat outputFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.ENGLISH);
                return outputFormat.format(thisDate);
            } catch (ParseException ex) {
                // Do nothing
                return str;
            }
        }
    }

    public class Relation {

        int arColumnId = -1;
        int erColumnId = -1;
        String arColName = "";
        String arColValue = "";
        String erColName = "";
        String erColValue = "";
    }
}
