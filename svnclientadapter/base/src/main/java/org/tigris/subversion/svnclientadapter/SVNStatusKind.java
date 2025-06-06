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
 * Base class for enumerating the possible types for a <code>Status</code>.
 */
public class SVNStatusKind {
    private final int kind;
    private boolean treeConflicted;

    private static final int none = 0;
    private static final int normal = 1;
    private static final int modified = 2;
    private static final int added = 3;
    private static final int deleted = 4;
    private static final int unversioned = 5;
    private static final int missing = 6;
    private static final int replaced = 7;
    private static final int merged = 8;
    private static final int conflicted = 9;
    private static final int obstructed = 10;
    private static final int ignored = 11;
    private static final int incomplete = 12;
    private static final int external = 13;

    /**
     * does not exist.
     */
    public static final SVNStatusKind NONE = new SVNStatusKind(none);

    /**
     * exists, but uninteresting.
     */
    public static final SVNStatusKind NORMAL = new SVNStatusKind(normal);

    /**
     * is scheduled for addition.
     */
    public static final SVNStatusKind ADDED = new SVNStatusKind(added);

    /**
     * under v.c., but is missing.
     */
    public static final SVNStatusKind MISSING = new SVNStatusKind(missing);

    /**
     * a directory doesn't contain a complete entries list.
     */
    public static final SVNStatusKind INCOMPLETE = new SVNStatusKind(incomplete);

    /**
     * scheduled for deletion.
     */
    public static final SVNStatusKind DELETED = new SVNStatusKind(deleted);

    /**
     * was deleted and then re-added.
     */
    public static final SVNStatusKind REPLACED = new SVNStatusKind(replaced);

    /**
     * text or props have been modified.
     */
    public static final SVNStatusKind MODIFIED = new SVNStatusKind(modified);

    /**
     * local mods received repos mods.
     */
    public static final SVNStatusKind MERGED = new SVNStatusKind(merged);

    /**
     * local mods received conflicting repos mods.
     */
    public static final SVNStatusKind CONFLICTED = new SVNStatusKind(conflicted);

    /**
     * an unversioned resource is in the way of the versioned resource.
     */
    public static final SVNStatusKind OBSTRUCTED = new SVNStatusKind(obstructed);

    /**
     * a resource marked as ignored.
     */
    public static final SVNStatusKind IGNORED = new SVNStatusKind(ignored);

    /**
     * an unversioned path populated by an svn:external property.
     */
    public static final SVNStatusKind EXTERNAL = new SVNStatusKind(external);

    /**
     * is not a versioned thing in this wc.
     */
    public static final SVNStatusKind UNVERSIONED = new SVNStatusKind(unversioned);

    //Constructors

    /**
     * Constructs an {@code SVNStatusKind} instance corresponding to the given integer value.
     * </p>
     * This constructor is typically used internally to create a status kind from its numeric representation,
     * such as values returned by native Subversion libraries.
     * </p>
     *
     * @param kind the integer value representing the status kind
     * @throws IllegalArgumentException if the provided value does not correspond to a valid status kind
     */
    private SVNStatusKind(int kind) throws IllegalArgumentException {
        this.kind = kind;
    }

    /**
     * Returns the integer representation of this status kind.
     * </p>
     * This value corresponds to the internal identifier used by the Subversion implementation.
     * </p>
     *
     * @return an integer value representing this {@code SVNStatusKind}
     */
    public int toInt() {
        return kind;
    }

    /**
     * Sets whether the status kind is affected by a tree conflict.
     *
     * @param treeConflicted {@code true} if this item is tree-conflicted; {@code false} otherwise
     */
    public void setTreeConflicted(boolean treeConflicted) {
        this.treeConflicted = treeConflicted;
    }

    /**
     * Checks whether this status kind represents a tree conflict.
     *
     * @return {@code true} if this status kind is tree-conflicted; {@code false} otherwise
     */
    public boolean hasTreeConflict() {
        return treeConflicted;
    }

