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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date and time conversion operations.
 * <p>
 * Provides methods to format timestamps into strings and parse date strings into {@link Date} objects
 * using custom masks and locales.
 */
public class DateUtils {

    public static final Locale defaultLocale = Locale.ENGLISH;

    public static String timestampToString(long timeStamp,String dateMask) {
        Date date = new Date(timeStamp);
        DateFormat df = new SimpleDateFormat(dateMask);
        return df.format(date);
    }

    public static Date parseDate(String dateString, String dateMask) throws ParseException {
        return parseDate(dateString,dateMask,defaultLocale);
    }

    public static Date parseDate(String dateString,String dateMask, Locale locale) throws ParseException {
        DateFormat format = new SimpleDateFormat(dateMask, locale);
        Date date = null;        
        date = format.parse(dateString);        
        return date;
    }
}
