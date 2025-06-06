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
import java.io.IOException;

import org.tigris.subversion.svnclientadapter.utils.StringUtils;

/**
 * During notification (both with svn command line and javahl), the files and
 * directories are sometimes relative (with svn commit for ex). However it is
 * not relative to current directory but relative to the common parent of the
 * current directory and the working copy target
 * </p>
 * ex : if working copy is at /home/cedric/programmation/sources/test and
 * current dir is /home/cedric/projects/subversion/subclipse
 * </p>
 * $svn ci /home/cedric/programmation/sources/test/essai8 Adding
 * programmation/sources/test/essai8
 *
 * @author Cedric Chabanois (cchab at tigris.org)
 * @author John M Flinchbaugh (john at hjsoft.com)
 */
public class SVNBaseDir {

    /**
     * Returns the deepest common ancestor directory between two files, or {@code null}
     * if there is no common directory.
     * </p>
     * If both files are the same, returns the file itself.
     * If the files reside on different root paths (e.g., different drives on Windows),
     * returns {@code null}.
     *
     * @param file1 the first file
     * @param file2 the second file
     * @return the common ancestor directory, or {@code null} if none exists
     */
    protected static File getCommonPart(File file1, File file2) {
        if (file1 == null) {
            return null;
        }
        if (file2 == null) {
            return null;
        }
        String file1AbsPath;
        String file2AbsPath;
        file1AbsPath = file1.getAbsolutePath();
        file2AbsPath = file2.getAbsolutePath();

        if (file1AbsPath.equals(file2AbsPath)) {
            return new File(file1AbsPath);
        }

        String[] file1Parts = StringUtils.split(file1AbsPath,
                File.separatorChar);
        String[] file2Parts = StringUtils.split(file2AbsPath,
                File.separatorChar);
        if (file1Parts[0].equals("")) {
            file1Parts[0] = File.separator;
        }
        if (file2Parts[0].equals("")) {
            file2Parts[0] = File.separator;
        }

        int parts1Length = file1Parts.length;
        int parts2Length = file2Parts.length;

        int minLength = (parts1Length < parts2Length) ? parts1Length
                : parts2Length;

        String part1;
        String part2;
        StringBuffer commonsPart = new StringBuffer();
        for (int i = 0; i < minLength; i++) {
            part1 = file1Parts[i];
            part2 = file2Parts[i];
            if (!part1.equals(part2)) {
                break;
            }

            if (i > 0) {
                commonsPart.append(File.separatorChar);
            }

            commonsPart.append(part1);
        }

        if (commonsPart.length() == 0) {
            return null; // the two files have nothing in common (one on disk c:
            // and the other on d: for ex)
        }

        return new File(commonsPart.toString());
    }

    /**
     * Determines the base directory for a single file.
     * </p>
     * This is effectively a convenience method that calls {@link #getBaseDir(File[])}
     * with a single-element array.
     *
     * @param file the file for which to determine the base directory
     * @return the base directory, or {@code null} if none exists
     */
    public static File getBaseDir(File file) {
        return getBaseDir(new File[]{file});
    }

    /**
     * Determines the base directory common to a set of files.
     * </p>
     * The base directory is the shared directory between the set of files and the
     * current working directory.
     *
     * @param files an array of files
     * @return the common base directory, or {@code null} if there is none
     */
    public static File getBaseDir(File[] files) {
        File rootDir = getRootDir(files);

        // get the common part between current directory and other files
        File baseDir = getCommonPart(rootDir, new File("."));
        return baseDir;
    }

    /**
     * Computes the root directory common to all specified files.
     * </p>
     * The root directory is defined as the deepest shared ancestor directory
     * among all files in the array.
     *
     * @param files an array of files
     * @return the common root directory, or {@code null} if there is no common ancestor
     */
    public static File getRootDir(File[] files) {
        if ((files == null) || (files.length == 0)) {
            return null;
        }
        File[] canonicalFiles = new File[files.length];
        for (int i = 0; i < files.length; i++) {
            canonicalFiles[i] = files[i].getAbsoluteFile();
        }

        // first get the common part between all files
        File commonPart = canonicalFiles[0];
        for (int i = 0; i < files.length; i++) {
            commonPart = getCommonPart(commonPart, canonicalFiles[i]);
            if (commonPart == null) {
                return null;
            }
        }
        if (commonPart.isFile()) {
            return commonPart.getParentFile();
        } else {
            return commonPart;
        }
    }

    /**
     * Returns the path of a file relative to a specified root directory.
     *
     * @param rootDir the base directory to calculate the relative path from
     * @param file    the target file
     * @return the relative path from {@code rootDir} to {@code file}, or {@code null}
     *         if the file is not under the root
     * @throws SVNClientException if an I/O error occurs while resolving paths
     */
    public static String getRelativePath(File rootDir, File file)
            throws SVNClientException {
        try {
            String rootPath = rootDir.getCanonicalPath();
            String filePath = file.getCanonicalPath();
            if (!filePath.startsWith(rootPath)) {
                return null;
            }
            return filePath.substring(rootPath.length());
        } catch (IOException e) {
            throw SVNClientException.wrapException(e);
        }
    }
}
