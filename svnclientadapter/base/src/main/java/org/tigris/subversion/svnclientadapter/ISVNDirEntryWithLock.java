/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.tigris.subversion.svnclientadapter;

/**
 * Represents a directory entry in a Subversion repository along with its associated lock information.
 * <p>
 * This interface is typically used in operations that list directory contents
 * and want to include information about file locks.
 */
public interface ISVNDirEntryWithLock {

    /**
     * Returns the directory entry (metadata such as name, kind, revision, etc.).
     *
     * @return the {@link ISVNDirEntry} representing the directory or file
     */
    public ISVNDirEntry getDirEntry();

    /**
     * Returns the lock information associated with the entry, if any.
     *
     * @return the {@link ISVNLock} object representing the lock, or {@code null} if the entry is not locked
     */
    public ISVNLock getLock();

}
