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

import java.util.List;
import java.util.Map;

import org.qubership.automation.pc.comparator.impl.CsvComparator;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.converters.CsvConverter;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.exceptions.ValueConverterException;
import org.qubership.automation.pc.models.HighlighterResult;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuildColoredCsv {

    public static HighlighterResult highlight(List<DiffMessage> differences, String er, String ar,
                                              Map<String, List<String>> rules) throws ComparatorException {
        CsvConverter csvConverter = new CsvConverter();
        Map<String, String> csvParserConfiguration = CsvComparator.getCsvParserRules(new Parameters(rules));
        String tableEr;
        String tableAr;
        try {
            tableEr = csvConverter.process(er, csvParserConfiguration).getValue();
            tableAr = csvConverter.process(ar, csvParserConfiguration).getValue();
        } catch (ValueConverterException e) {
            String message = "Error occurred while parsing input csv for highlighting. Message: " + e.getMessage();
            log.error(message);
            throw new ComparatorException(message, e);
        }
        return BuildColoredTable.highlight(differences, tableEr, tableAr);
    }

}
