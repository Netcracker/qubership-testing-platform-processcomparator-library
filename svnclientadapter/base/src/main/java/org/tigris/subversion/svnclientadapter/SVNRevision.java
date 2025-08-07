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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.tigris.subversion.svnclientadapter.utils.SafeSimpleDateFormat;

/**
 * Class to specify a revision in a svn command.
 * This class has been copied directly from javahl and renamed to SVNRevision
 * the static method getRevision has then been added to the class
 */
public class SVNRevision {
    // See chapter 3 section 3.3 of the SVN book for valid date strings
    /**
     * Default date format used to serialize revision dates.
     */
    protected static final SafeSimpleDateFormat dateFormat = new SafeSimpleDateFormat("yyyyMMdd'T'HHmmssZ");

    /**
     * Type of the revision (see {@link Kind}).
     */
    protected int revKind;

    public SVNRevision(int kind) {
        revKind = kind;
    }

    public int getKind() {
        return revKind;
    }

    public String toString() {
        switch (revKind) {
            case Kind.unspecified:
                return "START";
            case Kind.base:
                return "BASE";
            case Kind.committed:
                return "COMMITTED";
            case Kind.head:
                return "HEAD";
            case Kind.previous:
                return "PREV";
            case Kind.working:
                return "WORKING";
            default:
                // Do nothing for unknown revision kind
                break;
        }
        return super.toString();
    }

    public boolean equals(Object target) {
        if (this == target) {
            return true;
        }
        if (!(target instanceof SVNRevision)) {
            return false;
        }

        return ((SVNRevision) target).revKind == revKind;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return revKind;
    }

    /**
     * Constant representing the latest repository revision.
     */
    public static final SVNRevision HEAD = new SVNRevision(Kind.head);

    /**
     * Constant representing an unspecified revision.
     */
    public static final SVNRevision START = new SVNRevision(Kind.unspecified);

    /**
     * Constant representing the committed revision.
     */
    public static final SVNRevision COMMITTED = new SVNRevision(Kind.committed);

    /**
     * Constant representing the previous revision.
     */
    public static final SVNRevision PREVIOUS = new SVNRevision(Kind.previous);

    /**
     * Constant representing the base revision from .svn/entries.
     */
    public static final SVNRevision BASE = new SVNRevision(Kind.base);

    /**
     * Constant representing the working copy revision (with local mods).
     */
    public static final SVNRevision WORKING = new SVNRevision(Kind.working);

    /**
     * Invalid revision number constant (-1).
     */
    public static final int SVN_INVALID_REVNUM = -1;

    /**
     * Constant representing an invalid revision object.
     */
    public static final SVNRevision.Number INVALID_REVISION = new SVNRevision.Number(SVN_INVALID_REVNUM);

    /**
     * Represents a numeric revision (e.g., revision number 1234).
     */
    public static class Number extends SVNRevision implements Comparable {
        /**
         * The numeric revision number.
         */
        protected long revNumber;

        /**
         * Constructs a new {@code Number} revision.
         *
         * @param number the revision number
         */
        public Number(long number) {
            super(Kind.number);
            revNumber = number;
        }

        /**
         * Returns the numeric revision number.
         *
         * @return the revision number
         */
        public long getNumber() {
            return revNumber;
        }

        /**
         * Returns string representation of the revision number.
         *
         * @return the revision number as a string
         */
        public String toString() {
            return Long.toString(revNumber);
        }

        /**
         * Checks equality with another object.
         *
         * @param target the object to compare to
         * @return true if both are of type {@code Number} and have the same revision
         */
        public boolean equals(Object target) {
            return super.equals(target) && ((Number) target).revNumber == revNumber;
        }

        /**
         * Returns a hash code based on the revision number.
         *
         * @return the hash code
         */
        public int hashCode() {
            return (int) revNumber;
        }

        /**
         * Compares this revision number to another.
         *
         * @param target the target object to compare with
         * @return -1, 0, or 1 depending on comparison
         */
        public int compareTo(Object target) {
            SVNRevision.Number compare = (SVNRevision.Number) target;
            return Long.compare(revNumber, compare.getNumber());
        }
    }

    /**
     * Represents a revision specified by date.
     */
    public static class DateSpec extends SVNRevision {

        /**
         * The revision date.
         */
        protected Date revDate;

        /**
         * Constructs a new {@code DateSpec} revision.
         *
         * @param date the date specifying the revision
         */
        public DateSpec(Date date) {
            super(Kind.date);
            revDate = date;
        }

        /**
         * Returns the date of this revision.
         *
         * @return the revision date
         */
        public Date getDate() {
            return revDate;
        }

