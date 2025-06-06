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
import java.util.List;

import org.qubership.automation.pc.configuration.ComparatorConfiguration;

/**
 * Represents a container for holding expected and actual data along with comparison configuration.
 * </p>
 * This class is used in data comparison scenarios where a single expected result (ER) is
 * compared against one or more actual results (AR), using a given {@link ComparatorConfiguration}.
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *     <li>{@code er} – the expected data object.</li>
 *     <li>{@code ar} – a list of actual data objects to compare against the expected one.</li>
 *     <li>{@code configuration} – configuration settings that define how the comparison should be performed.</li>
 * </ul>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>
 *     DataPackage dataPackage = new DataPackage();
 *     dataPackage.setEr(expectedData);
 *     dataPackage.setAr(Arrays.asList(actualData1, actualData2));
 *     dataPackage.setConfiguration(config);
 * </pre>
 */
public class DataPackage {
    private Data er;
    private List<Data> ar = new ArrayList<>();
    private ComparatorConfiguration configuration = new ComparatorConfiguration();

    public Data getEr() {
        return er;
    }

    public void setEr(Data er) {
        this.er = er;
    }

    public List<Data> getAr() {
        return ar;
    }

    public void setAr(List<Data> ar) {
        this.ar = ar;
    }

    public ComparatorConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ComparatorConfiguration configuration) {
        this.configuration = configuration;
    }
        
}
