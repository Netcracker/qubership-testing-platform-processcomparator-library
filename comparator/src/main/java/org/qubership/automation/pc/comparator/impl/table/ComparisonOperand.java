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

public enum ComparisonOperand {
    EQUAL("="),
    NOTEQUAL("<>"),
    LESS_OR_EQUAL("<="),
    MORE_OR_EQUAL(">="),
    LESS("<"),
    MORE(">"),
    LIKE("like"),
    UNLIKE("unlike");

    private final String symbols;

    ComparisonOperand(String name) {
        this.symbols = name;
    }

    public String getSymbols() {
        return symbols;
    }

    public static ComparisonOperand fromSymbols(String symbols) {
        for (ComparisonOperand operand: ComparisonOperand.values()) {
            if (operand.symbols.equals(symbols)) {
                return operand;
            }
        }
        throw new IllegalArgumentException("Comparison operand '" + symbols + "' not found");
    }
}
