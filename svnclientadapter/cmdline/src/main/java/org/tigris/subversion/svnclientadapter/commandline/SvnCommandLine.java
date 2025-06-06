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

import java.io.InputStream;

import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.commandline.parser.SvnOutputParser;

/**
 * </p>
 * Performs the gruntwork of calling "svn". Is a bare-bones interface to using
 * the Subversion commandline client.</p>
 *
 * @author Philip Schatz (schatz at tigris)
 * @author Cï¿½dric Chabanois (cchabanois at no-log.org)
 * @author John M Flinchbaugh (john at hjsoft.com)
 */
public class SvnCommandLine extends CommandLine {

    protected String user;
    protected String pass;
    protected SvnOutputParser svnOutputParser = new SvnOutputParser();
    protected long rev = SVNRevision.SVN_INVALID_REVNUM;
    protected boolean parseSvnOutput = false;
    protected String configDir = null;

    //Constructors
    SvnCommandLine(String svnPath, CmdLineNotificationHandler notificationHandler) {
        super(svnPath, notificationHandler);
    }

    /**
     * Returns a valid Subversion revision string.
     *
     * @summary Returns "HEAD" if the input revision is null or empty; otherwise, returns the original revision.
     *
     * @param revision The revision string to validate.
     * @return "HEAD" if the input is null or empty, otherwise the input revision.
     */
    protected static String validRev(String revision) {
        return (revision == null || "".equals(revision)) ? "HEAD" : revision;
    }

    /**
     * </p>
     * Sets the username used by this client.</p>
     *
     * @param username The username to use for authentication.
     */
    void setUsername(String username) {
        user = username;
    }

    /**
     * </p>
     * Sets the password used by this client.</p>
     *
     * @param password The password to use for authentication.
     */
    void setPassword(String password) {
        pass = password;
    }

