/*******************************************************************************
 * Copyright (c) 2003, 2006 svnClientAdapter project and others.
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
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * High level API for Subversion.
 */
public interface ISVNClientAdapter {

    /**
     * constant identifying the "bdb"  repository type.
     */
    public static final String REPOSITORY_FSTYPE_BDB = "bdb";
    /**
     * constant identifying the "fsfs"  repository type.
     */
    public static final String REPOSITORY_FSTYPE_FSFS = "fsfs";

    public static final String[] DEFAULT_LOG_PROPERTIES = new String[]{"svn:author", "svn:date", "svn:log"};


    /**
     * Returns whether the client adapter implementation is threadsafe.
     */
    public abstract boolean isThreadsafe();

    /**
     * Add a notification listener.
     *
     * @param listener notify listener
     */
    public abstract void addNotifyListener(ISVNNotifyListener listener);

    /**
     * Remove a notification listener.
     *
     * @param listener notify listener
     */
    public abstract void removeNotifyListener(ISVNNotifyListener listener);

    /**
     * Get notification Handler.
     *
     * @return the notification handler
     */
    public abstract SVNNotificationHandler getNotificationHandler();

    /**
     * Sets the username.
     *
     * @param username username
     */
    public abstract void setUsername(String username);

    /**
     * Sets the password.
     *
     * @param password password
     */
    public abstract void setPassword(String password);

    /**
     * Add a callback for prompting for username, password SSL etc...
     *
     * @param callback callback
     */
    public abstract void addPasswordCallback(ISVNPromptUserPassword callback);

    /**
     * Add a callback for resolving conflicts during up/sw/merge.
     *
     * @param callback callback
     */
    public abstract void addConflictResolutionCallback(ISVNConflictResolver callback);

    /**
     * Set a progress listener.
     *
     * @param progressListener progress listener
     */
    public abstract void setProgressListener(ISVNProgressListener progressListener);

    /**
     * Adds a single file to version control in the working copy.
     * The file is scheduled for addition and will be committed with the next commit operation.
     *
     * @param file the file to be added to version control.
     * @throws SVNClientException if an error occurs during the addition.
     */
    public abstract void addFile(File file) throws SVNClientException;

    /**
     * Adds a directory and optionally its contents to version control.
     * The directory is scheduled for addition and will be committed with the next commit operation.
     *
     * @param dir     the directory to be added.
     * @param recurse if true, adds all children recursively; if false, adds only the top-level directory.
     * @throws SVNClientException if an error occurs during the addition.
     */
    public abstract void addDirectory(File dir, boolean recurse) throws SVNClientException;

    /**
     * Adds a directory and optionally its contents to version control, with an option to force the addition.
     * The directory is scheduled for addition and will be committed with the next commit operation.
     *
     * @param dir     the directory to be added.
     * @param recurse if true, adds all children recursively.
     * @param force   if true, adds files even if they are already versioned or ignored.
     * @throws SVNClientException if an error occurs during the addition.
     */
    public abstract void addDirectory(File dir, boolean recurse, boolean force) throws SVNClientException;

    /**
     * Checks out a working copy from a given URL to a local path.
     *
     * @param moduleName the repository URL to check out.
     * @param destPath   the local directory where the working copy will be created.
     * @param revision   the revision to check out; if -1, the HEAD revision is used.
     * @param recurse    if true, check out recursively; if false, only the top-level directory is checked out.
     * @throws SVNClientException if an error occurs during the checkout.
     */
    public abstract void checkout(SVNUrl moduleName,
                                  File destPath,
                                  SVNRevision revision,
                                  boolean recurse)
            throws SVNClientException;

    /**
     * Checks out a working copy from a given URL to a local path with fine-grained control.
     *
     * @param moduleName      the repository URL to check out.
     * @param destPath        the local directory where the working copy will be created.
     * @param revision        the revision to check out; if -1, the HEAD revision is used.
     * @param depth           the depth to check out (e.g., empty, files, immediates, infinity).
     * @param ignoreExternals whether to ignore externals definitions during the checkout.
     * @param force           allow unversioned paths that obstruct additions to be overwritten.
     * @throws SVNClientException if an error occurs during the checkout.
     */
    public abstract void checkout(SVNUrl moduleName,
                                  File destPath,
                                  SVNRevision revision,
                                  int depth,
                                  boolean ignoreExternals,
                                  boolean force)
            throws SVNClientException;

    /**
     * Commits the specified files or directories to the repository.
     *
     * @param paths   the array of files/directories to commit.
     * @param message the commit message describing the changes.
     * @param recurse if true, includes subdirectories recursively.
     * @return the new revision number after the commit, or -1 if the revision is invalid.
     * @throws SVNClientException if an error occurs during the commit.
     */
    public abstract long commit(File[] paths, String message, boolean recurse) throws SVNClientException;

    /**
     * Commits the specified files or directories to the repository, with an option to retain locks.
     *
     * @param paths     the array of files/directories to commit.
     * @param message   the commit message describing the changes.
     * @param recurse   if true, includes subdirectories recursively.
     * @param keepLocks if true, retains any locks held on the committed files.
     * @return the new revision number after the commit, or -1 if the revision is invalid.
     * @throws SVNClientException if an error occurs during the commit.
     */
    public abstract long commit(File[] paths,
                                String message,
                                boolean recurse,
                                boolean keepLocks)
            throws SVNClientException;

    /**
     * Commits changes across multiple working copies in a single or multiple commit operations.
     *
     * @param paths     the array of files/directories from potentially different working copies.
     * @param message   the commit message describing the changes.
     * @param recurse   if true, includes subdirectories recursively.
     * @param keepLocks if true, retains any locks held on the committed files.
     * @param atomic    if true, commits all items from the same repository in one commit operation;
     *                  otherwise, commits each working copy separately.
     * @return an array of new revision numbers for each commit; -1 indicates an invalid revision.
     * @throws SVNClientException if an error occurs during the commit.
     */
    public abstract long[] commitAcrossWC(File[] paths,
                                          String message,
                                          boolean recurse,
                                          boolean keepLocks,
                                          boolean atomic)
            throws SVNClientException;

    /**
     * Returns the error message (if any) that occurred after a commit operation.
     *
     * @return the error message string, or null if no error occurred.
     */
    public String getPostCommitError();

    /**
     * Lists the entries of a directory at a given URL and revision.
     *
     * @param url      the URL of the directory to list.
     * @param revision the revision to list the directory at.
     * @param recurse  if true, list directories recursively.
     * @return an array of {@link ISVNDirEntry} objects representing the contents.
     * @throws SVNClientException if an error occurs while retrieving the list.
     */
    public abstract ISVNDirEntry[] getList(SVNUrl url, SVNRevision revision, boolean recurse) throws SVNClientException;

    /**
     * Lists the entries of a directory at a given URL and revision, using a peg revision for resolving renames.
     *
     * @param url         the URL of the directory to list.
     * @param revision    the operative revision to list the directory at.
     * @param pegRevision the revision in which the URL is valid.
     * @param recurse     if true, list directories recursively.
     * @return an array of {@link ISVNDirEntry} objects.
     * @throws SVNClientException if an error occurs during the operation.
     */
    public abstract ISVNDirEntry[] getList(SVNUrl url,
                                           SVNRevision revision,
                                           SVNRevision pegRevision,
                                           boolean recurse)
            throws SVNClientException;

    /**
     * Lists the entries of a local directory.
     *
     * @param path     the local directory path.
     * @param revision the revision to use for listing.
     * @param recurse  whether to list entries recursively.
     * @return an array of {@link ISVNDirEntry} objects.
     * @throws SVNClientException if an error occurs during the listing.
     */
    public ISVNDirEntry[] getList(File path, SVNRevision revision, boolean recurse) throws SVNClientException;

