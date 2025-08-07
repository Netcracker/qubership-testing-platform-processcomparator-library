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

package org.tigris.subversion.svnclientadapter.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class has been taken from SVNKit.
 */
public class ReaderThread extends Thread {

    private final InputStream myInputStream;
    private final OutputStream myOutputStream;

    /**
     * Creates a new daemon thread that continuously reads from the given input stream
     * and writes to the given output stream.
     *
     * @param is the input stream to read from (must not be {@code null})
     * @param os the output stream to write to (must not be {@code null})
     */
    public ReaderThread(InputStream is, OutputStream os) {
        myInputStream = is;
        myOutputStream = os;
        setDaemon(true);
    }

    public void run() {
        try {
            while (true) {
                int read = myInputStream.read();
                if (read < 0) {
                    return;
                }
                myOutputStream.write(read);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                myInputStream.close();
                myOutputStream.flush();
            } catch (IOException e) {
                //Just ignore. Stream closing.
            }
        }
    }
}
