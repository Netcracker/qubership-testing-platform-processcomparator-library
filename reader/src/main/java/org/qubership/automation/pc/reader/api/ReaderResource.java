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

package org.qubership.automation.pc.reader.api;

import java.util.List;
import java.util.Map;

import org.qubership.automation.pc.configuration.ITFReaderConfiguration;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.enums.ReaderType;
import org.qubership.automation.pc.core.exceptions.ReaderManagerException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.data.DataList;
import org.qubership.automation.pc.reader.ReaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Entry point for handling reader operations such as data retrieval and connection testing.
 *
 * <p>
 * This class acts as a bridge between external input (typically JSON-based context)
 * and internal reader management. It deserializes configuration, invokes the appropriate
 * reader logic, and returns results or status messages in JSON format.
 * </p>
 */
public class ReaderResource {
    private final Logger log = LoggerFactory.getLogger(ReaderResource.class);
    private final Gson gson = new Gson();
    
    public String read(String context) {
        try {
            JsonObject jsObject = new JsonParser().parse(context).getAsJsonObject();
            Parameters params = gson.fromJson(jsObject.get("global"), Parameters.class);
            String readerModeValue = params.get("readerMode");
            if (readerModeValue == null) {
                return JSONUtils.statusMessage(20204, ResponseMessages.msg(20204)).toString();
            }
            String readerTypeValue = params.get("readerType");
            ReaderType readerType = ReaderType.valueOf(readerTypeValue);
            ReaderManager readerManager = new ReaderManager();
            List<DataList> resultList = readerManager.read(getReaderConfiguration(readerType,context));
            return gson.toJson(resultList);
        } catch (ReaderManagerException ex) {
            return JSONUtils.statusMessage(20000, ex.getMessage()).toString();
        }
    }

    public String testConnection(String context) {
        try {
            JsonObject jsObject = new JsonParser().parse(context).getAsJsonObject();
            Map<String,String> params = gson.fromJson(jsObject.get("parameters"), Map.class);
            String readerTypeValue = jsObject.get("ReaderType").getAsString();
            ReaderManager readerManager = new ReaderManager();
            return readerManager.testConnection(readerTypeValue, params);
        } catch (ReaderManagerException ex) {
            return JSONUtils.statusMessage(20000, ex.getMessage()).toString();
        }
    }
    
    private Object getReaderConfiguration(ReaderType readerType, String context) {
        if (readerType == null) {
            return null;
        } else {
            if (readerType == ReaderType.ITFReader) {
                return gson.fromJson(context, ITFReaderConfiguration.class);
            } else {
                return gson.fromJson(context, SQLReaderConfiguration.class);
            }
        }
    }
}
