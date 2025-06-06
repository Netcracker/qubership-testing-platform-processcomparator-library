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

package org.qubership.automation.pc.comparator.impl;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.interfaces.IComparator;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Comparator implementation for JMS-style string messages that separates and compares
 * parameters and body content. This comparator supports XPath-based value mapping
 * with regular expression validation.
 * </p>
 * The class parses incoming `Data` objects into parameters and body sections, validates
 * body content using configured XPath and RegExp rules, and records all mismatches
 * as {@link DiffMessage} entries.
 * </p>
 * <p>Supported configuration parameters:</p>
 * <ul>
 *   <li><b>mapping_xpath</b>: newline-separated list of XPath expressions.</li>
 *   <li><b>mapping_regexp</b>: newline-separated list of regex patterns corresponding to XPath.</li>
 *   <li><b>body.comparator</b>: (optional) defines a specific comparator to use for body comparison.</li>
 * </ul>
 * Ported from Process Comparator 1.0.
 */
public class JmsComparator implements IComparator {

    List<DiffMessage> resultList = new ArrayList<>();

    public static final Pattern BODY
            = Pattern.compile("^([a-zA-Z\\s]+[\\=\\{]+[a-zA-Z\\s]+[\\=\\{]+)([\\s\\=\\{\\w\\}]+)([\\}]+)"
            + "([a-zA-Z\\s]+[\\=\\{\\s]+)([[\\s\\w%<>\\=\\+\\-\\:\\.\"\\/]*]+)([\\}\\s]+).*");
    public static final Pattern PARAMETERS
            = Pattern.compile("^([a-zA-Z\\s]+[\\=\\{]+[a-zA-Z\\s]+[\\=\\{]+)([\\s\\=\\{\\w\\}]+)([\\}]+)"
            + "([a-zA-Z\\s]+[\\=\\{\\s]+)([\\D\\d]+)([\\}\\s]+).*");
    public static final Pattern EMPTY_PARAM
            = Pattern.compile("^([a-zA-Z\\s]*)([\\s\\=\\{]+)([^$]+)([\\}]?).*");
    public static final Pattern PARAM
            = Pattern.compile("^([a-zA-Z\\s]*)([\\s\\=\\{]+)([\\w]+)([\\}]?).*");
    public static final String BODY_COMPARATOR = "body.comparator";


    @Override
    public List<DiffMessage> compare(Data er, Data ar, Parameters configuration) throws ComparatorException {

        String erJms = DataContentConverter.toString(er);
        final Parameters expectedParams = parseParameters(erJms);
        final String expectedBody = parseBody(erJms);


        String arJms = DataContentConverter.toString(ar);
        final Parameters actualParams = parseParameters(arJms);
        final String actualBody = parseBody(arJms);

        String test2 = "fdf";

        /*
        ParametersComparator.compareParameters(expectedParams, actualParams, result);
        final ComparatorsProvider comparatorsProvider
        = LocalContext.getInstance().getAs(LocalContextConstants.COMPARATORS_PROVIDER);
        final String comparatorName
        = parameters.getProperty(BODY_COMPARATOR, ComparatorsProvider.DEFAULT_INSTANCE_NAME);
        ParameterComparator parameterComparator = comparatorsProvider.getInstance(comparatorName);
        if (parameterComparator == null) {
            parameterComparator = comparatorsProvider.getInstance(ComparatorsProvider.DEFAULT_INSTANCE_NAME);
        }
        parameterComparator.compare(expectedBody, actualBody, result, parameters);
       */ 
        
        
        /* xml for test:
        <?xml version="1.0" encoding="UTF-8"?>
        <addresses>
        <address>
        <name>Joe</name>
        <street>Baker street </street>
        </address>
        </addresses>
        
       
        // configuration parameters for test above:
        configuration.put("mapping_xpath", "/addresses[1]/address[1]/name[1]\n/addresses[1]/address[1]/street[1]");
        configuration.put("mapping_regexp", "Joe\nBaker");
        //     }
        */
        String[] xpathList = configuration.getParameters("mapping_xpath").get(0).split("\n|\r|\n\r");
        String[] regexpList = configuration.getParameters("mapping_regexp").get(0).split("\n|\r|\n\r");

        String erXML = DataContentConverter.toString(er);
        String arXML = DataContentConverter.toString(ar);

        InputSource expected;
        InputSource actual;
        final XPath xpath = XPathFactory.newInstance().newXPath();
        for (int i = 0; i < xpathList.length; i++) {
            expected = new InputSource(new StringReader(erXML));
            actual = new InputSource(new StringReader(arXML));
            XPathExpression expr;
            try {
                expr = xpath.compile(xpathList[i]);
            } catch (XPathExpressionException e) {
                throw new ComparatorException("Failed to parse xpath", e);
            }
            final Pattern regexp = Pattern.compile(regexpList[i]);
            final String mappedXpath = xpathList[i];
            validate(expected, expr, regexp, mappedXpath, true);
            validate(actual, expr, regexp, mappedXpath, false);
        }
        return resultList;
    }

