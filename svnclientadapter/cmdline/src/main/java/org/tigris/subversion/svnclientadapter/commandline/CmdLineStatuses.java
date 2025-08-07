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

package org.tigris.subversion.svnclientadapter.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;

/**
 * Digests <code>status</code> and <code>info</code> information from
 * the command-line.
 *
 * @author Cédric Chabanois (cchabanois at no-log.org)
 * @author Daniel Rall
 */
public class CmdLineStatuses {
    private CmdLineInfoPart[] cmdLineInfoParts;
    private CmdLineStatusPart[] cmdLineStatusParts;
    private ISVNStatus[] cmdLineStatuses;

    CmdLineStatuses(String infoLines, CmdLineStatusPart[] cmdLineStatusParts) {
        this.cmdLineStatusParts = cmdLineStatusParts;

        if (infoLines.length() == 0) {
            cmdLineInfoParts = new CmdLineInfoPart[0];
        } else {
            String[] parts = CmdLineInfoPart.parseInfoParts(infoLines);
            cmdLineInfoParts = new CmdLineInfoPart[parts.length];
            for (int i = 0; i < parts.length; i++) {
                cmdLineInfoParts[i] = new CmdLineInfoPart(parts[i]);
            }
        }
        this.cmdLineStatuses = buildStatuses();
    }

    CmdLineStatuses(CmdLineInfoPart[] cmdLineInfoParts,
                    CmdLineStatusPart[] cmdLineStatusParts) {
        this.cmdLineInfoParts = cmdLineInfoParts;
        this.cmdLineStatusParts = cmdLineStatusParts;
        this.cmdLineStatuses = buildStatuses();
    }

    /**
     * Procures status objects for the {@link #cmdLineStatuses}
     * instance field.
     */
    private ISVNStatus[] buildStatuses() {
        processExternalStatuses(cmdLineStatusParts);
        List statuses = new LinkedList();
        for (int i = 0; i < cmdLineStatusParts.length; i++) {
            CmdLineStatusPart cmdLineStatusPart = cmdLineStatusParts[i];
            File absPath = cmdLineStatusPart.getFile();
            if (cmdLineStatusPart == null || !cmdLineStatusPart.isManaged()) {
                boolean isIgnored = false;
                if (cmdLineStatusPart != null) {
                    isIgnored = SVNStatusKind.IGNORED.equals(cmdLineStatusPart.getTextStatus());
                }
                statuses.add(new SVNStatusUnversioned(absPath, isIgnored));
            } else {
                CmdLineInfoPart cmdLineInfoPart =
                        getCorrespondingInfoPart(absPath);
                if (cmdLineInfoPart != null) {
                    statuses.add(new CmdLineStatusComposite(cmdLineStatusPart,
                            cmdLineInfoPart));
                }
            }
        }

        return (ISVNStatus[])
                statuses.toArray(new ISVNStatus[statuses.size()]);
    }

    /**
     * Retrieves the {@link CmdLineInfoPart} instance that corresponds to the specified absolute path.
     * </p>
     * This method searches through the array of available {@code cmdLineInfoParts} and returns the one
     * whose associated file matches the provided {@code absPath}.
     * </p>
     * Comparison is done using {@link File#equals(Object)}, which means both files must refer to the
     * same absolute path in the file system.
     *
     * @param absPath the absolute {@link File} path for which to retrieve status information.
     * @return the corresponding {@link CmdLineInfoPart} if found; {@code null} otherwise.
     */
    private CmdLineInfoPart getCorrespondingInfoPart(File absPath) {
        for (int i = 0; i < cmdLineInfoParts.length; i++) {
            if (absPath.equals(cmdLineInfoParts[i].getFile())) {
                return cmdLineInfoParts[i];
            }
        }
        return null;
    }

    /**
     * Post-processes `svn:externals` statuses to unify their representation.
     *
     * <p>The SVN command-line client may return multiple status entries for externals,
     * including minimal ones with {@code textStatus=EXTERNAL} and null URLs, or complete ones with
     * {@code textStatus=NORMAL} and a full set of data. This method adjusts all matching
     * entries to consistently mark them as {@code EXTERNAL}.</p>
     *
     * @param statuses the array of {@link CmdLineStatusPart} to process
     * @return the updated array of {@link CmdLineStatusPart} with unified external statuses
     */
    protected CmdLineStatusPart[] processExternalStatuses(CmdLineStatusPart[] statuses) {
        //Collect indexes of external statuses
        List externalStatusesIndexes = new ArrayList();
        for (int i = 0; i < statuses.length; i++) {
            if (SVNStatusKind.EXTERNAL.equals(statuses[i].getTextStatus())) {
                externalStatusesIndexes.add(new Integer(i));
            }
        }

        if (externalStatusesIndexes.isEmpty()) {
            return statuses;
        }

        //Check the "second" externals so their textStatus is actually external
        for (Iterator iter = externalStatusesIndexes.iterator(); iter.hasNext(); ) {
            int index = ((Integer) iter.next()).intValue();
            CmdLineStatusPart cmdLineStatusPart = statuses[index];
            for (int i = 0; i < statuses.length; i++) {
                if ((statuses[i].getPath() != null) && (statuses[i].getPath().equals(cmdLineStatusPart.getPath()))) {
                    statuses[i].setTextStatus(SVNStatusKind.EXTERNAL);
                }
            }
        }

        return statuses;
    }

    /**
     * Returns the {@link ISVNStatus} at the specified index.
     *
     * @param i the index of the status to return
     * @return the {@link ISVNStatus} at the specified index
     */
    public ISVNStatus get(int i) {
        return cmdLineStatuses[i];
    }

    /**
     * Returns the number of status entries held by this object.
     *
     * @return the number of {@link ISVNStatus} objects
     */
    public int size() {
        return cmdLineStatuses.length;
    }

    /**
     * Returns all status entries as an array.
     *
     * @return an array of {@link ISVNStatus} objects
     */
    public ISVNStatus[] toArray() {
        return cmdLineStatuses;
    }

}
