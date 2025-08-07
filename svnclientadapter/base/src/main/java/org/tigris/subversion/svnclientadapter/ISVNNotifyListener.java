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


/**
 * A callback interface used for receiving notifications of the progress of
 * a subversion command invocation.
 *
 * @author Cédric Chabanois <a
 * href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 */
public interface ISVNNotifyListener {

    /**
     * Represents the types of Subversion commands that may trigger notifications.
     * <p>
     * Each constant corresponds to a specific SVN operation (e.g., commit, update, merge),
     * used in the {@link ISVNNotifyListener#setCommand(int)} method.
     */
    final class Command {

        /**
         * Private constructor to prevent instantiation of this utility class.
         */
        private Command() {
            // No instances allowed
        }

        /** Undefined command. */
        public static final int UNDEFINED = 0;

        /** Add operation. */
        public static final int ADD = 1;

        /** Checkout operation. */
        public static final int CHECKOUT = 2;

        /** Commit operation. */
        public static final int COMMIT = 3;

        /** Update operation. */
        public static final int UPDATE = 4;

        /** Move operation. */
        public static final int MOVE = 5;

        /** Copy operation. */
        public static final int COPY = 6;

        /** Remove (delete) operation. */
        public static final int REMOVE = 7;

        /** Export operation. */
        public static final int EXPORT = 8;

        /** Import operation. */
        public static final int IMPORT = 9;

        /** Create directory operation. */
        public static final int MKDIR = 10;

        /** List directory contents. */
        public static final int LS = 11;

        /** Status operation. */
        public static final int STATUS = 12;

        /** Log operation. */
        public static final int LOG = 13;

        /** Set property operation. */
        public static final int PROPSET = 14;

        /** Delete property operation. */
        public static final int PROPDEL = 15;

        /** Revert operation. */
        public static final int REVERT = 16;

        /** Diff operation. */
        public static final int DIFF = 17;

        /** Cat (output file content) operation. */
        public static final int CAT = 18;

        /** Info operation. */
        public static final int INFO = 19;

        /** Get property operation. */
        public static final int PROPGET = 20;

        /** List properties operation. */
        public static final int PROPLIST = 21;

        /** Resolved operation. */
        public static final int RESOLVED = 22;

        /** Create repository operation. */
        public static final int CREATE_REPOSITORY = 23;

        /** Cleanup working copy operation. */
        public static final int CLEANUP = 24;

        /** Annotate (blame) operation. */
        public static final int ANNOTATE = 25;

        /** Switch operation. */
        public static final int SWITCH = 26;

        /** Merge operation. */
        public static final int MERGE = 27;

        /** Lock operation. */
        public static final int LOCK = 28;

        /** Unlock operation. */
        public static final int UNLOCK = 29;

        /** Relocate working copy. */
        public static final int RELOCATE = 30;

        /** Resolve conflicts operation. */
        public static final int RESOLVE = 31;

        /** Retrieve merge info operation. */
        public static final int MERGEINFO = 32;

        /** Upgrade working copy format. */
        public static final int UPGRADE = 33;
    }

    /**
     * Tell the callback the command to be executed.
     *
     * @param command one of {@link Command}.* constants
     */
    void setCommand(int command);

    /**
     * called at the beginning of the command.
     *
     * @param commandLine log command line
     */
    void logCommandLine(String commandLine);

    /**
     * called multiple times during the execution of a command.
     *
     * @param message log message
     */
    void logMessage(String message);

    /**
     * called when an error happen during a command.
     *
     * @param message error message
     */
    void logError(String message);

    /**
     * Called when a command has completed to report
     * that the command completed against the specified
     * revision.
     *
     * @param revision revision
     * @param path     path to folder which revision is reported (either root, or some of svn:externals)
     */
    void logRevision(long revision, String path);

    /**
     * called when a command has completed.
     *
     * @param message log message
     */
    void logCompleted(String message);

    /**
     * called when a subversion action happen on a file (add, delete, update ...).
     *
     * @param path the canonical path of the file or dir
     * @param kind file or dir or unknown
     */
    void onNotify(File path, SVNNodeKind kind);

}
