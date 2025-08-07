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
 * A special {@link ISVNStatus} implementation that is used if a File/Folder is not versioned or is ignored.
 *
 * @author Philip Schatz (schatz at tigris)
 * @author Cédric Chabanois (cchabanois at no-log.org)
 */
public class SVNStatusUnversioned implements ISVNStatus {
    private File file;
    private boolean isIgnored = false;

    /**
     * Constructs a new {@code SVNStatusUnversioned} instance for the given file.
     *
     * @param file       the file that is unversioned
     * @param isIgnored  {@code true} if the file is ignored (e.g. matches an ignore pattern),
     *                   {@code false} if it's simply unversioned
     *                   <p>
     *                   Note: a file may be both unversioned and ignored.
     *                   This flag indicates that the item is treated as ignored
     *                   by the Subversion client.
     */
    public SVNStatusUnversioned(File file, boolean isIgnored) {
        this.file = file;
        // A file can be both unversioned and ignored.
        this.isIgnored = isIgnored;
    }

    /**
     * Constructs a new {@code SVNStatusUnversioned} instance for the given file,
     * which is assumed to be unversioned but not ignored.
     *
     * @param file the file that is unversioned
     */
    public SVNStatusUnversioned(File file) {
        this.file = file;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getPath()
     */
    public String getPath() {
        return file.getPath();
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getMovedFromAbspath()
     */
    public String getMovedFromAbspath() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getMovedToAbspath()
     */
    public String getMovedToAbspath() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getFile()
     */
    public File getFile() {
        return file.getAbsoluteFile();
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getUrl()
     */
    public SVNUrl getUrl() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getUrlString()
     */
    public String getUrlString() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLastChangedRevision()
     */
    public SVNRevision.Number getLastChangedRevision() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLastChangedDate()
     */
    public Date getLastChangedDate() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLastCommitAuthor()
     */
    public String getLastCommitAuthor() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getTextStatus()
     */
    public SVNStatusKind getTextStatus() {
        if (isIgnored) {
            return SVNStatusKind.IGNORED;
        }
        return SVNStatusKind.UNVERSIONED;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getPropStatus()
     */
    public SVNStatusKind getPropStatus() {
        //As this status does not describe a managed resource, we
        //cannot pretend that there is property status, and thus always
        //{@link SVNStatusKind#NONE}.
        return SVNStatusKind.NONE;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getRepositoryTextStatus()
     */
    public SVNStatusKind getRepositoryTextStatus() {
        return SVNStatusKind.UNVERSIONED;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getRepositoryPropStatus()
     */
    public SVNStatusKind getRepositoryPropStatus() {
        return SVNStatusKind.UNVERSIONED;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getRevision()
     */
    public SVNRevision.Number getRevision() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#isCopied()
     */
    public boolean isCopied() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#isWcLocked()
     */
    public boolean isWcLocked() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#isSwitched()
     */
    public boolean isSwitched() {
        return false;
    }

    /* (non-Javadoc)
     * @see
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getNodeKind()
     */
    public SVNNodeKind getNodeKind() {
        //As this status does not describe a managed resource, we
        //cannot pretend to know the node kind, and thus always return
        //{@link SVNNodeKind#UNKNOWN}.
        return SVNNodeKind.UNKNOWN;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getConflictNew()
     */
    public File getConflictNew() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getConflictOld()
     */
    public File getConflictOld() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getConflictWorking()
     */
    public File getConflictWorking() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLockComment()
     */
    public String getLockComment() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLockCreationDate()
     */
    public Date getLockCreationDate() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getLockOwner()
     */
    public String getLockOwner() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#getConflictDescriptor()
     */
    public SVNConflictDescriptor getConflictDescriptor() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#hasTreeConflict()
     */
    public boolean hasTreeConflict() {
        // TODO Auto-generated method stub
        return false;
    }


    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNStatus#isFileExternal()
     */
    public boolean isFileExternal() {
        return false;
    }
}
