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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.helpers.ThreadUtils;
import org.slf4j.MDC;

/**
 * Manages the lifecycle of concurrent {@link CompareSession} instances and their execution queues.
 *
 * <p>
 * This singleton class acts as a central coordinator for:
 * <ul>
 *     <li>Registering and tracking active comparison sessions</li>
 *     <li>Handling execution of sessions via named threads</li>
 *     <li>Managing {@link MultiThreadsQueue} objects and their associated semaphores</li>
 *     <li>Ensuring proper cleanup of sessions and resources upon completion</li>
 * </ul>
 *
 * <p>
 * Each session belongs to a queue and may be executed asynchronously in a separate thread.
 * The manager ensures controlled access to queue resources using {@link Semaphore}.
 * </p>
 *
 * <p>
 * Use {@link #getInstance()} to access the singleton instance.
 * </p>
 */
public class CompareSessionsManager {

    private ConcurrentHashMap<String, CompareSession> activeSessions;
    private ConcurrentHashMap<String, Semaphore> queueSemaphores;
    private ConcurrentHashMap<String, MultiThreadsQueue> queues;

    public CompareSessionsManager() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.queueSemaphores = new ConcurrentHashMap<>();
        this.queues = new ConcurrentHashMap<>();
    }

    private static final CompareSessionsManager instance = new CompareSessionsManager();

    public static CompareSessionsManager getInstance() {
        return instance;
    }

    public ConcurrentHashMap<String, CompareSession> getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(ConcurrentHashMap<String, CompareSession> activeSessions) {
        this.activeSessions = activeSessions;
    }

    public ConcurrentHashMap<String, Semaphore> getQueueSemaphores() {
        return queueSemaphores;
    }

    public void setQueueSemaphores(ConcurrentHashMap<String, Semaphore> queueSemaphores) {
        this.queueSemaphores = queueSemaphores;
    }

    public void addSession(CompareSession session) {
        addSession(session, false);
    }

    public void addSession(CompareSession session, boolean executeAfterAdd) {
        if (this.activeSessions == null) {
            this.activeSessions = new ConcurrentHashMap<>();
        }
        this.activeSessions.put(session.getSessionId(), session);

        if (executeAfterAdd) {
            runSession(session.getSessionId());
        }
    }

    public void updateSession(String sessionId) {
    }

    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
    }

    public void removeSemaphore(String queueId) {
        queueSemaphores.remove(queueId);
    }

    public void removeQueue(String queueId) {
        queues.remove(queueId);
    }

    public void releaseQueue(String queueId) {
        MultiThreadsQueue queue = getQueue(queueId);
        if (queue.queueIsCompleted()) {
            for (String sessionId : queue.getCompletedSessions()) {
                removeSession(sessionId);
            }

            Semaphore semaphore = queue.getSemaphore();
            if (!semaphore.isFair() && !semaphore.hasQueuedThreads()) {
                removeSemaphore(queueId);
            }
            removeQueue(queueId);
        }
    }

    public CompareSession getSession(String sessionId) {
        return null;
    }

    public void runSession(String sessionId) {
        if (activeSessions.containsKey(sessionId)) {
            final CompareSession selectedSession = activeSessions.get(sessionId);
            String sessionThreadName = ThreadUtils.getThreadName() + "%session:" + selectedSession.getSessionId();
            Map<String, String> mdcMap = MDC.getCopyOfContextMap();
            Thread sessionThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ThreadUtils.setMdcContextMap(mdcMap);
                    try {
                        queueSemaphores.get(selectedSession.getParent().getId()).acquire();
                        selectedSession.run();
                    } catch (InterruptedException | ComparatorException e) {
                        //TODO: print out exception in log
                    } finally {
                        queueSemaphores.get(selectedSession.getParent().getId()).release();
                    }
                }
            }, sessionThreadName);
            sessionThread.start();
        }
    }

    public void runQueue(final MultiThreadsQueue queue) {
        queues.put(queue.getId(), queue);
        String queueThreadName = ThreadUtils.getThreadName() + "%queue:" + queue.getId();
        Map<String, String> mdcMap = MDC.getCopyOfContextMap();
        Thread queueThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ThreadUtils.setMdcContextMap(mdcMap);
                queue.run();
            }
        }, queueThreadName);
        queueThread.start();
    }

    public MultiThreadsQueue getQueue(String queueId) {
        if (queues.containsKey(queueId)) {
            return queues.get(queueId);
        } else {
            return null;
        }
    }

    public Map<String, CompareSessionStatus> getSessionStatusesByQueue(String queueId) {
        if (queues.containsKey(queueId)) {
            Map<String, CompareSessionStatus> resultMap = new HashMap<>();
            MultiThreadsQueue queue = queues.get(queueId);
            List<CompareSession> sessions = queue.getQueueSessions();
            for (CompareSession session : sessions) {
                resultMap.put(session.getSessionId(), session.getStatus());
            }
            return resultMap;
        } else {
            return null;
        }
    }

}
