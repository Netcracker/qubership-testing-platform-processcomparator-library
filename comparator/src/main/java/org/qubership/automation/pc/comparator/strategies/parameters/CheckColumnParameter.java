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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.qubership.automation.pc.comparator.enums.ComparatorParameters;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.models.table.CheckColumnRule;

public class CheckColumnParameter implements ParameterData {

    private ComparatorParameters comparatorParameters;

    public CheckColumnParameter(ComparatorParameters comparatorParameters) {
        this.comparatorParameters = comparatorParameters;
    }

    @Override
    public List<CheckColumnRule> getParameterData(Parameters parameters) {
        return Optional.ofNullable(parameters.getParameters(comparatorParameters.getParameterName()))
                .orElse(comparatorParameters.getDefaultValue())
                .stream()
                .map(CheckColumnRule::buildObject)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
