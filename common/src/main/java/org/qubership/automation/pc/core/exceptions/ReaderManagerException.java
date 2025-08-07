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

package org.qubership.automation.pc.core.exceptions;

/**
 * Exception class for errors that occur within the Reader Manager component.
 * <p>
 * Thrown when managing or coordinating data readers fails due to misconfiguration,
 * unavailable resources, or internal errors. Supports message customization,
 * root causes, and advanced exception handling features.
 */
public class ReaderManagerException extends Exception {

    public ReaderManagerException() {
        super();
    }

    public ReaderManagerException(String message) {
        super(message);
    }

    public ReaderManagerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReaderManagerException(Throwable cause) {
        super(cause);
    }

    protected ReaderManagerException(String message, Throwable cause, boolean enableSuppression,
                                     boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