    /**
     * Lists the entries of a local directory, using a peg revision.
     *
     * @param path        the local directory.
     * @param revision    the revision to list.
     * @param pegRevision the peg revision to resolve path.
     * @param recurse     whether to recurse into subdirectories.
     * @return an array of {@link ISVNDirEntry}.
     * @throws SVNClientException if an error occurs.
     */
    public ISVNDirEntry[] getList(File path,
                                  SVNRevision revision,
                                  SVNRevision pegRevision,
                                  boolean recurse)
            throws SVNClientException;

    /**
     * Lists directory entries of a given URL along with associated lock information.
     *
     * @param url         the URL of the directory to list.
     * @param revision    the revision to list the directory at.
     * @param pegRevision the revision in which the URL is valid.
     * @param recurse     if true, list recursively.
     * @return an array of {@link ISVNDirEntryWithLock} including lock metadata.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNDirEntryWithLock[] getListWithLocks(SVNUrl url,
                                                            SVNRevision revision,
                                                            SVNRevision pegRevision,
                                                            boolean recurse)
            throws SVNClientException;

    /**
     * Returns metadata about a specific directory or file at the given URL and revision.
     *
     * @param url      the repository URL.
     * @param revision the revision at which to retrieve the metadata.
     * @return an {@link ISVNDirEntry} with directory or file metadata.
     * @throws SVNClientException if an error occurs.
     */
    public ISVNDirEntry getDirEntry(SVNUrl url, SVNRevision revision) throws SVNClientException;

    /**
     * Returns metadata about a specific directory or file in the working copy.
     *
     * @param path     the local file or directory path.
     * @param revision the revision to query metadata for.
     * @return an {@link ISVNDirEntry} describing the entry.
     * @throws SVNClientException if an error occurs.
     */
    public ISVNDirEntry getDirEntry(File path, SVNRevision revision) throws SVNClientException;

    /**
     * Returns the status of a single file or directory in the working copy.
     *
     * @param path the file or directory to check.
     * @return a {@link ISVNStatus} representing the status.
     * @throws SVNClientException if an error occurs while retrieving the status.
     */
    public abstract ISVNStatus getSingleStatus(File path) throws SVNClientException;

    /**
     * Returns the status for an array of files or directories.
     *
     * @param path an array of files or directories to check.
     * @return an array of {@link ISVNStatus} objects.
     * @throws SVNClientException if an error occurs during status retrieval.
     */
    public abstract ISVNStatus[] getStatus(File[] path) throws SVNClientException;

    /**
     * Returns the status of a file or directory and optionally its children.
     *
     * @param path    the target file or directory.
     * @param descend whether to recurse into subdirectories.
     * @param getAll  whether to include all items, or only modified/out-of-date ones.
     * @return an array of {@link ISVNStatus} entries.
     * @throws SVNClientException if the status operation fails.
     */
    public abstract ISVNStatus[] getStatus(File path,
                                           boolean descend,
                                           boolean getAll)
            throws SVNClientException;

    /**
     * Returns the status of a path and its children, optionally contacting the repository.
     *
     * @param path          the file or directory.
     * @param descend       recurse into children.
     * @param getAll        include all entries or only interesting ones.
     * @param contactServer whether to contact the repository for status.
     * @return an array of {@link ISVNStatus}.
     * @throws SVNClientException if an error occurs.
     */
    public abstract ISVNStatus[] getStatus(File path,
                                           boolean descend,
                                           boolean getAll,
                                           boolean contactServer)
            throws SVNClientException;

    /**
     * Returns the status of a path with additional options.
     *
     * @param path            the file or directory.
     * @param descend         recurse into children.
     * @param getAll          include all items.
     * @param contactServer   contact the server for remote changes.
     * @param ignoreExternals ignore externals definitions.
     * @return an array of {@link ISVNStatus}.
     * @throws SVNClientException if status operation fails.
     */
    public abstract ISVNStatus[] getStatus(File path,
                                           boolean descend,
                                           boolean getAll,
                                           boolean contactServer,
                                           boolean ignoreExternals)
            throws SVNClientException;

    /**
     * Returns the status of a path with the ability to process each result via a callback.
     *
     * @param path            file or directory to check.
     * @param descend         recurse into subdirectories.
     * @param getAll          include unmodified items.
     * @param contactServer   if true, contact server for changes.
     * @param ignoreExternals whether to ignore externals.
     * @param callback        callback for status entries.
     * @return an array of {@link ISVNStatus} objects.
     * @throws SVNClientException on error.
     */
    public abstract ISVNStatus[] getStatus(File path,
                                           boolean descend,
                                           boolean getAll,
                                           boolean contactServer,
                                           boolean ignoreExternals,
                                           ISVNStatusCallback callback)
            throws SVNClientException;

    /**
     * Returns the status of a path, allowing for complete control of traversal and filters.
     *
     * @param path            target file or directory.
     * @param descend         recurse into subdirectories.
     * @param getAll          include all items.
     * @param contactServer   contact the repository for remote info.
     * @param ignoreExternals if true, skip externals.
     * @param noIgnore        if true, include ignored items.
     * @param callback        status callback interface.
     * @return an array of {@link ISVNStatus}.
     * @throws SVNClientException if operation fails.
     */
    public abstract ISVNStatus[] getStatus(File path,
                                           boolean descend,
                                           boolean getAll,
                                           boolean contactServer,
                                           boolean ignoreExternals,
                                           boolean noIgnore,
                                           ISVNStatusCallback callback)
            throws SVNClientException;

    /**
     * Copies a file or directory within the working copy and schedules it for addition with history.
     *
     * @param srcPath  the source file or directory in the working copy.
     * @param destPath the destination path where the item will be copied.
     * @throws SVNClientException if an error occurs during the copy.
     */
    public abstract void copy(File srcPath, File destPath) throws SVNClientException;

    /**
     * Immediately commits a working copy item to the given repository URL.
     *
     * @param srcPath the source item from the working copy.
     * @param destUrl the destination URL in the repository.
     * @param message the commit message.
     * @throws SVNClientException if an error occurs during commit.
     */
    public abstract void copy(File srcPath, SVNUrl destUrl, String message) throws SVNClientException;

    /**
     * Immediately commits multiple working copy items to the given repository URL.
     *
     * @param srcPaths    array of source paths in the working copy.
     * @param destUrl     destination URL in the repository.
     * @param message     commit message.
     * @param copyAsChild if true, each source is copied as a child of destUrl.
     * @param makeParents if true, creates intermediate directories if needed.
     * @throws SVNClientException if an error occurs during commit.
     */
    public abstract void copy(File[] srcPaths,
                              SVNUrl destUrl,
                              String message,
                              boolean copyAsChild,
                              boolean makeParents)
            throws SVNClientException;

    /**
     * Checks out a URL into the working copy and schedules it for addition.
     *
     * @param srcUrl   source URL in the repository.
     * @param destPath local destination path.
     * @param revision revision to checkout.
     * @throws SVNClientException if the checkout fails.
     */
    public abstract void copy(SVNUrl srcUrl, File destPath, SVNRevision revision) throws SVNClientException;

    /**
     * Checks out a URL into the working copy and schedules it for addition.
     *
     * @param srcUrl      source URL in the repository.
     * @param destPath    local destination path.
     * @param revision    revision to checkout.
     * @param copyAsChild if true, adds as child of destination.
     * @param makeParents if true, creates intermediate directories.
     * @throws SVNClientException if an error occurs.
     */
    public abstract void copy(SVNUrl srcUrl,
                              File destPath,
                              SVNRevision revision,
                              boolean copyAsChild,
                              boolean makeParents)
            throws SVNClientException;

