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

public class JsonDiffMessage extends DiffMessage {

    private String expectedJsonPath;
    private String actualJsonPath;

    public JsonDiffMessage() {
        super();
    }

    public JsonDiffMessage(int orderId, String expected, String actual, ResultType result, String expectedJsonPath,
                           String actualJsonPath) {
        super(orderId, expected, actual, result);
        this.expectedJsonPath = expectedJsonPath;
        this.actualJsonPath = actualJsonPath;
    }

    public JsonDiffMessage(int orderId, String expected, String actual, ResultType result, String description,
                           String expectedJsonPath, String actualJsonPath) {
        super(orderId, expected, actual, result, description);
        this.expectedJsonPath = expectedJsonPath;
        this.actualJsonPath = actualJsonPath;
    }

    public JsonDiffMessage(DiffMessage diffTemplate) {
        super(diffTemplate);
    }

    public String getExpectedJsonPath() {
        return expectedJsonPath;
    }

    public JsonDiffMessage setExpectedJsonPath(String expectedJsonPath) {
        this.expectedJsonPath = expectedJsonPath;
        return this;
    }

    public String getActualJsonPath() {
        return actualJsonPath;
    }

    public JsonDiffMessage setActualJsonPath(String actualJsonPath) {
        this.actualJsonPath = actualJsonPath;
        return this;
    }
}
