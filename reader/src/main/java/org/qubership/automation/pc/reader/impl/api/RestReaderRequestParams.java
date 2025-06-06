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

package org.qubership.automation.pc.reader.impl.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.core.enums.RequestMethod;

public class RestReaderRequestParams {

    private Map<String, String> headers = new HashMap<>();
    private RequestMethod method;
    private String endpoint;
    private int connectionTimeout = 10;
    private int connectionRequestTimeout = 10;
    private int socketTimeout = 10;
    private String body;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public RestReaderRequestParams setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public RequestMethod getMethod() {
        return method;
    }

    public RestReaderRequestParams setMethod(RequestMethod method) {
        this.method = method;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public RestReaderRequestParams setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public RestReaderRequestParams setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public RestReaderRequestParams setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
        return this;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public RestReaderRequestParams setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
        return this;
    }

    public String getBody() {
        return body;
    }

    public RestReaderRequestParams setBody(String body) {
        this.body = body;
        return this;
    }

    public void prepareFullEndpoint(String server) {
        if (!server.endsWith("/")) {
            server += "/";
        }
        if (StringUtils.isNotBlank(this.endpoint)) {
            if (this.endpoint.startsWith("/")) {
                this.endpoint = this.endpoint.substring(0);
            }
            server += this.endpoint;
        }
        this.endpoint = server;
    }
}