    /**
     * Checks out a URL into the working copy and schedules it for addition, using a peg revision.
     *
     * @param srcUrl      source URL in the repository.
     * @param destPath    local destination path.
     * @param revision    operative revision.
     * @param pegRevision peg revision to resolve the URL.
     * @param copyAsChild add as child of destination path.
     * @param makeParents create intermediate folders if needed.
     * @throws SVNClientException if an error occurs.
     */
    public abstract void copy(SVNUrl srcUrl,
                              File destPath,
                              SVNRevision revision,
                              SVNRevision pegRevision,
                              boolean copyAsChild,
                              boolean makeParents)
            throws SVNClientException;

    /**
     * Performs a server-side copy operation from srcUrl to destUrl.
     *
     * @param srcUrl   source URL in repository.
     * @param destUrl  destination URL in repository.
     * @param message  commit message.
     * @param revision revision to copy from.
     * @throws SVNClientException if the copy fails.
     */
    public abstract void copy(SVNUrl srcUrl,
                              SVNUrl destUrl,
                              String message,
                              SVNRevision revision)
            throws SVNClientException;

    /**
     * Server-side copy with parent directory creation option.
     *
     * @param srcUrl      source URL.
     * @param destUrl     destination URL.
     * @param message     commit message.
     * @param revision    revision to copy from.
     * @param makeParents create intermediate directories.
     * @throws SVNClientException if copy fails.
     */
    public abstract void copy(SVNUrl srcUrl,
                              SVNUrl destUrl,
                              String message,
                              SVNRevision revision,
                              boolean makeParents)
            throws SVNClientException;

    /**
     * Performs a server-side copy of multiple URLs to a destination URL.
     *
     * @param srcUrls     array of source URLs.
     * @param destUrl     target URL.
     * @param message     commit message.
     * @param revision    revision to copy from.
     * @param copyAsChild copy each URL as child of destination.
     * @param makeParents create intermediate directories.
     * @throws SVNClientException if an error occurs.
     */
    public abstract void copy(SVNUrl[] srcUrls,
                              SVNUrl destUrl,
                              String message,
                              SVNRevision revision,
                              boolean copyAsChild,
                              boolean makeParents)
            throws SVNClientException;

    /**
     * Deletes the specified repository URLs via immediate commit.
     *
     * @param url     array of repository URLs to delete.
     * @param message commit message.
     * @throws SVNClientException if deletion fails.
     */
    public abstract void remove(SVNUrl[] url, String message) throws SVNClientException;

    /**
     * Schedules files or directories in the working copy for deletion.
     *
     * @param file  array of files or directories to delete.
     * @param force if true, forces deletion even with unversioned or modified content.
     * @throws SVNClientException if deletion fails.
     */
    public abstract void remove(File[] file, boolean force) throws SVNClientException;

    /**
     * Exports a clean directory tree from a repository URL at a specified revision.
     *
     * @param srcUrl   source repository URL.
     * @param destPath destination directory.
     * @param revision revision to export.
     * @param force    overwrite destination if it exists.
     * @throws SVNClientException if export fails.
     */
    public abstract void doExport(SVNUrl srcUrl,
                                  File destPath,
                                  SVNRevision revision,
                                  boolean force)
            throws SVNClientException;

    /**
     * Exports a clean copy of a local working directory or file.
     * Unversioned files will not be copied.
     *
     * @param srcPath  source local path.
     * @param destPath destination path.
     * @param force    overwrite destination if it exists.
     * @throws SVNClientException if export fails.
     */
    public abstract void doExport(File srcPath, File destPath, boolean force) throws SVNClientException;

    /**
     * Imports a local file or directory into a repository directory.
     *
     * @param path    local path to import.
     * @param url     target URL in the repository.
     * @param message log message for the import.
     * @param recurse import recursively if path is a directory.
     * @throws SVNClientException if import fails.
     */
    public abstract void doImport(File path, SVNUrl url, String message, boolean recurse) throws SVNClientException;

    /**
     * Creates a new directory in the repository at the specified URL.
     *
     * @param url     the target URL where directory should be created.
     * @param message log message for the operation.
     * @throws SVNClientException if directory creation fails.
     */
    public abstract void mkdir(SVNUrl url, String message) throws SVNClientException;

    /**
     * Creates a directory at a given URL in the repository, with option to create parent directories.
     *
     * @param url         target URL.
     * @param makeParents whether to create missing parent directories.
     * @param message     commit message.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void mkdir(SVNUrl url, boolean makeParents, String message) throws SVNClientException;

    /**
     * Creates a directory in the working copy and schedules it for addition.
     *
     * @param file local directory to create.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void mkdir(File file) throws SVNClientException;

    /**
     * Moves or renames a file or directory in the working copy.
     *
     * @param srcPath  source path.
     * @param destPath destination path.
     * @param force    whether to overwrite existing destination.
     * @throws SVNClientException if move fails.
     */
    public abstract void move(File srcPath, File destPath, boolean force) throws SVNClientException;

    /**
     * Moves or renames a file or directory in the repository via a commit.
     *
     * @param srcUrl   source URL.
     * @param destUrl  destination URL.
     * @param message  log message.
     * @param revision revision to move from.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void move(SVNUrl srcUrl,
                              SVNUrl destUrl,
                              String message,
                              SVNRevision revision)
            throws SVNClientException;

    /**
     * Updates the working copy to the specified revision.
     *
     * @param path     local file or directory.
     * @param revision target revision to update to.
     * @param recurse  update recursively.
     * @return the revision number after update.
     * @throws SVNClientException if update fails.
     */
    public abstract long update(File path, SVNRevision revision, boolean recurse) throws SVNClientException;

    /**
     * Updates the working copy to the specified revision with fine-grained control.
     *
     * @param path            target file or directory.
     * @param revision        revision to update to.
     * @param depth           update depth (e.g., infinity, files, immediates).
     * @param setDepth        change the working copy depth.
     * @param ignoreExternals whether to ignore externals.
     * @param force           force update even if unversioned obstructions exist.
     * @return the updated revision number.
     * @throws SVNClientException if the update fails.
     */
    public abstract long update(File path,
                                SVNRevision revision,
                                int depth,
                                boolean setDepth,
                                boolean ignoreExternals,
                                boolean force)
            throws SVNClientException;

    /**
     * Updates the directories or files from repository.
     *
     * @param path            array of target files.
     * @param revision        the revision number to update.
     * @param recurse         recursively update.
     * @param ignoreExternals if externals are ignored during update
     * @return Returns an array of longs representing the revision. It returns a
     *         -1 if the revision number is invalid.
     * @throws SVNClientException if the update fails.
     * @since 1.2
     */
    public abstract long[] update(
            File[] path,
            SVNRevision revision,
            boolean recurse,
            boolean ignoreExternals)
            throws SVNClientException;

    /**
     * Updates the directories or files from repository.
     *
     * @param path            array of target files.
     * @param revision        the revision number to update.
     * @param depth           the depth to recursively update.
     * @param setDepth        change working copy to specified depth
     * @param ignoreExternals if externals are ignored during update.
     * @param force           allow unversioned paths that obstruct adds.
     * @return Returns an array of longs representing the revision. It returns a
     *        -1 if the revision number is invalid.
     * @throws SVNClientException if the update fails.
     */
    public abstract long[] update(
            File[] path,
            SVNRevision revision,
            int depth,
            boolean setDepth,
            boolean ignoreExternals,
            boolean force)
            throws SVNClientException;

    /**
     * Reverts local modifications for the specified file or directory,
     * restoring it to its pristine state.
     *
     * @param path    the file or directory to revert.
     * @param recurse whether to revert recursively in case of a directory.
     * @throws SVNClientException if the revert operation fails.
     */
    public abstract void revert(File path, boolean recurse) throws SVNClientException;

