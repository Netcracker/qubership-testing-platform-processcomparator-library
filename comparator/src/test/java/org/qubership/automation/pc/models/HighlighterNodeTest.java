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

package org.qubership.automation.pc.models;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.helpers.BuildColoredText;

public class HighlighterNodeTest {

    @Test
    public void given_plainText_modified_convertToPlain_encodeModeFalse_makesCorrectHTML_isPlainIsTrue_childrenArrayIsEmpty() {
        String expectedResult = "<div style=\"margin-left: 0px\"><span data-block-id=\"pc-highlight-block\" class=\"MODIFIED\">123\n</span></div>";
        String er = "123";
        String ar = "AAA";
        DiffMessage diff = new DiffMessage(1, "0-0", "0-0", ResultType.MODIFIED);
        HighlighterResult h = BuildColoredText.highlight(Collections.singletonList(diff), er, ar);
        h.getEr().convertToPlain(false);
        Assertions.assertEquals(h.getEr().getValue(), expectedResult);
        Assertions.assertTrue(h.getEr().getIsPlain());
        Assertions.assertTrue(h.getEr().getChildren().isEmpty());
    }
}
