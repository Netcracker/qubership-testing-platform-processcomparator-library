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

package org.qubership.automation.pc.core;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.qubership.automation.pc.core.exceptions.FactoryInstatiationException;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSProvider;

/**
 * Factory class responsible for instantiating implementations of {@link IVCSProvider}
 * based on the given provider name.
 *
 * <p>
 * This factory uses reflection to dynamically load provider classes from a predefined package.
 * It allows for flexible and extensible integration with various version control systems
 * (e.g., SVN, Git) without hardcoding the provider implementations.
 * </p>
 *
 * <p>
 * The class assumes that the fully qualified class name of the provider can be constructed
 * by appending the provided name to a fixed package path.
 * </p>
 *
 * @see IVCSProvider
 * @see FactoryInstatiationException
 */
public class VCSProviderFactory {

    private static final String PACKAGE_PATH = "org.qubership.automation.pc.reader.impl.vcs.providers";

    public static IVCSProvider getProvider(String vcsName) throws FactoryInstatiationException {
        Class<?> readerClass;
        try {
            readerClass = Class.forName(PACKAGE_PATH + "." + vcsName);
            return (IVCSProvider) readerClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(VCSProviderFactory.class.getName()).log(Level.SEVERE, null, ex);
            throw new FactoryInstatiationException(ex);
        }
    }
}
