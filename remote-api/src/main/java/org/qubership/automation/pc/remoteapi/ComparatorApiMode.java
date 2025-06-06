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

package org.qubership.automation.pc.remoteapi;

/**
 * Defines the available modes for accessing the comparator API.
 *
 * <p>
 * This enum is used to distinguish between different API invocation contexts.
 * </p>
 * <ul>
 *     <li>{@link #JSP} – Access via JSP-based interface.</li>
 *     <li>{@link #REST} – Access via RESTful web services.</li>
 * </ul>
 */
public enum ComparatorApiMode {
    JSP,
    REST
}
