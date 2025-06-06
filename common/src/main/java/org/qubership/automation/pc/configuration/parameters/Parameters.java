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

package org.qubership.automation.pc.configuration.parameters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.qubership.automation.pc.core.interfaces.IConfiguration;

/**
 * Represents a collection of configuration parameters used across various components.
 *
 * <p>
 * The {@code Parameters} class stores a list of key-value pairs (parameters), supporting
 * multiple values per key and providing utility methods for retrieving parameters
 * as different data types (e.g., String, Integer, Long, Boolean).
 * </p>
 *
 * <p>
 * This class implements the {@link IConfiguration} interface and can be used to inject
 * or extract configuration data in a consistent way throughout the application.
 * </p>
 */
public class Parameters implements IConfiguration {
    
    private List<Parameter> parameters = new ArrayList<>();
    private int lastContainsKeyIndex = -1;
    
    public Parameters() {
        
    }
    
    public Parameters(Map<String, List<String>> sourceMap) {
        for (Map.Entry<String, List<String>> item : sourceMap.entrySet()) {
            for (String curStr : item.getValue()) {
                put(item.getKey(), curStr);
            }    
        }
    }

    public void clear() {
        this.parameters.clear();
        lastContainsKeyIndex = -1;
    }
    
    public void put(String key, String value) {
        Parameter parameter = new Parameter();
        parameter.name = key;
        parameter.value = value;
        this.parameters.add(parameter);
    }

    public void putAll(List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            String[] valueArray = parameter.value.split("\\n");
            for (String value : valueArray) {
                this.put(parameter.name, value);
            }
        }
    }
    
    public void putAll(Parameters parameters) {
        for (Parameter parameter : parameters.getParameters()) {
            this.parameters.add(parameter);
        }
    }
    
    
    public boolean containsKey(String parameterName) {
        int counter = -1;
        for (Parameter parameter : this.parameters) {
            counter++;
            if (parameter.name.equals(parameterName)) {
                this.lastContainsKeyIndex = counter;
                return true;
            }
            
        }
        return false;
    }
    
    public String get(String parameterName) {
        if (containsKey(parameterName)) {
            return this.parameters.get(lastContainsKeyIndex).value;
        } else {
            return null;
        }
    }
    
    @Override
    public Boolean has(String parameterName) {
        return containsKey(parameterName);
    }

    public List<Parameter> getParameters() {
        return this.parameters;
    }
    
    @Override
    public List<String> getParameters(String parameterName) {
        if (containsKey(parameterName)) {
            List<String> resultList = new ArrayList<>();
            for (int i = lastContainsKeyIndex; i < this.parameters.size(); i++) {
                if (this.parameters.get(i).name.equals(parameterName)) {
                    resultList.add(this.parameters.get(i).value);
                }
            }
            return resultList;
        } else {
            return null;
        }
    }
    
    @Override
    public String getParameter(String parameterName) {
        if (containsKey(parameterName)) {
            return get(parameterName);
        } else {
            return null;
        }
    }

    @Override
    public String getParameter(String parameterName, String defaultValue) {
        if (containsKey(parameterName)) {
            return get(parameterName);
        } else {
            return defaultValue;
        }
    }

    @Override
    public Integer getIntegerParameter(String parameterName) {
        if (containsKey(parameterName)) {
            return Integer.valueOf(get(parameterName));
        } else {
            return null;
        }        
    }

    @Override
    public Integer getIntegerParameter(String parameterName, Integer defaultValue) {
        if (containsKey(parameterName)) {
            return Integer.valueOf(get(parameterName));
        } else {
            return defaultValue;
        }
        
    }        

    @Override
    public Long getLongParameter(String parameterName) {
        if (containsKey(parameterName)) {
            return Long.valueOf(get(parameterName));
        } else {
            return null;
        }
    }

    @Override
    public Long getLongParameter(String parameterName, Long defaultValue) {
        if (containsKey(parameterName)) {
            return Long.valueOf(get(parameterName));
        } else {
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanParameter(String parameterName) {
        if (containsKey(parameterName)) {
            return Boolean.valueOf(get(parameterName));
        } else {
            return null;
        }
    }

    @Override
    public Boolean getBooleanParameter(String parameterName, Boolean defaultValue) {
        if (containsKey(parameterName)) {
            return Boolean.valueOf(get(parameterName));
        } else {
            return defaultValue;
        }
    }        

    public Map<String, List<String>> toMap() {
        Map<String, List<String>> result = new HashMap<>();
        for (Parameter item : this.parameters) {
            if (!result.containsKey(item.name)) {
                result.put(item.name, getParameters(item.name));
            }
        }
        return result;
    }
    
    public void removeParameter(String parameterName) {
        Iterator<Parameter> it = this.parameters.iterator();
        while (it.hasNext()) {
            Parameter item = it.next();
            if (item.name.equals(parameterName)) {
                it.remove();
            }
        }
    }
}
