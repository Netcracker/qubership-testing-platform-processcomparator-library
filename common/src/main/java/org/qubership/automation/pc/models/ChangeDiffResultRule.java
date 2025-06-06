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

package org.qubership.automation.pc.models;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qubership.automation.pc.compareresult.ResultType;

public class ChangeDiffResultRule {

    private ResultType oldResult;
    private ResultType newResult;
    private String path;
    private static final Pattern CHANGE_DIFF_RESULT_RULE_REGEXP = Pattern.compile("^(.+?)=(.+?)=(.+?)$");

    public ChangeDiffResultRule() {
    }

    public static Optional<ChangeDiffResultRule> buildObject(String rule) {
        Optional<ChangeDiffResultRule> result = Optional.empty();
        Matcher m = CHANGE_DIFF_RESULT_RULE_REGEXP.matcher(rule);
        if (m.matches()) {
            ChangeDiffResultRule changeDiffResultRule = new ChangeDiffResultRule();
            changeDiffResultRule.setOldResult(ResultType.valueOf(m.group(1)))
                    .setNewResult(ResultType.valueOf(m.group(2)))
                    .setPath(m.group(3));
            result = Optional.of(changeDiffResultRule);
        }
        return result;
    }

    public ResultType getOldResult() {
        return oldResult;
    }

    public ChangeDiffResultRule setOldResult(ResultType oldResult) {
        this.oldResult = oldResult;
        return this;
    }

    public ResultType getNewResult() {
        return newResult;
    }

    public ChangeDiffResultRule setNewResult(ResultType newResult) {
        this.newResult = newResult;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ChangeDiffResultRule setPath(String path) {
        this.path = path;
        return this;
    }
}
