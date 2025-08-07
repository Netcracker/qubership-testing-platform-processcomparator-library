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


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * {@code FTPReader} is a reader class for retrieving data from a remote FTP/SFTP server.
 * It supports reading individual files or multiple files by mask from specified directories.
 * The class implements the {@link IReader} interface and extends {@link AbstractFileReader}.
 * <p>
 * Main Features:
 * <ul>
 *   <li>Supports SFTP connections using username/password authentication</li>
 *   <li>Downloads files from remote servers based on path and optional filename mask</li>
 *   <li>Processes downloaded content into {@link Data} objects</li>
 *   <li>Supports parameterized script processing via {@code ScriptUtils}</li>
 * </ul>
 *
 * Usage Modes:
 * <ul>
 *   <li>{@code readSimple()} – executes file reading using static configuration</li>
 *   <li>{@code readProcess()} – allows use of input parameters and multiple steps</li>
 * </ul>
 *
 * Expected Parameters:
 * Parameters are extracted from {@link SQLReaderConfiguration} and associated {@link SQLDataSource} entries.
 * <ul>
 *   <li><b>connectionString</b>: Host and optional port, e.g. "ftp.example.com:22"</li>
 *   <li><b>dbUser</b>: Username for authentication</li>
 *   <li><b>dbPass</b>: Password for authentication</li>
 *   <li><b>path</b>: Path to the remote file or folder</li>
 *   <li><b>mask</b>: Regex pattern for matching filenames</li>
 *   <li><b>convert</b>: Optional conversion instruction for postprocessing file content</li>
 * </ul>
 *
 * Connection Testing:
 * The {@code testConnection()} method can be used to verify if the connection is successful
 * based on provided parameters.
 * Notes:
 * <ul>
 *   <li>Connection is established over SFTP (port 22 by default)</li>
 *   <li>Files are read as UTF-8 text with null characters removed</li>
 *   <li>Downloaded file contents can be further processed using {@code prepareContents()}</li>
 * </ul>
 *
 * @see IReader
 * @see SQLReaderConfiguration
 * @see SQLDataSource
 * @see ScriptUtils
 * @see DataList
 * @see Data
 */
public class FTPReader extends AbstractFileReader implements IReader {

    private String curHost = "";
    private String curUser = "";
    private String curPassword = "";
    private int curPort;
    private SQLReaderConfiguration configuration;

    private Session session = null;
    private ChannelSftp channelSftp = null;