    private void compare(NodeList nodeList, Pattern regexp, String mappedXpath, boolean isControl) {

        for (int index = 0; index < nodeList.getLength(); index++) {
            final Node node = nodeList.item(index);
            if (node.getChildNodes().getLength() == 0
                    && (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE)) {

                String nodeXpath = getFullXPath(node);
                if (nodeXpath.equals(mappedXpath)) { //check only that node which xpath in xpath map
                    Node actNode = nodeList.item(index);
                    String nodeValue = actNode.getTextContent();
                    DiffMessage diffMessage = new DiffMessage();

                    //check only regexp with map values because xpath is checked in previously IF
                    if (!regexp.matcher(nodeValue).matches()) {
                        if (isControl) {
                            diffMessage.setExpected(nodeXpath);
                            diffMessage.setActual("");
                            diffMessage.setResult(ResultType.MODIFIED);
                            resultList.add(diffMessage);
                            break;
                        } else {
                            diffMessage.setExpected("");
                            diffMessage.setActual(nodeXpath);
                            diffMessage.setResult(ResultType.MODIFIED);
                            resultList.add(diffMessage);
                            break;
                        }
                    }
                }
            } else {
                compare(node.getChildNodes(), regexp, mappedXpath, isControl);
            }
        }
    }

    @Override
    public List<DiffMessage> compare(Data er, Data ar, Parameters configuration,
                                     boolean encoded) throws ComparatorException {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration,
                                     boolean encoded) throws ComparatorException {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String parseBody(String message) {
        final Matcher matcher = BODY.matcher(message);
        if (matcher.matches()) {
            return matcher.replaceFirst(matcher.group(5)).trim();
        }
        return "";
    }

    private Parameters parseParameters(String message) {
        final Parameters params = new Parameters();
        final Matcher matcher = PARAMETERS.matcher(message);
        if (!matcher.matches()) {
            return params;
        }
        String[] lines = matcher.replaceFirst(matcher.group(2)).trim().split("\n");
        for (String line : lines) {
            if (line.contains("{}")) {
                final Matcher match = EMPTY_PARAM.matcher(line);
                if (match.matches()) {
                    params.put(match.replaceFirst(match.group(1)).trim().replaceAll(" ", ""),
                            match.replaceFirst("").trim());
                }
            } else {
                final Matcher match = PARAM.matcher(line);
                if (match.matches()) {
                    params.put(match.replaceFirst(match.group(1)).trim().replaceAll(" ", ""),
                            match.replaceFirst(match.group(3)).trim());
                }
            }
        }
        return params;
    }


    // mapped xml place
    private void validate(InputSource source, XPathExpression expr, Pattern regexp, String mappedXpath,
                          boolean isControl) throws ComparatorException {

        NodeList nodeList;

        try {
            nodeList = (NodeList) expr.evaluate(source, XPathConstants.NODESET);

        } catch (XPathExpressionException e) {
            throw new ComparatorException("Failed to evaluate xpath", e);
        }
        compare(nodeList, regexp, mappedXpath, isControl);
    }

    public static String getFullXPath(Node n) {
        // abort early
        if (null == n) {
            return null;
        }

        // declarations
        Node parent = null;
        Stack<Node> hierarchy = new Stack<Node>();

        // push element on stack
        hierarchy.push(n);

        switch (n.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                parent = ((Attr) n).getOwnerElement();
                break;
            case Node.ELEMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.DOCUMENT_NODE:
                parent = n.getParentNode();
                break;
            case Node.TEXT_NODE:
                parent = n.getParentNode();
                break;
            default:
                throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
        }

        while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
            // push on stack
            hierarchy.push(parent);

            // get parent of parent
            parent = parent.getParentNode();
        }

        StringBuffer buffer = new StringBuffer();
        // construct xpath
        Object obj = null;
        while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
            Node node = (Node) obj;
            boolean handled = false;

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) node;

                buffer.append("/");
                buffer.append(node.getNodeName());

                if (node.hasAttributes()) {
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
                            if (prevSibling.getNodeName().equalsIgnoreCase(
                                    node.getNodeName())) {
                                prevSiblings++;
                            }
                        }
                        prevSibling = prevSibling.getPreviousSibling();
                    }
                    buffer.append("[" + prevSiblings + "]");
                }
                // }
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                buffer.append("/@");
                buffer.append(node.getNodeName());
            }
        }
        // return buffer
        return buffer.toString();
    }
}
