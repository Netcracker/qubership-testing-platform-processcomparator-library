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

package org.qubership.automation.pc.converters;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.core.exceptions.ValueConverterException;
import org.qubership.automation.pc.core.interfaces.IValueConverter;
import org.qubership.automation.pc.core.interfaces.IValueConverterValue;

/**
 * Central class for executing value converters by name and parameters.
 * </p>
 * Maintains a registry of available converter implementations and provides methods
 * to parse action strings, instantiate the corresponding converter, and invoke its logic.
 * Designed as a singleton-style utility with internal caching and dynamic class loading.
 */
public class Converter {

    Map<String, String> registeredConverters = new HashMap<>();

    private static Converter _instance;

    private static Converter getInstance() {
        if (_instance == null) {
            _instance = new Converter();
        }
        return _instance;
    }

    public Converter() {
        this.registeredConverters
                .put("CsvConverter", "org.qubership.automation.pc.converters.CsvConverter");
        this.registeredConverters
                .put("CsvConverterLegacy", "org.qubership.automation.pc.converters.CsvConverterLegacy");
    }

    public static IValueConverterValue exec(String inputString, String actionString) throws ValueConverterException {
        return getInstance().convert(inputString, actionString);
    }

    public IValueConverterValue convert(String inputString, String actionString) throws ValueConverterException {
        String actionRegexp = "(.*?)\\(\\\"(.*?)\\\"\\)";
        Pattern regexpPattern = Pattern.compile(actionRegexp);
        Matcher m = regexpPattern.matcher(actionString);
        if (m.matches()) {
            String functionName = m.group(1);
            String parameters = m.group(2);
            if (!StringUtils.isBlank(parameters)) {
                String[] actionParameters = parameters.split("\",\"");
                if (actionParameters.length > 0) {
                    String converterName = actionParameters[0];
                    Map<String, String> converterParameters = new HashMap<>();
                    if (actionParameters.length > 1) {
                        for (int i = 1; i < actionParameters.length; i++) {
                            String[] splittedActionParameter = actionParameters[i].split("=");
                            String actionParameterValue
                                    = splittedActionParameter.length == 2 ? splittedActionParameter[1] : null;
                            converterParameters.put(splittedActionParameter[0], actionParameterValue);
                        }
                    }
                    return this.executeConverter(inputString, converterName, converterParameters);
                }
            }
        }
        throw new ValueConverterException("Failed to parse action parameters");
    }

    public IValueConverterValue executeConverter(String inputString,
                                                 String converterName,
                                                 Map<String, String> converterProperties)
            throws ValueConverterException {
        if (!this.registeredConverters.containsKey(converterName)) {
            throw new ValueConverterException("Converter not found");
        }
        try {
            Class<?> converterClass = Class.forName(this.registeredConverters.get(converterName));
            IValueConverter converter = (IValueConverter) converterClass.newInstance();
            return converter.process(inputString, converterProperties);
        } catch (ClassNotFoundException ex) {
            throw new ValueConverterException("Converter not found");
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new ValueConverterException("Failed to initialize converter");
        }
    }

}
