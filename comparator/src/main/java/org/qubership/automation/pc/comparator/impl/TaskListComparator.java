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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.helpers.TextHelpers;

/**
 * A comparator implementation for validating and comparing task lists line by line.
 * This comparator supports different comparison modes, including:
 * <ul>
 *   <li>Hash-based equality for exact string matches.</li>
 *   <li>Regular expression matching for flexible validation.</li>
 *   <li>Support for one-to-one or multiple expected-to-actual task matches.</li>
 *   <li>Order checking with detection of broken step indexes.</li>
 * </ul>
 * The comparison behavior is controlled by configuration parameters:
 * <ul>
 *   <li>{@code ignoreIdentical} – skips identical matches if enabled.</li>
 *   <li>{@code TaskAsRegexp} – interprets expected tasks as regular expressions.</li>
 *   <li>{@code multipleMatch} – allows multiple expected entries to match a single actual entry.</li>
 * </ul>
 * The comparator returns a list of {@link DiffMessage} objects describing all found differences.
 */
public class TaskListComparator extends AbstractComparator {

    public static final String IGNORE_IDENTICAL = "ignoreIdentical";
    public static final String TASK_AS_REGEXP = "TaskAsRegexp";
    public static final String MULTIPLE_MATCH = "multipleMatch";

    public static boolean ignoreIdentical;
    public static boolean taskAsRegexp;
    public static boolean multipleMatch;

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        getConfigurationParameters(configuration);
        return compareTaskLists(er, ar);
    }

    /**
     * Compares two lists of strings by existence of expected results.
     * Returns {@code List<DiffMessage>} - array of detailed diffs.
     */
    private List<DiffMessage> compareTaskLists(String er, String ar) {
        int diffCounter = 0;
        List<DiffMessage> differences = new ArrayList<>();

        List<String> erSplit = TextHelpers.stringToList(er);
        List<String> arSplit = TextHelpers.stringToList(ar);
        List<Integer> arHash = new ArrayList<>();
        List<Integer> erHash = new ArrayList<>();
        if (!taskAsRegexp) {
            fillHash(erSplit, erHash);
            fillHash(arSplit, arHash);
        }

        if (multipleMatch) {
            return taskAsRegexp
                    ? checkMultipleMatch(erSplit, arSplit)
                    : checkMultipleEquals(erHash, arHash);
        }
        boolean equalSize = (erSplit.size() == arSplit.size());

        LinkedHashSet<Integer> existingAr = new LinkedHashSet<>();
        LinkedHashSet<Integer> foundER = new LinkedHashSet<>();
        for (int i = 0; i < erSplit.size(); i++) {
            boolean matchFound = false;
            for (int j = 0; j < arSplit.size(); j++) {
                if (!existingAr.contains(j)) {
                    boolean isMatch;
                    if (taskAsRegexp) {
                        isMatch = arSplit.get(j).matches(erSplit.get(i)); // match by regexp
                    } else {
                        isMatch = erHash.get(i).equals(arHash.get(j)); // match by hash
                    }
                    if (isMatch) {
                        existingAr.add(j);
                        foundER.add(i);
                        matchFound = true;
                        break;
                    }
                }
            }
            if (!matchFound) {
                differences.add(new DiffMessage(++diffCounter, String.format("%s-%s", i, i), "",
                        (equalSize) ? ResultType.MODIFIED : ResultType.MISSED,
                        "er task# " + i + "is missed."));
            }
        }
        for (int i = 0; i < arSplit.size(); i++) {
            if (!existingAr.contains(i)) {
                differences.add(new DiffMessage(++diffCounter, "", String.format("%s-%s", i, i),
                        (equalSize) ? ResultType.MODIFIED : ResultType.EXTRA,
                        "ar task# " + i + "is extra."));
            }
        }

        if (foundER.size() > 1) {
            List<Integer> arLst = new ArrayList<>(existingAr);
            List<Integer> erLst = new ArrayList<>(foundER);
            int prevAR = arLst.get(0);
            for (int i = 1; i < arLst.size(); i++) {
                int thisAR = arLst.get(i);
                int thisER = erLst.get(i);
                if (thisAR < prevAR) {
                    differences.add(
                            new DiffMessage(
                                    ++diffCounter,
                                    String.format("%s-%s", thisER, thisER),
                                    String.format("%s-%s", thisAR, thisAR),
                            ResultType.BROKEN_STEP_INDEX,
                            "There is broken order of tasks (er task# "
                                    + thisER + ", ar task# " + thisAR + ")"));
                }
                prevAR = thisAR;
            }
        }

        return differences;
    }

    private void fillHash(List<String> arSplit, List<Integer> arHash) {
        for (String s : arSplit) {
            arHash.add(s.hashCode());
        }
    }

    private List<DiffMessage> checkMultipleMatch(List<String> erSplit, List<String> arSplit) {
        int diffCounter = 0;
        List<DiffMessage> differences = new ArrayList<>();
        for (int j = 0; j < arSplit.size(); j++) {
            boolean matchFound = false;
            String ar = arSplit.get(j);
            for (String er : erSplit) {
                // match by regexp
                if (ar.matches(er)) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                differences.add(new DiffMessage(++diffCounter, "", String.format("%s-%s", j, j),
                        ResultType.MODIFIED, "ar task# " + j + " doesn't match any of er."));
            }
        }
        return differences;
    }

    private List<DiffMessage> checkMultipleEquals(List<Integer> erHash, List<Integer> arHash) {
        int diffCounter = 0;
        List<DiffMessage> differences = new ArrayList<>();
        for (int j = 0; j < arHash.size(); j++) {
            boolean matchFound = false;
            Integer ar = arHash.get(j);
            for (Integer er : erHash) {
                if (ar.equals(er)) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                differences.add(new DiffMessage(++diffCounter, "", String.format("%s-%s", j, j),
                        ResultType.MODIFIED, "ar task# " + j + " isn't equal any of er."));
            }
        }
        return differences;
    }

    private void getConfigurationParameters(Parameters configuration) {
        ignoreIdentical = configuration.getBooleanParameter(IGNORE_IDENTICAL, false);
        taskAsRegexp = configuration.getBooleanParameter(TASK_AS_REGEXP, false);
        multipleMatch = configuration.getBooleanParameter(MULTIPLE_MATCH, false);
    }
}
