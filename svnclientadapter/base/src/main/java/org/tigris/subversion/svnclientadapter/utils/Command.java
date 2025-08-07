/*******************************************************************************
 * Copyright (c) 2004, 2006 svnClientAdapter project and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/
package org.tigris.subversion.svnclientadapter.utils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Executes an external system command as a separate process.
 * <p>
 * This class allows setting the command and its parameters, redirecting standard
 * output and error streams, and waiting for the process to complete.
 * Some parts of this class are inspired by SVNKit.
 * </p>
 */
public class Command {
    /**
     * The process created by executing the command.
     */
    private Process process = null;

    /**
     * The command to execute (e.g., "git", "ls", "java").
     */
    private String command;

    /**
     * Parameters to be passed to the command.
     */
    private String[] parameters = new String[]{};

    /**
     * Output stream for standard output of the process.
     */
    private OutputStream out = System.out;

    /**
     * Output stream for standard error of the process.
     */
    private OutputStream err = System.err;

    /**
     * Constructs a Command instance with the specified command.
     *
     * @param command the command to execute
     */
    public Command(String command) {
        this.command = command;
    }

    /**
     * Sets the output stream for the standard error of the process.
     *
     * @param err the error output stream to set
     */
    public void setErr(OutputStream err) {
        this.err = err;
    }

    /**
     * Sets the output stream for the standard output of the process.
     *
     * @param out the standard output stream to set
     */
    public void setOut(OutputStream out) {
        this.out = out;
    }

    /**
     * Sets the parameters for the command.
     *
     * @param parameters the parameters to set
     */
    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the underlying process instance.
     *
     * @return the process created by this command
     */
    public Process getProcess() {
        return process;
    }

    /**
     * Terminates the currently running process, if any.
     */
    public void kill() {
        if (process != null) {
            process.destroy();
            process = null;
        }
    }

    /**
     * Executes the command with the specified parameters.
     * Redirects the standard output and error streams to the configured output streams.
     *
     * @throws IOException if an I/O error occurs while executing the command
     */
    public void exec() throws IOException {
        String[] cmdArray = new String[parameters.length + 1];
        cmdArray[0] = command;
        System.arraycopy(parameters, 0, cmdArray, 1, parameters.length);
        process = Runtime.getRuntime().exec(cmdArray);
        if (process != null) {
            new ReaderThread(process.getInputStream(), out).start();
            new ReaderThread(process.getErrorStream(), err).start();
            process.getOutputStream().close();
        }
    }

    /**
     * Causes the current thread to wait, if necessary, until the process
     * represented by this {@code Command} object has terminated.
     *
     * @return the exit value of the process. By convention, {@code 0} indicates normal termination.
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public int waitFor() throws InterruptedException {
        return process.waitFor();
    }
}