    /**
     * Returns the {@link SVNStatusKind} corresponding to the given integer value.
     * </p>
     * The integer must match one of the known internal constants for status kinds,
     * such as {@code normal}, {@code added}, {@code deleted}, etc.
     * </p>
     *
     * @param kind the integer value of the status kind
     * @return the corresponding {@code SVNStatusKind}, or {@code null} if no match is found
     */
    public static SVNStatusKind fromInt(int kind) {
        switch (kind) {
            case none:
                return NONE;
            case normal:
                return NORMAL;
            case added:
                return ADDED;
            case missing:
                return MISSING;
            case deleted:
                return DELETED;
            case replaced:
                return REPLACED;
            case modified:
                return MODIFIED;
            case merged:
                return MERGED;
            case conflicted:
                return CONFLICTED;
            case ignored:
                return IGNORED;
            case incomplete:
                return INCOMPLETE;
            case external:
                return EXTERNAL;
            case unversioned:
                return UNVERSIONED;
            case obstructed:
                return OBSTRUCTED;
            default:
                return null;
        }
    }

    /**
     * Converts a string representation of a Subversion status to its corresponding {@link SVNStatusKind} enum.
     * </p>
     * Supported status kinds include:
     * <ul>
     *   <li>{@code "none"} or {@code "non-svn"} — not versioned</li>
     *   <li>{@code "normal"} — no local modifications</li>
     *   <li>{@code "added"} — scheduled for addition</li>
     *   <li>{@code "missing"} — item is missing</li>
     *   <li>{@code "deleted"} — scheduled for deletion</li>
     *   <li>{@code "replaced"} — item is replaced</li>
     *   <li>{@code "modified"} — item has been modified locally</li>
     *   <li>{@code "merged"} — item has been merged</li>
     *   <li>{@code "conflicted"} — item is in a conflict state</li>
     *   <li>{@code "ignored"} — item is ignored by Subversion</li>
     *   <li>{@code "incomplete"} — directory is incomplete</li>
     *   <li>{@code "external"} — item is an external definition</li>
     *   <li>{@code "unversioned"} — item is not under version control</li>
     *   <li>{@code "obstructed"} — item is obstructed in the working copy</li>
     * </ul>
     *
     * @param kind the string representation of the status kind
     * @return the matching {@code SVNStatusKind} constant
     * @throws IllegalArgumentException if the provided string does not match any known status kind
     */
    public static SVNStatusKind fromString(String kind) {
        if ("none".equals(kind) || "non-svn".equals(kind)) {
            return NONE;
        } else if ("normal".equals(kind)) {
            return NORMAL;
        } else if ("added".equals(kind)) {
            return ADDED;
        } else if ("missing".equals(kind)) {
            return MISSING;
        } else if ("deleted".equals(kind)) {
            return DELETED;
        } else if ("replaced".equals(kind)) {
            return REPLACED;
        } else if ("modified".equals(kind)) {
            return MODIFIED;
        } else if ("merged".equals(kind)) {
            return MERGED;
        } else if ("conflicted".equals(kind)) {
            return CONFLICTED;
        } else if ("ignored".equals(kind)) {
            return IGNORED;
        } else if ("incomplete".equals(kind)) {
            return INCOMPLETE;
        } else if ("external".equals(kind)) {
            return EXTERNAL;
        } else if ("unversioned".equals(kind)) {
            return UNVERSIONED;
        } else if ("obstructed".equals(kind)) {
            return OBSTRUCTED;
        } else {
            throw new IllegalArgumentException("Unknown status " + kind);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        switch (kind) {
            case none:
                return "non-svn";
            case normal:
                return "normal";
            case added:
                return "added";
            case missing:
                return "missing";
            case deleted:
                return "deleted";
            case replaced:
                return "replaced";
            case modified:
                return "modified";
            case merged:
                return "merged";
            case conflicted:
                return "conflicted";
            case ignored:
                return "ignored";
            case incomplete:
                return "incomplete";
            case external:
                return "external";
            case obstructed:
                return "obstructed";
            case unversioned:
            default:
                return "unversioned";
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof SVNStatusKind)) {
            return false;
        }
        return ((SVNStatusKind) obj).kind == kind;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new Integer(kind).hashCode();
    }
}
