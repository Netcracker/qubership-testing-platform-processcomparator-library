/*******************************************************************************
 * Copyright (c) 2004, 2006 svnClientAdapter project and others.
 * </p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * </p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/

package org.tigris.subversion.svnclientadapter.commandline;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Common superclass for both SvnCommandLine and SvnAdminCommandLine.
 *
 * @author Philip Schatz (schatz at tigris)
 * @author Cédric Chabanois (cchabanois at no-log.org)
 * @author Daniel Rall
 */
abstract class CommandLine {

    protected String commandName;
    protected CmdLineNotificationHandler notificationHandler;
    protected Process process;

    protected CommandLine(String commandName, CmdLineNotificationHandler notificationHandler) {
        this.commandName = commandName;
        this.notificationHandler = notificationHandler;
    }

    String version() throws CmdLineException {
        CmdArguments args = new CmdArguments();
        args.add("--version");
        return execString(args, false);
    }

    /**
     * Executes the given svn command and returns the corresponding
     * <code>Process</code> object.
     *
     * @param svnArguments The command-line arguments to execute.
     */
    private Process execProcess(CmdArguments svnArguments)
            throws CmdLineException {
        // We add "svn" or "svnadmin" to the arguments (as
        // appropriate), and convert it to an array of strings.
        int svnArgsLen = svnArguments.size();
        String[] cmdline = new String[svnArgsLen + 1];
        cmdline[0] = commandName;

        StringBuffer svnCommand = new StringBuffer();
        boolean nextIsPassword = false;

        for (int i = 0; i < svnArgsLen; i++) {
            if (i != 0) {
                svnCommand.append(' ');
            }

            Object arg = svnArguments.get(i);
            if (arg != null) {
                arg = arg.toString();
            }

            if ("".equals(arg)) {
                arg = "\"\"";
            }

            if (nextIsPassword) {
                // Avoid showing the password on the console.
                svnCommand.append("*******");
                nextIsPassword = false;
            } else {
                svnCommand.append(arg);
            }

            if ("--password".equals(arg)) {
                nextIsPassword = true;
            }

            // Regardless of the data type passed in via svnArguments,
            // at this point we expect to have a String object.
            cmdline[i + 1] = (String) arg;
        }
        notificationHandler.logCommandLine(svnCommand.toString());

        // Run the command, and return the associated Process object.
        try {
            return process = Runtime.getRuntime().exec(cmdline, getEnvironmentVariables());
        } catch (IOException e) {
            throw new CmdLineException(e);
        }
    }

    /**
     * Get environment variables to be set when invoking the command-line.
     * Includes <code>LANG</code> and <code>LC_ALL</code> so Subversion's output is not localized.
     * <code>Systemroot</code> is required on windows platform.
     * Without this variable present, the windows' DNS resolver does not work.
     * <code>APR_ICONV_PATH</code> is required on windows platform for UTF-8 translation.
     * The <code>PATH</code> is there, well, just to be sure ;-)
     */
    protected String[] getEnvironmentVariables() {
        final String path = CmdLineClientAdapter.getEnvironmentVariable("PATH");
        final String systemRoot = CmdLineClientAdapter.getEnvironmentVariable("SystemRoot");
        final String aprIconv = CmdLineClientAdapter.getEnvironmentVariable("APR_ICONV_PATH");
        int i = 3;
        if (path != null) {
            i++;
        }
        if (systemRoot != null) {
            i++;
        }
        if (aprIconv != null) {
            i++;
        }
        String[] lcVars = getLocaleVariables();
        String[] env = new String[i + lcVars.length];
        i = 0;
        //Clear the LC_ALL, we're going to override the LC_MESSAGES and LC_TIME
        env[i] = "LC_ALL=";
        i++;
        //Set the LC_MESSAGES to "C" to avoid translated svn output. (We're parsing the english one)
        env[i] = "LC_MESSAGES=C";
        i++;
        env[i] = "LC_TIME=C";
        i++;
        if (path != null) {
            env[i] = "PATH=" + path;
            i++;
        }
        if (systemRoot != null) {
            env[i] = "SystemRoot=" + systemRoot;
            i++;
        }
        if (aprIconv != null) {
            env[i] = "APR_ICONV_PATH=" + aprIconv;
            i++;
        }
        //Add the remaining LC vars
        for (int j = 0; j < lcVars.length; j++) {
            env[i] = lcVars[j];
            i++;
        }
        return env;
    }

