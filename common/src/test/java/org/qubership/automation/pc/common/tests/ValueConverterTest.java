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

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.converters.Converter;
import org.qubership.automation.pc.core.exceptions.ValueConverterException;
import org.qubership.automation.pc.core.interfaces.IValueConverterValue;

public class ValueConverterTest {

    @Test
    public void testCsvValueConverter() {
        String inputString = "\"CMO\"|\"33D551\"|\"Wohnung\"|\"E\"|\"VH\"|\"10\"|\"9\"|\"R\"|\"EG\"|\"13\"|\"O\"|\"1a\"|\"FlatInfo2\"|\"hg\"|\"DEU.DTAG.CC17\"|\"585FFFD126254B\"|\"AKTIV\"|\"123\"|\"2011-11-11T11:11:11\"|\"156899301229\" ";
        //String inputString = "a|b|c";
        //String inputString = "\"a\"|\"b\"|\"c\"";
        String actionString = "$CONVERT(\"CsvConverter\",\"delimeter=|\",\"firstRowIsColumns=false\")";
        try {
            IValueConverterValue value = Converter.exec(inputString, actionString);
        } catch (ValueConverterException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testCsvValueConverter2() {
        String inputString =
                "\"02\",\"202311\",\"35555\",\"128\",\"01\",\"9920\",\"3\",\"3\",\"1000\",\"\",\"0\",\"0\",\"20231110\",\"1111111111222222222233333328\",\"00\",\"01\",\"01\"\n"
                + "\"02\",\"202311\",\"35555\",\"128\",\"01\",\"9920\",\"3\",\"3\",\"1000\",\"\",\"0\",\"0\",\"20231110\",\"1111111111222222222233333328\",\"00\",\"01\",\"01\"\n"
                + "\"02\",\"202411\",\"35555\",\"128\",\"01\",\"9920\",\"3\",\"3\",\"1000\",\"\",\"0\",\"0\",\"20231110\",\"1111111111222222222233333328\",\"00\",\"01\",\"01\"\n"
                + "\"02\",\"202511\",\"35555\",\"128\",\"01\",\"9920\",\"3\",\"3\",\"1000\",\"\",\"0\",\"0\",\"20231110\",\"1111111111222222222233333328\",\"00\",\"01\",\"01\"\n"
                + "\"09\",\"00000001\"\n";
        String actionString = "$CONVERT(\"CsvConverter\",\"delimeter=,\",\"firstRowIsColumns=false\")";
        try {
            IValueConverterValue value = Converter.exec(inputString, actionString);
        } catch (ValueConverterException ex) {
            fail(ex.getMessage());
        }
    }
}
