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

package org.qubership.automation.pc.models;

import java.util.ArrayList;
import java.util.List;

import org.qubership.automation.pc.data.DataContentConverter;

/**
 * Represents a node in a hierarchical highlighting structure, used for visual representation
 * and comparison of structured data (e.g., XML or JSON) with optional encoding support.
 *
 * <p>
 * Each node may contain a value, child nodes, and metadata such as row number, display status,
 * and validation information. Nodes can be encoded/decoded and flattened into plain values.
 * </p>
 */
public class HighlighterNode {

    private List<HighlighterNode> children = new ArrayList<HighlighterNode>();
    private transient HighlighterNode parent;
    private int rowNumber;
    private int linkedRow = -1;
    private Boolean isDisplaied = true;
    private String value;
    private boolean isValueEncoded = false;
    private String validationStatus;
    private String originalXpath;
    private Boolean isPlain = false;

    public List<HighlighterNode> getChildren() {
        return children;
    }

    public void setChildren(List<HighlighterNode> children) {
        this.children = children;
    }

    public HighlighterNode getParent() {
        return parent;
    }

    public void setParent(HighlighterNode parent) {
        this.parent = parent;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getLinkedRow() {
        return linkedRow;
    }

    public void setLinkedRow(int linkedRow) {
        this.linkedRow = linkedRow;
    }

    public Boolean getIsDisplaied() {
        return isDisplaied;
    }

    public void setIsDisplaied(Boolean isDisplaied) {
        this.isDisplaied = isDisplaied;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValidationStatus() {
        return validationStatus;
    }

    public void setValidationStatus(String validationStatus) {
        this.validationStatus = validationStatus;
    }

    public String getOriginalXpath() {
        return originalXpath;
    }

    public void setOriginalXpath(String originalXpath) {
        this.originalXpath = originalXpath;
    }

    public Boolean getIsPlain() {
        return isPlain;
    }

    public void setIsPlain(Boolean isPlain) {
        this.isPlain = isPlain;
    }

    public boolean isValueEncoded() {
        return isValueEncoded;
    }

    public void setValueEncoded(boolean valueEncoded) {
        isValueEncoded = valueEncoded;
    }

    /**
     * Composes all children nodes' values into parent node and clears the children array.
     * Encodes result into Base64 if needed.
     *
     * @param encodeMode whether to encode the result into Base64 format
     */
    public void convertToPlain(boolean encodeMode) {
        String newValue = getComposedValue(encodeMode);
        this.value = encodeMode ? DataContentConverter.fromString(newValue) : newValue;
        this.isPlain = true;
        this.children.clear();
    }

    public String getComposedValue(boolean encodeMode) {
        if (this.isPlain) {
            return encodeMode ? DataContentConverter.toString(this.getValue()) : this.getValue();
        } else {
            return getComposedValue(children);
        }
    }

    public String getComposedValue(List<HighlighterNode> nodes) {
        return getComposedValue(nodes, 0);
    }

    public String getComposedValue(List<HighlighterNode> nodes, int deep) {
        String resultString = "";
        String resWithSpacing;
        for (HighlighterNode node : nodes) {
            resWithSpacing = "<div style=\"margin-left: " + deep * 15 + "px\">"
                    + (node.isValueEncoded ? DataContentConverter.toString(node.getValue()) : node.getValue())
                    + "</div>";
            resultString += resWithSpacing;
            if (!node.isPlain) {
                resultString += getComposedValue(node.getChildren(), deep + 1);
            }
        }
        return resultString;
    }

    public void encodeNode() {
        if (this.isPlain) {
            this.value = DataContentConverter.fromString(this.value);
            this.isValueEncoded = true;
        } else {
            this.children.forEach((childNode) -> {
                childNode.encodeValueNodes();
            });
        }
    }

    private void encodeValueNodes() {
        this.value = DataContentConverter.fromString(this.value);
        this.isValueEncoded = true;
        if (this.children.size() > 0) {
            this.children.forEach((childNode) -> {
                childNode.encodeValueNodes();
            });
        }
    }
}
