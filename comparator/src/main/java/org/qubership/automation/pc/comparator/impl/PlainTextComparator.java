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
import java.util.List;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.helpers.TextHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * Comparator implementation for comparing plain text content.
 *
 * <p>
 * This class supports two modes of comparison:
 * <ul>
 *     <li><b>Default mode:</b> Uses a diff algorithm to detect changes across the full content.</li>
 *     <li><b>Single-row mode:</b> Compares lines one-by-one, useful when precise line alignment is expected.</li>
 * </ul>
 *
 * <p>Supports configuration options such as case-insensitive comparison, skipping identical lines,
 * and treating the content as a single row or multiline text.</p>
 *
 * <p>Returns a list of {@link DiffMessage} objects describing detected differences,
 * including inserted, removed, or modified lines.</p>
 *
 * <p>This comparator is typically used in systems that require visual diffing
 * or validation of plain-text documents such as logs, text files, or flat data records.</p>
 */
public class PlainTextComparator extends AbstractComparator {
    // rule: name = "ignoreCase", value = "true" or "false" (default) - Ignore case while comparison ( = "true")
    public static final String IGNORE_CASE = "ignoreCase";
    public static final String SINGLE_ROW_MODE = "singleRowMode";
    public static final String IGNORE_IDENTICAL = "ignoreIdentical";

    public static boolean ignoreIdentical;

