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

package org.qubership.automation.pc.utils;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import org.qubership.automation.pc.core.exceptions.ParentException;

public class MatcherForParentException extends TypeSafeMatcher<ParentException> {

    private int actualStatusCode;
    private final int expectedStatusCode;

    public static MatcherForParentException hasCode(int item) {
        return new MatcherForParentException(item);
    }

    private MatcherForParentException(int expectedStatusCode) {
        this.expectedStatusCode = expectedStatusCode;
    }

    @Override
    protected boolean matchesSafely(ParentException item) {
        actualStatusCode = item.getStatusCode();
        return actualStatusCode == expectedStatusCode;
    }

    @Override
    public void describeTo(Description description) {
        description.appendValue(actualStatusCode)
                .appendText(" was not found instead of ")
                .appendValue(expectedStatusCode);
    }
}
