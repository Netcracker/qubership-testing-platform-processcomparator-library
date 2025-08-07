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
 * Exception class indicating that a requested reader could not be found.
 * <p>
 * Thrown when the system attempts to load or access a reader by type or name,
 * but no matching implementation is available. Supports detailed error messages
 * and exception chaining for diagnostics.
 */
public class ReaderNotFoundException extends Exception {

    public ReaderNotFoundException() {
        super();
    }

    public ReaderNotFoundException(String message) {
        super(message);
    }

    public ReaderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReaderNotFoundException(Throwable cause) {
        super(cause);
    }

    protected ReaderNotFoundException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