    private boolean ignoreCase;
    private boolean singleRowMode;
    private final Logger log = LoggerFactory.getLogger(PlainTextComparator.class);

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        try {
            getConfigurationParameters(configuration);

            final List<String> erList = TextHelpers.stringToList((ignoreCase) ? er.toLowerCase() : er);
            final List<String> arList = TextHelpers.stringToList((ignoreCase) ? ar.toLowerCase() : ar);

            TextHelpers.processRule_ExcludeTextBlocks(erList, arList, configuration);

            return ((singleRowMode) ? compareRowByRow(erList, arList) : defaultComparison(erList, arList));

        } catch (Exception ex) {
            throw new ComparatorException(ex.getMessage(),20002);
        }
    }

    private List<DiffMessage> compareRowByRow(List<String> erList, List<String> arList) {
        List<DiffMessage> differences = new ArrayList<>();
        int diffCounter = 0;
        int erSize = erList.size();
        int arSize = arList.size();
        for (int k = 0; k < Math.min(erSize, arSize); k++) {
            if (!erList.get(k).equals(arList.get(k))) {
                differences.add(new DiffMessage(++diffCounter,
                                            TextHelpers.formatdiffCoords(k, 1), 
                                            TextHelpers.formatdiffCoords(k, 1), 
                                            ResultType.MODIFIED,
                                            "er/ar rows# " + k + " are different."));
            }
        }
        if (erSize < arSize) {
            differences.add(new DiffMessage(++diffCounter,
                                            TextHelpers.formatdiffEmptyLinesCoords(erSize, arSize - erSize),
                                            TextHelpers.formatdiffCoords(erSize, arSize - erSize),
                                            ResultType.EXTRA,
                                            "At er row# " + erSize + ": ar row(s)## " + erSize + "-"
                                                    + (arSize - 1) + " are inserted."));
        } else if (erSize > arSize) {
            differences.add(new DiffMessage(++diffCounter,
                                            TextHelpers.formatdiffCoords(arSize, erSize - arSize),
                                            TextHelpers.formatdiffEmptyLinesCoords(arSize, erSize - arSize),
                                            ResultType.MISSED,
                                            "At ar row# " + arSize + ": er row(s)## " + arSize + "-"
                                                    + (erSize - 1) + " are deleted."));
        }
        return differences;
    }
    
    private List<DiffMessage> defaultComparison(List<String> erList, List<String> arList) {
        List<DiffMessage> differences = new ArrayList<>();
        final Patch diff = DiffUtils.diff(erList, arList);
        int diffCounter = 0;
        for (Delta delta : (List<Delta>)diff.getDeltas()) {
            int erLinesCount = delta.getOriginal().getLines().size();
            int arLinesCount = delta.getRevised().getLines().size();
            int erPosition = delta.getOriginal().getPosition();
            int arPosition = delta.getRevised().getPosition();
            switch (delta.getType()) {
                case INSERT :   differences.add(new DiffMessage(++diffCounter, 
                                                                    TextHelpers
                                                                            .formatdiffEmptyLinesCoords(erPosition,
                                                                                    arLinesCount),
                                                                    TextHelpers
                                                                            .formatdiffCoords(arPosition,
                                                                                    arLinesCount),
                                                                    ResultType.EXTRA,
                                                                    "At er row# " + erPosition
                                                                            + ": ar row(s)## "
                                                                            + arPosition
                                                                            + "-"
                                                                            + (arPosition - 1 + arLinesCount)
                                                                            + " are inserted."));
                                break;
                case CHANGE :   differences.add(new DiffMessage(++diffCounter, 
                                                                    TextHelpers
                                                                            .formatdiffCoords(erPosition,
                                                                                    erLinesCount),
                                                                    TextHelpers
                                                                            .formatdiffCoords(arPosition,
                                                                                    arLinesCount),
                                                                    ResultType.MODIFIED,
                                                                    "er row(s)## "
                                                                            + erPosition
                                                                            + "-"
                                                                            + (erPosition - 1 + erLinesCount)
                                                                            + " are replaced with ar row(s)## "
                                                                            + arPosition
                                                                            + "-"
                                                                            + (arPosition - 1 + arLinesCount)));
                                // Check if the number of er/ar Lines isn't equal.
                                // Add INSERT or DELETE diffMessage
                                // (insert empty lines before next line of er/ar) in that case
                                if (erLinesCount > arLinesCount) {
                                    differences.add(new DiffMessage(++diffCounter, 
                                                                    "", 
                                                                    TextHelpers
                                                                            .formatdiffEmptyLinesCoords(
                                                                                    arPosition + 1,
                                                                                    erLinesCount - arLinesCount
                                                                            ),
                                                                    ResultType.MISSED,
                                                                    /* There is no description because this
                                                                    is 'technical'-diffMessage needed for 'merge-style'
                                                                    highlighting */
                                                                    ""));
                                } else if (erLinesCount < arLinesCount) {
                                    differences.add(new DiffMessage(++diffCounter, 
                                                                    TextHelpers.formatdiffEmptyLinesCoords(
                                                                            erPosition + 1,
                                                                            arLinesCount - erLinesCount),
                                                                    "", 
                                                                    ResultType.EXTRA,
                                                                    /* There is no description because this
                                                                    is 'technical'-diffMessage needed for 'merge-style'
                                                                    highlighting */
                                                                    ""));
                                }
                                break;
                case DELETE :   differences.add(new DiffMessage(++diffCounter, 
                                                                    TextHelpers
                                                                            .formatdiffCoords(erPosition,
                                                                                    erLinesCount),
                                                                    TextHelpers
                                                                            .formatdiffEmptyLinesCoords(arPosition,
                                                                                    erLinesCount),
                                                                    ResultType.MISSED,
                                                                    "At ar row# "
                                                                            + arPosition
                                                                            + ": er row(s)## "
                                                                            + erPosition
                                                                            + "-"
                                                                            + (erPosition - 1 + erLinesCount)
                                                                            + " are deleted."));
                                break;
                default:
                    // Unhandled delta type — no action taken
                    break;
            }
        }
        return differences;        
    }
    
    private void getConfigurationParameters(Parameters configuration) throws ComparatorException {
        ignoreCase = configuration.getBooleanParameter(IGNORE_CASE, false);
        singleRowMode = configuration.getBooleanParameter(SINGLE_ROW_MODE, false);
        ignoreIdentical = configuration.getBooleanParameter(IGNORE_IDENTICAL, false);
    }
}
