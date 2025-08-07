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
 * Defines a callback interface for resolving conflicts encountered during
 * Subversion operations such as merge, update, or switch.
 *
 * <p>Implementations of this interface allow client applications to programmatically
 * resolve file or property conflicts when they occur.</p>
 */
public interface ISVNConflictResolver {
    /**
     * The callback method invoked for each conflict during a
     * merge/update/switch operation.
     *
     * @param descrip A description of the conflict.
     * @return The result of any conflict resolution.
     * @throws SVNClientException If an error occurs.
     */
    public SVNConflictResult resolve(SVNConflictDescriptor descrip) throws SVNClientException;

    /**
     * From JavaHL.
     */
    public final class Choice {

        /**
         * Default constructor.
         */
        public Choice() {
            super();
        }
        /**
         * User did nothing; conflict remains.
         */
        public static final int postpone = 0;

        /**
         * User chooses the base file.
         */
        public static final int chooseBase = 1;

        /**
         * User chooses the repository file.
         */
        public static final int chooseTheirsFull = 2;

        /**
         * User chooses own version of file.
         */
        public static final int chooseMineFull = 3;

        /**
         * Resolve the conflict by choosing the incoming (repository)
         * version of the object (for conflicted hunks only).
         */
        public static final int chooseTheirs = 4;

        /**
         * Resolve the conflict by choosing own (local) version of the
         * object (for conflicted hunks only).
         */
        public static final int chooseMine = 5;

        /**
         * Resolve the conflict by choosing the merged object
         * (potentially manually edited).
         */
        public static final int chooseMerged = 6;
    }

}
