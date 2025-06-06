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

public class SafeSimpleDateFormat {
    private final String format;
    private static final ThreadLocal<Map<String, SimpleDateFormat>> dateFormats
            = new ThreadLocal<Map<String, SimpleDateFormat>>() {
        public Map<String, SimpleDateFormat> initialValue() {
            return new HashMap<String, SimpleDateFormat>();
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

    public SafeSimpleDateFormat(String format) {
        this.format = format;
    }

    public String format(Date date) {
        return getDateFormat(format).format(date);
    }

    public String format(Object date) {
        return getDateFormat(format).format(date);
    }

    public Date parse(String day) throws ParseException {
        return getDateFormat(format).parse(day);
    }

    public void setTimeZone(TimeZone tz) {
        getDateFormat(format).setTimeZone(tz);
    }

    public void setCalendar(Calendar cal) {
        getDateFormat(format).setCalendar(cal);
    }

    public void setNumberFormat(NumberFormat format) {
        getDateFormat(this.format).setNumberFormat(format);
    }

    public void setLenient(boolean lenient) {
        getDateFormat(format).setLenient(lenient);
    }

    public void setDateFormatSymbols(DateFormatSymbols symbols) {
        getDateFormat(format).setDateFormatSymbols(symbols);
    }

    public void set2DigitYearStart(Date date) {
        getDateFormat(format).set2DigitYearStart(date);
    }
}
