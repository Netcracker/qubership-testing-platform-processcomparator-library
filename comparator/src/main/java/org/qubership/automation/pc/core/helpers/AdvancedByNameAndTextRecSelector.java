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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.comparator.impl.XmlComparator.KeyChildDescription;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xmlunit.diff.ElementSelector;

/**
 *     Source: xmlunit-core-2.3.0-sources.jar;
 *     org/xmlunit/diff/ByNameAndTextRecSelector.java Causes: 1) There is no 100%
 *     safe way to set nodes' correspondence if comparator doesn't have outer
 *     information about 'key'-nodes. We should have outer rules which will instruct
 *     comparator how it must identify correspondent nodes. 2) 1st implemented as
 *     '\org\quberhsip\automation\pc\core\helpers\XmlRecursiveElementNameAndTextQualifier.java'
 *     - advanced ElementQualifier which (1) cleaned from errors found in XMLUnit's
 *     RecursiveElementNameAndTextQualifier (2) could be configured with the
 *     'keyChild'-rules
 *     </p>
 *     Example of XML structure which will potentially be compared incorrectly:
 *     <node1><item><value>1111</value><key>AA</key></item>
 *     <item><value>2222</value><key>BB</key></item>
 *     <item><value>3333</value><key>CC</key></item> ... </node1>
 */
public class AdvancedByNameAndTextRecSelector implements ElementSelector {

    private Map<String, List<KeyChildDescription>> keyChildren = new HashMap<>();
    private boolean keyChildrenIsSet = false;
    private static final Pattern NON_UNIQUE_TAG_KEY_REGEXP = Pattern.compile("(.*)\\[(\\d+)\\]");

    public AdvancedByNameAndTextRecSelector() {
    }

    public AdvancedByNameAndTextRecSelector(Map<String, List<KeyChildDescription>> keyChildren) {
        if (!keyChildren.isEmpty()) {
            this.keyChildren.putAll(keyChildren);
            this.keyChildrenIsSet = true;
        }
    }

    @Override
    public boolean canBeCompared(Element controlElement, Element testElement) {
        if (!keyChildren.containsKey(controlElement.getLocalName())
                || !controlElement.getLocalName().equals(testElement.getLocalName())) {
            return false;
        }

        NodeList controlChildren = controlElement.getChildNodes();
        NodeList testChildren = testElement.getChildNodes();
        final int controlLen = controlChildren.getLength();
        final int testLen = testChildren.getLength();
        int controlIndex;
        int testIndex;

        // 'keyChild'-rule processing - Start
        if (keyChildrenIsSet) {
            // LocalNames of currentControl & currentTest are equal at this point
            String controlName = controlElement.getLocalName();
            if (keyChildren.containsKey(controlName)) {
                KeyChildDescription kd = checkForKeyChild(controlElement, keyChildren);


                if (kd != null) {
                    Boolean compareByKeys = compareByKeyChildren(
                            kd.chainChildren,
                            controlElement,
                            testElement,
                            controlChildren,
                            testChildren,
                            controlLen,
                            testLen
                    );
                    if (compareByKeys != null) {
                        // If decision (comparable nodes or not) is made - return it!
                        // Otherwise - continue common recursive algorithm...
                        return compareByKeys;
                    }
                }
            }
        }
        // 'keyChild'-rule processing - End

        for (controlIndex = testIndex = 0; controlIndex < controlLen && testIndex < testLen; ) {
            // find next non-text child nodes
            Map.Entry<Integer, Node> control = findNonText(controlChildren, controlIndex, controlLen);
            controlIndex = control.getKey();
            Node c = control.getValue();
            if (isText(c)) {
                break;
            }
            Map.Entry<Integer, Node> test = findNonText(testChildren, testIndex, testLen);
            testIndex = test.getKey();
            Node t = test.getValue();
            if (isText(t)) {
                break;
            }

            // different types of children make elements non-comparable
            if (c.getNodeType() != t.getNodeType()) {
                return false;
            }
            // recurse for child elements
            if (c instanceof Element && !canBeCompared((Element) c, (Element) t)) {
                return false;
            }
            controlIndex++;
            testIndex++;
        }

        // child lists exhausted?
        if (controlIndex < controlLen) {
            Map.Entry<Integer, Node> p = findNonText(controlChildren, controlIndex, controlLen);
            controlIndex = p.getKey();
            // some non-Text children remained
            if (controlIndex < controlLen) {
                return false;
            }
        }
        if (testIndex < testLen) {
            Map.Entry<Integer, Node> p = findNonText(testChildren, testIndex, testLen);
            testIndex = p.getKey();
            // some non-Text children remained
            if (testIndex < testLen) {
                return false;
            }
        }
        return true;
    }

