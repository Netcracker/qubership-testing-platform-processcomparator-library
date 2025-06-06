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
 * Exception class for errors that occur during factory instantiation.
 * </p>
 * Thrown when an object cannot be created through a factory due to misconfiguration,
 * missing dependencies, or other runtime issues. Supports custom messages, root causes,
 * and optional status codes for enhanced error reporting.
 */
public class FactoryInstatiationException extends ParentException {

    public FactoryInstatiationException() {
        super();
    }

    public FactoryInstatiationException(String message) {
        super(message);
    }

    public FactoryInstatiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FactoryInstatiationException(String message, int statusCode) {
        super(message, statusCode);
    }

    public FactoryInstatiationException(String message, Throwable cause, int statusCode) {
        super(message, cause, statusCode);
    }

    public FactoryInstatiationException(Throwable cause) {
        super(cause);
    }

    protected FactoryInstatiationException(String message, Throwable cause, boolean enableSuppression,
                                           boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
