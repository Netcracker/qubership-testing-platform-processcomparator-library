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

package org.qubership.automation.pc.reader.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.enums.RequestMethod;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.qubership.automation.pc.reader.impl.api.RestReaderRequestParams;

import com.google.gson.Gson;

public class RESTReader implements IReader {
    private static final String RESPONSE = "response";
    private static final String RESPONSE_CODE = "responseCode";
    private static final String REASON_PHRASE = "reasonPhrase";
    private static final String RESPONSE_CONTENT_TYPE = "responseContentType";
    private SQLReaderConfiguration configuration;

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        this.setLocalConfiguration(configuration);
        return read(false);
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        this.setLocalConfiguration(configuration);
        return read(true);
    }

    @Override
    public String testConnection(Map<String, String> parameters) throws ReaderException {
        String connectionString = parameters.get("connectionString");
        try {
            RestReaderRequestParams params = new RestReaderRequestParams();
            CloseableHttpClient client = buildClient(params);
            client.execute(new HttpGet(connectionString));
            return JSONUtils.statusMessage(10000, "Success!").toString();
        } catch (Exception ex) {
            throw new ReaderException(ResponseMessages.msg(20211, ex.getMessage()));
        }
    }


    private void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

    private List<DataList> read(boolean isProcess) throws ReaderException {
        List<DataList> resultList = new ArrayList<>();
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());
            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }
            List<Data> dataRecords = new ArrayList<>();
            for (SQLReaderConfiguration.Script script : this.configuration.getScripts()) {
                RestReaderRequestParams requestParams = parseParametersFromScript(script);
                requestParams.prepareFullEndpoint(dataSource.getConnectionString());
                dataRecords.addAll(readFromApi(requestParams, script));
            }
            if (isProcess) {
                Data parentData = new Data();
                parentData.setDataType(DataType.PROCESS);
                parentData.setTimeStamp(new Date());
                parentData.setInternalId(UUID.randomUUID().toString());
                parentData.setChilds(dataRecords);
                dataList.setDatas(Arrays.asList(parentData));
                resultList.add(dataList);
            } else {
                dataList.setDatas(dataRecords);
                resultList.add(dataList);
            }
        }
        return resultList;
    }

    private RestReaderRequestParams parseParametersFromScript(SQLReaderConfiguration.Script script)
            throws ReaderException {
        if (StringUtils.isBlank(script.script)) {
            throw new ReaderException("Parameters cannot be empty!");
        }
        return new Gson().fromJson(script.script, RestReaderRequestParams.class);
    }

    private List<Data> readFromApi(RestReaderRequestParams requestParams,
                                   SQLReaderConfiguration.Script script) throws ReaderException {
        List<Data> dataRecords = new ArrayList<>();
        List<Data> datas = new ArrayList<>();
        HttpResponse requestResponse = sendRequest(requestParams);
        try {
            Data resultDataResponse = new Data(RESPONSE,DataType.SIMPLE, "0",new Date());
            resultDataResponse.setContent(DataContentConverter.fromString(
                    requestResponse.getEntity() == null ? "" :
                            IOUtils.toString(requestResponse.getEntity().getContent(), StandardCharsets.UTF_8.name())));
            Data resultDataResponsePhrase = new Data(REASON_PHRASE,DataType.SIMPLE, "0",new Date());
            resultDataResponsePhrase.setContent(
                    DataContentConverter.fromString(requestResponse.getStatusLine().getReasonPhrase()));
            Data resultDataResponseCode = new Data(RESPONSE_CODE,DataType.SIMPLE, "0",new Date());
            resultDataResponseCode.setContent(
                    DataContentConverter.fromString((String.valueOf(requestResponse.getStatusLine().getStatusCode()))));
            Data resultDataResponseContentType = new Data(RESPONSE_CONTENT_TYPE,DataType.SIMPLE, "0",new Date());
            resultDataResponseContentType.setContent(DataContentConverter.fromString(
                    requestResponse.getEntity() == null ? "" :
                            String.valueOf(requestResponse.getEntity().getContentType())));
            datas.add(resultDataResponse);
            datas.add(resultDataResponsePhrase);
            datas.add(resultDataResponseCode);
            datas.add(resultDataResponseContentType);
        } catch (IOException e) {
            throw new ReaderException(e);
        }
        for (Data data : datas) {
            String columnContentType = null;
            if (!script.fieldTypes.isEmpty()
                    && script.fieldTypes.get("*") != null
                    && script.fieldTypes.get("*").containsKey("*")) {
                columnContentType = script.fieldTypes.get("*").get("*");
            }
            if (!EnumUtils.isValidEnum(DataContentType.class, columnContentType) || columnContentType == null) {
                data.setContentType(DataContentType.PLAIN_TEXT);
            } else {
                data.setContentType(DataContentType.valueOf(columnContentType));
            }
            dataRecords.add(data);
        }
        return dataRecords;
    }

    private HttpResponse sendRequest(RestReaderRequestParams params) throws ReaderException {
        try {
            CloseableHttpClient client = buildClient(params);
            HttpUriRequest request = defineHttpRequest(params.getMethod(), params.getEndpoint(), params.getBody());
            for (Map.Entry<String, String> header : params.getHeaders().entrySet()) {
                request.setHeader(header.getKey(), header.getValue());
            }
            HttpResponse response = client.execute(request);
            return response;
        } catch (ReaderException | IOException ex) {
            throw new ReaderException(ex);
        }
    }

    private CloseableHttpClient buildClient(RestReaderRequestParams params) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(params.getConnectionTimeout() * 1000)
                .setConnectionRequestTimeout(params.getConnectionRequestTimeout() * 1000)
                .setSocketTimeout(params.getSocketTimeout() * 1000)
                .build();
        CloseableHttpClient client = this.createTrustAllHttpClientBuilder().setDefaultRequestConfig(config).build();
        return client;
    }


    private HttpClientBuilder createTrustAllHttpClientBuilder() {
        try {
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, (chain, authType) -> true);
            SSLConnectionSocketFactory sslsf = new
                    SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
            return HttpClients.custom().setSSLSocketFactory(sslsf);
        } catch (Exception e) {
            e.printStackTrace();
            return HttpClientBuilder.create();
        }
    }


    private HttpUriRequest defineHttpRequest(RequestMethod method, String url, String body) throws ReaderException {
        try {
            StringEntity entity = null;
            if (StringUtils.isNotBlank(body)) {
                entity = new StringEntity(body);
            }
            switch (method) {
                case GET:
                    return new HttpGet(url);
                case PUT:
                    HttpPut put = new HttpPut(url);
                    if (entity != null) {
                        put.setEntity(entity);
                    }
                    return put;
                case POST:
                    HttpPost post = new HttpPost(url);
                    if (entity != null) {
                        post.setEntity(entity);
                    }
                    return post;
                case DELETE:
                    return new HttpDelete(url);
                case OPTIONS:
                    return new HttpOptions(url);
                default:
                    throw new ReaderException("Unsupported request method [ " + method.toString() + " ] !");
            }
        } catch (UnsupportedEncodingException ex) {
            throw new ReaderException(ex);
        }
    }
}
