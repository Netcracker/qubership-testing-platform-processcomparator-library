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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * Represents a queue of {@link CompareSession} instances that are executed concurrently.
 *
 * <p>
 * This class provides management of session execution using a fixed-size semaphore to control concurrency.
 * Sessions are tracked for completion status and can be added individually or in bulk.
 * It also integrates with {@code CompareSessionsManager} to manage execution threads.
 * </p>
 *
 * <p>
 * Implements {@link Runnable} to allow execution in a separate thread.
 * </p>
 */
public class MultiThreadsQueue implements Runnable {

    private String id = UUID.randomUUID().toString();

    protected List<CompareSession> queueSessions = new ArrayList<>();
    protected ConcurrentHashMap<String, CompareSession> sessions = new ConcurrentHashMap<>();
    protected List<String> completedSessions = new CopyOnWriteArrayList<>();
    private Semaphore semaphore;
    protected int queueSize = 0;
    public CountDownLatch cdl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getSize() {
        return queueSize;
    }

    public List<CompareSession> getQueueSessions() {
        return queueSessions;
    }

    public void setQueueSessions(List<CompareSession> queueSessions) {
        this.queueSessions = queueSessions;
    }

    public ConcurrentHashMap<String, CompareSession> getSessions() {
        return sessions;
    }

    public void setSessions(ConcurrentHashMap<String, CompareSession> sessions) {
        this.sessions = sessions;
    }

    public void add(CompareSession session) {
        if (queueSessions == null) {
            queueSessions = new ArrayList<>();
        }
        session.setParent(this);
        queueSessions.add(session);
    }

    public void addAll(Collection<CompareSession> collection) {
        for (CompareSession session : collection) {
            add(session);
        }
    }

    public List<String> getCompletedSessions() {
        return completedSessions;
    }
    
    public List<String> getSessionIds() {
        List<String> resultList = new ArrayList<>();
        for (CompareSession session : queueSessions) {
            resultList.add(session.getSessionId());
        }
        return resultList;
    }
    
    public void setCompletedSessions(List<String> completedSessions) {
        this.completedSessions = completedSessions;
    }
    
    public boolean queueIsCompleted() {
        return completedSessions.size() == queueSessions.size();
    }
    
    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        if (queueSize > 0) {
            semaphore = new Semaphore(queueSize, true);
            cdl = new CountDownLatch(queueSessions.size());
            CompareSessionsManager.getInstance().getQueueSemaphores().put(this.id,semaphore);
            for (CompareSession session : queueSessions) {
                CompareSessionsManager.getInstance().addSession(session, true);
            }
            try {
                cdl.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    
}
