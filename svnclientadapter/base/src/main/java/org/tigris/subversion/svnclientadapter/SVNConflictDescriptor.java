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
 * The description of a merge conflict, encountered during
 * merge, update, or switch operations.
 * <p>
 * This class was copied and adapted from JavaHL's {@code ConflictDescriptor}.
 */
public class SVNConflictDescriptor {
    private String path;

    /**
     * Conflict kind variable.
     *
     * @see Kind
     */
    private int conflictKind;

    /**
     * Node kind variable.
     */
    private int nodeKind;

    private String propertyName;
    private boolean isBinary;
    private String mimeType;

    private int action;
    private int reason;
    private int operation;

    private SVNConflictVersion srcLeftVersion;
    private SVNConflictVersion srcRightVersion;

    // File paths, present only when the conflict involves the merging
    // of two files descended from a common ancestor, here are the
    // paths of up to four fulltext files that can be used to
    // interactively resolve the conflict.
    private String basePath;
    private String theirPath;
    private String myPath;
    private String mergedPath;

    /**
     * Constructs a full conflict descriptor.
     *
     * @param path            the path of the conflicted item
     * @param conflictKind    the kind of conflict
     * @param nodeKind        the kind of node involved
     * @param propertyName    name of the conflicting property (if applicable)
     * @param isBinary        whether the file is binary
     * @param mimeType        MIME type of the item
     * @param action          conflict action (see {@link Action})
     * @param reason          reason for conflict (see {@link Reason})
     * @param operation       operation causing the conflict (see {@link Operation})
     * @param srcLeftVersion  source version (left)
     * @param srcRightVersion source version (right)
     * @param basePath        path to the base file
     * @param theirPath       path to "their" version
     * @param myPath          path to "my" version
     * @param mergedPath      path to merged result
     */
    public SVNConflictDescriptor(String path, int conflictKind, int nodeKind,
                                 String propertyName, boolean isBinary,
                                 String mimeType, int action, int reason, int operation,
                                 SVNConflictVersion srcLeftVersion, SVNConflictVersion srcRightVersion,
                                 String basePath, String theirPath,
                                 String myPath, String mergedPath) {
        this.path = path;
        this.conflictKind = conflictKind;
        this.nodeKind = nodeKind;
        this.propertyName = propertyName;
        this.isBinary = isBinary;
        this.mimeType = mimeType;
        this.action = action;
        this.reason = reason;
        this.srcLeftVersion = srcLeftVersion;
        this.srcRightVersion = srcRightVersion;
        this.operation = operation;
        this.basePath = basePath;
        this.theirPath = theirPath;
        this.myPath = myPath;
        this.mergedPath = mergedPath;
    }

    /**
     * Constructs a minimal conflict descriptor.
     *
     * @param path            path of the conflicted item
     * @param action          conflict action
     * @param reason          reason for conflict
     * @param operation       operation that caused the conflict
     * @param srcLeftVersion  left source version
     * @param srcRightVersion right source version
     */
    public SVNConflictDescriptor(String path,
                                 int action,
                                 int reason,
                                 int operation,
                                 SVNConflictVersion srcLeftVersion,
                                 SVNConflictVersion srcRightVersion) {
        this.path = path;
        this.action = action;
        this.reason = reason;
        this.operation = operation;
        this.srcLeftVersion = srcLeftVersion;
        this.srcRightVersion = srcRightVersion;
    }

    /**
     * Returns the path to the conflicted item.
     *
     * @return path to the conflicted item
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the kind of conflict.
     *
     * @return the kind of conflict (see {@link Kind})
     */
    public int getConflictKind() {
        return conflictKind;
    }

    /**
     * Returns the kind of node (e.g. file or directory).
     *
     * @return the node kind
     */
    public int getNodeKind() {
        return nodeKind;
    }

    /**
     * Returns the name of the conflicting property, if any.
     *
     * @return the property name or {@code null}
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Checks if the conflicted file is binary.
     *
     * @return {@code true} if binary, {@code false} otherwise
     */
    public boolean isBinary() {
        return isBinary;
    }

