/*******************************************************************************
 * Copyright (c) 2004, 2006 svnClientAdapter project and others.
 * </p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * </p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/

package org.tigris.subversion.svnclientadapter.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for various string operations.
 * </p>
 * This class provides utility methods for splitting strings and removing
 * characters from the beginning of a string. It is designed to be compatible
 * with older Java versions such as JDK 1.3, where some modern methods (like
 * {@code String.split}) are not available.
 * </p>
 */
public class StringUtils {

    /**
     * Splits a string into an array of substrings using the specified character
     * as a delimiter.
     * </p>
     * This method does not use {@code String.split} and is compatible with JDK 1.3+.
     * </p>
     *
     * @param str       the input string to split; must not be {@code null}
     * @param separator the character to use as the delimiter
     * @return an array of string segments resulting from the split
     */
    public static String[] split(String str, char separator) {
        int pos = 0;
        List list = new ArrayList();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char ch = str.charAt(i);
            if (ch == separator) {
                list.add(str.substring(pos, i));
                pos = i + 1;
            }
        }
        if (pos != length) {
            list.add(str.substring(pos, length));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    /**
     * Splits a string into an array of substrings using the specified string
     * as a delimiter.
     *
     * @param str       the input string to split; must not be {@code null}
     * @param separator the string to use as the delimiter
     * @return an array of string segments resulting from the split
     */
    public static String[] split(String str, String separator) {
        List list = new ArrayList();
        StringBuffer sb = new StringBuffer(str);
        int pos;

        while ((pos = sb.indexOf(separator)) != -1) {
            list.add(sb.substring(0, pos));
            sb.delete(0, pos + separator.length());
        }
        if (sb.length() > 0) {
            list.add(sb.toString());
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    // The following method is adapted from Apache Commons Lang's StringUtils class

    /**
     * Strips any of a set of characters from the start of a string.
     * </p>
     * A {@code null} input string returns {@code null}. An empty string ("")
     * returns the empty string. If the {@code stripChars} string is {@code null},
     * then whitespace is stripped as defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StringUtils.stripStart(null, *)          = null
     * StringUtils.stripStart("", *)            = ""
     * StringUtils.stripStart("abc", "")        = "abc"
     * StringUtils.stripStart("abc", null)      = "abc"
     * StringUtils.stripStart("  abc", null)    = "abc"
     * StringUtils.stripStart("abc  ", null)    = "abc  "
     * StringUtils.stripStart(" abc ", null)    = "abc "
     * StringUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str        the string to remove characters from; may be {@code null}
     * @param stripChars the characters to remove; {@code null} is treated as whitespace
     * @return the stripped string, or {@code null} if the input string is {@code null}
     */
    public static String stripStart(String str, String stripChars) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }
        int start = 0;
        if (stripChars == null) {
            while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
                start++;
            }
        } else if (stripChars.length() == 0) {
            return str;
        } else {
            while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != -1)) {
                start++;
            }
        }
        return str.substring(start);
    }
}

