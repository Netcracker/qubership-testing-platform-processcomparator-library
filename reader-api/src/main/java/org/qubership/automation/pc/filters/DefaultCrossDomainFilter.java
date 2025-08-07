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

package org.qubership.automation.pc.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * A JAX-RS response filter that adds Cross-Origin Resource Sharing (CORS) headers
 * to HTTP responses to allow cross-domain requests.
 *
 * <p>This filter enables web clients (such as JavaScript running in browsers)
 * to access the API even if it is hosted on a different domain.</p>
 *
 * The filter allows the following:
 * <ul>
 *   <li>Any origin ("*") to access the resource</li>
 *   <li>HTTP methods: OPTIONS, GET, POST, PUT, DELETE</li>
 *   <li>Headers: Content-Type, Accept, Origin, Authorization</li>
 * </ul>
 *
 * <p>To use this filter, simply include it in your JAX-RS application;
 * the {@code @Provider} annotation ensures it is automatically registered.</p>
 *
 * @see ContainerResponseFilter
 */
@Provider
public class DefaultCrossDomainFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext response) {
        response.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        response.getHeaders().putSingle("Access-Control-Allow-Methods", "OPTIONS, GET, POST, PUT, DELETE");
        response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type,Accept,Origin,Authorization");
    }

}
