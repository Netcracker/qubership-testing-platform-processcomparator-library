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

import org.qubership.automation.pc.comparator.strategies.parameters.ParametersFactory;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.models.table.CheckColumnRule;

public enum TableComparatorParameters implements ComparatorParameters {

    //List<String>, def = new ArrayList<>()+
    PARAMETER_PRIMARY_KEY("primaryKey", new ArrayList<>()),
    //List<String> to List<CheckColumnRule>, def = new ArrayList<CheckColumnRule>()+
    PARAMETER_CHECK_COLUMN("checkColumn", new ArrayList<CheckColumnRule>());

    private String parameterName;
    private Object defaultValue;

    TableComparatorParameters(String name, Object defaultValue) {
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
