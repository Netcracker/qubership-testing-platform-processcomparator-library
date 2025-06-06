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
 * A JAX-RS response filter that enables Cross-Origin Resource Sharing (CORS).
 *
 * <p>This filter is registered as a JAX-RS {@code @Provider} and is applied
 * to all HTTP responses. It sets the necessary headers to allow cross-domain
 * requests from any origin.</p>
 *
 * <p>By default, it allows the following HTTP methods: {@code OPTIONS, GET, POST, PUT, DELETE},
 * and permits the headers: {@code Content-Type, Accept, Origin, Authorization}.</p>
 *
 * <p>Use this filter to enable CORS support in RESTful applications deployed on
 * servers that do not handle CORS by default (e.g., embedded servers or minimal configurations).</p>
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
