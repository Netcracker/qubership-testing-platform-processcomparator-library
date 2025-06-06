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

package org.qubership.automation.pc.core.interfaces.vcs;

import java.util.Properties;

import org.qubership.automation.pc.core.exceptions.ReaderException;

/**
 * Interface for a Version Control System (VCS) provider used to read files or metadata
 * from remote VCS repositories such as Git, SVN, etc.
 *
 * <p>
 * Implementations of this interface should handle authentication, connection, and data retrieval
 * based on the provided configuration.
 * </p>
 *
 * <p>Standard property keys for provider configuration:</p>
 * <ul>
 *   <li>{@link #PROP_EXTENSION_FILTER} - a regex or wildcard expression to filter files by extension</li>
 *   <li>{@link #PROP_DEPTH_READ} - optional depth of recursive reads (if supported)</li>
 *   <li>{@link #PROP_SORT_BY} - sorting strategy (e.g., name, date)</li>
 *   <li>{@link #PROP_VCS_PROVIDER} - name of the VCS provider implementation</li>
 *   <li>{@link #PROP_PATH} - repository path to be accessed</li>
 *   <li>{@link #PROP_REVISION} - revision or branch to retrieve</li>
 * </ul>
 *
 * <p>Implementations must also implement {@link AutoCloseable} to allow resource cleanup.</p>
 */
public interface IVCSProvider extends AutoCloseable {

    static final String PROP_EXTENSION_FILTER = "extensionFilter";
    static final String PROP_DEPTH_READ = "depthRead";
    static final String PROP_SORT_BY = "sortBy";
    static final String PROP_VCS_PROVIDER = "provider";
    static final String PROP_PATH = "path";
    static final String PROP_REVISION = "revision";

    void init(Properties providerParameters) throws ReaderException;

    void init(Properties providerParameters, String userName, String password) throws ReaderException;

    void setUser(String userName);

    void setPassword(String password);

    IVCSContext readRemote(IVCSReadRequest request) throws ReaderException;
}
