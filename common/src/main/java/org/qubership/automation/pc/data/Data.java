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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;

/*
 * MAIN Data format for Process Comparator
*/
public class Data {

    /**
     * Generating by Process Comparator Internal use.
     */
    protected String internalId = UUID.randomUUID().toString();

    /**
     * Id from reader.
     */
    protected String externalId;

    /**
     * Data type.
     */
    protected DataType dataType;

    /**
     * Data name.
     */
    protected String name;

    /**
     * Data time stamp.
     */
    protected Date timeStamp;

    /**
     * Content data type.
     */
    protected DataContentType contentType;

    /**
     * Data content For primitives its stay in decoded format For
     * CSV,XML,JSON,EXCEL,BITMAP it's will be encoded with Base64.
     */
    protected String content;

    /**
     * Children.
     */
    protected List<Data> childs = new ArrayList<>();

    protected int orderNum;

    //CONSTRUCTORS
    public Data() {
    }

    public Data(String name, DataType dataType, String externalId, Date timeStamp) {
        this.name = name;
        this.dataType = dataType;
        this.externalId = externalId;
        this.timeStamp = timeStamp;
    }

    //GETTERS AND SETTERS
    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public DataContentType getContentType() {
        return contentType;
    }

    public void setContentType(DataContentType contentType) {
        this.contentType = contentType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Data> getChilds() {
        return childs;
    }

    public void setChilds(List<Data> childs) {
        this.childs = childs;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    //END: GETTERS AND SETTERS
    public boolean containsChildWithName(String name) {
        if (this.childs != null) {
            for (Data child : this.childs) {
                if (child.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Data getChildByName(String name) {
        for (Data child : this.childs) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    public int getChildIndexByName(String name) {
        int counter = 0;
        for (Data child : this.childs) {
            if (child.getName().equals(name)) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    public int countOfChildsWhoseNameContains(String containsString) {
        int counter = 0;
        if (this.childs != null) {
            for (Data child : this.childs) {
                if (child.getName().startsWith(containsString)) {
                    counter++;
                }
            }
        }
        return counter;
    }

    public static int countOfChildsWhoseNameContains(List<Data> list, String containsString) {
        int counter = 0;
        for (Data child : list) {
            if (child.getName().startsWith(containsString)) {
                counter++;
            }
        }
        return counter;
    }

}
