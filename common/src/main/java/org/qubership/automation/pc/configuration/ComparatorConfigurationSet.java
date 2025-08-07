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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qubership.automation.pc.configuration.parameters.Parameter;
import org.qubership.automation.pc.configuration.parameters.Parameters;

/**
 * Represents a set of configuration rules and parameters used in the data comparison process.
 * <p>
 * This class supports:
 * <ul>
 *     <li>Global comparison parameters
 *     (with optional grouping using square-bracket notation like {@code param[group]}).</li>
 *     <li>Step-specific rules via the {@link Rule} class, allowing fine-grained control over step comparisons.</li>
 *     <li>Support for merging configurations from multiple sources.</li>
 * </ul>
 * <p>
 * Grouped parameters can be extracted using the {@code groupParameters()} method, which transforms grouped keys
 * into flat structure for further processing.
 * The merging logic ensures consistent rule extension and parameter consolidation.
 */
public class ComparatorConfigurationSet {
    public static final String GROUP_REGEXP_MASK = "(.*?)\\[(.*?)\\]";
    
    private String applyTo = "";
    private Parameters parameters = new Parameters();
    private List<Rule> rules = new ArrayList<>();    

    public String getApplyTo() {
        return applyTo;
    }

    public void setApplyTo(String applyTo) {
        this.applyTo = applyTo;
    }
    
    public Parameters getParameters() {
        return this.getParameters(false);
    }
    
    public Parameters getParameters(Boolean groupParameters) {
        this.groupParameters();
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }        
    
    public Rule getStepRule(String stepNumber) {
        for (int i = 0; i < rules.size(); i++) {
            if (rules.get(i).getStep().equals(stepNumber)) {
                return rules.get(i);
            }
        }
        return new Rule(stepNumber);        
    }
    
    public void groupParameters() {
        Map<String,Map<String,String>> groupedParameters = new HashMap<>();
        Pattern p = Pattern.compile(GROUP_REGEXP_MASK);
        for (Parameter parameter : this.parameters.getParameters()) {
            Matcher m = p.matcher(parameter.name);
            if (m.find()) {
                String parameterName = m.group(1);
                String parameterGroup = m.group(2);
                if (!groupedParameters.containsKey(parameterGroup)) {
                    groupedParameters.put(parameterGroup,new HashMap<>());
                }
                if (!groupedParameters.get(parameterGroup).containsKey(parameterName)) {
                    groupedParameters.get(parameterGroup).put(parameterName, "");
                }
                String newLine = parameter.value;                
                String parameterValue = groupedParameters.get(parameterGroup).get(parameterName);
                if (!parameterValue.endsWith("\n")) {
                    parameterValue += "\n";
                }
                groupedParameters.get(parameterGroup).put(parameterName, parameterValue + newLine);
            }
        }
        for (Map.Entry<String,Map<String,String>> parameterGroup : groupedParameters.entrySet()) {
            for (Map.Entry<String,String> groupedParameter : parameterGroup.getValue().entrySet()) {
                String targetParameter = groupedParameter.getKey() + "[" + parameterGroup.getKey() + "]";
                this.parameters.removeParameter(targetParameter);                
                this.parameters.put(groupedParameter.getKey(), groupedParameter.getValue());                
            }            
        }
    }
    
    public void merge(ComparatorConfigurationSet set) {
        if (!this.applyTo.isEmpty()) {
            if (!set.getApplyTo().equals(this.applyTo)) {
                return;
            }
        }        
        //merge parameters
        this.parameters.putAll(set.getParameters());        
        //merge rules
        for (Rule mergeRule : set.getRules()) {
            Rule foundRule = null;
            for (Rule rule : this.rules) {
                if (rule.getStep().equals(mergeRule.getStep())) {
                    foundRule = rule;
                    break;
                }
            }
            if (foundRule != null) {
                for (Message mergeMessage : mergeRule.getMessages()) {
                    Message foundMessage = null;
                    for (Message message : foundRule.getMessages()) {
                        if (message.getName().equals(mergeMessage.getName())) {
                            foundMessage = message;
                            break;
                        }
                    }
                    if (foundMessage != null) {
                        foundMessage.getParameters().putAll(mergeMessage.getParameters());
                    } else {
                        foundRule.getMessages().add(mergeMessage);
                    }                    
                }                
            } else {
                this.rules.add(mergeRule);
            }           
        }
    }
}
