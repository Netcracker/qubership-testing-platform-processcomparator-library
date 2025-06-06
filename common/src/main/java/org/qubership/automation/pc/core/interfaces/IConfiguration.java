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

package org.qubership.automation.pc.core.interfaces;

import java.util.List;

/**
 * Interface for accessing configuration parameters in a flexible and type-safe manner.
 * </p>
 * Provides methods to check for the presence of parameters and retrieve their values
 * as strings, integers, longs, or booleans, with optional default values.
 */
public interface IConfiguration {
    Boolean has(String parameterName);

    List<String> getParameters(String parameterName);

    String getParameter(String parameterName);

    String getParameter(String parameterName,String defaultValue);

    Integer getIntegerParameter(String parameterName);

    Integer getIntegerParameter(String parameterName,Integer defaultValue);

    Long getLongParameter(String parameterName);

    Long getLongParameter(String parameterName,Long defaultValue);

    Boolean getBooleanParameter(String parameterName);

    Boolean getBooleanParameter(String parameterName, Boolean defaultValue);
}
