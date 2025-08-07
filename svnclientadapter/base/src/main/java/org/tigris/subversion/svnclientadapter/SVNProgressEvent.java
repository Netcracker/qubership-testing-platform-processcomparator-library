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
 * Represents a progress event for long-running SVN operations.
 * <p>
 * This class is typically used to report the amount of progress made
 * and the total amount of work expected, if known.
 */
public class SVNProgressEvent {

    /** The current amount of progress. */
    private long progress;

    /** The total amount of work expected, or {@link #UNKNOWN} if not known. */
    private long total;

    /** Constant indicating that the total work is unknown. */
    public static final long UNKNOWN = -1;

    /**
     * Constructs a new SVNProgressEvent instance.
     *
     * @param progress the current progress made
     * @param total    the total amount of work expected, or {@link #UNKNOWN} if unknown
     */
    public SVNProgressEvent(long progress, long total) {
        super();
        this.progress = progress;
        this.total = total;
    }

    /**
     * Returns the current progress value.
     *
     * @return the amount of progress completed
     */
    public long getProgress() {
        return progress;
    }

    /**
     * Returns the total amount of work expected.
     *
     * @return the total work, or {@link #UNKNOWN} if not known
     */
    public long getTotal() {
        return total;
    }

}
