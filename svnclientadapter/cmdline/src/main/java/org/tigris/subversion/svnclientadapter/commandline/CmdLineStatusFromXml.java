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
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CmdLineStatusFromXml extends CmdLineXmlCommand {

    private SVNRevision.Number lastChangedRevision;
    private Date lastChangedDate;
    private String lastCommitAuthor;
    private SVNStatusKind textStatus;
    private SVNStatusKind repositoryTextStatus;
    private SVNStatusKind propStatus;
    private SVNStatusKind repositoryPropStatus;
    private SVNRevision.Number revision;
    private String path;
    private boolean copied;
    private boolean wcLocked;
    private boolean switched;
    private File conflictNew;
    private File conflictOld;
    private File conflictWorking;
    private String lockOwner;
    private Date lockCreationDate;
    private String lockComment;

    protected CmdLineStatusFromXml(String path) {
        super();
        this.path = path;
    }

    /**
     * Get the conflictNew.
     *
     * @return Returns the conflictNew.
     */
    public File getConflictNew() {
        return conflictNew;
    }

    /**
     * Get the conflictOld.
     *
     * @return Returns the conflictOld.
     */
    public File getConflictOld() {
        return conflictOld;
    }

    /**
     * Get the conflictWorking.
     *
     * @return Returns the conflictWorking.
     */
    public File getConflictWorking() {
        return conflictWorking;
    }

    /**
     * Get the copied.
     *
     * @return Returns the copied.
     */
    public boolean isCopied() {
        return copied;
    }

    /**
     * Get the wcLocked.
     *
     * @return Returns the wcLocked.
     */
    public boolean isWcLocked() {
        return wcLocked;
    }

    /**
     * Get the switched.
     *
     * @return Returns the switched.
     */
    public boolean isSwitched() {
        return switched;
    }

    /**
     * Get the file.
     *
     * @return Returns the file.
     */
    public File getFile() {
        return new File(getPath()).getAbsoluteFile();
    }

    /**
     * Get the lastCommitAuthor.
     *
     * @return Returns the lastCommitAuthor.
     */
    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }

    /**
     * Get the lastChangedDate.
     *
     * @return Returns the lastChangedDate.
     */
    public Date getLastChangedDate() {
        return lastChangedDate;
    }

    /**
     * Get the lastChangedRevision.
     *
     * @return Returns the lastChangedRevision.
     */
    public SVNRevision.Number getLastChangedRevision() {
        return lastChangedRevision;
    }

    /**
     * Get the lockComment.
     *
     * @return Returns the lockComment.
     */
    public String getLockComment() {
        return lockComment;
    }

    /**
     * Get the lockCreationDate.
     *
     * @return Returns the lockCreationDate.
     */
    public Date getLockCreationDate() {
        return lockCreationDate;
    }

    /**
     * Get the lockOwner.
     *
     * @return Returns the lockOwner.
     */
    public String getLockOwner() {
        return lockOwner;
    }

    /**
     * Get the path.
     *
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the propStatus.
     *
     * @return Returns the propStatus.
     */
    public SVNStatusKind getPropStatus() {
        return propStatus;
    }

    /**
     * Get the repositoryPropStatus.
     *
     * @return Returns the repositoryPropStatus.
     */
    public SVNStatusKind getRepositoryPropStatus() {
        return repositoryPropStatus;
    }

    /**
     * Get the repositoryTextStatus.
     *
     * @return Returns the repositoryTextStatus.
     */
    public SVNStatusKind getRepositoryTextStatus() {
        return repositoryTextStatus;
    }

    /**
     * Get the revision.
     *
     * @return Returns the revision.
     */
    public SVNRevision.Number getRevision() {
        return revision;
    }

    /**
     * Get the textStatus.
     *
     * @return Returns the textStatus.
     */
    public SVNStatusKind getTextStatus() {
        return textStatus;
    }

    /**
     * Set the conflictNew.
     *
     * @param conflictNew The conflictNew to set.
     */
    protected void setConflictNew(File conflictNew) {
        this.conflictNew = conflictNew;
    }

    /**
     * Set the conflictOld.
     *
     * @param conflictOld The conflictOld to set.
     */
    protected void setConflictOld(File conflictOld) {
        this.conflictOld = conflictOld;
    }

    /**
     * Set the conflictWorking.
     *
     * @param conflictWorking The conflictWorking to set.
     */
    protected void setConflictWorking(File conflictWorking) {
        this.conflictWorking = conflictWorking;
    }

    /**
     * Set the copied.
     *
     * @param copied The copied to set.
     */
    protected void setCopied(boolean copied) {
        this.copied = copied;
    }

    /**
     * Set the wcLocked.
     *
     * @param wcLocked The wcLocked to set.
     */
    protected void setWcLocked(boolean wcLocked) {
        this.wcLocked = wcLocked;
    }

    /**
     * Set the switched.
     *
     * @param switched The switched to set.
     */
    protected void setSwitched(boolean switched) {
        this.switched = switched;
    }

    /**
     * Set the lastCommitAuthor.
     *
     * @param lastCommitAuthor The lastCommitAuthor to set.
     */
    protected void setLastCommitAuthor(String lastCommitAuthor) {
        this.lastCommitAuthor = lastCommitAuthor;
    }

    /**
     * Set the lastChangedDate.
     *
     * @param lastChangedDate The lastChangedDate to set.
     */
    protected void setLastChangedDate(Date lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    /**
     * Set the lastChangedRevision.
     *
     * @param lastChangedRevision The lastChangedRevision to set.
     */
    protected void setLastChangedRevision(SVNRevision.Number lastChangedRevision) {
        this.lastChangedRevision = lastChangedRevision;
    }

    /**
     * Set the lockComment.
     *
     * @param lockComment The lockComment to set.
     */
    protected void setLockComment(String lockComment) {
        this.lockComment = lockComment;
    }

    /**
     * Set the lockCreationDate.
     *
     * @param lockCreationDate The lockCreationDate to set.
     */
    protected void setLockCreationDate(Date lockCreationDate) {
        this.lockCreationDate = lockCreationDate;
    }

    /**
     * Set the lockOwner.
     *
     * @param lockOwner The lockOwner to set.
     */
    protected void setLockOwner(String lockOwner) {
        this.lockOwner = lockOwner;
    }

    /**
     * Set the path.
     *
     * @param path The path to set.
     */
    protected void setPath(String path) {
        this.path = path;
    }

    /**
     * Set the propStatus.
     *
     * @param propStatus The propStatus to set.
     */
    protected void setPropStatus(SVNStatusKind propStatus) {
        this.propStatus = propStatus;
    }

    /**
     * Set the repositoryPropStatus.
     *
     * @param repositoryPropStatus The repositoryPropStatus to set.
     */
    protected void setRepositoryPropStatus(SVNStatusKind repositoryPropStatus) {
        this.repositoryPropStatus = repositoryPropStatus;
    }

    /**
     * Set the repositoryTextStatus.
     *
     * @param repositoryTextStatus The repositoryTextStatus to set.
     */
    protected void setRepositoryTextStatus(SVNStatusKind repositoryTextStatus) {
        this.repositoryTextStatus = repositoryTextStatus;
    }

    /**
     * Set the revision.
     *
     * @param revision The revision to set.
     */
    protected void setRevision(SVNRevision.Number revision) {
        this.revision = revision;
    }

    /**
     * Set the textStatus.
     *
     * @param textStatus The textStatus to set.
     */
    protected void setTextStatus(SVNStatusKind textStatus) {
        this.textStatus = textStatus;
    }

    /**
     * Parses the given XML output from `svn status --xml` and creates an array of {@link CmdLineStatusFromXml}
     * instances.
     * </p>
     * The method extracts information such as text status, property status, last commit info, lock details,
     * repository status, etc. It supports full status parsing including nested tags such as `commit` and `lock`.
     *
     * @param cmdLineResults The raw XML output from the `svn status` command.
     * @return an array of {@link CmdLineStatusFromXml} objects representing the parsed status entries.
     * @throws CmdLineException if the XML is malformed or required fields are missing.
     */
    public static CmdLineStatusFromXml[] createStatuses(byte[] cmdLineResults) throws CmdLineException {
        Collection statuses = new ArrayList();

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
                Node statusEntry = nodes.item(i);

                String entryPath = statusEntry.getAttributes().getNamedItem("path").getNodeValue();

                CmdLineStatusFromXml status = new CmdLineStatusFromXml(entryPath);

                Element wcStatusNode = getFirstNamedElement(statusEntry, "wc-status");
                if (wcStatusNode == null) {
                    throw new Exception("'wc-status' tag expected under 'entry'");
                }

                Node wcItemStatusAttr = wcStatusNode.getAttributes().getNamedItem("item");
                status.setTextStatus(SVNStatusKind.fromString(wcItemStatusAttr.getNodeValue()));
                Node wcPpropStatusAttr = wcStatusNode.getAttributes().getNamedItem("props");
                status.setPropStatus(SVNStatusKind.fromString(wcPpropStatusAttr.getNodeValue()));
                Node wcRevisionAttribute = wcStatusNode.getAttributes().getNamedItem("revision");
                if (wcRevisionAttribute != null) {
                    status.setRevision(Helper.toRevNum(wcRevisionAttribute.getNodeValue()));
                }
                Node wcLockedAttr = wcStatusNode.getAttributes().getNamedItem("wc-locked");
                status.setWcLocked((wcLockedAttr != null) && "true".equals(wcLockedAttr.getNodeValue()));
                Node copiedAttr = wcStatusNode.getAttributes().getNamedItem("copied");
                status.setCopied((copiedAttr != null) && "true".equals(copiedAttr.getNodeValue()));
                Node switchedAttr = wcStatusNode.getAttributes().getNamedItem("switched");
                status.setSwitched((switchedAttr != null) && "true".equals(switchedAttr.getNodeValue()));

                Element commitNode = getFirstNamedElement(wcStatusNode, "commit");
                if (commitNode != null) {
                    Node commitRevisionAttribute = commitNode.getAttributes().getNamedItem("revision");
                    status.setLastChangedRevision(Helper.toRevNum(commitRevisionAttribute.getNodeValue()));
                    Element authorNode = getFirstNamedElement(commitNode, "author");
                    if (authorNode != null) {
                        status.setLastCommitAuthor(authorNode.getFirstChild().getNodeValue());
                    }
                    Element dateNode = getNextNamedElement(authorNode, "date");
                    if (dateNode != null) {
                        status.setLastChangedDate(Helper.convertXMLDate(dateNode.getFirstChild().getNodeValue()));
                    }
                }

                Element lockNode = getNextNamedElement(commitNode, "lock");
                if (lockNode != null) {
                    Element tokenNode = getFirstNamedElement(lockNode, "token");
                    if (tokenNode == null) {
                        throw new Exception("'token' tag expected under 'lock'");
                    }
                    Element ownerNode = getNextNamedElement(lockNode, "owner");
                    if (ownerNode == null) {
                        throw new Exception("'owner' tag expected under 'lock'");
                    }
                    status.setLockOwner(ownerNode.getFirstChild().getNodeValue());
                    Element lockCommentNode = getNextNamedElement(ownerNode, "comment");
                    status.setLockComment((lockCommentNode != null)
                            ? lockCommentNode.getFirstChild().getNodeValue() : null);
                    Element lockCreatedNode = getNextNamedElement(lockCommentNode, "created");
                    status.setLockCreationDate(Helper.convertXMLDate((lockCreatedNode != null)
                            ? lockCreatedNode.getFirstChild().getNodeValue() : null));
                }

                Element reposStatusNode = getNextNamedElement(wcStatusNode, "repos-status");
                if (reposStatusNode != null) {
                    Node reposItemStatusAttr = reposStatusNode.getAttributes().getNamedItem("item");
                    status.setRepositoryTextStatus(SVNStatusKind.fromString(reposItemStatusAttr.getNodeValue()));
                    Node reposPropStatusAttr = reposStatusNode.getAttributes().getNamedItem("props");
                    status.setRepositoryPropStatus(SVNStatusKind.fromString(reposPropStatusAttr.getNodeValue()));
                }

                statuses.add(status);
            }
        } catch (Exception e) {
            throw new CmdLineException(e);
        }

        return (CmdLineStatusFromXml[]) statuses.toArray(new CmdLineStatusFromXml[statuses.size()]);

    }


}
