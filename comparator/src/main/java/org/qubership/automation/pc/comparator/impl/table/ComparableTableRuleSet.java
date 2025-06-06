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

package org.qubership.automation.pc.comparator.impl.table;

import static org.qubership.automation.pc.comparator.enums.TableComparatorParameters.PARAMETER_CHECK_COLUMN;
import static org.qubership.automation.pc.comparator.enums.TableComparatorParameters.PARAMETER_PRIMARY_KEY;

import java.util.List;

import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.models.table.CheckColumnRule;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ComparableTableRuleSet {

    private List<String> primaryKeys;
    private List<CheckColumnRule> columnChecks;

    public static ComparableTableRuleSet fromParameters(Parameters parameters) {
        return new ComparableTableRuleSet(
                PARAMETER_PRIMARY_KEY.getValue(parameters),
                PARAMETER_CHECK_COLUMN.getValue(parameters)
        );
    }
}
