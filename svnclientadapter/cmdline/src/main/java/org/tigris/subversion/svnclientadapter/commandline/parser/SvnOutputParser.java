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

package org.tigris.subversion.svnclientadapter.commandline.parser;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineNotify;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineNotifyAction;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineNotifyStatus;

/**
 * parser for the output of svn.
 *
 * @author Cédric Chabanois (cchabanois at no-log.org)
 */
public class SvnOutputParser {
    private static final Logger log = Logger.getLogger(SvnOutputParser.class.getName());

    private static final String NEWLINE = "\n\r";

    // See see subversion/clients/cmdline/notify.c for possible outputs
    // we depend on javahl because it would be a waste to duplicate the notification actions 
    private SvnActionRE[] svnActionsRE = new SvnActionRE[]{
            new SvnActionRE("Skipped missing target: '(.+)'", CmdLineNotifyAction.skip, CmdLineNotifyStatus.missing,
                    new String[]{SvnActionRE.PATH}),
            new SvnActionRE("Skipped '(.+)'", CmdLineNotifyAction.skip, SvnActionRE.PATH),
            new SvnActionRE("D    ([^ ].+)", CmdLineNotifyAction.update_delete, SvnActionRE.PATH),
            new SvnActionRE("A    ([^ ].+)", CmdLineNotifyAction.update_add, SvnActionRE.PATH),
            new SvnActionRE("Restored '(.+)'", CmdLineNotifyAction.restore, SvnActionRE.PATH),
            new SvnActionRE("Reverted '(.+)'", CmdLineNotifyAction.revert, SvnActionRE.PATH),
            new SvnActionRE("Failed to revert '(.+)' -- try updating instead\\.", CmdLineNotifyAction.failed_revert,
                    SvnActionRE.PATH),
            new SvnActionRE("Resolved conflicted state of '(.+)'", CmdLineNotifyAction.resolved, SvnActionRE.PATH),
            new SvnActionRE("A  (bin)  ([^ ].+)", CmdLineNotifyAction.add, SvnActionRE.PATH),
            new SvnActionRE("A         ([^ ].+)", CmdLineNotifyAction.add, SvnActionRE.PATH),
            new SvnActionRE("D         ([^ ].+)", CmdLineNotifyAction.delete, SvnActionRE.PATH),
            new SvnActionRE("([CGU ])([CGU ])   (.+)", CmdLineNotifyAction.update_update,
                    new String[]{SvnActionRE.CONTENTSTATE, SvnActionRE.PROPSTATE, SvnActionRE.PATH}),
            new SvnActionRE("Fetching external item into '(.+)'", CmdLineNotifyAction.update_external,
                    SvnActionRE.PATH),
            new SvnActionRE("Exported external at revision (\\d+)\\.", CmdLineNotifyAction.update_completed,
                    SvnActionRE.REVISION),
            new SvnActionRE("Exported revision (\\d+)\\.", CmdLineNotifyAction.update_completed,
                    SvnActionRE.REVISION),
            new SvnActionRE("Checked out external at revision (\\d+)\\.", CmdLineNotifyAction.update_completed,
                    SvnActionRE.REVISION),
            new SvnActionRE("Checked out revision (\\d+)\\.", CmdLineNotifyAction.update_completed,
                    SvnActionRE.REVISION),
            new SvnActionRE("Updated external to revision (\\d+)\\.", CmdLineNotifyAction.update_completed,
                    SvnActionRE.REVISION),
            new SvnActionRE("Updated to revision (\\d+)\\.", CmdLineNotifyAction.update_completed,
                    SvnActionRE.REVISION),
            new SvnActionRE("External at revision (\\d+)\\.", CmdLineNotifyAction.update_completed,
                    SvnActionRE.REVISION),
            new SvnActionRE("At revision (\\d+)\\.", CmdLineNotifyAction.update_completed, SvnActionRE.REVISION),
            new SvnActionRE("External export complete\\.", CmdLineNotifyAction.update_completed,
                    CmdLineNotifyStatus.inapplicable, CmdLineNotifyStatus.inapplicable),
            new SvnActionRE("Export complete\\.", CmdLineNotifyAction.update_completed,
                    CmdLineNotifyStatus.inapplicable, CmdLineNotifyStatus.inapplicable),
            new SvnActionRE("External checkout complete\\.", CmdLineNotifyAction.update_completed,
                    CmdLineNotifyStatus.inapplicable, CmdLineNotifyStatus.inapplicable),
            new SvnActionRE("Checkout complete\\.", CmdLineNotifyAction.update_completed,
                    CmdLineNotifyStatus.inapplicable, CmdLineNotifyStatus.inapplicable),
            new SvnActionRE("External update complete\\.", CmdLineNotifyAction.update_completed,
                    CmdLineNotifyStatus.inapplicable, CmdLineNotifyStatus.inapplicable),
            new SvnActionRE("Update complete\\.", CmdLineNotifyAction.update_completed,
                    CmdLineNotifyStatus.inapplicable, CmdLineNotifyStatus.inapplicable),
            new SvnActionRE("Performing status on external item at '(.+)'", CmdLineNotifyAction.status_external,
                    SvnActionRE.PATH),
            new SvnActionRE("Status against revision:  *(\\d+)", CmdLineNotifyAction.status_completed,
                    SvnActionRE.REVISION),
            new SvnActionRE("Sending        (.+)", CmdLineNotifyAction.commit_modified, SvnActionRE.PATH),
            new SvnActionRE("Adding  (bin)  (.+)", CmdLineNotifyAction.commit_added, SvnActionRE.PATH),
            new SvnActionRE("Adding         (.+)", CmdLineNotifyAction.commit_added, SvnActionRE.PATH),
            new SvnActionRE("Deleting       (.+)", CmdLineNotifyAction.commit_deleted, SvnActionRE.PATH),
            new SvnActionRE("Replacing      (.+)", CmdLineNotifyAction.commit_replaced, SvnActionRE.PATH),
            new SvnActionRE("Transmitting file data \\.*", CmdLineNotifyAction.commit_postfix_txdelta),
            new SvnActionRE("'(.+)' locked by user.*", CmdLineNotifyAction.locked),
            new SvnActionRE("'(.+)' unlocked.*", CmdLineNotifyAction.unlocked),

            // this one is not a notification
            new SvnActionRE("Committed revision (\\d+)\\.", -1, SvnActionRE.REVISION)
    };
    private List listeners = new LinkedList();

