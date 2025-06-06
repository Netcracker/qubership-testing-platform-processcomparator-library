/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.automation.pc.reader.impl.vcs.providers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.qubership.automation.pc.core.enums.VCSFileType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSContext;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSFile;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSProvider;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSReadRequest;
import org.qubership.automation.pc.reader.impl.vcs.VCSContext;
import org.qubership.automation.pc.reader.impl.vcs.VCSFile;

public final class GITProvider implements IVCSProvider {

    private String userName;
    private String password;
    private Properties parameters;
    private CredentialsProvider credentialsProvider;
    private Pattern extensionFilterPattern;

    @Override
    public void init(Properties providerParameters) {
        this.parameters = providerParameters;
        buildExtensionFilterPattern();
    }

    @Override
    public void init(Properties providerParameters, String userName, String password) {
        this.init(providerParameters);
        this.setUser(userName);
        this.setPassword(password);
    }

    @Override
    public void setUser(String userName) {
        if (userName != null && !userName.trim().isEmpty()) {
            this.userName = userName;
            updateCredentialsProvider();
        }
    }

    @Override
    public void setPassword(String password) {
        if (password != null && !password.trim().isEmpty()) {
            this.password = password;
            updateCredentialsProvider();
        }
    }

    private void updateCredentialsProvider() {
        if (userName != null && password != null) {
            credentialsProvider = new UsernamePasswordCredentialsProvider(userName, password);
        }
    }

    @Override
    public IVCSContext readRemote(IVCSReadRequest request) throws ReaderException {
        File tempDir = null;
        try {
            String fullUrl = request.getTargetUrl();
            String remoteUrl = parseRepositoryUrl(fullUrl);
            String revision = parameters.containsKey(IVCSProvider.PROP_REVISION)
                    ? parameters.getProperty(IVCSProvider.PROP_REVISION) : extractBranch(fullUrl);

            if (remoteUrl == null || revision == null) {
                throw new ReaderException("Missing required properties: remoteUrl or revision.");
            }

            tempDir = new File(System.getProperty("java.io.tmpdir"), "temp-git-repo");
            boolean isTree = isTreeUrl(fullUrl) || isRootRepoUrl(fullUrl);
            boolean isBlob = isBlobUrl(fullUrl);

            String targetPath = isTree
                    ? extractTreePath(fullUrl)
                    : isBlob ? extractFilePath(fullUrl)
                    : "/";

            if (targetPath.isEmpty()) {
                targetPath = "/";
            }

            boolean isRecursive = isBlob || (isTree && !targetPath.equals("/"));

            Map<String, byte[]> files = getFilesFromGit(remoteUrl, targetPath, revision, isTree, isRecursive, tempDir);

            IVCSContext resultContext = new VCSContext();
            for (Map.Entry<String, byte[]> entry : files.entrySet()) {
                IVCSFile vcsFile = new VCSFile(
                        entry.getKey(), revision, userName, null,
                        new ByteArrayInputStream(entry.getValue()), VCSFileType.FILE);
                resultContext.getFiles().add(vcsFile);
            }
            resultContext.setUrl(targetPath);
            resultContext.setRevision(revision);
            return resultContext;
        } catch (Exception ex) {
            throw new ReaderException(ex);
        } finally {
            if (tempDir != null && tempDir.exists()) {
                deleteDirectory(tempDir);
            }
        }
    }

