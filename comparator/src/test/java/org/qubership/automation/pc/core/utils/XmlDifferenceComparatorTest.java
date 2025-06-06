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

package org.qubership.automation.pc.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;

public class XmlDifferenceComparatorTest {

    @Test
    public void xmlDiffsComparator_givenDifferences_canCompare() {
        List<DiffMessage> sortedDiffs = Arrays.asList(
            generateDiff("root/"),
            generateDiff("root/level[0]"),
            generateDiff("parent:root/text[2]/@attr"),
            generateDiff("root/text[15]/node"),
            generateDiff("root/text[15]/node[2]"),
            generateDiff("root/text[15]/node1"),
            generateDiff("root/text[15]/node1/add")
        );

        Comparator<DiffMessage> diffsComparator = new XmlDifferenceComparator();

        //compare each pair of differences in sorted list
        for (int i=0; i< sortedDiffs.size(); i++) {
            for (int j=0; j<sortedDiffs.size(); j++) {
                int res = diffsComparator.compare(sortedDiffs.get(i), sortedDiffs.get(j));
                if (i==j) {
                    assertEquals(0, res, "Expected 0 because differences are identical");
                } else if (i>j) {
                    assertTrue(res >= 0, "Expected positive or zero because i>j so diff[i]>=diff[j]");
                } else if (i>j) {
                    assertTrue(res <= 0, "Expected negative or zero because i<j so diff[i]<=diff[j]");
                }
            }
        }
    }

    private static DiffMessage generateDiff(String expected) {
        return new DiffMessage(1, expected, "", ResultType.MODIFIED);
    }
}
