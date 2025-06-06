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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration.InputParameter;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for processing and substituting macros (placeholders) in text scripts.
 * </p>
 * This class supports identifying macros (e.g., placeholders like <code>{param}</code>)
 * within SQL or other text-based scripts, and replacing them with values provided either via
 * a list of {@link InputParameter} or a map of parameter names and values.
 * </p>
 * It supports features such as:
 * <ul>
 *   <li>Default values for macros</li>
 *   <li>Optional ID-based matching</li>
 *   <li>Customizable regular expressions for macro detection</li>
 *   <li>Wildcard-based matching for parameterized script mappings</li>
 * </ul>
 * </p>
 * Designed for integration in systems requiring dynamic script parameterization, such as SQL query builders.
 * </p>
 * This class is stateless and only exposes static utility methods.
 */
public class ScriptUtils {

    private static final Pattern defaultInputParametersPattern
            = Pattern.compile("\\{([a-zA-Z0-9_\\.\\x20]+)\\}"); // Old variant: Pattern.compile("\\{(.*?)\\}");
    private static final Logger log = LoggerFactory.getLogger(ScriptUtils.class);

    public static List<String> getMacros(String script) {
        return getMacros(script, defaultInputParametersPattern);
    }

    public static List<String> getMacros(String script, String regex) {
        return getMacros(script, (StringUtils.isBlank(regex)) ? defaultInputParametersPattern : Pattern.compile(regex));
    }

    public static List<String> getMacros(String script, Pattern regexPattern) {
        List<String> listNames = new ArrayList<>();
        Matcher matcher = regexPattern.matcher(script);
        while (matcher.find()) {
            String fullParameterName = matcher.group(1).trim();
            if (!fullParameterName.isEmpty()) {
                listNames.add(fullParameterName);
            }
        }
        return listNames;
    }

    public static String prepareParameterizedScript(String sqlScript,
                                                    List<InputParameter> listInputs) throws ReaderException {
        return prepareParameterizedScript(sqlScript, listInputs, true, false,
                defaultInputParametersPattern);
    }

    public static String prepareParameterizedScript(
            String sqlScript,
            List<InputParameter> listInputs,
            boolean enableDefaultValues,
            boolean extraIdChecking) throws ReaderException {
        return prepareParameterizedScript(sqlScript, listInputs, enableDefaultValues, extraIdChecking,
                defaultInputParametersPattern);
    }

    public static String prepareParameterizedScript(
            String sqlScript,
            List<InputParameter> listInputs,
            boolean enableDefaultValues,
            boolean extraIdChecking,
            String regex) throws ReaderException {
        return prepareParameterizedScript(sqlScript, listInputs, enableDefaultValues, extraIdChecking,
                (StringUtils.isBlank(regex)) ? defaultInputParametersPattern : Pattern.compile(regex));
    }

    public static String prepareParameterizedScript(
            String sqlScript,
            List<InputParameter> listInputs,
            boolean enableDefaultValues,
            boolean extraIdChecking,
            Pattern regexPattern) throws ReaderException {
        //Parse macroses in Script
        Matcher matcher = regexPattern.matcher(sqlScript);
        while (matcher.find()) {
            String fullParameterName = matcher.group(1);

            MacroDescription macroDescription = new MacroDescription(fullParameterName, enableDefaultValues,
                    extraIdChecking);

            //Prepare Input Parameters and replace it
            String parameterValue = null;
            boolean parameterFound = false;
            for (InputParameter inputParameter : listInputs /*configuration.getInputParameters()*/) {
                if (inputParameter.name.equals(macroDescription.name)) {
                    if (extraIdChecking && !StringUtils.isBlank(macroDescription.id)) {
                        if (inputParameter.id.equals(macroDescription.id)) {
                            parameterValue = inputParameter.value;
                            parameterFound = true;
                            break;
                        }
                    } else {
                        parameterValue = inputParameter.value;
                        parameterFound = true;
                        break;
                    }
                }
            }
            if (parameterValue == null) {
                continue;
            }
            parameterValue = checkExceptions(parameterValue, fullParameterName, parameterFound, enableDefaultValues,
                    macroDescription.defaultValue);

            sqlScript = sqlScript.replace(matcher.group(0), parameterValue);
        }
        return sqlScript;
    }

    public static String prepareParameterizedScript(String sqlScript,
                                                    Map<String, String> listInputs) throws ReaderException {
        return prepareParameterizedScript(sqlScript, listInputs, true, false,
                defaultInputParametersPattern);
    }

    public static String prepareParameterizedScript(
            String sqlScript,
            Map<String, String> listInputs,
            boolean enableDefaultValues,
            boolean extraIdChecking) throws ReaderException {
        return prepareParameterizedScript(sqlScript, listInputs, enableDefaultValues, extraIdChecking,
                defaultInputParametersPattern);
    }

    public static String prepareParameterizedScript(
            String sqlScript,
            Map<String, String> listInputs,
            boolean enableDefaultValues,
            boolean extraIdChecking,
            String regex) throws ReaderException {
        return prepareParameterizedScript(sqlScript, listInputs, enableDefaultValues, extraIdChecking,
                (StringUtils.isBlank(regex)) ? defaultInputParametersPattern : Pattern.compile(regex));
    }

