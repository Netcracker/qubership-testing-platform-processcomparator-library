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

import java.util.Date;

/**
 * An interface defining a single subversion commit with log message,
 * author, date and paths changed within the commit.
 *
 * @author Philip Schatz <a href="mailto:schatzp@purdue.edu">schatzp@purdue.edu</a>
 */
public interface ISVNLogMessage {
    /**
     * The standard Subversion property name for the commit author.
     */
    String AUTHOR = "svn:author";

    /**
     * The standard Subversion property name for the commit log message.
     */
    String MESSAGE = "svn:log";

    /**
     * The standard Subversion property name for the commit date.
     */
    String DATE = "svn:date";

    /**
     * Custom property name used by svnclientadapter to store commit time in microseconds.
     */
    String TIME_MICROS = "svnclientadapter:timemicros";

    /**
     * Returns the revision number.
     *
     * @return the revision number
     */
    SVNRevision.Number getRevision();

    /**
     * Returns the author of the commit.
     *
     * @return the author of the commit
     */
    String getAuthor();

    /**
     * Returns the time of the commit.
     *
     * @return the time of the commit measured in the number of
     *         microseconds since 00:00:00 January 1, 1970 UTC
     */
    long getTimeMicros();

    /**
     * Returns the time of the commit.
     *
     * @return the time of the commit measured in the number of
     *         milliseconds since 00:00:00 January 1, 1970 UTC
     */
    long getTimeMillis();

    /**
     * Returns the date of the commit.
     *
     * @return the date of the commit
     */
    Date getDate();

    /**
     * Return the log message text.
     *
     * @return the log message text
     */
    String getMessage();

    /**
     * Returns the changes items by this commit.
     *
     * @return the changes items by this commit
     */
    ISVNLogMessageChangePath[] getChangedPaths();

    /**
     * Returns the number of child log messages.  When merge-sensitive
     * log option was specified.
     *
     * @return the number of revisions merged by this commit
     */

    long getNumberOfChildren();

    /**
     * Returns the child log messages.  When merge-sensitive
     * log option was specified.
     *
     * @return the revisions merged by this commit
     */
    ISVNLogMessage[] getChildMessages();

    /**
     * Add a child logMessage to an existing message.
     *
     * @param msg - child log message
     */
    void addChild(ISVNLogMessage msg);

    /**
     * Indicates whether this log message has any child log messages.
     *
     * @return {@code true} if the log message has children; {@code false} otherwise
     */
    boolean hasChildren();
}
