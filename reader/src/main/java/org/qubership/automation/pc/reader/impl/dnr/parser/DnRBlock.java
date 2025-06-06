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

package org.qubership.automation.pc.reader.impl.dnr.parser;

import java.util.List;

/**
 * Represents a single block of parsed data in a Device and Routing (DnR) file.
 *
 * <p>
 * Each block contains a line of text and may be part of a hierarchical structure,
 * where a block can have a parent and multiple children. This structure is used
 * to model nested or indented configurations, commonly found in network configuration files.
 * </p>
 *
 * <p>This class is typically used by parsers such as {@code CiscoTxtFileParser} to
 * build a tree representation of the input text.</p>
 *
 * @see CiscoTxtFileParser
 * @see org.qubership.automation.pc.reader.impl.DnRReader
 */
public class DnRBlock {

    private String line;
    private DnRBlock parent;
    private List<DnRBlock> children;

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    public DnRBlock getParent() {
        return parent;
    }

    public void setParent(DnRBlock parent) {
        this.parent = parent;
    }

    public List<DnRBlock> getChildren() {
        return children;
    }

    public void setChildren(List<DnRBlock> children) {
        this.children = children;
    }

}
