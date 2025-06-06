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
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.comparator.impl.table.FatTableComparator;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class FatTableComparatorTest extends AbstractComparatorTest {

    @Test
    public void compare_onlyAr_checkColumnsRule_equalsOperation_oneDiff() throws ComparatorException, IOException {
        String ar = getStringFromFile("src/test/resources/fat_table/checkColumn_equals/ar.json");
        Parameters parameters = new Parameters();
        parameters.put("checkColumn", "Status=EQUALS=Active,Activation Pending");

        FatTableComparator comparator = new FatTableComparator();

        List<DiffMessage> diffs = comparator.compare(null, ar, parameters);

        Assertions.assertEquals(1, diffs.size());
    }

    @Test
    public void compare_onlyAr_checkColumnsRule_notEqualsOperation_twoDiffs() throws ComparatorException, IOException {
        String ar = getStringFromFile("src/test/resources/fat_table/checkColumn_notEquals/ar.json");
        Parameters parameters = new Parameters();
        parameters.put("checkColumn", "Status=NOT_EQUALS=Active,Activation Pending");

        FatTableComparator comparator = new FatTableComparator();

        List<DiffMessage> diffs = comparator.compare(null, ar, parameters);

        Assertions.assertEquals(2, diffs.size());
    }
}
