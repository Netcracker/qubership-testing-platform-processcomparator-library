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

package org.qubership.automation.pc.configuration.datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a configuration object for a SQL data source.
 *
 * <p>
 * This class holds connection-related properties such as connection string,
 * credentials, and additional data source-specific parameters.
 * It is commonly used to configure and manage SQL connections in readers or processors
 * that interact with relational databases.
 * </p>
 */
public class SQLDataSource {
    private String id;
    private String name;
    private String connectionString;
    private String user;
    private String password;
    private Map<String,String> dsParameters = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }        

    public Map<String, String> getDsParameters() {
        return dsParameters;
    }

    public void setDsParameters(Map<String, String> dsParameters) {
        this.dsParameters = dsParameters;
    }        
}
