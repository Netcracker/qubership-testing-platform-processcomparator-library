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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qubership.automation.pc.reader.impl.DnRReader;

/**
 * Parser implementation for structured plain text files in Cisco-like format.
 *
 * <p>
 * This parser interprets indentation levels to form a hierarchical tree of {@link DnRBlock} objects,
 * representing parent-child relationships between lines based on whitespace prefixes.
 * </p>
 *
 * <p>
 * Used primarily in the DnRReader context to analyze device output logs or configuration dumps.
 * </p>
 */
public class CiscoTxtFileParser implements IDnRParser {

    @Override
    public List<DnRBlock> parse(BufferedReader bufferedReader) {
        List<DnRBlock> parsedFile = new ArrayList<>();
        try {
            String line;
            String previousIndents = "";
            DnRBlock block = null; 
            int shift = 1;
            while ((line = bufferedReader.readLine()) != null) {
                Matcher m = Pattern.compile("^(\\s+)").matcher(line); 
                if (m.find()) {
                    String indents = m.group(1);
                    if (indents.length() > previousIndents.length()) {
                        // start a new block
                        shift = indents.length() - previousIndents.length();
                        block = this.createNewBlock(line, block); 
                    } else if (previousIndents.equals(indents)) {
                        // update current block
                        block = block.getParent();
                        block = this.createNewBlock(line, block);
                    } else {
                        // current block finished, return to parent
                        while ((indents.length() - shift) < previousIndents.length()) {
                            previousIndents = previousIndents.substring(shift);
                            block = block.getParent();
                        }
                        block = this.createNewBlock(line, block);

                    }
                    previousIndents = indents;
                } else {
                    block = new DnRBlock();
                    block.setLine(line);
                    previousIndents = "";
                    parsedFile.add(block);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DnRReader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parsedFile;

    }

    private DnRBlock createNewBlock(String line, DnRBlock block) {
        DnRBlock newChildBlock = new DnRBlock();
        newChildBlock.setLine(line);
        if (block != null) {
            newChildBlock.setParent(block);
            if (block.getChildren() == null) {
                block.setChildren(new ArrayList<DnRBlock>());
            }
            block.getChildren().add(newChildBlock);
            block = newChildBlock;
        }
        return block;
    }
}
