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

package org.qubership.automation.pc.comparator.api;

import java.util.ArrayList;
import java.util.List;

import org.qubership.automation.pc.comparator.ComparatorManager;
import org.qubership.automation.pc.compareresult.CompareResult;
import org.qubership.automation.pc.configuration.ComparatorConfiguration;
import org.qubership.automation.pc.core.exceptions.ComparatorManagerException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.data.DataPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * API Resource.
 */
public class ComparatorResource {

    private final Logger log = LoggerFactory.getLogger(ComparatorResource.class);
    Gson gson = new Gson();

    public String compare(String context) {
        try {
            JsonObject contextObject = new JsonParser().parse(context).getAsJsonObject();
            ComparatorConfiguration globalConfiguration;
            List<DataPackage> dataPackages = new ArrayList<>();

            //Deserialize contexts            
            if (contextObject.has("comparatorConfiguration")) {
                globalConfiguration = gson.fromJson(
                        contextObject.getAsJsonObject("comparatorConfiguration").toString(),
                        ComparatorConfiguration.class
                );
            } else {
                log.info(ResponseMessages.msg(10101));
                globalConfiguration = new ComparatorConfiguration();
            }

            if (contextObject.has("dataPackages")) {
                JsonArray jsonDataPackages = contextObject.getAsJsonArray("dataPackages");
                for (int i = 0; i < jsonDataPackages.size(); i++) {
                    DataPackage dataPackage = gson.fromJson(jsonDataPackages.get(i).getAsJsonObject().toString(),
                            DataPackage.class);
                    dataPackages.add(dataPackage);
                }
                if (!dataPackages.isEmpty()) {
                    ComparatorManager comparatorManager = new ComparatorManager();
                    List<CompareResult> compareResult = comparatorManager.compare(dataPackages, globalConfiguration);
                    return gson.toJson(compareResult);
                } else {
                    return reportError(20101, ResponseMessages.msg(20101));
                }
            } else {
                return reportError(20101, ResponseMessages.msg(20101));
            }
        } catch (JsonSyntaxException ex) {
            return reportError(20102, ResponseMessages.msg(20102, ex.getMessage()), ex);
        } catch (ComparatorManagerException ex) {
            return reportError(ex.getStatusCode(), ResponseMessages.msg(ex.getStatusCode(), ex.getMessage()), ex);
        } catch (InterruptedException ex) {
            return reportError(20002, ResponseMessages.msg(20002, ex.getMessage()), ex);
        }
    }
    
    private String reportError(int statusCode, String errMessage, Throwable t) {
        log.error(errMessage, t);
        return JSONUtils.statusMessage(statusCode, errMessage).toString();
    }
    
    private String reportError(int statusCode, String errMessage) {
        log.error(errMessage);
        return JSONUtils.statusMessage(statusCode, errMessage).toString();
    }
}
