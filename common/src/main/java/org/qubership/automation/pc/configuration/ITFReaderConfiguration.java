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
import java.util.List;

import org.qubership.automation.pc.configuration.datasource.SQLDataSource;

/**
 * Configuration class for ITF (Integration Test Framework) data readers.
 * <p>
 * Extends {@link ReaderConfiguration} to include a list of SQL data sources
 * which are required to retrieve or process data for test execution.
 *
 * <p><strong>Key Responsibilities:</strong></p>
 * <ul>
 *     <li>Encapsulates a list of {@link SQLDataSource} instances used by the reader.</li>
 *     <li>Provides getter and setter methods for managing those data sources.</li>
 * </ul>
 */
public class ITFReaderConfiguration extends ReaderConfiguration {
    private List<SQLDataSource> dataSources = new ArrayList<>();

    public List<SQLDataSource> getDataSources() {
        return dataSources;
    }

    public void setDataSources(List<SQLDataSource> dataSources) {
        this.dataSources = dataSources;
    }    
}
