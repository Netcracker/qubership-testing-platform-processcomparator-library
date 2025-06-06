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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.compareresult.ResultType;
import org.qubership.automation.pc.models.HighlighterNode;
import org.qubership.automation.pc.models.HighlighterResult;

/**
 * Responsible for building syntax-highlighted HTML representations of XML content
 * based on XSD validation differences.
 *
 * <p>
 * This utility class generates annotated HTML output from raw XML text by highlighting
 * elements and positions where validation errors, warnings, or fatal errors were detected.
 *
 * <p>
 * The differences are provided as a list of {@link DiffMessage} objects containing
 * line and column coordinates, which are used to identify and decorate problematic
 * parts of the original XML string.
 *
 * <p>
 * Output is wrapped in a {@code <pre>} block with embedded {@code <span>} tags
 * carrying validation status (e.g., ERROR, WARNING) and metadata for UI interaction
 * such as popovers and anchors.
 * </p>
 * Please take in mind that the algorithm of 'processDifferences' assumes that
 * differences are ordered as (line asc, column asc). If there will be new rules
 * or some other changes of XsdComparator, be aware of differences' order or
 * implement new algorithm of highlighter
 */
public class BuildColoredXsdXML {

    public static HighlighterResult highlight(List<DiffMessage> differences, String er, String ar) {

        String erMessage = (StringUtils.isBlank(er)) ? "" : processDifferences(differences, er, false);
        String arMessage = (StringUtils.isBlank(ar)) ? "" : processDifferences(differences, ar, true);

        HighlighterNode erNode = new HighlighterNode();
        HighlighterNode arNode = new HighlighterNode();
        erNode.setValue(erMessage);
        erNode.setIsPlain(true);
        arNode.setValue(arMessage);
        arNode.setIsPlain(true);
        HighlighterResult resultMap = new HighlighterResult();
        resultMap.setEr(erNode);
        resultMap.setAr(arNode);

        return resultMap;
    }

    private static String diffGetText(DiffMessage diff, Boolean isActual) {
        return isActual ? diff.getActual() : diff.getExpected();
        /* er/ar are validated by XSD SEPARATELY but results are in combined array! */
    }

    private static String processDifferences(List<DiffMessage> differences, String docEar, Boolean isActual) {
        String diffCoords;
        String resultmsg = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><pre>";
        int lineNumber;
        int colNumber;
        int curN = 0;

        /* Need to remove all dust before "<?xml..." but we can't use XmlHelpers.cleanXml
        - it collapses all content to single line */
        int k = docEar.indexOf("<?xml");
        if (k > 0) {
            docEar = docEar.substring(k);
        }

        List<String> ear = new ArrayList<String>(Arrays.asList(docEar.split("\\r\\n|\\r|\\n")));

        for (DiffMessage diff : differences) {
            // Actual / Expected in format: N1-N2 where N1,N2 - lineNumber & colNumber (next symbol after error)
            diffCoords = diffGetText(diff, isActual);

            if (diff.getResult() == ResultType.SKIPPED || diffCoords.isEmpty()) {
                continue;
            } else {
                String[] diffPositionSplit = diffCoords.split("-");
                if (diffPositionSplit == null || diffPositionSplit.length != 2) {
                    continue; // May be we should throw an exception here
                } else {
                    try {
                        lineNumber = Integer.parseInt(diffPositionSplit[0]);
                        colNumber = Integer.parseInt(diffPositionSplit[1]);
                        if (lineNumber < 1 || colNumber < 1) {
                            continue;  // May be we should throw an exception here
                        }
                    } catch (NumberFormatException ex) {
                        continue;  // May be we should throw an exception here
                    }
                }
            }

            /* If curN < lineNumber: transfer lines[curN]...[lineNumber-1] to resultmsg */
            for (int i = curN; i < lineNumber - 1; i++) {
                resultmsg = resultmsg + escape(ear.get(i)) + "<br>";
            }

            /* If Warning or Error, colNumber points to next symbol (after incorrect tag) */
            /* But in case of Fatal Error, colNumber points to first symbol of incorrect tag !!!
                Decision: highlight WHOLE LINE in case of Fatal Error */
            if (diff.getResult() == ResultType.ERROR) {
                resultmsg = resultmsg
                        + startTag(diff.getOrderId(), diff.getResult().toString(), diffCoords, diff.getDescription());
                resultmsg = resultmsg + escape(ear.get(lineNumber - 1)) + "</span>";
                /* Print whole line as highlighted text */
            } else {
                /* Current line: 
                    1. Search LAST '<' before colNumber 
                    2. Print the beginning of the line as normal text
                    3. Print tag found in [1] as highlighted text
                    4. Print the tail of the line as normal text
                 */
                String s = ear.get(lineNumber - 1);
                int i = s.lastIndexOf("<", colNumber >= s.length() ? (s.length() - 1) : colNumber - 1);
                if (i == -1) {
                    i = 0;
                }

                resultmsg = resultmsg + escape(s.substring(0, i));
                /* 2. Print the beginning of the line as normal text */
                resultmsg = resultmsg
                        + startTag(diff.getOrderId(), diff.getResult().toString(), diffCoords, diff.getDescription());
                resultmsg = resultmsg + escape(s.substring(i, colNumber - 1)) + "</span>";
                /* 3. Print tag found in [1] as highlighted text */

                if (colNumber <= s.length()) {
                    resultmsg = resultmsg + escape(s.substring(colNumber - 1));
                }
            }
            resultmsg = resultmsg + "<br>";
            curN = lineNumber;
        }

        for (int i = curN; i < ear.size(); i++) {
            resultmsg = resultmsg + escape(ear.get(i)) + "<br>";
        }
        return resultmsg + "</pre>";
    }

    private static String startTag(int orderId, String result, String diffCoords, String diffDescription) {
        return "<span data-block-id=\"pc-highlight-block\" class=\""
                + result + "\" title=\""
                + diffCoords + ": " + diffDescription.replaceAll("\"", "'").replaceAll("\n", " ") + "\""
                + " id=\"anchor-" + UUID.randomUUID().toString().substring(0, 8) + "\""
                + " data-diff-name=\"Diff-" + orderId + "\""
                + " data-toggle=\"popover\""
                + ">";
    }

    private static String escape(String src) {
        return src.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }
}