    public static KeyChildDescription checkForKeyChild(Element controlElement,
                                                       Map<String, List<KeyChildDescription>> keyChildren) {
        // LocalNames of currentControl & currentTest are equal at this point
        String controlName = controlElement.getLocalName();
        if (keyChildren.containsKey(controlName)) {
            List<KeyChildDescription> keyChildDescriptionList = keyChildren.get(controlName);
            KeyChildDescription kd = null;
            String parentName = null;
            for (int idx = 0; idx < keyChildDescriptionList.size(); idx++) {
                kd = keyChildDescriptionList.get(idx);
                if (kd.checkParent) {
                    if (StringUtils.isBlank(parentName)) {
                        parentName = controlElement.getParentNode().getLocalName();
                    }
                    if (!StringUtils.isBlank(parentName) && kd.negation != parentName.equals(kd.parentNode)) {
                        break;
                    }
                } else {
                    break;
                }
                kd = null;
            }

            return kd;
        }
        return null;
    }

    private static Map.Entry<Integer, Node> findNonText(NodeList nl, int current, int len) {
        Node n = nl.item(current);
        while (isText(n) && ++current < len) {
            n = nl.item(current);
        }
        return new AbstractMap.SimpleImmutableEntry<Integer, Node>(current, n);
    }

    private static boolean isText(Node n) {
        return n instanceof Text || n instanceof CDATASection;
    }

    private static Boolean compareByKeyChildren(List<String> keyChildChain,
                                                Element controlElement, Element testElement,
                                                NodeList controlChildren, NodeList testChildren,
                                                int controlLen, int testLen) {
        Map<String, String> controls = new HashMap<>();
        Map<String, String> tests = new HashMap<>();

        NodeSearchResults searchResults = new NodeSearchResults();

        /* The logic of comparison is (for example, if 'keyChild' is attribute):
            1. If both nodes have key attribute:
                1.1. If values are equal ==> nodes correspond to each other ==> return true
                1.2. Otherwise ==> nodes DON'T correspond to each other ==> return false
            2. If both nodes DON'T have key attribute:
                - They can OR cann't correspond to each other ==> we should continue recursive algorithm
                ==> DO NOT return from this method
            3. One node has key attribute BUT the other node DOESN'T have key attribute:
                - They definitely DON'T correspond to each other ==> return false
         */
        if (keyChildChain.get(0).startsWith("@")) { // key node - attribute node
            searchResults = searchNodesByAttribute(controlElement, testElement, keyChildChain.get(0).substring(1));
        } else {
            // Mode for cases when er set as specific
            for (String keyNode : keyChildChain.get(0).split("&&")) {
                Node controlKeyNode = null;
                Node testKeyNode = null;
                Matcher m = NON_UNIQUE_TAG_KEY_REGEXP.matcher(keyNode);
                if (m.find()) {
                    // Search for Non-Unique Tag by index
                    controlKeyNode = getKeyNode(m.group(1), controlChildren, controlLen, Integer.valueOf(m.group(2)));
                    testKeyNode = getKeyNode(m.group(1), testChildren, testLen, controlKeyNode.getTextContent().trim());
                } else {
                    // Search as usual
                    controlKeyNode = getKeyNode(keyNode, controlChildren, controlLen);
                    testKeyNode = getKeyNode(keyNode, testChildren, testLen);
                }
                if (controlKeyNode != null) {
                    searchResults.controls.put(keyNode, controlKeyNode.getTextContent().trim());
                }
                if (testKeyNode != null) {
                    searchResults.tests.put(keyNode, testKeyNode.getTextContent().trim());
                }
            }
        }
        return searchResults.doResultsMatch();
    }

