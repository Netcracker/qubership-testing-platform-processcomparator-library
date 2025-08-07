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

package org.tigris.subversion.svnclientadapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Implementation of {@link ISVNLogMessageCallback} that collects SVN log messages,
 * preserving hierarchical (parent-child) relationships between messages.
 * <p>
 * This class is typically used when retrieving logs via SVN operations that may
 * return messages with children, such as merge histories.
 */
public class SVNLogMessageCallback implements ISVNLogMessageCallback {

    /** List of top-level log messages. */
    private List<ISVNLogMessage> messages = new ArrayList<>();

    /** Stack used to manage nested log messages. */
    private Stack<ISVNLogMessage> stack = new Stack<>();

    /**
     * Handles a single SVN log message, building the parent-child hierarchy if needed.
     * <p>
     * If {@code msg} is {@code null}, the method pops the current message from the stack.
     *
     * @param msg the log message to process; may be {@code null}
     */
    public void singleMessage(ISVNLogMessage msg) {
        if (msg == null) {
            if (!stack.empty()) {
                stack.pop();
            }
            return;
        }
        if (stack.empty()) {
            messages.add(msg);
        } else {
            ISVNLogMessage current = (ISVNLogMessage) stack.peek();
            current.addChild(msg);
        }
        if (msg.hasChildren()) {
            stack.push(msg);
        }
    }

    /**
     * Returns all collected top-level log messages.
     *
     * @return an array of collected {@link ISVNLogMessage} objects
     */
    public ISVNLogMessage[] getLogMessages() {
        ISVNLogMessage[] array = new ISVNLogMessage[messages.size()];
        return (ISVNLogMessage[]) messages.toArray(array);
    }

}