    /**
     * set the directory from which user configuration files will be read.
     */
    void setConfigDirectory(String dir) {
        configDir = dir;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.commandline.CommandLine#version()
     */
    String version() throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.UNDEFINED, false);
        return super.version();
    }

    /**
     * </p>
     * Adds an unversioned file into the repository.</p>
     *
     * @param path      Local path of resource to add.
     * @param recursive true if this is a directory and its children should be
     *                  traversed recursively.
     * @param force     true if this is a directory that should be scanned even if
     *                  it's already added to the repository
     */
    String add(String path, boolean recursive, boolean force) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.ADD, true);
        CmdArguments args = new CmdArguments();
        args.add("add");
        if (!recursive) {
            args.add("-N");
        }
        if (force) {
            args.add("--force");
        }
        args.add(path);
        return execString(args, false);
    }

    /**
     * </p>
     * Output the content of specified file or URL.</p>
     *
     * @param url Either the local path to a file, or URL to print the contents
     *            of.
     * @return An stream containing the contents of the file.
     */
    InputStream cat(String url, String revision) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.CAT, false);
        CmdArguments args = new CmdArguments();
        args.add("cat");
        args.add("-r");
        args.add(validRev(revision));
        args.add(url);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execInputStream(args);
    }

    /**
     * Sends local changes to the repository (equivalent to {@code svn commit}).
     *
     * @summary Commits the specified files or directories to the Subversion repository
     *          with an optional lock retention flag.
     *
     * @param path      The array of local file or directory paths to commit.
     * @param message   The commit log message describing the changes.
     * @param keepLocks If {@code true}, keeps any locks on the committed items; otherwise, releases them.
     * @return The output from the Subversion command-line client.
     * @throws CmdLineException If an error occurs during execution of the commit command.
     */
    String checkin(String[] path, String message, boolean keepLocks) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.COMMIT, true);
        CmdArguments args = new CmdArguments();
        args.add("ci");
        if (keepLocks) {
            args.add("--no-unlock");
        }
        args.addLogMessage(message);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);

        for (int i = 0; i < path.length; i++) {
            args.add(path[i]);
        }

        return execString(args, false);
    }

    /**
     * </p>
     * Recursively clean up the working copy, removing locks, resuming
     * unfinished operations.</p>
     *
     * @param path The local path to clean up.
     */
    void cleanup(String path) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.CLEANUP, true);
        CmdArguments args = new CmdArguments();
        args.add("cleanup");
        args.add(path);
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    /**
     * Checks out a working copy from the specified repository URL.
     *
     * @summary Performs a checkout from the given repository URL into a local directory, optionally recursively.
     *
     * @param url         The repository URL to check out from.
     * @param destination The local directory path where the working copy will be created.
     * @param revision    The revision to check out. If {@code null} or empty, defaults to {@code HEAD}.
     * @param recursive   {@code true} to check out directories recursively, {@code false} for non-recursive checkout.
     * @return The output from the Subversion command-line client.
     * @throws CmdLineException If an error occurs during the execution of the checkout command.
     */
    String checkout(String url, String destination, String revision, boolean recursive)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.CHECKOUT, true);
        CmdArguments args = new CmdArguments();
        args.add("co");
        args.add("-r");
        args.add(validRev(revision));
        args.add(url + "@" + validRev(revision));
        args.add(destination);

        if (!recursive) {
            args.add("-N");
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * </p>
     * Duplicate something in working copy or repos, remembering history.</p>
     *
     * <p>
     * <tt>src</tt> and <tt>dest</tt> can each be either a working copy (WC)
     * path or URL.</p>
     * <dl>
     * <dt>WC -&gt; WC</dt>
     * <dd>copy and schedule for addition (with history)</dd>
     *
     * <dt>WC -&gt; URL</dt>
     * <dd>immediately commit a copy of WC to URL</dd>
     *
     * <dt>URL -&gt; WC</dt>
     * <dd>check out URL into WC, schedule for addition</dd>
     *
     * <dt>URL -&gt; URL</dt>
     * <dd>complete server-side copy; used to branch and tag</dd>
     * </dl>
     *
     * @param src         Local path or URL to copy from.
     * @param dest        Local path or URL to copy to.
     * @param message     Commit message.
     * @param revision    Optional revision to copy from.
     * @param makeparents <code>true</code> <=> Create parents first when
     *                    copying from source url to dest url.
     */
    void copy(String src, String dest, String message, String revision, boolean makeparents) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.COPY, true);
        CmdArguments args = new CmdArguments();
        args.add("cp");
        if (revision != null) {
            args.add("-r");
            args.add(validRev(revision));
        }
        if (makeparents) {
            args.add("--parents");
        }
        if (message != null) {
            args.addLogMessage(message);
        }
        args.add(src);
        args.add(dest);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    /**
     * Copies a versioned resource within the local file system.
     *
     * @summary Performs a local Subversion copy operation, scheduling the destination for addition with history.
     *
     * @param src  The source local path of the resource to copy.
     * @param dest The destination local path where the resource will be copied to.
     * @throws CmdLineException If an error occurs while executing the copy command.
     */
    void copy(String src, String dest) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.COPY, true);
        CmdArguments args = new CmdArguments();
        args.add("cp");
        args.add(src);
        args.add(dest);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    /**
     * </p>
     * Remove files and directories from version control.</p>
     *
     * @param target  Local path or URL to remove.
     * @param message Associated message when deleting from URL.
     */
    String delete(String[] target, String message, boolean force) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.REMOVE, true);
        CmdArguments args = new CmdArguments();
        args.add("rm");
        if (message != null) {
            args.addLogMessage(message);
        }
        if (force) {
            args.add("--force");
        }
        for (int i = 0; i < target.length; i++) {
            args.add(target[i]);
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * </p>
     * Display the differences between two paths.</p>
     */
    InputStream diff(String oldPath, String oldRev, String newPath, String newRev, boolean recurse,
                     boolean ignoreAncestry, boolean noDiffDeleted, boolean force) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.DIFF, false);
        CmdArguments args = new CmdArguments();
        args.add("diff");
        if (newRev != null) {
            args.add("-r");
            if (newRev.equals("WORKING")) { // "WORKING" is not a valid revision argument at least in 0,35,1
                args.add(oldRev);
            } else {
                args.add(oldRev + ":" + newRev);
            }
        }
        if (!recurse) {
            args.add("-N");
        }
        if (!ignoreAncestry) {
            args.add("--notice-ancestry");
        }
        if (noDiffDeleted) {
            args.add("--no-diff-deleted");
        }
        if (force) {
            args.add("--force");
        }
        args.add("--old");
        args.add(oldPath);
        args.add("--new");
        args.add(newPath);
        args.addConfigInfo(this.configDir);
        return execInputStream(args);
    }

    /**
     * </p>
     * export files and directories from remote URL.</p>
     */
    void export(String url, String path, String revision, boolean force) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.EXPORT, true);
        CmdArguments args = new CmdArguments();
        args.add("export");
        args.add("-r");
        args.add(validRev(revision));
        args.add(url);
        args.add(path);
        if (force) {
            args.add("--force");
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    /**
     * </p>
     * Commit an unversioned file or directory into the repository.</p>
     *
     * @param path    Local path to import from.
     * @param url     Remote URL to import to.
     * @param message commit message
     */
    String importFiles(String path, String url, String message, boolean recursive)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.IMPORT, true);
        CmdArguments args = new CmdArguments();
        args.add("import");
        args.add(path);
        args.add(url);
        if (!recursive) {
            args.add("-N");
        }
        args.addLogMessage(message);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * Executes the {@code svn info} command to retrieve information about one or more working copy paths or URLs.
     *
     * @param target   An array of local paths or URLs to query information for.
     * @param revision The operative revision to use for retrieving info (may be {@code null}).
     * @param peg      The peg revision to resolve the specified targets (may be {@code null}).
     * @return A string containing the output of the {@code svn info} command.
     * @throws CmdLineException If the info command fails to execute properly.
     */
    String info(String[] target, String revision, String peg) throws CmdLineException {
        if (target.length == 0) {
            // otherwise we would do a "svn info" without args
            return "";
        }

        setCommand(ISVNNotifyListener.Command.INFO, false);
        CmdArguments args = new CmdArguments();
        args.add("info");
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        if (revision != null) {
            args.add("-r");
            args.add(revision);
        }
        for (int i = 0; i < target.length; i++) {
            if (peg == null) {
                args.add(target[i]);
            } else {
                args.add(target[i] + "@" + peg);
            }
        }

        return execString(args, false);
    }

    /**
     * </p>
     * List directory entries of a URL.</p>
     *
     * @param url       Remote URL.
     * @param revision  Revision to use. can be <tt>null</tt>
     *                  Defaults to <tt>HEAD</tt>.
     * @param recursive Should this operation recurse into sub-directories
     */
    byte[] list(String url, String revision, boolean recursive) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.LS, false);
        CmdArguments args = new CmdArguments();
        args.add("list");
        if (recursive) {
            args.add("-R");
        }
        args.add("--xml");
        args.add("-r");
        args.add(revision);
        args.add(url);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execBytes(args, false);
    }

    /**
     * </p>
     * Show the log messages for a set of revision(s) and/or file(s).</p>
     *
     * @param target   Local path or URL.
     * @param revision Optional revision range to get log messages from.
     */
    byte[] log(String target, String revision, boolean stopOnCopy, long limit) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.LOG, false);
        CmdArguments args = new CmdArguments();
        args.add("log");
        args.add("-r");
        args.add(validRev(revision));
        args.add(target);
        args.add("--xml");
        if (stopOnCopy) {
            args.add("--stop-on-copy");
        }
        if (limit > 0) {
            args.add("--limit");
            args.add(Long.toString(limit));
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execBytes(args, true);
    }

    /**
     * </p>
     * Show the log messages for a set of revision(s) and/or file(s).</p>
     * </p>
     * The difference to the methode log is the parameter -v
     *
     * @param target   Local path or URL.
     * @param paths    list of paths relative to target, may be null
     * @param revision Optional revision range to get log messages from.
     */
    byte[] logVerbose(String target,
                      String[] paths,
                      String revision,
                      boolean stopOnCopy,
                      long limit)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.LOG, false);
        CmdArguments args = new CmdArguments();
        args.add("log");
        args.add("-r");
        args.add(validRev(revision));
        args.add(target);
        if (paths != null) {
            for (int i = 0; i < paths.length; i++) {
                args.add(paths[i]);
            }
        }
        args.add("--xml");
        args.add("-v");
        if (stopOnCopy) {
            args.add("--stop-on-copy");
        }
        if (limit > 0) {
            args.add("--limit");
            args.add(Long.toString(limit));
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execBytes(args, true);
    }

    /**
     * </p>
     * Create a new directory under revision control.</p>
     *
     * @param url     URL to create. (contains existing url, followed by
     *                "/newDirectoryName").
     * @param message Commit message to send.
     */
    void mkdir(String url, String message) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.MKDIR, true);
        CmdArguments args = new CmdArguments();
        args.add("mkdir");
        args.addLogMessage(message);
        args.add(url);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    void mkdir(String localPath) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.MKDIR, true);
        CmdArguments args = new CmdArguments();
        args.add("mkdir");
        args.add(localPath);
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    /**
     * </p>
     * Move/rename something in working copy or repository.</p>
     *
     * <p>
     * <tt>source</tt> and <tt>dest</tt> can both be working copy (WC) paths or
     * URLs.</p>
     * <dt>WC -&gt; WC</dt>
     * <dd>move and schedule for addition (with history)</dd>
     * <dt>URL -&gt; URL</dt>
     * <dd>complete server-side rename.</dd>
     *
     * @param source  Local path or URL to move from.
     * @param dest    Local path or URL to move to.
     * @param message Optional message to send with URL.
     * @param force   Perform move even if there are modifications to working copy
     */
    String move(String source, String dest, String message, String revision, boolean force)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.MOVE, true);
        CmdArguments args = new CmdArguments();
        args.add("mv");
        args.add("-r");
        args.add(validRev(revision));
        args.add(source);
        args.add(dest);
        if (message != null) {
            args.addLogMessage(message);
        }
        if (force) {
            args.add("--force");
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * </p>
     * Print value of <tt>propName</tt> on files, dirs, or revisions.</p>
     *
     * @param path    path of resource.
     * @param propName Property name whose value we wish to find.
     */
    InputStream propget(String path, String propName) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.PROPGET, false);
        CmdArguments args = new CmdArguments();
        args.add("propget");
        args.add("--strict");
        args.add(propName);
        args.add(path);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execInputStream(args);
    }

    /**
     * </p>
     * Print value of <tt>propName</tt> on files, dirs, or revisions.</p>
     *
     * @param path    path of resource.
     * @param propName Property name whose value we wish to find.
     */
    InputStream propget(String path, String propName, String revision, String peg) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.PROPGET, false);
        CmdArguments args = new CmdArguments();
        args.add("propget");
        args.add("--strict");
        args.add("-r");
        args.add(revision);
        args.add(propName);
        args.add(path + "@" + peg);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execInputStream(args);
    }

    /**
     * </p>
     * Set <tt>propName</tt> to <tt>propVal</tt> on files or dirs.</p>
     *
     * @param propName  name of the property.
     * @param propValue New value to set <tt>propName</tt> to.
     * @param target    Local path to resource.
     */
    void propset(String propName, String propValue, String target, boolean recurse)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.PROPSET, false);
        CmdArguments args = new CmdArguments();
        args.add("propset");
        if (recurse) {
            args.add("-R");
        }
        args.add(propName);
        args.add(propValue);
        args.add(target);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    /**
     * Executes the {@code svn proplist} command to list the properties set on a given file or directory.
     *
     * @summary Retrieves the list of versioned properties associated with the specified resource using
     *          the Subversion command-line client.
     *          </p>
     *          If {@code recurse} is true and the target is a directory,
     *          the command will include properties of all child items recursively.
     *
     * @param target   The path to the file or directory whose properties should be listed.
     * @param recurse  If true, list properties recursively for all nested files and subdirectories.
     * @return A string containing the output of the {@code svn proplist} command.
     * @throws CmdLineException If an error occurs while executing the command.
     */
    String proplist(String target, boolean recurse) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.PROPLIST, false);
        CmdArguments args = new CmdArguments();
        args.add("proplist");
        if (recurse) {
            args.add("-R");
        }
        args.add(target);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);

        return execString(args, false);
    }

    /**
     * Executes the {@code svn propdel} command to remove a versioned property from a file or directory.
     *
     * @summary Removes the specified Subversion property from the target resource(s).
     *          </p>
     *          This method uses the Subversion command-line interface to delete a property named {@code propName}
     *          from the specified {@code target} path. If {@code recurse} is true and the target is a directory,
     *          the property will be deleted from all files and subdirectories recursively.
     *
     * @param propName The name of the property to delete.
     * @param target   The file or directory from which the property should be removed.
     * @param recurse  If true, apply the deletion recursively to all nested files and directories.
     * @throws CmdLineException If the command execution fails or the target is invalid.
     */
    void propdel(String propName, String target, boolean recurse) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.PROPDEL, true);
        CmdArguments args = new CmdArguments();
        args.add("propdel");
        if (recurse) {
            args.add("-R");
        }
        args.add(propName);
        args.add(target);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);

        execVoid(args);
    }

    /**
     * </p>
     * Sets a binary file as the value of a property.</p>
     *
     * @param propName name of the property.
     * @param propFile Local path to binary file.
     * @param target   Local path to resource.
     */
    void propsetFile(String propName, String propFile, String target, boolean recurse)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.PROPSET, false);
        CmdArguments args = new CmdArguments();
        args.add("propset");
        if (recurse) {
            args.add("-R");
        }
        args.add(propName);
        args.add("-F");
        args.add(propFile);
        args.add(target);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);

        execVoid(args);
    }

    /**
     * </p>
     * Restore pristine working copy file (undo all local edits).</p>
     *
     * @param paths     Local paths to revert.
     * @param recursive <tt>true</tt> if reverting subdirectories.
     */
    String revert(String[] paths, boolean recursive) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.REVERT, true);
        CmdArguments args = new CmdArguments();
        args.add("revert");
        if (recursive) {
            args.add("-R");
        }
        for (int i = 0; i < paths.length; i++) {
            args.add(paths[i]);
        }
        args.addConfigInfo(this.configDir);

        return execString(args, false);
    }

    /**
     * Executes the {@code svn resolved} command on the given files or directories.
     * </p>
     * Marks conflicted files or directories as resolved in the working copy.
     * </p>
     * This method removes the 'conflicted' state from the specified paths in the working copy.
     * It does not modify the file content or perform merging; it only updates the conflict status
     * to indicate that the conflict has been manually resolved.
     *
     * @param paths     An array of file or directory paths to mark as resolved.
     * @param recursive If true, apply the operation recursively to directories and their contents.
     * @throws CmdLineException If the underlying command-line execution fails.
     */
    void resolved(String[] paths, boolean recursive) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.RESOLVED, true);
        CmdArguments args = new CmdArguments();
        args.add("resolved");
        if (recursive) {
            args.add("-R");
        }
        for (int i = 0; i < paths.length; i++) {
            args.add(paths[i]);
        }
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    /**
     * </p>
     * Print the status of working copy files and directories.</p>
     *
     * @param path         Local path of resource to get status of.
     * @param allEntries   if false, only interesting entries will be get (local
     *                     mods and/or out-of-date).
     * @param checkUpdates Check for updates on server.
     */
    byte[] status(String[] path,
                  boolean descend,
                  boolean allEntries,
                  boolean checkUpdates,
                  boolean ignoreExternals)
            throws CmdLineException {
        if (path.length == 0) {
            // otherwise we would do a "svn status" without args
            return new byte[0];
        }
        setCommand(ISVNNotifyListener.Command.STATUS, false);
        CmdArguments args = new CmdArguments();
        args.add("status");
        args.add("--xml");
        if (allEntries) {
            args.add("-v");
        }
        if (!descend) {
            args.add("-N");
        }
        if (checkUpdates) {
            args.add("-u");
        }
        if (allEntries) {
            args.add("--no-ignore"); // disregard default and svn:ignore property ignores
        }
        if (ignoreExternals) {
            args.add("--ignore-externals");
        }

        for (int i = 0; i < path.length; i++) {
            args.add(path[i]);
        }

        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execBytes(args, false);
    }

    /**
     * </p>
     * Bring changes from the repository into the working copy.</p>
     *
     * @param path     Local path to possibly update.
     * @param revision Optional revision to update to.
     */
    String update(String path, String revision) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.UPDATE, true);
        CmdArguments args = new CmdArguments();
        args.add("up");
        args.add("-r");
        args.add(validRev(revision));
        args.add(path);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * </p>
     * Bring changes from the repository into the working copy.</p>
     *
     * @param paths    Local paths to possibly update.
     * @param revision Optional revision to update to.
     */
    String update(String[] paths, String revision) throws CmdLineException {
        StringBuffer pathsArg = new StringBuffer();
        for (int i = 0; i < paths.length; i++) {
            pathsArg.append(paths[i]);
            pathsArg.append(" ");
        }
        setCommand(ISVNNotifyListener.Command.UPDATE, true);
        CmdArguments args = new CmdArguments();
        args.add("up");
        args.add("-r");
        args.add(validRev(revision));
        args.add(pathsArg.toString());
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * Executes the {@code svn annotate} command on a given file or URL and returns the result in XML format.
     * </p>
     * Produces an annotated view of the file showing revision and author for each line.
     * </p>
     * This method retrieves the blame/annotate information for the specified path within the
     * provided revision range. The output includes details about which revision last modified each line,
     * who made the change, and when.
     *
     * @param path          The path to a file or URL to annotate.
     * @param revisionStart The starting revision of the range. May be {@code null} or empty to indicate no range.
     * @param revisionEnd   The ending revision of the range.
     * @return A {@code byte[]} containing the XML output of the annotation results.
     * @throws CmdLineException If execution of the underlying SVN command fails.
     */
    byte[] annotate(String path, String revisionStart, String revisionEnd) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.ANNOTATE, false);
        CmdArguments args = new CmdArguments();
        args.add("annotate");
        args.add("--xml");
        args.add("-r");
        if ((revisionStart != null) && (revisionStart.length() > 0)) {
            args.add(validRev(revisionStart) + ":" + validRev(revisionEnd));
        } else {
            args.add(validRev(revisionEnd));
        }
        args.add(path);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execBytes(args, false);
    }

    /**
     * Update the working copy to mirror a new URL within the repository.
     */
    String switchUrl(String path, String url, String revision, boolean recurse) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.SWITCH, true);
        CmdArguments args = new CmdArguments();
        args.add("sw");
        args.add(url);
        args.add(path);
        if (!recurse) {
            args.add("-N");
        }
        args.add("-r");
        args.add(validRev(revision));
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * Update the working copy to point to a new repository URL.
     */
    String relocate(String from, String to, String path, boolean recurse) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.RELOCATE, false);
        CmdArguments args = new CmdArguments();
        args.add("sw");
        args.add("--relocate");
        if (!recurse) {
            args.add("-N");
        }
        args.add(from);
        args.add(to);
        args.add(path);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * Update the working copy to mirror a new URL within the repository.
     */
    String merge(String path1,
                 String revision1,
                 String path2,
                 String revision2,
                 String localPath,
                 boolean force,
                 boolean recurse,
                 boolean dryRun,
                 boolean ignoreAncestry)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.MERGE, true);
        CmdArguments args = new CmdArguments();
        args.add("merge");
        if (!recurse) {
            args.add("-N");
        }
        if (force) {
            args.add("--force");
        }
        if (ignoreAncestry) {
            args.add("--ignore-ancestry");
        }
        if (dryRun) {
            args.add("--dry-run");
        }
        if (path1.equals(path2)) {
            args.add("-r");
            args.add(validRev(revision1) + ":" + validRev(revision2));
            args.add(path1);
        } else {
            args.add(path1 + "@" + validRev(revision1));
            args.add(path2 + "@" + validRev(revision2));
        }
        args.add(localPath);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

    /**
     * </p>
     * Set <tt>propName</tt> to <tt>propVal</tt> on revision
     * <tt>revision</tt>.</p>
     *
     * @param propName  name of the property.
     * @param propValue New value to set <tt>propName</tt> to.
     * @param target    Local path or URL to resource.
     * @param force     If the propset should be forced.
     */
    void revpropset(String propName, String propValue, String target, String revision, boolean force)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.PROPSET, false);
        CmdArguments args = new CmdArguments();
        args.add("propset");
        args.add(propName);

        args.add("--revprop");

        args.add(propValue);
        args.add(target);

        args.add("-r");
        args.add(revision);

        if (force) {
            args.add("--force");
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        execVoid(args);
    }

    /**
     * Locks one or more working copy paths or repository URLs with an optional comment.
     * </p>
     * Executes the {@code svn lock} command to lock resources in the working copy or repository.
     * </p>
     * This method is used to apply a lock on the specified paths, optionally with a comment.
     * If {@code --force} is specified, existing locks can be broken and the command will not fail
     * on errors from individual targets.
     *
     * @param paths   An array of working copy file paths or repository URLs to lock.
     * @param comment A comment associated with the lock, or {@code null} for no comment.
     * @param force   If {@code true}, includes the {@code --force} flag in the command.
     * @return The command-line output as a {@code String}.
     * @throws CmdLineException If an error occurs during execution of the command.
     */
    String lock(Object[] paths, String comment, boolean force)
            throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.LOCK, true);
        CmdArguments args = new CmdArguments();
        args.add("lock");
        if (force) {
            args.add("--force");
        }
        if (comment != null && !comment.equals("")) {
            args.add("-m");
            args.add(comment);
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);

        for (int i = 0; i < paths.length; i++) {
            args.add(paths[i]);
        }

        return execString(args, false);
    }

    /**
     * Unlocks one or more working copy paths or repository URLs.
     * </p>
     * Executes the {@code svn unlock} command to remove locks from specified resources.
     * </p>
     * This method attempts to unlock the specified working copy files or repository resources.
     * If {@code --force} is specified, the command will forcibly remove locks even if the user
     * does not own them.
     *
     * @param paths An array of working copy paths or repository URLs to unlock.
     * @param force If {@code true}, includes the {@code --force} flag to break locks not owned by the user.
     * @return The command-line output as a {@code String}.
     * @throws CmdLineException If an error occurs during execution of the unlock command.
     */
    String unlock(Object[] paths, boolean force) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.UNLOCK, true);
        CmdArguments args = new CmdArguments();
        args.add("unlock");
        if (force) {
            args.add("--force");
        }
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);

        for (int i = 0; i < paths.length; i++) {
            args.add(paths[i]);
        }

        return execString(args, false);
    }

    /*
     * (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.commandline.CommandLine#notifyFromSvnOutput(java.lang.String)
     */
    protected void notifyFromSvnOutput(String svnOutput) {
        this.rev = SVNRevision.SVN_INVALID_REVNUM;
        // we call the super implementation : handles logMessage and logCompleted
        super.notifyFromSvnOutput(svnOutput);

        if (parseSvnOutput) {
            // we parse the svn output
            CmdLineNotify notify = new CmdLineNotify() {

                public void onNotify(
                        String path,
                        int action,
                        int kind,
                        String mimeType,
                        int contentState,
                        int propState,
                        long revision) {
                    // we only call notifyListenersOfChange and logRevision
                    // logMessage and logCompleted have already been called
                    if (path != null) {
                        notificationHandler.notifyListenersOfChange(path);
                    }
                    if (revision != SVNRevision.SVN_INVALID_REVNUM) {
                        SvnCommandLine.this.rev = revision;
                        notificationHandler.logRevision(revision, path);
                    }
                }

            };

            try {
                svnOutputParser.addListener(notify);
                svnOutputParser.parse(svnOutput);
            } finally {
                svnOutputParser.removeListener(notify);
            }
        }

    }

    /**
     * Calls the base implementation of output notification handling.
     * </p>
     * This method delegates to {@code super.notifyFromSvnOutput()} to process
     * generic logging messages like {@code logMessage()} and {@code logCompleted()}.
     * It resets the internal revision state prior to calling the parent logic.
     * Subclasses can use this method to invoke the grandparent implementation explicitly.
     *
     * @param svnOutput The raw output from an SVN command.
     */
    protected void notifyMessagesFromSvnOutput(String svnOutput) {
        this.rev = SVNRevision.SVN_INVALID_REVNUM;
        // we call the super implementation : handles logMessage and logCompleted
        super.notifyFromSvnOutput(svnOutput);
    }

    /**
     * Sets the current SVN command and specifies how its output should be interpreted.
     * </p>
     * This method sets the current command being executed and determines whether
     * the SVN output should be parsed as notifications (e.g., {@code update}, {@code commit})
     * using {@code SvnOutputParser}, or treated as raw output (e.g., {@code list}, {@code cat}).
     *
     * @param command The SVN command identifier (see {@link ISVNNotifyListener.Command}).
     * @param ouputIsNotification {@code true} if the output should be parsed as notifications;
     *                             {@code false} otherwise.
     */
    protected void setCommand(int command, boolean ouputIsNotification) {
        this.parseSvnOutput = ouputIsNotification;
        notificationHandler.setCommand(command);
    }

    /**
     * get the revision notified for latest command. If an error occured, the
     * value of revision must be ignored
     *
     * @return Returns the revision.
     */
    public long getRevision() {
        return rev;
    }

}
