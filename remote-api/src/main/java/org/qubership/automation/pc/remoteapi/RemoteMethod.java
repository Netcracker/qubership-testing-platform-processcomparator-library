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
 * The {@code RemoteMethod} enum defines supported HTTP methods
 * used for making remote API calls.
 *
 * <p>These methods correspond to standard HTTP verbs and are used
 * in the {@link RemoteApi} class to configure how data is sent over HTTP.</p>
 *
 * <ul>
 *   <li>{@link #POST} - Used to submit data to be processed to a specified resource.</li>
 *   <li>{@link #GET} - Used to retrieve data from a specified resource.</li>
 *   <li>{@link #PUT} - Used to update a specified resource with data.</li>
 * </ul>
 *
 * @see RemoteApi
 */
public enum RemoteMethod {
    POST,
    GET,
    PUT
}