    /**
     * Retrieves log messages for a range of revisions on a repository URL.
     *
     * @param url           the target repository URL.
     * @param revisionStart the starting revision.
     * @param revisionEnd   the ending revision.
     * @return an array of log messages in the specified revision range.
     * @throws SVNClientException if the log retrieval fails.
     */
    public abstract ISVNLogMessage[] getLogMessages(SVNUrl url,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd)
            throws SVNClientException;

    /**
     * Retrieves log messages with optional path change details.
     *
     * @param url             the target repository URL.
     * @param revisionStart   the starting revision.
     * @param revisionEnd     the ending revision.
     * @param fetchChangePath if true, include paths modified in each revision.
     * @return an array of log messages.
     * @throws SVNClientException if an error occurs during retrieval.
     */
    public abstract ISVNLogMessage[] getLogMessages(SVNUrl url,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd,
                                                    boolean fetchChangePath)
            throws SVNClientException;

    /**
     * Retrieves log messages for specific paths within a repository.
     *
     * @param url             the repository URL.
     * @param paths           specific paths to include in the log.
     * @param revStart        the start revision.
     * @param revEnd          the end revision.
     * @param stopOnCopy      whether to stop on copy operations.
     * @param fetchChangePath include detailed path changes.
     * @return an array of log messages.
     * @throws SVNClientException if retrieval fails.
     */
    public ISVNLogMessage[] getLogMessages(final SVNUrl url, final String[] paths,
                                           SVNRevision revStart, SVNRevision revEnd,
                                           boolean stopOnCopy, boolean fetchChangePath) throws SVNClientException;

    /**
     * Retrieves log messages for a working copy path within a revision range.
     *
     * @param path          local working copy path.
     * @param revisionStart the starting revision.
     * @param revisionEnd   the ending revision.
     * @return an array of log messages.
     * @throws SVNClientException if an error occurs.
     */
    public abstract ISVNLogMessage[] getLogMessages(File path,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd)
            throws SVNClientException;

    /**
     * Retrieves log messages for a local path, with optional path change details.
     *
     * @param path            working copy path.
     * @param revisionStart   start revision.
     * @param revisionEnd     end revision.
     * @param fetchChangePath whether to include modified paths.
     * @return log messages.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract ISVNLogMessage[] getLogMessages(File path,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd,
                                                    boolean fetchChangePath)
            throws SVNClientException;

    /**
     * Retrieves log messages for a file with path and merge information.
     *
     * @param path            file or directory path.
     * @param revisionStart   first revision.
     * @param revisionEnd     last revision.
     * @param stopOnCopy      whether to stop on copy history.
     * @param fetchChangePath include changed paths per revision.
     * @return array of log messages.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract ISVNLogMessage[] getLogMessages(File path,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd,
                                                    boolean stopOnCopy,
                                                    boolean fetchChangePath)
            throws SVNClientException;

    /**
     * Retrieves a limited number of log messages.
     *
     * @param path            file or directory.
     * @param revisionStart   start revision.
     * @param revisionEnd     end revision.
     * @param stopOnCopy      whether to stop on copy operations.
     * @param fetchChangePath include changed paths.
     * @param limit           maximum number of messages to return.
     * @return array of log messages.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNLogMessage[] getLogMessages(File path,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd,
                                                    boolean stopOnCopy,
                                                    boolean fetchChangePath,
                                                    long limit)
            throws SVNClientException;

    /**
     * Retrieves log messages including merged revisions.
     *
     * @param path                   file or directory.
     * @param pegRevision            peg revision for path resolution.
     * @param revisionStart          start revision.
     * @param revisionEnd            end revision.
     * @param stopOnCopy             stop on copy operations.
     * @param fetchChangePath        include changed paths.
     * @param limit                  maximum number of messages.
     * @param includeMergedRevisions whether to include merged revisions.
     * @return array of log messages.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNLogMessage[] getLogMessages(File path,
                                                    SVNRevision pegRevision,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd,
                                                    boolean stopOnCopy,
                                                    boolean fetchChangePath,
                                                    long limit,
                                                    boolean includeMergedRevisions)
            throws SVNClientException;

    /**
     * Retrieves log messages for a remote URL.
     *
     * @param url             the repository URL.
     * @param pegRevision     peg revision for URL resolution.
     * @param revisionStart   starting revision.
     * @param revisionEnd     ending revision.
     * @param stopOnCopy      stop on copy operations.
     * @param fetchChangePath include changed paths.
     * @param limit           limit number of messages.
     * @return array of log messages.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract ISVNLogMessage[] getLogMessages(SVNUrl url,
                                                    SVNRevision pegRevision,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd,
                                                    boolean stopOnCopy,
                                                    boolean fetchChangePath,
                                                    long limit)
            throws SVNClientException;

    /**
     * Retrieves log messages for a remote URL, with option to include merged revisions.
     *
     * @param url                    repository URL.
     * @param pegRevision            peg revision for resolution.
     * @param revisionStart          first revision.
     * @param revisionEnd            last revision.
     * @param stopOnCopy             whether to stop on copy.
     * @param fetchChangePath        include changed paths.
     * @param limit                  limit number of entries.
     * @param includeMergedRevisions include merged revisions.
     * @return array of log messages.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract ISVNLogMessage[] getLogMessages(SVNUrl url,
                                                    SVNRevision pegRevision,
                                                    SVNRevision revisionStart,
                                                    SVNRevision revisionEnd,
                                                    boolean stopOnCopy,
                                                    boolean fetchChangePath,
                                                    long limit,
                                                    boolean includeMergedRevisions)
            throws SVNClientException;

    /**
     * Retrieves log messages for a local path, using a callback interface.
     *
     * @param path                   local path.
     * @param pegRevision            peg revision.
     * @param revisionStart          starting revision.
     * @param revisionEnd            ending revision.
     * @param stopOnCopy             whether to stop on copy operations.
     * @param fetchChangePath        include changed paths.
     * @param limit                  max messages to retrieve.
     * @param includeMergedRevisions include merged revisions.
     * @param requestedProperties    specific revision properties to retrieve.
     * @param callback               callback to receive log messages.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract void getLogMessages(File path,
                                        SVNRevision pegRevision,
                                        SVNRevision revisionStart,
                                        SVNRevision revisionEnd,
                                        boolean stopOnCopy,
                                        boolean fetchChangePath,
                                        long limit,
                                        boolean includeMergedRevisions,
                                        String[] requestedProperties,
                                        ISVNLogMessageCallback callback)
            throws SVNClientException;

    /**
     * Retrieves log messages for a URL using a callback.
     *
     * @param url                    repository URL.
     * @param pegRevision            peg revision for resolution.
     * @param revisionStart          start revision.
     * @param revisionEnd            end revision.
     * @param stopOnCopy             whether to stop on copy operations.
     * @param fetchChangePath        include changed paths.
     * @param limit                  number of log entries to fetch.
     * @param includeMergedRevisions include merged revisions.
     * @param requestedProperties    properties to include in log.
     * @param callback               callback to process each log entry.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract void getLogMessages(SVNUrl url,
                                        SVNRevision pegRevision,
                                        SVNRevision revisionStart,
                                        SVNRevision revisionEnd,
                                        boolean stopOnCopy,
                                        boolean fetchChangePath,
                                        long limit,
                                        boolean includeMergedRevisions,
                                        String[] requestedProperties,
                                        ISVNLogMessageCallback callback)
            throws SVNClientException;

    /**
     * Retrieves the contents of a file from the repository at a specific revision.
     *
     * @param url         the repository URL of the file.
     * @param revision    the revision to retrieve.
     * @param pegRevision the peg revision used to resolve the path.
     * @return an InputStream containing the file contents.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract InputStream getContent(SVNUrl url,
                                           SVNRevision revision,
                                           SVNRevision pegRevision)
            throws SVNClientException;

    /**
     * Retrieves the contents of a file from the repository.
     *
     * @param url      the repository URL of the file.
     * @param revision the specific revision to retrieve.
     * @return an InputStream with the file content.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract InputStream getContent(SVNUrl url, SVNRevision revision) throws SVNClientException;


    /**
     * Retrieves the content of a file at a specific revision from the working copy.
     *
     * @param path     the local file path.
     * @param revision the desired revision of the file.
     * @return an InputStream containing the file content at the specified revision.
     * @throws SVNClientException if an error occurs during content retrieval.
     */
    public InputStream getContent(File path, SVNRevision revision) throws SVNClientException;

