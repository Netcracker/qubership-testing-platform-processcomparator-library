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

import java.io.InputStream;
import java.util.Date;

/**
 * An interface defining the result of a call to svn blame/annotate. For each
 * line in the file, last modification data are returned.
 */
public interface ISVNAnnotations {

    /**
     * Get the date of the last change for the given <code>lineNumber</code>.
     *
     * @param lineNumber    line number
     * @return              date of last change
     */
    public abstract Date getChanged(int lineNumber);

    /**
     * Get the revision of the last change for the given <code>lineNumber</code>.
     *
     * @param lineNumber    line number
     * @return              the revision of last change
     */
    public abstract long getRevision(int lineNumber);

    /**
     * Get the author of the last change for the given <code>lineNumber</code>.
     *
     * @param lineNumber    line number
     * @return              the author of last change or null
     */
    public abstract String getAuthor(int lineNumber);

    /**
     * Get the content (line itself) of the given <code>lineNumber</code>.
     *
     * @param lineNumber    line number
     * @return              the line content
     */
    public abstract String getLine(int lineNumber);

    /**
     * Get an input stream providing the content of the file being annotated.
     *
     * @return an inputstream of the content of the file
     */
    public abstract InputStream getInputStream();

    /**
     * Get the number of annotated lines.
     *
     * @return number of lines of file being annotated
     */
    public abstract int numberOfLines();
}
