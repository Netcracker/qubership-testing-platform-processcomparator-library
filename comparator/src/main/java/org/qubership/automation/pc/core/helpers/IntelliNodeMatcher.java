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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.pc.comparator.impl.XmlComparator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlunit.diff.ByNameAndTextRecSelector;
import org.xmlunit.diff.DefaultNodeMatcher.DefaultNodeTypeMatcher;
import org.xmlunit.diff.DefaultNodeMatcher.NodeTypeMatcher;
import org.xmlunit.diff.ElementSelector;
import org.xmlunit.diff.ElementSelectors;
import org.xmlunit.diff.NodeMatcher;
import org.xmlunit.util.Linqy;

/**
 * A custom implementation of the {@link NodeMatcher} interface that performs intelligent
 * matching between XML nodes based on a configurable sequence of {@link ElementSelector}s
 * and a {@link NodeTypeMatcher}.
 * </p>
 * This matcher is capable of:
 * <ul>
 *   <li>Handling element comparison strategies such as matching by name, text, or custom logic.</li>
 *   <li>Tracking and using "key children" metadata to influence matching decisions.</li>
 *   <li>Allowing unmatched nodes to be revisited by fallback selectors.</li>
 * </ul>
 * It supports advanced matching for XML diff/merge tools and is particularly suitable
 * for comparing complex XML documents where structural and semantic context matters.
 * </p>
 * The matcher is designed to be extensible and reusable in XML comparison frameworks.
 */
public class IntelliNodeMatcher implements NodeMatcher {
    private static final short TEXT = Node.TEXT_NODE;
    private static final short CDATA = Node.CDATA_SECTION_NODE;
    private final ElementSelector[] elementSelectors;
    private final NodeTypeMatcher nodeTypeMatcher;
    
    private ElementSelector currentElementSelector;

    private Map<String, List<XmlComparator.KeyChildDescription>> keyChildren = new HashMap<>();
    private boolean keyChildrenIgnore = false;

    /**
     * Creates a matcher using {@link ElementSelectors#Default} and
     * {@link DefaultNodeTypeMatcher}.
     */
    public IntelliNodeMatcher() {
        this(ElementSelectors.Default);
    }

    /**
     * Creates a matcher using the given {@link ElementSelector}s and
     * {@link DefaultNodeTypeMatcher}.
     */
    public IntelliNodeMatcher(ElementSelector... es) {
        this(new DefaultNodeTypeMatcher(), es);
    }

    /**
     * Default Constructor.
     */
    public IntelliNodeMatcher(Map<String,
            List<XmlComparator.KeyChildDescription>> keyChildren, boolean keyChildrenIgnore) {
        this(!keyChildren.isEmpty()
                        ? new AdvancedByNameAndTextRecSelector(keyChildren) : new ByNameAndTextRecSelector(),
                ElementSelectors.byNameAndText,
                ElementSelectors.byName);
        this.keyChildren = keyChildren;
        this.keyChildrenIgnore = keyChildrenIgnore;
    }

    /**
     * Creates a matcher using the given {@link ElementSelector}s and
     * {@link NodeTypeMatcher}.
     *
     * <p>The {@link ElementSelector}s are consulted in order so that
     * the second {@link ElementSelector} only gets to match the nodes
     * that the first one couldn't match to any test nodes ate all and
     * so on.</p>
     */
    public IntelliNodeMatcher(NodeTypeMatcher ntm, ElementSelector... es) {
        nodeTypeMatcher = ntm;
        elementSelectors = es;
    }

    @Override
    public Iterable<Map.Entry<Node, Node>> match(Iterable<Node> controlNodes, Iterable<Node> testNodes) {
        Map<Node, Node> matches = new LinkedHashMap<Node, Node>();
        List<Node> controlList = Linqy.asList(controlNodes);
        List<Node> testList = Linqy.asList(testNodes);
        final int testSize = testList.size();
        Set<Integer> unmatchedTestIndexes = new HashSet<Integer>();
        for (int i = 0; i < testSize; i++) {
            unmatchedTestIndexes.add(Integer.valueOf(i));
        }
        final int controlSize = controlList.size();
        
        Match lastMatch = new Match(null, -1);
        Map<Node,XmlComparator.KeyChildDescription> keyChildrenNodes = new HashMap<>();
        for (ElementSelector e : elementSelectors) {
            currentElementSelector = e;
            for (int i = 0; i < controlSize; i++) {
                Node control = controlList.get(i);
                if (matches.containsKey(control) || (keyChildrenNodes.containsKey(control) && keyChildrenIgnore)) {
                    continue;
                }
                Match testMatch = findMatchingNode(control, testList, lastMatch.index, unmatchedTestIndexes);
                if (testMatch != null) {
                    unmatchedTestIndexes.remove(testMatch.index);
                    matches.put(control, testMatch.node);
                } else if (!keyChildren.isEmpty() && (control instanceof Element)) {
                    XmlComparator.KeyChildDescription keyChildDescription
                            = AdvancedByNameAndTextRecSelector.checkForKeyChild((Element) control, keyChildren);
                    if (keyChildDescription != null) {
                        keyChildrenNodes.put(control,keyChildDescription);
                    }
                }
            }
        }
        return matches.entrySet();
    }

    private Match findMatchingNode(final Node searchFor,
                                   final List<Node> searchIn,
                                   final int indexOfLastMatch,
                                   final Set<Integer> availableIndexes) {
        final int searchSize = searchIn.size();
        Match m = searchIn(searchFor, searchIn, availableIndexes, indexOfLastMatch + 1, searchSize);
        return m != null ? m : searchIn(searchFor, searchIn, availableIndexes, 0, indexOfLastMatch);
    }

    private Match searchIn(final Node searchFor,
                           final List<Node> searchIn,
                           final Set<Integer> availableIndexes,
                           final int fromInclusive, final int toExclusive) {
        /*
        for (ElementSelector e : elementSelectors) {
            Match m = searchIn(searchFor, searchIn, availableIndexes, fromInclusive, toExclusive, e);
            if (m != null) {
                return m;
            }
        }*/
        Match m = searchIn(searchFor, searchIn, availableIndexes, fromInclusive, toExclusive, currentElementSelector);
        if (m != null) {
            return m;
        }
        return null;
    }

    private Match searchIn(final Node searchFor,
                           final List<Node> searchIn,
                           final Set<Integer> availableIndexes,
                           final int fromInclusive, final int toExclusive,
                           final ElementSelector e) {
        for (int i = fromInclusive; i < toExclusive; i++) {
            if (!availableIndexes.contains(Integer.valueOf(i))) {
                continue;
            }
            if (nodesMatch(searchFor, searchIn.get(i), e)) {
                return new Match(searchIn.get(i), i);
            }
        }
        return null;
    }

    private boolean nodesMatch(final Node n1, final Node n2, final ElementSelector elementSelector) {
        if (n1 instanceof Element && n2 instanceof Element) {
            return elementSelector.canBeCompared((Element) n1, (Element) n2);
        }
        return nodeTypeMatcher.canBeCompared(n1.getNodeType(), n2.getNodeType());
    }

    private static class Match {
        private final Node node;
        private final int index;

        private Match(Node match, int index) {
            this.node = match;
            this.index = index;
        }
    }
}