    /**
     * Sets a Subversion property on a file or directory.
     *
     * @param path          the target file or directory.
     * @param propertyName  the name of the property to set.
     * @param propertyValue the value of the property.
     * @param recurse       whether to set the property recursively.
     * @throws SVNClientException if setting the property fails.
     */
    public abstract void propertySet(File path,
                                     String propertyName,
                                     String propertyValue,
                                     boolean recurse)
            throws SVNClientException;

    /**
     * Sets a revision property on a specific URL and revision.
     *
     * @param url           the repository URL.
     * @param baseRev       the base revision.
     * @param propertyName  the name of the property.
     * @param propertyValue the value of the property.
     * @param message       a commit message (if required).
     * @throws SVNClientException if setting the property fails.
     */
    public abstract void propertySet(SVNUrl url,
                                     SVNRevision.Number baseRev,
                                     String propertyName,
                                     String propertyValue,
                                     String message)
            throws SVNClientException;

    /**
     * Sets a property using the contents of a file.
     *
     * @param path         the local file or directory.
     * @param propertyName the name of the property to set.
     * @param propertyFile the file whose contents will be used as the property value.
     * @param recurse      whether to apply recursively.
     * @throws SVNClientException if the operation fails.
     * @throws IOException        if reading the property file fails.
     */
    public abstract void propertySet(File path,
                                     String propertyName,
                                     File propertyFile,
                                     boolean recurse)
            throws SVNClientException, IOException;

    /**
     * Gets a property from a file or directory.
     *
     * @param path         the local path.
     * @param propertyName the name of the property.
     * @return the property value or null if not found.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNProperty propertyGet(File path, String propertyName) throws SVNClientException;

    /**
     * Gets a property from a repository URL.
     *
     * @param url          the repository URL.
     * @param propertyName the name of the property.
     * @return the property value or null if not found.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract ISVNProperty propertyGet(SVNUrl url, String propertyName) throws SVNClientException;

    /**
     * Gets a property from a repository URL at a specific revision.
     *
     * @param url          the repository URL.
     * @param revision     the target revision.
     * @param peg          the peg revision.
     * @param propertyName the property name.
     * @return the property value or null if not found.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract ISVNProperty propertyGet(SVNUrl url,
                                             SVNRevision revision,
                                             SVNRevision peg,
                                             String propertyName)
            throws SVNClientException;

    /**
     * Deletes a property from a file or directory.
     *
     * @param path         the local file or directory.
     * @param propertyName the name of the property to delete.
     * @param recurse      whether to delete recursively.
     * @throws SVNClientException if deletion fails.
     */
    public abstract void propertyDel(File path, String propertyName, boolean recurse) throws SVNClientException;

    /**
     * Sets a revision property for a specific revision number.
     *
     * @param path         the repository URL.
     * @param revisionNo   the revision number.
     * @param propName     the property name.
     * @param propertyData the new property value.
     * @param force        whether to force the operation.
     * @throws SVNClientException if setting the property fails.
     */
    public abstract void setRevProperty(SVNUrl path,
                                        SVNRevision.Number revisionNo,
                                        String propName,
                                        String propertyData,
                                        boolean force)
            throws SVNClientException;

    /**
     * Retrieves a revision property value for a given revision.
     *
     * @param path       the repository URL.
     * @param revisionNo the revision number.
     * @param propName   the property name.
     * @return the property value or null.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract String getRevProperty(SVNUrl path,
                                          SVNRevision.Number revisionNo,
                                          String propName)
            throws SVNClientException;

    /**
     * Retrieves ignore patterns for a given directory.
     *
     * @param path the local directory path.
     * @return a list of ignore patterns or null if not a directory.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract List getIgnoredPatterns(File path) throws SVNClientException;

    /**
     * Adds a pattern to the svn:ignore property for a directory.
     *
     * @param path    the local directory.
     * @param pattern the pattern to ignore.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void addToIgnoredPatterns(File path, String pattern) throws SVNClientException;

    /**
     * Sets the svn:ignore property with the provided list of patterns.
     *
     * @param path     the local directory.
     * @param patterns the list of patterns to ignore.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void setIgnoredPatterns(File path, List patterns) throws SVNClientException;

    /**
     * Displays differences between two file or directory paths.
     *
     * @param oldPath         the original path.
     * @param oldPathRevision the revision of the original path.
     * @param newPath         the updated path.
     * @param newPathRevision the revision of the updated path.
     * @param outFile         file to write the diff output.
     * @param recurse         whether to recurse into directories.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void diff(File oldPath,
                              SVNRevision oldPathRevision,
                              File newPath,
                              SVNRevision newPathRevision,
                              File outFile,
                              boolean recurse)
            throws SVNClientException;

    /**
     * Displays differences between two paths with advanced options.
     *
     * @param oldPath         the original path.
     * @param oldPathRevision the original revision.
     * @param newPath         the new path.
     * @param newPathRevision the new revision.
     * @param outFile         output file for diff.
     * @param recurse         recurse into subdirectories.
     * @param ignoreAncestry  ignore file ancestry during comparison.
     * @param noDiffDeleted   do not display deleted files.
     * @param force           force operation despite conflicts.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void diff(File oldPath,
                              SVNRevision oldPathRevision,
                              File newPath,
                              SVNRevision newPathRevision,
                              File outFile,
                              boolean recurse,
                              boolean ignoreAncestry,
                              boolean noDiffDeleted,
                              boolean force)
            throws SVNClientException;

    /**
     * Displays local modifications for a working copy file or directory.
     *
     * @param path    the file or directory.
     * @param outFile output file for diff.
     * @param recurse whether to recurse into subdirectories.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void diff(File path,
                              File outFile,
                              boolean recurse)
            throws SVNClientException;

    /**
     * Displays combined differences for multiple working copy paths.
     *
     * @param paths   array of paths to compare.
     * @param outFile output file for diff result.
     * @param recurse whether to recurse into subdirectories.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void diff(File[] paths,
                              File outFile,
                              boolean recurse)
            throws SVNClientException;

    /**
     * Displays the differences between two repository URLs at specific revisions.
     *
     * @param oldUrl         original repository URL.
     * @param oldUrlRevision revision of the original URL.
     * @param newUrl         updated repository URL.
     * @param newUrlRevision revision of the updated URL.
     * @param outFile        output file for diff result.
     * @param recurse        whether to recurse into directories.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void diff(SVNUrl oldUrl,
                              SVNRevision oldUrlRevision,
                              SVNUrl newUrl,
                              SVNRevision newUrlRevision,
                              File outFile,
                              boolean recurse)
            throws SVNClientException;

    /**
     * Displays differences between two URLs at specified revisions.
     *
     * @param oldUrl         the original repository URL.
     * @param oldUrlRevision the revision of the original URL.
     * @param newUrl         the target repository URL.
     * @param newUrlRevision the revision of the target URL.
     * @param outFile        the file to write the diff output to.
     * @param recurse        whether to recurse into subdirectories.
     * @param ignoreAncestry whether to ignore ancestry in diff comparison.
     * @param noDiffDeleted  whether to suppress output of deleted files.
     * @param force          whether to force diff even if errors occur.
     * @throws SVNClientException if the diff operation fails.
     */
    public abstract void diff(SVNUrl oldUrl,
                              SVNRevision oldUrlRevision,
                              SVNUrl newUrl,
                              SVNRevision newUrlRevision,
                              File outFile,
                              boolean recurse,
                              boolean ignoreAncestry,
                              boolean noDiffDeleted,
                              boolean force)
            throws SVNClientException;

