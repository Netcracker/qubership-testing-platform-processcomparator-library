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
import java.util.HashSet;
import java.util.List;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.helpers.TextHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comparator implementation for primitive values such as strings, numbers, booleans, etc.
 * <p>
 * Compares expected and actual values using type inference and supports both strict
 * equality and unordered list comparisons. Designed to handle flat, primitive data
 * typically encountered in CSV-like structures or parameter lists.
 */
public class PrimitivesComparator extends AbstractComparator {

    public static final String IGNORE_IDENTICAL  = "ignoreIdentical";

    public static boolean ignoreIdentical;


    private final Logger log = LoggerFactory.getLogger(PrimitivesComparator.class);

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        getConfigurationParameters(configuration);
        return compareStrings(er, ar);
    }
    
    /**
     *   Legacy version of primitives comparison.
     */
    private DiffMessage compareString(String er, String ar) {
        DiffMessage diff = new DiffMessage();
        diff.setExpected(er);
        diff.setActual(ar);
        if (ar.equals(er))  {
            diff.setResult(ResultType.IDENTICAL);
        } else {
            diff.setResult(ResultType.CHANGED);
        }
        
        log.debug(String.format("Compare String: er - %s, ar - %s, Compare Result - %s", er, ar,
                diff.getResult().toString()));
        return diff;
    }   

    /**
     *     Compares two lists of strings by existence of expected results (Order of values is not significant).
     *     Returns {@code List<DiffMessage>} - array of detailed diffs.
     */
    private List<DiffMessage> compareStrings(String er, String ar) {
        int diffCounter = 0;
        List<DiffMessage> differences = new ArrayList<>();

        List<String> erSplit = TextHelpers.stringToList(er);
        List<String> arSplit = TextHelpers.stringToList(ar);
        boolean equalSize = (erSplit.size() == arSplit.size());

        List<Character> arType = new ArrayList<>();
        List<Character> erType = new ArrayList<>();
        List<Integer> arHash = new ArrayList<>();
        List<Integer> erHash = new ArrayList<>();
        List<Long> arLong = new ArrayList<>();
        List<Long> erLong = new ArrayList<>();
        List<Double> arDouble = new ArrayList<>();
        List<Double> erDouble = new ArrayList<>();
        List<Boolean> arBoolean = new ArrayList<>();
        List<Boolean> erBoolean = new ArrayList<>();
        testEArTypes(erSplit, erType, erLong, erDouble, erBoolean, erHash);
        testEArTypes(arSplit, arType, arLong, arDouble, arBoolean, arHash);
        
        HashSet<Integer> existingAr = new HashSet<>();
        for (int i = 0; i < erSplit.size(); i++) {
            boolean matchFound = false;
            for (int j = 0; j < arSplit.size(); j++) {
                if (!existingAr.contains(j)) {
                    switch (erType.get(i)) {
                        case 'l' :
                            if (arType.get(j).equals('l') && erLong.get(i).equals(arLong.get(j))) {
                                matchFound = true;
                            }
                            break;
                        case 'd' :
                            if (arType.get(j).equals('d') && erDouble.get(i).equals(arDouble.get(j))) {
                                matchFound = true;
                            }
                            break;
                        case 'b' :
                            if (arType.get(j).equals('b') && erBoolean.get(i).equals(arBoolean.get(j))) {
                                matchFound = true;
                            }
                            break;
                        case 's' :
                            if (arType.get(j).equals('s') && erHash.get(i).equals(arHash.get(j))) {
                                matchFound = true;
                            }
                            break;
                        default:
                            log.warn(String.format("Unexpected type character in erType: %s", erType.get(i)));
                            break;
                    }
                    if (matchFound) {
                        existingAr.add(j);
                        break;
                    }
                }
            }
            if (!matchFound) {
                differences.add(
                        new DiffMessage(
                                ++diffCounter,
                                TextHelpers.formatdiffCoords(i, 1),
                                "",
                                (equalSize) ? ResultType.MODIFIED : ResultType.MISSED,
                                "er row# " + (i + 1) + " is MISSED in ar."));
            }
        }
        for (int i = 0; i < arSplit.size(); i++) {
            if (!existingAr.contains(i)) {
                differences.add(
                        new DiffMessage(
                                ++diffCounter,
                                "",
                                TextHelpers.formatdiffCoords(i, 1),
                                (equalSize) ? ResultType.MODIFIED : ResultType.EXTRA,
                                "ar has EXTRA row# " + (i + 1) + "."));
            }
        }
        return differences;
    }
    
    private void testEArTypes(List<String> arSplit,
                              List<Character> arType,
                              List<Long> arLong,
                              List<Double> arDouble,
                              List<Boolean> arBoolean,
                              List<Integer> arHash) {
        for (int j = 0; j < arSplit.size(); j++) {
            String str = arSplit.get(j);
            arHash.add(str.hashCode());
            str = str.trim();
            try {
                long i = Long.parseLong(str);
                arLong.add(i);
                arType.add('l');
                arDouble.add(i + 0.0);
                arBoolean.add(false);
                continue;
            } catch (Exception ex) {
                // do nothing
            }
            try {
                Double i = Double.parseDouble(str);
                arDouble.add(i);
                arBoolean.add(false);
                if (i == i.longValue()) {
                    arType.add('l');
                    arLong.add(i.longValue());
                } else {
                    arType.add('d');
                    arLong.add(0L);
                }
                continue;
            } catch (Exception ex) {
                // do nothing
            }
            
            if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no")) {
                arLong.add(0L);
                arType.add('b');
                arDouble.add(0.0);
                arBoolean.add(false);
            } else if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("yes")) {
                arLong.add(0L);
                arType.add('b');
                arDouble.add(0.0);
                arBoolean.add(true);
            } else {
                arLong.add(0L);
                arType.add('s');
                arDouble.add(0.0);
                arBoolean.add(false);
            }
        }
    }

    private void getConfigurationParameters(Parameters configuration) throws ComparatorException {
        ignoreIdentical = configuration.getBooleanParameter(IGNORE_IDENTICAL, false);
    }
}
