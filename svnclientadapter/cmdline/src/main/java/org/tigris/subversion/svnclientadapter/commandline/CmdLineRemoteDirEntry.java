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

import static org.tigris.subversion.svnclientadapter.utils.XmlUtils.secureXmlFactory;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNRevision.Number;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * </p>
 * Implements a DirEntry on a remote location using the
 * "svn list" command.</p>
 *
 * @author Philip Schatz (schatz at tigris)
 */
class CmdLineRemoteDirEntry extends CmdLineXmlCommand implements ISVNDirEntry {

    private String path;
    private SVNRevision.Number revision;
    private SVNNodeKind nodeKind;
    private String lastCommitAuthor;
    private Date lastChangedDate;
    private long size;

    /**
     * Constructs a new instance of {@code CmdLineRemoteDirEntry} representing a remote directory entry.
     *
     * @param path     the relative path of the entry within the repository.
     * @param revision the revision in which this entry was last changed.
     * @param size     the size of the entry in bytes (only applicable for files).
     * @param author   the author of the last commit that affected this entry.
     * @param date     the date of the last change.
     * @param kind     the node kind, such as {@link SVNNodeKind#FILE} or {@link SVNNodeKind#DIR}.
     */
    protected CmdLineRemoteDirEntry(String path,
                                    Number revision,
                                    long size,
                                    String author,
                                    Date date,
                                    SVNNodeKind kind) {
        super();
        lastCommitAuthor = author;
        lastChangedDate = date;
        nodeKind = kind;
        this.path = path;
        this.revision = revision;
        this.size = size;
    }


    /**
     * Parses the output of {@code svn list --xml} and constructs an array of {@code CmdLineRemoteDirEntry} objects.
     * </p>
     * This method expects the XML data produced by the {@code svn list} command run with the {@code --xml} flag.
     * It extracts each {@code <entry>} element, including its name, size (for files), kind (file or dir),
     * and commit metadata such as author, revision, and date.
     *
     * @param cmdLineResults the raw XML output from {@code svn list --xml}, as a byte array.
     * @return an array of {@link CmdLineRemoteDirEntry} objects representing remote directory entries.
     * @throws SVNClientException if an error occurs while parsing the XML or required data is missing.
     */
    public static CmdLineRemoteDirEntry[] createDirEntries(byte[] cmdLineResults) throws SVNClientException {
        Collection logMessages = new ArrayList();

        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            secureXmlFactory(factory);
            factory.setValidating(false);

            // Create the builder and parse the file
            InputSource source = new InputSource(new ByteArrayInputStream(cmdLineResults));

            Document doc = factory.newDocumentBuilder().parse(source);

            NodeList nodes = doc.getElementsByTagName("entry");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node logEntry = nodes.item(i);

                String kindName = logEntry.getAttributes().getNamedItem("kind").getNodeValue();

                Element nameNode = getFirstNamedElement(logEntry, "name");
                if (nameNode == null) {
                    throw new Exception("'name' tag expected under 'entry'");
                }

                long size = 0;
                Element sizeNode = null;
                if ("file".equals(kindName)) {
                    sizeNode = getNextNamedElement(nameNode, "size");
                    if (sizeNode == null) {
                        throw new Exception("'size' tag expected under 'entry'");
                    }
                    size = Long.parseLong(sizeNode.getFirstChild().getNodeValue());
                } else {
                    sizeNode = nameNode;
                }

                Element commitNode = getNextNamedElement(sizeNode, "commit");
                if (commitNode == null) {
                    throw new Exception("'commit' tag expected under 'entry'");
                }

                Element authorNode = getFirstNamedElement(commitNode, "author");
                String author = null;
                if (authorNode != null) {
                    author = authorNode.getFirstChild().getNodeValue();
                }

                Element dateNode = getNextNamedElement(authorNode, "date");
                Date date = null;
                if (dateNode != null) {
                    date = Helper.convertXMLDate(dateNode.getFirstChild().getNodeValue());
                }

                SVNNodeKind kind = SVNNodeKind.UNKNOWN;
                if ("file".equals(kindName)) {
                    kind = SVNNodeKind.FILE;
                } else if ("dir".equals(kindName)) {
                    kind = SVNNodeKind.DIR;
                }

                Node revisionAttribute = commitNode.getAttributes().getNamedItem("revision");
                String name = nameNode.getFirstChild().getNodeValue();
                SVNRevision.Number rev = Helper.toRevNum(revisionAttribute.getNodeValue());

                CmdLineRemoteDirEntry entry = new CmdLineRemoteDirEntry(name, rev, size, author, date, kind);

                logMessages.add(entry);
            }
        } catch (Exception e) {
            throw new SVNClientException(e);
        }

        return (CmdLineRemoteDirEntry[]) logMessages.toArray(new CmdLineRemoteDirEntry[logMessages.size()]);

    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNDirEntry#getHasProps()
     */
    public boolean getHasProps() {
        //TODO unhardcode this
        return false;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNDirEntry#getNodeKind()
     */
    public SVNNodeKind getNodeKind() {
        return nodeKind;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNDirEntry#getLastChangedRevision()
     */
    public SVNRevision.Number getLastChangedRevision() {
        return revision;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNDirEntry#getLastChangedDate()
     */
    public Date getLastChangedDate() {
        return lastChangedDate;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNDirEntry#getLastCommitAuthor()
     */
    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNDirEntry#getPath()
     */
    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

}
