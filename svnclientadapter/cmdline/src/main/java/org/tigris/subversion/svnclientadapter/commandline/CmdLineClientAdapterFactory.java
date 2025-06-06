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

package org.tigris.subversion.svnclientadapter.commandline;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;

/**
 * Concrete implementation of SVNClientAdapterFactory for command line
 * interface. To register this factory, just call
 * {@link CmdLineClientAdapterFactory#setup()}
 */
public class CmdLineClientAdapterFactory extends SVNClientAdapterFactory {

    /**
     * Client adapter implementation identifier.
     */
    public static final String COMMANDLINE_CLIENT = "commandline";

    private static boolean is13ClientAvailable = false;

    /**
     * Private constructor. Clients are expected the use
     * {@link #createSvnClientImpl()}, res. ask the
     * {@link SVNClientAdapterFactory}.
     */
    private CmdLineClientAdapterFactory() {
        super();
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory#createSVNClientImpl()
     */
    protected ISVNClientAdapter createSvnClientImpl() {
        if (is13ClientAvailable) {
            return new CmdLineClientAdapter(new CmdLineNotificationHandler());
        } else {
            return new CmdLineClientAdapter12(new CmdLineNotificationHandler());
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory#getClientType()
     */
    protected String getClientType() {
        return COMMANDLINE_CLIENT;
    }

    /**
     * Initializes and registers the command-line client adapter implementation (version 1.2).
     * </p>
     * This method performs the following steps:
     * <ul>
     *     <li>Checks if the Subversion command-line client is available in the environment.</li>
     *     <li>If not available, throws an {@link SVNClientException}.</li>
     *     <li>Detects whether version 1.3 of the client is also available
     *     and updates the internal flag accordingly.</li>
     *     <li>Registers the {@link CmdLineClientAdapterFactory} implementation
     *     in the {@link SVNClientAdapterFactory}.</li>
     * </ul>
     *
     * @throws SVNClientException if the required command-line client is not available
     */
    public static void setup() throws SVNClientException {
        if (!CmdLineClientAdapter12.isAvailable()) {
            throw new SVNClientException("Command line client adapter is not available");
        }

        is13ClientAvailable = CmdLineClientAdapter.isAvailable();

        SVNClientAdapterFactory.registerAdapterFactory(new CmdLineClientAdapterFactory());
    }

}
