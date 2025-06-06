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

import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;

/**
 * SvnCommandLine subclass providing features compatible with version 1.2 of svn client.
 *
 * @author Martin Letenay
 */
public class SvnCommandLine12 extends SvnCommandLine {

    //Constructors
    SvnCommandLine12(String svnPath, CmdLineNotificationHandler notificationHandler) {
        super(svnPath, notificationHandler);
    }

    /**
     * Executes the {@code svn info} command to display information about one or more resources.
     * </p>
     * Prints metadata for each specified path, including repository URL, revision, node kind, and more.
     * Skips execution and returns an empty string if the input array is empty to avoid running
     * {@code svn info} without arguments.
     * </p>
     * <b>Usage:</b> {@code svn info [PATH [PATH ...]]}
     * </p>
     * <b>Valid options:</b>
     * <ul>
     *   <li>{@code --targets arg} — Pass contents of file {@code ARG} as additional args</li>
     *   <li>{@code -R}, {@code --recursive} — Descend recursively</li>
     * </ul>
     *
     * @param target An array of local paths or URLs to query.
     * @return The output of the {@code svn info} command as a string.
     * @throws CmdLineException If an error occurs during command execution.
     */
    String info(String[] target) throws CmdLineException {
        if (target.length == 0) {
            // otherwise we would do a "svn info" without args
            return "";
        }

        setCommand(ISVNNotifyListener.Command.INFO, false);
        CmdArguments args = new CmdArguments();
        args.add("info");
        args.addConfigInfo(this.configDir);
        for (int i = 0; i < target.length; i++) {
            args.add(target[i]);
        }

        return execString(args, false);
    }

    /**
     * </p>
     * Print the status of working copy files and directories.</p>
     *
     * @param path         Local path of resource to get status of.
     * @param allEntries   if false, only interesting entries will be get (local mods and/or out-of-date).
     * @param checkUpdates Check for updates on server.
     */
    String statusByStdout(String[] path,
                          boolean descend,
                          boolean allEntries,
                          boolean checkUpdates,
                          boolean ignoreExternals)
            throws CmdLineException {
        if (path.length == 0) {
            // otherwise we would do a "svn status" without args
            return "";
        }
        setCommand(ISVNNotifyListener.Command.STATUS, false);
        CmdArguments args = new CmdArguments();
        args.add("status");
        args.add("-v");
        if (!allEntries) {
            args.add("-q");
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
        return execString(args, false);
    }

    /**
     * Executes the {@code svn annotate} command and returns annotated file content as a string.
     * </p>
     * Outputs the contents of the specified file or URL, annotated with revision and author information
     * for each line. This version writes the result to standard output (stdout) and returns it as a plain string.
     * </p>
     * Equivalent to: {@code svn annotate -r REV[:REV2] <path>}
     *
     * @param path           The file or URL path to annotate.
     * @param revisionStart  The starting revision (optional). If {@code null}, only {@code revisionEnd} is used.
     * @param revisionEnd    The ending revision to annotate up to.
     * @return A {@code String} containing the output of the {@code svn annotate} command.
     * @throws CmdLineException If the underlying command execution fails.
     */
    String annotateByStdout(String path, String revisionStart, String revisionEnd) throws CmdLineException {
        setCommand(ISVNNotifyListener.Command.ANNOTATE, false);
        CmdArguments args = new CmdArguments();
        args.add("annotate");
        args.add("-r");
        if ((revisionStart != null) && (revisionStart.length() > 0)) {
            args.add(validRev(revisionStart) + ":" + validRev(revisionEnd));
        } else {
            args.add(validRev(revisionEnd));
        }
        args.add(path);
        args.addAuthInfo(this.user, this.pass);
        args.addConfigInfo(this.configDir);
        return execString(args, false);
    }

}
