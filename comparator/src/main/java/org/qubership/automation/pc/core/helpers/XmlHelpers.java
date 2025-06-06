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

package org.qubership.automation.pc.core.helpers;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Utility class for working with XML data structures and content transformations.
 * </p>
 * Provides methods for cleaning, parsing, and transforming XML strings;
 * extracting XPath expressions; and building in-memory XSL transformers.
 * Designed for use in comparison tools, highlighters, and XML-aware processing logic.
 */
public class XmlHelpers {

    /**
     * Remove comments and linebreaks from XML.
     *
     * @param inputString Input XML String
     * @return String cleaned XML String
     */
    // STARTTAG    = "\\s+<\\s+\\?xml"
    private static final String STARTTAG = "[\\S\\s]+<\\s*\\?xml";
    private static final String STARTTAG_REPLACE = "<\\?xml";
    private static final String LINEBREAKS = "\\r?\\n|\\r|\\n";
    private static final String LINEBREAKS_REPLACE = " ";
    private static final String COMMENTS = "<!--[\\s\\S]*?-->";
    private static final String COMMENTS_REPLACE = "";
    private static final String WS = ">\\s+<";
    private static final String WS_REPLACE = "><"; // Testing variant: ">[\\t\\x20]+<";

    private static final Pattern getXmlPrefixPattern = Pattern.compile("([\\S\\s]+)<\\s*\\?xml");

    // maybe some when we will set it to true/false globally or by rules?
    private static boolean include_Id_and_Name_Attrs_toFullXPath = false;

    // Suddenly - 22/02/2017 - 'replaceFirst' became VERY slow
    // (sometimes about 1.5 minutes: each execution if breakpoints are set and some executions without breakpoints ).
    // Old methods using STARTTAG and/or replaceFirst are renamed to '...ByRegex'
    //  They are will be removed later
    public static String getXmlPrefix(String inputString) {
        int i = inputString.indexOf("<?xml");
        if (i > 0) {
            return inputString.substring(0, i);
        } else {
            return "";
        }
    }

    public static String getXmlPrefixByRegex(String inputString) {
        Matcher matcher = getXmlPrefixPattern.matcher(inputString);
        if (matcher.find()) {
            //String fullMatch = matcher.group(0); // commented; currently is not used
            String xmlPrefix = matcher.group(1);
            return xmlPrefix.trim();
        } else {
            return "";
        }
    }

    public static String cleanXml(String inputString) {
        String resultString;
        int i = inputString.indexOf("<?xml");
        if (i > 0) {
            resultString = inputString.substring(i).replaceAll(COMMENTS, COMMENTS_REPLACE);
        } else {
            resultString = inputString.replaceAll(COMMENTS, COMMENTS_REPLACE);
        }
        resultString = resultString.replaceAll(LINEBREAKS, LINEBREAKS_REPLACE);
        resultString = resultString.replaceAll(WS, WS_REPLACE);
        return resultString;
    }

    public static String cleanXmlByRegex(String inputString) {
        String resultString = inputString.replaceFirst(STARTTAG, STARTTAG_REPLACE);
        resultString = resultString.replaceAll(COMMENTS, COMMENTS_REPLACE);
        resultString = resultString.replaceAll(LINEBREAKS, LINEBREAKS_REPLACE);
        resultString = resultString.replaceAll(WS, WS_REPLACE);
        return resultString;
    }

    public static String cleanCommentsAndWs(String inputString) {
        String resultString;
        int i = inputString.indexOf("<?xml");
        if (i > 0) {
            resultString = inputString.substring(i).replaceAll(COMMENTS, COMMENTS_REPLACE);
        } else {
            resultString = inputString.replaceAll(COMMENTS, COMMENTS_REPLACE);
        }
        resultString = resultString.replaceAll(WS, WS_REPLACE);
        return resultString;
    }

