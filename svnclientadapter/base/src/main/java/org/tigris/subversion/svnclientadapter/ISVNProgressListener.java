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
 * A listener interface for receiving progress updates during the execution
 * of Subversion operations (e.g. checkout, update, commit).
 * <p>
 * Implementations of this interface can be used to monitor long-running
 * commands by receiving {@link SVNProgressEvent} callbacks.
 */
public interface ISVNProgressListener {

    /**
     * Called to notify the listener of a progress event during an SVN operation.
     *
     * @param progressEvent the progress event providing details about the current operation progress
     */
    void onProgress(SVNProgressEvent progressEvent);
}