    private Map<String, byte[]> getFilesFromGit(String remoteUrl,
                                                String path,
                                                String revision,
                                                boolean isTree,
                                                boolean isRecursive,
                                                File tempDir) throws IOException, GitAPIException {
        Map<String, byte[]> fileMap = new HashMap<>();

        try (Git git = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(tempDir)
                .setBranch(revision)
                .setBare(true)
                .setNoCheckout(true)
                .setCredentialsProvider(credentialsProvider)
                .call()) {

            try (RevWalk revWalk = new RevWalk(git.getRepository())) {
                RevCommit commit = revWalk.parseCommit(git.getRepository().resolve(revision));
                RevTree tree = commit.getTree();

                try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                    treeWalk.addTree(tree);
                    treeWalk.setRecursive(isRecursive);
                    while (treeWalk.next()) {
                        String currentPath = treeWalk.getPathString();

                        if (isTree) {
                            if (path.equals("/") || (isRecursive && isDirectChild(path, currentPath))) {
                                if (treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB
                                        && allowedType(currentPath)) {
                                    ObjectLoader loader = git.getRepository().open(treeWalk.getObjectId(0));
                                    fileMap.put(currentPath, loader.getBytes());
                                }
                            }
                        } else {
                            if (treeWalk.getFileMode(0).getObjectType() == Constants.OBJ_BLOB
                                    && currentPath.equals(path)
                                    && allowedType(currentPath)) {
                                ObjectLoader loader = git.getRepository().open(treeWalk.getObjectId(0));
                                fileMap.put(currentPath, loader.getBytes());
                                break;
                            }
                        }
                    }
                }
            }
        }
        return fileMap;
    }

    private boolean isDirectChild(String folderPath, String fullPath) {
        if (!fullPath.startsWith(folderPath)) {
            return false;
        }
        String remainder = fullPath.substring(folderPath.length());
        if (remainder.startsWith("/")) {
            remainder = remainder.substring(1);
        }
        return !remainder.isEmpty() && !remainder.contains("/");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    String extractFilePath(String fullUrl) {
        return extractPathAfter(fullUrl, "blob");
    }

    String extractTreePath(String fullUrl) {
        if (isRootRepoUrl(fullUrl)) {
            return "/";
        }
        return extractPathAfter(fullUrl, "tree");
    }

    String extractPathAfter(String url, String keyword) {
        if (!url.contains("/" + keyword + "/")) {
            return "";
        }

        String[] parts = url.split("/");
        StringBuilder path = new StringBuilder();
        boolean start = false;
        boolean skipNext = false;

        for (String part : parts) {
            if (part.equals(keyword)) {
                start = true;
                skipNext = true;
                continue;
            }
            if (start) {
                if (skipNext) {
                    skipNext = false;
                    continue;
                }
                path.append(part).append("/");
            }
        }
        return path.toString().replaceAll("/$", "");
    }

    String extractBranch(String fullUrl) {
        String[] parts = fullUrl.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ((parts[i].equals("blob") || parts[i].equals("tree")) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return parameters.getProperty(PROP_REVISION, "main");
    }

    String parseRepositoryUrl(String fullUrl) {
        if (fullUrl.endsWith(".git")) {
            return fullUrl;
        }

        int treeIndex = fullUrl.indexOf("/-/tree/");
        if (treeIndex == -1) {
            treeIndex = fullUrl.indexOf("/tree/");
        }
        int blobIndex = fullUrl.indexOf("/-/blob/");
        if (blobIndex == -1) {
            blobIndex = fullUrl.indexOf("/blob/");
        }

        int cutoffIndex = -1;
        if (treeIndex != -1) {
            cutoffIndex = treeIndex;
        } else if (blobIndex != -1) {
            cutoffIndex = blobIndex;
        }

        if (cutoffIndex != -1) {
            return fullUrl.substring(0, cutoffIndex) + ".git";
        }

        return fullUrl.endsWith(".git") ? fullUrl : fullUrl + ".git";
    }

    boolean isTreeUrl(String url) {
        return url.contains("/-/tree/") || url.contains("/tree/") || url.endsWith(".git");
    }

    boolean isBlobUrl(String url) {
        return url.contains("/-/blob/") || url.contains("/blob/");
    }

    boolean isRootRepoUrl(String url) {
        return !url.contains("/blob/") && !url.contains("/tree/") && !url.contains("/-/") && !url.endsWith(".git");
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        directory.delete();
    }

    private void buildExtensionFilterPattern() {
        if (parameters != null && parameters.containsKey(PROP_EXTENSION_FILTER)) {
            String propValue = parameters.getProperty(PROP_EXTENSION_FILTER, ".*");
            String pattern = "^.*\\.(" + propValue + ")$";
            extensionFilterPattern = Pattern.compile(pattern);
        }
    }

    private boolean allowedType(String path) {
        if (extensionFilterPattern == null) {
            return true;
        }
        Matcher m = extensionFilterPattern.matcher(path);
        return m.matches();
    }
}
