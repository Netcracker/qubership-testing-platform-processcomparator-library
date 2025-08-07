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
 * Exception class for handling errors that occur during value conversion operations.
 * <p>
 * Thrown when a value cannot be converted between types or formats as expected.
 * Includes support for detailed messages, root causes, and optional status codes.
 */
public class ValueConverterException extends ParentException {

    public ValueConverterException() {
        super();
    }

    public ValueConverterException(String message) {
        super(message);
    }

    public ValueConverterException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ValueConverterException(String message, int statusCode) {
        super(message,statusCode);        
    }

    public ValueConverterException(String message, Throwable cause,int statusCode) {
        super(message, cause,statusCode);        
    }

    public ValueConverterException(Throwable cause) {
        super(cause);
    }

    protected ValueConverterException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
