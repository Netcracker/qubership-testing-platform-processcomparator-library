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

import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.anyString;
import static net.sf.expectit.matcher.Matchers.contains;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.keyboard.UserAuthKeyboardInteractiveFactory;
import org.apache.sshd.client.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.future.OpenFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.input.NoCloseInputStream;
import org.apache.sshd.common.util.io.output.NoCloseOutputStream;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration;
import org.qubership.automation.pc.configuration.SQLReaderConfiguration.Script;
import org.qubership.automation.pc.configuration.datasource.SQLDataSource;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.helpers.JSONUtils;
import org.qubership.automation.pc.core.helpers.ResponseMessages;
import org.qubership.automation.pc.core.helpers.ScriptUtils;
import org.qubership.automation.pc.core.interfaces.IReader;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;
import org.qubership.automation.pc.data.DataList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jregex.Matcher;
import jregex.Pattern;
import jregex.REFlags;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.Result;

/**
 * CLIReader is an implementation of the {@link IReader} interface that executes
 * CLI (Command Line Interface) commands on remote systems over SSH or Telnet protocols.
 *
 * <p>
 * The class supports running parameterized commands, parsing their output using
 * regular expressions, and structuring the results into {@code DataList} objects for further use.
 *
 * <p>
 * It can be configured with connection parameters, pre/post session commands, and
 * per-command expectations such as delays or prompt strings. It also provides optional
 * logging of CLI interactions to temporary files for debugging or auditing purposes.
 *
 * <p>
 * This reader is typically used for interacting with network devices (e.g., routers, switches)
 * or remote systems that expose administrative interfaces via command-line access.
 * </p>
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>SSH and Telnet protocol support</li>
 *   <li>Parameterized script execution with macro replacement</li>
 *   <li>Output parsing using regular expressions</li>
 *   <li>Structured output as {@code DataList} objects</li>
 *   <li>CLI output logging (optional)</li>
 * </ul>
 *
 * <p><strong>Note:</strong> Only SSH protocol is fully supported; Telnet may be limited
 * and protocol-specific behavior should be tested per use case.
 * </p>
 *
 * @see IReader
 * @see SQLReaderConfiguration
 * @see CommandConfig
 * @see DataList
 */
public class CLIReader implements IReader {
    // Old Variant: "\\{(.*?)\\}"
    private final Pattern inputParametersPattern = new Pattern("\\{([a-zA-Z0-9_\\.\\x20]+)\\}");
    private static final int MAX_BUF_SIZE = 512;

    private final String cliEol = "\r\n";

    private SQLReaderConfiguration configuration;

    private String cliHost = "";
    private String cliPort = "";
    private String cliProtocol = "";
    private String cliUser = "";
    private String cliPassword = "";
    private List<String> startCommands;
    private List<String> finishCommands;
    private static int defaultTimeout = 1000;
    private static int defaultCmdTimeout = 4000;

    private List<CommandConfig> listCommands;
    private static String defaultCmdWaitFor;
    private static int defaultCmdDelay;

    private SshClient client = null;
    private ClientSession session = null;
    private InputStream instr = null;
    private PrintStream outstr = null;
    private byte[] buff = new byte[MAX_BUF_SIZE];
    private boolean logCliOutput = false; // true; // Was set to true for debug purposes
    private FileWriter fileWriter = null;
    private String fileName = null;

    private TelnetClient telnet = null;

    private final Logger log = LoggerFactory.getLogger(CLIReader.class);

    @Override
    public List<DataList> readSimple(Object configuration) throws ReaderException {
        setLocalConfiguration(configuration);
        return read();
    }

    @Override
    public List<DataList> readProcess(Object configuration) throws ReaderException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String testConnection(Map<String, String> parameters) throws ReaderException {
        getStorage(parameters.get("connectionString"), parameters.get("dbUser"), parameters.get("dbPass"));
        if (cliConnect(cliProtocol, cliHost, cliPort, cliUser, cliPassword)) {
            cliDisconnect();
            return JSONUtils.statusMessage(10000, "Success!").toString();
        } else {
            throw new ReaderException(ResponseMessages.msg(20501, "Error while connecting to server!"));
        }
    }

    protected void setLocalConfiguration(Object configuration) {
        this.configuration = (SQLReaderConfiguration) configuration;
    }

