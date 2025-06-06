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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.qubership.automation.pc.comparator.impl.AbstractComparator;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.table.CheckColumnRule;
import org.qubership.automation.pc.models.table.FatTable;

import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FatTableComparator extends AbstractComparator {

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        try {
            List<DiffMessage> diffs = new ArrayList<>();

            ComparableTableRuleSet rules = ComparableTableRuleSet.fromParameters(configuration);

            FatTable expectedTable = FatTable.fromString(er);
            FatTable actualTable = FatTable.fromString(ar);

            if (actualTable != null) {
                diffs.addAll(processCheckColumnRules(actualTable, rules.getColumnChecks()));
            }
            if (actualTable != null && expectedTable != null) {
                diffs.addAll(compareTwoTables(expectedTable, actualTable, rules));
            }
            return diffs;
        } catch (
                JsonSyntaxException jsonEx) {
            log.error("[PC2] Error when comparing two tables! One of the tables has incorrect format!", jsonEx);
            throw new ComparatorException(jsonEx);
        }
    }

    private List<DiffMessage> processCheckColumnRules(@Nonnull FatTable table,
                                                      @Nonnull List<CheckColumnRule> rules) {
        List<DiffMessage> diffs = new ArrayList<>();
        for (CheckColumnRule columnCheck : rules) {
            diffs.addAll(columnCheck.processTable(table));
        }
        return diffs;
    }

    private List<DiffMessage> compareTwoTables(FatTable expectedTable, FatTable actualTable,
                                               ComparableTableRuleSet rules) {
        List<DiffMessage> diffs = new ArrayList<>();
        List<Map<String, String>> unprocessedArRows = actualTable.getRows();
        List<Integer> erRowsWithoutPair = new ArrayList<>();
        List<String> primaryKeys = rules.getPrimaryKeys();
        Set<String> combinedHeaders = new HashSet<>();
        combinedHeaders.addAll(expectedTable.getHeaders());
        combinedHeaders.addAll(actualTable.getHeaders());
        boolean searchByKeyColumns = !primaryKeys.isEmpty();
        for (int erIdx = 0; erIdx < expectedTable.getRows().size(); erIdx++) {
            Map<String, String> erRow = expectedTable.getRow(erIdx);
            Map<String, String> arRow = null;
            Integer arFoundIdx = null;
            if (searchByKeyColumns) {
                List<String> erRowValues = new ArrayList<>(primaryKeys.size());
                primaryKeys.forEach(key -> erRowValues.add(erRow.get(key)));
                for (int arIdx = 0; arIdx < unprocessedArRows.size(); arIdx++) {
                    Map<String, String> currAr = unprocessedArRows.get(arIdx);
                    if (currAr != null) {
                        List<String> arRowValues = new ArrayList<>(primaryKeys.size());
                        primaryKeys.forEach(key -> arRowValues.add(currAr.get(key)));
                        if (erRowValues.equals(arRowValues)) {
                            arRow = currAr;
                            unprocessedArRows.set(arIdx, null);
                            arFoundIdx = arIdx;
                            break;
                        }
                    }
                }
            } else {
                try {
                    arRow = unprocessedArRows.get(erIdx);
                    unprocessedArRows.set(erIdx, null);
                    arFoundIdx = erIdx;
                } catch (IndexOutOfBoundsException ex) {
                    // Do nothing
                }
            }
            if (arRow != null) {
                for (String header : combinedHeaders) {
                    String erValue = erRow.get(header);
                    String arValue = arRow.get(header);
                    if (erValue == null) {
                        DiffMessage diff = new DiffMessage();
                        diff.setActual(String.format("%s|%s|%s", actualTable.getName(), arFoundIdx, header));
                        diff.setActualValue(arValue);
                        diff.setDescription("Column is not found in er");
                        diff.setResult(ResultType.EXTRA);
                        diffs.add(diff);
                        continue;
                    }
                    if (arValue == null) {
                        DiffMessage diff = new DiffMessage();
                        diff.setExpected(String.format("%s|%s|%s", expectedTable.getName(), erIdx, header));
                        diff.setExpectedValue(erValue);
                        diff.setDescription("Column is not found in ar");
                        diff.setResult(ResultType.MISSED);
                        diffs.add(diff);
                        continue;
                    }
                    if (!erValue.equals(arValue)) {
                        DiffMessage diff = new DiffMessage();
                        diff.setActual(String.format("%s|%s|%s", actualTable.getName(), arFoundIdx, header));
                        diff.setActualValue(arValue);
                        diff.setExpected(String.format("%s|%s|%s", expectedTable.getName(), erIdx, header));
                        diff.setExpectedValue(erValue);
                        diff.setDescription("Column has different values");
                        diff.setResult(ResultType.MODIFIED);
                        diffs.add(diff);
                    }
                }
            } else {
                erRowsWithoutPair.add(erIdx);
            }
        }
        // PROCESS UNMATCHED ROWS
        for (int i = 0; i < unprocessedArRows.size(); i++) {
            if (unprocessedArRows.get(i) != null) {
                for (String header : combinedHeaders) {
                    DiffMessage diff = new DiffMessage();
                    diff.setActual(String.format("%s|%s|%s", actualTable.getName(), i, header));
                    diff.setDescription("ar has extra row");
                    diff.setResult(ResultType.EXTRA);
                    diffs.add(diff);
                }
            }
        }
        for (Integer unmatchedEr : erRowsWithoutPair) {
            for (String header : combinedHeaders) {
                DiffMessage diff = new DiffMessage();
                diff.setExpected(String.format("%s|%s|%s", expectedTable.getName(), unmatchedEr, header));
                diff.setDescription("er has extra row");
                diff.setResult(ResultType.MISSED);
                diffs.add(diff);
            }
        }
        return diffs;
    }
}
