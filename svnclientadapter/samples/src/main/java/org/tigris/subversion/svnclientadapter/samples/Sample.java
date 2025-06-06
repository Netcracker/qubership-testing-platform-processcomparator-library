/*******************************************************************************
 * Copyright (c) 2004, 2006 svnClientAdapter project and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.svnclientadapter.samples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.svnkit.SvnKitClientAdapterFactory;;;

/**
 * A very simple sample
 */
public class Sample {

    public static class NotifyListener implements ISVNNotifyListener {
        public void setCommand(int cmd) {
            // the command that is being executed. See ISVNNotifyListener.Command
            // ISVNNotifyListener.Command.ADD for example
        }

        public void logMessage(String message) {
        }

        public void logCommandLine(String message) {
            // the command line used
            // Note that the notification handler suppresses these for
            // many of the simple SVN commands that might be run a lot
            // in order to generate less noise.
        }

        public void logError(String message) {
            // when an error occurs
        }

        public void logRevision(long revision, String path) {
            // when command completes against revision
        }

        public void logCompleted(String message) {
            // when command completed
        }

        public void onNotify(File path, SVNNodeKind nodeKind) {
            // each time the status of a file or directory changes (file added, reverted ...)
            // nodeKind is SVNNodeKind.FILE or SVNNodeKind.DIR

            // this is the function we use in subclipse to know which files need to be refreshed
            // So commands like checkout/update/commit will generate these messages but
            // command like ls/log/status/diff will not

        }
    }

    public void setup() throws SVNClientException {
        // You could register JavaHL or CommandLine
        // or all of them and then choose which one
        // to use later.  For ease of demo just
        // using the SVNKit pure-Java adapter

        SvnKitClientAdapterFactory.setup();
    }

    public void run() {
        // register the factories
        try {
            setup();
        } catch (SVNClientException e) {
            return;
        }

        ISVNClientAdapter svnClient;

        try {
            // If you registered multiple factories, this would return
            // the best one available - JavaHL > SVNKit > CmdLine
            String bestClientType = SVNClientAdapterFactory.getPreferredSVNClientType();

            // This code uses the returned value, but you could just use
            // a String constant here too
            svnClient = SVNClientAdapterFactory.createSVNClient(bestClientType);
        } catch (SVNClientException e) {
            return;
        }


        // set username and password if necessary based on repository
        svnClient.setUsername("guest");
        svnClient.setPassword(" ");


        //	add a listener if you wish. This is almost always necessary
        // if you are doing anything interesting as the listener receives
        // all the feedback from the API

        NotifyListener listener = new Sample.NotifyListener();
        svnClient.addNotifyListener(listener);

        try {
            //	use the svn commands
            String SVNROOT = "http://svn.apache.org/repos/asf/subversion/trunk";
            boolean RECURSE = false;

            // This does equivalent of svn ls command.
            ISVNDirEntry[] list = svnClient.getList(new SVNUrl(SVNROOT), SVNRevision.HEAD, RECURSE);
            for (int i = 0; i < list.length; i++) {
            }

            String SVNFILE = SVNROOT + "/COMMITTERS";

            // This does equivalent of svn cat command to read the file from repository
            InputStream is = svnClient.getContent(new SVNUrl(SVNFILE), SVNRevision.HEAD);

            byte[] bytes = new byte[100];
            is.read(bytes);

            // This does equivalent of svn log command to fetch history
            ISVNLogMessage[] logMessages = svnClient.getLogMessages(new SVNUrl(SVNROOT), SVNRevision.HEAD,
                    SVNRevision.HEAD, new SVNRevision.Number(0), false, false, 1, false);
            for (int i = 0; i < logMessages.length; i++) {
            }

        } catch (IOException e) {
        } catch (SVNClientException e) {
        } finally {
            // You should dispose svnClient when done with it to free native handles
            svnClient.dispose();
        }
    }


    public static void main(String[] args) {
        Sample sample = new Sample();
        sample.run();
        System.exit(0);
    }
}
