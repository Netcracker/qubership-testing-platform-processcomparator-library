/*******************************************************************************
 * Copyright (c) 2005, 2006 svnClientAdapter project and others.
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

package org.tigris.subversion.svnclientadapter.utils;

import java.net.MalformedURLException;

import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * Utility class for common operations on {@link SVNUrl} instances.
 * </p>
 * Provides methods to compute relative paths, determine shared URL roots,
 * and reconstruct SVN URLs from local working copy paths.
 * </p>
 */
public class SVNUrlUtils {

    /**
     * Returns the common root URL shared by two given SVN URLs.
     *
     * @param url1 the first SVN URL (must not be {@code null})
     * @param url2 the second SVN URL (must not be {@code null})
     * @return the common root {@link SVNUrl}, or {@code null}
     *         if the URLs do not share the same protocol, host, and port
     */
    public static SVNUrl getCommonRootUrl(SVNUrl url1, SVNUrl url2) {
        if ((!url1.getProtocol().equals(url2.getProtocol()))
                || (!url1.getHost().equals(url2.getHost()))
                || (url1.getPort() != url2.getPort())) {
            return null;
        }
        String url = url1.getProtocol() + "://" + url1.getHost() + ":" + url1.getPort();
        String[] segs1 = url1.getPathSegments();
        String[] segs2 = url2.getPathSegments();
        int minLength = Math.min(segs1.length, segs2.length);
        for (int i = 0; i < minLength; i++) {
            if (!segs1[i].equals(segs2[i])) {
                break;
            }
            url += "/" + segs1[i];
        }
        try {
            return new SVNUrl(url);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Returns the common root URL shared by a set of SVN URLs.
     *
     * @param urls array of SVN URLs (must contain at least one entry)
     * @return the common root {@link SVNUrl}, or {@code null} if no common root exists
     */
    public static SVNUrl getCommonRootUrl(SVNUrl[] urls) {
        SVNUrl commonRoot = urls[0];
        for (int i = 0; i < urls.length; i++) {
            commonRoot = getCommonRootUrl(commonRoot, urls[i]);
            if (commonRoot == null) {
                return null;
            }
        }
        return commonRoot;
    }

    /**
     * Returns the path of a URL relative to a given root URL.
     *
     * @param rootUrl the root URL
     * @param url     the target URL
     * @return the relative path string, or {@code null} if {@code rootUrl} is not a parent of {@code url}
     */
    public static String getRelativePath(SVNUrl rootUrl, SVNUrl url) {
        return getRelativePath(rootUrl, url, false);
    }

    /**
     * Returns the path of a URL relative to a given root URL.
     *
     * @param rootUrl              the root URL
     * @param url                  the target URL
     * @param includeStartingSlash whether to include a leading "/" in the result
     * @return the relative path string, or {@code null} if {@code rootUrl} is not a parent of {@code url}
     */
    public static String getRelativePath(SVNUrl rootUrl, SVNUrl url, boolean includeStartingSlash) {
        // TODO: Consider more efficient logic than converting to strings
        String rootUrlStr = rootUrl.toString();
        String urlStr = url.toString();
        if (urlStr.indexOf(rootUrlStr) == -1) {
            return null;
        }
        if (urlStr.length() == rootUrlStr.length()) {
            return "";
        }
        return urlStr.substring(rootUrlStr.length() + (includeStartingSlash ? 0 : 1));
    }

    /**
     * Constructs the {@link SVNUrl} for a given file in the working copy based on a known parent.
     *
     * @param localFileName  full path to the local file in the working copy
     * @param parentUrl      the SVN URL of a known parent resource
     * @param parentPathName the local path to the parent resource
     * @return the reconstructed {@link SVNUrl}, or {@code null} if the relationship is invalid or URL is malformed
     */
    public static SVNUrl getUrlFromLocalFileName(String localFileName, SVNUrl parentUrl, String parentPathName) {
        return getUrlFromLocalFileName(localFileName, parentUrl.toString(), parentPathName);
    }

    /**
     * Constructs the {@link SVNUrl} for a given file in the working copy based on a known parent.
     *
     * @param localFileName  full path to the local file in the working copy
     * @param parentUrl      string representation of the parent SVN URL
     * @param parentPathName local path to the parent resource
     * @return the reconstructed {@link SVNUrl}, or {@code null} if the relationship is invalid or URL is malformed
     */
    public static SVNUrl getUrlFromLocalFileName(String localFileName, String parentUrl, String parentPathName) {
        String parentPath = parentPathName.contains("\\") ? parentPathName.replaceAll("\\\\", "/") : parentPathName;
        String localFile = localFileName.contains("\\") ? localFileName.replaceAll("\\\\", "/") : localFileName;
        try {
            if (!localFile.startsWith(parentPath)) {
                return null;
            }
            if (localFile.length() == parentPath.length()) {
                return new SVNUrl(parentUrl);
            }
            char lastChar = parentPath.charAt(parentPath.length() - 1);
            String relativeFileName = localFile.substring(parentPath.length() + (((lastChar != '\\')
                    && (lastChar != '/')) ? 1 : 0));

            if (parentUrl.endsWith("/")) {
                return new SVNUrl(parentUrl + relativeFileName);
            } else {
                return new SVNUrl(parentUrl + "/" + relativeFileName);
            }
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
