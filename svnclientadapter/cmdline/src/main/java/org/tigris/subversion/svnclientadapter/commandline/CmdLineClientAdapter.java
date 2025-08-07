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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.tigris.subversion.svnclientadapter.AbstractClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNAnnotations;
import org.tigris.subversion.svnclientadapter.ISVNConflictResolver;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.ISVNDirEntryWithLock;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback;
import org.tigris.subversion.svnclientadapter.ISVNMergeInfo;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.ISVNProgressListener;
import org.tigris.subversion.svnclientadapter.ISVNProperty;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.ISVNStatusCallback;
import org.tigris.subversion.svnclientadapter.SVNBaseDir;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNDiffSummary;
import org.tigris.subversion.svnclientadapter.SVNNotificationHandler;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.tigris.subversion.svnclientadapter.SVNRevisionRange;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNStatusUnversioned;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * <p>Implements a {@code ISVNClientAdapter} using the
 * Command line client. This expects the svn
 * executible to be in the path.</p>
 *
 * @author Philip Schatz (schatz at tigris)
 * @author C~dric Chabanois (cchabanois at no-log.org)
 */
public class CmdLineClientAdapter extends AbstractClientAdapter {

    /** The notification handler for processing SVN command line output. */
    protected final CmdLineNotificationHandler notificationHandler;

    /** The command-line interface to the SVN client. */
    protected final SvnCommandLine cmd;

    /** The multi-argument command-line interface to the SVN client. */
    protected final SvnMultiArgCommandLine cmdMulti;

    /** The command-line interface for administrative SVN operations. */
    protected final SvnAdminCommandLine svnAdminCmd;

    /** Cached version of the SVN client, retrieved lazily. */
    protected String version = null;

    private static boolean availabilityCached = false;
    private static boolean available;
    private static String dirName;

    /**
     * Creates a new {@code CmdLineClientAdapter} with the specified notification handler.
     *
     * @param notificationHandler the notification handler to use
     */
    public CmdLineClientAdapter(CmdLineNotificationHandler notificationHandler) {
        this(notificationHandler,
                new SvnCommandLine("svn", notificationHandler),
                new SvnMultiArgCommandLine("svn", notificationHandler),
                new SvnAdminCommandLine("svnadmin", notificationHandler));
    }

    /**
     * Creates a new {@code CmdLineClientAdapter} with full command-line clients specified.
     *
     * @param notificationHandler the notification handler
     * @param cmd the command-line SVN client
     * @param multiCmd the multi-argument command-line SVN client
     * @param adminCmd the command-line SVN admin client
     */
    protected CmdLineClientAdapter(CmdLineNotificationHandler notificationHandler,
                                   SvnCommandLine cmd,
                                   SvnMultiArgCommandLine multiCmd,
                                   SvnAdminCommandLine adminCmd) {
        super();
        this.notificationHandler = notificationHandler;
        this.cmd = cmd;
        this.cmdMulti = multiCmd;
        this.svnAdminCmd = adminCmd;
    }

    public boolean isThreadsafe() {
        return false;
    }

    /**
     * Checks whether the SVN command-line client is available and supported.
     *
     * @return {@code true} if the SVN client is available and supported, {@code false} otherwise
     */
    public static boolean isAvailable() {
        // availabilityCached flag must be reset if location of client changes
        if (!availabilityCached) {
            // this will need to be fixed when path to svn will be customizable
            SvnCommandLine cmd = new SvnCommandLine("svn", new CmdLineNotificationHandler());
            try {
                String version = cmd.version();
                int i = version.indexOf(System.getProperty("line.separator")); // NOI18N
                version = version.substring(0, i);
                available = true;
                available &= version.indexOf("version 0.") == -1;
                available &= version.indexOf("version 1.0") == -1;
                available &= version.indexOf("version 1.1") == -1;
                available &= version.indexOf("version 1.2") == -1;
            } catch (Exception e) {
                available = false;
            }
            availabilityCached = true;
        }
        return available;
    }

    /**
     * Retrieves the version of the installed SVN command-line client.
     *
     * <p>This method executes the {@code svn --version} command via the underlying
     * command-line interface and extracts the first line of the output, which typically contains
     * the version information.</p>
     *
     * <p>The output is cached after the first successful call to avoid repeated executions.</p>
     *
     * @return the SVN client version string, for example {@code "svn, version 1.14.1 (r1886195)"}
     * @throws SVNClientException if the version command fails to execute or its output cannot be parsed
     */
    public String getVersion() throws SVNClientException {
        if (version != null) {
            return version;
        }
        try {
            // we don't want to log this ...
            notificationHandler.disableLog();
            version = cmd.version();
            int i = version.indexOf(System.getProperty("line.separator")); // NOI18N
            version = version.substring(0, i);
            return version;
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        } finally {
            notificationHandler.enableLog();
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#addNotifyListener(
     * org.tigris.subversion.subclipse.client.ISVNClientNotifyListener)
     */
    public void addNotifyListener(ISVNNotifyListener listener) {
        notificationHandler.add(listener);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#removeNotifyListener(
     * org.tigris.subversion.subclipse.client.ISVNClientNotifyListener)
     */
    public void removeNotifyListener(ISVNNotifyListener listener) {
        notificationHandler.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getNotificationHandler()
     */
    public SVNNotificationHandler getNotificationHandler() {
        return notificationHandler;
    }

    private boolean isManaged(File file) {
        return file.isDirectory() && isManagedDir(file)
                || file.getParentFile() != null && isManagedDir(file.getParentFile());
    }

    private boolean isManagedDir(File dir) {
        // a directory that has a .svn dir or that has a parent directory with a .svn dir is versioned
        File entries = new File(dir, getAdminDirectoryName() + "/entries");
        return entries.exists() || dir.getParentFile() != null && isManagedDir(dir.getParentFile());
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#getStatus(java.io.File[])
     */
    public ISVNStatus[] getStatus(File[] files) throws SVNClientException {

        ISVNStatus[] statuses = new ISVNStatus[files.length];

        // all files (and dirs) that are in nonmanaged dirs are unversioned
        ArrayList pathsList = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (isManaged(file)) {
                pathsList.add(toString(file));
            } else {
                statuses[i] = new SVNStatusUnversioned(file, false);
            }
        }
        String[] paths = (String[]) pathsList.toArray(new String[pathsList.size()]);

        // we must do a svn status and svn info only on resources that are in versioned dirs 
        // because otherwise svn will stop after the first "svn: 'resource' is not a working copy" 
        CmdLineStatuses cmdLineStatuses;
        try {
            CmdLineStatusPart[] cmdLineStatusParts = getCmdStatuses(paths, false, true, false, false);
            List targetsInfo = new ArrayList(cmdLineStatusParts.length);
            for (int i = 0; i < cmdLineStatusParts.length; i++) {
                if (cmdLineStatusParts[i].isManaged()) {
                    targetsInfo.add(cmdLineStatusParts[i].getFile().toString());
                }
            }
            String cmdLineInfoStrings = cmd.info((String[]) targetsInfo.toArray(new String[targetsInfo.size()]),
                    null, null);

            cmdLineStatuses = new CmdLineStatuses(cmdLineInfoStrings, cmdLineStatusParts);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }

        for (int i = 0; i < cmdLineStatuses.size(); i++) {
            ISVNStatus status = cmdLineStatuses.get(i);
            for (int j = 0; j < files.length; j++) {
                if (files[j].getAbsoluteFile().equals(status.getFile())) {
                    statuses[j] = status;
                }
            }
        }
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i] == null) {
                statuses[i] = new SVNStatusUnversioned(files[i], false);
            }
        }

