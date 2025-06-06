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

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class TaskListComparatorTest extends AbstractComparatorTest{

    private TaskListComparator comparator;

    @BeforeEach
    public void setUp() {comparator = new TaskListComparator();}

    @Test
    public void taskListComparator_givenStringsIncorrectOrder_returnBrokenStepIndexDiff()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/task_list/incorrect_order/er.txt");
        String ar = getStringFromFile("src/test/resources/task_list/incorrect_order/ar.txt");
        Parameters params = new Parameters();

        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        Assertions.assertEquals(1, diffs.size(), "Comparison result should contain 1 difference");
        Assertions.assertEquals(ResultType.BROKEN_STEP_INDEX, diffs.get(0).getResult(),
                "difference should be of type broken step index");
        Assertions.assertEquals("1-1", diffs.get(0).getExpected(), "Incorrect order should be in 2nd row of ER");
        Assertions.assertEquals("0-0", diffs.get(0).getActual(), "Incorrect order should be in 1st row of AR");
    }

    @Test
    public void taskListComparator_givenDifferentStringsInErArWithEqualSize_returnModifiedDiff()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/task_list/equal_size_different_strings/er.txt");
        String ar = getStringFromFile("src/test/resources/task_list/equal_size_different_strings/ar.txt");
        Parameters params = new Parameters();

        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        Assertions.assertEquals(2, diffs.size(), "Comparison result should contain 2 differences");
        Assertions.assertEquals(ResultType.MODIFIED, diffs.get(0).getResult(),
                "Diff 0 should be modified, because 3d row in ER is missed and ER is the same size as AR");
        Assertions.assertEquals(ResultType.MODIFIED, diffs.get(1).getResult(),
                "Diff 1 should be modified, because 3d row in AR is missed and ER is the same size as AR");
    }

    @Test
    public void taskListComparator_givenDifferentStringsInErArWithDifferentSize_returnMissedAndExtra()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/task_list/different_size/er.txt");
        String ar = getStringFromFile("src/test/resources/task_list/different_size/ar.txt");
        Parameters params = new Parameters();

        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        Assertions.assertEquals(3, diffs.size(), "Comparison result should contain 3 differences");
        Assertions.assertEquals(ResultType.MISSED, diffs.get(0).getResult(),
                "Diff 0 should be missed, because 4th row in ER is missed and sizes of ER and AR are different");
        Assertions.assertEquals(ResultType.EXTRA, diffs.get(1).getResult(),
                "Diff 1 should be extra, because 5th row in AR is extra and sizes of ER and AR are different");
        Assertions.assertEquals(ResultType.EXTRA, diffs.get(2).getResult(),
                "Diff 2 should be extra, because 6th row in AR is extra and sizes of ER and AR are different");
    }

    @Test
    public void taskListComparator_givenErAsRegexp_returnModified()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/task_list/taskAsRegexp_rule/er.txt");
        String ar = getStringFromFile("src/test/resources/task_list/taskAsRegexp_rule/ar.txt");
        Parameters params = new Parameters();
        params.put("TaskAsRegexp", "true");

        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        Assertions.assertEquals(3, diffs.size(), "Comparison result should contain 3 differences");
        Assertions.assertEquals(ResultType.MODIFIED, diffs.get(0).getResult(),
                "Diff 0 should be modified, because 2nd row in ER is missed and ER is the same size as AR");
        Assertions.assertEquals(ResultType.MODIFIED, diffs.get(1).getResult(),
                "Diff 1 should be modified, because 1st row in AR is extra and ER is the same size as AR");
        Assertions.assertEquals(ResultType.BROKEN_STEP_INDEX, diffs.get(2).getResult(),
                "Diff 2 should be broken_step_index, because 1st and 3d rows in ER are in different order in AR");
    }

    @Test
    public void taskListComparator_givenTaskAsRegexp_givenMultipleMatch_example1_returnIdentical()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/task_list/multipleMatch_rule/example1_er.txt");
        String ar = getStringFromFile("src/test/resources/task_list/multipleMatch_rule/example1_ar.txt");
        Parameters params = new Parameters();
        params.put("TaskAsRegexp", "true");
        params.put("multipleMatch", "true");

        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        Assertions.assertEquals(0, diffs.size(), "Comparison result should contain no difference");
    }

    @Test
    public void taskListComparator_givenTaskAsRegexp_givenMultipleMatch_example2_returnIdentical()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/task_list/multipleMatch_rule/example2_er.txt");
        String ar = getStringFromFile("src/test/resources/task_list/multipleMatch_rule/example2_ar.txt");
        Parameters params = new Parameters();
        params.put("TaskAsRegexp", "true");
        params.put("multipleMatch", "true");

        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        Assertions.assertEquals(0, diffs.size(), "Comparison result should contain no difference");
    }

    @Test
    public void taskListComparator_givenNoTaskAsRegexp_givenMultipleMatch_example3_returnIdentical()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/task_list/multipleMatch_rule/example3_er.txt");
        String ar = getStringFromFile("src/test/resources/task_list/multipleMatch_rule/example3_ar.txt");
        Parameters params = new Parameters();
        params.put("multipleMatch", "true");

        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        Assertions.assertEquals(0, diffs.size(), "Comparison result should contain no difference");
    }

    @Test
    public void taskListComparator_givenTaskAsRegexp_givenMultipleMatch_example4_returnModified()
            throws IOException, ComparatorException {
        String er = getStringFromFile("src/test/resources/task_list/multipleMatch_rule/example4_er.txt");
        String ar = getStringFromFile("src/test/resources/task_list/multipleMatch_rule/example4_ar.txt");
        Parameters params = new Parameters();
        params.put("TaskAsRegexp", "true");
        params.put("multipleMatch", "true");

        List<DiffMessage> diffs = comparator.compare(er, ar, params);

        Assertions.assertEquals(2, diffs.size(), "Comparison result should contain 2 difference");
        Assertions.assertEquals(ResultType.MODIFIED, diffs.get(0).getResult(),
                "Diff 0 type should be MODIFIED");
        Assertions.assertEquals("11-11", diffs.get(0).getActual(),
                "Diff 0: AR task# 11 doesn't match any of ER. (enumeration starts from 0)");
        Assertions.assertEquals(ResultType.MODIFIED, diffs.get(1).getResult(),
                "Diff 1 type should be MODIFIED");
        Assertions.assertEquals("12-12", diffs.get(1).getActual(),
                "Diff 1: AR task# 12 doesn't match any of ER. (enumeration starts from 0)");
    }

}
