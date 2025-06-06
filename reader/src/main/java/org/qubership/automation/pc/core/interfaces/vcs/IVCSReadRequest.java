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

/**
 * Interface representing a read request to a Version Control System (VCS).
 *
 * <p>
 * Implementations of this interface provide information about what file or resource
 * should be read from the VCS, including the target URL and the specific revision.
 * </p>
 */
public interface IVCSReadRequest {

    String getTargetUrl();

    String getRevision();
}
