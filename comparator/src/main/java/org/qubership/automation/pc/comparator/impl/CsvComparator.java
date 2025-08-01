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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.converters.CsvConverter;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.exceptions.ValueConverterException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsvComparator extends AbstractComparator {

    public static final String CSV_DELIMITER_RULE = "delimiter";
    public static final String CSV_FIRST_ROW_IS_COLUMNS_RULE = "firstRowIsColumns";
    private final CsvConverter csvConverter = new CsvConverter();

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        Map<String, String> csvParserConfiguration = getCsvParserRules(configuration);
        String tableEr;
        String tableAr;
        try {
            tableEr = csvConverter.process(er, csvParserConfiguration).getValue();
            tableAr = csvConverter.process(ar, csvParserConfiguration).getValue();
        } catch (ValueConverterException e) {
            String message = "Error occurred while parsing input csv. Message: " + e.getMessage();
            log.error(message);
            throw new ComparatorException(message, e);
        }

        TableComparator tableComparator = new TableComparator();
        return tableComparator.compare(tableEr, tableAr, configuration);
    }

    public static Map<String, String> getCsvParserRules(Parameters configuration) {
        Map<String, String> params = new HashMap<>();
        if (Objects.nonNull(configuration)) {
            params.put(CsvConverter.PROP_DELIMETER, configuration.getParameter(CSV_DELIMITER_RULE, ","));
            params.put(CsvConverter.PROP_FIRST_ROW_IS_COLUMNS,
                    configuration.getParameter(CSV_FIRST_ROW_IS_COLUMNS_RULE, "true"));
        }
        return params;
    }
}
