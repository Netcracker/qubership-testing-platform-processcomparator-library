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

/**
 * Represents a source item for a Subversion copy operation.
 * <p>
 * A copy source consists of:
 * <ul>
 *   <li>A path or URL</li>
 *   <li>A revision from which to copy</li>
 *   <li>An optional peg revision used for resolving the path</li>
 * </ul>
 * This is used in APIs where multiple sources can be copied into a destination,
 * possibly with history.
 */
public class SVNCopySource {

    /**
     * The source path or URL.
     */
    private String path;

    /**
     * The source revision.
     */
    private SVNRevision revision;

    /**
     * The peg revision.
     */
    private SVNRevision pegRevision;

    /**
     * Create a new instance.
     *
     * @param path        The source path or URL.
     * @param revision    The source revision.
     * @param pegRevision The peg revision.  Typically interpreted as
     *                    {@link org.tigris.subversion.svnclientadapter.SVNRevision#HEAD} when <code>null</code>.
     */
    public SVNCopySource(String path, SVNRevision revision, SVNRevision pegRevision) {
        this.path = path;
        this.revision = revision;
        this.pegRevision = pegRevision;
    }

    /**
     * Get source path or URL.
     *
     * @return The source path or URL.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Get source revision.
     *
     * @return The source revision.
     */
    public SVNRevision getRevision() {
        return this.revision;
    }

    /**
     * Get peg revision.
     *
     * @return The peg revision.
     */
    public SVNRevision getPegRevision() {
        return this.pegRevision;
    }

}
