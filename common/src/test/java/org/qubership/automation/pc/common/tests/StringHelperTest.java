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

package org.qubership.automation.pc.common.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.core.helpers.StringHelper;

public class StringHelperTest {

    @Test
    public void correctMask_appliedSuccesfully() {
        String fileName = "abc_def_123_456";
        String mask = "abc_(.*)_123_(.*)";
        String expectedFileName = "abc_XXX_123_XXX";
        assertEquals(StringHelper.maskName(fileName, mask), expectedFileName);
    }

    @Test
    public void maskWithNoGroups_fileNameNotChanged() {
        String fileName = "abc_def_123_456";
        String mask = "abc_def*";
        assertEquals(StringHelper.maskName(fileName, mask), fileName);
    }

    @Test
    public void viasatExample_twoExtensions_maskApplied_oneExtensionCut() {
        String fileName = "ATPOLA365123_2_1_6_20200305.xml.processed";
        String mask = "ATPOLA365123.*?_(.*).xml.processed";
        String expectedFileName = "ATPOLA365123_XXXXXXXXXXXXXX.xml";
        assertEquals(expectedFileName, FilenameUtils.getBaseName(StringHelper.maskName(fileName, mask)));
    }

    @Test
    public void trimToLength_stringShorterThanLen_returnFullString() {
        String str = "string";

        String result = StringHelper.trimToLength(str, 6);

        assertEquals(str, result);
    }

    @Test
    public void trimToLength_stringLongerThanLen_trimStrToLenAndAddCountOfTrimmedChars() {
        String str = "string";
        String expectedStr = "str...[+3]";

        String result = StringHelper.trimToLength(str, 3);

        assertEquals(expectedStr, result);
    }
}
