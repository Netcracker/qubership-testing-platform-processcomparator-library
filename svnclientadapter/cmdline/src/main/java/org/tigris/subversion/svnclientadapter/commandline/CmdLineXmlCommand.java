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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class CmdLineXmlCommand {

    /**
     * Searches the children of the given parent node and returns the first element with the specified name.
     * </p>
     * This method is not a full XPath implementation, but rather a simple traversal of direct child elements.
     *
     * @param parent       the parent node whose children will be searched
     * @param elementName  the name of the element to find
     * @return the first {@link Element} with the specified name, or {@code null} if none is found
     */
    protected static Element getFirstNamedElement(Node parent, String elementName) {
        if (parent == null) {
            return null;
        }
        return findNamedElementSibling(parent.getFirstChild(), elementName);
    }

    protected static Element getNextNamedElement(Node foundNode, String elementName) {
        if (foundNode == null) {
            return null;
        }
        return findNamedElementSibling(foundNode.getNextSibling(), elementName);
    }

    private static Element findNamedElementSibling(Node sibling, String elementName) {
        if (sibling == null) {
            return null;
        }
        while (sibling != null) {
            if (sibling.getNodeType() == Node.ELEMENT_NODE && sibling.getNodeName().equals(elementName)) {
                return (Element) sibling;
            }
            sibling = sibling.getNextSibling();
        }
        return null;
    }

}
