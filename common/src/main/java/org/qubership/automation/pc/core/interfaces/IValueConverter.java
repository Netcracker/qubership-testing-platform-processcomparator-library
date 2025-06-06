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

import java.util.Map;

import org.qubership.automation.pc.core.exceptions.ValueConverterException;

/**
 * Interface for value conversion operations.
 * </p>
 * Implementations of this interface are responsible for converting an input string value
 * into a structured result ({@link IValueConverterValue}), possibly using additional parameters.
 *
 * <p><strong>Typical Use Case:</strong>
 * Transformation of raw input values (e.g., from test data or configuration files)
 * into formatted or typed representations suitable for further processing.</p>
 */
public interface IValueConverter {
    IValueConverterValue process(String inputValue,Map<String,String> parameters) throws ValueConverterException;
}
