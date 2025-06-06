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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;

import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.models.HighlighterResult;

public class BuildColoredCsvTest {

    @Test
    public void csvComparator_givenCsvWithDifference_canHighlightAsTable() throws ComparatorException {
        String er = "\"1.1\",\"1.2\",\"1.3\"\n"
                + "\"2.1\",\"2.2\",\"2.3\"\n"
                + "\"3.1\",\"3.2\",\"3.3\"";
        String ar = "\"1.1\",\"1.2\",\"1.3\"\n"
                + "\"2.1\",\"2-2\",\"2.3\"\n"
                + "\"3.1\",\"3.2\",\"3.3\"";
        Map<String, List<String>> parameters = new HashMap<>();
        DiffMessage diff = new DiffMessage(1, "/CSV/0/1/1.2", "/CSV/0/1/1.2", ResultType.MODIFIED);
        String expectedEr = "<table border=1 cellpadding=2 cellspacing=2>"
                + "<tr><th>1.1</th><th>1.2</th><th>1.3</th></tr>"
                + "<tr><td>2.1</td><td class=\"MODIFIED\">2.2</td><td>2.3</td></tr></table>";
        String expectedAr = "<table border=1 cellpadding=2 cellspacing=2>"
                + "<tr><th>1.1</th><th>1.2</th><th>1.3</th></tr>"
                + "<tr><td>2.1</td><td class=\"MODIFIED\">2-2</td><td>2.3</td></tr></table>";
        String expectedErOld = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<h3>CSV</h3><table border=1 cellpadding=2 cellspacing=2>"
                + "<tr><th>1.1</th><th>1.2</th><th>1.3</th></tr>"
                + "<tr><td>2.1</td><td class=\"MODIFIED\">2.2</td><td>2.3</td></tr></table>";
        String expectedArOld = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<h3>CSV</h3><table border=1 cellpadding=2 cellspacing=2>"
                + "<tr><th>1.1</th><th>1.2</th><th>1.3</th></tr>"
                + "<tr><td>2.1</td><td class=\"MODIFIED\">2-2</td><td>2.3</td></tr></table>";

        HighlighterResult result = BuildColoredCsv.highlight(Collections.singletonList(diff), er, ar, parameters);

        assertEquals(expectedEr, result.getEr().getValue());
        assertEquals(expectedAr, result.getAr().getValue());

    }
}