    private final Logger log = LoggerFactory.getLogger(FTPReader.class);

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        setLocalConfiguration(configuration);
        return read(false);
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        setLocalConfiguration(configuration);
        return read(true);
    }

    @Override
    public String testConnection(Map<String, String> parameters) throws ReaderException {
        try {
            getStorage(parameters.get("connectionString"), parameters.get("dbUser"), parameters.get("dbPass"));
        } catch (NumberFormatException ex) {
            throw new ReaderException(ResponseMessages.msg(20501, "Invalid port number!"));
        }
        if (connect(curHost, curPort, curUser, curPassword)) {
            disconnect();
            return JSONUtils.statusMessage(10000, "Success!").toString();
        } else {
            throw new ReaderException(ResponseMessages.msg(20501, "Error while connecting to server!"));
        }
    }

    private boolean connect(String host, int port, String user, String pass) throws ReaderException {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(user, host,
                    port);
            session.setPassword(pass);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            return true;
        } catch (JSchException ex) {
            disconnect();
            throw new ReaderException(ex);
        }
    }

    private void disconnect() throws ReaderException {
        try {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception ex) {
            log.error(ResponseMessages.msg(20002,
                    "Error while disconnecting from host: " + ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20002,
                    "Error while disconnecting from host: " + ex.getMessage()));
        }
    }

    protected void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

    protected List<DataList> read(boolean isProcess) throws ReaderException {
        List<DataList> resultList = new ArrayList<>();
        String path;
        String mask;
        String convert = null;
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            getStorage(dataSource);
            if (!connect(curHost, curPort, curUser, curPassword)) {
                continue;
            }

            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());

            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }
            List<Data> dataRecords = new ArrayList<>();

            if (isProcess) {
                Map<String, String> params = dataSource.getDsParameters();
                path = (params.containsKey(PATH_NAME_COLUMN)) ? params.get(PATH_NAME_COLUMN).trim() : "";
                mask = (params.containsKey(MASK_NAME_COLUMN)) ? params.get(MASK_NAME_COLUMN).trim() : "";
                if (params.containsKey(STEP_NAME_COLUMN)) {
                    childName = params.get(STEP_NAME_COLUMN).trim();
                    if (StringUtils.isBlank(childName)) {
                        childName = DEFAULT_STEP_NAME;
                    }
                } else {
                    childName = DEFAULT_STEP_NAME;
                }
                if (params.containsKey(CONVERT_PROP)) {
                    convert = params.get(CONVERT_PROP).trim();
                }

                Data process = new Data();
                process.setDataType(DataType.PROCESS);
                process.setTimeStamp(new Date());
                process.setInternalId(UUID.randomUUID().toString());
                process.setChildren(new ArrayList<Data>());
                process.getChildren().addAll(downloadFiles(isProcess,
                        ScriptUtils
                                .prepareParameterizedScript(path, this.configuration.getInputParameters()),
                        ScriptUtils
                                .prepareParameterizedScript(mask, this.configuration.getInputParameters()), convert));
                dataRecords.add(process);
            } else {
                for (SQLReaderConfiguration.Script script : configuration.getScripts()) {
                    String cmdScript = ScriptUtils
                            .prepareParameterizedScript(script.script, this.configuration.getInputParameters());
                    if (cmdScript.contains(MASK_NAME_COLUMN) || cmdScript.contains(PATH_NAME_COLUMN)) {
                        mask = "";
                        path = "";
                        convert = null;
                        String[] items = cmdScript.split("\r\n|\n|\r");
                        for (int k = 0; k < items.length; k++) {
                            String s = items[k].trim();
                            if (!s.isEmpty()) {
                                if (s.startsWith(CONVERT_PROP + "=")) {
                                    convert = s.substring(CONVERT_PROP.length() + 1).trim();
                                } else if (s.startsWith(MASK_NAME_COLUMN + "=")) {
                                    mask = s.substring(MASK_NAME_COLUMN.length() + 1).trim();
                                } else if (s.startsWith(PATH_NAME_COLUMN + "=")) {
                                    path = s.substring(PATH_NAME_COLUMN.length() + 1).trim();
                                }
                            }
                        }
                    } else {
                        mask = "";
                        path = cmdScript;
                    }
                    dataRecords.addAll(downloadFiles(isProcess, path, mask, convert));
                }
            }
            dataList.setDatas(dataRecords);
            resultList.add(dataList);

            disconnect();
        }
        return resultList;
    }

    private List<Data> downloadFiles(boolean isProcess,
                                     String path,
                                     final String mask,
                                     String convert) throws ReaderException {
        List<Data> dataRecords = new ArrayList<>();
        if (!(mask.isEmpty() && path.isEmpty())) {
            // Self-limitation 1: if <path> points to remote folder then we require that mask is not empty
            // Self-limitation 2: no recursion into subdirectories
            if (!mask.isEmpty()) {
                if (!path.endsWith("/")) {
                    path = path + "/";
                }
                try {
                    Vector<ChannelSftp.LsEntry> files = channelSftp.ls(path);
                    for (ChannelSftp.LsEntry file : files) {
                        String fname = file.getFilename();
                        if (fname.matches(mask)) {
                            String contents = downloadFile(path + fname);
                            dataRecords.add(prepareContents(contents, convert, fname, isProcess,mask));
                        }
                    }
                } catch (SftpException ex) {
                    throw new ReaderException(ResponseMessages.msg(20501,
                            "Error while reading directory " + path + "! " + ex.getMessage()));
                }
            } else {
                String contents = downloadFile(path);
                dataRecords.add(prepareContents(contents, convert, path, isProcess,mask));
            }
        }
        return dataRecords;
    }

    protected void getStorage(String connectionString, String user, String passwd) throws NumberFormatException {
        /* dataSource.getConnectionString() - returns connection string like "host:port" */
        curUser = (StringUtils.isBlank(user)) ? "" : user.trim();
        curPassword = (StringUtils.isBlank(passwd)) ? "" : passwd.trim();
        if (StringUtils.isBlank(connectionString)) {
            curHost = "";
            curPort = 22;
        } else {
            String[] parts = connectionString.split(":");
            int i = parts.length;
            curHost = (i > 0) ? parts[0].trim() : "";
            curPort = (i > 1) ? Integer.parseInt(parts[1].trim()) : 22;
        }
    }

    protected void getStorage(SQLDataSource dataSource) {
        if (dataSource != null) {
            getStorage(dataSource.getConnectionString(), dataSource.getUser(), dataSource.getPassword());
        }
    }

    private String downloadFile(String remoteFile) throws ReaderException {
        try (BufferedInputStream inputStream = new BufferedInputStream(channelSftp.get(remoteFile))) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8).replaceAll("\u0000","");
        } catch (IOException | SftpException ex) {
            log.error(ResponseMessages.msg(20002,
                    "Error while downloading file: " + remoteFile + "\n" + ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20002,
                    "Error while downloading file: " + remoteFile + "\n" + ex.getMessage()));
        }
    }

    public InputStream getInputStreamForExcelData(Object configuration,
                                                  String filePath) throws ReaderException, SftpException {
        InputStream inputStream = null;
        this.getStorage((SQLDataSource) configuration);
        if (connect(curHost,curPort,curUser,curPassword)) {
            inputStream = this.channelSftp.get(filePath);
        }
        return inputStream;
    }
}
