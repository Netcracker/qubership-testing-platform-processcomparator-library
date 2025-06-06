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

package org.qubership.automation.pc.rest;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * RESTful endpoint for handling comparison requests.
 *
 * <p>
 * This resource class exposes an HTTP API under the path <code>/compare</code> and delegates
 * comparison logic to the internal comparator service implementation.
 * </p>
 *
 * <p>It supports HTTP <code>PUT</code> requests and produces JSON responses containing comparison results.</p>
 *
 * <p>Designed to be used in environments that support JAX-RS (e.g., Jersey, RESTEasy, etc.).</p>
 */
@Path("/compare")
public class ComparatorResource {

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public String compare(String context) {
        org.qubership.automation.pc.comparator.api.ComparatorResource comparatorResource
                = new org.qubership.automation.pc.comparator.api.ComparatorResource();

        return comparatorResource.compare(context);
    }
}
