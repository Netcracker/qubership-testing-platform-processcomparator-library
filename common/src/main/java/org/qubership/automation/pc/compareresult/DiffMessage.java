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

package org.qubership.automation.pc.compareresult;

import java.util.Objects;

/**
 * Represents a single message detailing a difference found during data comparison.
 * </p>
 * Contains both raw and human-readable representations of expected and actual values,
 * as well as metadata such as comparison order and result type.
 * Can be used in reporting, highlighters, and integration with external systems.
 */
public class DiffMessage {

    private int orderId;
    private String expected;
    private String expectedValue;
    private String actual;
    private String actualValue;
    // To set human-understandable description of the difference in comparator - for future use in highlighter,
    // reports or external systems
    private String description;
    private ResultType result;

    public DiffMessage() {
    }

    public DiffMessage(int orderId, String expected, String actual, ResultType result) {
        this.expected = expected;
        this.actual = actual;
        this.result = result;
        this.orderId = orderId;
    }

    public DiffMessage(int orderId, String expected, String actual, ResultType result, String description) {
        this(orderId, expected, actual, result);
        this.description = description;
    }

    public DiffMessage(DiffMessage diffTemplate) {
        this.expected = diffTemplate.getExpected();
        this.actual = diffTemplate.getActual();
        this.result = diffTemplate.getResult();
        this.orderId = diffTemplate.getOrderId();
        this.description = diffTemplate.getDescription();
        this.actualValue = diffTemplate.getActualValue();
    }

    public String getExpected() {
        return expected;
    }

    public DiffMessage setExpected(String expected) {
        this.expected = expected;
        return this;
    }

    public String getActual() {
        return actual;
    }

    public int getOrderId() {
        return orderId;
    }

    public DiffMessage setOrderId(int orderId) {
        this.orderId = orderId;
        return this;
    }

    public DiffMessage setActual(String actual) {
        this.actual = actual;
        return this;
    }

    public ResultType getResult() {
        return result;
    }

    public DiffMessage setResult(ResultType result) {
        this.result = result;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DiffMessage setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public DiffMessage setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
        return this;
    }

    public String getActualValue() {
        return actualValue;
    }

    public DiffMessage setActualValue(String actualValue) {
        this.actualValue = actualValue;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DiffMessage that = (DiffMessage) o;
        return orderId == that.orderId
                && Objects.equals(expected, that.expected)
                && Objects.equals(expectedValue, that.expectedValue)
                && Objects.equals(actual, that.actual)
                && Objects.equals(actualValue, that.actualValue)
                && Objects.equals(description, that.description)
                && result == that.result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, expected, expectedValue, actual, actualValue, description, result);
    }
}
