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

package org.qubership.automation.pc.core.utils;

import java.util.Comparator;
import java.util.Objects;

import org.qubership.automation.pc.compareresult.DiffMessage;

public class XmlDifferenceComparator implements Comparator<DiffMessage> {

    /**
     * Compare 2 difference messages by expected paths.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return negative if o1 path is higher in the document,
     *         positive if o1 path is lower,
     *         0 if paths are the same.
     */
    @Override
    public int compare(DiffMessage o1, DiffMessage o2) {
        String path1 = o1.getExpected();
        String path2 = o2.getExpected();
        if (path1.startsWith("parent:")) {
            path1 = path1.substring(7);
        }
        if (path2.startsWith("parent:")) {
            path2 = path2.substring(7);
        }
        String[] paths1 = path1.split("/");
        String[] paths2 = path2.split("/");

        for (int i = 0; i < paths1.length && i < paths2.length; i++) {
            if (paths1[i].equals(paths2[i])) {
                continue;
            }
            PathNode node1 = new PathNode(paths1[i]);
            PathNode node2 = new PathNode(paths2[i]);

            if (!node1.name.equals(node2.name)) {
                return node1.name.compareTo(node2.name);
            }
            return compareByPositionInArray(node1, node2);
        }

        if (paths1.length > paths2.length) {
            return 1;
        } else if (paths1.length < paths2.length) {
            return -1;
        } else {
            return 0;
        }
    }

    private static int compareByPositionInArray(PathNode node1, PathNode node2) {
        if (Objects.nonNull(node1.positionInArray) && Objects.nonNull(node2.positionInArray)) {
            return node1.positionInArray.compareTo(node2.positionInArray);
        } else if (Objects.nonNull(node1.positionInArray)) {
            return 1;
        } else {
            return -1;
        }
    }

    private static class PathNode {
        String name;
        Integer positionInArray;

        public PathNode(String name) {
            this.name = name;
            if (name.endsWith("]")) {
                int idx = name.lastIndexOf("[");
                if (idx != -1) {
                    String positionStr = name.substring(idx + 1, name.length() - 1);
                    try {
                        positionInArray = Integer.parseInt(positionStr);
                        this.name = name.substring(0, idx);
                    } catch (IllegalArgumentException ex) {
                        positionInArray = null;
                        this.name = name;
                    }
                }
            }
        }

    }
}