        return statuses;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getStatus(java.io.File, boolean, boolean)
     */
    public ISVNStatus[] getStatus(File path, boolean descend, boolean getAll)
            throws SVNClientException {
        return getStatus(path, descend, getAll, false);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getStatus(
     * java.io.File, boolean, boolean, boolean)
     */
    public ISVNStatus[] getStatus(File path,
                                  boolean descend,
                                  boolean getAll,
                                  boolean contactServer)
            throws SVNClientException {
        return getStatus(path, descend, getAll, contactServer, false);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getStatus(
     * java.io.File, boolean, boolean, boolean, boolean)
     */
    public ISVNStatus[] getStatus(File path,
                                  boolean descend,
                                  boolean getAll,
                                  boolean contactServer,
                                  boolean ignoreExternals)
            throws SVNClientException {
        try {
            // first we get the status of the files
            CmdLineStatusPart[] cmdLineStatusParts = getCmdStatuses(new File[]{path}, descend, getAll,
                    contactServer, ignoreExternals);
            List targetsInfo = new ArrayList(cmdLineStatusParts.length);
            List nonManagedParts = new ArrayList();
            for (int i = 0; i < cmdLineStatusParts.length; i++) {
                if (cmdLineStatusParts[i].isManaged()) {
                    targetsInfo.add(cmdLineStatusParts[i].getFile().toString());
                } else {
                    nonManagedParts.add(new Integer(i));
                }
            }

            // this is not enough, so we get info from the files
            String infoLinesString = cmd.info((String[]) targetsInfo.toArray(new String[targetsInfo.size()]),
                    null, null);

            String[] parts = CmdLineInfoPart.parseInfoParts(infoLinesString);
            CmdLineInfoPart[] cmdLineInfoParts = new CmdLineInfoPart[parts.length];
            for (int i = 0; i < parts.length; i++) {
                cmdLineInfoParts[i] = new CmdLineInfoPart(parts[i]);
            }

            CmdLineInfoPart[] allInfoParts = new CmdLineInfoPart[cmdLineStatusParts.length];
            //Put the unversioned at corrent indexes.
            for (Iterator iter = nonManagedParts.iterator(); iter.hasNext(); ) {
                Integer indexOfNonManaged = (Integer) iter.next();
                allInfoParts[indexOfNonManaged.intValue()] = CmdLineInfoPart.createUnversioned(null);
            }
            //Fill the remaining indexes with versioned infos.
            for (int i = 0; i < cmdLineInfoParts.length; i++) {
                for (int j = i; j < allInfoParts.length; j++) {
                    if (allInfoParts[j] == null) {
                        allInfoParts[j] = cmdLineInfoParts[i];
                        break;
                    }
                }
            }

            CmdLineStatuses cmdLineStatuses = new CmdLineStatuses(cmdLineInfoParts, cmdLineStatusParts);

            return cmdLineStatuses.toArray();

        } catch (CmdLineException e) {
            if (e.getMessage().trim().matches("svn:.*is not a working copy.*")) {
                return new ISVNStatus[]{new SVNStatusUnversioned(path)};
            }
            throw SVNClientException.wrapException(e);
        }
    }

    @Override
    public ISVNStatus[] getStatus(File path, boolean descend, boolean getAll,
                                  boolean contactServer, boolean ignoreExternals,
                                  ISVNStatusCallback callback) throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    public ISVNStatus[] getStatus(File path,
                                  boolean descend,
                                  boolean getAll,
                                  boolean contactServer,
                                  boolean ignoreExternals,
                                  boolean noIgnore,
                                  ISVNStatusCallback callback)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getSingleStatus(java.io.File)
     */
    public ISVNStatus getSingleStatus(File path)
            throws SVNClientException {
        return getStatus(new File[]{path})[0];
    }

    private ISVNDirEntry[] getList(String target, SVNRevision rev, boolean recursive)
            throws SVNClientException {

        byte[] listXml;
        try {
            listXml = cmd.list(target, toString(rev), recursive);
            return CmdLineRemoteDirEntry.createDirEntries(listXml);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#getList(
     * java.net.URL, org.tigris.subversion.subclipse.client.ISVNRevision, boolean)
     */
    public ISVNDirEntry[] getList(SVNUrl svnUrl, SVNRevision revision, boolean recurse)
            throws SVNClientException {
        return getList(toString(svnUrl), revision, recurse);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getList(
     * java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, boolean)
     */
    public ISVNDirEntry[] getList(File path, SVNRevision revision,
                                  boolean recurse) throws SVNClientException {
        return getList(toString(path), revision, recurse);
    }

    public ISVNDirEntry[] getList(SVNUrl url, SVNRevision revision,
                                  SVNRevision pegRevision, boolean recurse) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
        return null;
    }

    public ISVNDirEntry[] getList(File path, SVNRevision revision,
                                  SVNRevision pegRevision, boolean recurse) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
        return null;
    }

    public ISVNDirEntryWithLock[] getListWithLocks(SVNUrl url,
                                                   SVNRevision revision, SVNRevision pegRevision, boolean recurse)
            throws SVNClientException {
        ISVNDirEntry[] entries = getList(url, revision, pegRevision, recurse);
        ISVNDirEntryWithLock[] entriesWithLocks = new ISVNDirEntryWithLock[entries.length];
        for (int i = 0; i < entries.length; i++) {
            entriesWithLocks[i] = new CmdLineRemoteDirEntryWithLock(entries[i], null);
        }
        return entriesWithLocks;
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getDirEntry(
     * org.tigris.subversion.svnclientadapter.SVNUrl, org.tigris.subversion.svnclientadapter.SVNRevision)
     */
    public ISVNDirEntry getDirEntry(SVNUrl url, SVNRevision revision)
            throws SVNClientException {

        // list give the DirEntrys of the elements of a directory or the DirEntry
        // of a file
        ISVNDirEntry[] entries = getList(url.getParent(), revision, false);

        String expectedPath = url.getLastPathSegment();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getPath().equals(expectedPath)) {
                return entries[i];
            }
        }
        return null; // not found
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getDirEntry(
     * java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision)
     */
    public ISVNDirEntry getDirEntry(File path, SVNRevision revision)
            throws SVNClientException {
        // list give the DirEntrys of the elements of a directory or the DirEntry
        // of a file
        ISVNDirEntry[] entries = getList(path.getParentFile(), revision, false);

        String expectedPath = path.getName();
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].getPath().equals(expectedPath)) {
                return entries[i];
            }
        }
        return null; // not found
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#remove(java.io.File[], boolean)
     */
    public void remove(File[] files, boolean force) throws SVNClientException {
        String[] paths = new String[files.length];
        try {
            for (int i = 0; i < files.length; i++) {
                paths[i] = files[i].toString();
            }
            cmd.delete(paths, null, force);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#remove(java.net.URL[], java.lang.String)
     */
    public void remove(SVNUrl[] urls, String message) throws SVNClientException {
        String[] urlsStrings = new String[urls.length];
        for (int i = 0; i < urls.length; i++) {
            urlsStrings[i] = urls[i].toString();
        }
        try {
            cmd.delete(urlsStrings, message, false);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#revert(java.io.File, boolean)
     */
    public void revert(File file, boolean recursive) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(file));
            cmd.revert(new String[]{toString(file)}, recursive);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }

    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#getContent(
     * java.net.SVNUrl, org.tigris.subversion.subclipse.client.ISVNRevision)
     */
    public InputStream getContent(SVNUrl arg0, SVNRevision arg1) throws SVNClientException {

        try {
            InputStream content = cmd.cat(toString(arg0), toString(arg1));

            //read byte-by-byte and put it in a vector.
            //then take the vector and fill a byteArray.
            byte[] byteArray;
            byteArray = streamToByteArray(content);
            content.close();
            return new ByteArrayInputStream(byteArray);
        } catch (IOException e) {
            throw SVNClientException.wrapException(e);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }

    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getContent(
     * java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision)
     */
    public InputStream getContent(File path, SVNRevision revision) throws SVNClientException {

        try {
            InputStream content = cmd.cat(toString(path), toString(revision));

            //read byte-by-byte and put it in a vector.
            //then take the vector and fill a byteArray.
            byte[] byteArray;
            byteArray = streamToByteArray(content);
            content.close();
            return new ByteArrayInputStream(byteArray);
        } catch (IOException e) {
            throw SVNClientException.wrapException(e);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }

    }

    public InputStream getContent(SVNUrl url, SVNRevision revision,
                                  SVNRevision pegRevision) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
        return null;
    }


    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#mkdir(java.net.URL, java.lang.String)
     */
    public void mkdir(SVNUrl arg0, String arg1) throws SVNClientException {
        try {
            cmd.mkdir(toString(arg0), arg1);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#mkdir(java.io.File)
     */
    public void mkdir(File file) throws SVNClientException {
        try {
            cmd.mkdir(toString(file));
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
        //sometimes the dir has not yet been created.
        //wait up to 5 sec for the dir to be created.
        for (int i = 0; i < 50 && !file.exists(); i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e2) {
                //do nothing if interrupted
            }
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#copy(
     * java.net.URL, java.net.URL, java.lang.String, org.tigris.subversion.subclipse.client.ISVNRevision)
     */
    public void copy(SVNUrl src, SVNUrl dest, String message, SVNRevision rev)
            throws SVNClientException {
        try {
            if (message == null) {
                message = "";
            }
            cmd.copy(toString(src), toString(dest), message, toString(rev), false);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#copy(java.io.File, java.io.File)
     */
    public void copy(File srcPath, File destPath) throws SVNClientException {
        try {
            cmd.copy(toString(srcPath), toString(destPath));
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
        //sometimes the dir has not yet been created.
        //wait up to 5 sec for the dir to be created.
        for (int i = 0; i < 50 && !destPath.exists(); i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e2) {
                //do nothing if interrupted
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#copy(
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * java.lang.String)
     */
    public void copy(File srcPath, SVNUrl destUrl, String message) throws SVNClientException {
        try {
            if (message == null) {
                message = "";
            }
            cmd.copy(toString(srcPath), toString(destUrl), message, null, false);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#copy(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision)
     */
    public void copy(SVNUrl srcUrl, File destPath, SVNRevision revision)
            throws SVNClientException {
        try {
            cmd.copy(toString(srcUrl), toString(destPath), null, toString(revision), false);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    public void copy(SVNUrl srcUrl, SVNUrl destUrl, String message,
                     SVNRevision revision, boolean makeParents)
            throws SVNClientException {
        try {
            if (message == null) {
                message = "";
            }
            cmd.copy(toString(srcUrl), toString(destUrl), message, toString(revision), makeParents);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    public void copy(File[] srcPaths, SVNUrl destUrl, String message,
                     boolean copyAsChild, boolean makeParents) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
    }

    public void copy(SVNUrl[] srcUrls, SVNUrl destUrl, String message,
                     SVNRevision revision, boolean copyAsChild, boolean makeParents)
            throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
    }

    public void copy(SVNUrl srcUrl, File destPath, SVNRevision revision,
                     boolean copyAsChild, boolean makeParents) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
    }

    public void copy(SVNUrl srcUrl, File destPath, SVNRevision revision,
                     SVNRevision pegRevision, boolean copyAsChild, boolean makeParents)
            throws SVNClientException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#move(
     * java.net.URL, java.net.URL, java.lang.String, org.tigris.subversion.subclipse.client.ISVNRevision)
     */
    public void move(SVNUrl url, SVNUrl destUrl, String message, SVNRevision revision)
            throws SVNClientException {
        try {
            notificationHandler.setBaseDir(new File("."));
            cmd.move(toString(url), toString(destUrl), message, toString(revision), false);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#move(java.io.File, java.io.File, boolean)
     */
    public void move(File file, File file2, boolean force) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(new File[]{file, file2}));
            cmd.move(toString(file), toString(file2), null, null, force);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#setUsername(java.lang.String)
     */
    public void setUsername(String string) {
        if (string == null || string.length() == 0) {
            return;
        }
        cmd.setUsername(string);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        if (password == null) {
            return;
        }

        cmd.setPassword(password);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#addDirectory(java.io.File, boolean)
     */
    public void addDirectory(File file, boolean recurse) throws SVNClientException {
        addDirectory(file, recurse, false);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#addDirectory(java.io.File, boolean, boolean)
     */
    public void addDirectory(File file, boolean recurse, boolean force) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(file));
            cmd.add(toString(file), recurse, force);
        } catch (CmdLineException e) {
            //if something is already in svn and we
            //try to add it, we get a warning.
            //ignore it.\
            if (e.getMessage().startsWith("svn: warning: ")) {
                return;
            }
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#addFile(java.io.File)
     */
    public void addFile(File file) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(file));
            cmd.add(toString(file), false, false);
        } catch (CmdLineException e) {
            //if something is already in svn and we
            //try to add it, we get a warning.
            //ignore it.\
            if (e.getMessage().startsWith("svn: warning: ")) {
                return;
            }
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#commit(java.io.File[], java.lang.String, boolean)
     */
    public long commit(File[] parents, String comment, boolean recurse) throws SVNClientException {
        return commit(parents, comment, recurse, false);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#commit(
     * java.io.File[], java.lang.String, boolean, boolean)
     */
    public long commit(File[] parents, String comment, boolean recurse, boolean keepLocks) throws SVNClientException {
        String[] paths = new String[parents.length];
        for (int i = 0; i < parents.length; i++) {
            paths[i] = toString(parents[i]);
        }
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(parents));
            cmd.checkin(paths, comment, keepLocks);
            return cmd.getRevision();
        } catch (CmdLineException e) {
            if ("".equals(e.getMessage())) {
                return SVNRevision.SVN_INVALID_REVNUM;
            }
            if (e.getMessage().startsWith("svn: Attempted to lock an already-locked dir")) {
                //PHIL is this the best way to handle pending locks? (ie caused by "svn cp")
                //loop through up to 5 sec, waiting for locks
                //to be removed.
                for (int i = 0; i < 50; i++) {
                    try {
                        notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(parents));
                        cmd.checkin(paths, comment, keepLocks);
                        return cmd.getRevision();
                    } catch (CmdLineException e1) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e2) {
                            //do nothing if interrupted
                        }
                    }
                }
            }
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#update(
     * java.io.File, org.tigris.subversion.subclipse.client.ISVNRevision, boolean)
     */
    public long update(File file, SVNRevision revision, boolean b) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(file));
            cmd.update(toString(file), toString(revision));
            return cmd.getRevision();
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#update(
     * java.io.File[], org.tigris.subversion.svnclientadapter.SVNRevision, boolean, boolean)
     */
    public long[] update(File[] files,
                         SVNRevision revision,
                         boolean recurse,
                         boolean ignoreExternals) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(files[0]));
            cmdMulti.update(toString(files), toString(revision));
            return cmdMulti.getRevisions();
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    public long update(File path, SVNRevision revision, int depth,
                       boolean setDepth, boolean ignoreExternals, boolean force)
            throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
        return 0;
    }

    public long[] update(File[] path, SVNRevision revision, int depth,
                         boolean setDepth, boolean ignoreExternals, boolean force)
            throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNClientAdapter#checkout(
     * java.net.URL, java.io.File, org.tigris.subversion.subclipse.client.ISVNRevision, boolean)
     */
    public void checkout(SVNUrl url, File destPath, SVNRevision revision, boolean b)
            throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(destPath));
            cmd.checkout(toString(url), toString(destPath), toString(revision), b);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    public void checkout(SVNUrl moduleName, File destPath,
                         SVNRevision revision, int depth, boolean ignoreExternals,
                         boolean force) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();

    }

