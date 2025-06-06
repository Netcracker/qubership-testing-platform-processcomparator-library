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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class TextHelpersTest {

    @Test
    public void stringToList_givenStringWithAllSeparatorTypes_canSplitByAllSeparators() {
        String crlf = "TextCRLF";
        String lf = "TextLF";
        String cr = "TextCR";
        String last = "Control line";
        List<String> expected = Arrays.asList(crlf, lf, cr, last);
        String merged = crlf + "\r\n" + lf + "\n" + cr + "\r" + last;

        List<String> splitted = TextHelpers.stringToList(merged);

        assertEquals(splitted, expected);
    }

    @Test
    public void stringToList_givenEmptyString_returnEmptyList() {

        List<String> splitted = TextHelpers.stringToList("");

        assertTrue(splitted.isEmpty());
    }

    @Test
    public void escapeHtmlEntities_givenEntities_canEscape() {
        String original = "&reg;&<\tdiff>&&amp;";
        String expectedResult = "&amp;reg;&amp;&lt;    diff&gt;&amp;&amp;amp;";

        String result = TextHelpers.escapeHtmlEntities(original);

        assertEquals(expectedResult, result);
    }

}
