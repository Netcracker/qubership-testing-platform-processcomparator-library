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

import static org.qubership.automation.pc.core.helpers.XmlUtils.secureXmlFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.helpers.XmlHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Comparator for validating XML nodes against XPath and regular expression mappings.
 * </p>
 * Designed to check if the values at specific XPath locations in the expected (ER)
 * and actual (AR) XML documents match defined regular expression patterns.
 * Useful for masked or template-based XML validation where exact value matches are not required.
 * Ported from Process Comparator 1.0.
 */
public class MaskedXmlComparator extends AbstractComparator {
    public static final String PARAMETER_NAME_MAPPING_XPATH = "mappingXpath";
    public static final String PARAMETER_NAME_MAPPING_REGEXP = "mappingRegexp";
  
    final XPath xpath = XPathFactory.newInstance().newXPath();
    private List<XPathExpression> mappingXpathCompiled;
    private List<Pattern> mappingRegexpCompiled;
    private List<String> mappingXpath;
    private List<String> mappingRegexp;
    
    List<DiffMessage> resultList = new ArrayList<>();
    int diffCounter = 0; // Global diffs' counter (diffMessages are produced in different places of this comparator)

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        getConfigurationParameters(configuration);
        Document docER;
        Document docAR;
        try {
            DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
            secureXmlFactory(fctr);
            fctr.setNamespaceAware(true);
            DocumentBuilder bldr;
            bldr = fctr.newDocumentBuilder();
            docER = bldr.parse(new InputSource(new StringReader(er)));
            docAR = bldr.parse(new InputSource(new StringReader(ar)));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new ComparatorException(ex.getMessage().replace("\"", "`").replace("'", "`"), 20002);
        }

        for (int i = 0; i < mappingXpathCompiled.size(); i++) {
            validate(docER, mappingXpathCompiled.get(i), mappingRegexpCompiled.get(i), mappingRegexp.get(i), true);
            validate(docAR, mappingXpathCompiled.get(i), mappingRegexpCompiled.get(i), mappingRegexp.get(i), false);
        }
        return resultList;
    }

    private void getConfigurationParameters(Parameters configuration) throws ComparatorException {
        mappingXpath = configuration.getParameters(PARAMETER_NAME_MAPPING_XPATH);
        if (mappingXpath == null) {
            mappingXpath = new ArrayList<>();
        }
        mappingRegexp = configuration.getParameters(PARAMETER_NAME_MAPPING_REGEXP);
        if (mappingRegexp == null) {
            mappingRegexp = new ArrayList<>();
        }
        
        if (mappingXpath.size() != mappingRegexp.size()) {
            throw new ComparatorException("Mappings and Regexps don't correspond each other", 20002);
        } else {
            mappingXpathCompiled = new ArrayList<>();
            for (int k = 0; k < mappingXpath.size(); k++) {
                try {
                    mappingXpathCompiled.add(xpath.compile(mappingXpath.get(k)));
                } catch (XPathExpressionException ex) {
                    throw new ComparatorException(" Xpath = " + mappingXpath.get(k).replace("\"", "`")
                            .replace("'","`") + "; " + ex.getMessage(), 20004);
                }
            }
            mappingRegexpCompiled = new ArrayList<>();
            for (int k = 0; k < mappingRegexp.size(); k++) {
                try {
                    mappingRegexpCompiled.add(Pattern.compile(mappingRegexp.get(k)));
                } catch (PatternSyntaxException ex) {
                    throw new ComparatorException(" Regexp = " + mappingRegexp.get(k)
                            .replace("\"", "`")
                            .replace("'","`") + "; " + ex.getMessage(), 20003);
                }
            }
        }
    }
        
    private void validate(Document doc, XPathExpression expr, Pattern regexp, String regexpString,
                          boolean isControl) throws ComparatorException {
        NodeList nodeList;
     
        try {
            nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);  
        } catch (XPathExpressionException e) {
            throw new ComparatorException("Failed to evaluate xpath ", e);
        }
        checkRecursively(nodeList, regexp, regexpString, isControl, 0);
    }

    private void checkRecursively(NodeList nodeList, Pattern regexp, String regexpString, boolean isControl,
                                  int level) {

        for (int index = 0; index < nodeList.getLength(); index++) {
            final Node node = nodeList.item(index);
            if (node.getChildNodes().getLength() == 0
                    && (node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE)) {
                String nodeValue = node.getTextContent();
                if (!regexp.matcher(nodeValue).matches()) {
                    DiffMessage diffMessage = new DiffMessage();
                    String nodeXpath = XmlHelpers.getFullXPath(node, true);
                    if (isControl) {
                        diffMessage.setExpected(nodeXpath);
                        diffMessage.setActual("");
                    } else {
                        diffMessage.setExpected("");
                        diffMessage.setActual(nodeXpath);
                    }        
                    diffMessage.setResult(ResultType.MODIFIED);
                    diffMessage.setOrderId(++diffCounter);
                    diffMessage.setDescription("Node value doesn't match regexp: " + regexpString);
                    resultList.add(diffMessage);
                }
            } else  if (level == 0) {
                // Only one level of recursion. We are interested only in children, not in grandchildren
                checkRecursively(node.getChildNodes(), regexp, regexpString, isControl, 1);
            }           
        }
    }

}
