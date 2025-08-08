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

package org.qubership.automation.pc.models;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HighlighterResult {

    @SerializedName("ER")
    private HighlighterNode er;
    @SerializedName("AR")
    private HighlighterNode ar;
    private String trId;
    private String objectId;
    private JsonArray rules;
    private String statusCode;
    private String statusMessage;
    private String readMessage;
    private HighlighterNode combined;

    public HighlighterResult(HighlighterResult node) {
        this.er = node.er;
        this.ar = node.ar;
        this.trId = node.trId;
        this.objectId = node.objectId;
        this.rules = node.rules;
        this.statusCode = node.statusCode;
        this.statusMessage = node.statusMessage;
        this.readMessage = node.readMessage;
        this.combined = node.combined;
    }

}
