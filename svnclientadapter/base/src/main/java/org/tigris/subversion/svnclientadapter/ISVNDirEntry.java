/*******************************************************************************
 * Copyright (c) 2003, 2006 svnClientAdapter project and others.
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

import java.util.Date;

/**
 * An interface describing subversion directory entry.
 * (E.g. a record returned by call to svn list)
 *
 * @author Cédric Chabanois
 */
public interface ISVNDirEntry {

    /**
     * Get pathname of the entry.
     *
     * @return the pathname of the entry
     */
    String getPath();

    /**
     * Get the date of the last change.
     *
     * @return the date of the last change
     */
    Date getLastChangedDate();

    /**
     * Get the revision number of the last change.
     *
     * @return the revision number of the last change
     */
    SVNRevision.Number getLastChangedRevision();

    /**
     * True if the item has properties managed by subversion.
     *
     * @return true if the item has properties managed by subversion
     */
    boolean getHasProps();

    /**
     * Get the name of the author of the last change.
     *
     * @return the name of the author of the last change
     */
    String getLastCommitAuthor();

    /**
     * Get the kind of the node (directory or file).
     *
     * @return the kind of the node (directory or file)
     */
    SVNNodeKind getNodeKind();

    /**
     * Get length of file text, or 0 for directories.
     *
     * @return length of file text, or 0 for directories
     */
    long getSize();
}
