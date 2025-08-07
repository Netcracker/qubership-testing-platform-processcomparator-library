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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.helpers.IntelliNodeMatcher;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.helpers.XmlHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Comparison.Detail;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

/**
 * Comparator for detailed XML comparison with support for XPath exclusions,
 * sorting, value masking using regular expressions, and customizable diff output formatting.
 * <p>
 * Supports a wide range of configuration parameters to tune comparison behavior,
 * including whitespace ignoring, attribute/value matching, node exclusion, and
 * advanced key-based node pairing.
 * Ported from Process Comparator 1.0.
 */
public class XmlComparator extends AbstractComparator {

    public static final String PARAMETER_NAME_SAVE_DIFF_VALUE = "saveDiffValue";

    public static final String PARAMETER_NAME_EXCLUDE_XPATH = "excludeXpath";
    public static final String PARAMETER_NAME_MAPPING_XPATH = "mappingXpath";
    public static final String PARAMETER_NAME_MAPPING_REGEXP = "mappingRegexp";
    public static final String PARAMETER_NAME_XR = "mappingXR";
    public static final String DELIMETER_XR_STRING = "==";
    public static final String PARAMETER_NAME_SORTBY = "SortBy";
    public static final String PARAMETER_ALPHABET_SORT = "abcSort";
    public static final String PARAMETER_NAME_IGNORE_WHITE_SPACE = "ignoreWhiteSpace";
    public static final String PARAMETER_NAME_SIMILAR2MODIFIED = "similar2modified";
    public static final String PARAMETER_NAME_CHANGE_DIFF_RESULT = "changeDiffResult";
    public static final String PARAMETER_NAME_KEY_CHILD = "keyChild";
    public static final String PARAMETER_NAME_KEY_CHILD_IGNORE = "keyChildStrict";
    public static final String PARAMETER_ATP_DIFFERENCES_FORMAT = "atpDiffData";
    public static final String PARAMETER_ATP_DIFFERENCES_ADDITIONAL_KEY = "atpDiffDataAdditionalKey";

    private static final List<ComparisonType> ALLOWED_DIFFERENCES
            = Arrays.asList(new ComparisonType[]{
            ComparisonType.ATTR_NAME_LOOKUP,
            ComparisonType.ATTR_VALUE,
            ComparisonType.TEXT_VALUE,
            ComparisonType.CHILD_LOOKUP,
            ComparisonType.ELEMENT_TAG_NAME
    });
    /* May be we should process some other difference types such as:
        CHILD_NODELIST_LENGTH   // Result if corresponding nodes have different numbers of children;
                                // ignore it because it will be at least one extra difference pointing
                                //to concrete child
        ELEMENT_NUM_ATTRIBUTES  // Result if corresponding nodes have different numbers of attributes;
                                // ignore it because it will be at least one extra difference pointing
                                // to concrete attribute (missed or extra)
        NODE_TYPE               // When?
     */

    private final XPath xpath = XPathFactory.newInstance().newXPath();
    private List<XPathExpression> excludeXPathsCompiled;
    private List<XPathExpression> mappingXpathCompiled;
    private List<Pattern> mappingRegexpCompiled;

    private List<String> excludeXPaths;
    private List<String> sortBy;
    private List<String> mappingXpath;
    private List<String> mappingRegexp;
    private List<String> mappingXR;
    private boolean changeDiffResult;
    private boolean abcSort;
    private List<ChangeDiffResultRule> listChangeDiffResultRule;
    private Map<String, List<KeyChildDescription>> keyChildren;
    private boolean keyChildrenIgnore;

    private List<String> atpMacros;
    private String atpFormat;
    private boolean needAtpFormatting;
    private Map<String, String> atpMacrosValues;
    private List<AtpDiffAdditionalKeyRule> atpDiffAdditionalKeyRules;

    private boolean saveDiffValue;

    private static final Logger log = LoggerFactory.getLogger(XmlComparator.class);

    int diffCounter = 0; // Global diffs' counter (diffMessages are produced in different places of this comparator)

