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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

public class CsvComparatorTest {

    private CsvComparator comparator = new CsvComparator();

    @Test
    public void csvComparator_givenDefaultConfig_canCompareAsTable() throws ComparatorException {
        String er = "\"1.1\",\"1.2\",\"1.3\"\n"
                + "\"2.1\",\"2.2\",\"2.3\"\n"
                + "\"3.1\",\"3.2\",\"3.3\"";
        String ar = "\"1.1\",\"1.2\",\"1.3\"\n"
                + "\"2.1\",\"2-2\",\"2.3\"\n"
                + "\"3.1\",\"3.2\",\"3.3\"";
        Parameters parameters = new Parameters();
        List<DiffMessage> diffs = comparator.compare(er, ar, parameters);
        assertEquals(1, diffs.size());
        DiffMessage diff = diffs.get(0);
        assertEquals(ResultType.MODIFIED, diff.getResult());
    }
}
