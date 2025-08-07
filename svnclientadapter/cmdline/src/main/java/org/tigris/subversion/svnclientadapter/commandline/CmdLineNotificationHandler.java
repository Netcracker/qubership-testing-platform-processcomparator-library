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

import java.util.StringTokenizer;

import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNNotificationHandler;

/**
 * Command line specific extension to generic notification handler.
 *
 * @author Cédric Chabanois (cchabanois@ifrance.com)
 */
public class CmdLineNotificationHandler extends SVNNotificationHandler {

    /**
     * Default constructor.
     */
    public CmdLineNotificationHandler() {
        super();
    }

    /**
     * Log the supplied command line exception as Error.
     *
     * @param e an exception to log
     */
    public void logException(CmdLineException e) {
        StringTokenizer st = new StringTokenizer(e.getMessage(), Helper.NEWLINE);
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            for (Object notifyListener : notifylisteners) {
                ISVNNotifyListener listener = (ISVNNotifyListener) notifyListener;
                listener.logError(line);
            }
        }
    }

}
