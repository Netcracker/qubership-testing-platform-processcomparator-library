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

package org.qubership.automation.pc.configuration.parameters;


/**
 * Represents a single configuration parameter with a name and a value.
 *
 * <p>This class is used to encapsulate key-value pairs typically used in various
 * configuration contexts such as comparator rules, global settings, or step-level overrides.</p>
 *
 * Instances of this class are typically collected into a list or map and
 * processed to influence the behavior of comparison logic.
 */
public class Parameter {
    public String name;
    public String value;

    public String getValue() {
        return this.value;
    }
}