    protected List<DataList> read() throws ReaderException {
        List<DataList> resultList = new ArrayList<>();
        String procOutput;

        //read parameters for each DataSource
        for (SQLDataSource dataSource : this.configuration.getDataSources()) {
            File tempfile;
            fileWriter = null;
            if (logCliOutput) {
                try {
                    tempfile = File.createTempFile("CLIReaderlog_"
                            + DateFormatUtils.format(new Date(), "yyyyMMdd_HHmmss"), ".txt");
                    fileName = tempfile.getPath();
                    fileWriter = new FileWriter(tempfile);
                    log.info("Temp CLI Log: " + fileName);
                } catch (IOException ex) {
                    log.error(ResponseMessages.msg(20002,
                            "Exception opening tempFile: " + fileName + " " + ex.getMessage()));
                    throw new ReaderException(ResponseMessages.msg(20002,
                            "Exception opening tempFile: " + fileName + " " + ex.getMessage()));
                }
            }

            //Set CLI connection parameters for data source
            getStorage(dataSource);

            //Connect to server... only SSH protocol is supported now - 2016-10-10
            if (!cliConnect(cliProtocol, cliHost, cliPort, cliUser, cliPassword)) {
                continue;
            }

            String s;
            /* For Huawey devices - some start commands should be executed immediately after successful connect  */
            /* They are configured via datasource configuration (connectionString field) */
            for (int idx = 0; idx < startCommands.size(); idx++) {
                s = sendCommand(ScriptUtils.prepareParameterizedScript(startCommands.get(idx),
                        this.configuration.getInputParameters()), defaultCmdWaitFor, defaultCmdDelay, true);
            }

            DataList dataList = new DataList();
            dataList.setId((dataSource.getId().isEmpty()) ? UUID.randomUUID().toString() : dataSource.getId());

            if (dataSource.getName() == null || dataSource.getName().isEmpty()) {
                dataList.setName(dataSource.getConnectionString());
            }
            List<Data> dataRecords = new ArrayList<>();

            for (Script script : configuration.getScripts()) {
                //get parameterized script
                String cmdScript = ScriptUtils.prepareParameterizedScript(script.script,
                        this.configuration.getInputParameters());
                StringBuilder totalProcOutput = new StringBuilder(); /* String for collecting script output */

                listCommands = parseCommands(cmdScript);
                boolean childsFilled = false;
                for (CommandConfig cfg : listCommands) {
                    procOutput = sendCommand(cfg.cmd, cfg.waitFor, cfg.delay, cfg.usepty);
                    if (cfg.regexps.isEmpty()) {
                        /*if( totalProcOutput.length() > 0 ) {
                            totalProcOutput.append("\n");
                        }*/
                        totalProcOutput.append(procOutput);
                    } else {
                        procOutput = procOutput.replace(cliEol, "\n");
                        for (int j = 0; j < cfg.regexps.size(); j++) {
                            StringBuilder strResult = new StringBuilder();
                            if (cfg.regexps.get(j).isEmpty()) {
                                strResult.append(cfg.regexpNames.get(j)).append("\n");
                            } else {
                                strResult.append(cfg.regexpNames.get(j));
                                try {
                                    Pattern r = new Pattern(cfg.regexps.get(j), REFlags.MULTILINE);
                                    Matcher m = r.matcher(procOutput);
                                    boolean firstRow = true;
                                    while (m.find()) {
                                        if (firstRow) {
                                            firstRow = false;
                                        } else {
                                            strResult.append("\n");
                                        }
                                        for (int i = 1; i < m.groupCount(); i++) {
                                            if (m.group(i) == null) {
                                                continue;
                                            }
                                            strResult.append(m.group(i));
                                        }
                                    }
                                } catch (Exception ex) {
                                    logCliError(fileWriter, "CLI output parsing error: "
                                            + cfg.regexps.get(j) + " " + ex.getMessage(), fileName);
                                }
                            }

                            if (!cfg.childNames.get(j).isEmpty()) {
                                addStr(dataRecords, cfg.childNames.get(j), strResult.toString(),
                                        DataContentType.PRIMITIVES, true);
                                childsFilled = true;
                            }
                            totalProcOutput.append(strResult);
                        }
                    }
                }
                if (!childsFilled) {
                    addStr(dataRecords, "output", totalProcOutput.toString(), DataContentType.PRIMITIVES,
                            true);
                }
            }

            // Data content should be url-encoded & base64-encoded
            // because CLI device output could contain special characters
            encodeData(dataRecords);
            dataList.setDatas(dataRecords);
            resultList.add(dataList);

            /* Disconnect from CLI-server */
            /* For Huawey devices - some finish commands should be executed before disconnect  */
            /* Configured via datasource configuration (connectionString field) */
            for (int idx = 0; idx < finishCommands.size(); idx++) {
                s = sendCommand(ScriptUtils.prepareParameterizedScript(finishCommands.get(idx),
                        this.configuration.getInputParameters()), defaultCmdWaitFor, defaultCmdDelay, true);
            }

            cliDisconnect();

            if (logCliOutput) {
                try {
                    fileWriter.close();
                    log.info("Closed Temp CLI Log:" + fileName);
                } catch (IOException ex) {
                    log.error(ResponseMessages.msg(20002,
                            "Exception closing tempFile: " + ex.getMessage()));
                    throw new ReaderException(ResponseMessages.msg(20002,
                            "Exception closing tempFile: " + ex.getMessage()));
                }
            }
        }
        return resultList;
    }