    /**
     * Returns the array of {@link CmdLineStatusPart} for the given paths.
     *
     * @param paths the files to get status for
     * @param descend whether to descend recursively
     * @param getAll whether to get all statuses
     * @param contactServer whether to contact the server
     * @param ignoreExternals whether to ignore externals
     * @return the status parts
     * @throws CmdLineException if the command fails
     */
    protected CmdLineStatusPart[] getCmdStatuses(File[] paths,
                                                 boolean descend,
                                                 boolean getAll,
                                                 boolean contactServer,
                                                 boolean ignoreExternals)
            throws CmdLineException {
        String[] pathNames = new String[paths.length];
        for (int i = 0; i < pathNames.length; i++) {
            pathNames[i] = toString(paths[i]);
        }
        return getCmdStatuses(pathNames, descend, getAll, contactServer, ignoreExternals);
    }

    /**
     * Executes the SVN status command for the specified file paths.
     *
     * @param paths array of file or directory paths
     * @param descend whether to descend into subdirectories
     * @param getAll whether to retrieve all statuses (including unchanged files)
     * @param contactServer whether to contact the repository for remote status
     * @param ignoreExternals whether to ignore externals
     * @return array of {@link CmdLineStatusPart} representing status of each path
     * @throws CmdLineException if the SVN command fails
     */
    protected CmdLineStatusPart[] getCmdStatuses(String[] paths,
                                                 boolean descend,
                                                 boolean getAll,
                                                 boolean contactServer,
                                                 boolean ignoreExternals)
            throws CmdLineException {
        if (paths.length == 0) {
            return new CmdLineStatusPart[0];
        }
        byte[] listXml;
        listXml = cmd.status(paths, descend, getAll, contactServer, ignoreExternals);
        return CmdLineStatusPart.CmdLineStatusPartFromXml.createStatusParts(listXml);
    }

