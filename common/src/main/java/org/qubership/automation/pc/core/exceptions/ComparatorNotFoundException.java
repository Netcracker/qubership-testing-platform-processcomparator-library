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
 * Exception class indicating that a required comparator was not found.
 * </p>
 * Thrown when the system fails to locate a comparator implementation
 * for a specific data type, content type, or configuration.
 * Supports detailed error messages, causes, and optional status codes.
 */
public class ComparatorNotFoundException extends ParentException {

    public ComparatorNotFoundException() {
        super();
    }

    public ComparatorNotFoundException(String message) {
        super(message);
    }

    public ComparatorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ComparatorNotFoundException(String message,int statusCode) {
        super(message,statusCode);
    }

    public ComparatorNotFoundException(String message, Throwable cause, int statusCode) {
        super(message, cause, statusCode);
    }

    public ComparatorNotFoundException(Throwable cause) {
        super(cause);
    }

    protected ComparatorNotFoundException(String message, Throwable cause, boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
