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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration.Script;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reader implementation for fetching and parsing build information from remote HTTP sources.
 *
 * <p>This class retrieves content using HTTP GET requests and applies regular expressions to extract
 * specific values, such as build numbers. The behavior is configurable via {@link SQLReaderConfiguration}.</p>
 *
 * <p>Each configured data source provides a URL to request. The reader parses the response and wraps
 * extracted values into {@link DataList} objects.</p>
 *
 * <p>Used in automated testing or monitoring pipelines to collect build metadata for comparison or validation.</p>
 *
 * @see SQLReaderConfiguration
 * @see IReader
 */
public class BuildInfoReader implements IReader {

    private SQLReaderConfiguration configuration;

    private static final HttpClient HTTP_CLIENT = HttpClientBuilder.create().build();
    private static final HttpGet HTTP_GET = new HttpGet();

    private final Logger log = LoggerFactory.getLogger(BuildInfoReader.class);
    private static final String DEFAULT_REGEXP = "build_number:(.*)";

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        setLocalConfiguration(configuration);
        try {
            return this.httpBuildVersion();
        } catch (IOException ex) {
            log.error(ResponseMessages.msg(20501, ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20501, ex.getMessage()));
        }
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String testConnection(Map<String, String> parameters) throws ReaderException {
        try {
            HttpResponse httpResponse = getResponse(parameters.get("connectionString"));
            return JSONUtils.statusMessage(10000, "Success!").toString();
        } catch (Exception ex) {
            throw new ReaderException(ResponseMessages.msg(20501, ex.getMessage()));
        }
    }

    private void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

    private List<Data> httpXpath(List<Script> scripts, String content) {
        List<Data> dataRecords = new ArrayList<>();

        for (Script script : scripts) {
            Data data = new Data();
            data.setDataType(DataType.SIMPLE);
            data.setName("BUILD");
            data.setExternalId("0");
            data.setTimeStamp(new Date());
            data.setContentType(DataContentType.PLAIN_TEXT);
            data.setContent(DataContentConverter.fromString(this.preparingTextWithRegexp(script.script, content)));
            dataRecords.add(data);
        }
        return dataRecords;
    }

    private HttpResponse getResponse(String connectionString) throws IOException, ReaderException {
        HttpResponse httpResponse = sendGetRequest(connectionString);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new ReaderException(ResponseMessages.msg(20501,
                    "Http GET reguest status code: " + String.valueOf(statusCode)));
        }
        return httpResponse;
    }

    private List<DataList> httpBuildVersion() throws IOException, ReaderException {
        List<DataList> resultList = new ArrayList<>();
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());
            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }

            HttpResponse httpResponse = getResponse(dataSource.getConnectionString());
            String content = getContent(httpResponse);

            List<Script> scripts = this.configuration.getScripts();
            List<Data> dataRecords = this.httpXpath(scripts, content);
            dataList.setDatas(dataRecords);
            resultList.add(dataList);
        }
        return resultList;
    }

    private String getContent(HttpResponse httpResponse) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }

    private String preparingTextWithRegexp(String regex, String text) {
        int grpNumber;
        Pattern pattern;
        if (regex.isEmpty()) {
            grpNumber = 1;
            pattern = Pattern.compile(DEFAULT_REGEXP);
        } else {
            grpNumber = 0;
            pattern = Pattern.compile(regex);
        }
        Matcher matcher = pattern.matcher(text);
        String returnText = "";
        while (matcher.find()) {
            returnText = matcher.group(grpNumber);
        }
        return returnText;
    }

    private HttpResponse sendGetRequest(String url) throws IOException {
        HTTP_GET.setURI(URI.create(url));
        return HTTP_CLIENT.execute(HTTP_GET);
    }
}
