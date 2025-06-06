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

package org.qubership.automation.pc.core.enums;

/**
 * Enumeration that defines sorting strategies for version control system (VCS) files.
 *
 * <p>
 * These values can be used to specify how files should be ordered when retrieved from
 * a VCS source, either by name or modification date, in ascending or descending order.
 * </p>
 */
public enum VCSSortBy {
    NAME_ASC, NAME_DESC, DATE_ASC, DATE_DESC
}