    private void addStr(List<Data> dataRecords,
                        String childName,
                        String strResult,
                        DataContentType contentType,
                        boolean append) {
        boolean found = false;

        if (append) {
            for (int i = 0; i < dataRecords.size(); i++) {
                Data dt = dataRecords.get(i);
                if (dt.getName().equals(childName)) {
                    dt.setContent(dt.getContent() + strResult);
                    found = true;
                    break;
                }
            }
        }

        if (!found || !append) {
            Data data = new Data();
            data.setName(childName);
            data.setContentType(contentType); // DataContentType.PRIMITIVES
            data.setContent(strResult);
            data.setDataType(DataType.SIMPLE);
            data.setExternalId("0");
            data.setTimeStamp(new Date());
            dataRecords.add(data);
        }
    }

    protected void getStorage(SQLDataSource dataSource) {
        if (dataSource != null) {
            getStorage(dataSource.getConnectionString(), dataSource.getUser(), dataSource.getPassword());
        }
    }

    protected void getStorage(String connectionString, String user, String passwd) {
        cliUser = (StringUtils.isBlank(user)) ? "" : user.trim();
        cliPassword = (StringUtils.isBlank(passwd)) ? "" : passwd.trim();
        defaultCmdDelay = defaultTimeout;
        startCommands = new ArrayList();
        finishCommands = new ArrayList();
        if (StringUtils.isBlank(connectionString)) {
            cliProtocol = "";
            cliHost = "";
            cliPort = "";
        } else {
            /* dataSource.getConnectionString() - returns connection string like "protocol:host:port" */
            String[] parts = connectionString.split(":");
            int i = parts.length;

            cliProtocol = (i > 0) ? parts[0].trim() : "";
            cliHost = (i > 1) ? parts[1].trim() : "";
            cliPort = (i > 2) ? parts[2].trim() : "";
            for (int k = 3; k < i; k++) {
                getOptionalCommands(parts[k]);
            }
        }
    }

    private void getOptionalCommands(String cmdList) {
        if (cmdList.substring(0, 6).equalsIgnoreCase("start=")) {
            startCommands.addAll(Arrays.asList(cmdList.substring(6).split(";")));
        } else if (cmdList.substring(0, 7).equalsIgnoreCase("finish=")) {
            finishCommands.addAll(Arrays.asList(cmdList.substring(7).split(";")));
        } else if (cmdList.substring(0, 8).equalsIgnoreCase("WAITFOR=")) {
            defaultCmdWaitFor = cmdList.substring(8);
        } else if (cmdList.substring(0, 6).equalsIgnoreCase("DELAY=")) {
            String s = cmdList.substring(6);
            try {
                defaultCmdDelay = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                defaultCmdDelay = defaultTimeout;
            }
        }
    }