    private void diff(
            String oldPath,
            SVNRevision oldPathRevision,
            String newPath,
            SVNRevision newPathRevision,
            File outFile,
            boolean recurse,
            boolean ignoreAncestry,
            boolean noDiffDeleted,
            boolean force) throws SVNClientException {
        if (newPath == null) {
            newPath = oldPath;
        }
        if (oldPathRevision == null) {
            oldPathRevision = SVNRevision.BASE;
        }
        if (newPathRevision == null) {
            newPathRevision = SVNRevision.WORKING;
        }

        try {
            InputStream is =
                    cmd.diff(
                            oldPath,
                            toString(oldPathRevision),
                            newPath,
                            toString(newPathRevision),
                            recurse,
                            ignoreAncestry,
                            noDiffDeleted,
                            force);

            streamToFile(is, outFile);
            is.close();
        } catch (IOException e) {
            //this should never happen
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, boolean)
     */
    public void diff(
            File oldPath,
            SVNRevision oldPathRevision,
            File newPath,
            SVNRevision newPathRevision,
            File outFile,
            boolean recurse)
            throws SVNClientException {
        if (oldPath == null) {
            oldPath = new File(".");
        }
        diff(oldPath,
                oldPathRevision,
                newPath,
                newPathRevision,
                outFile,
                recurse, true, false, false);
    }

    public void diff(
            File oldPath,
            SVNRevision oldPathRevision,
            File newPath,
            SVNRevision newPathRevision,
            File outFile,
            boolean recurse,
            boolean ignoreAncestry,
            boolean noDiffDeleted,
            boolean force)
            throws SVNClientException {
        if (oldPath == null) {
            oldPath = new File(".");
        }
        diff(
                toString(oldPath),
                oldPathRevision,
                toString(newPath),
                newPathRevision,
                outFile,
                recurse, ignoreAncestry, noDiffDeleted, force);
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(java.io.File, java.io.File, boolean)
     */
    public void diff(File path, File outFile, boolean recurse) throws SVNClientException {
        diff(path, null, null, null, outFile, recurse);
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     *  org.tigris.subversion.svnclientadapter.SVNRevision,
     *  org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, boolean)
     */
    public void diff(
            SVNUrl oldUrl,
            SVNRevision oldUrlRevision,
            SVNUrl newUrl,
            SVNRevision newUrlRevision,
            File outFile,
            boolean recurse)
            throws SVNClientException {
        diff(oldUrl, oldUrlRevision, newUrl, newUrlRevision, outFile, recurse, true,
                false, false);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * java.io.File, boolean, boolean, boolean, boolean)
     */
    public void diff(
            SVNUrl oldUrl,
            SVNRevision oldUrlRevision,
            SVNUrl newUrl,
            SVNRevision newUrlRevision,
            File outFile,
            boolean recurse,
            boolean ignoreAncestry,
            boolean noDiffDeleted,
            boolean force)
            throws SVNClientException {
        diff(toString(oldUrl), oldUrlRevision, toString(newUrl), newUrlRevision, outFile, recurse, ignoreAncestry,
                noDiffDeleted, force);
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision, java.io.File, boolean)
     */
    public void diff(
            SVNUrl url,
            SVNRevision oldUrlRevision,
            SVNRevision newUrlRevision,
            File outFile,
            boolean recurse)
            throws SVNClientException {
        diff(url, oldUrlRevision, url, newUrlRevision, outFile, recurse);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#diff(
     * java.io.File, org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * java.io.File,
     * boolean)
     */
    public void diff(File path, SVNUrl url, SVNRevision urlRevision,
                     File outFile, boolean recurse) throws SVNClientException {
        diff(
                toString(path),
                null,
                toString(url) + "@" + toString(urlRevision),
                null,
                outFile,
                recurse, true, false, false);
    }

    public void diff(SVNUrl target, SVNRevision pegRevision,
                     SVNRevision startRevision, SVNRevision endRevision, File outFile,
                     int depth, boolean ignoreAncestry, boolean noDiffDeleted,
                     boolean force) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();

    }

    public void diff(SVNUrl target, SVNRevision pegRevision,
                     SVNRevision startRevision, SVNRevision endRevision, File outFile,
                     boolean recurse) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();

    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#propertyGet(java.io.File, java.lang.String)
     */
    public ISVNProperty propertyGet(File path, String propertyName) throws SVNClientException {
        try {
            InputStream valueAndData = cmd.propget(toString(path), propertyName);

            byte[] bytes = streamToByteArray(valueAndData);
            valueAndData.close();
            if (bytes.length == 0) {
                return null; // the property does not exist
            }

            return new CmdLineProperty(propertyName, new String(bytes), path, bytes);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        } catch (IOException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    public ISVNProperty propertyGet(SVNUrl url, String propertyName) throws SVNClientException {
        try {
            InputStream valueAndData = cmd.propget(url.toString(), propertyName);

            byte[] bytes = streamToByteArray(valueAndData);
            valueAndData.close();
            if (bytes.length == 0) {
                return null; // the property does not exist
            }

            return new CmdLineProperty(propertyName, new String(bytes), url, bytes);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        } catch (IOException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    public ISVNProperty propertyGet(SVNUrl url, SVNRevision revision,
                                    SVNRevision peg, String propertyName) throws SVNClientException {
        try {
            InputStream valueAndData = cmd.propget(url.toString(), propertyName, toString(revision), toString(peg));

            byte[] bytes = streamToByteArray(valueAndData);
            if (bytes.length == 0) {
                return null; // the property does not exist
            }

            return new CmdLineProperty(propertyName, new String(bytes), url, bytes);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        } catch (IOException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#propertySet(java.io.File,
     * java.lang.String, java.io.File, boolean)
     */
    public void propertySet(File path, String propertyName, File propertyFile, boolean recurse)
            throws SVNClientException, IOException {
        try {
            cmd.propsetFile(propertyName, toString(propertyFile), toString(path), recurse);

            // there is no notification (Notify.notify is not called) when we set a property
            // so we will do notification ourselves
            ISVNStatus[] statuses = getStatus(path, recurse, false);
            for (int i = 0; i < statuses.length; i++) {
                notificationHandler.notifyListenersOfChange(statuses[i].getFile().getAbsolutePath());
            }

        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#propertySet(
     * java.io.File, java.lang.String, java.lang.String, boolean)
     */
    public void propertySet(File path, String propertyName, String propertyValue, boolean recurse)
            throws SVNClientException {
        try {
            cmd.propset(propertyName, propertyValue, toString(path), recurse);

            // there is no notification (Notify.notify is not called) when we set a property
            // so we will do notification ourselves
            ISVNStatus[] statuses = getStatus(path, recurse, false);
            for (int i = 0; i < statuses.length; i++) {
                notificationHandler.notifyListenersOfChange(statuses[i].getFile().getAbsolutePath());
            }

        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    @Override
    public void propertySet(SVNUrl url, Number baseRev, String propertyName,
                            String propertyValue, String message) throws SVNClientException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#propertyDel(
     * java.io.File, java.lang.String, boolean)
     */
    public void propertyDel(File path, String propertyName, boolean recurse)
            throws SVNClientException {
        try {
            cmd.propdel(propertyName, toString(path), recurse);

            // there is no notification (Notify.notify is not called) when we delete a property
            // so we will do notification ourselves
            ISVNStatus[] statuses = getStatus(path, recurse, false);
            for (int i = 0; i < statuses.length; i++) {
                notificationHandler.notifyListenersOfChange(statuses[i].getFile().getAbsolutePath());
            }

        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#setRevProperty(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision.Number,
     * java.lang.String, java.lang.String, boolean)
     */
    public void setRevProperty(SVNUrl path,
                               SVNRevision.Number revisionNo,
                               String propName,
                               String propertyData,
                               boolean force)
            throws SVNClientException {
        try {
            cmd.revpropset(propName, propertyData, toString(path), Long.toString(revisionNo.getNumber()), force);
            // there is no notification to send

        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#doImport(
     * java.io.File, org.tigris.subversion.svnclientadapter.SVNUrl, java.lang.String, boolean)
     */
    public void doImport(File path, SVNUrl url, String message, boolean recurse)
            throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            cmd.importFiles(toString(path), toString(url), message, recurse);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#doExport(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * java.io.File, org.tigris.subversion.svnclientadapter.SVNRevision, boolean)
     */
    public void doExport(SVNUrl srcUrl, File destPath, SVNRevision revision, boolean force)
            throws SVNClientException {
        try {
            cmd.export(toString(srcUrl), toString(destPath), toString(revision), force);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#doExport(java.io.File, java.io.File, boolean)
     */
    public void doExport(File srcPath, File destPath, boolean force) throws SVNClientException {
        try {
            cmd.export(toString(srcPath), toString(destPath), null, force);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /**
     * Converts an {@link SVNRevision} to its string representation.
     *
     * @param r the revision
     * @return the string representation
     */
    protected static String toString(SVNRevision r) {
        return (r == null) ? null : r.toString();
    }

    /**
     * Converts a {@link File} to its string path, adding an '@' if needed.
     *
     * @param f the file
     * @return the string path
     */
    protected static String toString(File f) {
        return (f == null) ? null : atSign(f.toString());
    }

    /**
     * Converts an array of files to a space-separated string of their paths.
     *
     * @param f the array of files
     * @return the space-separated string representation
     */
    protected static String toString(File[] f) {
        if (f == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < f.length; i++) {
            buf.append(atSign(f[i].toString()) + " ");
        }
        return buf.toString();
    }

    /**
     * Converts a {@link SVNUrl} to its string representation, applying '@' escaping if needed.
     *
     * @param u the SVN URL
     * @return the string representation of the URL
     */
    protected static String toString(SVNUrl u) {
        return (u == null) ? null : atSign(u.toString());
    }

    /**
     * The command line requires paths that contain an '@'
     * have an '@' at the end of the string. Otherwise it
     * interprets the '@' as the start of a peg revision
     * </p>
     * Rather than always add an '@' to end of path we use
     * this method to only do so when needed.
     */
    private static String atSign(String s) {
        if (s.contains("@")) {
            return s + "@";
        } else {
            return s;
        }
    }


    /**
     * Implementation used by overloads of <code>getLogMessages()</code>.
     *
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(
     *org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(
     *java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean)
     */
    private ISVNLogMessage[] getLogMessages(
            String pathOrUrl,
            String[] paths,
            SVNRevision revisionStart,
            SVNRevision revisionEnd,
            boolean stopOnCopy,
            boolean fetchChangePath,
            long limit)
            throws SVNClientException {
        String revRange = toString(revisionStart) + ":"
                + toString(revisionEnd);
        try {
            byte[] messages;

            // To acquire the paths associated with each delta, we'd
            // have to include the --verbose argument.
            if (fetchChangePath) {
                messages = cmd.logVerbose(pathOrUrl, paths, revRange, stopOnCopy, limit);
            } else {
                messages = cmd.log(pathOrUrl, revRange, stopOnCopy, limit);
            }
            return CmdLineLogMessage.createLogMessages(messages);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean)
     */
    public ISVNLogMessage[] getLogMessages(File path, SVNRevision revStart,
                                           SVNRevision revEnd, boolean fetchChangePath)
            throws SVNClientException {
        return getLogMessages(path, revStart, revEnd, false, fetchChangePath);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean, boolean)
     */
    public ISVNLogMessage[] getLogMessages(File path, SVNRevision revStart,
                                           SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath)
            throws SVNClientException {
        return getLogMessages(path, revStart, revEnd, stopOnCopy,
                fetchChangePath, 0);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean,
     * boolean,
     * long)
     */
    public ISVNLogMessage[] getLogMessages(File path, SVNRevision revStart,
                                           SVNRevision revEnd, boolean stopOnCopy, boolean fetchChangePath,
                                           long limit) throws SVNClientException {
        String target = toString(path);
        //If the file is an uncommitted rename/move, we have to refer to original/source, not the new copy.
        ISVNInfo info = getInfoFromWorkingCopy(path);
        if ((SVNScheduleKind.ADD == info.getSchedule()) && (info.getCopyUrl() != null)) {
            target = info.getCopyUrl().toString();
        }
        return getLogMessages(target, null, revStart, revEnd, stopOnCopy,
                fetchChangePath, limit);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean)
     */
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision revStart,
                                           SVNRevision revEnd, boolean fetchChangePath)
            throws SVNClientException {
        return getLogMessages(url, null, revStart, revEnd, false,
                fetchChangePath);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean,
     * boolean,
     * long)
     */
    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision pegRevision,
                                           SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy,
                                           boolean fetchChangePath, long limit) throws SVNClientException {
        //TODO pegRevision not supported !
        return getLogMessages(toString(url), null, revStart, revEnd, stopOnCopy,
                fetchChangePath, limit);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getLogMessages(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * java.lang.String[],
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean,
     * boolean)
     */
    public ISVNLogMessage[] getLogMessages(SVNUrl url, String[] paths,
                                           SVNRevision revStart, SVNRevision revEnd, boolean stopOnCopy,
                                           boolean fetchChangePath) throws SVNClientException {
        return getLogMessages(toString(url), paths, revStart, revEnd, stopOnCopy,
                fetchChangePath, 0);
    }

    public ISVNLogMessage[] getLogMessages(File path, SVNRevision pegRevision,
                                           SVNRevision revisionStart, SVNRevision revisionEnd,
                                           boolean stopOnCopy, boolean fetchChangePath, long limit,
                                           boolean includeMergedRevisions) throws SVNClientException {
        notImplementedYet();
        return null;
    }

    public ISVNLogMessage[] getLogMessages(SVNUrl url, SVNRevision pegRevision,
                                           SVNRevision revisionStart, SVNRevision revisionEnd,
                                           boolean stopOnCopy, boolean fetchChangePath, long limit,
                                           boolean includeMergedRevisions) throws SVNClientException {
        notImplementedYet();
        return null;
    }

    public void getLogMessages(File path, SVNRevision pegRevision,
                               SVNRevision revisionStart, SVNRevision revisionEnd,
                               boolean stopOnCopy, boolean fetchChangePath, long limit,
                               boolean includeMergedRevisions, String[] requestedProperties,
                               ISVNLogMessageCallback callback) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();

    }

    public void getLogMessages(SVNUrl url, SVNRevision pegRevision,
                               SVNRevision revisionStart, SVNRevision revisionEnd,
                               boolean stopOnCopy, boolean fetchChangePath, long limit,
                               boolean includeMergedRevisions, String[] requestedProperties,
                               ISVNLogMessageCallback callback) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();

    }

    private static void streamToFile(InputStream stream, File outFile) throws IOException {
        int tempByte;
        try {
            FileOutputStream os = new FileOutputStream(outFile);
            while ((tempByte = stream.read()) != -1) {
                os.write(tempByte);
            }
            os.close();
            stream.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] streamToByteArray(InputStream stream)
            throws IOException {
        //read byte-by-byte and put it in a vector.
        //then take the vector and fill a byteArray.
        Vector buffer = new Vector(1024);
        int tempByte;
        while ((tempByte = stream.read()) != -1) {
            buffer.add(new Byte((byte) tempByte));
        }

        byte[] byteArray = new byte[buffer.size()];
        for (int i = 0; i < byteArray.length; i++) {
            Byte b = (Byte) buffer.get(i);
            byteArray[i] = b.byteValue();
        }
        return byteArray;
    }

    /**
     * Gets the annotation information for the specified target and revisions.
     *
     * @param target the target file or URL
     * @param revisionStart the start revision
     * @param revisionEnd the end revision
     * @return the annotations
     * @throws SVNClientException if an error occurs
     */
    protected ISVNAnnotations annotate(String target,
                                       SVNRevision revisionStart,
                                       SVNRevision revisionEnd)
            throws SVNClientException {
        try {
            notificationHandler.setCommand(ISVNNotifyListener.Command.ANNOTATE);
            if (revisionStart == null) {
                revisionStart = new SVNRevision.Number(1);
            }
            if (revisionEnd == null) {
                revisionEnd = SVNRevision.HEAD;
            }

            byte[] annotations = cmd.annotate(target, toString(revisionStart), toString(revisionEnd));
            InputStream contents = cmd.cat(target, revisionEnd.toString());
            CmdLineAnnotations result = CmdLineAnnotations.createFromXml(annotations, contents);
            try {
                contents.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
            }
            return result;
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /**
     * Retrieves the annotation (blame) information for the specified URL and revision range.
     *
     * @param url the SVN URL to annotate
     * @param revisionStart the start revision for annotation (usually 1)
     * @param revisionEnd the end revision for annotation (typically HEAD)
     * @return annotation information containing author and revision data per line
     * @throws SVNClientException if the command fails
     */
    public ISVNAnnotations annotate(SVNUrl url, SVNRevision revisionStart, SVNRevision revisionEnd)
            throws SVNClientException {
        return annotate(toString(url), revisionStart, revisionEnd);
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#annotate(
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision)
     */
    public ISVNAnnotations annotate(File file, SVNRevision revisionStart, SVNRevision revisionEnd)
            throws SVNClientException {
        String target = toString(file);
        //If the file is an uncommitted rename/move, we have to refer to original/source, not the new copy.
        ISVNInfo info = getInfoFromWorkingCopy(file);
        if ((SVNScheduleKind.ADD == info.getSchedule()) && (info.getCopyUrl() != null)) {
            target = info.getCopyUrl().toString();
        }
        return annotate(target, revisionStart, revisionEnd);
    }

    public ISVNAnnotations annotate(File file, SVNRevision revisionStart,
                                    SVNRevision revisionEnd, boolean ignoreMimeType,
                                    boolean includeMergedRevisions) throws SVNClientException {
        notImplementedYet();
        return null;
    }

    public ISVNAnnotations annotate(SVNUrl url, SVNRevision revisionStart,
                                    SVNRevision revisionEnd, boolean ignoreMimeType,
                                    boolean includeMergedRevisions) throws SVNClientException {
        notImplementedYet();
        return null;
    }

    public ISVNAnnotations annotate(SVNUrl url, SVNRevision revisionStart,
                                    SVNRevision revisionEnd, SVNRevision pegRevision,
                                    boolean ignoreMimeType, boolean includeMergedRevisions)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    public ISVNAnnotations annotate(File file, SVNRevision revisionStart,
                                    SVNRevision revisionEnd, SVNRevision pegRevision,
                                    boolean ignoreMimeType, boolean includeMergedRevisions)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves the properties of a file or directory from the working copy.
     *
     * @param path the file or directory
     * @return array of SVN properties, or an empty array if none
     * @throws SVNClientException if an error occurs while retrieving properties
     */
    public ISVNProperty[] getProperties(File path) throws SVNClientException {
        try {
            String propertiesString = cmd.proplist(toString(path), false);
            String propertyName;
            List properties = new LinkedList();

            StringTokenizer st = new StringTokenizer(propertiesString, Helper.NEWLINE);
            while (st.hasMoreTokens()) {
                String propertyLine = st.nextToken();
                if (propertyLine.startsWith("Properties on '")) {
                    propertyName = propertyLine.substring(2);
                    properties.add(propertyGet(path, propertyName));
                }
            }
            return (ISVNProperty[]) properties.toArray(new ISVNProperty[0]);

        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    public ISVNProperty[] getProperties(SVNUrl url) throws SVNClientException {
        try {
            String propertiesString = cmd.proplist(url.toString(), false);
            String propertyName;
            List properties = new LinkedList();

            StringTokenizer st = new StringTokenizer(propertiesString, Helper.NEWLINE);
            while (st.hasMoreTokens()) {
                String propertyLine = st.nextToken();
                if (propertyLine.startsWith("Properties on '")) {
                    propertyName = propertyLine.substring(2);
                    properties.add(propertyGet(url, propertyName));
                }
            }
            return (ISVNProperty[]) properties.toArray(new ISVNProperty[0]);

        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /**
     * Retrieves the properties of a remote URL at a specific revision and peg revision.
     *
     * @param url the SVN URL
     * @param revision the operative revision
     * @param peg the peg revision
     * @return array of SVN properties
     * @throws SVNClientException if an error occurs
     */
    public ISVNProperty[] getProperties(SVNUrl url, SVNRevision revision,
                                        SVNRevision peg) throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves the properties of a file or directory, optionally recursively.
     *
     * @param path the file or directory
     * @param descend if true, retrieves properties recursively
     * @return array of SVN properties
     * @throws SVNClientException if an error occurs
     */
    public ISVNProperty[] getProperties(File path, boolean descend)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves the properties of a remote SVN URL at a specific revision and peg revision,
     * optionally recursively.
     *
     * @param url the remote SVN URL to query
     * @param revision the operative revision to retrieve properties from
     * @param peg the peg revision used to identify the object in history
     * @param recurse if true, retrieves properties recursively for all children (if directory)
     * @return an array of {@link ISVNProperty} objects, or {@code null} if not implemented
     * @throws SVNClientException if an error occurs during retrieval
     */
    public ISVNProperty[] getProperties(SVNUrl url, SVNRevision revision,
                                        SVNRevision peg, boolean recurse) throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Removes the 'conflicted' state from the specified file or directory in the working copy.
     *
     * <p>This method marks the given {@code path} as resolved, indicating that any conflicts
     * have been manually addressed by the user. It wraps the {@code svn resolve} command and
     * also explicitly notifies listeners of the change, since the SVN CLI does not emit notifications
     * for this operation.</p>
     *
     * @param path the file or directory to mark as resolved
     * @throws SVNClientException if the resolve operation fails or the command cannot be executed
     */
    public void resolved(File path)
            throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            cmd.resolved(new String[]{toString(path)}, false);

            // there is no notification when we do svn resolve, we will do notification ourselves
            notificationHandler.notifyListenersOfChange(path.getAbsolutePath());
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }

    }


    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#createRepository(java.io.File)
     */
    public void createRepository(File path, String repositoryType) throws SVNClientException {
        try {
            svnAdminCmd.create(toString(path), repositoryType);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#cancelOperation()
     */
    public void cancelOperation() throws SVNClientException {
        notificationHandler.logMessage("Warning: operation canceled.");
        cmd.stopProcess();
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getInfoFromWorkingCopy(java.io.File)
     */
    public ISVNInfo getInfoFromWorkingCopy(File path) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));

            // first we get the status of the files to find out whether it is versioned
            CmdLineStatusPart[] cmdLineStatusParts = getCmdStatuses(new File[]{path}, false, true, false, false);
            // if the file is managed, it is safe to call info
            if ((cmdLineStatusParts.length > 0) && (cmdLineStatusParts[0].isManaged())) {
                String cmdLineInfoStrings = cmd.info(new String[]{toString(path)}, null, null);
                return new CmdLineInfoPart(cmdLineInfoStrings);
            } else {
                return CmdLineInfoPart.createUnversioned(path.getPath());
            }
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getInfo(java.io.File)
     */
    public ISVNInfo getInfo(File path) throws SVNClientException {
        return getInfoFromWorkingCopy(path);
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#getInfo(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * org.tigris.subversion.svnclientadapter.SVNRevision)
     */
    public ISVNInfo getInfo(SVNUrl url, SVNRevision revision, SVNRevision peg) throws SVNClientException {
        return getInfo(new SVNUrl[]{url}, revision, peg);
    }

    private ISVNInfo getInfo(SVNUrl[] urls, SVNRevision revision, SVNRevision peg) throws SVNClientException {
        try {
            String[] urlStrings = new String[urls.length];
            for (int i = 0; i < urls.length; i++) {
                urlStrings[i] = toString(urls[i]);
            }
            //notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(urls));
            String cmdLineInfoStrings = cmd.info(urlStrings, toString(revision), toString(peg));
            return new CmdLineInfoPart(cmdLineInfoStrings);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /**
     * Retrieves SVN info for an array of URLs.
     *
     * @param urls array of SVN URLs
     * @return SVN information describing each URL
     * @throws SVNClientException if the command fails
     */
    public ISVNInfo getInfo(SVNUrl[] urls) throws SVNClientException {
        return getInfo(urls, null, null);
    }

    public ISVNInfo[] getInfo(File file, boolean descend)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#switchUrl(
     * org.tigris.subversion.svnclientadapter.SVNUrl,
     * java.io.File,
     * org.tigris.subversion.svnclientadapter.SVNRevision,
     * boolean)
     */
    public void switchToUrl(File path, SVNUrl url, SVNRevision revision, boolean recurse) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            cmd.switchUrl(toString(path), toString(url), toString(revision), recurse);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    @Override
    public void switchToUrl(File path,
                            SVNUrl url,
                            SVNRevision revision,
                            int depth,
                            boolean setDepth,
                            boolean ignoreExternals,
                            boolean force) throws SVNClientException {
        // TODO Auto-generated method stub
    }

    @Override
    public void switchToUrl(File path,
                            SVNUrl url,
                            SVNRevision revision,
                            SVNRevision pegRevision,
                            int depth,
                            boolean setDepth,
                            boolean ignoreExternals,
                            boolean force) throws SVNClientException {
        // TODO Auto-generated method stub
    }

    @Override
    public void switchToUrl(File path, SVNUrl url, SVNRevision revision,
                            SVNRevision pegRevision, int depth, boolean setDepth,
                            boolean ignoreExternals, boolean force, boolean ignoreAncestry)
            throws SVNClientException {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNClientAdapter#setConfigDirectory(java.io.File)
     */
    public void setConfigDirectory(File dir) throws SVNClientException {
        cmd.setConfigDirectory(toString(dir));
    }

    /**
     * Runs the {@code svn cleanup} command on the specified working copy path.
     *
     * <p>This operation is used to clean up the working copy directory after
     * an incomplete operation (e.g., interrupted checkout or update). It can also
     * be useful when the working copy is locked or corrupted.</p>
     *
     * @param path the working copy directory or file to clean up
     * @throws SVNClientException if the cleanup operation fails
     */
    public void cleanup(File path) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(path));
            cmd.cleanup(toString(path));
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /**
     * Performs a two-URL merge between two different repository locations and applies the result to a local working copy.
     *
     * <p>This method is typically used to merge the differences between two branches or revisions
     * into a local path. It corresponds to the command-line usage:
     * {@code svn merge <source1>@<rev1> <source2>@<rev2> <localPath>}</p>
     *
     * @param path1 the first source URL (typically the older branch or revision)
     * @param revision1 the revision of the first source URL
     * @param path2 the second source URL (typically the newer branch or revision)
     * @param revision2 the revision of the second source URL
     * @param localPath the local working copy path where changes will be applied
     * @param force whether to force the merge even if there are local modifications
     * @param recurse whether to recurse into subdirectories (legacy; use depth in newer APIs)
     * @param dryRun if true, performs a test merge without making actual changes
     * @param ignoreAncestry whether to ignore merge history and treat sources as unrelated
     * @throws SVNClientException if the merge operation fails
     */
    public void merge(SVNUrl path1, SVNRevision revision1, SVNUrl path2,
                      SVNRevision revision2, File localPath, boolean force,
                      boolean recurse, boolean dryRun, boolean ignoreAncestry) throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(localPath));
            cmd.merge(toString(path1), toString(revision1), toString(path2), toString(revision2),
                    toString(localPath), force, recurse, dryRun, ignoreAncestry);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /**
     * Merges a list of revision ranges from a single source URL into a working copy.
     *
     * @param url the source branch URL
     * @param pegRevision the peg revision that identifies the branch
     * @param revisions array of revision ranges to merge
     * @param localPath the local working copy path to merge into
     * @param force whether to force the merge even if there are local changes
     * @param depth the depth of the merge (e.g., files only, immediates, infinity)
     * @param ignoreAncestry whether to ignore merge history and ancestry
     * @param dryRun if true, performs a test run without making any changes
     * @throws SVNClientException if an error occurs during the merge operation
     */
    public void merge(SVNUrl url, SVNRevision pegRevision,
                      SVNRevisionRange[] revisions, File localPath, boolean force,
                      int depth, boolean ignoreAncestry, boolean dryRun)
            throws SVNClientException {
        notImplementedYet();
    }

    /**
     * Merges specific revision ranges from a source URL into a working copy,
     * with the option to record the merge without applying changes.
     *
     * @param url the source branch URL
     * @param pegRevision the peg revision of the source URL
     * @param revisions the revision ranges to merge
     * @param localPath the target working copy path
     * @param force if true, forces the merge even if conflicts or local changes exist
     * @param depth the depth of the merge (e.g., files, immediates, infinity)
     * @param ignoreAncestry if true, merge will not consider common ancestry
     * @param dryRun if true, performs a test merge without making changes
     * @param recordOnly if true, records the merge as if it happened, but makes no changes
     * @throws SVNClientException if the merge fails
     */
    public void merge(SVNUrl url, SVNRevision pegRevision,
                      SVNRevisionRange[] revisions, File localPath, boolean force,
                      int depth, boolean ignoreAncestry, boolean dryRun,
                      boolean recordOnly) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();

    }

    /**
     * Performs a two-source merge between two URLs and merges the differences into the local path.
     *
     * @param path1 the first source URL (usually the older branch)
     * @param revision1 the revision of the first source
     * @param path2 the second source URL (usually the newer branch)
     * @param revision2 the revision of the second source
     * @param localPath the working copy path to merge into
     * @param force if true, forces the merge even with local modifications
     * @param depth the depth of the merge operation
     * @param dryRun if true, simulates the merge without applying any changes
     * @param ignoreAncestry whether to ignore merge history when comparing sources
     * @param recordOnly if true, records merge metadata without performing a file-level merge
     * @throws SVNClientException if the merge operation fails
     */
    public void merge(SVNUrl path1, SVNRevision revision1, SVNUrl path2,
                      SVNRevision revision2, File localPath, boolean force, int depth,
                      boolean dryRun, boolean ignoreAncestry, boolean recordOnly)
            throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
    }

    /**
     * Locks the specified repository URLs with an optional comment.
     *
     * @param uris the repository URLs to lock
     * @param comment the lock comment
     * @param force if true, forces the lock
     * @throws SVNClientException if locking fails
     */
    public void lock(SVNUrl[] uris, String comment, boolean force)
            throws SVNClientException {
        // notificationHandler isn't used because we're operating on
        // the repository (rather than the WC).
        try {
            cmd.lock(uris, comment, force);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /**
     * Locks the specified files in the working copy with an optional comment.
     *
     * @param paths the files to lock
     * @param comment the lock comment
     * @param force if true, forces the lock
     * @throws SVNClientException if locking fails
     */
    public void lock(File[] paths, String comment, boolean force)
            throws SVNClientException {
        String[] files = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = toString(paths[i]);
        }
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(paths));
            cmd.lock(files, comment, force);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        } finally {
            for (int i = 0; i < files.length; i++) {
                notificationHandler.notifyListenersOfChange(files[i]);
            }
        }
    }

    /**
     * Unlocks the specified repository URLs.
     *
     * @param uris the repository URLs to unlock
     * @param force if true, forces unlock
     * @throws SVNClientException if unlocking fails
     */
    public void unlock(SVNUrl[] uris, boolean force)
            throws SVNClientException {
        // notificationHandler isn't used because we're operating on
        // the repository (rather than the WC).
        try {
            cmd.unlock(uris, force);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    /**
     * Unlocks the specified working copy files.
     *
     * @param paths the files to unlock
     * @param force if true, forces unlock
     * @throws SVNClientException if unlocking fails
     */
    public void unlock(File[] paths, boolean force) throws SVNClientException {
        String[] files = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            files[i] = toString(paths[i]);
        }
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(paths));
            cmd.unlock(files, force);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        } finally {
            for (int i = 0; i < files.length; i++) {
                notificationHandler.notifyListenersOfChange(files[i]);
            }
        }
    }

    public String getAdminDirectoryName() {
        if (dirName == null) {
            // svn only supports this feature on Windows
            if (isOsWindows()) {
                dirName = getEnvironmentVariable("SVN_ASP_DOT_NET");
            }
            // If the environment variable was present, then use _svn
            // as the directory name, otherwise the default of .svn
            if (dirName != null) {
                dirName = "_svn";
            } else {
                dirName = ".svn";
            }
        }
        return dirName;
    }

    public boolean isAdminDirectory(String name) {
        return getAdminDirectoryName().equals(name);
    }

    /**
     * Retrieves the environment variable with the given name.
     *
     * @param var the name of the environment variable
     * @return the value of the environment variable, or {@code null} if not found
     */
    public static String getEnvironmentVariable(String var) {
        try {
            // pre-Java 1.5 this throws an Error.  On Java 1.5 it
            // returns the environment variable
            return System.getenv(var);
        } catch (Error e) {
            try {
                // This means we are on 1.4.  Get all variables into
                // a Properties object and get the variable from that
                return getEnvVars().getProperty(var);
            } catch (Throwable e1) {
                return null;
            }
        }
    }

    /**
     * Gets all environment variables available in the system.
     *
     * @return the properties containing all environment variables
     * @throws Throwable if retrieval fails
     */
    public static Properties getEnvVars() throws Throwable {
        Process p = null;
        Properties envVars = new Properties();
        Runtime r = Runtime.getRuntime();
        if (isOsWindows()) {
            if (System.getProperty("os.name").toLowerCase().indexOf("windows 9") > -1) {
                p = r.exec("command.com /c set");
            } else {
                p = r.exec("cmd.exe /c set");
            }
        } else {
            p = r.exec("env");
        }
        if (p != null) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                int idx = line.indexOf('=');
                String key = line.substring(0, idx);
                String value = line.substring(idx + 1);
                envVars.setProperty(key, value);
            }
            p.getInputStream().close();
            p.getOutputStream().close();
            p.getErrorStream().close();
        }
        return envVars;
    }

    public void relocate(String from, String to, String path, boolean recurse)
            throws SVNClientException {
        try {
            notificationHandler.setBaseDir(SVNBaseDir.getBaseDir(new File(path)));
            cmd.relocate(from, to, path, recurse);
        } catch (CmdLineException e) {
            throw SVNClientException.wrapException(e);
        }
    }

    public void addConflictResolutionCallback(ISVNConflictResolver callback) {
        // TODO
    }

    public ISVNMergeInfo getMergeInfo(File path, SVNRevision revision)
            throws SVNClientException {
        notImplementedYet();
        return null;
    }

    public ISVNMergeInfo getMergeInfo(SVNUrl url, SVNRevision revision)
            throws SVNClientException {
        notImplementedYet();
        return null;
    }

    /**
     * Produces a summary of differences between revisions for the given local path.
     *
     * @param target the local working copy path
     * @param pegRevision the peg revision
     * @param startRevision the start revision
     * @param endRevision the end revision
     * @param depth the depth of traversal
     * @param ignoreAncestry if true, ignores ancestry when calculating differences
     * @return an array of {@link SVNDiffSummary} objects describing the changes
     * @throws SVNClientException if an error occurs
     */
    public SVNDiffSummary[] diffSummarize(File target,
                                          SVNRevision pegRevision,
                                          SVNRevision startRevision,
                                          SVNRevision endRevision,
                                          int depth,
                                          boolean ignoreAncestry)
            throws SVNClientException {
        notImplementedYet();
        return null;
    }

    /**
     * Produces a summary of differences between revisions for the given local path.
     *
     * @param target1 the local working copy path
     * @param revision1 the start revision
     * @param revision2 the reg revision
     * @param depth the depth of traversal
     * @param ignoreAncestry if true, ignores ancestry when calculating differences
     * @return an array of {@link SVNDiffSummary} objects describing the changes
     * @throws SVNClientException if an error occurs
     */
    public SVNDiffSummary[] diffSummarize(File target1,
                                          SVNRevision revision1,
                                          SVNUrl target2,
                                          SVNRevision revision2,
                                          int depth,
                                          boolean ignoreAncestry)
            throws SVNClientException {
        notImplementedYet();
        return null;
    }

    /**
     * Produces a summary of differences between revisions for the given local path.
     *
     * @param target the local working copy path
     * @param pegRevision the peg revision
     * @param startRevision the start revision
     * @param endRevision the end revision
     * @param depth the depth of traversal
     * @param ignoreAncestry if true, ignores ancestry when calculating differences
     * @return an array of {@link SVNDiffSummary} objects describing the changes
     * @throws SVNClientException if an error occurs
     */
    public SVNDiffSummary[] diffSummarize(SVNUrl target,
                                          SVNRevision pegRevision,
                                          SVNRevision startRevision,
                                          SVNRevision endRevision,
                                          int depth,
                                          boolean ignoreAncestry)
            throws SVNClientException {
        notImplementedYet();
        return null;
    }

    /**
     * Produces a summary of differences between revisions for the given local path.
     *
     * @param target1 the local working copy path
     * @param revision1 the start revision
     * @param revision2 the reg revision
     * @param depth the depth of traversal
     * @param ignoreAncestry if true, ignores ancestry when calculating differences
     * @return an array of {@link SVNDiffSummary} objects describing the changes
     * @throws SVNClientException if an error occurs
     */
    public SVNDiffSummary[] diffSummarize(SVNUrl target1,
                                          SVNRevision revision1,
                                          SVNUrl target2,
                                          SVNRevision revision2,
                                          int depth,
                                          boolean ignoreAncestry)
            throws SVNClientException {
        notImplementedYet();
        return null;
    }

    public SVNDiffSummary[] diffSummarize(File path, SVNUrl toUrl,
                                          SVNRevision toRevision, boolean recurse) throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    public String[] suggestMergeSources(File path) throws SVNClientException {
        notImplementedYet();
        return null;
    }

    public String[] suggestMergeSources(SVNUrl url, SVNRevision peg) throws SVNClientException {
        notImplementedYet();
        return null;
    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public void resolve(File path, int result) throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();

    }

    public void setProgressListener(ISVNProgressListener progressListener) {
        // TODO Auto-generated method stub

    }

    /**
     * Performs a reintegrate merge from the specified URL to the working copy.
     *
     * @param path the branch URL to reintegrate
     * @param pegRevision the peg revision of the branch
     * @param localPath the target local path
     * @param force if true, forces the merge
     * @param dryRun if true, simulates the merge
     * @throws SVNClientException if the merge fails
     */
    public void mergeReintegrate(SVNUrl path, SVNRevision pegRevision,
                                 File localPath, boolean force, boolean dryRun)
            throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
    }

    public ISVNLogMessage[] getMergeinfoLog(int kind, File path,
                                            SVNRevision pegRevision, SVNUrl mergeSourceUrl,
                                            SVNRevision srcPegRevision, boolean discoverChangedPaths)
            throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
        return null;
    }

    public ISVNLogMessage[] getMergeinfoLog(int kind, SVNUrl url,
                                            SVNRevision pegRevision, SVNUrl mergeSourceUrl,
                                            SVNRevision srcPegRevision, boolean discoverChangedPaths)
            throws SVNClientException {
        // TODO Auto-generated method stub
        notImplementedYet();
        return null;
    }

    public String getRevProperty(SVNUrl path, Number revisionNo, String propName)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    public ISVNProperty[] getRevProperties(SVNUrl url, Number revision)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPostCommitError() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void upgrade(File dir) throws SVNClientException {
        // TODO Auto-generated method stub

    }

    /**
     * Retrieves properties for a file or directory including inherited ones.
     *
     * @param path the file or directory path
     * @return array of SVN properties including inherited ones
     * @throws SVNClientException if retrieval fails
     */
    public ISVNProperty[] getPropertiesIncludingInherited(File path) throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves inherited properties with filtering and options.
     *
     * @param path the file or directory
     * @param includeEmptyProperties whether to include empty properties
     * @param includeClosestOnly if true, includes only the closest inherited properties
     * @param filterProperties list of property names to include (null for all)
     * @return array of inherited SVN properties
     * @throws SVNClientException if an error occurs
     */
    public ISVNProperty[] getPropertiesIncludingInherited(File path,
                                                          boolean includeEmptyProperties,
                                                          boolean includeClosestOnly,
                                                          List<String> filterProperties)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves inherited properties for the given repository URL.
     *
     * @param path the SVN URL
     * @return array of inherited properties
     * @throws SVNClientException if an error occurs
     */
    public ISVNProperty[] getPropertiesIncludingInherited(SVNUrl path) throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Retrieves inherited properties for the given URL with filtering options.
     *
     * @param path the SVN URL
     * @param includeEmptyProperties whether to include empty properties
     * @param includeClosestOnly if true, includes only the closest inherited properties
     * @param filterProperties list of property names to include (null for all)
     * @return array of inherited properties
     * @throws SVNClientException if retrieval fails
     */
    public ISVNProperty[] getPropertiesIncludingInherited(SVNUrl path,
                                                          boolean includeEmptyProperties,
                                                          boolean includeClosestOnly,
                                                          List<String> filterProperties)
            throws SVNClientException {
        // TODO Auto-generated method stub
        return null;
    }

}
