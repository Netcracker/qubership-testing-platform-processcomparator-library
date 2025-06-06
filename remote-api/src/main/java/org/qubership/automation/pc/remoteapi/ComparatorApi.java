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

package org.qubership.automation.pc.remoteapi;

import java.util.List;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.compareresult.CompareResult;
import org.qubership.automation.pc.models.HighlighterResult;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * {@code ComparatorApi} is a client-side utility for interacting with a remote data comparison API.
 *
 * <p>
 * It supports sending data packages and configurations to a comparator service,
 * and retrieving either comparison results or highlighted differences.
 * The API can operate in two modes: JSP and REST.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * ComparatorApi comparatorApi = new ComparatorApi("http://localhost:8080/api", "REST");
 * List<CompareResult> results = comparatorApi.compare(dataPackages, comparatorConfig);
 * }</pre>
 */
public class ComparatorApi extends RemoteApi {
    protected ComparatorApiMode comparatorApiMode;
    private String comparatorApiUrl;
    private String comparatorPath;
    private String highlighterPath;

    private Gson gson = new Gson();

    public ComparatorApi(String comparatorApiUrl) {
        this.comparatorApiUrl = comparatorApiUrl;
    }

    public ComparatorApi(String readerApiUrl, String comparatorApiModeString) {
        this.comparatorApiUrl = readerApiUrl;
        ComparatorApiMode comparatorApiMode = ComparatorApiMode.JSP;
        if (!StringUtils.isBlank(comparatorApiModeString)) {
            if (EnumUtils.isValidEnum(ComparatorApiMode.class, comparatorApiModeString)) {
                comparatorApiMode = ComparatorApiMode.valueOf(comparatorApiModeString);
            }
        }
        this.setComparatorApiMode(comparatorApiMode);
    }

    public void setComparatorApiMode(ComparatorApiMode comparatorApiMode) {
        this.comparatorApiMode = comparatorApiMode;
        this.comparatorPath = this.comparatorApiUrl + ((this.comparatorApiMode == ComparatorApiMode.JSP)
                ? "/comparator.jsp?action=compare" : "/rest/compare");
        this.highlighterPath = this.comparatorApiUrl + ((this.comparatorApiMode == ComparatorApiMode.JSP)
                ? "/highlighter" : "/rest/highlighter");
        this.remoteMethod = ((this.comparatorApiMode == ComparatorApiMode.JSP) ? RemoteMethod.POST : RemoteMethod.PUT);
    }

    public List<CompareResult> compare(Object dataPackages, Object comparatorConfiguration) throws RemoteApiException {
        String responseString = prepareRequest(dataPackages, comparatorConfiguration);
        JsonElement jsonElement = new JsonParser().parse(responseString);
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("statusCode")) {
                String statusCode = jsonObject.get("statusCode").getAsString();
                String statusMessage = "Unknown comparator exception (statusCode = " + statusCode + ")";
                if (jsonObject.has("statusMessage")) {
                    statusMessage = jsonObject.get("statusMessage").getAsString();
                }
                throw new RemoteApiException(statusMessage);
            }
        }
        List<CompareResult> resultList = gson.fromJson(responseString, new TypeToken<List<CompareResult>>() {
        }.getType());
        return resultList;
    }

    public String prepareRequest(Object dataPackages, Object comparatorConfiguration) throws RemoteApiException {
        JsonObject jsRequest = new JsonObject();
        jsRequest.add("comparatorConfiguration", gson.toJsonTree(comparatorConfiguration));
        jsRequest.add("dataPackages", gson.toJsonTree(dataPackages));

        return readRemote(this.comparatorPath, jsRequest.toString());
    }

    public List<HighlighterResult> getHighlight(Object diffs) throws RemoteApiException {
        String responseString = prepareHighLightRequest(diffs);
        JsonElement jsonElement = new JsonParser().parse(responseString);
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("statusCode")) {
                String statusCode = jsonObject.get("statusCode").getAsString();
                String statusMessage = "Unknown highlighter exception (statusCode = " + statusCode + ")";
                if (jsonObject.has("statusMessage")) {
                    statusMessage = jsonObject.get("statusMessage").getAsString();
                }
                throw new RemoteApiException(statusMessage);
            }
        }
        List<HighlighterResult> resultList = gson.fromJson(responseString, new TypeToken<List<HighlighterResult>>() {
        }.getType());
        return resultList;
    }

    public String prepareHighLightRequest(Object diffs) throws RemoteApiException {
        return readRemote(this.highlighterPath, ((JsonArray) diffs).toString());
    }
}
