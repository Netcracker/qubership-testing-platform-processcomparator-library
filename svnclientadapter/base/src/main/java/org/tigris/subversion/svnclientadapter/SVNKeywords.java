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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Represents the set of SVN keywords enabled for a particular resource.
 * <p>
 * SVN keywords are used for embedding metadata such as author, revision, and date into files.
 */
public class SVNKeywords {

    /** Keyword for the last changed date */
    public static final String LAST_CHANGED_DATE = "LastChangedDate";

    /** Alias for {@link #LAST_CHANGED_DATE} */
    public static final String DATE = "Date";

    /** Keyword for the last changed revision */
    public static final String LAST_CHANGED_REVISION = "LastChangedRevision";

    /** Alias for {@link #LAST_CHANGED_REVISION} */
    public static final String REV = "Rev";

    /** Keyword for the author of the last change */
    public static final String LAST_CHANGED_BY = "LastChangedBy";

    /** Alias for {@link #LAST_CHANGED_BY} */
    public static final String AUTHOR = "Author";

    /** Keyword for the HEAD URL of the repository */
    public static final String HEAD_URL = "HeadURL";

    /** Alias for {@link #HEAD_URL} */
    public static final String URL = "URL";

    /** Keyword for a summary of revision, date, author, and path */
    public static final String ID = "Id";

    private boolean lastChangedDate = false;
    private boolean lastChangedRevision = false;
    private boolean lastChangedBy = false;
    private boolean headUrl = false;
    private boolean id = false;

    /**
     * Creates an empty {@code SVNKeywords} instance with all keywords disabled.
     */
    public SVNKeywords() {

    }

    /**
     * Constructs an instance by parsing a space-separated list of enabled keywords.
     *
     * @param keywords the space-separated list of keyword names (can be {@code null})
     */
    public SVNKeywords(String keywords) {
        if (keywords == null) {
            return;
        }
        StringTokenizer st = new StringTokenizer(keywords, " ");

        while (st.hasMoreTokens()) {
            String keyword = st.nextToken();
            if ((keyword.equals(SVNKeywords.HEAD_URL)) || (keyword.equals(SVNKeywords.URL))) {
                headUrl = true;
            } else if (keyword.equals(SVNKeywords.ID)) {
                id = true;
            } else if ((keyword.equals(SVNKeywords.LAST_CHANGED_BY)) || (keyword.equals(SVNKeywords.AUTHOR))) {
                lastChangedBy = true;
            } else if ((keyword.equals(SVNKeywords.LAST_CHANGED_DATE)) || (keyword.equals(SVNKeywords.DATE))) {
                lastChangedDate = true;
            } else if ((keyword.equals(SVNKeywords.LAST_CHANGED_REVISION)) || (keyword.equals(SVNKeywords.REV))) {
                lastChangedRevision = true;
            }
        }
    }

    /**
     * Constructs an instance using explicit keyword flags.
     *
     * @param lastChangedDate     whether the last changed date keyword is enabled
     * @param lastChangedRevision whether the last changed revision keyword is enabled
     * @param lastChangedBy       whether the last changed by keyword is enabled
     * @param headUrl             whether the head URL keyword is enabled
     * @param id                  whether the ID keyword is enabled
     */
    public SVNKeywords(boolean lastChangedDate, boolean lastChangedRevision,
                       boolean lastChangedBy, boolean headUrl, boolean id) {
        this.lastChangedDate = lastChangedDate;
        this.lastChangedRevision = lastChangedRevision;
        this.lastChangedBy = lastChangedBy;
        this.headUrl = headUrl;
        this.id = id;
    }

    /**
     * Checks if the {@link #HEAD_URL} keyword is enabled.
     *
     * @return {@code true} if HEAD_URL is enabled; {@code false} otherwise
     */
    public boolean isHeadUrl() {
        return headUrl;
    }

    /**
     * Checks if the {@link #ID} keyword is enabled.
     *
     * @return {@code true} if ID is enabled; {@code false} otherwise
     */
    public boolean isId() {
        return id;
    }

    /**
     * Checks if the {@link #LAST_CHANGED_BY} keyword is enabled.
     *
     * @return {@code true} if LAST_CHANGED_BY is enabled; {@code false} otherwise
     */
    public boolean isLastChangedBy() {
        return lastChangedBy;
    }

    /**
     * Checks if the {@link #LAST_CHANGED_DATE} keyword is enabled.
     *
     * @return {@code true} if LAST_CHANGED_DATE is enabled; {@code false} otherwise
     */
    public boolean isLastChangedDate() {
        return lastChangedDate;
    }

    /**
     * Checks if the {@link #LAST_CHANGED_REVISION} keyword is enabled.
     *
     * @return {@code true} if LAST_CHANGED_REVISION is enabled; {@code false} otherwise
     */
    public boolean isLastChangedRevision() {
        return lastChangedRevision;
    }

    /**
     * Returns a list of the currently enabled SVN keywords.
     *
     * @return a list of SVN keyword strings
     */
    public List getKeywordsList() {
        ArrayList list = new ArrayList();
        if (headUrl) {
            list.add(HEAD_URL);
        }
        if (id) {
            list.add(ID);
        }
        if (lastChangedBy) {
            list.add(LAST_CHANGED_BY);
        }
        if (lastChangedDate) {
            list.add(LAST_CHANGED_DATE);
        }
        if (lastChangedRevision) {
            list.add(LAST_CHANGED_REVISION);
        }
        return list;
    }

    /**
     * Returns a space-separated string of enabled keywords.
     *
     * @return a string listing the enabled keywords
     */
    public String toString() {
        String result = "";

        for (Iterator it = getKeywordsList().iterator(); it.hasNext(); ) {
            String keyword = (String) it.next();
            result += keyword;
            if (it.hasNext()) {
                result += ' ';
            }
        }
        return result;
    }

    /**
     * Enables or disables the {@link #HEAD_URL} keyword.
     *
     * @param b {@code true} to enable; {@code false} to disable
     */
    public void setHeadUrl(boolean b) {
        headUrl = b;
    }

    /**
     * Enables or disables the {@link #ID} keyword.
     *
     * @param b {@code true} to enable; {@code false} to disable
     */
    public void setId(boolean b) {
        id = b;
    }

    /**
     * Enables or disables the {@link #LAST_CHANGED_BY} keyword.
     *
     * @param b {@code true} to enable; {@code false} to disable
     */
    public void setLastChangedBy(boolean b) {
        lastChangedBy = b;
    }

    /**
     * Enables or disables the {@link #LAST_CHANGED_DATE} keyword.
     *
     * @param b {@code true} to enable; {@code false} to disable
     */
    public void setLastChangedDate(boolean b) {
        lastChangedDate = b;
    }

    /**
     * Enables or disables the {@link #LAST_CHANGED_REVISION} keyword.
     *
     * @param b {@code true} to enable; {@code false} to disable
     */
    public void setLastChangedRevision(boolean b) {
        lastChangedRevision = b;
    }

}
