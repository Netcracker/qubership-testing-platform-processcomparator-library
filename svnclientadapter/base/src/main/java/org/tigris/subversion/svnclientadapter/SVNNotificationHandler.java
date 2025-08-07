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

package org.tigris.subversion.svnclientadapter;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Abstract base class for sending notifications to registered {@link ISVNNotifyListener}s.
 * <p>
 * Provides basic logging, error reporting, and change notification mechanisms
 * for SVN client operations.
 */
public abstract class SVNNotificationHandler {

    /** Set of registered notification listeners. */
    protected Set<ISVNNotifyListener> notifylisteners = new HashSet<>();

    /** Current SVN command code being executed. */
    protected int command;

    /** Flag indicating whether logging is currently enabled. */
    protected boolean logEnabled = true;

    /** Base directory used for resolving relative paths. */
    protected File baseDir = new File(".");

    /**
     * Registers a new {@link ISVNNotifyListener} to receive notification events.
     *
     * @param listener the listener to add
     */
    public void add(ISVNNotifyListener listener) {
        notifylisteners.add(listener);
    }

    /**
     * Unregisters an existing {@link ISVNNotifyListener}.
     *
     * @param listener the listener to remove
     */
    public void remove(ISVNNotifyListener listener) {
        notifylisteners.remove(listener);
    }

    /**
     * Enables logging of messages to listeners.
     */
    public void enableLog() {
        logEnabled = true;
    }

    /**
     * Disables logging of messages to listeners.
     */
    public void disableLog() {
        logEnabled = false;
    }

    /**
     * Logs a message to all listeners if logging is enabled.
     *
     * @param message the message to log
     */
    public void logMessage(String message) {
        if (logEnabled) {
            for (Iterator it = notifylisteners.iterator(); it.hasNext(); ) {
                ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
                listener.logMessage(message);
            }
        }
    }

    /**
     * Logs an error message to all listeners if logging is enabled.
     *
     * @param message the error message to log
     */
    public void logError(String message) {
        if (logEnabled) {
            for (Iterator it = notifylisteners.iterator(); it.hasNext(); ) {
                ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
                listener.logError(message);
            }
        }
    }

    /**
     * Logs a revision number and path to all listeners if logging is enabled.
     *
     * @param revision the revision number
     * @param path     the related path
     */
    public void logRevision(long revision, String path) {
        if (logEnabled) {
            for (Iterator it = notifylisteners.iterator(); it.hasNext(); ) {
                ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
                listener.logRevision(revision, path);
            }
        }
    }

    /**
     * Logs a completion message to all listeners if logging is enabled.
     *
     * @param message the completion message
     */
    public void logCompleted(String message) {
        if (logEnabled) {
            for (Iterator it = notifylisteners.iterator(); it.hasNext(); ) {
                ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
                listener.logCompleted(message);
            }
        }
    }

    /**
     * Sets the current SVN command and notifies all listeners.
     *
     * @param command the command ID
     */
    public void setCommand(int command) {
        this.command = command;
        for (Iterator it = notifylisteners.iterator(); it.hasNext(); ) {
            ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
            listener.setCommand(command);
        }
    }

    /**
     * Logs the command line being executed if logging is enabled
     * and the command is not in the skip list.
     *
     * @param commandLine the command line string
     */
    public void logCommandLine(String commandLine) {
        if (logEnabled && !skipCommand()) {
            for (Iterator it = notifylisteners.iterator(); it.hasNext(); ) {
                ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
                listener.logCommandLine(commandLine);
            }
        }
    }

    /**
     * Logs an exception and its cause chain to all listeners.
     *
     * @param clientException the exception to log
     */
    public void logException(Exception clientException) {
        if (logEnabled) {
            Throwable e = clientException;
            while (e != null) {
                logError(e.getMessage());
                e = e.getCause();
            }
        }
    }

    /**
     * Sets the base directory used for resolving relative paths.
     * If {@code baseDir} is {@code null}, the base directory will be auto-detected.
     *
     * @param baseDir the base directory, or {@code null} to reset to current directory
     */
    public void setBaseDir(File baseDir) {
        if (baseDir != null) {
            this.baseDir = baseDir;
        } else {
            setBaseDir();
        }
    }

    /**
     * Resets the base directory to the current directory.
     */
    public void setBaseDir() {
        this.baseDir = new File(".");
    }

    /**
     * Resolves a path into an absolute {@link File}, relative to {@link #baseDir}.
     *
     * @param path the path to resolve
     * @return the absolute file, or {@code null} if input is null
     */
    private File getAbsoluteFile(String path) {
        if (path == null) {
            return null;
        }
        File f = new File(path);
        if (!f.isAbsolute()) {
            f = new File(baseDir, path);
        }
        return f;
    }

    /**
     * Notifies all listeners that the specified path has changed.
     * The node kind is determined automatically from the file system.
     *
     * @param path the changed path
     */
    public void notifyListenersOfChange(String path) {
        if (path == null) {
            return;
        }
        File f = getAbsoluteFile(path);
        if (f == null) {
            // this should not happen
            logMessage("Warning : invalid path :" + path);
            return;
        }

        SVNNodeKind kind;
        if (f.isFile()) {
            kind = SVNNodeKind.FILE;
        } else if (f.isDirectory()) {
            kind = SVNNodeKind.DIR;
        } else {
            kind = SVNNodeKind.UNKNOWN;
        }

        for (Iterator it = notifylisteners.iterator(); it.hasNext(); ) {
            ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
            listener.onNotify(f, kind);
        }

    }

    /**
     * Notifies all listeners that the specified path has changed with known {@link SVNNodeKind}.
     *
     * @param path the changed path
     * @param kind the node kind
     */
    public void notifyListenersOfChange(String path, SVNNodeKind kind) {
        if (path == null) {
            return;
        }
        File f = getAbsoluteFile(path);
        if (f == null) {
            // this should not happen
            logMessage("Warning : invalid path :" + path);
            return;
        }

        for (Iterator it = notifylisteners.iterator(); it.hasNext(); ) {
            ISVNNotifyListener listener = (ISVNNotifyListener) it.next();
            listener.onNotify(f, kind);
        }
    }

    /**
     * Determines whether the current command should skip logging the command line.
     *
     * @return {@code true} if the command should be skipped from logging;
     *         {@code false} otherwise
     */
    protected boolean skipCommand() {
        return command == ISVNNotifyListener.Command.CAT
                || command == ISVNNotifyListener.Command.INFO
                || command == ISVNNotifyListener.Command.LOG
                || command == ISVNNotifyListener.Command.LS
                || command == ISVNNotifyListener.Command.PROPGET
                || command == ISVNNotifyListener.Command.PROPLIST
                || command == ISVNNotifyListener.Command.STATUS;
    }

}
