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

package org.qubership.automation.pc.core.threads;

import java.util.Date;
import java.util.UUID;

import org.qubership.automation.pc.compareresult.CompareResult;
import org.qubership.automation.pc.core.exceptions.ComparatorException;

/**
 * An abstract base class representing a single comparison session within a multi-threaded
 * comparison framework.
 *
 * <p>
 * Each {@code CompareSession} instance encapsulates metadata such as a unique session ID,
 * lifecycle timestamps (start and finish), status, and a reference to its parent
 * {@link MultiThreadsQueue}.
 * </p>
 *
 * <p>
 * The session manages the execution and tracking of a single comparison task and stores
 * the resulting {@link org.qubership.automation.pc.compareresult.CompareResult}.
 * Subclasses are expected to implement the
 * {@link #run()} method to define specific comparison logic.
 * </p>
 *
 * <p>
 * This class also provides utility methods to update the session state and mark
 * completion.
 * </p>
 */
public abstract class CompareSession {
    private String sessionId = UUID.randomUUID().toString();
    private CompareSessionStatus status;
    private Date started;
    private Date finished;
    private MultiThreadsQueue parent;
    private CompareResult compareResult;

    public CompareSession() {
        status = CompareSessionStatus.NOT_STARTED;
    }
    
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public CompareSessionStatus getStatus() {
        return status;
    }

    public void setStatus(CompareSessionStatus status) {
        this.status = status;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public MultiThreadsQueue getParent() {
        return parent;
    }

    public void setParent(MultiThreadsQueue parent) {
        this.parent = parent;
    }

    public CompareResult getCompareResult() {
        return compareResult;
    }

    public void setCompareResult(CompareResult compareResult) {
        this.compareResult = compareResult;
    }
    
    public void markAsCompleted() {
        setFinished(new Date());
        this.parent.getCompletedSessions().add(sessionId);
    }

    public abstract void run() throws ComparatorException;
        
    
}