    /**
     * Registers a new command line notification listener.
     *
     * @param listener the listener to be added; must not be null
     */
    public void addListener(CmdLineNotify listener) {
        listeners.add(listener);
    }

    /**
     * Unregisters an existing command line notification listener.
     *
     * @param listener the listener to be removed; must not be null
     */
    public void removeListener(CmdLineNotify listener) {
        listeners.remove(listener);
    }

    /**
     * Parses the output produced by an SVN command-line operation and notifies
     * registered listeners about the interpreted SVN actions.
     * </p>
     * The given output may contain multiple lines, each corresponding to a different action.
     * Each line is matched against a list of known regular expressions representing
     * various SVN actions. If a match is found, the corresponding listeners are notified.
     * If no match is found, the line is logged as a warning.</p>
     *
     * @param svnOutput the raw output text from the SVN command-line interface;
     *                  must not be null
     */
    public void parse(String svnOutput) {
        StringTokenizer st = new StringTokenizer(svnOutput, NEWLINE);

        while (st.hasMoreTokens()) {
            String line = st.nextToken();

            synchronized (this) {
                // only one client must access a given SvnActionRE at a time
                SvnActionRE svnActionRE = getMatchingSvnActionRE(line);
                if (svnActionRE != null) {
                    notifyListeners(svnActionRE);
                } else {
                    // if we don't find a matching svnActionRE, we just log it
                    log.warning("Can't find a svn action for svn output line : " + line);
                }
            }
        }
    }

    /**
     * Attempts to match the given line of SVN output against known SVN action
     * patterns and returns the corresponding {@link SvnActionRE} if a match is found.
     * </p>
     * If multiple patterns match the line, this is considered an error and
     * only the first matching action will be returned while logging a severe warning.
     * </p>
     * @param line the line of SVN output to match; must not be null
     * @return the matching {@link SvnActionRE} instance, or {@code null} if no match is found
     */
    private SvnActionRE getMatchingSvnActionRE(String line) {
        SvnActionRE result = null;
        for (int i = 0; i < svnActionsRE.length; i++) {
            if (svnActionsRE[i].match(line)) {
                if (result != null) {
                    log.severe("Multiple regular expressions match : " + line);
                } else {
                    result = svnActionsRE[i];
                }
            }
        }
        return result;
    }

    private void notifyListeners(SvnActionRE svnActionRE) {
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
            CmdLineNotify listener = (CmdLineNotify) it.next();
            listener.onNotify(
                    svnActionRE.getPath(),
                    svnActionRE.getAction(),
                    SVNNodeKind.UNKNOWN.toInt(),   // we don't know the kind
                    (String) null,       // we don't know the mimeType
                    svnActionRE.getContentState(),
                    svnActionRE.getPropStatus(),
                    svnActionRE.getRevision());
        }
    }


}