    private Document docER;
    private Document docAR;
    private Document docErWoNamespace;
    private Document docArWoNamespace;

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        List<DiffMessage> resultList = new ArrayList<>();
        try {
            if (er.isEmpty() && ar.isEmpty()) {
                return resultList;
            }
            log.debug("[XML comparator] building");
            DocumentBuilderFactory fctr = DocumentBuilderFactory.newInstance();
            DocumentBuilderFactory fctrWoNamespace = DocumentBuilderFactory.newInstance();
            secureXmlFactory(fctr);
            secureXmlFactory(fctrWoNamespace);
            fctr.setNamespaceAware(true);
            DocumentBuilder bldr;
            DocumentBuilder bldrWoName;
            log.debug("[XML comparator] built");
            try {
                bldr = fctr.newDocumentBuilder();
                bldrWoName = fctrWoNamespace.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                throw new ComparatorException(ex.getMessage(), 20002);
            }
            log.debug("[XML comparator] getting conf");
            getConfigurationParameters(configuration);
            String erXML;
            String arXML;
            if (abcSort) {
                log.debug("[XML comparator] abcSorting");
                Transformer abcTransformerXslt = XmlHelpers.createAbcTransformer();
                erXML = XmlHelpers.transformXml(er, abcTransformerXslt);
                arXML = XmlHelpers.transformXml(ar, abcTransformerXslt);
                log.debug("[XML comparator] abcSorted");
            } else {
                erXML = er;
                arXML = ar;
            }
            Transformer transformerXslt = null;
            if (!excludeXPaths.isEmpty() || !sortBy.isEmpty()) {
                log.debug("[XML comparator] transformer");
                transformerXslt = XmlHelpers.createTransformer(excludeXPaths, sortBy);
                log.debug("[XML comparator] transformed");
            }
            CleanupAndParseResult parsedER = prepareXml(bldr, transformerXslt, erXML, false);
            CleanupAndParseResult parsedAR = prepareXml(bldr, transformerXslt, arXML, true);
            if (!parsedER.errorMessage.isEmpty() || !parsedAR.errorMessage.isEmpty()) {
                throw new SAXException((parsedER.errorMessage.isEmpty())
                        ? parsedAR.errorMessage : parsedER.errorMessage + "\n" + parsedAR.errorMessage);
            }
            log.debug("[XML comparator] parsing");
            docER = bldr.parse(new InputSource(new StringReader(parsedER.preparedXML)));
            docAR = bldr.parse(new InputSource(new StringReader(parsedAR.preparedXML)));
            docErWoNamespace = bldrWoName.parse(new InputSource(new StringReader(parsedER.preparedXML)));
            docArWoNamespace = bldrWoName.parse(new InputSource(new StringReader(parsedAR.preparedXML)));
            IntelliNodeMatcher nodeMatcher = new IntelliNodeMatcher(keyChildren, keyChildrenIgnore);
            log.debug("[XML comparator] diff builder compare");
            Diff myDiff = DiffBuilder.compare(docER).withTest(docAR)
                    .checkForSimilar() // a different order is always 'similar' not equals.
                    .withNodeMatcher(nodeMatcher)
                    .build();
            log.debug("[XML comparator] compared");
            Iterable<Difference> differences = myDiff.getDifferences();
            log.debug("[XML comparator] got differences");
            for (Difference difference : differences) {
                Comparison comparison = difference.getComparison();
                if (!ALLOWED_DIFFERENCES.contains(comparison.getType())) {
                    continue;
                }

                DiffMessage diffMessage = new DiffMessage();

                if (saveDiffValue) {
                    try {
                        log.debug("[XML comparator] saveDiffValue");
                        diffMessage.setExpectedValue(
                                nodeToString(difference.getComparison().getControlDetails().getTarget()));
                    } catch (NullPointerException ex) {
                        throw new RuntimeException(ex);
                    }
                    try {
                        log.debug("[XML comparator] saveDiffValue1");
                        diffMessage.setActualValue(
                                nodeToString(difference.getComparison().getTestDetails().getTarget()));
                    } catch (NullPointerException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                boolean attributeDifference = false;
                String ruleActionDescription = "";
                switch (comparison.getType()) {
                    case ATTR_NAME_LOOKUP: // node's attribute is missed or extra on the other side
                        log.debug("[XML comparator] ATTR_NAME_LOOKUP");
                        diffMessage.setResult(ResultType.MODIFIED);
                        diffMessage.setExpected(comparison.getControlDetails().getXPath());
                        diffMessage.setActual(comparison.getTestDetails().getXPath());
                        attributeDifference = true;
                        break;
                    // attribute values are different, but it's important if there is an empty value on the one side
                    case ATTR_VALUE:
                        log.debug("[XML comparator] ATTR_VALUE");
                        if (StringUtils.isBlank(comparison.getControlDetails().getValue().toString())
                                || StringUtils.isBlank(comparison.getTestDetails().getValue().toString())) {
                            diffMessage.setResult(ResultType.MODIFIED);
                        } else {
                            diffMessage.setResult(ResultType.SIMILAR);
                            if (comparison.getControlDetails().getValue().toString() != null
                                    && comparison.getControlDetails().getValue().toString().startsWith("regexp:")) {
                                if (comparison.getTestDetails().getValue().toString()
                                        .matches(comparison.getControlDetails().getValue().toString()
                                                .substring(7))) {
                                    diffMessage.setResult(ResultType.IDENTICAL);
                                } else {
                                    diffMessage.setResult(ResultType.MODIFIED);
                                }
                            }
                        }
                        log.debug("[XML comparator] getControlDetails");
                        diffMessage.setExpected(comparison.getControlDetails().getXPath());
                        diffMessage.setActual(comparison.getTestDetails().getXPath());
                        attributeDifference = true;
                        break;
                    case TEXT_VALUE:
                        diffMessage.setResult(ResultType.SIMILAR);
                        if (comparison.getControlDetails().getValue().toString() != null
                                && comparison.getControlDetails().getValue().toString().startsWith("regexp:")) {
                            log.debug("[XML comparator] TEXT_VALUE");
                            try {
                                if (comparison.getTestDetails().getValue().toString()
                                        .matches(comparison.getControlDetails().getValue().toString()
                                                .substring(7))) {
                                    diffMessage.setResult(ResultType.IDENTICAL);
                                } else {
                                    diffMessage.setResult(ResultType.MODIFIED);
                                }
                            } catch (PatternSyntaxException ex) {
                                log.warn("PatternSyntaxException: ", ex);
                            }
                        }
                        break;
                    // Result if the parent node has the child on the one side
                    // but corresponding parent doesn't have corresponding child on the other side
                    case CHILD_LOOKUP:
                        log.debug("[XML comparator] CHILD_LOOKUP");
                        if (comparison.getControlDetails().getXPath() == null) {
                            diffMessage.setResult(ResultType.EXTRA);
                        } else {
                            diffMessage.setResult(ResultType.MISSED);
                        }
                        break;
                    default:
                        diffMessage.setResult(ResultType.MODIFIED);
                }

                if (!attributeDifference) {
                    log.debug("[XML comparator] attributeDifference");
                    diffMessage.setExpected((comparison.getControlDetails().getXPath() == null)
                            ? ((comparison.getControlDetails().getParentXPath() == null)
                            ? "" : "parent:" + comparison.getControlDetails().getParentXPath())
                            : comparison.getControlDetails().getXPath()
                    );
                    diffMessage.setActual((comparison.getTestDetails().getXPath() == null)
                            ? ((comparison.getTestDetails().getParentXPath() == null)
                            ? "" : "parent:" + comparison.getTestDetails().getParentXPath())
                            : comparison.getTestDetails().getXPath()
                    );
                }
                if (changeDiffResult && (comparison.getControlDetails().getXPath() != null
                        || comparison.getTestDetails().getXPath() != null)) {
                    log.debug("[XML comparator] changeDiffResult");
                    for (ChangeDiffResultRule changeDiffResultRule : listChangeDiffResultRule) {
                        if (verifyXpath(comparison.getControlDetails(), docER, changeDiffResultRule.xpathsCompiled,
                                diffMessage.getExpected()) || verifyXpath(comparison.getTestDetails(),
                                docAR, changeDiffResultRule.xpathsCompiled, diffMessage.getActual())) {
                            if (changeDiffResultRule.action.equals("ignore")) {
                                diffMessage.setResult(ResultType.IDENTICAL);
                                ruleActionDescription = "; Result is set to IDENTICAL due to 'ignore'-rule.";
                            } else if (changeDiffResultRule.action.equals("change")
                                    && diffMessage.getResult().equals(changeDiffResultRule.oldResult)) {
                                diffMessage.setResult(changeDiffResultRule.newResult);
                                ruleActionDescription = "; Result is set to " + changeDiffResultRule.newResult
                                        + " due to 'changeDiffResult'-rule.";
                            }
                            break; // Skip all other rules' checking if this rule is applied
                        }
                    }
                }
                if (needAtpFormatting) {
                    log.debug("[XML comparator] needAtpFormatting");
                    Map<String, String> customDiffFormattingMacroses = fillAtpValues(difference, atpMacrosValues);

                    // Create Additional Keys
                    for (AtpDiffAdditionalKeyRule rule : atpDiffAdditionalKeyRules) {
                        // ar
                        if (verifyNodeTagName(comparison.getTestDetails().getXPath(), rule.sourceStr)) {
                            String targetXPath = rule.buildTarget(comparison.getTestDetails().getXPath());
                            String result = this.getAtpDiffAdditionalKey(targetXPath, docArWoNamespace);
                            customDiffFormattingMacroses.put(rule.name, result);
                            customDiffFormattingMacroses.put(rule.name + ".xpath", targetXPath);
                        }
                    }

                    String atpDescription = ScriptUtils.prepareParameterizedScript(
                            atpFormat, customDiffFormattingMacroses, true, false);
                    diffMessage.setDescription(atpDescription);
                } else {
                    diffMessage.setDescription(difference.toString() + ruleActionDescription);
                }
                diffMessage.setOrderId(++diffCounter);
                resultList.add(diffMessage);
            }

            resultList.addAll(doExtraXpathRegexpCheckings(docER, docAR, mappingXpathCompiled, mappingRegexpCompiled));
            log.debug("[XML comparator] resultList");
            return resultList;
        } catch (Exception ex) {
            String exString = ex.getMessage().replace("\"", "`").replace("'", "`");
            if (ex.getCause() != null) {
                exString += "\n" + ex.getCause().getMessage();
            }
            throw new ComparatorException(exString, 20002); // Escaping " - in order to show messages in UI
        }
    }

    private static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        Node target = node.getNodeType() == Node.TEXT_NODE ? node.getParentNode() : node;
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(target), new StreamResult(sw));
        } catch (TransformerException te) {
            //            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }

    private CleanupAndParseResult prepareXml(DocumentBuilder bldr,
                                             Transformer transformerXslt,
                                             String xmlString,
                                             boolean isActual) throws ComparatorException {
        CleanupAndParseResult result = new CleanupAndParseResult();

        //1st Draft parsing of er/ar - only to check if er/ar are valid XML documents
        //Before check XML remove linebreaks and comments
        String prepared = XmlHelpers.cleanCommentsAndStartXML(xmlString);
        if (!prepared.isEmpty()) {
            try {
                Document doc = bldr.parse(new InputSource(new StringReader(prepared)));
            } catch (SAXParseException ex) {
                result.errorMessage = String.format(((isActual) ? "ar" : "er")
                        + " has XML-format error(s): [line;column]=[%d;%d] - %s",
                        ex.getLineNumber(), ex.getColumnNumber(), ex.getMessage());
            } catch (SAXException | IOException ex) {
                result.errorMessage = "Error while checking XML format of "
                        + ((isActual) ? "ar" : "er") + ": " + ex.getMessage();
            }
        }
        if (!result.errorMessage.isEmpty()) {
            result.preparedXML = prepared;
            return result;
        }

        prepared = XmlHelpers.cleanLineBreaks(prepared);

        if (!excludeXPaths.isEmpty() || !sortBy.isEmpty()) {
            prepared = (prepared.isEmpty())
                    ? prepared : XmlHelpers.cleanXml(XmlHelpers.transformXml(prepared, transformerXslt));
        }

        // After all cleanups & XSLT-transformations (rules 'excludeXPath' and/or 'SortBy' can be invalid - why not?)
        // er and/or ar can become empty
        if (prepared.isEmpty()) {
            result.errorMessage = ((isActual) ? "ar" : "er")
                    + " value is empty (After all cleanups & XSLT-transformations)!";
        }

        result.preparedXML = prepared;
        return result;
    }

    private void getConfigurationParameters(Parameters configuration) throws ComparatorException {
        abcSort = configuration.getBooleanParameter(PARAMETER_ALPHABET_SORT, false);
        saveDiffValue = configuration.getBooleanParameter(PARAMETER_NAME_SAVE_DIFF_VALUE, false);

        excludeXPaths = configuration.getParameters(PARAMETER_NAME_EXCLUDE_XPATH);
        if (excludeXPaths == null) {
            excludeXPaths = new ArrayList<>();
        }
        excludeXPathsCompiled = new ArrayList<>();
        for (int k = 0; k < excludeXPaths.size(); k++) {
            try {
                excludeXPathsCompiled.add(xpath.compile(excludeXPaths.get(k)));
            } catch (XPathExpressionException ex) {
                throw new ComparatorException(" Xpath = " + excludeXPaths.get(k).replace("\"", "`")
                        .replace("'", "`") + "; " + ex.getMessage(), 20004);
            }
        }
        sortBy = configuration.getParameters(PARAMETER_NAME_SORTBY);
        if (sortBy == null) {
            sortBy = new ArrayList<>();
        }

        changeDiffResult = false;
        listChangeDiffResultRule = new ArrayList<>();
        List<String> arrayChangeDiffResultRule = configuration.getParameters(PARAMETER_NAME_CHANGE_DIFF_RESULT);
        if (arrayChangeDiffResultRule != null) {
            ChangeDiffResultRule prevRule = null;
            for (int k = 0; k < arrayChangeDiffResultRule.size(); k++) {
                String ruleItem = arrayChangeDiffResultRule.get(k);
                ChangeDiffResultRule thisRule = ChangeDiffResultRule.checkRule(ruleItem);
                if (thisRule == null) {
                    // ruleItem consists of xpaths delimited with new line
                    if (prevRule != null) {
                        prevRule.addXPaths(Arrays.asList(ruleItem.split("\r\n|\n|\r")));
                    }
                } else {
                    if (prevRule != null) {
                        listChangeDiffResultRule.add(prevRule);
                    }
                    prevRule = thisRule;
                }
            }
            if (prevRule != null) {
                listChangeDiffResultRule.add(prevRule);
            }
        }

        // Unneeded;
        // Left for backward compatibility (rule 'similar2modified' was introduced before than 'changeDiffResult')
        List<String> changeDiffResultXPaths = configuration.getParameters(PARAMETER_NAME_SIMILAR2MODIFIED);
        if (changeDiffResultXPaths != null) {
            listChangeDiffResultRule.add(new ChangeDiffResultRule("change", ResultType.SIMILAR,
                    ResultType.MODIFIED, changeDiffResultXPaths));
        }
        if (!listChangeDiffResultRule.isEmpty()) {
            changeDiffResult = true;
        }

        mappingXpath = configuration.getParameters(PARAMETER_NAME_MAPPING_XPATH);
        if (mappingXpath == null) {
            mappingXpath = new ArrayList<>();
        }
        mappingRegexp = configuration.getParameters(PARAMETER_NAME_MAPPING_REGEXP);
        if (mappingRegexp == null) {
            mappingRegexp = new ArrayList<>();
        }

        mappingXR = configuration.getParameters(PARAMETER_NAME_XR);
        if (mappingXR != null) {
            for (String str : mappingXR) {
                String[] stringArray = str.split(DELIMETER_XR_STRING);
                if (stringArray.length != 2) {
                    continue; // Required format is: xpath==regexp i.e. //*[local-name()='item']==qwerty
                }
                mappingXpath.add(stringArray[0]);
                mappingRegexp.add(stringArray[1]);
            }
        }

        if (mappingXpath.size() != mappingRegexp.size()) {
            throw new ComparatorException("Mappings and Regexps don't correspond each other", 20002);
        } else {
            mappingXpathCompiled = new ArrayList<>();
            for (int k = 0; k < mappingXpath.size(); k++) {
                try {
                    mappingXpathCompiled.add(xpath.compile(mappingXpath.get(k)));
                } catch (XPathExpressionException ex) {
                    throw new ComparatorException(" Xpath = " + mappingXpath.get(k)
                            .replace("\"", "`")
                            .replace("'", "`") + "; " + ex.getMessage(), 20004);
                }
            }
            mappingRegexpCompiled = new ArrayList<>();
            for (int k = 0; k < mappingRegexp.size(); k++) {
                try {
                    mappingRegexpCompiled.add(Pattern.compile(mappingRegexp.get(k)));
                } catch (PatternSyntaxException ex) {
                    throw new ComparatorException(" Regexp = " + mappingRegexp.get(k)
                            .replace("\"", "`")
                            .replace("'", "`") + "; " + ex.getMessage(), 20003);
                }
            }
        }
        keyChildrenIgnore = configuration.getBooleanParameter(PARAMETER_NAME_KEY_CHILD_IGNORE, false);
        keyChildren = new HashMap<>();
        List<String> keyChildrenCfg = configuration.getParameters(PARAMETER_NAME_KEY_CHILD);
        if (keyChildrenCfg != null) {
            for (String item : keyChildrenCfg) {
                KeyChildDescription kd = new KeyChildDescription(item);
                List<KeyChildDescription> kdList;
                if (kd.valid) {
                    if (keyChildren.containsKey(kd.thisNode)) {
                        kdList = keyChildren.get(kd.thisNode);
                    } else {
                        kdList = new ArrayList<>();
                    }
                    kdList.add(kd);
                    keyChildren.put(kd.thisNode, kdList);
                }
            }
        }

        atpFormat = configuration.getParameter(PARAMETER_ATP_DIFFERENCES_FORMAT);
        if (StringUtils.isBlank(atpFormat)) {
            needAtpFormatting = false;
        } else {
            atpMacros = ScriptUtils.getMacros(atpFormat);
            needAtpFormatting = !atpMacros.isEmpty();
            if (needAtpFormatting) {
                atpMacrosValues = new HashMap<>();
                for (String key : atpMacros) {
                    atpMacrosValues.put(key, "");
                }
            }
        }
        List<String> atpAdditionalKeysRulesList
                = configuration.getParameters(PARAMETER_ATP_DIFFERENCES_ADDITIONAL_KEY);
        this.atpDiffAdditionalKeyRules = new ArrayList<>();
        if (atpAdditionalKeysRulesList != null) {
            for (String atpAdditionalKeysRulesListItem : atpAdditionalKeysRulesList) {
                AtpDiffAdditionalKeyRule atpDiffAdditionalKeyRule
                        = new AtpDiffAdditionalKeyRule(atpAdditionalKeysRulesListItem);
                this.atpDiffAdditionalKeyRules.add(atpDiffAdditionalKeyRule);
            }
        }
    }

    private Boolean verifyXpath(Detail differenceNodeDetail,
                                Document docEar,
                                List<XPathExpression> listXpaths,
                                String differenceNodeFullXpath) throws ComparatorException, XPathExpressionException {
        if (differenceNodeDetail.getXPath() == null) {
            return false;
        } else if (listXpaths.isEmpty()) {
            return false;
        }
        for (XPathExpression xp : listXpaths) {
            NodeList nodeList = (NodeList) xp.evaluate(docEar, XPathConstants.NODESET);
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (verifyRecursive(nodeList.item(i), differenceNodeDetail.getTarget(), differenceNodeFullXpath)) {
                    return true;
                }
            }
        }
        return false; // Node should NOT be excluded from comparison
    }

    private Boolean verifyRecursive(Node parent, Node diffNode, String differenceNodeFullXpath) {
        String currentFullXpath = XmlHelpers.getFullXPath(parent, true);
        if (differenceNodeFullXpath.startsWith(currentFullXpath)) {
            return true;
        }
        NodeList nodeList = parent.getChildNodes();
        for (int index = 0; index < nodeList.getLength(); index++) {
            short nodeType = nodeList.item(index).getNodeType();
            String nodeListItemXpath = XmlHelpers.getFullXPath(nodeList.item(index), true);
            if (differenceNodeFullXpath.startsWith(nodeListItemXpath)) {
                return true;
            } else {
                return verifyRecursive(nodeList.item(index), diffNode, differenceNodeFullXpath);
            }
        }
        return false; // Node should NOT be excluded from comparison
    }

    private List<DiffMessage> doExtraXpathRegexpCheckings(Document docER,
                                                          Document docAR,
                                                          List<XPathExpression> mappingXpathCompiled,
                                                          List<Pattern> mappingRegexpCompiled)
            throws ComparatorException {
        List<DiffMessage> resultList = new ArrayList<>();

        if (mappingXpathCompiled.isEmpty()
                || mappingRegexpCompiled.isEmpty()
                || mappingXpathCompiled.size() != mappingRegexpCompiled.size()) {
            return resultList;
        }

        for (int i = 0; i < mappingXpathCompiled.size(); i++) {
            resultList
                    .addAll(validate(docER, mappingRegexpCompiled.get(i), mappingXpathCompiled.get(i), true));
            resultList
                    .addAll(validate(docAR, mappingRegexpCompiled.get(i), mappingXpathCompiled.get(i), false));
        }
        return resultList;
    }

    private List<DiffMessage> validate(Document doc,
                                       Pattern regex,
                                       XPathExpression xpath,
                                       boolean isControl) throws ComparatorException {
        List<DiffMessage> resultList = new ArrayList<>();
        NodeList nodeList;

        try {
            nodeList = (NodeList) xpath.evaluate(doc, XPathConstants.NODESET);

            resultList.addAll(check(nodeList, regex, isControl, 0));
        } catch (XPathExpressionException e) {
            throw new ComparatorException("Failed to evaluate xpath", e);
        }
        return resultList;
    }

    private List<DiffMessage> check(NodeList nodeList, Pattern regexp, boolean isControl, int level) {
        List<DiffMessage> resultList = new ArrayList<>();
        for (int index = 0; index < nodeList.getLength(); index++) {
            final Node node = nodeList.item(index);
            if (node.getChildNodes().getLength() == 0 && (node.getNodeType() == Node.TEXT_NODE
                    || node.getNodeType() == Node.ATTRIBUTE_NODE)) {
                String nodeValue = node.getTextContent().trim();
                if (!regexp.matcher(nodeValue).matches()) {
                    DiffMessage diffMessage = new DiffMessage();
                    String nodeFullXpath = XmlHelpers.getFullXPath(node, true);
                    if (isControl) {
                        diffMessage.setExpected(nodeFullXpath);
                        diffMessage.setActual("");
                    } else {
                        diffMessage.setExpected("");
                        diffMessage.setActual(nodeFullXpath);
                    }
                    diffMessage.setResult(ResultType.MODIFIED);
                    diffMessage.setOrderId(++diffCounter);
                    diffMessage.setDescription("Xpath-Regexp checking is violated: " + nodeFullXpath);
                    resultList.add(diffMessage);
                }
            } else if (level == 0) {
                // Only one level of recursion. We are interested only in children, not in grandchildren
                resultList.addAll(check(node.getChildNodes(), regexp, isControl, 1));
            }
        }
        return resultList;
    }

    public class KeyChildDescription {

        public String thisNode;
        public String parentNode;
        public boolean negation;
        public boolean checkParent;
        public boolean valid;
        public List<String> chainChildren;

        public KeyChildDescription() {
            this.valid = false;
        }

        // Simple syntax:
        //      thisNode/chain-of-names-to-keyChild
        //          Meaning: If this node name equals 'thisNode',
        //          key value should be obtained from the last node in chain <chain-of-names-to-keyChild>
        //          Examples:   service/describingSpecificationKey/primaryKey
        //                      item/characteristic
        //                      key/type
        // Advanced syntax:
        //      [[!]parentNode/]thisNode//chain-of-names-to-keyChild
        //          Meaning: If this node name equals 'thisNode' 
        //                      AND parent node name [NOT] equals 'parentNode',
        //                      key value should be obtained from the last node in chain <chain-of-names-to-keyChild>
        //          Examples:   serviceOrderItems/item//service/describingSpecificationKey/primaryKey
        //                      !serviceOrderItems/item//characteristic
        //          Situation:
        //              XML contains 'item' nodes of two kinds. 
        //                  If parent is 'serviceOrderItems', key is 'service/describingSpecificationKey/primaryKey'
        //                  Otherwise, key is 'characteristic'        
        public KeyChildDescription(String config) {
            this.valid = false;
            if (!StringUtils.isBlank(config)) {
                int len = 2;
                boolean advanced = true;
                int pos = config.indexOf("//"); // Advanced syntax (see above)
                if (pos == -1) {
                    pos = config.indexOf("/"); // Simple syntax (see above)
                    len = 1;
                    advanced = false;
                }
                if (pos > 0) {
                    String key = config.substring(0, pos).trim();
                    String value = config.substring(pos + len).trim();
                    if (!StringUtils.isBlank(key) && !StringUtils.isBlank(value)) {
                        this.checkParent = false;
                        this.negation = false;
                        if (advanced) {
                            pos = key.indexOf("/");
                            if (pos > -1) {
                                this.parentNode = key.substring(0, pos).trim();
                                this.thisNode = key.substring(pos + 1).trim();
                                this.checkParent = true;
                                if (this.parentNode.startsWith("!")) {
                                    this.parentNode = this.parentNode.substring(1).trim();
                                    this.negation = true;
                                } else {
                                    this.negation = false;
                                }
                            } else {
                                this.thisNode = key;
                            }
                        } else {
                            this.thisNode = key;
                        }

                        String[] items = value.split("/");
                        this.chainChildren = new ArrayList<>();
                        for (int k = 0; k < items.length; k++) {
                            String s = items[k].trim();
                            if (!s.isEmpty()) {
                                this.chainChildren.add(s);
                            }
                        }

                        this.valid = (!StringUtils.isBlank(this.thisNode) && !this.chainChildren.isEmpty());
                        if (this.valid && this.checkParent) {
                            this.valid = (!StringUtils.isBlank(this.parentNode));
                        }
                    }
                }
            }
        }
    }

    public static class ChangeDiffResultRule {

        public String action; // "change", "ignore"
        public ResultType oldResult;
        public ResultType newResult;
        public List<String> xpaths;
        public List<XPathExpression> xpathsCompiled;

        public ChangeDiffResultRule() {
            action = "";
            oldResult = null;
            newResult = null;
            xpaths = new ArrayList();
            xpathsCompiled = new ArrayList();
        }

        public ChangeDiffResultRule(String parAction,
                                    String parOldResult,
                                    String parNewResult,
                                    List<String> parXPaths) throws ComparatorException {
            this();
            if (parXPaths.isEmpty()) {
                return;
            }
            switch (parAction.trim().toLowerCase()) {
                case "change":
                    if (!EnumUtils.isValidEnum(ResultType.class, parOldResult)
                            || !EnumUtils.isValidEnum(ResultType.class, parNewResult)) {
                        return;
                    }
                    action = "change";
                    oldResult = ResultType.valueOf(parOldResult);
                    newResult = ResultType.valueOf(parNewResult);
                    break;
                case "ignore":
                    action = "ignore";
                    break;
                default:
                    return;
            }
            setXPaths(parXPaths, XPathFactory.newInstance().newXPath());
        }

        public ChangeDiffResultRule(String parAction,
                                    ResultType parOldResult,
                                    ResultType parNewResult,
                                    List<String> parXPaths) throws ComparatorException {
            this();
            if (parXPaths.isEmpty()) {
                return;
            }
            switch (parAction.trim().toLowerCase()) {
                case "change":
                    action = "change";
                    oldResult = parOldResult;
                    newResult = parNewResult;
                    break;
                case "ignore":
                    action = "ignore";
                    break;
                default:
                    return;
            }
            setXPaths(parXPaths, XPathFactory.newInstance().newXPath());
        }

        // Rule string can be like
        //  SIMILAR=MODIFIED=//*[local-name()='productionPlan']
        //      or
        //  ignore=//*[local-name()='characteristicID']
        //      or
        //  //*[local-name()='Subscription-ID']
        // Thats why we should firstly determine the type of row.
        //  If row consists of xpath (not starts with "ignore=" or smth. like "SIMILAR=MODIFIED="
        //  then one of previous rows should start with "ignore=" or smth. like "SIMILAR=MODIFIED="
        public static ChangeDiffResultRule checkRule(String ruleItem) throws ComparatorException {
            int idx = ruleItem.indexOf("=");
            String ruleAction;
            String oldRes;
            String newRes;
            if (idx >= 1) {
                String part1 = ruleItem.substring(0, idx).trim().toLowerCase();
                String tail1 = ruleItem.substring(idx + 1);
                if (part1.equals("ignore")) {
                    ruleAction = part1;
                    return new ChangeDiffResultRule(ruleAction, "", "",
                            Arrays.asList(tail1.split("\r\n|\n|\r")));
                } else {
                    // change Result from old to new
                    ruleAction = "change";
                    oldRes = part1.toUpperCase();
                    int idx1 = tail1.indexOf("=");
                    if (idx1 >= 1) { // Otherwise - invalid rule syntax
                        newRes = tail1.substring(0, idx1).trim().toUpperCase();
                        String tail2 = tail1.substring(idx1 + 1);
                        if (EnumUtils.isValidEnum(ResultType.class, oldRes)
                                && EnumUtils.isValidEnum(ResultType.class, newRes)) {
                            return new ChangeDiffResultRule(ruleAction, oldRes, newRes,
                                    Arrays.asList(tail2.split("\r\n|\n|\r")));
                        }
                    }
                }
            }
            return null;
        }

        public void addXPaths(List<String> parXPaths) throws ComparatorException {
            this.setXPaths(parXPaths, XPathFactory.newInstance().newXPath());
        }

        private void setXPaths(List<String> parXPaths, XPath xpath) throws ComparatorException {
            for (String item : parXPaths) {
                if (item.isEmpty()) {
                    continue;
                }
                try {
                    xpathsCompiled.add(xpath.compile(item));
                    xpaths.add(item);
                } catch (XPathExpressionException ex) {
                    throw new ComparatorException(" Xpath = " + item.replace("\"", "`")
                            .replace("'", "`") + "; " + ex.getMessage(), 20004);
                }
            }
        }

    }

    private class CleanupAndParseResult {

        public String preparedXML = "";
        public String errorMessage = "";
    }

    private Boolean verifyNodeTagName(String sourceXPath, String targetName) throws ComparatorException {
        String xpathStr = XmlHelpers.clearXPath(sourceXPath);
        return XmlHelpers.xpathStringContainsTag(xpathStr, targetName);
    }

    private String getAtpDiffAdditionalKey(String targetXPath, Document docEar) throws ComparatorException {
        NodeList nodeList;
        try {
            nodeList = (NodeList) xpath.evaluate(targetXPath, docEar.getDocumentElement(), XPathConstants.NODESET);
            if (nodeList != null && nodeList.getLength() > 0) {
                return nodeList.item(0).getNodeValue();
            }

        } catch (XPathExpressionException e) {
            throw new ComparatorException("Failed to evaluate xpath", e);
        }
        return "";
    }

    private class AtpDiffAdditionalKeyRule {

        public static final String RULE_PARAMETER_REGEXP = "(.*?)=(.*?$)";

        String name;
        XPathExpression source;
        XPathExpression target;
        String sourceStr;
        String targetStr;

        public AtpDiffAdditionalKeyRule() {

        }

        public AtpDiffAdditionalKeyRule(String notParsedStructure) {
            this();
            try {
                this.parseStructure(notParsedStructure);
            } catch (XPathExpressionException ex) {
                // just ignore parse
            }
        }

        private void parseStructure(String structure) throws XPathExpressionException {
            String[] lines = structure.split("\n");
            Pattern p = Pattern.compile(RULE_PARAMETER_REGEXP);
            for (String line : lines) {
                Matcher m = p.matcher(line);
                if (m.find()) {
                    String key = m.group(1);
                    String value = m.group(2);
                    switch (key.toLowerCase()) {
                        case "name":
                            this.name = value;
                            break;
                        case "source":
                            this.source = this.getXPathExpression(value);
                            this.sourceStr = value;
                            break;
                        case "target":
                            this.target = this.getXPathExpression(value);
                            this.targetStr = value;
                            break;
                        default:
                            log.warn("Unknown structure parameter key: {}", key);
                            break;
                    }
                }
            }
        }

        private XPathExpression getXPathExpression(String xpath) throws XPathExpressionException {
            return XPathFactory.newInstance().newXPath().compile(xpath);
        }

        public String buildTarget(String sourceXpath) throws XPathExpressionException {
            String resultXPath = XmlHelpers.clearXPath(sourceXpath);
            if (!sourceXpath.endsWith("/")) {
                resultXPath += "/";
            }
            resultXPath += this.targetStr;
            return resultXPath;
        }
    }

    private Map<String, String> fillAtpValues(Difference difference, Map<String, String> atpMap) {
        String val;
        String propName;
        Detail details;
        for (Map.Entry<String, String> item : atpMap.entrySet()) {
            try {
                propName = (String) item.getKey();
                if (propName.equals("ERXPATH") || propName.equals("ERTAGVALUE")
                        || propName.equals("ERTAGNAME")) {
                    details = difference.getComparison().getControlDetails();
                } else if (propName.equals("ARXPATH") || propName.equals("ARTAGVALUE")
                        || propName.equals("ARTAGNAME")) {
                    details = difference.getComparison().getTestDetails();
                } else {
                    item.setValue("");
                    continue;
                }
                if (details == null) {
                    item.setValue("");
                    continue;
                }
                switch (propName) {
                    case "ERXPATH":
                    case "ARXPATH":
                        val = details.getXPath();
                        break;
                    case "ERTAGVALUE":
                    case "ARTAGVALUE":
                        val = details.getTarget().getTextContent(); //val = getTagValue(details.getValue());
                        break;
                    case "ERTAGNAME":
                    case "ARTAGNAME":
                        val = details.getTarget().getNodeName();    //val = getTagName(details.getXPath());       
                        break;
                    default:
                        val = "";
                }
                if (val == null) {
                    val = "";
                }
            } catch (Exception ignore) {
                val = "";
            }
            item.setValue(val);
        }
        return atpMap;
    }
}
