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
 * Represents a specific version of a conflicted item in a Subversion repository.
 * <p>
 * This class contains details such as the repository URL, revision, path within the repository,
 * and the node kind (file, directory, etc).
 */
public class SVNConflictVersion {

    private String reposURL;
    private long pegRevision;
    private String pathInRepos;
    private int nodeKind;

    /**
     * Constructs a new {@code SVNConflictVersion} instance.
     *
     * @param reposURL     the repository URL
     * @param pegRevision  the peg revision of the version
     * @param pathInRepos  the path within the repository
     * @param nodeKind     the kind of node (e.g. file, directory)
     */
    public SVNConflictVersion(String reposURL, long pegRevision, String pathInRepos, int nodeKind) {
        this.reposURL = reposURL;
        this.pegRevision = pegRevision;
        this.pathInRepos = pathInRepos;
        this.nodeKind = nodeKind;
    }

    /**
     * @return the repository URL
     */
    public String getReposURL() {
        return reposURL;
    }

    /**
     * Get revision number.
     *
     * @return the peg revision number
     */
    public long getPegRevision() {
        return pegRevision;
    }

    /**
     * Get path within the repository.
     *
     * @return the path within the repository
     */
    public String getPathInRepos() {
        return pathInRepos;
    }

    /**
     * Get the kind of node.
     *
     * @return the kind of node (see {@link NodeKind})
     */
    public int getNodeKind() {
        return nodeKind;
    }

    /**
     * Returns a string representation of this {@code SVNConflictVersion}.
     *
     * @return a human-readable description of the conflict version
     */
    public String toString() {
        StringBuffer sb = new StringBuffer("(");
        switch (nodeKind) {
            case NodeKind.none:
                sb.append("none");
                break;
            case NodeKind.file:
                sb.append("file");
                break;
            case NodeKind.directory:
                sb.append("dir");
                break;
            default:
                sb.append(nodeKind);
                break;
        }
        sb.append(") ").append(reposURL).append("/").append(pathInRepos).append("@").append(pegRevision);
        return sb.toString();
    }

    /**
     * Constants representing the kind of a node involved in a conflict.
     */
    public final class NodeKind {

        /**
         * Default constructor.
         */
        private NodeKind() {
            super();
        }

        /**
         * No node kind specified or unknown kind.
         */
        public static final int none = 0;

        /**
         * The node is a file.
         */
        public static final int file = 1;

        /**
         * The node is a directory.
         */
        public static final int directory = 2;
    }

}