    private boolean cliConnect(String protocol,
                               String host,
                               String port,
                               String username,
                               String passwd) throws ReaderException {
        try {
            if (protocol.equalsIgnoreCase("ssh")) {
                client = SshClient.setUpDefaultClient(); //JSch jsch = new JSch();
                client.setUserAuthFactories(Arrays.asList(UserAuthPasswordFactory.INSTANCE,
                        UserAuthPublicKeyFactory.INSTANCE, UserAuthKeyboardInteractiveFactory.INSTANCE));
                //client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
                client.start();
                final ConnectFuture connectFuture = client.connect(username, host,
                        Integer.parseInt(port)).verify(12000);
                if (connectFuture.isConnected()) {
                    session = connectFuture.getSession();
                    if (session != null) {
                        session.setUsername(username);
                        session.addPasswordIdentity(passwd);
                        final AuthFuture authFuture = session.auth().verify(12000);
                        if (!authFuture.isSuccess()) {
                            Throwable ex = authFuture.getException();
                            throw new IOException(ex);
                        }
                    } else {
                        throw new Exception("Invalid session!");
                    }
                } else {
                    Throwable ex = connectFuture.getException();
                    throw new IOException(ex);
                }
            } else if (protocol.equalsIgnoreCase("telnet")) {
                telnet = new TelnetClient();
                telnet.connect(host, Integer.parseInt(port));
                if (telnet.isConnected()) {
                    instr = telnet.getInputStream();
                    outstr = new PrintStream(telnet.getOutputStream());
                    instr.read(buff, 0, MAX_BUF_SIZE); // Read all before login prompt
                    outstr.println(username);
                    outstr.flush();
                    instr.read(buff, 0, MAX_BUF_SIZE); // Read all before password prompt
                    outstr.println(passwd);
                    outstr.flush();
                    // Read all greetings after successful login. Currently, not analyzed
                    instr.read(buff, 0, MAX_BUF_SIZE);
                } else {
                    return false; // May be exception should be thrown
                }
            } else {
                return false; //Unsupported or empty protocol. May be exception should be thrown 
            }
        } catch (Exception ex) {
            if (session != null) {
                try {
                    session.close();
                    session = null;
                } catch (IOException ioex) {
                    throw new RuntimeException(ioex);
                }
            }
            if (client != null) {
                client.close(false);
                client = null;
            }
            log.error(ResponseMessages.msg(20002, "Error while connecting to CLI: " + ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20002, "Error while connecting to CLI: "
                    + ex.getMessage()));
        }
        return true;
    }

    private void cliDisconnect() throws ReaderException {
        try {
            if (cliProtocol.equalsIgnoreCase("ssh")) {
                if (session != null) {
                    session.close();
                    session = null;
                }
                if (client != null) {
                    client.close(false);
                    client = null;
                }
            } else if (cliProtocol.equalsIgnoreCase("telnet")) {
                if (telnet != null) {
                    telnet.disconnect();
                    telnet = null;
                }
            }
        } catch (Exception ex) {
            log.error(ResponseMessages.msg(20002,
                    "Error while dicconnecting from CLI: " + ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20002,
                    "Error while dicconnecting from CLI: " + ex.getMessage()));
        }
    }

    private String sendCommand(String command, String cmdWaitFor, int cmdDelay, boolean usePty) throws ReaderException {
        StringBuilder outputBuffer = new StringBuilder();
        long expectDelay = 10L;
        long beforeDelay = 5L;
        if (cmdDelay >= 1000) { // Delay in milliseconds; 1000 millis = 1 sec.
            if (cmdDelay / 1000 > expectDelay) {
                expectDelay = cmdDelay / 1000;
            }
        } else if (cmdDelay >= 1) { // Delay in seconds (user mistake but we are user-friendly or not?)
            if (cmdDelay > expectDelay) {
                expectDelay = cmdDelay;
            }
        }
        try {
            ChannelShell channel = session.createShellChannel();
            if (!usePty) {
                // A such setting in case of usePTY == true is unnecessary because channel uses pty by default
                channel.setUsePty(usePty);
            } else {
                channel.setPtyType("vt100");
                channel.setPtyColumns(640);
            }
            //channel.setPtyWidth(500);
            OpenFuture openedChannel = channel.open().verify(beforeDelay, TimeUnit.SECONDS);
            if (!openedChannel.isOpened()) {
                Throwable ex = openedChannel.getException();
                throw new IOException(ex);
            }
            Expect expect = new ExpectBuilder()
                    .withOutput(new NoCloseOutputStream(channel.getInvertedIn()))
                    .withInputs(new NoCloseInputStream(channel.getInvertedOut()),
                            new NoCloseInputStream(channel.getInvertedErr()))
                    .withEchoInput(System.out)
                    .withEchoOutput(System.err)
                    .withInputFilters(removeColors(), removeNonPrintable())
                    .withExceptionOnFailure()
                    .withTimeout(beforeDelay, TimeUnit.SECONDS)
                    .build();
            Result res;
            String resInput = "";
            String resBefore = "";
            if (StringUtils.isBlank(cmdWaitFor)) {
                while (true) {
                    try {
                        res = expect.expect(anyString());
                        resInput = res.getInput();
                        resBefore = res.getBefore();
                    } catch (Exception ex) {
                        break;
                    }
                }
            } else {
                try {
                    res = expect.expect(contains(cmdWaitFor));
                    resInput = res.getInput();
                    resBefore = res.getBefore();
                } catch (Exception ex) {
                    // 
                }
            }

            expect = expect.withTimeout(expectDelay, TimeUnit.SECONDS);
            expect.sendLine(command);
            res = null;
            if (StringUtils.isBlank(cmdWaitFor)) {
                while (true) {
                    try {
                        res = expect.expect(anyString());
                        outputBuffer.append(res.getInput());
                    } catch (Exception ex) {
                        break;
                    }
                }
            } else {
                try {
                    res = expect.expect(contains(cmdWaitFor));
                    outputBuffer.append(res.getBefore());
                } catch (Exception ex) {
                    if (res != null) {
                        outputBuffer.append(res.getInput());
                    }
                    throw new IOException(ex.getMessage());
                }
            }
            channel.close(false);
        } catch (IOException ex) {
            log.error(ResponseMessages.msg(20002,
                    "Exception sending command (" + command + "): " + ex.getMessage()));
            //Maybe discursive decision, but we do NOT throw an exception here and send
            // the exception message to read result instead
            //throw new ReaderException(ResponseMessages.msg(20002,
            // "Exception sending command (" + command + "): " + ex.getMessage()));
            outputBuffer.append("Exception sending command (" + command + "): " + ex.getMessage());
        }
        return outputBuffer.toString().replace("\r", ""); // skip invalid newline chars
    }

