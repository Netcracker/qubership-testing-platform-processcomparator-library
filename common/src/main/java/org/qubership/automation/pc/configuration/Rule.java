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

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.configuration.parameters.Parameters;

/**
 * Represents a single processing rule used in test or data-driven automation flows.
 * </p>
 * Each rule is associated with a specific step and can contain multiple {@link Message} entries,
 * each with its own set of parameters. A rule may also be marked as {@code skip}, meaning it should
 * be bypassed during execution.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *     <li>Step identification via {@code step} field.</li>
 *     <li>Optional skipping logic via {@code skip} flag.</li>
 *     <li>Message handling through a list of named {@link Message} objects.</li>
 *     <li>Convenience methods to retrieve or create messages and their parameters by name.</li>
 * </ul>
 */
public class Rule {

    private String step;
    private boolean skip = false;
    private List<Message> messages;

    public Rule() {

    }

    public Rule(String step) {
        this.step = step;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public Parameters getParameters(String messageName) {
        if (this.messages != null) {
            for (Message message : this.messages) {
                if (message.getName().equals(messageName)) {
                    return message.getParameters();
                }
            }
        }
        return new Parameters();
    }

    public Message getMessage(String messageName, boolean createNew) {
        if (this.messages != null) {
            for (int i = 0; i < this.messages.size(); i++) {
                if (!StringUtils.isEmpty(this.messages.get(i).getName())
                        && this.messages.get(i).getName().equals(messageName)) {
                    return this.messages.get(i);
                }
            }
        } else {
            this.messages = new ArrayList<>();
        }
        if (createNew) {
            Message newMessage = new Message();
            newMessage.setName(messageName);
            this.messages.add(newMessage);
            int index = this.messages.size() - 1;
            return this.messages.get(index);
        } else {
            return null;
        }
    }
}
