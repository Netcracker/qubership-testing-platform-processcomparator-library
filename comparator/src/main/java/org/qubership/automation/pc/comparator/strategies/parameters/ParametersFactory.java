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

package org.qubership.automation.pc.comparator.strategies.parameters;

import org.qubership.automation.pc.comparator.enums.JsonComparatorParameters;
import org.qubership.automation.pc.comparator.enums.TableComparatorParameters;

public class ParametersFactory {

    public ParameterData getParameterDataValidation(JsonComparatorParameters jsonComparatorParameters) {
        ParameterData parameterData;
        switch (jsonComparatorParameters) {
            case PARAMETER_FIND_ER_IN_AR:
            case PARAMETER_DISABLE_TYPE_CHECK_IF_REGEXP:
            case PARAMETER_IGNORE_ARRAY_ELEMENTS_ORDER:
            case PARAMETER_IGNORE_EXTRA:
            case PARAMETER_VALIDATE_AS_SIMPLE_SCHEMA:
            case PARAMETER_KEYS_CASE_INSENSITIVE:
            case PARAMETER_SAVE_DIFF_VALUE:
                parameterData = new BooleanParameter(jsonComparatorParameters);
                break;

            case PARAMETER_OBJECT_PRIMARY_KEY:
                parameterData = new ObjectPrimaryKeyParameter(jsonComparatorParameters);
                break;
            case PARAMETER_OBJECT_PRIMARY_KEY_V2:
                parameterData = new ObjectPrimaryKeyV2Parameter(jsonComparatorParameters);
                break;
            case PARAMETER_IGNORE_PROPERTIES:
            case PARAMETER_IGNORE_VALUE:
            case PARAMETER_CHECK_ARRAY:
            case PARAMETER_MANDATORY_ATTRIBUTE:
            case PARAMETER_IGNORE_PROPERTIES_V2:
            case PARAMETER_READ_BY_PATH:
                parameterData = new ListParameter(jsonComparatorParameters);
                break;

            case PARAMETER_VALIDATE_SCHEMA:
                parameterData = new ValidateSchemaParameter(jsonComparatorParameters);
                break;

            case PARAMETER_DIFF_SUMMARY_TEMPLATE:
                parameterData = new StringParameter(jsonComparatorParameters);
                break;

            case PARAMETER_CHANGE_DIFF_RESULT:
                parameterData = new ChangeDiffResultParameter(jsonComparatorParameters);
                break;
            default:
                throw new IllegalArgumentException("Wrong Template name: " + jsonComparatorParameters);
        }

        return parameterData;
    }

    public ParameterData getParameterDataValidation(TableComparatorParameters tableComparatorParameters) {
        ParameterData parameterData;
        switch (tableComparatorParameters) {
            case PARAMETER_PRIMARY_KEY:
                parameterData = new ListParameter(tableComparatorParameters);
                break;
            case PARAMETER_CHECK_COLUMN:
                parameterData = new CheckColumnParameter(tableComparatorParameters);
                break;
            default:
                throw new IllegalArgumentException("Wrong Template name: " + tableComparatorParameters);
        }
        return parameterData;
    }

}
