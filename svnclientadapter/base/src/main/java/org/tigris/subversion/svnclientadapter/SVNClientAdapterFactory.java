/*******************************************************************************
 * Copyright (c) 2004, 2006 svnClientAdapter project and others.
 * </p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * </p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/

package org.tigris.subversion.svnclientadapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract Factory for creating instances of {@link ISVNClientAdapter}.
 * <p>
 * Concrete factories (e.g., for JavaHL, command-line) should extend this class
 * and register themselves using {@link #registerAdapterFactory(SVNClientAdapterFactory)}.
 * The first factory registered becomes the preferred client.
 * </p>
 *
 * @author Cédric Chabanois
 * @author Panagiotis Korros
 */
public abstract class SVNClientAdapterFactory {

    private static Map ourFactoriesMap;

    // The first factory added is the preferred one
    private static SVNClientAdapterFactory preferredFactory;

    /**
     * Default constructor.
     */
    protected SVNClientAdapterFactory() {
        // No-op
    }

    /**
     * Creates a new instance of an SVN client adapter implementation.
     *
     * @return an implementation of {@link ISVNClientAdapter}
     */
    protected abstract ISVNClientAdapter createSvnClientImpl();

    /**
     * Returns the type identifier of the client implementation (e.g., "javahl", "cmdline").
     *
     * @return a string representing the client type
     */
    protected abstract String getClientType();

    /**
     * Creates a new {@link ISVNClientAdapter} based on the specified client type.
     * You can create a JavaHL client or a command-line client, depending on availability.
     *
     * @param clientType the SVN client type
     * @return the requested client adapter, or {@code null} if not available
     */
    public static ISVNClientAdapter createSvnClient(String clientType) {
        if (ourFactoriesMap == null || !ourFactoriesMap.containsKey(clientType)) {
            return null;
        }
        SVNClientAdapterFactory factory = (SVNClientAdapterFactory) ourFactoriesMap.get(clientType);
        if (factory != null) {
            return factory.createSvnClientImpl();
        }
        return null;
    }

    /**
     * Checks whether the given client type is available.
     *
     * @param clientType the SVN client type
     * @return {@code true} if the client type is available; {@code false} otherwise
     */
    public static boolean isSvnClientAvailable(String clientType) {
        return ourFactoriesMap != null && ourFactoriesMap.containsKey(clientType);
    }

    /**
     * Gets the preferred SVN client type.
     *
     * @return the type identifier of the preferred SVN client
     * @throws SVNClientException if no client has been registered
     */
    public static String getPreferredSvnClientType() throws SVNClientException {
        if (preferredFactory != null) {
            return preferredFactory.getClientType();
        }
        throw new SVNClientException("No subversion client interface found.");
    }

    /**
     * Registers a new {@link SVNClientAdapterFactory}.
     * <p>
     * The first registered factory becomes the preferred one. Subsequent
     * registrations for the same type will throw an exception.
     * </p>
     *
     * @param factory the factory to register
     * @throws SVNClientException if a factory with the same type has already been registered
     */
    protected static void registerAdapterFactory(SVNClientAdapterFactory factory) throws SVNClientException {
        if (factory == null) {
            return;
        }
        if (ourFactoriesMap == null) {
            ourFactoriesMap = new HashMap();
        }
        String type = factory.getClientType();
        if (!ourFactoriesMap.containsKey(type)) {
            ourFactoriesMap.put(type, factory);
            if (preferredFactory == null) {
                preferredFactory = factory;
            }
        } else {
            throw new SVNClientException("factory for type " + type + " already registered");
        }
    }
}