    public static String cleanCommentsAndWsByRegex(String inputString) {
        String resultString = inputString.replaceFirst(STARTTAG, STARTTAG_REPLACE);
        resultString = resultString.replaceAll(COMMENTS, COMMENTS_REPLACE);
        resultString = resultString.replaceAll(WS, WS_REPLACE);
        return resultString;
    }

    public static String cleanCommentsAndStartXML(String inputString) {
        String resultString;
        int i = inputString.indexOf("<?xml");
        if (i > 0) {
            resultString = inputString.substring(i).replaceAll(COMMENTS, COMMENTS_REPLACE);
        } else {
            resultString = inputString.replaceAll(COMMENTS, COMMENTS_REPLACE);
        }
        return resultString;
    }

    public static String cleanCommentsAndStartXMLByRegex(String inputString) {
        String resultString = inputString.replaceFirst(STARTTAG, STARTTAG_REPLACE);
        resultString = resultString.replaceAll(COMMENTS, COMMENTS_REPLACE);
        return resultString;
    }

    public static String cleanLineBreaks(String inputString) {
        String resultString = inputString.replaceAll(LINEBREAKS, LINEBREAKS_REPLACE);
        resultString = resultString.replaceAll(WS, WS_REPLACE);
        return resultString;
    }

    public static Transformer createTransformer(List<String> xpaths, List<String> sortBy) throws ComparatorException {
        StringBuilder xsl = new StringBuilder(900);
        //xsl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xsl.append("<xsl:stylesheet version=\"2.0\" \n");
        xsl.append("     xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n");
        xsl.append("  <xsl:output method=\"xml\" indent=\"yes\" />\n");
        xsl.append("  <xsl:strip-space elements=\"*\"/>\n\n");

        /* 'excludeXpath'-rules are applied 1st! */
        xsl.append("  <!-- this empty template will remove them -->\n");
        for (String str : xpaths) {
            xsl.append(String.format("  <xsl:template match=\"%s\"/>\n", str));
        }

        for (String str : sortBy) {
            xsl.append(String.format("  %s\n", str));
        }

        if (!xpaths.isEmpty() && sortBy.isEmpty()) {
            xsl.append("  <xsl:template match=\"node() | @*\">\n");
            xsl.append("    <xsl:copy>\n");
            xsl.append("      <xsl:apply-templates select=\"node() | @*\"/>\n");
            xsl.append("    </xsl:copy>\n");
            xsl.append("  </xsl:template>\n\n");
        }

        xsl.append("</xsl:stylesheet>");
        try {
            return createTransformer(xsl.toString());
        } catch (ComparatorException e) {
            throw new ComparatorException(e.getMessage());
        }
    }