    /**
     * Displays the differences between a URL at a range of revisions.
     *
     * @param target          the repository URL to compare.
     * @param pegRevision     the peg revision to interpret the target.
     * @param startRevision   the start revision of the comparison range.
     * @param endRevision     the end revision of the comparison range.
     * @param outFile         the file to write the diff output to.
     * @param depth           depth of recursion for the diff.
     * @param ignoreAncestry  whether to ignore ancestry.
     * @param noDiffDeleted   suppress output of deleted files.
     * @param force           force diff despite conflicts.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void diff(SVNUrl target,
                              SVNRevision pegRevision,
                              SVNRevision startRevision,
                              SVNRevision endRevision,
                              File outFile,
                              int depth,
                              boolean ignoreAncestry,
                              boolean noDiffDeleted,
                              boolean force)
            throws SVNClientException;

    /**
     * Displays the differences between a URL at a range of revisions.
     *
     * @param target        the repository URL to compare.
     * @param pegRevision   the peg revision.
     * @param startRevision the start revision of the comparison.
     * @param endRevision   the end revision of the comparison.
     * @param outFile       the file to write the diff to.
     * @param recurse       whether to recurse into directories.
     * @throws SVNClientException if the diff fails.
     */
    public abstract void diff(SVNUrl target,
                              SVNRevision pegRevision,
                              SVNRevision startRevision,
                              SVNRevision endRevision,
                              File outFile,
                              boolean recurse)
            throws SVNClientException;

    /**
     * Displays differences between two revisions of the same URL.
     *
     * @param url             the repository URL.
     * @param oldUrlRevision  the older revision.
     * @param newUrlRevision  the newer revision.
     * @param outFile         the file to write the diff output to.
     * @param recurse         whether to recurse into directories.
     * @throws SVNClientException if the diff fails.
     */
    public abstract void diff(SVNUrl url,
                              SVNRevision oldUrlRevision,
                              SVNRevision newUrlRevision,
                              File outFile,
                              boolean recurse)
            throws SVNClientException;

    /**
     * Displays differences between the working copy and a repository URL.
     *
     * @param path        the local working copy path.
     * @param url         the repository URL to compare with.
     * @param urlRevision the revision of the URL.
     * @param outFile     the output file for the diff.
     * @param recurse     whether to recurse into directories.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void diff(File path,
                              SVNUrl url,
                              SVNRevision urlRevision,
                              File outFile,
                              boolean recurse)
            throws SVNClientException;

    /**
     * Creates a patch file from local modifications.
     *
     * @param paths          array of modified files or directories.
     * @param relativeToPath path to compute relative paths in the patch.
     * @param outFile        output file for the patch.
     * @param recurse        whether to recurse into directories.
     * @throws SVNClientException if patch creation fails.
     */
    public abstract void createPatch(File[] paths,
                                     File relativeToPath,
                                     File outFile,
                                     boolean recurse)
            throws SVNClientException;

    /**
     * Returns the keywords currently set for substitution in a file.
     *
     * @param path the local file path.
     * @return the set of keywords used for substitution.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract SVNKeywords getKeywords(File path) throws SVNClientException;

    /**
     * Sets the keywords for substitution on a file or directory.
     *
     * @param path     the file or directory.
     * @param keywords the keywords to apply.
     * @param recurse  whether to apply recursively.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void setKeywords(File path, SVNKeywords keywords, boolean recurse) throws SVNClientException;

    /**
     * Adds keywords to the substitution list for the given file.
     *
     * @param path     the file to modify.
     * @param keywords the keywords to add.
     * @return the updated keywords after addition.
     * @throws SVNClientException if the operation fails.
     */
    public abstract SVNKeywords addKeywords(File path, SVNKeywords keywords) throws SVNClientException;

    /**
     * Removes keywords from the substitution list for the given file.
     *
     * @param path     the file to modify.
     * @param keywords the keywords to remove.
     * @return the updated keywords after removal.
     * @throws SVNClientException if the operation fails.
     */
    public SVNKeywords removeKeywords(File path, SVNKeywords keywords) throws SVNClientException;

    /**
     * Produces annotated content (blame) for a URL.
     *
     * @param url            the repository URL.
     * @param revisionStart  the earliest revision to show.
     * @param revisionEnd    the latest revision to show.
     * @return annotation data containing revision and author info.
     * @throws SVNClientException if the annotation fails.
     */
    public ISVNAnnotations annotate(SVNUrl url,
                                    SVNRevision revisionStart,
                                    SVNRevision revisionEnd)
            throws SVNClientException;

    /**
     * Produces annotated content (blame) for a file.
     *
     * @param file           the local file.
     * @param revisionStart  the earliest revision to show.
     * @param revisionEnd    the latest revision to show.
     * @return annotation data containing revision and author info.
     * @throws SVNClientException if the annotation fails.
     */
    public ISVNAnnotations annotate(File file,
                                    SVNRevision revisionStart,
                                    SVNRevision revisionEnd)
            throws SVNClientException;

    /**
     * Produces annotated content (blame) for a URL with extra options.
     *
     * @param url                   the repository URL.
     * @param revisionStart         the earliest revision to show.
     * @param revisionEnd           the latest revision to show.
     * @param pegRevision           the peg revision for URL resolution.
     * @param ignoreMimeType        ignore files with certain MIME types.
     * @param includeMergedRevisions include merged changes in annotation.
     * @return annotation data with metadata.
     * @throws SVNClientException if annotation fails.
     */
    public ISVNAnnotations annotate(SVNUrl url,
                                    SVNRevision revisionStart,
                                    SVNRevision revisionEnd,
                                    SVNRevision pegRevision,
                                    boolean ignoreMimeType,
                                    boolean includeMergedRevisions)
            throws SVNClientException;

    /**
     * Produces annotated content (blame) for a file with additional options.
     *
     * @param file                  the local file.
     * @param revisionStart         the earliest revision to show.
     * @param revisionEnd           the latest revision to show.
     * @param ignoreMimeType        whether to ignore MIME types.
     * @param includeMergedRevisions include merged changes in annotation.
     * @return annotation result.
     * @throws SVNClientException if the annotation fails.
     */
    public ISVNAnnotations annotate(File file,
                                    SVNRevision revisionStart,
                                    SVNRevision revisionEnd,
                                    boolean ignoreMimeType,
                                    boolean includeMergedRevisions)
            throws SVNClientException;

    /**
     * Produces annotated content (blame) for a file with all options.
     *
     * @param file                  the local file.
     * @param revisionStart         the earliest revision to show.
     * @param revisionEnd           the latest revision to show.
     * @param pegRevision           peg revision for resolution.
     * @param ignoreMimeType        ignore MIME-type files.
     * @param includeMergedRevisions include merged changes in annotation.
     * @return annotated file content.
     * @throws SVNClientException if annotation fails.
     */
    public ISVNAnnotations annotate(File file,
                                    SVNRevision revisionStart,
                                    SVNRevision revisionEnd,
                                    SVNRevision pegRevision,
                                    boolean ignoreMimeType,
                                    boolean includeMergedRevisions)
            throws SVNClientException;

