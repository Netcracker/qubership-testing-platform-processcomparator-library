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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.qubership.automation.pc.core.enums.CompareResultType;
import org.qubership.automation.pc.data.Data;

/**
 * Represents the result of comparing two data objects.
 * </p>
 * Contains:
 * <ul>
 *   <li>a unique identifier for the comparison result,</li>
 *   <li>the type of the comparison result,</li>
 *   <li>the original data,</li>
 *   <li>a list of comparison results for individual attributes,</li>
 *   <li>nested comparison results for child objects,</li>
 *   <li>a summary result and a message describing the differences.</li>
 * </ul>
 * Can be used to build a difference tree for complex data structures.
 */
public class CompareResult {
    protected String id = UUID.randomUUID().toString();
    protected CompareResultType type; //Compare Result Type
    protected Data data; //er Data
    protected List<ResultData> ar = new ArrayList<>(); //ar Compare Results
    protected List<CompareResult> childs = new ArrayList<>(); //Child Records
    protected ResultType summaryResult; //Summary result
    protected DiffMessage summaryMessage; //Contain DiffMessages

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }    
    
    public CompareResultType getType() {
        return type;
    }

    public void setType(CompareResultType type) {
        this.type = type;
    }   
    
    public Data getData() {
        return data;
    }

    public void setData(Data er) {
        this.data = er;
    }    

    public List<ResultData> getAr() {
        return ar;
    }

    public void setAr(List<ResultData> ar) {
        this.ar = ar;
    }

    public List<CompareResult> getChilds() {
        return childs;
    }

    public void setChilds(List<CompareResult> childs) {
        this.childs = childs;
    }       

    public ResultType getSummaryResult() {
        return summaryResult;
    }

    public void setSummaryResult(ResultType summaryResult) {
        this.summaryResult = summaryResult;
    }        

    public DiffMessage getSummaryMessage() {
        return summaryMessage;
    }

    public void setSummaryMessage(DiffMessage summaryMessage) {
        this.summaryMessage = summaryMessage;
    }        
}
