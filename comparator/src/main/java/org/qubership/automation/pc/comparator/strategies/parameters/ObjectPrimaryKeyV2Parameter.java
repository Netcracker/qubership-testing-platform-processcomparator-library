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

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.comparator.enums.JsonComparatorParameters;
import org.qubership.automation.pc.configuration.parameters.Parameters;

public class ObjectPrimaryKeyV2Parameter implements ParameterData {

    private JsonComparatorParameters jsonComparatorParameters;

    public ObjectPrimaryKeyV2Parameter(JsonComparatorParameters jsonComparatorParameters) {
        this.jsonComparatorParameters = jsonComparatorParameters;
    }

    @Override
    public Map<String, String> getParameterData(Parameters parameters) {
        return Optional.ofNullable(parameters.getParameters(jsonComparatorParameters.getParameterName()))
                .orElse(Collections.emptyList())
                .stream()
                .filter(StringUtils::isNoneEmpty)
                .filter(str -> StringUtils.containsAny(str, "/", "[", "]")
                        && !StringUtils.substringBefore(str, "[").isEmpty()
                        && !StringUtils.substringBetween(str, "[", "]").isEmpty())
                .collect(Collectors.toMap(str -> StringUtils.substringBefore(str, "["),
                        str -> StringUtils.substringBetween(str, "[", "]"), (path1, path2) -> path2));
    }
}
