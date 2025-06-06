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
 * The result returned by the callback API used to handle conflicts
 * encountered during merge/update/switch operations.
 */
public class SVNConflictResult {
    /**
     * Nothing done to resolve the conflict; conflict remains.
     */
    public static final int postpone = 0;

    /**
     * Resolve the conflict by choosing the base file.
     */
    public static final int chooseBase = 1;

    /**
     * Resolve the conflict by choosing the incoming (repository)
     * version of the object.
     */
    public static final int chooseTheirsFull = 2;

    /**
     * Resolve the conflict by choosing own (local) version of the
     * object.
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

    /**
     * A value corresponding to the
     * <code>svn_wc_conflict_choice_t</code> enum.
     */
    private int choice;

    /**
     * The path to the result of a merge, or <code>null</code>.
     */
    private String mergedPath;

    /**
     * Create a new conflict result instace.
     */
    public SVNConflictResult(int choice, String mergedPath) {
        this.choice = choice;
        this.mergedPath = mergedPath;
    }

    /**
     * Get value corresponding to the <code>svn_wc_conflict_choice_t</code> enum.
     *
     * @return A value corresponding to the
     *         <code>svn_wc_conflict_choice_t</code> enum.
     */
    public int getChoice() {
        return choice;
    }

    /**
     * Get The path to the result of a merge, or <code>null</code>.
     *
     * @return The path to the result of a merge, or <code>null</code>.
     */
    public String getMergedPath() {
        return mergedPath;
    }
}
