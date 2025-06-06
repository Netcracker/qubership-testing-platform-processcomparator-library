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

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.comparator.impl.table.TableComparatorNew;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class TableComparatorNewTest extends AbstractComparatorTest {

    private TableComparatorNew comparator;

    @BeforeEach
    public void setUp() throws Exception {
        comparator = new TableComparatorNew();
    }

    // GENERAL

    @Test
    public void compare_identicalTables_shuffledColumns_noDiffs() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/identical_but_shuffled/er.json");
        String ar = getStringFromFile("src/test/resources/table/identical_but_shuffled/ar.json");

        List<DiffMessage> diffs = comparator.compare(er, ar, new Parameters());

        Assertions.assertEquals(0, diffs.size());
    }

    @Test
    public void compare_haveCellsWithDifferences_twoModifiedDiffsExpected() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/simple_differences/er.json");
        String ar = getStringFromFile("src/test/resources/table/simple_differences/ar.json");

        List<DiffMessage> diffs = comparator.compare(er, ar, new Parameters());

        long modifiedDiffsCount = diffs.stream().filter(diff -> diff.getResult() == ResultType.MODIFIED).count();
        Assertions.assertEquals(2, diffs.size());
        Assertions.assertEquals(2, modifiedDiffsCount);
    }

    @Test
    public void compare_extraColumnInEr_twoMissedDiffsExpected() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_column_in_er/er.json");
        String ar = getStringFromFile("src/test/resources/table/extra_column_in_er/ar.json");

        List<DiffMessage> diffs = comparator.compare(er, ar, new Parameters());

        long missedDiffsCount = diffs.stream().filter(diff -> diff.getResult() == ResultType.MISSED).count();
        Assertions.assertEquals(2, diffs.size());
        Assertions.assertEquals(2, missedDiffsCount);
    }

    @Test
    public void compare_extraColumnInAr_twoExtraDiffsExpected() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_column_in_ar/er.json");
        String ar = getStringFromFile("src/test/resources/table/extra_column_in_ar/ar.json");

        List<DiffMessage> diffs = comparator.compare(er, ar, new Parameters());

        long extraDiffsCount = diffs.stream().filter(diff -> diff.getResult() == ResultType.EXTRA).count();
        Assertions.assertEquals(2, diffs.size());
        Assertions.assertEquals(2, extraDiffsCount);
    }

    @Test
    public void compare_extraRowInAr_sevenExtraDiffsExpected() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_rows_in_ar/er.json");
        String ar = getStringFromFile("src/test/resources/table/extra_rows_in_ar/ar.json");

        List<DiffMessage> diffs = comparator.compare(er, ar, new Parameters());

        long extraDiffsCount = diffs.stream().filter(diff -> diff.getResult() == ResultType.EXTRA).count();
        Assertions.assertEquals(7, diffs.size());
        Assertions.assertEquals(7, extraDiffsCount);
    }

    @Test
    public void compare_extraRowInEr_twoMissedDiffsExpected() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/extra_rows_in_er/er.json");
        String ar = getStringFromFile("src/test/resources/table/extra_rows_in_er/ar.json");

        List<DiffMessage> diffs = comparator.compare(er, ar, new Parameters());

        long missedDiffsCount = diffs.stream().filter(diff -> diff.getResult() == ResultType.MISSED).count();
        Assertions.assertEquals(7, diffs.size());
        Assertions.assertEquals(7, missedDiffsCount);
    }

    // WITH RULES

    @Test
    public void compare_shuffledRows_singlePrimaryKey_noDiffs() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/primary_key_single/er.json");
        String ar = getStringFromFile("src/test/resources/table/primary_key_single/ar.json");
        Parameters parameters = new Parameters();
        parameters.put("primaryKey", "PK");

        List<DiffMessage> diffs = comparator.compare(er, ar, parameters);

        Assertions.assertEquals(0, diffs.size());
    }

    @Test
    public void compare_shuffledRows_multiplePrimaryKey_noDiffs() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/primary_key_multiple/er.json");
        String ar = getStringFromFile("src/test/resources/table/primary_key_multiple/ar.json");
        Parameters parameters = new Parameters();
        parameters.put("primaryKey", "PK");
        parameters.put("primaryKey", "FK");

        List<DiffMessage> diffs = comparator.compare(er, ar, parameters);

        Assertions.assertEquals(0, diffs.size());
    }

    @Test
    public void compare_shuffledRows_multiplePrimaryKey_notAllPksExistInTables_noDiffs() throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/table/primary_key_not_all_present/er.json");
        String ar = getStringFromFile("src/test/resources/table/primary_key_not_all_present/ar.json");
        Parameters parameters = new Parameters();
        parameters.put("primaryKey", "PK");
        parameters.put("primaryKey", "ClearlyNonExistent");

        List<DiffMessage> diffs = comparator.compare(er, ar, parameters);

        Assertions.assertEquals(0, diffs.size());
    }

    @Test
    public void compare_shuffledRows_singleCheckColumn_equals_oneDiff() throws IOException, ComparatorException {
        String ar = getStringFromFile("src/test/resources/table/check_columns_single_equals/er.json");
        Parameters parameters = new Parameters();
        parameters.put("checkColumn", "Status=EQUALS=PASSED");

        List<DiffMessage> diffs = comparator.compare("", ar, parameters);

        Assertions.assertEquals(1, diffs.size());
    }

    @Test
    public void compare_shuffledRows_singleCheckColumn_notEquals_oneDiff() throws IOException, ComparatorException {
        String ar = getStringFromFile("src/test/resources/table/check_columns_single_not_equals/er.json");
        Parameters parameters = new Parameters();
        parameters.put("checkColumn", "Status=NOT_EQUALS=FAILED");

        List<DiffMessage> diffs = comparator.compare("", ar, parameters);

        Assertions.assertEquals(1, diffs.size());
    }

    @Test
    public void compare_shuffledRows_multipleCheckColumn_oneDiff() throws IOException, ComparatorException {
        String ar = getStringFromFile("src/test/resources/table/check_columns_multiple/er.json");
        Parameters parameters = new Parameters();
        parameters.put("checkColumn", "Status=NOT_EQUALS=FAILED");
        parameters.put("checkColumn", "State=EQUALS=FINISHED");

        List<DiffMessage> diffs = comparator.compare("", ar, parameters);

        Assertions.assertEquals(1, diffs.size());
    }

    // SPECIAL
    @Test
    public void compare_erIsEmptyString_noDiffs() throws IOException, ComparatorException {
        String ar = getStringFromFile("src/test/resources/table/test_table.json");

        List<DiffMessage> diffs = comparator.compare("", ar, new Parameters());

        Assertions.assertEquals(0, diffs.size());
    }

    @Test
    public void compare_wrongFormat_comparatorException() throws IOException, ComparatorException {
        String ar = getStringFromFile("src/test/resources/table/test_table_broken.json");

        Assertions.assertThrows(ComparatorException.class,
                () -> comparator.compare("", ar, new Parameters()));
    }
}
