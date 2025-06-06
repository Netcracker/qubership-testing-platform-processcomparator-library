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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.WriteType;
import com.datastax.driver.core.exceptions.DriverException;
import com.datastax.driver.core.policies.RetryPolicy;

/**
 * Copied solution from https://stackoverflow.com/questions/30329956/cassandra-datastax-driver-retry-policy
 * as a quick fix.
 */
public class CustomRetryPolicy implements RetryPolicy {

    private final int readAttempts;
    private final int writeAttempts;
    private final int unavailableAttempts;

    public CustomRetryPolicy(int readAttempts, int writeAttempts, int unavailableAttempts) {
        this.readAttempts = readAttempts;
        this.writeAttempts = writeAttempts;
        this.unavailableAttempts = unavailableAttempts;
    }

    @Override
    public RetryDecision onReadTimeout(Statement stmnt,
                                       ConsistencyLevel cl,
                                       int requiredResponses,
                                       int receivedResponses,
                                       boolean dataReceived,
                                       int time) {
        if (dataReceived) {
            return RetryDecision.ignore();
        } else if (time < readAttempts) {
            return RetryDecision.retry(cl);
        } else {
            return RetryDecision.rethrow();
        }

    }

    @Override
    public RetryDecision onWriteTimeout(Statement stmnt,
                                        ConsistencyLevel cl,
                                        WriteType wt,
                                        int requiredResponses,
                                        int receivedResponses,
                                        int time) {
        if (time < writeAttempts) {
            return RetryDecision.retry(cl);
        }
        return RetryDecision.rethrow();
    }

    @Override
    public RetryDecision onUnavailable(Statement stmnt,
                                       ConsistencyLevel cl,
                                       int requiredResponses,
                                       int receivedResponses,
                                       int time) {
        if (time < unavailableAttempts) {
            return RetryDecision.retry(ConsistencyLevel.ONE);
        }
        return RetryDecision.rethrow();
    }

    @Override
    public RetryDecision onRequestError(Statement statement,
                                        ConsistencyLevel consistencyLevel,
                                        DriverException driverException,
                                        int i) {
        return RetryDecision.rethrow();
    }

    @Override
    public void init(Cluster cluster) {
        // Do nothing
    }

    @Override
    public void close() {
        // Do nothing
    }

}
