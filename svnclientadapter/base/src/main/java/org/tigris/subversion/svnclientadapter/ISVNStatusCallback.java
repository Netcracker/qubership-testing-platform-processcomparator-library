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

package org.tigris.subversion.svnclientadapter;

/**
 * A callback interface for processing the results of an SVN status operation.
 * <p>
 * Implementations of this interface receive status information for each path
 * during a status traversal.
 */
public interface ISVNStatusCallback {

    /**
     * Called for each file or directory during a status operation.
     *
     * @param path   the file or directory path being reported
     * @param status the status information for the path
     */
    void doStatus(String path, ISVNStatus status);
}
