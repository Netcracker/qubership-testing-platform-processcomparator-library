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

import org.qubership.automation.pc.configuration.parameters.Parameters;

/**
 * Represents a generic message structure with a name and associated parameters.
 * </p>
 * This class is typically used to encapsulate a named message and a collection of key-value
 * parameters related to the message.
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *     <li>{@code name} – the identifier or title of the message.</li>
 *     <li>{@code parameters} – a {@link Parameters} object that holds additional message metadata.</li>
 * </ul>
 *
 * <strong>Usage Example:</strong>
 * <pre>
 *     Message msg = new Message();
 *     msg.setName("OrderRequest");
 *     msg.getParameters().put("orderId", "12345");
 * </pre>
 */
public class Message {
    private String name;
    private Parameters parameters = new Parameters();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }       
}