    public static Transformer createTransformer(String xslTemplate) throws ComparatorException {
        if (xslTemplate.isEmpty()) {
            throw new ComparatorException("Empty XSL-Template. Transformer can't be created.");
        }
        try {
            return TransformerFactory
                    .newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
                            null)
                    .newTransformer(new StreamSource(new StringReader(xslTemplate)));
        } catch (TransformerConfigurationException e) {
            throw new ComparatorException(e.getMessage());
        }
    }

    public static Transformer createAbcTransformer() throws ComparatorException {
        StringBuilder xsl = new StringBuilder(900);
        xsl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xsl.append("<xsl:stylesheet version=\"2.0\" \n");
        xsl.append("  xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n");
        xsl.append("  <xsl:output omit-xml-declaration=\"yes\" indent=\"yes\" />\n");
        xsl.append("  <xsl:strip-space elements=\"*\"/>\n\n");
        xsl.append("  <xsl:template match=\"node() | @*\">\n");
        xsl.append("    <xsl:copy>\n");
        xsl.append("      <xsl:apply-templates select=\"@*\">\n");
        xsl.append("         <xsl:sort select=\"name()\"/>\n");
        xsl.append("       </xsl:apply-templates>\n\n");
        xsl.append("       <xsl:apply-templates select=\"node()\">\n");
        xsl.append("          <xsl:sort select=\"name()\"/>\n");
        xsl.append("       </xsl:apply-templates>\n\n");
        xsl.append("    </xsl:copy>\n");
        xsl.append("  </xsl:template>\n\n");
        xsl.append("</xsl:stylesheet>");
        try {
            return createTransformer(xsl.toString());
        } catch (ComparatorException e) {
            throw new ComparatorException(e.getMessage());
        }
    }

    public static String transformXml(String inputXml, Transformer transformer) throws ComparatorException {
        if (transformer == null) {
            throw new ComparatorException("Failed to prepare xml (parameters are not set)");
        }
        Writer out = new StringWriter(inputXml.length());
        try {
            transformer.transform(new StreamSource(new StringReader(inputXml)), new StreamResult(out)); 
        } catch (TransformerException e) {
            throw new ComparatorException(e.getMessage());
        }
        return out.toString();
    }

    public static String getFullXPath(Node n, boolean localNames) {
        if (null == n) {
            return null;
        }

        Node parent = null;
        Stack<Node> hierarchy = new Stack<Node>();

        hierarchy.push(n); // push element on stack

        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                parent = ((Attr) n).getOwnerElement();
                break;
            case Node.ELEMENT_NODE:
            case Node.DOCUMENT_NODE:
            case Node.TEXT_NODE:
                parent = n.getParentNode();
                break;
            default:
                throw new IllegalStateException("Unexpected Node type " + n.getNodeType());
        }

        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            hierarchy.push(parent); // push on stack
            parent = parent.getParentNode(); // get parent of parent
        }

        StringBuilder buffer = new StringBuilder();
        // construct xpath
        Object obj = null;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            boolean handled = false;

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;

                buffer.append("/");
                buffer.append((localNames) ? node.getLocalName() : node.getNodeName());

                if (node.hasAttributes() && include_Id_and_Name_Attrs_toFullXPath) {
                    // see if the element has a name or id attribute
                    if (e.hasAttribute("id")) {
                        // id attribute found - use that
                        buffer.append("[@id='" + e.getAttribute("id") + "']");
                        handled = true;
                    } else if (e.hasAttribute("name")) {
                        // name attribute found - use that
                        buffer.append("[@name='" + e.getAttribute("name") + "']");
                        handled = true;
                    }
                }

                if (!handled) {
                    // no known attribute we could use - get sibling index
                    int prevSiblings = 1;
                    Node prevSibling = node.getPreviousSibling();
                    while (null != prevSibling) {
                        if (prevSibling.getNodeType() == node.getNodeType()) {
                            if (prevSibling.getNodeName().equalsIgnoreCase(node.getNodeName())) {
                                prevSiblings++;
                            }
                        }
                        prevSibling = prevSibling.getPreviousSibling();
                    }
                    buffer.append("[" + prevSiblings + "]");
                }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                buffer.append("/@");
                buffer.append((localNames) ? node.getLocalName() : node.getNodeName());
            }
        }
        return buffer.toString();
    }

    public static String clearXPath(String xpath) {
        String regexp = ".*?text\\(\\)";
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(xpath);
        if (m.find()) {
            int idx = xpath.lastIndexOf("/");
            return xpath.substring(0, idx);
        } else {
            return xpath;
        }
    }

    public static boolean xpathStringContainsTag(String xpath, String tagName) {
        boolean result = false;
        if (xpath.endsWith("/")) {
            xpath = xpath.substring(0, xpath.length() - 1);
        }
        String regexp = ".*\\/" + tagName + "(\\[\\d+\\])*$";
        Pattern p = Pattern.compile(regexp);
        Matcher m = p.matcher(xpath);
        return m.find();
    }

    public static String xmlDocumentToString(Document xmlDoc) {
        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(xmlDoc), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException ex) {
            //
        }
        return null;
    }

}