    /**
     * Returns the MIME type of the conflicted item.
     *
     * @return MIME type string
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the action that caused the conflict.
     *
     * @return conflict action (see {@link Action})
     */
    public int getAction() {
        return action;
    }

    /**
     * Returns the reason why the conflict occurred.
     *
     * @return conflict reason (see {@link Reason})
     */
    public int getReason() {
        return reason;
    }

    /**
     * Checks whether the conflict is a tree conflict.
     *
     * @return {@code true} if tree conflict, {@code false} otherwise
     */
    public boolean isTreeConflict() {
        return reason == SVNConflictDescriptor.Reason.deleted
                || reason == SVNConflictDescriptor.Reason.moved_away
                || reason == SVNConflictDescriptor.Reason.missing
                || reason == SVNConflictDescriptor.Reason.obstructed;
    }

    /**
     * Returns the operation that caused the conflict.
     *
     * @return the conflict operation (see {@link Operation})
     */
    public int getOperation() {
        return operation;
    }

    /**
     * Returns the left (their) version involved in the conflict.
     *
     * @return the left version
     */
    public SVNConflictVersion getSrcLeftVersion() {
        return srcLeftVersion;
    }

    /**
     * Returns the right (my) version involved in the conflict.
     *
     * @return the right version
     */
    public SVNConflictVersion getSrcRightVersion() {
        return srcRightVersion;
    }

    /**
     * Returns the path to the base file (if available).
     *
     * @return path to base file or {@code null}
     */
    public String getBasePath() {
        return basePath;
    }

    /**
     * Returns the path to the "their" version of the file.
     *
     * @return path to their version
     */
    public String getTheirPath() {
        return theirPath;
    }

    /**
     * Returns the path to the "my" version of the file.
     *
     * @return path to my version
     */
    public String getMyPath() {
        return myPath;
    }

    /**
     * Returns the path to the merged file.
     *
     * @return path to merged result
     */
    public String getMergedPath() {
        return mergedPath;
    }

    /**
     * Represents the type of a conflict (e.g., text or property).
     */
    public final class Kind {

        /**
         * Default constructor.
         */
        public Kind () {
            super();
        }

        /** Text conflict (e.g. line-based difference) */
        public static final int text = 0;

        /** Property conflict */
        public static final int property = 1;
    }

    /**
     * Constants for conflict actions.
     */
    public final class Action {

        /**
         * Default constructor.
         */
        public Action (){
            super();
        }

        /** Edit attempt */
        public static final int edit = 0;
        /** Add attempt */
        public static final int add = 1;
        /** Delete attempt */
        public static final int delete = 2;
    }

    /**
     * Constants for reasons that conflicts occur.
     */
    public final class Reason {

        /**
         * Default constructor.
         */
        public Reason (){
            super();
        }

        /**
         * Local edits are already present.
         */
        public static final int edited = 0;

        /**
         * Another object is in the way.
         */
        public static final int obstructed = 1;

        /**
         * Object is already schedule-delete.
         */
        public static final int deleted = 2;

        /**
         * Object is unknown or missing.
         */
        public static final int missing = 3;

        /**
         * Object is unversioned.
         */
        public static final int unversioned = 4;

        /**
         * Object is already added or schedule-add.
         */
        public static final int added = 5;

        /**
         * Object is already replaced.
         */
        public static final int replaced = 6;

        /**
         * Object is moved away.
         */
        public static final int moved_away = 7;

        /**
         * Object is moved here.
         */
        public static final int moved_here = 8;
    }

    /**
     * Constants representing operations during which conflicts occurred.
     */
    public final class Operation {

        /**
         * Default constructor.
         */
        public Operation (){
            super();
        }

        /** No operation specified. */
        public static final int _none = 0;

        /** Conflict occurred during an update operation. */
        public static final int _update = 1;

        /** Conflict occurred during a switch operation. */
        public static final int _switch = 2;

        /** Conflict occurred during a merge operation. */
        public static final int _merge = 3;
    }
}
