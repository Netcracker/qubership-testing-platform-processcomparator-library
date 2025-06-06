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

package org.qubership.automation.pc.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataContentType;

/**
 * Configuration class for SQL-based data readers.
 * </p>
 * Defines the data sources, input/output parameters, and SQL scripts used by the reader
 * to retrieve and process data from relational databases.
 */
public class SQLReaderConfiguration extends ReaderConfiguration {
    
    private List<SQLDataSource> dataSources = new ArrayList<>();
    private List<Script> scripts = new ArrayList<>();
    private List<InputParameter> inputParameters = new ArrayList<>();
    private List<OutputParameter> outputParameters = new ArrayList<>();

    public List<SQLDataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<SQLDataSource> dataSources) {
        this.dataSources = dataSources;
    }

    
    public List<InputParameter> getInputParameters() {
        return inputParameters;
    }

    public void setInputParameters(List<InputParameter> inputParameters) {
        this.inputParameters = inputParameters;
    }

    public List<OutputParameter> getOutputParameters() {
        return outputParameters;
    }

    public List<Script> getScripts() {
        return scripts;
    }

    public void setScripts(List<Script> scripts) {
        this.scripts = scripts;
    }

    public void setOutputParameters(List<OutputParameter> outputParameters) {
        this.outputParameters = outputParameters;
    }        
    
    public class InputParameter {
        public String name;
        public String id;
        public String value;
        
        public InputParameter() {

        }

        public InputParameter(String name, String id, String value) {
            this.name = name;
            this.id = id;
            this.value = value;
        }
        
        public InputParameter(String name, String value) {
            this(name, "", value);
        }
    }
    
    public class OutputParameter {
        public String name;
        public String defaultValue;
        public DataContentType contentType;
    }
    
    public class Script {
        public String sourceName;
        public String script;
        public LinkedHashMap<String,Map<String,String>> fieldTypes = new LinkedHashMap<>();
    }
}
