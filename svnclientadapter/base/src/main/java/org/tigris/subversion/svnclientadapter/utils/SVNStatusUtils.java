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

package org.tigris.subversion.svnclientadapter.utils;

import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * Utility class that provides static helper methods to evaluate and interpret
 * Subversion (SVN) status kinds.
 * <p>
 * This class is used to determine the version control status of resources,
 * such as whether they are versioned, ignored, modified, conflicted, or ready
 * for commit/revert operations.
 * </p>
 *
 * @author cedric chabanois (cchab at tigris.org)
 */
public class SVNStatusUtils {

    /**
     * Checks whether the given status indicates a versioned (i.e. managed) resource.
     *
     * @param textStatus the status to check (non-{@code null})
     * @return {@code true} if the status represents a managed resource; {@code false} otherwise
     */
    public static boolean isManaged(SVNStatusKind textStatus) {
        return (!textStatus.equals(SVNStatusKind.UNVERSIONED)
                && !textStatus.equals(SVNStatusKind.NONE)
                && !textStatus.equals(SVNStatusKind.IGNORED));
    }

    /**
     * Checks whether the given status object refers to a versioned (i.e. managed) resource.
     *
     * @param status the SVN status object to check
     * @return {@code true} if the resource is managed; {@code false} otherwise
     */
    public static boolean isManaged(ISVNStatus status) {
        return isManaged(status.getTextStatus());
    }

    /**
     * Checks whether the resource has a corresponding version in the repository.
     *
     * @param status the SVN status object to check
     * @return {@code true} if the resource has a remote version; {@code false} otherwise
     */
    public static boolean hasRemote(ISVNStatus status) {
        SVNStatusKind textStatus = status.getTextStatus();
        return (isManaged(textStatus)) && (!textStatus.equals(SVNStatusKind.ADDED) || status.isCopied());
    }

    /**
     * Checks if the resource has been added.
     *
     * @param status the SVN status object
     * @return {@code true} if added; {@code false} otherwise
     */
    public static boolean isAdded(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.ADDED);
    }

    /**
     * Checks if the resource has been deleted.
     *
     * @param status the SVN status object
     * @return {@code true} if deleted; {@code false} otherwise
     */
    public static boolean isDeleted(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.DELETED);
    }

    /**
     * Checks if the resource has been replaced.
     *
     * @param status the SVN status object
     * @return {@code true} if replaced; {@code false} otherwise
     */
    public static boolean isReplaced(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.REPLACED);
    }

    /**
     * Checks if the resource is missing from the file system.
     *
     * @param status the SVN status object
     * @return {@code true} if missing; {@code false} otherwise
     */
    public static boolean isMissing(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.MISSING);
    }

    /**
     * Checks if the resource is ignored by version control.
     *
     * @param status the SVN status object
     * @return {@code true} if ignored; {@code false} otherwise
     */
    public static boolean isIgnored(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.IGNORED);
    }

    /**
     * Checks if the text content of the resource has been merged.
     *
     * @param status the SVN status object
     * @return {@code true} if merged; {@code false} otherwise
     */
    public static boolean isTextMerged(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.MERGED);
    }

    /**
     * Checks if the text content of the resource has been modified.
     *
     * @param status the SVN status object
     * @return {@code true} if modified; {@code false} otherwise
     */
    public static boolean isTextModified(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.MODIFIED);
    }

    /**
     * Checks if the text content of the resource has conflicts.
     *
     * @param status the SVN status object
     * @return {@code true} if conflicted; {@code false} otherwise
     */
    public static boolean isTextConflicted(ISVNStatus status) {
        return status.getTextStatus().equals(SVNStatusKind.CONFLICTED);
    }

    /**
     * Checks if the properties of the resource have been modified.
     *
     * @param status the SVN status object
     * @return {@code true} if properties modified; {@code false} otherwise
     */
    public static boolean isPropModified(ISVNStatus status) {
        return status.getPropStatus().equals(SVNStatusKind.MODIFIED);
    }

    /**
     * Checks if the properties of the resource have conflicts.
     *
     * @param status the SVN status object
     * @return {@code true} if property conflicts exist; {@code false} otherwise
     */
    public static boolean isPropConflicted(ISVNStatus status) {
        return status.getPropStatus().equals(SVNStatusKind.CONFLICTED);
    }

    /**
     * Determines whether the resource is in an "outgoing" state, meaning it has
     * changes that are ready to be committed.
     *
     * @param status the SVN status object
     * @return {@code true} if ready for commit; {@code false} otherwise
     */
    public static boolean isReadyForCommit(ISVNStatus status) {
        return isTextModified(status) || isAdded(status) || isDeleted(status)
                || isReplaced(status) || isPropModified(status)
                || isTextConflicted(status) || isPropConflicted(status)
                || (!isManaged(status) && !isIgnored(status));
    }

    /**
     * Determines whether the resource is in a "changed" state, meaning it has
     * modifications that can be reverted.
     *
     * @param status the SVN status object
     * @return {@code true} if ready for revert; {@code false} otherwise
     */
    public static boolean isReadyForRevert(ISVNStatus status) {
        return isTextModified(status) || isAdded(status) || isDeleted(status)
                || isMissing(status) || isReplaced(status) || isPropModified(status)
                || isTextConflicted(status) || isPropConflicted(status);
    }
}
