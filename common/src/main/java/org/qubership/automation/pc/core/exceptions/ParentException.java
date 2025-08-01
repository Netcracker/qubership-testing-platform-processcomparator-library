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
 * Base class for custom exceptions with optional status code support.
 * </p>
 * Provides standard exception constructors along with a customizable
 * {@code statusCode} field for enhanced error reporting and integration with APIs.
 * Intended to be extended by more specific exception types in the system.
 */
public abstract class ParentException extends Exception {

    protected int statusCode = -1;

    public ParentException() {
        super();
    }

    public ParentException(String message) {
        super(message);
    }

    public ParentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ParentException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ParentException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public ParentException(Throwable cause) {
        super(cause);
    }

    protected ParentException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

}
