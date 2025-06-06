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

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;


public class XsdComparatorTest {

    XsdComparator comparator = new XsdComparator();

    private final String VALID_VALUE = "<addresses>\n" +
            "  <address><name>Alex</name><street>Longman ave.</street></address>\n" +
            "  <address><name>Peter</name></address>\n" +
            "  <address><street>White house st.</street></address>\n" +
            "  <address></address>\n" +
            "</addresses>";
    private final String MODIFIED_VALUE = "<addresses>\n" +
            "  <address><name>Alex</name><street>Longman ave.</street></address>\n" +
            "  <address><name>Peter</name><name>John</name></address>\n" +
            "  <address><street>Cavendish ave.</street><street>White house st.</street></address>\n" +
            "  <address>Empty address</address>\n" +
            "</addresses>";
    private final String XSD_FILE_RULE_VALUE = "\n" +
            "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>\n" +
            " \n" +
            "<xs:element name=\"addresses\">\n" +
            "    <xs:complexType>\n" +
            "        <xs:sequence>\n" +
            "            <xs:element ref=\"address\" minOccurs='1' maxOccurs='unbounded'/>\n" +
            "        </xs:sequence>\n" +
            "    </xs:complexType>\n" +
            "</xs:element>\n" +
            "<xs:element name=\"address\">\n" +
            "    <xs:complexType>\n" +
            "        <xs:sequence>\n" +
            "            <xs:element ref=\"name\" minOccurs='0' maxOccurs='1'/>\n" +
            "            <xs:element ref=\"street\" minOccurs='0' maxOccurs='1'/>\n" +
            "        </xs:sequence>\n" +
            "    </xs:complexType>\n" +
            "</xs:element>\n" +
            "<xs:element name=\"name\" type='xs:string'/>\n" +
            "<xs:element name=\"street\" type='xs:string'/>\n" +
            "</xs:schema>";

    /**
     * For this test we expect exeption to be thrown because xsdFile rule hasn't been set.
     */
    @Test
    public void noXSDFileRuleSetThrowsException() throws ComparatorException {
        Assertions.assertThrows(ComparatorException.class,
                () -> comparator.compare(VALID_VALUE, MODIFIED_VALUE, new Parameters()),
                "Expect exception to be thrown because xsdFile rule hasn't been set");
    }

    /**
     * For this test we expect exactly 3 diffs (all for AR which is invalid for schema).
     */
    @Test
    public void validErAndModifiedArReturnCorrectDiffs() throws ComparatorException {
        List<DiffMessage> diffs = comparator.compare(VALID_VALUE, MODIFIED_VALUE, getParamsWithXSDFileRule());
        assertEquals(3, diffs.size());
        // Checking ER diffs count
        assertEquals(0, diffs.stream().filter(diff -> StringUtils.isNotBlank(diff.getExpected())).count());
        // Checking AR diffs count
        assertEquals(3, diffs.stream().filter(diff -> StringUtils.isNotBlank(diff.getActual())).count());
    }

    /**
     * For this test we expect exactly 0 diffs. AR is valid, ER is not, but skipped.
     */
    @Test
    public void skipERRuleNoValidationsForEr() throws ComparatorException {
        Parameters params = getParamsWithXSDFileRule();
        params.put(XsdComparator.SKIP_ER_VALIDATION, "true");
        List<DiffMessage> diffs = comparator.compare(MODIFIED_VALUE, VALID_VALUE, params);
        assertEquals(0, diffs.size());
    }

    /**
     * For this test we expect exactly 3 diffs. skipER rule expected to work as if it was false.
     */
    @Test
    public void incorrectSkipERRuleValueSetToFalse() throws ComparatorException {
        Parameters params = getParamsWithXSDFileRule();
        params.put(XsdComparator.SKIP_ER_VALIDATION, "incorrectValue");
        List<DiffMessage> diffs = comparator.compare(MODIFIED_VALUE, VALID_VALUE, params);
        assertEquals(3, diffs.size());
    }

    /**
     * For this test we expect exactly 1 diff. Error for ER not being valid xml.
     */
    @Test
    public void invalidErReturnsErrorDiff() throws ComparatorException {
        List<DiffMessage> diffs = comparator.compare("", VALID_VALUE, getParamsWithXSDFileRule());
        assertEquals(1, diffs.size());
        assertEquals(ResultType.ERROR, diffs.get(0).getResult());
    }

    private Parameters getParamsWithXSDFileRule() {
        Parameters params = new Parameters();
        params.put(XsdComparator.XSD_FILE, XSD_FILE_RULE_VALUE);
        return params;
    }

}
