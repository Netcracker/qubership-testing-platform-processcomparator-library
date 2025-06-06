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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.qubership.automation.pc.comparator.impl.AbstractComparator;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.Table;
import org.qubership.automation.pc.models.table.CheckColumnRule;
import org.qubership.automation.pc.models.table.ComparableTable;
import org.qubership.automation.pc.models.table.ComparableTableCell;
import org.qubership.automation.pc.models.table.ComparableTableHeader;
import org.qubership.automation.pc.models.table.ComparableTableRow;

import com.google.gson.JsonSyntaxException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableComparatorNew extends AbstractComparator {

    @Override
    public List<DiffMessage> compare(@Nonnull String er, @Nonnull String ar, @Nonnull Parameters configuration)
            throws ComparatorException {
        try {
            List<DiffMessage> diffs = new ArrayList<>();

            ComparableTableRuleSet rules = ComparableTableRuleSet.fromParameters(configuration);

            ComparableTable expectedTable = ComparableTable.fromTable(Table.fromString(er));
            ComparableTable actualTable = ComparableTable.fromTable(Table.fromString(ar));

            if (actualTable != null) {
                diffs.addAll(processCheckColumnRules(actualTable, rules.getColumnChecks()));
            }

            if (expectedTable != null && actualTable != null) {
                diffs.addAll(compareTwoTables(expectedTable, actualTable, rules));
            }
            return diffs;
        } catch (JsonSyntaxException jsonEx) {
            log.error("[PC2] Error when comparing two tables! One of the tables has incorrect format!", jsonEx);
            throw new ComparatorException(jsonEx);
        }
    }

    private List<DiffMessage> processCheckColumnRules(@Nonnull ComparableTable table,
                                                      @Nonnull List<CheckColumnRule> rules) {
        List<DiffMessage> diffs = new ArrayList<>();
        for (CheckColumnRule columnCheck : rules) {
            diffs.addAll(columnCheck.processTable(table));
        }
        return diffs;
    }

    private List<DiffMessage> compareTwoTables(ComparableTable expectedTable, ComparableTable actualTable,
                                               ComparableTableRuleSet rules) {
        List<DiffMessage> diffs = new ArrayList<>();
        ColumnAnalysisResult columnAnalysisResult = analyzeTables(expectedTable, actualTable, rules);

        // Compare two tables (if they're present)
        List<ComparableTableRow> unprocessedErRows = expectedTable.getRows();
        List<ComparableTableRow> unprocessedArRows = actualTable.getRows();

        // Process matched rows
        ListIterator<ComparableTableRow> iterator = unprocessedErRows.listIterator();
        while (iterator.hasNext()) {
            ComparableTableRow currErRow = iterator.next();
            ComparableTableRow arMatchedRow = findMatchedRow(currErRow, unprocessedArRows, columnAnalysisResult);
            if (arMatchedRow != null) {
                // Processed matched columns
                columnAnalysisResult.matchedColumns.forEach(pair -> {
                    ComparableTableCell erCell = currErRow.getCell(pair.er);
                    ComparableTableCell arCell = arMatchedRow.getCell(pair.ar);
                    if (!erCell.getValue().equals(arCell.getValue())) {
                        DiffMessage diff = new DiffMessage()
                                .setResult(ResultType.MODIFIED)
                                .setDescription("Cells have different values")
                                .setExpected(String.format("%s-%s", erCell.getRow(), erCell.getColumn()))
                                .setExpectedValue(erCell.getValue())
                                .setActual(String.format("%s-%s", arCell.getRow(), arCell.getColumn()))
                                .setActualValue(arCell.getValue());
                        diffs.add(diff);
                    }
                });
                // Process extra columns
                diffs.addAll(processMismatchedColumns(currErRow, columnAnalysisResult.mismatchedExpected, false));
                diffs.addAll(processMismatchedColumns(arMatchedRow, columnAnalysisResult.mismatchedActual, true));
                // Remove rows for unprocessed
                iterator.remove();
                unprocessedArRows.remove(arMatchedRow);
            }
        }
        // Processed unmatched rows
        diffs.addAll(processUnmatchedRow(unprocessedErRows, false));
        diffs.addAll(processUnmatchedRow(unprocessedArRows, true));
        return diffs;
    }

    private ComparableTableRow findMatchedRow(ComparableTableRow erRow, List<ComparableTableRow> arRows,
                                              ColumnAnalysisResult analysis) {
        if (!analysis.primaryColumns.isEmpty()) {
            List<String> erValues = findPrimaryValues(erRow, analysis.primaryColumns, false);
            return arRows.stream().filter(arRow -> erValues.equals(
                    findPrimaryValues(arRow, analysis.primaryColumns, true)
            ))
                    .findFirst()
                    .orElse(null);
        } else {
            return arRows.stream()
                    .filter(arRow -> arRow.getIndex().equals(erRow.getIndex()))
                    .findFirst().orElse(null);
        }
    }

    private List<String> findPrimaryValues(ComparableTableRow row, Set<ColumnMatch> matches, boolean isAr) {
        return matches.stream()
                .map(match -> isAr ? row.getCell(match.ar).getValue() : row.getCell(match.er).getValue())
                .collect(Collectors.toList());
    }

    private ColumnAnalysisResult analyzeTables(ComparableTable erTable, ComparableTable arTable,
                                               ComparableTableRuleSet rules) {
        ColumnAnalysisResult result = new ColumnAnalysisResult();
        List<ComparableTableHeader> unmatchedEr = erTable.getHeaders();
        List<ComparableTableHeader> unmatchedAr = arTable.getHeaders();
        Iterator<ComparableTableHeader> iterator = unmatchedEr.listIterator();
        while (iterator.hasNext()) {
            ComparableTableHeader currErHeader = iterator.next();
            ComparableTableHeader arHeader = unmatchedAr.stream().filter(header ->
                    currErHeader.getName().equals(header.getName())
            ).findFirst().orElse(null);
            if (arHeader != null) {
                ColumnMatch match = new ColumnMatch(currErHeader.getIndex(), arHeader.getIndex());
                result.matchedColumns.add(match);
                if (rules.getPrimaryKeys().contains(currErHeader.getName())) {
                    result.primaryColumns.add(match);
                }
                iterator.remove();
                unmatchedAr.remove(arHeader);
            }
        }
        result.mismatchedExpected
                = unmatchedEr.stream().map(erHeader -> erHeader.getIndex()).collect(Collectors.toSet());
        result.mismatchedActual
                = unmatchedAr.stream().map(arHeader -> arHeader.getIndex()).collect(Collectors.toSet());
        return result;
    }


    private List<DiffMessage> processMismatchedColumns(ComparableTableRow row, Set<Integer> mismatchedColumns,
                                                       boolean isAr) {
        List<DiffMessage> diffs = new ArrayList<>();
        mismatchedColumns.forEach(column -> {
            ComparableTableCell cell = row.getCell(column);
            DiffMessage diff = new DiffMessage()
                    .setResult(isAr ? ResultType.EXTRA : ResultType.MISSED)
                    .setDescription(isAr ? "Column is extra in ar" : "Column is missed in ar")
                    .setExpected(String.format("%s-%s", cell.getRow(), cell.getColumn()));
            diffs.add(diff);
        });
        return diffs;
    }

    private List<DiffMessage> processUnmatchedRow(List<ComparableTableRow> unmatchedRows, boolean isAr) {
        List<DiffMessage> diffs = new ArrayList<>();
        unmatchedRows.forEach(row -> {
            row.getCells().forEach(cell -> {
                DiffMessage diff = new DiffMessage()
                        .setResult(isAr ? ResultType.EXTRA : ResultType.MISSED)
                        .setDescription(isAr ? "Row is extra in ar" : "Row is missed in ar")
                        .setExpected(String.format("%s-%s", cell.getRow(), cell.getColumn()));
                diffs.add(diff);
            });
        });
        return diffs;
    }

    public class ColumnAnalysisResult {
        public Set<ColumnMatch> matchedColumns = new HashSet<>();
        public Set<Integer> mismatchedExpected = new HashSet<>();
        public Set<Integer> mismatchedActual = new HashSet<>();
        public Set<ColumnMatch> primaryColumns = new HashSet<>();
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    public class ColumnMatch {
        public int er;
        public int ar;
    }
}
