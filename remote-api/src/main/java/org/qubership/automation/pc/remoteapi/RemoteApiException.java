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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.qubership.automation.pc.remoteapi;

/**
 * Exception thrown to indicate an error occurred during a remote API operation.
 *
 * <p>This exception is typically thrown by implementations of {@link RemoteApi}
 * when an error occurs during the communication with a remote server, such as
 * network failures, malformed responses, or protocol violations.</p>
 *
 * <p>The exception provides multiple constructors to allow setting a detailed message,
 * the underlying cause, or both, and to configure stack trace suppression and writability.</p>
 *
 * @see RemoteApi
 */
public class RemoteApiException extends Exception {

    public RemoteApiException() {
        super();
    }

    public RemoteApiException(String message) {
        super(message);
    }

    public RemoteApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemoteApiException(Throwable cause) {
        super(cause);
    }

    protected RemoteApiException(String message,
                                 Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
