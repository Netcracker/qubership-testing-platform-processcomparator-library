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
 * RESTful resource for handling reader-related operations.
 *
 * <p>This class exposes endpoints for reading data and testing data source connections.
 * It acts as a proxy to the internal API implementation found in
 * {@code org.qubership.automation.pc.reader.api.ReaderResource}.</p>
 *
 * <p>All responses are serialized to JSON format.</p>
 *
 * @see org.qubership.automation.pc.reader.api.ReaderResource
 */
@Path("/read")
@Produces(MediaType.APPLICATION_JSON)
public class ReaderResource {

    @PUT
    public String read(String context) {
        org.qubership.automation.pc.reader.api.ReaderResource readerResource
                = new org.qubership.automation.pc.reader.api.ReaderResource();

        return readerResource.read(context);
    }

    @PUT
    @Path("/testConnection")
    public String testConnection(String context) {
        org.qubership.automation.pc.reader.api.ReaderResource readerResource
                = new org.qubership.automation.pc.reader.api.ReaderResource();
        return readerResource.testConnection(context);
    }
}