        /**
         * Returns the string representation of the revision date.
         *
         * @return the formatted date string
         */
        public String toString() {
            return '{' + dateFormat.format(revDate) + '}';
        }

        /**
         * Checks if this revision equals another object.
         *
         * @param target the object to compare to
         * @return true if both are of type {@code DateSpec} and dates match
         */
        public boolean equals(Object target) {
            return super.equals(target) && ((DateSpec) target).revDate.equals(revDate);
        }

        /**
         * Returns hash code based on revision date.
         *
         * @return the hash code
         */
        public int hashCode() {
            return revDate.hashCode();
        }

    }

    /**
     * Various ways of specifying revisions.
     * <p>
     * Note:
     * In contexts where local mods are relevant, the `working' kind
     * refers to the uncommitted "working" revision, which may be modified
     * with respect to its base revision.  In other contexts, `working'
     * should behave the same as `committed' or `current'.
     */
    public static final class Kind {
        /**
         * No revision information given.
         */
        public static final int unspecified = 0;

        /**
         * revision given as number.
         */
        public static final int number = 1;

        /**
         * revision given as date.
         */
        public static final int date = 2;

        /**
         * rev of most recent change.
         */
        public static final int committed = 3;

        /**
         * (rev of most recent change) - 1.
         */
        public static final int previous = 4;

        /**
         * .svn/entries current revision.
         */
        public static final int base = 5;

        /**
         * current, plus local mods.
         */
        public static final int working = 6;

        /**
         * repository youngest.
         */
        public static final int head = 7;

    }

    /**
     * Parses a revision string and returns the corresponding {@link SVNRevision} instance.
     *
     * <p>The {@code revision} parameter can represent:
     * <ul>
     *   <li>A revision keyword (case-insensitive):
     *     <ul>
     *       <li>{@code HEAD} – the latest revision in the repository</li>
     *       <li>{@code BASE} – the base revision of the item's working copy</li>
     *       <li>{@code COMMITED} – the revision in which the item was last committed</li>
     *       <li>{@code PREV} – the revision prior to the item's last committed revision</li>
     *     </ul>
     *   </li>
     *   <li>A positive integer representing a revision number</li>
     *   <li>A date string in the format defined by {@code dateFormat}</li>
     * </ul>
     *
     * @param revision   the revision string to parse
     * @param dateFormat the date format to use when parsing dates;
     *                   if {@code null}, the default format {@code MM/dd/yyyy hh:mm a} is used
     * @return a corresponding {@link SVNRevision} object
     * @throws ParseException if the revision cannot be parsed into a valid {@code SVNRevision}
     */
    public static SVNRevision getRevision(String revision, SimpleDateFormat dateFormat) throws ParseException {

        if ((revision == null) || (revision.equals(""))) {
            return null;
        }

        // try special KEYWORDS
        if (revision.compareToIgnoreCase("HEAD") == 0) {
            return SVNRevision.HEAD; // latest in repository
        } else if (revision.compareToIgnoreCase("BASE") == 0) {
            return new SVNRevision(SVNRevision.Kind.base); // base revision of item's working copy
        } else if (revision.compareToIgnoreCase("COMMITED") == 0) {
            return new SVNRevision(SVNRevision.Kind.committed); // revision of item's last commit
        } else if (revision.compareToIgnoreCase("PREV") == 0) {
            return new SVNRevision(SVNRevision.Kind.previous);
        }

        // try revision number
        try {
            int revisionNumber = Integer.parseInt(revision);
            if (revisionNumber >= 0) {
                return new SVNRevision.Number(revisionNumber);
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);
        }

        // try date
        SimpleDateFormat df = (dateFormat != null) ? dateFormat : new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);

        try {
            Date revisionDate = df.parse(revision);
            return new SVNRevision.DateSpec(revisionDate);
        } catch (ParseException e) {
            // ignore
        }

        throw new ParseException("Invalid revision. Revision should be a number, a date in "
                + df.toPattern() + " format or HEAD, BASE, COMMITED or PREV", 0);
    }

    /**
     * Parses a revision string using the default US date format {@code MM/dd/yyyy hh:mm a}.
     *
     * <p>See {@link #getRevision(String, SimpleDateFormat)} for supported formats.</p>
     *
     * @param revision the revision string to parse
     * @return a corresponding {@link SVNRevision} object
     * @throws ParseException if the revision cannot be parsed
     */
    public static SVNRevision getRevision(String revision) throws ParseException {
        return getRevision(revision, new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US));
    }

}
