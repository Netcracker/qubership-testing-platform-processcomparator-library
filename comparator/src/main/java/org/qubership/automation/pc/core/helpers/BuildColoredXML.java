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

import static org.qubership.automation.pc.comparator.impl.XmlComparator.PARAMETER_ALPHABET_SORT;
import static org.qubership.automation.pc.comparator.impl.XmlComparator.PARAMETER_NAME_EXCLUDE_XPATH;
import static org.qubership.automation.pc.comparator.impl.XmlComparator.PARAMETER_NAME_SORTBY;
import static org.qubership.automation.pc.core.helpers.XmlUtils.secureXmlFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.utils.XmlDifferenceComparator;
import org.qubership.automation.pc.models.HighlighterNode;
import org.qubership.automation.pc.models.HighlighterResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BuildColoredXML {

    static final XPath XPATH = XPathFactory.newInstance().newXPath();

    private static String cutPrefixER = "";
    private static String cutPrefixAR = "";

    // Parameters which turn ON a XSLT-transformation of er/ar before highlighting
    private static Boolean abcSort = false;
    private static List<String> excludeXPaths;
    private static List<String> sortBy;
    private static List<NodeToInsert> erNodesToInsert = new ArrayList<>();
    private static List<NodeToInsert> arNodesToInsert = new ArrayList<>();

    public static HighlighterResult highlight(List<DiffMessage> differences,
                                              String er,
                                              String ar) throws ComparatorException {
        List<List<Integer>> erRowCoords = new ArrayList<>();
        List<List<Integer>> arRowCoords = new ArrayList<>();
        HighlighterNode erMessage;
        HighlighterNode arMessage;
        String preparedEr = XmlHelpers.cleanLineBreaks(er);
        String preparedAr = XmlHelpers.cleanLineBreaks(ar);

        try {
            Pattern compiledPattern = Pattern.compile("\\{[0-9]{0,4}\\}[a-zA-Z]{0,}");
            DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
            secureXmlFactory(fctr);
            //fctr.setNamespaceAware(true);
            DocumentBuilder bldr = fctr.newDocumentBuilder();

            Document parsedER = (StringUtils.isBlank(preparedEr))
                    ? null : bldr.parse(new InputSource(new StringReader(preparedEr)));
            Document parsedAR = (StringUtils.isBlank(preparedAr))
                    ? null : bldr.parse(new InputSource(new StringReader(preparedAr)));

            if (abcSort) {
                prepareInsertNodes(differences, parsedER, parsedAR);
            } else {
                erNodesToInsert = new ArrayList<>();
                arNodesToInsert = new ArrayList<>();
            }
            // check on null
            erMessage = processDifferences(
                    differences, parsedER, false, compiledPattern, erRowCoords, erNodesToInsert);
            arMessage = processDifferences(
                    differences, parsedAR, true, compiledPattern, arRowCoords, arNodesToInsert);

        } catch (ParserConfigurationException
                 | IOException
                 | SAXException
                 | TransformerException
                 | XPathExpressionException ex) {
            throw new ComparatorException(ex.getMessage(), 20150);
        }

        HighlighterResult hlResilt = new HighlighterResult();
        hlResilt.setEr(erMessage);
        hlResilt.setAr(arMessage);
        return hlResilt;
    }

    private static void getConfigurationParameters(Map<String,
            List<String>> rules) throws ParserConfigurationException {
        excludeXPaths = rules.get(PARAMETER_NAME_EXCLUDE_XPATH);
        if (excludeXPaths == null) {
            excludeXPaths = new ArrayList<>();
        } else {
            //Make (unnecessary, but for user convinience) test compile of excludeXPaths
            // - only to report error in more details
            // Unfortunately we can't do such test compile for 'sortBy' XSLT-template
            XPathExpression excludeXPathsCompiled;
            for (int k = 0; k < excludeXPaths.size(); k++) {
                try {
                    excludeXPathsCompiled = XPATH.compile(excludeXPaths.get(k));
                } catch (XPathExpressionException ex) {
                    throw new ParserConfigurationException(" Xpath = " + excludeXPaths.get(k)
                            .replace("\"", "`")
                            .replace("'", "`") + "; " + ex.getMessage());
                }
            }
        }
        sortBy = rules.get(PARAMETER_NAME_SORTBY);
        if (sortBy == null) {
            sortBy = new ArrayList<>();
        }

        abcSort = false;
        List<String> abcSortStrings = rules.get(PARAMETER_ALPHABET_SORT);
        if (abcSortStrings != null) {
            if ((!abcSortStrings.isEmpty()) && abcSortStrings.get(0).equals("true")) {
                abcSort = true;
            }
        }
    }

    public static HighlighterResult highlightByRules(List<DiffMessage> differences,
                                                     String er,
                                                     String ar,
                                                     Map<String, List<String>> rules) throws ComparatorException {
        abcSort = false;
        cutPrefixER = XmlHelpers.getXmlPrefix(er);
        cutPrefixAR = XmlHelpers.getXmlPrefix(ar);
        String preparedER = XmlHelpers.cleanCommentsAndStartXML(er);
        String preparedAR = XmlHelpers.cleanCommentsAndStartXML(ar);
        // inserting xpath attribute to each node to save original xpath before rule transformation
        try {
            DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
            secureXmlFactory(fctr);
            DocumentBuilder bldr = fctr.newDocumentBuilder();
            if (!StringUtils.isBlank(preparedER)) {
                Document parsedER = bldr.parse(new InputSource(new StringReader(preparedER)));
                getXPath(parsedER.getDocumentElement(), "", 1);
                preparedER = prettyPrintDocument(new DOMSource(parsedER), "xml");
            }

            if (!StringUtils.isBlank(preparedAR)) {
                Document parsedAR = bldr.parse(new InputSource(new StringReader(preparedAR)));
                getXPath(parsedAR.getDocumentElement(), "", 1);
                preparedAR = prettyPrintDocument(new DOMSource(parsedAR), "xml");
            }

        } catch (ParserConfigurationException | IOException | SAXException | TransformerException ex) {
            throw new ComparatorException(ex.getMessage(), 20150);
        }

        if (rules.isEmpty() || (preparedER.isEmpty() && preparedAR.isEmpty())) {
            return highlight(differences, preparedER, preparedAR);
        } else {
            try {
                getConfigurationParameters(rules);
                if (abcSort) {
                    Transformer abcTransformerXslt = XmlHelpers.createAbcTransformer();
                    if (!preparedER.isEmpty()) {
                        preparedER = XmlHelpers.transformXml(preparedER, abcTransformerXslt);
                    }
                    if (!preparedAR.isEmpty()) {
                        preparedAR = XmlHelpers.transformXml(preparedAR, abcTransformerXslt);
                    }
                }
                if (!(excludeXPaths.isEmpty() && sortBy.isEmpty())) {
                    final Transformer transformer = XmlHelpers.createTransformer(excludeXPaths, sortBy);
                    if (transformer == null) {
                        // Maybe do NOT throw an Exception? Simply show initial er/ar instead?
                        throw new ParserConfigurationException("Unknown error while creating XSL-Transformer.");
                    } else {
                        preparedER = (preparedER.isEmpty())
                                ? preparedER : XmlHelpers.transformXml(preparedER, transformer);
                        preparedAR = (preparedAR.isEmpty())
                                ? preparedAR : XmlHelpers.transformXml(preparedAR, transformer);
                    }
                }
                return highlight(differences, preparedER, preparedAR);

            } catch (ParserConfigurationException ex) {
                throw new ComparatorException(ex.getMessage(), 20154);
            }
        }
    }

    public static HighlighterResult transformByRules(List<DiffMessage> differences,
                                                     String er,
                                                     String ar,
                                                     Map<String, List<String>> rules) throws ComparatorException {
        abcSort = false;
        HighlighterNode erMessage = new HighlighterNode();
        HighlighterNode arMessage = new HighlighterNode();
        erMessage.setValue(er);
        erMessage.setIsPlain(true);
        arMessage.setValue(ar);
        arMessage.setIsPlain(true);
        HighlighterResult resultMap = new HighlighterResult();
        resultMap.setEr(erMessage);
        resultMap.setAr(arMessage);

        if (rules.isEmpty() || (er.isEmpty() && ar.isEmpty())) {
            return resultMap;
        } else {
            try {
                getConfigurationParameters(rules);
            } catch (ParserConfigurationException ex) {
                throw new ComparatorException(ex.getMessage(), 20153);
            }
            if (excludeXPaths.isEmpty() && sortBy.isEmpty() && !abcSort) {
                return resultMap;
            } else {
                try {
                    String transformedER = er;
                    String transformedAR = ar;
                    if (abcSort) {
                        Transformer abcTransformerXslt = XmlHelpers.createAbcTransformer();
                        if (!er.isEmpty()) {
                            transformedER = XmlHelpers.transformXml(er, abcTransformerXslt);
                        }
                        if (!ar.isEmpty()) {
                            transformedAR = XmlHelpers.transformXml(ar, abcTransformerXslt);
                        }
                    }

                    if (!(excludeXPaths.isEmpty() && sortBy.isEmpty())) {
                        final Transformer transformer = XmlHelpers.createTransformer(excludeXPaths, sortBy);
                        if (transformer == null) {
                            // Maybe do NOT throw an Exception? Simply show initial er/ar instead?
                            throw new ParserConfigurationException("Unknown error while creating XSL-Transformer.");
                        } else {
                            transformedER = XmlHelpers.transformXml(transformedER, transformer);
                            transformedAR = XmlHelpers.transformXml(transformedAR, transformer);
                        }
                    }
                    erMessage.setValue(transformedER);
                    arMessage.setValue(transformedAR);
                    return resultMap;

                } catch (ParserConfigurationException ex) {
                    throw new ComparatorException(ex.getMessage(), 20154);
                }
            }
        }
    }

    private static String diffGetText(DiffMessage diff, Boolean isActual) {
        return isActual ? diff.getActual() : diff.getExpected();
    }

    private static Node skipComment(Node thisNode) {
        Node prev = thisNode.getPreviousSibling();
        if (prev == null) {
            return thisNode;
        } else {
            if (prev.getNodeType() != Node.COMMENT_NODE) {
                return thisNode;
            } else {
                String val = prev.getNodeValue();
                if (val.startsWith("END-Diff-")) {
                    return thisNode;
                } else {
                    return prev;
                }
            }
        }
    }

    private static Node insertToAbsolutePosition(NodeToInsert item, NodeList children) {
        Node insertedNode;
        int position = 0;
        item.positionAmongSiblings = computePreviousSiblings(item.sourceNode, false);
        for (int k = 0; k < children.getLength(); k++) {
            Node curnode = children.item(k);
            if (curnode.getNodeType() == Node.COMMENT_NODE) {
                continue;
            }
            if (position < item.positionAmongSiblings) {
                position++;
            } else {
                item.processed = true;
                insertedNode = item.parentNode.insertBefore(item.insertedNode, skipComment(curnode));
                return insertedNode;
            }
        }
        if (!item.processed) {
            item.processed = true;
            insertedNode = item.parentNode.appendChild(item.insertedNode);
            return insertedNode;
        }
        return null; // In fact, this point is unreachable
    }

    private static void getXPath(Node node, String parentXpath, int orderInParent) {
        String nodeName = node.getNodeName();
        nodeName = nodeName.substring(nodeName.indexOf(":") + 1, nodeName.length()); //removing namespace from node name
        String xpath = parentXpath + "/" + nodeName + "[" + orderInParent + "]";
        //TODO: To enable Set Value features fix this part
        ((Element) node).setAttribute("xpath", "");
        Map<String, Integer> elementNameCount = new HashMap<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                String childName = nodeList.item(i).getNodeName();
                int counter = 0;
                if (elementNameCount.get(childName) != null) {
                    counter = elementNameCount.get(childName);
                }
                elementNameCount.put(childName, ++counter);
                getXPath(nodeList.item(i), xpath, counter);
            }
        }
    }

    private static HighlighterNode processDifferences(List<DiffMessage> differences,
                                                      Document docEar,
                                                      Boolean isActual,
                                                      Pattern compiledPattern,
                                                      List<List<Integer>> coords,
                                                      List<NodeToInsert> nodesToInsert)
            throws IOException, TransformerException, XPathExpressionException, ComparatorException {
        HighlighterNode rootNode = new HighlighterNode();
        rootNode.setValue("$$$root$$$");
        if (docEar == null) {
            return rootNode;
        }

        // 1st loop through differences - sort paths in ascending order
        List<DiffMessage> diffs = new ArrayList<>();

        for (DiffMessage diff : differences) {
            String diffPath = diffGetText(diff, isActual);
            if (!(diffPath.isEmpty() || diff.getResult() == ResultType.SKIPPED)) {
                DiffMessage newDiff = new DiffMessage(diff);
                if (isActual) {
                    // simplification. Sorting and highlighting below are based on .expected
                    newDiff.setExpected(diffPath);
                }
                diffs.add(newDiff);
            }
        }

        diffs.sort(new XmlDifferenceComparator());
        setDiffToNodes(diffs, docEar);

        // Process missed/extra nodes...
        if (!nodesToInsert.isEmpty()) {
            for (NodeToInsert item : nodesToInsert) {
                Node insertedNode = null;
                if (item.childPosition == -1) {
                    insertedNode = item.parentNode.appendChild(item.insertedNode); // This is text node
                    item.processed = true;
                } else {
                    NodeList nl = item.parentNode.getChildNodes();
                    if (nl == null) {
                        insertedNode = item.parentNode.appendChild(item.insertedNode);
                        item.processed = true;
                    } else {
                        //insertedNode = insertBetweenNamesakes(item, nl);
                        // ABC-inserting sometimes causes problems
                        // - in case when user-defined sorting breaks ABC-sorting
                        if (!item.processed) {
                            // Not 100% accurate decision but... inserting the node based on positionAmongSiblings value
                            insertedNode = insertToAbsolutePosition(item, nl);
                        }
                    }
                }
                DiffMessage newDiff = new DiffMessage(item.diffMessage);
                newDiff.setExpected(item.parentXpath);
                newDiff.setResult(ResultType.HIDDEN);
                diffs.add(newDiff);
                if (insertedNode != null) {
                    String nodeName = insertedNode.getNodeName();
                    nodeName = nodeName == null ? "" : nodeName.substring(nodeName.indexOf(":") + 1, nodeName.length());
                    String xpath = item.parentXpath + "/" + nodeName + "[" + item.positionAmongSiblings + "]";
                    if (insertedNode.getNodeType() == Node.ELEMENT_NODE) {
                        ((Element) insertedNode).setAttribute("xpath", xpath);
                    }
                    insertedNode.setUserData("diff", newDiff, null);
                }
            }
        }

        Element firstElement = docEar.getDocumentElement();
        createHighlighterNode(firstElement, rootNode, 1);
        addInfoTagsToNodeValues(rootNode);
        return rootNode;
    }

    private static int createHighlighterNode(Node docNode,
                                             HighlighterNode parentNode,
                                             int rowNumber) throws IOException, TransformerException {
        HighlighterNode curNode = new HighlighterNode();
        parentNode.getChildren().add(curNode);
        curNode.setParent(parentNode);
        curNode.setRowNumber(rowNumber++);
        curNode.setValidationStatus("IDENTICAL");
        curNode.setOriginalXpath(((Element) docNode).getAttribute("xpath"));
        ((Element) docNode).removeAttribute("xpath");
        Map<String, List<Node>> childNodeLists = getElementNodes(docNode);
        List<Node> childNodes = childNodeLists.get("nodeList"); // get only tag elements
        if (childNodes.size() > 0) {
            Element tempNode = (Element) docNode.cloneNode(true);
            removeChilds(tempNode);
            //String strNode = prettyPrintDocument(new DOMSource(tempNode), "html");

            //this if is need to highlight attributes in tags.
            // It adds diffs to docNode and copies them to tempNode.
            // After parsing the tempNode, attribute diffs will be in curNode.
            for (int i = 0; i < docNode.getAttributes().getLength(); i++) {
                final Node attribute = docNode.getAttributes().item(i);
                addDifferenceToNodeValue(attribute, curNode, "");
                tempNode.getAttributes().item(i).setTextContent(attribute.getTextContent());
            }
            // highlight and insert text child nodes.
            for (int i = 0; i < docNode.getChildNodes().getLength(); i++) {
                final Node childNode = docNode.getChildNodes().item(i);
                if (childNode.getNodeType() == Node.TEXT_NODE) {
                    addDifferenceToNodeValue(childNode, curNode, curNode.getValue());
                    tempNode.appendChild(childNode);
                }
            }
            String strNode = elementToString(tempNode);
            String beginTag = strNode.substring(0, strNode.indexOf("</"));
            String endTag = strNode.substring(strNode.indexOf("</"));
            curNode.setValue(beginTag);

            for (Node childNode : childNodes) {
                rowNumber = createHighlighterNode(childNode, curNode, rowNumber);
            }

            HighlighterNode endNode = new HighlighterNode();
            endNode.setValue(endTag);
            endNode.setRowNumber(rowNumber++);
            endNode.setLinkedRow(curNode.getRowNumber());
            curNode.setLinkedRow(endNode.getRowNumber());
            endNode.setOriginalXpath(curNode.getOriginalXpath());
            endNode.setParent(parentNode);
            parentNode.getChildren().add(endNode);
            if (docNode.getUserData("diff") != null) {
                addDifferenceToNodeValue(docNode, curNode, curNode.getValue());
                addDifferenceToChildNode(docNode, curNode);
                addDifferenceToNodeValue(docNode, endNode, endNode.getValue());
            }
        } else {
            //search for diff in current and child nodes
            for (int i = 0; i < docNode.getAttributes().getLength(); i++) {
                final Node attribute = (Node) docNode.getAttributes().item(i);
                addDifferenceToNodeValue(attribute, curNode, "");
            }
            List<Node> textChildNodes = childNodeLists.get("textList");
            if (textChildNodes.size() > 0) {
                textChildNodes.forEach((textNode) -> {
                    addDifferenceToNodeValue(textNode, curNode, "");
                });
            }
            String strNode = elementToString((Element) docNode);
            addDifferenceToNodeValue(docNode, curNode, strNode);
        }
        return rowNumber;
    }

    private static Map<String, List<Node>> getElementNodes(Node node) {
        Map<String, List<Node>> results = new HashMap<>();
        List<Node> nodeList = new ArrayList<>();
        List<Node> textList = new ArrayList<>();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                nodeList.add(child);
                continue;
            }
            if (child.getNodeType() == Node.TEXT_NODE) {
                textList.add(child);
            }
        }
        results.put("nodeList", nodeList);
        results.put("textList", textList);
        return results;
    }

    private static void removeChilds(Node node) {
        for (int i = node.getChildNodes().getLength() - 1; i > -1; i--) {
            Node child = node.getChildNodes().item(i);
            short nodeType = child.getNodeType();
            if (nodeType == Node.ELEMENT_NODE || nodeType == Node.TEXT_NODE) {
                node.removeChild(child);
            }
        }
    }

    private static void addDifferenceToNodeValue(Node docNode, HighlighterNode curNode, String strNode) {
        if (docNode.getUserData("diff") != null) {
            DiffMessage diff = (DiffMessage) docNode.getUserData("diff");
            curNode.setValidationStatus(diff.getResult().toString());
            if (docNode.getNodeType() == Node.ELEMENT_NODE) {
                strNode = "$$$L$span data-block-id=\"pc-highlight-block\" class=\""
                        + diff.getResult().toString() + "\" $$$G$" + strNode + "$$$L$/span$$$G$";
            } else {
                String nodeValue = "$$$L$span data-block-id=\"pc-highlight-block\" class=\""
                        + diff.getResult().toString() + "\" $$$G$" + docNode.getTextContent() + "$$$L$/span$$$G$";
                docNode.setTextContent(nodeValue);
            }
        }
        curNode.setValue(strNode);
    }

    private static void addDifferenceToChildNode(Node docNode, HighlighterNode curNode) {
        if (curNode.getChildren().size() > 0) {
            for (HighlighterNode childNode : curNode.getChildren()) {
                addDifferenceToNodeValue(docNode, childNode, childNode.getValue());
                addDifferenceToChildNode(docNode, childNode);
            }
        }
    }

    private static void setDiffToNodes(List<DiffMessage> diffs, Document docEar) {
        String diffText;
        NodeList byXpath;
        // Process differences in REVERSE order - because arrays' diffs are processed via inserting of elements.
        // That's why all LATER xpathes become incorrect
        for (int diffIndex = diffs.size() - 1; diffIndex >= 0; diffIndex--) {
            DiffMessage diff = diffs.get(diffIndex);
            diffText = diff.getExpected();
            try {
                byXpath = (NodeList) XPATH.evaluate(diffText, docEar.getDocumentElement(), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                continue;
            }
            for (int i = 0; i < byXpath.getLength(); ++i) {
                try {
                    Node cnode = byXpath.item(i);
                    cnode.setUserData("diff", diff, null);
                } catch (DOMException ex) {
                    // DOMExceptions are not processed here (listed in Node.java): HIERARCHY_REQUEST_ERR,
                    // WRONG_DOCUMENT_ERR, NO_MODIFICATION_ALLOWED_ERR, NOT_FOUND_ERR, NOT_SUPPORTED_ERR
                }
            }
        }
    }

    private static void addInfoTagsToNodeValues(HighlighterNode rootNode) {
        if (!rootNode.getValue().equals("$$$root$$$")) {
            String value = "<div class=\"NORMAL\">" + rootNode.getValue()
                    .replace("<", "&lt;")
                    .replace(">", "&gt;") + "</div>";
            rootNode.setValue(value.replace("$$$L$", "<").replace("$$$G$", ">"));
        }
        if (rootNode.getChildren().size() > 0) {
            rootNode.getChildren().forEach((childNode) -> {
                addInfoTagsToNodeValues(childNode);
            });
        }
    }

    private static String elementToString(Element el) {
        StringBuilder sb = new StringBuilder();
        sb.append("<");
        sb.append(el.getTagName());
        NamedNodeMap attributes = el.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node attr = attributes.item(i);
            sb.append(" ").append(attr.getNodeName()).append("=").append(String.format("\"%s\"", attr.getNodeValue()));
        }
        sb.append(">");
        String nodeValue = el.getTextContent();
        if (nodeValue != null) {
            sb.append(nodeValue);
        }
        sb.append("</");
        sb.append(el.getTagName());
        sb.append(">");
        return sb.toString();
    }

    private static String prettyPrintDocument(DOMSource domSource,
                                              String mode) throws IOException, TransformerException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes" /* "no" */);
        transformer.setOutputProperty(OutputKeys.METHOD, mode);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(domSource, new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        return out.toString("UTF-8");
    }

    private static Node getNodeByXpath(String nodeXpath, Document earDoc) throws ComparatorException {
        if (StringUtils.isBlank(nodeXpath)) {
            return null;
        } else {
            try {
                XPathExpression expression = XPATH.compile(nodeXpath);
                Node nodeToInsert = (Node) expression.evaluate(earDoc, XPathConstants.NODE);
                return nodeToInsert;
            } catch (XPathExpressionException ex) {
                throw new ComparatorException(ex.getMessage(), 20154);
            }
        }
    }

    private static void prepareInsertNodes(List<DiffMessage> differences,
                                           Document docER,
                                           Document docAR) throws ComparatorException {
        NodeToInsert nodeInfo;
        erNodesToInsert = new ArrayList<>();
        arNodesToInsert = new ArrayList<>();
        for (DiffMessage diffMessage : differences) {
            switch (diffMessage.getResult()) {
                case MISSED:
                    nodeInfo = getNodeInfo(
                            diffMessage.getExpected(), diffMessage.getActual(), docER, docAR, diffMessage);
                    if (nodeInfo != null) {
                        arNodesToInsert.add(nodeInfo);
                        // empty path more convenient for main highlight algorithm (value was "parent:<xpath>")
                        diffMessage.setActual("");
                    }
                    break;
                case EXTRA:
                    nodeInfo = getNodeInfo(
                            diffMessage.getActual(), diffMessage.getExpected(), docAR, docER, diffMessage);
                    if (nodeInfo != null) {
                        erNodesToInsert.add(nodeInfo);
                        // empty path more convenient for main highlight algorithm (value was "parent:<xpath>")
                        diffMessage.setExpected("");
                    }
                    break;
                default:
                    // do nothing
                    break;
            }
        }
        sortByParentAndPosition(erNodesToInsert);
        sortByParentAndPosition(arNodesToInsert);
    }

    private static void sortByParentAndPosition(List<NodeToInsert> items) {
        Collections.sort(items, new Comparator<NodeToInsert>() {
            @Override
            public int compare(NodeToInsert n1, NodeToInsert n2) {
                int cmp = n1.parentXpath.compareTo(n2.parentXpath);
                if (cmp == 0) {
                    return (n1.childPosition - n2.childPosition);
                } else {
                    return cmp;
                }
            }
        });
    }

    private static NodeToInsert getNodeInfo(String nodeXpath,
                                            String parentNodeXpath,
                                            Document docSource,
                                            Document docTarget,
                                            DiffMessage diffMessage) throws ComparatorException {
        NodeToInsert info;
        if (StringUtils.isBlank(nodeXpath) || StringUtils.isBlank(parentNodeXpath)) {
            return null;
        }
        if (!parentNodeXpath.startsWith("parent:")) {
            return null;
        }
        Node nodeToInsert = getNodeByXpath(nodeXpath, docSource);
        Node placeToInsert = getNodeByXpath(parentNodeXpath.substring(7), docTarget);
        Node imported = docTarget.importNode(nodeToInsert, true);
        if (nodeToInsert != null && placeToInsert != null) {
            info = new NodeToInsert();
            info.processed = false;
            info.diffMessage = diffMessage;
            info.sourceNode = nodeToInsert;
            info.insertedNode = imported; // = nodeToInsert;
            info.parentNode = placeToInsert;
            info.parentXpath = parentNodeXpath.substring(7);
            if (nodeXpath.contains("text()")) {
                info.childPosition = -1;
                info.positionAmongSiblings = 0;
            } else {
                info.childPosition = Integer.parseInt(
                        nodeXpath.substring(nodeXpath.lastIndexOf("[") + 1, nodeXpath.lastIndexOf("]")));
                // It may be needed (position of the node among its siblings)
                info.positionAmongSiblings = computePreviousSiblings(nodeToInsert, false);
            }
            return info;
        } else {
            return null;
        }
    }

    private static int computePreviousSiblings(Node thisNode, boolean countComments) {
        int count = 0;
        Node prevNode = thisNode;
        for (;;) {
            prevNode = prevNode.getPreviousSibling();
            if (prevNode == null) {
                break;
            }
            if (countComments) {
                count++;
            } else if (prevNode.getNodeType() != Node.COMMENT_NODE) {
                count++;
            }
        }
        return count;
    }

    private static class NodeToInsert {

        public DiffMessage diffMessage;
        public String parentXpath;
        public int childPosition;
        public int positionAmongSiblings;
        public Node parentNode;
        public Node insertedNode;
        public Node sourceNode;
        public boolean processed = false;

        public NodeToInsert() {
        }
    }
}