    private void encodeData(List<Data> dataRecords) throws ReaderException {
        try {
            for (int i = 0; i < dataRecords.size(); i++) {
                Data dt = dataRecords.get(i);
                if (dt.getContentType() == DataContentType.PLAIN_TEXT) {
                    String s = StringEscapeUtils.escapeJson(dt.getContent());
                    dt.setContent(DataContentConverter.fromString(s));
                } else {
                    // Maybe should pass through escapeJson too
                    dt.setContent(DataContentConverter.fromString(dt.getContent()));
                }
            }
        } catch (Exception ex) {
            logCliError(fileWriter, "Error while encoding data read: " + ex.getMessage(), fileName);
            log.error(ResponseMessages.msg(20002,
                    "Error while encoding data read: " + ex.getMessage()));
            throw new ReaderException(ResponseMessages.msg(20002,
                    "Error while encoding data read: " + ex.getMessage()));
        }
    }

    private void logCliError(FileWriter fileWriter, String err, String fileName) throws ReaderException {
        if (logCliOutput) {
            try {
                fileWriter.write(cliEol + err + cliEol);
                fileWriter.flush();
            } catch (IOException ex) {
                log.error(ResponseMessages.msg(20002,
                        "Exception writing tempFile: " + fileName + " " + err + " " + ex.getMessage()));
                throw new ReaderException(ResponseMessages.msg(20002,
                        "Exception writing tempFile: " + fileName + " " + err + " " + ex.getMessage()));
            }
        }
    }

    private List<CommandConfig> parseCommands(String cmdScript) {
        List<CommandConfig> result = new ArrayList<>();
        String cmd = cmdScript;
        while (true) {
            CommandConfig item = new CommandConfig(cmd);
            if (!item.cmd.isEmpty()) {
                result.add(item);
            }
            if (StringUtils.isBlank(item.nextCommands)) {
                break;
            } else {
                cmd = item.nextCommands;
            }
        }
        return result;
    }

    /* If cmdScript NOT starts with "CMD=" - execute cmdScript as one atomic OS command */
    /* else - cmdScript consists of seguence like this:
        CMD=command
        rest of command or other command
        REGEX=FIB Used: = Total Route Prefix Count\s+:\s+?(\d+)
        Public FIB Used: = Public Route Prefix Count\s+:\s+?(\d+)
        CMD=command
        ...
        In this case we split cmdScript to commands and regexps (regexps are regexp-name and regexp itself)
    */

    public static class CommandConfig {
        public String cmd;      // command itself
        public String waitFor;  // String we are waiting in channel inputStream before and after command
        // Delay (in 1/10000 of second; delay = 10000 = 1 sec.) Time to wait before command execution
        public int delay;
        public boolean usepty;  // if =true (default), ssh channel uses pty. Otherwise (=false) - doesn't use
        public List<String> childNames = new ArrayList<>();
        public List<String> regexpNames = new ArrayList<>();
        // Maybe later it will be 'List<List<String>>' (Dmitry Shipilov mentioned the possibility
        // to apply regexp not only to command's output but to the previous regexp's output too)
        public List<String> regexps = new ArrayList<>();
        // For parsing purposes: If nextCommands isn't empty after parsing 1st command
        // then we should parse 2nd command from nextCommands and so on
        public String nextCommands;

