/*******************************************************************************
 * Copyright (c) 2007 svnClientAdapter project and others.
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

/**
 * Defines depth constants for Subversion operations,
 * representing how deep a command should recurse into a directory structure.
 */
public class Depth {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Depth() {
        // Utility class — no instances allowed.
    }

    /**
     * Depth undetermined or ignored.
     */
    public static final int unknown = 0;

    /**
     * Exclude (remove, whatever) directory D.
     */
    public static final int exclude = 1;

    /**
     * Just the named directory D, no entries.
     */
    public static final int empty = 2;

    /**
     * D + its file children, but not subdirs.
     */
    public static final int files = 3;

    /**
     * D + immediate children (D and its entries).
     */
    public static final int immediates = 4;

    /**
     * D + all descendants (full recursion from D).
     */
    public static final int infinity = 5;

    /**
     * Converts a boolean recurse flag to the corresponding depth constant.
     *
     * @param recurse if {@code true}, returns {@link #infinity}; otherwise returns {@link #files}
     * @return one of the {@code Depth} constants representing recursion depth
     */
    public static final int fromRecurse(boolean recurse) {
        if (recurse) {
            return infinity;
        } else {
            return files;
        }
    }
}
