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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.qubership.automation.pc.comparator.impl.table.CheckPocRule;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.HighlighterNode;
import org.qubership.automation.pc.models.HighlighterResult;
import org.qubership.automation.pc.models.Table;
import org.qubership.automation.pc.models.TableDiffCoords;
import org.qubership.automation.pc.models.TablesList;
import org.qubership.automation.pc.models.table.differences.DifferencesCell;
import org.qubership.automation.pc.models.table.differences.DifferencesRow;
import org.qubership.automation.pc.models.table.differences.DifferencesTable;

import com.google.common.base.Strings;

/**
 * Utility class responsible for generating HTML representations of differences between two table-based data sources.
 *
 * <p>
 * This class takes lists of differences and corresponding expected (ER) and actual (AR) JSON documents representing
 * tables, processes them into intermediate table structures,
 * and outputs them as colored HTML tables for visual comparison.
 *
 * <p>
 * Supports optional filtering by specific POC rules and provides a combined table view for side-by-side comparison.
 *
 * <p>
 * Typical use case includes visualizing mismatches in tabular data during data validation or automated comparisons.
 * </p>
 *
 * <p>This class is stateless and only contains static methods.</p>
 */
public class BuildColoredTable {

    private static final String COMMENT_HEADER = "Comments";
    public static final String PARAMETER_NAME_CHECK_POC = "checkPOC";

    public static HighlighterResult highlight(List<DiffMessage> differences, String er, String ar)
            throws ComparatorException {
        return highlight(differences, er, ar, new HashMap<>());
    }

    public static HighlighterResult highlight(List<DiffMessage> differences, String er, String ar,
                                              Map<String, List<String>> rules) throws ComparatorException {
        List<DifferencesTable> erColoredTables = processDifferences(differences, er, false);
        List<DifferencesTable> arColoredTables = processDifferences(differences, ar, true);

        List<String> checkPocRows = rules.get(PARAMETER_NAME_CHECK_POC);
        if (Objects.nonNull(checkPocRows) && !checkPocRows.isEmpty()) {
            CheckPocRule checkPocRule = new CheckPocRule(checkPocRows);
            filterTableColumnsByCheckPocRule(erColoredTables, checkPocRule, false);
            filterTableColumnsByCheckPocRule(arColoredTables, checkPocRule, true);
        }

        String erHtml = convertDifferenceTables(erColoredTables);
        String arHtml = convertDifferenceTables(arColoredTables);

        HighlighterNode erNode = new HighlighterNode();
        HighlighterNode arNode = new HighlighterNode();
        erNode.setValue(erHtml);
        erNode.setIsPlain(true);
        arNode.setValue(arHtml);
        arNode.setIsPlain(true);
        HighlighterResult resultMap = new HighlighterResult();
        resultMap.setEr(erNode);
        resultMap.setAr(arNode);

        String combinedHtml = differenceTablesToCombinedHtml(erColoredTables, arColoredTables);
        HighlighterNode combinedNode = new HighlighterNode();
        combinedNode.setValue(combinedHtml);
        combinedNode.setIsPlain(true);
        resultMap.setCombined(combinedNode);
        return resultMap;
    }

