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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class StringHelper {
    public static String maskName(String filename, String mask) {
        StringBuilder fileName = new StringBuilder(filename);
        Pattern pattern = Pattern.compile(mask);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            for (int group = 1; group <= matcher.groupCount(); group++) {
                for (int pos = matcher.start(group); pos < matcher.end(group); pos++) {
                    fileName.replace(pos, pos + 1, "X");
                }
            }
        }
        return StringUtils.isNotEmpty(fileName) ? fileName.toString() : filename;
    }

    public static String trimToLength(String str, int length) {
        if (str.length() <= length) {
            return str;
        }
        return str.substring(0, length) + "...[+" + (str.length() - length) + "]";
    }
}
