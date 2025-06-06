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

import static com.google.common.base.Strings.isNullOrEmpty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.MDC;

public final class ThreadUtils {

    private static final ThreadLocal<String> originalThreadName = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isThreadNameChanged = new ThreadLocal<>();

    public static void setThreadName(String name) {
        if (isNullOrEmpty(name) || getThreadName().equals(name)) {
            return;
        }
        if (Objects.isNull(isThreadNameChanged.get()) || !isThreadNameChanged.get()) {
            originalThreadName.set(Thread.currentThread().getName());
        }
        Thread.currentThread().setName(name);
        isThreadNameChanged.set(true);
    }

    public static void appendToThreadName(String... suffixes) {
        String append = "";
        for (String suffix: suffixes) {
            append += "%" + suffix.replaceAll(" ", "_");
        }
        setThreadName(Thread.currentThread().getName() + append);
    }

    public static void returnToOriginalThreadName() {
        if (isNameChanged()) {
            Thread.currentThread().setName(originalThreadName.get());
            isThreadNameChanged.set(false);
        }
    }

    public static String getThreadName() {
        return Thread.currentThread().getName();
    }

    public static boolean isNameChanged() {
        return Objects.isNull(isThreadNameChanged.get()) ? false : isThreadNameChanged.get();
    }

    public static void setMdcContextMap(Map<String, String> mdcMap) {
        MDC.setContextMap((Objects.isNull(mdcMap) || mdcMap.isEmpty()) ? new HashMap<>() : mdcMap);
    }
}
