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

import java.io.File;

import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.utils.SVNStatusUtils;

abstract class CmdLineStatusPart {

    protected SVNStatusKind textStatus;
    protected SVNStatusKind propStatus;

    protected CmdLineStatusPart(SVNStatusKind textStatus, SVNStatusKind propStatus) {
        super();
        this.textStatus = textStatus;
        this.propStatus = propStatus;
    }

    public boolean isManaged() {
        return SVNStatusUtils.isManaged(textStatus);
    }

    /**
     * Indicates whether the item was copied from another location in the repository.
     *
     * @return {@code true} if the item was added with history (i.e., copied); {@code false} otherwise.
     */
    public abstract boolean isCopied();

    /**
     * Indicates whether the working copy directory of this item is currently locked.
     *
     * @return {@code true} if the item's working copy is locked; {@code false} otherwise.
     */
    public abstract boolean isWcLocked();

    /**
     * Indicates whether the item is switched relative to its parent directory.
     *
     * @return {@code true} if the item is switched to a different URL than its parent; {@code false} otherwise.
     */
    public abstract boolean isSwitched();

    /**
     * Determines if the resource has a corresponding item in the repository.
     *
     * @return {@code true} if the resource is versioned and not newly added; {@code false} otherwise.
     */
    public boolean hasRemote() {
        return (isManaged() && getTextStatus() != SVNStatusKind.ADDED);
    }

    /**
     * Get status of the item itself (e.g. directory or file).
     *
     * @return The status of the item itself (e.g. directory or file).
     */
    public SVNStatusKind getTextStatus() {
        return textStatus;
    }

    /* package */ void setTextStatus(SVNStatusKind textSt) {
        this.textStatus = textSt;
    }

    public SVNStatusKind getPropStatus() {
        return propStatus;
    }

    public abstract SVNStatusKind getRepositoryTextStatus();

    public abstract SVNStatusKind getRepositoryPropStatus();

    /**
     * Get absolute path to this item.
     *
     * @return The absolute path to this item.
     */
    public abstract File getFile();

    public abstract String getPath();

    static class CmdLineStatusPartFromStdout extends CmdLineStatusPart {

        public static final int STATUS_FILE_WIDTH = 40;
        protected String path;
        protected File file;
        protected char history;
        protected boolean wcLocked;
        protected boolean switched;

        /**
         * here are some statusLine samples :
         * A               0       ?   ?           added.txt
         * I                                       ignored.txt
         * Note that there is not output for files that do not exist and are not deleted.
         */
        CmdLineStatusPartFromStdout(String statusLine) {
            super(getTextStatus(statusLine.charAt(0)), getPropStatus(statusLine.charAt(1)));
            wcLocked = getWcLockStatus(statusLine.charAt(2));
            history = statusLine.charAt(3);
            switched = getSwitchedStatus(statusLine.charAt(4));
            path = statusLine.substring(STATUS_FILE_WIDTH).trim();
            file = new File(path);
        }

        /**
         * Indicates whether the item was copied from another location in the repository.
         *
         * @return {@code true} if the item was added with history (i.e., copied); {@code false} otherwise.
         */
        public boolean isCopied() {
            return (history == '+');
        }

        /**
         * Indicates whether the working copy directory of this item is currently locked.
         *
         * @return {@code true} if the item's working copy is locked; {@code false} otherwise.
         */
        public boolean isWcLocked() {
            return wcLocked;
        }

        /**
         * Indicates whether this item is switched relative to its parent directory.
         * A switched item refers to a different repository location than the rest of the working copy.
         *
         * @return {@code true} if the item is switched; {@code false} otherwise.
         */
        public boolean isSwitched() {
            return switched;
        }

        /**
         * Get status of the item itself (e.g. directory or file).
         *
         * @return The status of the item itself (e.g. directory or file).
         */
        private static SVNStatusKind getTextStatus(char statusChar) {
            switch (statusChar) {
                case ' ': // none or normal
                    return SVNStatusKind.NORMAL;
                case 'A':
                    return SVNStatusKind.ADDED;
                case '!': // missing or incomplete
                    return SVNStatusKind.MISSING;
                case 'D':
                    return SVNStatusKind.DELETED;
                case 'R':
                    return SVNStatusKind.REPLACED;
                case 'M':
                    return SVNStatusKind.MODIFIED;
                case 'G':
                    return SVNStatusKind.MERGED;
                case 'C':
                    return SVNStatusKind.CONFLICTED;
                case '~':
                    return SVNStatusKind.OBSTRUCTED;
                case 'I':
                    return SVNStatusKind.IGNORED;
                case 'X':
                    return SVNStatusKind.EXTERNAL;
                case '?':
                    return SVNStatusKind.UNVERSIONED;
                default:
                    return SVNStatusKind.NONE;
            }
        }

        private static SVNStatusKind getPropStatus(char statusChar) {
            switch (statusChar) {
                case ' ': // no modifications
                    return SVNStatusKind.NONE;
                case 'C':
                    return SVNStatusKind.CONFLICTED;
                case 'M':
                    return SVNStatusKind.MODIFIED;
                default:
                    return SVNStatusKind.NORMAL;
            }
        }

        private static boolean getWcLockStatus(char statusChar) {
            switch (statusChar) {
                case ' ': // no lock
                    return false;
                case 'L':
                    return true;
                default:
                    return false;
            }
        }

        private static boolean getSwitchedStatus(char statusChar) {
            switch (statusChar) {
                case ' ': // no switch
                    return false;
                case 'S':
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Get absolute path to this item.
         *
         * @return The absolute path to this item.
         */
        public File getFile() {
            return file.getAbsoluteFile();
        }

        public String getPath() {
            return path;
        }

        public SVNStatusKind getRepositoryTextStatus() {
            //Not available from cmdLine's stdout
            return null;
        }

        public SVNStatusKind getRepositoryPropStatus() {
            //Not available from cmdLine's stdout
            return null;
        }

    }

    static class CmdLineStatusPartFromXml extends CmdLineStatusPart {

        private CmdLineStatusFromXml status;

        private CmdLineStatusPartFromXml(CmdLineStatusFromXml status) {
            super(status.getTextStatus(), status.getPropStatus());
            this.status = status;
        }

        public static CmdLineStatusPartFromXml[] createStatusParts(byte[] cmdLineResults) throws CmdLineException {
            CmdLineStatusFromXml[] statuses = CmdLineStatusFromXml.createStatuses(cmdLineResults);
            CmdLineStatusPartFromXml[] result = new CmdLineStatusPartFromXml[statuses.length];
            for (int i = 0; i < statuses.length; i++) {
                result[i] = new CmdLineStatusPartFromXml(statuses[i]);
            }
            return result;
        }

        public File getFile() {
            return status.getFile();
        }

        public boolean isCopied() {
            return status.isCopied();
        }

        public boolean isWcLocked() {
            return status.isWcLocked();
        }

        public boolean isSwitched() {
            return status.isSwitched();
        }

        public SVNStatusKind getRepositoryTextStatus() {
            return status.getRepositoryTextStatus();
        }

        public SVNStatusKind getRepositoryPropStatus() {
            return status.getRepositoryPropStatus();
        }

        public String getPath() {
            return status.getPath();
        }

    }
}

