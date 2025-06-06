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

package org.qubership.automation.pc;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.qubership.automation.pc.filters.DefaultCrossDomainFilter;

/**
 * The entry point of the JAX-RS application used to configure and bootstrap the REST API.
 *
 * <p>
 * Registers components and filters required for the comparator service, such as cross-domain support.
 * This class extends {@link javax.ws.rs.core.Application} and overrides the set of resource and provider classes.
 * </p>
 *
 * <p>It is typically configured as part of a web.xml deployment descriptor or automatically discovered by JAX-RS.</p>
 */
public class ComparatorApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        classes.add(DefaultCrossDomainFilter.class);
        return classes;
    }
}
