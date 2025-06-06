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

import java.util.List;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.qubership.automation.pc.comparator.HighlighterManager;
import org.qubership.automation.pc.models.HighlighterResult;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

/**
 * RESTful endpoint for processing highlighting operations.
 *
 * <p>
 * This JAX-RS resource is mapped to the path <code>/highlighter</code> and is responsible for
 * receiving input data for highlighting via HTTP <code>PUT</code> requests.
 * </p>
 *
 * <p>The resource parses JSON input, delegates highlighting logic to {@link HighlighterManager},
 * and returns the result in JSON format.</p>
 *
 * <p>Intended for use in services that support JSON-based comparison highlighting.</p>
 */
@Path("/highlighter")
public class HighlighterResource {

    private Gson gson = new Gson();

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public String highlight(String context) {

        JsonArray arrayContext = new JsonParser().parse(context).getAsJsonArray();
        List<HighlighterResult> resultList = new HighlighterManager().highlight(arrayContext);

        return gson.toJson(resultList);
    }
}