        private static final String START_ITEM = "CMD=";
        private static final String WAITFOR_ITEM = "WAITFOR=";
        private static final String DELAY_ITEM = "DELAY=";
        private static final String REGEX_ITEM = "REGEX=";
        private static final String USEPTY_ITEM = "USEPTY=";
        private static final String NULL_VALUE = "[null]";

        public CommandConfig() {
            this.usepty = true; // default, for backward compatibility
        }

        public CommandConfig(String cmdConfig) {
            this.usepty = true; // default, for backward compatibility

            // If cmdConfig doesn't start with <startItem>
            // then the whole cmdConfig is a 'cmd' (waitFor, delay, regexps and nextCommands are nulls)
            if (cmdConfig.startsWith(START_ITEM)) {
                String cmdTail = cmdConfig.substring(START_ITEM.length()).trim();

                // If cmdTail doesn't contain <startItem> then there is only one command
                // - command we are currently parsing
                int i = cmdTail.indexOf(START_ITEM);
                if (i != -1) {
                    this.nextCommands = cmdTail.substring(i);
                    cmdTail = cmdTail.substring(0, i);
                }

                this.cmd = "";

                if (!StringUtils.isBlank(defaultCmdWaitFor)) {
                    this.waitFor = defaultCmdWaitFor;
                }
                if (defaultCmdDelay > 0) {
                    this.delay = defaultCmdDelay;
                }

                // If cmdTail doesn't contain "WAITFOR=","DELAY=" or "REGEX=" then the whole cmdTail is a 'cmd'
                String[] rows = cmdTail.split("\r\n|\n|\r");
                boolean regexpRows = false;
                boolean cmdRows = true;
                for (String row : rows) {
                    boolean isWaitFor = row.startsWith(WAITFOR_ITEM);
                    boolean isDelay = (isWaitFor) ? false : row.startsWith(DELAY_ITEM);
                    boolean isUsePty = (isWaitFor || isDelay) ? false : row.startsWith(USEPTY_ITEM);
                    boolean isRegex = (isWaitFor || isDelay || isUsePty) ? false : row.startsWith(REGEX_ITEM);
                    if (!isWaitFor && !isDelay && !isUsePty && !isRegex) {
                        if (cmdRows) {
                            this.cmd = (this.cmd.isEmpty()) ? row : this.cmd + " " + row;
                            continue;
                        }
                    } else {
                        cmdRows = false;
                        regexpRows = false;
                        if (isWaitFor) {
                            this.waitFor = row.substring(WAITFOR_ITEM.length());
                            if (this.waitFor.isEmpty()) {
                                this.waitFor = null;
                            }
                            continue;
                        } else if (isDelay) {
                            String strcmdDelay = row.substring(DELAY_ITEM.length());
                            try {
                                this.delay = Integer.parseInt(strcmdDelay);
                            } catch (NumberFormatException ex) {
                                this.delay = defaultCmdTimeout;
                            }
                            continue;
                        } else if (isUsePty) {
                            String strcmdUsePty = row.substring(USEPTY_ITEM.length());
                            try {
                                this.usepty = Boolean.parseBoolean(strcmdUsePty);
                            } catch (Exception ex) {
                                this.usepty = true;
                            }
                            continue;
                        } else {
                            row = row.substring(REGEX_ITEM.length());
                            regexpRows = true;
                        }
                    }
                    if (regexpRows) {
                        row = row.trim();
                        if (!row.isEmpty()) {
                            int ind = row.indexOf("=");
                            String childName;
                            String regName;
                            String regValue;
                            if (ind == -1) {
                                childName = "";
                                regName = "";
                                regValue = row;
                            } else {
                                childName = "";
                                regName = row.substring(0, ind);
                                int k = regName.indexOf("/");
                                if (k != -1) {
                                    childName = regName.substring(0, k).trim();
                                    regName = regName.substring(k + 1);
                                }
                                regValue = row.substring(ind + 1).trim();
                            }
                            if (!regName.isEmpty() || !regValue.isEmpty()) {
                                childNames.add(childName);
                                regexpNames.add(regName);
                                regexps.add(regValue);
                            }
                        }
                    }
                }
            } else {
                this.cmd = cmdConfig;
            }
        }
    }
}
