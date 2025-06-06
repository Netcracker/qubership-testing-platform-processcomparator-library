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

package org.qubership.automation.pc.data;

import java.util.List;

/*
 * Used for return Data values for multiple Data Source
 */
public class DataList {

    private String id;
    private String controlId;
    private String name;
    private List<Data> datas;
    private List<String> readWarnings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }   

    public String getControlId() {
        return controlId;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }        
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Data> getDatas() {
        return datas;
    }

    public void setDatas(List<Data> datas) {
        this.datas = datas;
    }

    public List<String> getReadWarnings() {
        return readWarnings;
    }

    public void setReadWarnings(List<String> readWarnings) {
        this.readWarnings = readWarnings;
    }
}
