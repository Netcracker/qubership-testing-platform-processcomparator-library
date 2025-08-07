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

package org.tigris.subversion.svnclientadapter.utils;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * A thread-safe wrapper around {@link SimpleDateFormat}, using {@link ThreadLocal}
 * to avoid concurrency issues in multi-threaded environments.
 *
 * <p>This class allows formatting and parsing of dates using a consistent pattern
 * without the need to create new {@code SimpleDateFormat} instances for every thread.
 */
public class SafeSimpleDateFormat {

    private final String format;

    private static final ThreadLocal<Map<String, SimpleDateFormat>> dateFormats
            = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        public Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<>();
        }
    };

    private SimpleDateFormat getDateFormat(String format) {
        Map<String, SimpleDateFormat> formatters = dateFormats.get();
        SimpleDateFormat formatter = formatters.get(format);
        if (formatter == null) {
            formatter = new SimpleDateFormat(format);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            formatters.put(format, formatter);
        }
        return formatter;
    }

    /**
     * Constructs a new thread-safe date formatter with the specified format pattern.
     *
     * @param format the date format pattern (e.g. {@code "yyyy-MM-dd"})
     */
    public SafeSimpleDateFormat(String format) {
        this.format = format;
    }

    /**
     * Formats the given {@link Date} using the configured pattern.
     *
     * @param date the date to format
     * @return the formatted date string
     */
    public String format(Date date) {
        return getDateFormat(format).format(date);
    }

    /**
     * Formats the given object using the configured pattern.
     * The object must be an instance of {@link Date}.
     *
     * @param date the date object to format
     * @return the formatted date string
     * @throws IllegalArgumentException if the object is not a {@link Date}
     */
    public String format(Object date) {
        return getDateFormat(format).format(date);
    }

    /**
     * Parses the given date string into a {@link Date} object using the configured pattern.
     *
     * @param day the string to parse
     * @return the parsed date
     * @throws ParseException if the date string cannot be parsed
     */
    public Date parse(String day) throws ParseException {
        return getDateFormat(format).parse(day);
    }

    /**
     * Sets the time zone used for date formatting and parsing.
     *
     * @param tz the time zone to use
     */
    public void setTimeZone(TimeZone tz) {
        getDateFormat(format).setTimeZone(tz);
    }

    /**
     * Sets the calendar used by the date formatter.
     *
     * @param cal the calendar to set
     */
    public void setCalendar(Calendar cal) {
        getDateFormat(format).setCalendar(cal);
    }

    /**
     * Sets the number format used to format numbers in dates.
     *
     * @param format the number format to use
     */
    public void setNumberFormat(NumberFormat format) {
        getDateFormat(this.format).setNumberFormat(format);
    }

    /**
     * Specifies whether date/time parsing is to be lenient.
     *
     * @param lenient {@code true} to be lenient; {@code false} otherwise
     */
    public void setLenient(boolean lenient) {
        getDateFormat(format).setLenient(lenient);
    }

    /**
     * Sets the symbols used by the date formatter, such as month and weekday names.
     *
     * @param symbols the date format symbols to use
     */
    public void setDateFormatSymbols(DateFormatSymbols symbols) {
        getDateFormat(format).setDateFormatSymbols(symbols);
    }

    /**
     * Sets the start date for interpreting two-digit year values.
     *
     * @param date the beginning of the 100-year period to use for two-digit years
     */
    public void set2DigitYearStart(Date date) {
        getDateFormat(format).set2DigitYearStart(date);
    }
}
