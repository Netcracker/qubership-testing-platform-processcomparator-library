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

import java.util.List;

import org.qubership.automation.pc.data.Data;

/**
 * Represents the result of comparing a single actual data element against the expected one.
 * </p>
 * Contains the actual data, a list of differences, and a summary result indicating the outcome.
 * Used as part of a broader comparison result structure.
 */
public class ResultData {
    
    private Data ar;
    private List<DiffMessage> differences;
    private ResultType summaryResult;

    public Data getAr() {
        return ar;
    }

    public void setAr(Data ar) {
        this.ar = ar;
    }

    public List<DiffMessage> getDifferences() {
        return differences;
    }

    public void setDifferences(List<DiffMessage> differences) {
        this.differences = differences;
    }

    public ResultType getSummaryResult() {
        return summaryResult;
    }

    public void setSummaryResult(ResultType summaryResult) {
        this.summaryResult = summaryResult;
    }        
}