    private static List<DifferencesTable> processDifferences(List<DiffMessage> differences,
                                                             String docEar,
                                                             Boolean isActual) throws ComparatorException {
        if (docEar.isEmpty()) {
            return Collections.singletonList(new DifferencesTable());
        }
        try {
            TablesList earTables = TablesList.getTableListFromJson(docEar);
            if (earTables == null || earTables.isEmpty()) {
                throw new ComparatorException(((isActual) ? "AR" : "ER")
                        + " table is missed! Please check source configuration and/or input files.");
            }
            List<DifferencesTable> diffTables = new ArrayList<>();
            Table curTable = null;
            DifferencesTable coloredTable = null;
            int prevRowId = -1;
            String currentCheck = null;
            for (DiffMessage diff : differences) {
                TableDiffCoords coords = getTableDiffCoords(diff, isActual);
                if (coords == null || coords.getTableName().isEmpty() || coords.getRowId() == -1) {
                    continue;
                }
                if (ResultType.IDENTICAL.equals(diff.getResult())) {
                    continue;
                }
                // Get table by name
                if (curTable == null || !curTable.name.equals(coords.getTableName())) {
                    coloredTable = null;
                    prevRowId = -1;
                    curTable = getTableByName(coords.getTableName(), earTables);
                    if (curTable != null) {
                        coloredTable = new DifferencesTable();
                        coloredTable.setName(curTable.name);
                        coloredTable.getHeaders().addAll(curTable.headers);
                        diffTables.add(coloredTable);
                    }
                }
                // Processing of the difference itself...
                // If coloredTable == null it means that er/ar and diffs do not correspond each other
                if (curTable == null || coloredTable == null) {
                    continue;
                }
                boolean isCheckChanged = !Strings.isNullOrEmpty(coords.getCheckName())
                        && !coords.getCheckName().equals(currentCheck);
                if (isCheckChanged && isActual) {
                    coloredTable.addDelimiterRow(coords.getCheckName());
                    currentCheck = coords.getCheckName();
                }
                // check if rowId equals previous
                if (coords.getRowId() == prevRowId && Strings.isNullOrEmpty(coords.getReportMessage())) {
                    // It means attribute value difference
                    if (coords.getColId() != -1) {
                        coloredTable.getLastRow().getCells().get(coords.getColId()).setResult(ResultType.MODIFIED);
                    }
                } else {
                    DifferencesRow newRow = new DifferencesRow(new LinkedList<>(), false, null, null);
                    newRow.getCells().addAll(curTable.rows.get(coords.getRowId())
                            .stream()
                            .map(cellValue -> new DifferencesCell(cellValue, null))
                            .collect(Collectors.toList()));
                    if (coords.getColId() == -1) {
                        newRow.setResult(coords.getResult());
                    } else {
                        newRow.getCells().get(coords.getColId()).setResult(ResultType.MODIFIED);
                    }
                    if (!Strings.isNullOrEmpty(coords.getReportMessage())) {
                        newRow.setComment(coords.getReportMessage());
                    }
                    coloredTable.getRows().add(newRow);
                }
                prevRowId = coords.getRowId();
            }
            return diffTables;
        } catch (Exception e) {
            throw new ComparatorException("Error while highlighting " + (isActual ? "AR" : "ER"), e);
        }
    }

    private static TableDiffCoords getTableDiffCoords(DiffMessage difference, boolean isAr) {
        if (isAr && !difference.getActual().isEmpty()) {
            return new TableDiffCoords(difference.getActual(), difference.getResult());
        } else if (!isAr && !difference.getExpected().isEmpty()) {
            return new TableDiffCoords(difference.getExpected(), difference.getResult());
        }
        return null;
    }

    private static Table getTableByName(String tname, TablesList earTables) {
        for (Table earTable : earTables) {
            if (tname.equals(earTable.name)) {
                return earTable;
            }
        }
        return null;
    }

    private static String differenceTablesToCombinedHtml(List<DifferencesTable> erColoredTables,
                                                         List<DifferencesTable> arColoredTables) {
        StringBuilder tables = new StringBuilder();

        /*
            The 1st limited implementation:
                - Combined tables are produced only in case tables count is the same.
                - And, it's assumed that the corresponding tables are in the same places in lists.
                - More complex implementation (using table names and checking extra/missed table names) is postponed.
         */
        if (erColoredTables.size() == arColoredTables.size()) {
            int size = erColoredTables.size();
            for (int i = 0; i < size; i++) {
                int columnsCount = Math.max(
                        erColoredTables.get(i).getHeaders().size(),
                        arColoredTables.get(i).getHeaders().size());
                tables.append(printTable(erColoredTables.get(i), size, true, true, true, false));
                tables.append("<tr bgcolor=\"lightyellow\"><td colspan=\"")
                        .append(columnsCount)
                        .append("\"><b>ar</b></td></tr>");
                tables.append(printTable(arColoredTables.get(i), size, false, false, false, true));
            }
        } else {
            tables.append("er/ar tables counts are different, combined highlighting of tables isn't implemented yet.");
        }
        return tables.toString();
    }

