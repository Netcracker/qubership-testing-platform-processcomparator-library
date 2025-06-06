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

package org.qubership.automation.pc.comparator.enums;

import java.util.ArrayList;
import java.util.HashMap;

import org.qubership.automation.pc.comparator.strategies.parameters.ParametersFactory;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.models.ChangeDiffResultRuleOld;

public enum JsonComparatorParameters implements ComparatorParameters {

    //boolean, def = false+
    PARAMETER_IGNORE_ARRAY_ELEMENTS_ORDER("ignoreArraysOrder", false),
    //List<String>, convert to  Map<String, String>, def = new HashMap<>()+
    PARAMETER_OBJECT_PRIMARY_KEY("objectPrimaryKey", new HashMap<String, String>()),
    //List<String>, convert to  Map<String, String>, def = new HashMap<>()+
    PARAMETER_OBJECT_PRIMARY_KEY_V2("objectPrimaryKeyV2", new HashMap<String, String>()),
    //List<String>, def = new ArrayList<>()+
    PARAMETER_READ_BY_PATH("readByPath", new ArrayList<String>()),
    //List<String>, def = new ArrayList<>()+
    PARAMETER_IGNORE_PROPERTIES("ignoreProperties", new ArrayList<String>()),
    //List<String>, def = new ArrayList<>()+
    PARAMETER_IGNORE_VALUE("ignoreValue", new ArrayList<String>()),
    //List<String>, def = new ArrayList<>()+
    PARAMETER_MANDATORY_ATTRIBUTE("mandatoryAttribute", new ArrayList<String>()),
    //List<String>, def = new ArrayList<>()+
    PARAMETER_IGNORE_PROPERTIES_V2("ignorePropertiesV2", new ArrayList<String>()),
    //List<String>, convert to String with separator '\n', def=""+
    PARAMETER_CHECK_ARRAY("checkArray", new ArrayList<String>()),
    //List<String>, def = new ArrayList<>()+
    PARAMETER_VALIDATE_SCHEMA("validateSchema", ""),
    //List<String>, def = false+
    PARAMETER_VALIDATE_AS_SIMPLE_SCHEMA("validateAsSimpleSchema", false),
    //String, def = "{SUMMARY}"+
    PARAMETER_DIFF_SUMMARY_TEMPLATE("diffSummaryTemplate", "{SUMMARY}"),
    //boolean, def = true+
    PARAMETER_DISABLE_TYPE_CHECK_IF_REGEXP("disableTypeCheckIfRegexp", true),
    //boolean, def = false+
    PARAMETER_FIND_ER_IN_AR("findERInAR", false),
    //boolean, def = false+
    PARAMETER_IGNORE_EXTRA("ignoreExtra", false),
    //List<String> to List<ChangeDiffResultRule>, def = new ArrayList<ChangeDiffResultRule>()
    PARAMETER_CHANGE_DIFF_RESULT("changeDiffResultJson", new ArrayList<ChangeDiffResultRuleOld>()),
    PARAMETER_KEYS_CASE_INSENSITIVE("keysCaseInsensitive", false),
    PARAMETER_SAVE_DIFF_VALUE("saveDiffValue", false);

    private String parameterName;
    private Object defaultValue;

    JsonComparatorParameters(String name, Object defaultValue) {
        this.parameterName = name;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public <T> T getDefaultValue() {
        return (T) defaultValue;
    }

    @Override
    public <T> T getValue(Parameters parameters) {
        return (T) new ParametersFactory()
                .getParameterDataValidation(this)
                .getParameterData(parameters);
    }

}
