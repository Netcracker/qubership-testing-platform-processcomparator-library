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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.core.enums.VCSFileType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSContext;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSFile;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSProvider;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSReadRequest;
import org.qubership.automation.pc.reader.impl.vcs.VCSContext;
import org.qubership.automation.pc.reader.impl.vcs.VCSFile;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.commandline.CmdLineClientAdapterFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of {@link IVCSProvider} for interacting with Subversion (SVN) repositories.
 *
 * <p>
 * This provider uses the command-line SVN client adapter to access remote repositories,
 * fetch file listings, and retrieve file contents for further processing.
 * </p>
 *
 * <p>
 * The class supports authentication via username and password, revision targeting,
 * and file extension filtering using regular expressions.
 * </p>
 *
 * <p>
 * It produces a {@link VCSContext} containing metadata and content of matching files,
 * encapsulated as {@link VCSFile} instances.
 * </p>
 *
 * <p>
 * The implementation is based on the {@code svnClientAdapter} library, particularly its command-line backend.
 * </p>
 *
 * <p><b>Note:</b> Recursive reading and extension filtering are supported via configurable properties.</p>
 *
 * @see IVCSProvider
 * @see VCSContext
 * @see VCSFile
 * @see SVNClientAdapterFactory
 */
@Slf4j
public final class SVNProvider implements IVCSProvider {

    private ISVNClientAdapter svnClient;
    private String userName;
    private String password;
    private Properties parameters;

    private Boolean recursively = false;
    private Pattern extensionFilterPattern;

    @Override
    public void init(Properties providerParameters, String userName, String password) throws ReaderException {
        this.init(providerParameters);
        this.setUser(userName);
        this.setPassword(password);
    }

    @Override
    public void init(Properties providerParameters) throws ReaderException {
        this.parameters = providerParameters;
        buildExtensionFilterPattern();
        try {
            if (!SVNClientAdapterFactory.isSvnClientAvailable("commandline")) {
                CmdLineClientAdapterFactory.setup();
            }
            //create svn client by using commandline wrapper        
            svnClient = SVNClientAdapterFactory.createSvnClient("commandline");
        } catch (SVNClientException ex) {
            throw new ReaderException(ex);
        }
    }

    @Override
    public void setUser(String userName) {
        if (StringUtils.isBlank(userName)) {
            return;
        }
        this.userName = userName;
        if (svnClient != null) {            
            svnClient.setUsername(userName);
        }
    }

    @Override
    public void setPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return;
        }
        this.password = password;
        if (svnClient != null) {
            svnClient.setPassword(password);
        }
    }

    @Override
    public IVCSContext readRemote(IVCSReadRequest request) throws ReaderException {
        try {
            SVNUrl startUrl = getUrl(request.getTargetUrl());
            SVNRevision revision = getRevision(request.getRevision());//read remote files list
            ISVNDirEntry[] filesList = svnClient.getList(startUrl, revision, this.recursively);
            IVCSContext resultContext = new VCSContext();
            for (ISVNDirEntry fileEntry : filesList) {
                if (fileEntry.getNodeKind() == SVNNodeKind.FILE && allowedType(fileEntry.getPath())) {
                    SVNUrl fileUrl;
                    if (!startUrl.toString().endsWith(fileEntry.getPath())) {
                        fileUrl = startUrl.appendPath(fileEntry.getPath());
                    } else {
                        fileUrl = startUrl;
                    }
                    InputStream is = svnClient.getContent(fileUrl, revision);
                    IVCSFile newFile = new VCSFile(fileEntry.getPath(),
                            revision.toString(),
                            fileEntry.getLastCommitAuthor(),
                            fileEntry.getLastChangedDate(),
                            is,
                            VCSFileType.FILE);
                    resultContext.getFiles().add(newFile);
                }
            }
            resultContext.setUrl(request.getTargetUrl());
            resultContext.setRevision(request.getRevision());
            return resultContext;
        } catch (SVNClientException | MalformedURLException ex) {
            throw new ReaderException(ex);
        }
    }

    private void buildExtensionFilterPattern() {
        if (parameters != null && parameters.containsKey(PROP_EXTENSION_FILTER)) {
            String propValue = parameters.getProperty(PROP_EXTENSION_FILTER, ".*");
            String pattern = "^.*\\.(" + propValue + ")$";
            extensionFilterPattern = Pattern.compile(pattern);
        }
    }

    private boolean allowedType(String path) {
        Matcher m = extensionFilterPattern.matcher(path);
        return m.matches();
    }

    private SVNUrl getUrl(String url) throws MalformedURLException {
        return new SVNUrl(url);
    }

    private SVNRevision getRevision(String revision) {
        try {
            return SVNRevision.getRevision(revision);
        } catch (ParseException ex) {
            log.warn(String.format("Failed to parse revision '%s', defaulting to HEAD", revision), ex);
        }

        // ok. return head revision as default
        return SVNRevision.HEAD;
    }

    @Override
    public void close() throws Exception {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
