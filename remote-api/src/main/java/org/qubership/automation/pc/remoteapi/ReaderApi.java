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
import org.qubership.automation.pc.data.DataList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

/**
 * {@code ReaderApi} is a client-side helper class for accessing a remote Reader API.
 * It supports different API modes (JSP or REST) and handles data reading and connection testing.
 *
 * <p>
 * It extends {@link RemoteApi} and uses its HTTP communication logic to interact with the configured endpoint.
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * ReaderApi api = new ReaderApi("http://localhost:8080/api", "REST");
 * List<DataList> data = api.readAsList(readerConfiguration);
 * }</pre>
 */
public class ReaderApi extends RemoteApi {
    protected ComparatorApiMode comparatorApiMode;
    private String readerApiUrl;
    private String readerPath;

    public ReaderApi(String readerApiUrl) {
        this.readerApiUrl = readerApiUrl;
    }

    public ReaderApi(String readerApiUrl, String comparatorApiModeString) {
        this.readerApiUrl = readerApiUrl;
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
        this.readerPath = this.readerApiUrl + ((this.comparatorApiMode == ComparatorApiMode.JSP)
                ? "/reader.jsp?action=read" : "/rest/read");
        this.remoteMethod = ((this.comparatorApiMode == ComparatorApiMode.JSP)
                ? RemoteMethod.POST : RemoteMethod.PUT);
    }
    
    public List<DataList> readAsList(Object readerConfiguration) throws RemoteApiException {
        String responseString = readAsString(readerConfiguration);
        JsonElement jsonElement = new JsonParser().parse(responseString);
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject.has("statusCode")) {
                String statusCode = jsonObject.get("statusCode").getAsString();
                String statusMessage = "Unknown reader exception (statusCode = " + statusCode + ")";
                if (jsonObject.has("statusMessage")) {
                    statusMessage = jsonObject.get("statusMessage").getAsString();
                }
                throw new RemoteApiException(statusMessage);
            }
        }
        List<DataList> resultList = gson.fromJson(responseString, new TypeToken<List<DataList>>() {
        }.getType());
        return resultList;
    }

    public String readAsString(Object readerConfiguration) throws RemoteApiException {
        return readRemote(this.readerPath, gson.toJson(readerConfiguration));
    }

    public String testConnection(Object readerConfiguration) throws RemoteApiException {
        return readRemote(this.readerPath + "/testConnection", gson.toJson(readerConfiguration));
    }
}
