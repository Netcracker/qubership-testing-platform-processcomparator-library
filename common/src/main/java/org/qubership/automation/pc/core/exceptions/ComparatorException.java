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
 * Exception class for handling errors that occur during data comparison.
 * </p>
 * Thrown when the comparator logic fails due to configuration issues,
 * data inconsistencies, or internal processing errors.
 * Supports detailed messages, root causes, and status codes for diagnostics.
 */
public class ComparatorException extends ParentException {

    public ComparatorException() {
        super();
    }

    public ComparatorException(String message) {
        super(message);
    }

    public ComparatorException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ComparatorException(String message, int statusCode) {
        super(message,statusCode);        
    }

    public ComparatorException(String message, Throwable cause,int statusCode) {
        super(message, cause,statusCode);        
    }

    public ComparatorException(Throwable cause) {
        super(cause);
    }

    protected ComparatorException(String message, Throwable cause, boolean enableSuppression,
                                  boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
