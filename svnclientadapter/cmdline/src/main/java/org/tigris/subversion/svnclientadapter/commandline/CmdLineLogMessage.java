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
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Implements a Log message using "svn log".
 *
 * @author Philip Schatz (schatz at tigris)
 */
class CmdLineLogMessage extends CmdLineXmlCommand implements ISVNLogMessage {

    private SVNRevision.Number rev;
    private String author;
    private Date date;
    private String msg;
    private ISVNLogMessageChangePath[] logMessageChangePaths;

    CmdLineLogMessage(
            SVNRevision.Number rev,
            String author,
            Date date,
            String msg,
            ISVNLogMessageChangePath[] logMessageChangePaths) {
        this.rev = rev;
        this.author = author;
        this.date = date;
        this.msg = msg;
        this.logMessageChangePaths = logMessageChangePaths;
    }

    /**
     * creates a log message from the output of svn log
     * This constructor is not used anymore.
     * The factory method createLogMessages is used instead
     */
    CmdLineLogMessage(StringTokenizer st) {
        //NOTE: the leading dashes are ommitted by ClientAdapter.

        //grab "rev 49:  phil | 2003-06-30 00:14:58 -0500 (Mon, 30 Jun 2003) | 1 line"
        String headerLine = st.nextToken();
        //split the line up into 3 parts, left, middle, and right.
        StringTokenizer ltr = new StringTokenizer(headerLine, "|");
        String left = ltr.nextToken();
        String middle = ltr.nextToken();

        //Now, we have the header, so set the internal variables

        //set info gotten from top-left.
        StringTokenizer leftToken = new StringTokenizer(left, ":");
        String revStr = leftToken.nextToken().trim(); //discard first bit.
        rev = Helper.toRevNum(revStr.substring(4, revStr.length()));

        // author is optional
        if (leftToken.hasMoreTokens()) {
            author = leftToken.nextToken();
        } else {
            author = "";
        }

        //set info from top-mid (date)
        date = Helper.toDate(middle.trim());

        String right = ltr.nextToken();

        //get the number of lines.
        StringTokenizer rightToken = new StringTokenizer(right, " ");
        int messageLineCount = Integer.parseInt(rightToken.nextToken());

        //get the body of the log.
        StringBuffer sb = new StringBuffer();
        //st.nextToken(); //next line is always blank.
        for (int i = 0; i < messageLineCount; i++) {
            sb.append(st.nextToken());

            //dont add a newline to the last line.
            if (i < messageLineCount - 1) {
                sb.append('\n');
            }
        }
        msg = sb.toString();

        //take off the last dashes "-----------------------------------------"
        st.nextToken();
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNLogMessage#getRevision()
     */
    public SVNRevision.Number getRevision() {
        return rev;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNLogMessage#getAuthor()
     */
    public String getAuthor() {
        return author;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNLogMessage#getDate()
     */
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.subclipse.client.ISVNLogMessage#getMessage()
     */
    public String getMessage() {
        return msg;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getMessage();
    }

    /**
     * Parses an XML output (as returned by {@code svn log --xml -v}) and constructs an array of
     * {@link CmdLineLogMessage} instances representing each log entry.
     * </p>
     * This method handles the standard XML format used by Subversion when generating verbose log entries.
     * It extracts key fields such as revision number, author, date, commit message, and path changes.
     *
     * @param cmdLineResults A byte array containing the XML output from the {@code svn log --xml -v} command.
     * @return An array of {@link CmdLineLogMessage} objects representing the parsed log entries.
     * @throws SVNClientException If the XML is malformed or cannot be parsed correctly.
     *         </p>
     *         <b>Example XML structure expected:</b>
     *         <pre>{@code
     * <log>
     *   <logentry revision="1234">
     *     <author>jdoe</author>
     *     <date>2024-01-01T12:00:00.000000Z</date>
     *     <paths>
     *       <path action="M" copyfrom-path="/trunk" copyfrom-rev="1233">/branches/feature</path>
     *     </paths>
     *     <msg>Commit message</msg>
     *   </logentry>
     * </log>
     * }</pre>
     */
    public static CmdLineLogMessage[] createLogMessages(byte[] cmdLineResults) throws SVNClientException {
        Collection logMessages = new ArrayList();

        try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            secureXmlFactory(factory);
            factory.setValidating(false);

            // Create the builder and parse the file
            InputSource source = new InputSource(new ByteArrayInputStream(cmdLineResults));

            Document doc = factory.newDocumentBuilder().parse(source);

            NodeList nodes = doc.getElementsByTagName("logentry");

            for (int i = 0; i < nodes.getLength(); i++) {
                Node logEntry = nodes.item(i);

                Element authorNode = getFirstNamedElement(logEntry, "author");

                Element dateNode;
                if (authorNode == null) {
                    dateNode = getFirstNamedElement(logEntry, "date");
                } else {
                    dateNode = getNextNamedElement(authorNode, "date");
                }
                if (dateNode == null) {
                    throw new Exception("'date' tag expected under 'logentry'");
                }

                Element pathsNode = getNextNamedElement(dateNode, "paths");
                Element msgNode = getNextNamedElement(pathsNode != null ? pathsNode : dateNode, "msg");
                Node revisionAttribute = logEntry.getAttributes().getNamedItem("revision");

                SVNRevision.Number rev = (revisionAttribute != null)
                        ? Helper.toRevNum(revisionAttribute.getNodeValue()) : null;
                String author = (authorNode != null) ? authorNode.getFirstChild().getNodeValue() : "";
                Date date = Helper.convertXMLDate(dateNode.getFirstChild().getNodeValue());
                Node msgTextNode = msgNode.getFirstChild();
                String msg = (msgTextNode != null) ? msgTextNode.getNodeValue() : "";

                List paths = new ArrayList();
                Element pathNode = getFirstNamedElement(pathsNode, "path");
                while (pathNode != null) {
                    String path = pathNode.getFirstChild().getNodeValue();

                    NamedNodeMap attributes = pathNode.getAttributes();
                    char action = attributes.getNamedItem("action").getNodeValue().charAt(0);
                    Node copyFromPathNode = attributes.getNamedItem("copyfrom-path");
                    Node copyFromRevNode = attributes.getNamedItem("copyfrom-rev");
                    String copyFromPath = null;
                    if (copyFromPathNode != null) {
                        copyFromPath = copyFromPathNode.getNodeValue();
                    }
                    SVNRevision.Number copyFromRev = null;
                    if (copyFromRevNode != null) {
                        copyFromRev = Helper.toRevNum(copyFromRevNode.getNodeValue());
                    }
                    paths.add(new SVNLogMessageChangePath(
                            path, copyFromRev, copyFromPath, action));

                    pathNode = getNextNamedElement(pathNode, "path");
                }
                ISVNLogMessageChangePath[] logMessageChangePath
                        = (ISVNLogMessageChangePath[]) paths.toArray(new ISVNLogMessageChangePath[paths.size()]);

                CmdLineLogMessage logMessage = new CmdLineLogMessage(rev, author, date, msg, logMessageChangePath);

                logMessages.add(logMessage);
            }
        } catch (Exception e) {
            throw new SVNClientException(e);
        }

        return (CmdLineLogMessage[]) logMessages.toArray(new CmdLineLogMessage[logMessages.size()]);

    }

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnclientadapter.ISVNLogMessage#getChangedPaths()
     */
    public ISVNLogMessageChangePath[] getChangedPaths() {
        return logMessageChangePaths;
    }

    public ISVNLogMessage[] getChildMessages() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getNumberOfChildren() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getTimeMicros() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getTimeMillis() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void addChild(ISVNLogMessage msg) {
        // TODO Auto-generated method stub

    }

    public boolean hasChildren() {
        // TODO Auto-generated method stub
        return false;
    }
}
