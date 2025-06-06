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
 * JAX-RS application configuration class for the Reader module.
 *
 * <p>This application class registers REST resources and filters needed for the Reader service,
 * such as CORS filters to allow cross-domain HTTP requests.</p>
 *
 * <p>It extends {@link javax.ws.rs.core.Application} and overrides {@code getClasses()} to
 * provide a custom set of components.</p>
 *
 * @see javax.ws.rs.core.Application
 */
public class ReaderApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        classes.add(DefaultCrossDomainFilter.class);
        return classes;
    }
}
