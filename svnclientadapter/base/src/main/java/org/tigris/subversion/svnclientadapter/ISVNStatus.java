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

import java.io.File;
import java.util.Date;

/**
 * An interface defining the status of one subversion item (file or directory) in
 * the working copy or repository.
 *
 * @author philip schatz
 */
public interface ISVNStatus {

    /**
     * Get the SVNUrl instance of url of the resource on repository.
     *
     * @return the SVNUrl instance of url of the resource on repository
     */
    SVNUrl getUrl();

    /**
     * Get the url (String) of the resource in repository.
     *
     * @return the url (String) of the resource in repository
     */
    String getUrlString();

    /**
     * Get the last changed revision or null if resource is not managed.
     *
     * @return the last changed revision or null if resource is not managed
     */
    SVNRevision.Number getLastChangedRevision();

    /**
     * Get date this resource last changed.
     *
     * @return date this resource last changed
     */
    Date getLastChangedDate();

    /**
     * get the last commit author or null if resource is not versioned
     * or if last commit author is unknown.
     *
     * @return the last commit author or null
     */
    String getLastCommitAuthor();

    /**
     * Get the file or directory status.
     *
     * @return the file or directory status
     */
    SVNStatusKind getTextStatus();

    /**
     * Get the file or directory status of base.
     *
     * @return the file or directory status of base
     */
    SVNStatusKind getRepositoryTextStatus();

    /**
     * Get status of properties.
     *
     * @return status of properties (either Kind.NORMAL, Kind.CONFLICTED or Kind.MODIFIED)
     */
    SVNStatusKind getPropStatus();

    /**
     * Get the status of the properties base.
     *
     * @return the status of the properties base (either Kind.NORMAL, Kind.CONFLICTED or Kind.MODIFIED)
     */
    SVNStatusKind getRepositoryPropStatus();

    /**
     * Get the revision of the resource or null if not managed.
     *
     * @return the revision of the resource or null if not managed
     */
    SVNRevision.Number getRevision();

    /**
     * Get the path to this item relative to the directory from
     * which <code>status</code> was run.
     *
     * @return The path to this item relative to the directory from
     *         which <code>status</code> was run.
     */
    String getPath();

    /**
     * Get the absolute path from which this item was moved.
     *
     * @return The absolute path from which this item was moved.
     */
    String getMovedFromAbspath();

    /**
     * Get the absolute path to which this item was moved.
     *
     * @return The absolute path to which this item was moved.
     */
    String getMovedToAbspath();

    /**
     * Get the absolute path to this item.
     *
     * @return The absolute path to this item.
     */
    File getFile();

    /**
     * Get the node kind of the managed resource.
     *
     * @return The node kind of the managed resource, or {@link
     *         SVNNodeKind#UNKNOWN} not managed.
     */
    SVNNodeKind getNodeKind();

    /**
     * Get true when the resource was copied.
     *
     * @return true when the resource was copied
     */
    boolean isCopied();

    /**
     * Get true when the working copy directory is locked.
     *
     * @return true when the working copy directory is locked.
     */
    boolean isWcLocked();

    /**
     * Get true when the resource was switched relative to its parent.
     *
     * @return true when the resource was switched relative to its parent.
     */
    boolean isSwitched();

    /**
     * Returns in case of conflict, the file of the most recent repository
     * version.
     *
     * @return the filename of the most recent repository version
     */
    public File getConflictNew();

    /**
     * Returns in case of conflict, the file of the common base version.
     *
     * @return the filename of the common base version
     */
    public File getConflictOld();

    /**
     * Returns in case of conflict, the file of the former working copy
     * version.
     *
     * @return the filename of the former working copy version
     */
    public File getConflictWorking();

    /**
     * Returns the lock  owner.
     *
     * @return the lock owner
     */
    public String getLockOwner();

    /**
     * Returns the lock creation date.
     *
     * @return the lock creation date
     */
    public Date getLockCreationDate();

    /**
     * Returns the lock  comment.
     *
     * @return the lock comment
     */
    public String getLockComment();

    /**
     * Returns the tree conflicted state.
     *
     * @return the tree conflicted state
     */
    public boolean hasTreeConflict();

    /**
     * Returns the conflict descriptor for the tree conflict.
     *
     * @return the conflict descriptor for the tree conflict
     */
    public SVNConflictDescriptor getConflictDescriptor();

    /**
     * Returns if the item is a file external.
     *
     * @return is the item is a file external
     */
    public boolean isFileExternal();

}
