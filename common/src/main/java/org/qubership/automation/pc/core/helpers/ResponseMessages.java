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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for retrieving predefined response messages by status or error code.
 * <p>
 * Used throughout the system to provide consistent and localized feedback for exceptions,
 * validation issues, and various components like Readers, Comparators, and Highlighters.
 * Supports message templates with variable substitution via {@code String.format()}.
 */
public class ResponseMessages {

    private static Map<Integer, String> messages = null;

    private static void fill() {
        if (messages == null) {
            messages = new HashMap<>();
        }
        messages.put(10000, "OK");
        messages.put(20000, "ERROR");

        messages.put(20001, "Exception. Read log for more information");
        messages.put(20002, "Exception: %s");
        messages.put(20003, "Regexp parsing exception: %s"); // These exceptions are common
        // - they can occur both in Reader and Comparator
        messages.put(20004, "Xpath parsing exception: %s");  // These exceptions are common
        // - they can occur both in Reader and Comparator

        //Comparator Messages
        messages.put(10101, "Using empty comparator configuration");    
        messages.put(10102, "Expected %s steps");
        messages.put(10103, "Actually %s steps");
        messages.put(10104, "Expected %s parameters");
        messages.put(10105, "Actually %s parameters");
        messages.put(10106, "Expected parameter \"%s\"");
        messages.put(10107, "Actually parameter \"%s\"");
        messages.put(20101, "ComparatorResource: Context doesn't have \"data_packages\" object");
        messages.put(20102, "ComparatorResource: Exception: %s");
        messages.put(20103, "Failed to instantiate Comparator - %s");
        messages.put(20104, "Comparator for %s content-type is Not Found");
        messages.put(20105, "DataPackages are Empty");
        messages.put(20106, "Step %s in process %s contains duplicate parameters");        
        messages.put(20107, "Steps count in ar process %s does not equal er steps count");
        messages.put(20108, "Parameters count in Process(%s)->Step(%s) does not equal er step parameters count");
        messages.put(20109, "Step %s is skipped");
        messages.put(20110, "Parameter with name \"%s\" is not found in ar step");
        
        messages.put(20111, "er isn't valid");
        messages.put(20112, "ar isn't valid");
        
        //Highlighter Messages
        messages.put(20150, "Highlighter exception while DOM parsing and/or Differences processing: %s");
        messages.put(20151, "Highlighter exception while initialising of STX-transformer: %s");
        messages.put(20152, "Highlighter exception while STX-transformation: %s");
        messages.put(20153, "Highlighter exception while rules checking: %s");
        messages.put(20153, "Highlighter exception while XSLT-transformation: %s");
        
        //Reader Messages
        messages.put(20201, "Reader by type %s is not found");        
        messages.put(20202, "Failed to instantiate Reader: %s");
        messages.put(20203, "Global parameter %s is not found");
        messages.put(20204, "Parameters Reader Mode or Reader Type are not defined");
        //SQL Reader
        messages.put(20205, "SQL Exception: %s");
        messages.put(20206, "Using POSITIVE and NEGATIVE (!) data source names is not allowed");
        messages.put(20207, "InputParameter %s is not found");
        messages.put(20208, "InputParameter %s doesn't have value");
        messages.put(20209, "DataSource parameters - Malformed URL: %s");
        messages.put(20210, "DataSource parameters - Unsupported Encoding: %s");
        messages.put(20211, "Error while connecting to database: %s");
        //ExcelFileReader
        messages.put(20301, "ExcelFileReader Exception: %s");

        messages.put(20501, "Reader error: %s");
    }
    
    public static String msg(int code) {
        if (messages == null) {
            fill();
        }
        return messages.get(code);
    }
    
    public static String msg(int code, String... args) {
        if (code == -1) {
            if (args == null) {
                return ""; /* Unknown error. Error message will be processed outside */
            } else {
                return args[0];
            }
        } else {
            if (messages == null) {
                fill();
            }
            if (args == null) {
                return String.format(messages.get(code));
            } else {
                return String.format(messages.get(code),Arrays.toString(args));
            }
        }        
    }
}