    private static String convertDifferenceTables(List<DifferencesTable> coloredTables) {
        StringBuilder tables = new StringBuilder();
        coloredTables.forEach(coloredTable -> tables.append(
                printTable(coloredTable, coloredTables.size(), true, true, true, true)
        ));
        return tables.toString();
    }

    private static String printTable(DifferencesTable coloredTable,
                                     int tablesCount,
                                     boolean printTableNameHeader,
                                     boolean printStartTable,
                                     boolean printHeaders,
                                     boolean printEndTable) {
        if (coloredTable == null || coloredTable.getRows().isEmpty()) {
            return "";
        } else {
            boolean hasComments = coloredTable.hasComments();
            int columnCount = coloredTable.getHeaders().size() + (hasComments ? 1 : 0);
            StringBuilder result = new StringBuilder(
                    tablesCount > 1 && printTableNameHeader
                            ? "<h3>" + coloredTable.getName() + "</h3>"
                            : ""
            );
            if (printStartTable) {
                result.append("<table border=1 cellpadding=2 cellspacing=2>");
            }
            if (printHeaders) {
                result.append(printHeaders(coloredTable.getHeaders(), hasComments));
            }
            for (DifferencesRow row: coloredTable.getRows()) {
                if (row.isDelimiter()) {
                    result.append(printDelimiterRow(row, columnCount));
                } else {
                    result.append(printRegularRow(row, hasComments));
                }
            }
            return result + (printEndTable ? "</table>" : "");
        }
    }

    private static String printHeaders(List<String> headers, boolean hasComments) {
        StringBuilder result = new StringBuilder("<tr>");
        for (String header: headers) {
            result.append("<th>").append(escape(header)).append("</th>");
        }
        if (hasComments) {
            result.append("<th bgcolor=\"lightgrey\"><i>" + COMMENT_HEADER + "</i></th>");
        }
        result.append("</tr>");
        return result.toString();
    }

    private static String printDelimiterRow(DifferencesRow row, int columnCount) {
        return "<tr bgcolor=\"lightgrey\"><td colspan=\"" + columnCount + "\">"
                + escape(row.getComment()) + "</td></tr>";
    }

    private static String printRegularRow(DifferencesRow row, boolean hasComments) {
        StringBuilder result;
        if (Objects.isNull(row.getResult())) {
            result = new StringBuilder("<tr>");
        } else {
            result = new StringBuilder("<tr class=\"" + row.getResult() + "\">");
        }
        for (DifferencesCell cell : row.getCells()) {
            result.append(printCell(cell));
        }
        if (hasComments) {
            result.append("<td bgcolor=\"lightgrey\">")
                    .append(Objects.isNull(row.getComment()) ? "" : escape(row.getComment()))
                    .append("</td>");
        }
        return result + "</tr>";
    }

    private static String printCell(DifferencesCell cell) {
        if (Objects.nonNull(cell.getResult())) {
            return "<td class=\"" + cell.getResult() + "\">" + escape(cell.getValue()) + "</td>";
        } else {
            return "<td>" + escape(cell.getValue()) + "</td>";
        }
    }

    private static String escape(String str) {
        return str.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\\R", "<br>");
    }

    private static void filterTableColumnsByCheckPocRule(List<DifferencesTable> tables,
                                                         CheckPocRule rule,
                                                         boolean isActualResult) {
        List<String> displayColumns = rule.getCheckPocSections().stream()
                .flatMap(section -> (isActualResult
                        ? section.displayColumnAr.stream() : section.displayColumnEr.stream()))
                .collect(Collectors.toList());
        if (!displayColumns.isEmpty()) {
            for (DifferencesTable table : tables) {
                table.filterTable(displayColumns);
            }
        }
    }
}
