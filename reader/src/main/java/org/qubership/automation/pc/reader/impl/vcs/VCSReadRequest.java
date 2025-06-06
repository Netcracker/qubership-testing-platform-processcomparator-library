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

package org.qubership.automation.pc.reader.impl.vcs;

import org.qubership.automation.pc.core.interfaces.vcs.IVCSReadRequest;

/**
 * Represents a request to read data from a Version Control System (VCS) repository.
 *
 * <p>
 * This class encapsulates the necessary parameters for reading remote files
 * from a VCS, such as the target URL and the revision identifier.
 * </p>
 *
 * <p>
 * It serves as a concrete implementation of the {@link IVCSReadRequest} interface.
 * </p>
 *
 * @see IVCSReadRequest
 */
public class VCSReadRequest implements IVCSReadRequest {

    private final String targetUrl;
    private final String revision;

    public VCSReadRequest(String targetUrl, String revision) {
        this.targetUrl = targetUrl;
        this.revision = revision;
    }

    @Override
    public String getTargetUrl() {
        return this.targetUrl;
    }

    @Override
    public String getRevision() {
        return this.revision;
    }

}
