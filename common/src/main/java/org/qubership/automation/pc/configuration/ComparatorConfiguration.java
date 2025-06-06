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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a container for comparator configuration sets used during data comparison.
 * </p>
 * This class maintains a global configuration set and a list of additional
 * named configuration sets that can apply to specific comparison targets.
 * It provides utility methods to retrieve specific configurations and to merge multiple
 * configurations into a single unified set.
 * </p>
 * The configuration sets encapsulate comparison rules, step-level overrides, and
 * parameter mappings that control how comparisons are performed between expected
 * and actual data structures.
 */
public class ComparatorConfiguration {

    private ComparatorConfigurationSet global = new ComparatorConfigurationSet();
    private List<ComparatorConfigurationSet> sets = new ArrayList<>();

    public ComparatorConfigurationSet getGlobal() {
        return global;
    }

    public void setGlobal(ComparatorConfigurationSet global) {
        this.global = global;
    }

    public List<ComparatorConfigurationSet> getSets() {
        return sets;
    }

    public void setSets(List<ComparatorConfigurationSet> sets) {
        this.sets = sets;
    }

    public ComparatorConfigurationSet getComparatorConfigurationSet(String applyTo) {        
        if (StringUtils.isEmpty(applyTo)) {
            for (int i = 0; i < sets.size(); i++) {
                if (sets.get(i).getApplyTo().equals(applyTo)) {
                    return sets.get(i);
                }
            }
        }
        return global;
    }

    public void merge(ComparatorConfiguration configuration) {
        
    }

    /**
     * Merges multiple {@link ComparatorConfigurationSet} instances into a single configuration set.
     * </p>
     * The merge is performed in the order of the provided sets. Starting with the first set, each
     * subsequent set can add new parameters or overwrite existing ones from the previous sets.
     * </p>
     *
     * @param sets an array of {@link ComparatorConfigurationSet} instances to merge. The order matters;
     *             later sets can override values from earlier ones.
     * @return a single {@link ComparatorConfigurationSet} containing the merged configuration.
     */
    public static ComparatorConfigurationSet mergeConfigurationSets(ComparatorConfigurationSet... sets) {
        List<ComparatorConfigurationSet> setsList = new ArrayList<>();
        setsList.addAll(Arrays.asList(sets));
        return ComparatorConfiguration.mergeConfigurationSets(setsList);
    }
    
    public static ComparatorConfigurationSet mergeConfigurationSets(List<ComparatorConfigurationSet> sets) {
        ComparatorConfigurationSet resultSet = new ComparatorConfigurationSet();
        for (ComparatorConfigurationSet set : sets) {
            resultSet.merge(set);
        }
        return resultSet;
    }
}