    /**
     * Retrieves all properties for the given URL at the specified revision and peg revision.
     *
     * @param url       the target repository URL.
     * @param revision  the operative revision to get properties from.
     * @param peg       the peg revision to resolve the URL.
     * @return an array of properties for the specified URL.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNProperty[] getProperties(SVNUrl url,
                                                 SVNRevision revision,
                                                 SVNRevision peg)
            throws SVNClientException;

    /**
     * Retrieves all properties for the given URL using the HEAD revision.
     *
     * @param url the target repository URL.
     * @return an array of properties for the specified URL.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNProperty[] getProperties(SVNUrl url) throws SVNClientException;

    /**
     * Retrieves revision properties for a specific revision of a given URL.
     *
     * @param url      the target repository URL.
     * @param revision the specific revision number.
     * @return an array of revision properties.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNProperty[] getRevProperties(SVNUrl url, SVNRevision.Number revision) throws SVNClientException;

    /**
     * Marks a working copy path as resolved, clearing conflict state.
     *
     * @param path the local file or directory to resolve.
     * @throws SVNClientException if the operation fails.
     */
    public abstract void resolved(File path) throws SVNClientException;

    /**
     * Resolves a working copy path using a specified conflict resolution strategy.
     *
     * @param path   the local path to resolve.
     * @param result the chosen resolution strategy (see {@link ISVNConflictResolver.Choice}).
     * @throws SVNClientException if the operation fails.
     */
    public abstract void resolve(File path, int result) throws SVNClientException;

    /**
     * Creates a new, empty repository at the specified path.
     *
     * @param path           the filesystem path for the new repository.
     * @param repositoryType type of repository (e.g., BDB or FSFS), or null to use default.
     * @throws SVNClientException if repository creation fails.
     */
    public abstract void createRepository(File path, String repositoryType) throws SVNClientException;

    /**
     * Cancels the current operation if in progress.
     *
     * @throws SVNClientException if cancellation fails.
     */
    public void cancelOperation() throws SVNClientException;

    /**
     * Retrieves info from the working copy without contacting the repository.
     *
     * @param file the local file or directory.
     * @return metadata about the file/directory.
     * @throws SVNClientException if the operation fails.
     */
    public ISVNInfo getInfoFromWorkingCopy(File file) throws SVNClientException;

    /**
     * Retrieves info from the working copy, contacting the repository.
     *
     * @param file the local file or directory.
     * @return metadata about the file/directory.
     * @throws SVNClientException if the operation fails.
     */
    public ISVNInfo getInfo(File file) throws SVNClientException;

    /**
     * Retrieves info recursively from the working copy.
     *
     * @param file    the local file or directory.
     * @param descend whether to retrieve info recursively.
     * @return an array of metadata objects.
     * @throws SVNClientException if the operation fails.
     */
    public ISVNInfo[] getInfo(File file, boolean descend) throws SVNClientException;

    /**
     * Retrieves info for a repository URL.
     *
     * @param url the target repository URL.
     * @return metadata about the URL.
     * @throws SVNClientException if the operation fails.
     */
    public ISVNInfo getInfo(SVNUrl url) throws SVNClientException;

    /**
     * Retrieves info for a repository URL at a specific revision and peg.
     *
     * @param url      the repository URL.
     * @param revision the operative revision.
     * @param peg      the peg revision.
     * @return metadata about the URL.
     * @throws SVNClientException if the operation fails.
     */
    public ISVNInfo getInfo(SVNUrl url, SVNRevision revision, SVNRevision peg) throws SVNClientException;

    /**
     * Switches the working copy to a new URL within the same repository.
     *
     * @param path     the working copy path.
     * @param url      the new repository URL.
     * @param revision the revision to switch to.
     * @param recurse  whether to recurse into subdirectories.
     * @throws SVNClientException if the switch fails.
     */
    public void switchToUrl(File path, SVNUrl url, SVNRevision revision, boolean recurse) throws SVNClientException;

    /**
     * Switches the working copy to a new URL with additional options.
     *
     * @param path            the working copy path.
     * @param url             the new repository URL.
     * @param revision        the revision to switch to.
     * @param depth           the depth of the switch.
     * @param setDepth        whether to explicitly set the depth.
     * @param ignoreExternals whether to ignore externals.
     * @param force           whether to force the switch.
     * @throws SVNClientException if the operation fails.
     */
    public void switchToUrl(File path,
                            SVNUrl url,
                            SVNRevision revision,
                            int depth,
                            boolean setDepth,
                            boolean ignoreExternals,
                            boolean force)
            throws SVNClientException;

    /**
     * Switches the working copy to a new URL with peg revision.
     *
     * @param path            the working copy path.
     * @param url             the new repository URL.
     * @param revision        operative revision.
     * @param pegRevision     peg revision to resolve URL.
     * @param depth           depth of switch.
     * @param setDepth        whether to set depth explicitly.
     * @param ignoreExternals ignore externals during switch.
     * @param force           force the operation.
     * @throws SVNClientException if the operation fails.
     */
    public void switchToUrl(File path,
                            SVNUrl url,
                            SVNRevision revision,
                            SVNRevision pegRevision,
                            int depth,
                            boolean setDepth,
                            boolean ignoreExternals,
                            boolean force)
            throws SVNClientException;

    /**
     * Switches the working copy with ancestry ignore support.
     *
     * @param path            the working copy path.
     * @param url             the new repository URL.
     * @param revision        operative revision.
     * @param pegRevision     peg revision.
     * @param depth           depth of switch.
     * @param setDepth        whether to set depth.
     * @param ignoreExternals whether to ignore externals.
     * @param force           whether to force.
     * @param ignoreAncestry  whether to ignore ancestry.
     * @throws SVNClientException if the operation fails.
     */
    public void switchToUrl(File path,
                            SVNUrl url,
                            SVNRevision revision,
                            SVNRevision pegRevision,
                            int depth,
                            boolean setDepth,
                            boolean ignoreExternals,
                            boolean force,
                            boolean ignoreAncestry)
            throws SVNClientException;

    /**
     * Sets the configuration directory used by the client.
     *
     * @param dir the directory to use for configuration.
     * @throws SVNClientException if the directory cannot be set.
     */
    public void setConfigDirectory(File dir) throws SVNClientException;

    /**
     * Performs cleanup on the specified working copy directory.
     *
     * @param dir the directory to clean up.
     * @throws SVNClientException if cleanup fails.
     */
    public abstract void cleanup(File dir) throws SVNClientException;

    /**
     * Upgrades the working copy metadata format to the latest supported version.
     *
     * @param dir the working copy root.
     * @throws SVNClientException if upgrade fails.
     */
    public abstract void upgrade(File dir) throws SVNClientException;

    /**
     * Indicates whether the {@code commitAcrossWC} method is supported.
     *
     * @return {@code true} if the adapter implementation supports commit across multiple working copies.
     */
    public abstract boolean canCommitAcrossWC();

    /**
     * Returns the name of the Subversion administrative working copy directory.
     * Typically this is ".svn".
     *
     * @return the name of the administrative directory.
     */
    public abstract String getAdminDirectoryName();

    /**
     * Determines if a given name corresponds to a Subversion administrative directory.
     *
     * @param name the name of the directory.
     * @return {@code true} if the name is recognized as an admin directory (e.g., ".svn").
     */
    public abstract boolean isAdminDirectory(String name);

    /**
     * Rewrites URLs in the working copy, typically after repository relocation.
     *
     * @param from    the original repository root URL.
     * @param to      the new repository root URL.
     * @param path    the local working copy path.
     * @param recurse whether to recurse into subdirectories.
     * @throws SVNClientException if relocation fails.
     */
    public abstract void relocate(String from, String to, String path, boolean recurse) throws SVNClientException;