    private String[] getLocaleVariables() {
        String lcAll = CmdLineClientAdapter.getEnvironmentVariable("LC_ALL");
        if ((lcAll == null) || (lcAll.length() == 0)) {
            lcAll = CmdLineClientAdapter.getEnvironmentVariable("LANG");
            if (lcAll == null) {
                lcAll = "";
            }
        }

        final String[] lcVarNames = new String[]{
                "LC_CTYPE",
                "LC_NUMERIC",
                "LC_COLLATE",
                "LC_MONETARY",
                "LC_PAPER",
                "LC_NAME",
                "LC_ADDRESS",
                "LC_TELEPHONE",
                "LC_MEASUREMENT",
                "LC_IDENTIFICATION"};

        List variables = new ArrayList(lcVarNames.length);
        for (int i = 0; i < lcVarNames.length; i++) {
            String varValue = CmdLineClientAdapter.getEnvironmentVariable(lcVarNames[i]);
            variables.add(lcVarNames[i] + "=" + ((varValue != null) ? varValue : lcAll));
        }
        return (String[]) variables.toArray(new String[variables.size()]);
    }

    /**
     * Pumps the standard output and error streams from a running process until both are fully read.
     * </p>
     * This method starts separate threads to asynchronously consume both the output and error
     * streams of the provided process. It blocks until both threads finish reading. All streams
     * are closed afterwards.
     *
     * @param proc       the running process whose streams are to be pumped
     * @param outPumper  stream pumper for the process's standard output
     * @param errPumper  stream pumper for the process's standard error
     */
    private void pumpProcessStreams(Process proc, StreamPumper outPumper,
                                    StreamPumper errPumper) {
        new Thread(outPumper).start();
        new Thread(errPumper).start();

        try {
            outPumper.waitFor();
            errPumper.waitFor();
        } catch (InterruptedException ignored) {
            notificationHandler.logError("Command output processing interrupted !");
        } finally {
            try {
                proc.getInputStream().close();
                proc.getOutputStream().close();
                proc.getErrorStream().close();
            } catch (IOException ioex) {
                //Just ignore. Exception when closing the stream.
            }
        }
    }

    /**
     * Executes a Subversion command using the specified arguments and returns its standard output as a string.
     * </p>
     * The command is executed as an external process, and its output is read and returned. If the error
     * stream contains any output, a {@link CmdLineException} is thrown. Optionally coalesces lines of standard
     * output based on the {@code coalesceLines} flag.
     *
     * @param svnArguments    the command-line arguments to execute
     * @param coalesceLines   whether to merge lines of output into a single logical result
     * @return the standard output from the executed command
     * @throws CmdLineException if any output is received on the standard error stream
     */
    protected String execString(CmdArguments svnArguments, boolean coalesceLines)
            throws CmdLineException {
        Process proc = execProcess(svnArguments);
        StreamPumper outPumper =
                new CharacterStreamPumper(proc.getInputStream(), coalesceLines);
        StreamPumper errPumper =
                new CharacterStreamPumper(proc.getErrorStream(), false);
        pumpProcessStreams(proc, outPumper, errPumper);

        try {
            String errMessage = errPumper.toString();
            if (errMessage.length() > 0) {
                throw new CmdLineException(errMessage);
            }
            String outputString = outPumper.toString();

            notifyFromSvnOutput(outputString);
            return outputString;
        } catch (CmdLineException e) {
            notificationHandler.logException(e);
            throw e;
        }
    }

