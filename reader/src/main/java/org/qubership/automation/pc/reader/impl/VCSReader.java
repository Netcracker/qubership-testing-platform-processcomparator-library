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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.VCSProviderFactory;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.FactoryInstatiationException;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSContext;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSFile;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSProvider;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSReadRequest;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.qubership.automation.pc.reader.impl.vcs.VCSReadRequest;

/**
 * Reader implementation for interacting with Version Control Systems (VCS) such as Git or SVN.
 *
 * <p>
 * The {@code VCSReader} is designed to read files or directories from a VCS repository using a provided
 * configuration. It supports both simple and process reading modes:
 * </p>
 * <ul>
 *     <li><b>Simple mode</b> - reads files based on configured scripts and parameters.</li>
 *     <li><b>Process mode</b> - reads an entire structure or directory
 *     and packages all content into a single parent {@code Data} object.</li>
 * </ul>
 *
 * <p>
 * This reader relies on {@link IVCSProvider} to abstract access to various VCS implementations.
 * Configuration is provided via {@link SQLReaderConfiguration}, and each data source can include user credentials,
 * connection strings, and path/revision information.
 * </p>
 *
 * <p>
 * The content of the retrieved files is base64-encoded and wrapped into {@link Data} objects
 * that are grouped in {@link DataList}s and returned to the caller.
 * </p>
 */
public class VCSReader implements IReader {

    private SQLReaderConfiguration configuration;
    private IVCSProvider vcsClient;

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        this.setLocalConfiguration(configuration);
        return readOverall(false);
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        this.setLocalConfiguration(configuration);
        return readOverall(true);
    }

    private List<DataList> readOverall(boolean isProcess) throws ReaderException {
        List<DataList> resultList = new ArrayList<>();
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());

            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }
            List<Data> dataRecords = new ArrayList<>();

            if (isProcess) {
                Properties props = getProperties(dataSource.getDsParameters());
                List<Data> readedFiles = readRemote(
                        props, dataSource.getConnectionString(), dataSource.getUser(), dataSource.getPassword());
                Data parentData = new Data();
                parentData.setTimeStamp(new Date());
                parentData.setInternalId(UUID.randomUUID().toString());
                parentData.setChilds(readedFiles);
                parentData.setDataType(DataType.PROCESS);
                dataRecords.add(parentData);
            } else {
                for (SQLReaderConfiguration.Script script : this.configuration.getScripts()) {
                    Properties props = getProperties(
                            ScriptUtils.prepareParameterizedScript(
                                    script.script, this.configuration.getInputParameters()));
                    dataRecords.addAll(readRemote(props, dataSource.getConnectionString(),
                                    dataSource.getUser(), dataSource.getPassword()));
                }
            }
            dataList.setDatas(dataRecords);
            resultList.add(dataList);
        }
        return resultList;
    }

    private List<Data> readRemote(Properties props,
                                  String url,
                                  String userName,
                                  String password) throws ReaderException {
        List<Data> dataRecords = new ArrayList<>();
        //String targetContentType = script.fieldTypes.get("*").get("*");
        if (!props.containsKey(IVCSProvider.PROP_VCS_PROVIDER)) {
            throw new ReaderException("VCSProvider doesn't set");
        }
        try {
            String targetUrl = !StringUtils.isBlank(url) ? url : "";
            if (props.containsKey(IVCSProvider.PROP_PATH)) {
                targetUrl += props.getProperty(IVCSProvider.PROP_PATH);
            }
            String revision = props.containsKey(IVCSProvider.PROP_REVISION)
                    ? props.getProperty(IVCSProvider.PROP_REVISION) : "HEAD";
            IVCSProvider provider = VCSProviderFactory.getProvider(props.getProperty(IVCSProvider.PROP_VCS_PROVIDER));
            provider.init(props, userName, password);
            IVCSReadRequest request = new VCSReadRequest(targetUrl, revision);
            IVCSContext context = provider.readRemote(request);
            if (!context.getFiles().isEmpty()) {
                int orderNum = 1;
                for (IVCSFile file : context.getFiles()) {
                    Data convertedFile = convertToFile(file);
                    convertedFile.setOrderNum(orderNum);
                    dataRecords.add(convertedFile);
                    orderNum++;
                }
            }
        } catch (FactoryInstatiationException ex) {
            throw new ReaderException(ex);
        }
        return dataRecords;
    }

    @Override
    public String testConnection(Map<String, String> parameters) throws ReaderException {
        //To change body of generated methods, choose Tools | Templates.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void init(String vcsProvider, String userName, String password) throws ReaderException {

    }

    protected void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

    private Properties getProperties(String inputString) throws ReaderException {
        try {
            Properties result = new Properties();
            InputStream stream = new ByteArrayInputStream(inputString.getBytes(StandardCharsets.UTF_8));
            result.load(stream);
            return result;
        } catch (IOException ex) {
            throw new ReaderException(ex);
        }
    }

    private Properties getProperties(Map<String, String> inputParameters) {
        Properties result = new Properties();
        for (Map.Entry<String, String> entry : inputParameters.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private Data convertToFile(IVCSFile file) {
        Data resultData = new Data();
        String fileName = FilenameUtils.getBaseName(file.getPath());
        String ext = FilenameUtils.getExtension(file.getPath());
        resultData.setName(fileName);
        resultData.setDataType(DataType.SIMPLE);
        resultData.setInternalId(UUID.randomUUID().toString());
        resultData.setExternalId("0");
        resultData.setTimeStamp(file.getDate());
        resultData.setContentType((EnumUtils.isValidEnum(DataContentType.class, ext.toUpperCase()))
                ? DataContentType.valueOf(ext.toUpperCase()) : DataContentType.PRIMITIVES);
        String fileContent = inputStreamToString(file.getContent());
        if (fileContent == null) {
            fileContent = "[null]";
        }
        resultData.setContent(DataContentConverter.fromString(fileContent));
        return resultData;
    }

    private String inputStreamToString(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            StringBuilder sb = new StringBuilder();
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (counter != 0) {
                    sb.append(System.getProperty("line.separator"));
                }
                sb.append(line);
                counter++;
            }
            return sb.toString();
        } catch (IOException ex) {
            return null;
        }
    }
}