    public static String prepareParameterizedScript(
            String sqlScript,
            Map<String, String> listInputs,
            boolean enableDefaultValues,
            boolean extraIdChecking,
            Pattern regexPattern) throws ReaderException {
        //Parse macroses in Script
        log.debug("[prepareParameterizedScript] matcher");
        Matcher matcher = regexPattern.matcher(sqlScript);
        while (matcher.find()) {
            log.debug("[prepareParameterizedScript] while");
            String fullParameterName = matcher.group(1);

            MacroDescription macroDescription = new MacroDescription(fullParameterName, enableDefaultValues,
                    false); // extraIdChecking - we can't implement this checking in case
            // when Map<String,String> listInputs is supplied

            //Prepare Input Parameters and replace it
            String parameterValue = null;
            boolean parameterFound = false;
            if (listInputs.containsKey(macroDescription.name)) {
                parameterValue = listInputs.get(macroDescription.name);
                parameterFound = true;
            }

            if (parameterValue == null) {
                continue;
            }
            log.debug("[prepareParameterizedScript] checkExceptions");
            parameterValue = checkExceptions(parameterValue, fullParameterName, parameterFound, enableDefaultValues,
                    macroDescription.defaultValue);
            log.debug("[prepareParameterizedScript] replace");
            sqlScript = sqlScript.replace(matcher.group(0), parameterValue);
        }
        return sqlScript;
    }

    private static String checkExceptions(String parameterValue, String fullParameterName, boolean parameterFound,
                                          boolean enableDefaultValues, String defaultValue) throws ReaderException {
        if (!parameterFound) {
            // If parameter isn't found:
            //  If default value is set - we use it (even in case when it's empty)
            //  Otherwise: throw an exception
            //      This behaviour is discussed and agreed with Stas Lunev - 2017/05/10
            if (enableDefaultValues && defaultValue != null) {
                return defaultValue;
            } else {
                throw new ReaderException(ResponseMessages.msg(20207, fullParameterName));
            }
        } else {
            // Parameter is found; now we check if parameter's value is empty
            //  If default value is set - we use it (even in case when it's empty)
            //  Otherwise: throw an exception
            //      This behaviour is discussed and agreed with Stas Lunev - 2017/05/10
            if (parameterValue.isEmpty()) {
                if (enableDefaultValues && defaultValue != null) {
                    return defaultValue;
                } else {
                    throw new ReaderException(ResponseMessages.msg(20208, fullParameterName));
                }
            }
        }
        return parameterValue;
    }
    
    public static String fieldTypesContainsStepName(Map<String, Map<String, String>> fieldTypes,
                                                    String stepNameForFind) {
        for (Map.Entry<String, Map<String, String>> entry : fieldTypes.entrySet()) {
            String stepName = entry.getKey();
            if (stepName.length() > 1) {
                if (stepName.startsWith("*") && stepName.endsWith("*")) {
                    stepName = stepName.replaceAll("\\*", "");
                    if (stepNameForFind.contains(stepName)) {
                        return entry.getKey();
                    }
                } else if (stepName.startsWith("*")) {
                    stepName = stepName.replaceAll("\\*", "");
                    if (stepNameForFind.endsWith(stepName)) {
                        return entry.getKey();
                    }
                } else if (stepName.endsWith("*")) {
                    stepName = stepName.replaceAll("\\*", "");
                    if (stepNameForFind.startsWith(stepName)) {
                        return entry.getKey();
                    }
                }
            }
        }
        return null;
    }

    private static class MacroDescription {

        public String name;
        public String id;
        public String defaultValue;

        public MacroDescription() {
            this.name = "";
            this.id = "";
            this.defaultValue = "";
        }

        public MacroDescription(String str, boolean enableDefaultValues, boolean extraIdChecking) {
            String fullName = str;
            if (enableDefaultValues) {
                int idx = str.indexOf("=");
                if (idx == -1) { // Default values are enabled BUT
                    // this placeholder (str) doesn't contain default value.
                    this.defaultValue = "";
                } else {
                    this.defaultValue = str.substring(idx + 1);
                    if (idx == 0) { // Empty name!? Well; all input parameters in BV are named and names are not empty.
                        // That's why it will be an exception later while replacing
                        this.name = "";
                        this.id = null;
                        return;
                    } else {
                        fullName = str.substring(0, idx);
                    }
                }
            } else {
                this.defaultValue = "";
            }
            if (extraIdChecking) {
                int idx = fullName.indexOf(".");
                if (idx == -1) { // ID checking is enabled BUT this placeholder doesn't match "name.id" pattern. 
                    this.name = fullName;
                    this.id = null;
                } else {
                    this.id = fullName.substring(idx + 1);
                    if (idx == 0) { // Empty name!? Well; all input parameters in BV are named and names are not empty.
                        // That's why it will be an exception later while replacing
                        this.name = "";
                    } else {
                        this.name = fullName.substring(0, idx);
                    }
                }
            } else {
                this.name = fullName;
                this.id = null;
            }
        }
    }
}