    /**
     * Runs the process and returns the results.
     *
     * @param svnArguments The arguments to pass to the command-line
     *                     binary.
     * @param assumeUtf8   Whether the output of the command should be
     *                     treated as UTF-8 (as opposed to the JVM's default encoding).
     * @return String
     */
    protected byte[] execBytes(CmdArguments svnArguments, boolean assumeUtf8)
            throws CmdLineException {
        Process proc = execProcess(svnArguments);
        ByteStreamPumper outPumper =
                new ByteStreamPumper(proc.getInputStream());
        StreamPumper errPumper =
                new CharacterStreamPumper(proc.getErrorStream(), false);
        pumpProcessStreams(proc, outPumper, errPumper);

        try {
            String errMessage = errPumper.toString();
            if (errMessage.length() > 0) {
                throw new CmdLineException(errMessage);
            }
            byte[] bytes = outPumper.getBytes();

            String notifyMessage = "";
            if (assumeUtf8) {
                try {
                    notifyMessage = new String(bytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // It is guaranteed to be there!
                }
            } else {
                // This uses the default charset, which is likely
                // wrong if we are trying to get the bytes, anyway...
                notifyMessage = new String(bytes);
            }
            notifyFromSvnOutput(notifyMessage);

            return bytes;
        } catch (CmdLineException e) {
            notificationHandler.logException(e);
            throw e;
        }
    }

    /**
     * Executes a Subversion command with the specified arguments and discards any output.
     * </p>
     * This method runs the command-line process but does not return or process its output.
     * Any errors encountered during execution are captured and result in a {@link CmdLineException}.
     *
     * @param svnArguments the command-line arguments to execute
     * @throws CmdLineException if the process writes to the standard error stream or fails to execute
     */
    protected void execVoid(CmdArguments svnArguments) throws CmdLineException {
        execString(svnArguments, false);
    }

    //TODO check the deprecation

    /**
     * Runs the process and returns the results.
     *
     * @param svnArguments The arguments to pass to the command-line
     *                     binary.
     * @return the InputStream on commads result. Caller has to close it explicitelly().
     * @deprecated this does not sound as a good idea. Check if we're able to live without it.
     */
    protected InputStream execInputStream(CmdArguments svnArguments)
            throws CmdLineException {
        Process proc = execProcess(svnArguments);
        try {
            proc.getOutputStream().close();
            proc.getErrorStream().close();
            //InputStream has to be closed by caller !
        } catch (IOException ioex) {
            //Just ignore. Exception when closing the stream.
        }
        return proc.getInputStream();
    }

    /**
     * Notifies the registered listeners with messages parsed from the SVN command output.
     *
     * @summary Parses the provided SVN output line by line and notifies listeners.
     *          All lines except the last one are treated as intermediate messages,
     *          while the last line is treated as the final "completed" message.
     *
     * @param svnOutput the full output string returned by the SVN command
     */
    protected void notifyFromSvnOutput(String svnOutput) {
        StringTokenizer st = new StringTokenizer(svnOutput, Helper.NEWLINE);
        int size = st.countTokens();
        //do everything but the last line
        for (int i = 1; i < size; i++) {
            notificationHandler.logMessage(st.nextToken());
        }

        //log the last line as the completed message.
        if (size > 0) {
            notificationHandler.logCompleted(st.nextToken());
        }
    }


    protected void stopProcess() {
        try {
            process.getInputStream().close();
            process.getOutputStream().close();
            process.getErrorStream().close();
        } catch (IOException ioex) {
            //Just ignore. Closing streams.
        }
        process.destroy();
    }

    /**
     * Pulls all the data out of a stream.  Inspired by Ant's
     * StreamPumper (by Robert Field).
     */
    private abstract static class StreamPumper implements Runnable {
        private boolean finished;

        /**
         * Constructor.
         */
        protected StreamPumper() {
            super();
        }

        /**
         * Copies data from the input stream to the internal buffer.
         * Terminates as soon as the input stream is closed, or an
         * error occurs.
         */
        public void run() {
            synchronized (this) {
                // Just in case this object is reused in the future.
                this.finished = false;
            }

            try {
                pumpStream();
            } finally {
                synchronized (this) {
                    this.finished = true;
                    notify();
                }
            }
        }

        /**
         * Called by {@link #run()} to pull the data out of the
         * stream.
         */
        protected abstract void pumpStream();

        /**
         * Tells whether the end of the stream has been reached.
         *
         * @return true is the stream has been exhausted.
         **/
        public synchronized boolean isFinished() {
            return this.finished;
        }

        /**
         * Waits until the stream pumper has completed processing.
         *
         * @summary Blocks the calling thread until the stream pumper signals completion.
         *          This method continuously waits until {@link #isFinished()} returns true.
         *
         * @throws InterruptedException if the current thread is interrupted while waiting
         * @see #isFinished()
         */
        public synchronized void waitFor() throws InterruptedException {
            while (!isFinished()) {
                wait();
            }
        }

    }

    /**
     * Extracts character data from streams.
     */
    private static class CharacterStreamPumper extends StreamPumper {
        private BufferedReader reader;
        private StringBuffer sb = new StringBuffer();
        private boolean coalesceLines = false;

        /**
         * Constructs a new {@code CharacterStreamPumper} to read character data from the provided input stream.
         *
         * @summary Initializes a character stream pumper with the specified input stream and line coalescing behavior.
         *          This is typically used to capture and process output from an external process,
         *          such as a command-line SVN operation.
         *
         * @param is            Input stream from which to read the data.
         * @param coalesceLines If {@code true}, multiple lines of output will be combined into a single string;
         *                      otherwise, each line is processed individually.
         */
        public CharacterStreamPumper(InputStream is, boolean coalesceLines) {
            this.reader = new BufferedReader(new InputStreamReader(is));
            this.coalesceLines = coalesceLines;
        }

        /**
         * Copies data from the input stream to the internal string
         * buffer.
         */
        protected void pumpStream() {
            try {
                String line;
                while ((line = this.reader.readLine()) != null) {
                    if (this.coalesceLines) {
                        this.sb.append(line);
                    } else {
                        this.sb.append(line).append(Helper.NEWLINE);
                    }
                }
            } catch (IOException ex) {
                System.err
                        .println("Problem occured during fetching the command output: "
                                + ex.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Exception during closing the stream. Just ignore.
                }
            }
        }

        public synchronized String toString() {
            return this.sb.toString();
        }
    }

    /**
     * Extracts byte data from streams.
     */
    private static class ByteStreamPumper extends StreamPumper {
        private InputStream bis;
        private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private static final int BUFFER_LENGTH = 1024;
        private byte[] inputBuffer = new byte[BUFFER_LENGTH];

        /**
         * Create a new stream pumper.
         *
         * @param is input stream to read data from
         */
        public ByteStreamPumper(InputStream is) {
            this.bis = is;
        }

        /**
         * Copies data from the input stream to the string buffer
         * </p>
         * Terminates as soon as the input stream is closed or an error occurs.
         */
        protected void pumpStream() {
            try {
                int bytesRead;
                while ((bytesRead = this.bis.read(this.inputBuffer)) != -1) {
                    this.bytes.write(this.inputBuffer, 0, bytesRead);
                }
            } catch (IOException ex) {
                System.err
                        .println("Problem occured during fetching the command output: "
                                + ex.getMessage());
            } finally {
                try {
                    this.bytes.flush();
                    this.bytes.close();
                    this.bis.close();
                } catch (IOException e) {
                    // Exception during closing the stream. Just ignore.
                }
            }
        }

        /**
         * Returns the content of the input stream as a byte array.
         *
         * @summary Retrieves all raw bytes that have been read from the associated input stream.
         *          This method is typically used after the stream pumper has finished processing
         *          the stream content.
         *
         * @return A byte array containing the raw bytes read from the input stream.
         */
        public synchronized byte[] getBytes() {
            return bytes.toByteArray();
        }
    }

    protected static class CmdArguments {
        private List args = new ArrayList();

        protected void add(Object arg) {
            this.args.add(arg);
        }

        protected void addAuthInfo(String user, String pass) {
            if (user != null && user.length() > 0) {
                add("--username");
                add(user);
            }

            if (pass != null && pass.length() > 0) {
                add("--password");
                add(pass);
            }

            add("--non-interactive");
        }

        protected void addConfigInfo(String configDir) {
            if (configDir != null) {
                add("--config-dir");
                add(configDir);
            }
        }

        protected void addLogMessage(String message) {
            add("--force-log");
            add("-m");
            add((message != null) ? message : "");
        }

        private int size() {
            return this.args.size();
        }

        private Object get(int index) {
            return this.args.get(index);
        }
    }
}