    /**
     * Merge changes from two paths into a new local path.
     * @param path1         first path or url
     * @param revision1     first revision
     * @param path2         second path or url
     * @param revision2     second revision
     * @param localPath     target local path
     * @param force         overwrite local changes
     * @param recurse       traverse into subdirectories
     * @exception SVNClientException if an error occurs during the merge operation,
     *                              such as conflicts, invalid paths, or repository access issues
     */
    public abstract void merge(SVNUrl path1, SVNRevision revision1, SVNUrl path2,
                               SVNRevision revision2, File localPath, boolean force,
                               boolean recurse) throws SVNClientException;

    /**
     * Merge changes from two paths into a new local path.
     * @param path1         first path or url
     * @param revision1     first revision
     * @param path2         second path or url
     * @param revision2     second revision
     * @param localPath     target local path
     * @param force         overwrite local changes
     * @param recurse       traverse into subdirectories
     * @param dryRun        do not update working copy
     * @exception SVNClientException    if an error occurs during the merge operation,
     *                                  such as conflicts, invalid paths, or repository access issues
     */
    public abstract void merge(SVNUrl path1, SVNRevision revision1, SVNUrl path2,
                               SVNRevision revision2, File localPath, boolean force,
                               boolean recurse, boolean dryRun) throws SVNClientException;

    /**
     * Merge changes from two paths into a new local path.
     * @param path1         first path or url
     * @param revision1     first revision
     * @param path2         second path or url
     * @param revision2     second revision
     * @param localPath     target local path
     * @param force         overwrite local changes
     * @param recurse       traverse into subdirectories
     * @param dryRun        do not update working copy
     * @param ignoreAncestry ignore ancestry when calculating merges
     * @exception SVNClientException    if an error occurs during the merge operation,
     *                                  such as conflicts, invalid paths, or repository access issues
     */
    public abstract void merge(SVNUrl path1, SVNRevision revision1, SVNUrl path2,
                               SVNRevision revision2, File localPath, boolean force,
                               boolean recurse, boolean dryRun, boolean ignoreAncestry) throws SVNClientException;

    /**
     * Merge changes from two paths into a new local path.
     * @param path1         first path or url
     * @param revision1     first revision
     * @param path2         second path or url
     * @param revision2     second revision
     * @param localPath     target local path
     * @param force         overwrite local changes
     * @param depth         depth
     * @param dryRun        do not update working copy
     * @param ignoreAncestry ignore ancestry when calculating merges
     * @param recordOnly    just records mergeinfo, does not perform merge
     * @exception SVNClientException    if an error occurs during the merge operation,
     *                                  such as conflicts, invalid paths, or repository access issues
     */
    public abstract void merge(SVNUrl path1, SVNRevision revision1, SVNUrl path2,
                               SVNRevision revision2, File localPath, boolean force,
                               int depth, boolean dryRun, boolean ignoreAncestry,
                               boolean recordOnly) throws SVNClientException;


    /**
     * Retrieves merge information for the specified local path at a specific revision.
     *
     * @param path     the local working copy path.
     * @param revision the revision to query.
     * @return merge information.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract ISVNMergeInfo getMergeInfo(File path, SVNRevision revision) throws SVNClientException;

    /**
     * Retrieves merge information for the specified URL at a specific revision.
     *
     * @param url      the repository URL.
     * @param revision the revision to query.
     * @return merge information.
     * @throws SVNClientException if retrieval fails.
     */
    public abstract ISVNMergeInfo getMergeInfo(SVNUrl url, SVNRevision revision) throws SVNClientException;

    /**
     * Retrieves merge history (either merged or eligible revisions) for a local path.
     *
     * @param kind                 type of revisions (e.g., merged, eligible).
     * @param path                 target path.
     * @param pegRevision          peg revision to resolve the path.
     * @param mergeSourceUrl       source URL of the merge.
     * @param srcPegRevision       peg revision for the source URL.
     * @param discoverChangedPaths whether to retrieve changed paths.
     * @return array of log messages for the revisions.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNLogMessage[] getMergeinfoLog(int kind,
                                                     File path,
                                                     SVNRevision pegRevision,
                                                     SVNUrl mergeSourceUrl,
                                                     SVNRevision srcPegRevision,
                                                     boolean discoverChangedPaths)
            throws SVNClientException;

    /**
     * Retrieves merge history (either merged or eligible revisions) for a repository URL.
     *
     * @param kind                 type of revisions (e.g., merged, eligible).
     * @param url                  target URL.
     * @param pegRevision          peg revision to resolve the URL.
     * @param mergeSourceUrl       source URL of the merge.
     * @param srcPegRevision       peg revision for the source URL.
     * @param discoverChangedPaths whether to retrieve changed paths.
     * @return array of log messages for the revisions.
     * @throws SVNClientException if the operation fails.
     */
    public abstract ISVNLogMessage[] getMergeinfoLog(int kind,
                                                     SVNUrl url,
                                                     SVNRevision pegRevision,
                                                     SVNUrl mergeSourceUrl,
                                                     SVNRevision srcPegRevision,
                                                     boolean discoverChangedPaths)
            throws SVNClientException;

    /**
     * Produces a summary of changes between two repository URLs and revisions.
     *
     * @param target1        the first URL.
     * @param revision1      revision of the first target.
     * @param target2        the second URL.
     * @param revision2      revision of the second target.
     * @param depth          depth to recurse during comparison.
     * @param ignoreAncestry whether to ignore file ancestry.
     * @return array of difference summaries.
     * @throws SVNClientException if the diff operation fails.
     */
    public abstract SVNDiffSummary[] diffSummarize(SVNUrl target1, SVNRevision revision1,
                                                   SVNUrl target2, SVNRevision revision2,
                                                   int depth, boolean ignoreAncestry) throws SVNClientException;

    /**
     * Produces a summary of changes for a single target over a revision range.
     *
     * @param target         the URL to analyze.
     * @param pegRevision    peg revision of the target.
     * @param startRevision  start of revision range.
     * @param endRevision    end of revision range.
     * @param depth          depth to recurse during comparison.
     * @param ignoreAncestry whether to ignore ancestry.
     * @return array of difference summaries.
     * @throws SVNClientException if the diff operation fails.
     */
    public abstract SVNDiffSummary[] diffSummarize(SVNUrl target, SVNRevision pegRevision,
                                                   SVNRevision startRevision, SVNRevision endRevision,
                                                   int depth, boolean ignoreAncestry) throws SVNClientException;

    /**
     * Produces a summary of changes between a working copy path and a URL.
     *
     * @param path        the working copy path.
     * @param toUrl       the URL to compare against.
     * @param toRevision  the revision of the URL.
     * @param recurse     whether to recurse into subdirectories.
     * @return array of difference summaries.
     * @throws SVNClientException if the diff operation fails.
     */
    public abstract SVNDiffSummary[] diffSummarize(File path,
                                                   SVNUrl toUrl,
                                                   SVNRevision toRevision,
                                                   boolean recurse)
            throws SVNClientException;

    /**
     * Suggests a list of merge source URLs for a local path.
     *
     * @param path the local path to analyze.
     * @return array of suggested merge source URLs.
     * @throws SVNClientException if the operation fails.
     */
    public abstract String[] suggestMergeSources(File path) throws SVNClientException;

    /**
     * Suggests a list of merge source URLs for a repository URL.
     *
     * @param url the repository URL to analyze.
     * @param peg the peg revision to resolve the URL.
     * @return array of suggested merge source URLs.
     * @throws SVNClientException if the operation fails.
     */
    public abstract String[] suggestMergeSources(SVNUrl url, SVNRevision peg) throws SVNClientException;

    /**
     * Releases any native resources or handles held by the client adapter.
     * Should be called explicitly, as relying on finalization is discouraged.
     */
    public abstract void dispose();

}
