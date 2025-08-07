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
 * Represents a lock on a Subversion-managed item, as returned by lock-related operations.
 * A lock is used to prevent concurrent modification of a versioned file, typically for
 * exclusive access workflows. This interface provides information about the lock owner,
 * lock token, and relevant timestamps.
 */
public interface ISVNLock {

    /**
     * Returns the username of the user who owns the lock.
     *
     * @return the lock owner as a {@link String}, or {@code null} if unknown.
     */
    String getOwner();

    /**
     * Returns the path of the locked item within the repository or working copy.
     *
     * @return the absolute path to the locked item.
     */
    String getPath();

    /**
     * Returns the lock token associated with the locked item.
     * <p>
     * This token is required to release or refresh the lock and uniquely identifies the lock instance.
     *
     * @return the lock token.
     */
    String getToken();

    /**
     * Returns the comment or description that was provided when the lock was created.
     *
     * @return the lock comment, or {@code null} if none was provided.
     */
    String getComment();

    /**
     * Returns the date and time when the lock was created.
     *
     * @return the lock creation timestamp.
     */
    Date getCreationDate();

    /**
     * Returns the expiration date of the lock, if any.
     * <p>
     * If the lock does not expire automatically, this may return {@code null}.
     *
     * @return the lock expiration date, or {@code null} if the lock does not expire.
     */
    Date getExpirationDate();
}
