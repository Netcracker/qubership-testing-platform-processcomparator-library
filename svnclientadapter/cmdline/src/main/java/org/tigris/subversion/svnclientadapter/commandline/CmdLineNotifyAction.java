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

/**
 * The type of action triggering the notification.
 */
public interface CmdLineNotifyAction {
    /**
     * Adding a path to revision control.
     */
    public static final int add = 0;

    /**
     * Copying a versioned path.
     */
    public static final int copy = 1;

    /**
     * Deleting a versioned path.
     */
    public static final int delete = 2;

    /**
     * Restoring a missing path from the pristine text-base.
     */
    public static final int restore = 3;

    /**
     * Reverting a modified path.
     */
    public static final int revert = 4;

    /**
     * A revert operation has failed.
     */
    public static final int failed_revert = 5;

    /**
     * Resolving a conflict.
     */
    public static final int resolved = 6;

    /**
     * Skipping a path.
     */
    public static final int skip = 7;

    /* The update actions are also used for checkouts, switches, and merges. */

    /**
     * Got a delete in an update.
     */
    public static final int update_delete = 8;

    /**
     * Got an add in an update.
     */
    public static final int update_add = 9;

    /**
     * Got any other action in an update.
     */
    public static final int update_update = 10;

    /**
     * The last notification in an update.
     */
    public static final int update_completed = 11;

    /**
     * About to update an external module, use for checkouts and switches too,
     * end with @c svn_wc_update_completed.
     */
    public static final int update_external = 12;

    /**
     * The last notification in a status (including status on externals).
     */
    public static final int status_completed = 13;

    /**
     * Running status on an external module.
     */
    public static final int status_external = 14;


    /**
     * Committing a modification.
     */
    public static final int commit_modified = 15;

    /**
     * Committing an addition.
     */
    public static final int commit_added = 16;

    /**
     * Committing a deletion.
     */
    public static final int commit_deleted = 17;

    /**
     * Committing a replacement.
     */
    public static final int commit_replaced = 18;

    /**
     * Transmitting post-fix text-delta data for a file.
     */
    public static final int commit_postfix_txdelta = 19;

    /**
     * Processed a single revision's blame.
     */
    public static final int blame_revision = 20;

    /**
     * Indicates that a path has been successfully locked.
     * </p>
     * This event type is typically used in {@link ISVNNotifyListener} implementations to notify that a
     * working copy path or repository URL has been locked for exclusive access.
     */
    public static final int locked = 21;

    /**
     * Indicates that a path has been successfully unlocked.
     * </p>
     * This event type is triggered after a successful removal of a lock from a path that was previously
     * locked via Subversion's locking mechanism.
     */
    public static final int unlocked = 22;

    /**
     * Indicates that an attempt to lock a path has failed.
     * </p>
     * This may occur due to permission issues, existing locks held by others, or network problems.
     * Provides a way to notify tools or UIs that the lock operation was not successful.
     */
    public static final int failed_lock = 23;

    /**
     * Indicates that an attempt to unlock a path has failed.
     * </p>
     * Possible reasons include trying to unlock a path that is not locked, lack of permissions,
     * or repository access errors.
     */
    public static final int failed_unlock = 24;


    /**
     * textual representation of the action types.
     */
    public static final String[] actionNames =
            {
                    "add",
                    "copy",
                    "delete",
                    "restore",
                    "revert",
                    "failed revert",
                    "resolved",
                    "skip",
                    "update delete",
                    "update add",
                    "update modified",
                    "update completed",
                    "update external",
                    "status completed",
                    "status external",
                    "sending modified",
                    "sending added   ",
                    "sending deleted ",
                    "sending replaced",
                    "transfer",
                    "blame revision processed",
                    "locked",
                    "unlocked",
                    "locking failed",
                    "unlocking failed",
            };
}