    private static NodeSearchResults searchNodesByAttribute(Element controlElement,
                                                            Element testElement,
                                                            String keyAttributeName) {
        NodeSearchResults results = new NodeSearchResults();
        String controlKeyValue = getTrimmedAttributeValue(controlElement, keyAttributeName);
        String testKeyValue = getTrimmedAttributeValue(testElement, keyAttributeName);
        if (controlKeyValue != null) {
            results.controls.put(keyAttributeName, controlKeyValue);
        }
        if (testKeyValue != null) {
            results.tests.put(keyAttributeName, testKeyValue);
        }
        return results;
    }

    private static Node getKeyNode(String thisKey, NodeList children, int len) {
        return getKeyNode(thisKey, children, len, null, null);
    }

    private static Node getKeyNode(String thisKey, NodeList children, int len, Integer repetitionTarget) {
        return getKeyNode(thisKey, children, len, repetitionTarget, null);
    }

    private static Node getKeyNode(String thisKey, NodeList children, int len, String searchByValue) {
        return getKeyNode(thisKey, children, len, null, searchByValue);
    }

    private static Node getKeyNode(String thisKey,
                                   NodeList children,
                                   int len,
                                   Integer repetitionTarget,
                                   String searchByValue) {
        if (StringUtils.isBlank(thisKey)
                || children == null
                || len == 0
                || (repetitionTarget != null
                && repetitionTarget > len)) {
            return null;
        }
        int tagsWithSameNameCount = 0;
        for (int i = 0; i < len; i++) {
            Node currChild = children.item(i);
            if (currChild.getLocalName() == null) {
                throw new RuntimeException("XML document is broken. Item [ "
                        + currChild.getNodeValue() + " ] doesn't contain tag name");
            }
            if (currChild.getLocalName().equals(thisKey)) {
                // Check for hardcoded element count
                if (repetitionTarget == null || tagsWithSameNameCount == repetitionTarget) {
                    // Check if node value is equal for that we search for
                    if (StringUtils.isBlank(searchByValue) || currChild.getTextContent().trim().equals(searchByValue)) {
                        return currChild;
                    }
                }
                tagsWithSameNameCount++;
            }
        }
        return null;
    }

    private static String getTrimmedTextValue(Node thisNode, List<String> keyChildChain, int idx) {
        if (idx >= keyChildChain.size()) {
            String value = thisNode.getTextContent();
            return (StringUtils.isBlank(value)) ? "" : value.trim();
        } else {
            if (keyChildChain.get(idx).startsWith("@")) {
                return getTrimmedAttributeValue(thisNode, keyChildChain.get(idx).substring(1));
            } else {
                NodeList children = thisNode.getChildNodes();
                Node child = getKeyNode(keyChildChain.get(idx), children, children.getLength());
                if (child != null) {
                    return getTrimmedTextValue(child, keyChildChain, idx + 1); // Recursive algorithm
                }
            }
            return null;
        }
    }

    private static String getTrimmedAttributeValue(Node thisNode, String thisKey) {
        NamedNodeMap attrs = thisNode.getAttributes();
        if (attrs != null) {
            Node attrNode = attrs.getNamedItem(thisKey);
            if (attrNode != null) {
                return attrNode.getNodeValue().trim();
            }
        }
        return null;
    }

    public static class NodeSearchResults {
        public Map<String, String> controls = new HashMap<>();
        public Map<String, String> tests = new HashMap<>();

        public Boolean doResultsMatch() {
            // This condition mimics older functionality (null returned if no match was found)
            if (controls.isEmpty() && tests.isEmpty()) {
                return null;
            }
            return controls.equals(tests);
        }
    }

}
