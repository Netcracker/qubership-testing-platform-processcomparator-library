/*******************************************************************************
 * Copyright (c) 2004, 2006 svnClientAdapter project and others.
 * </p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * </p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * </p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 * Contributors:
 *     svnClientAdapter project committers - initial API and implementation
 ******************************************************************************/

package org.tigris.subversion.svnclientadapter;

import java.lang.reflect.InvocationTargetException;

/**
 * A generic exception thrown from any {@link ISVNClientAdapter} methods.
 * Represents errors during Subversion operations.
 *
 * @author philip schatz
 */
public class SVNClientException extends Exception {

    private int aprError = NONE;

    private static final long serialVersionUID = 1L;

    /**
     * Indicates that no APR error code is associated.
     */
    public static final int NONE = -1;

    /**
     * APR error code indicating a merge conflict.
     */
    public static final int MERGE_CONFLICT = 155015;

    /**
     * APR error code indicating an unsupported feature.
     */
    public static final int UNSUPPORTED_FEATURE = 200007;

    /**
     * Error message used to identify operation interruption.
     */
    public static final String OPERATION_INTERRUPTED = "operation was interrupted";

    /**
     * Constructs a new exception with {@code null} as its detail message.
     */
    public SVNClientException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     */
    public SVNClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the detail message (saved for later retrieval
     *                by the {@link #getMessage()} method)
     * @param cause   the cause (saved for later retrieval by the
     *                {@link #getCause()} method). A {@code null} value is
     *                permitted and indicates that the cause is nonexistent or unknown
     */
    public SVNClientException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause (saved for later retrieval by the
     *              {@link #getCause()} method). A {@code null} value is
     *              permitted and indicates that the cause is nonexistent or unknown
     */
    public SVNClientException(Throwable cause) {
        super(cause);
    }

    /**
     * Factory method for wrapping exceptions in an {@link SVNClientException}.
     *
     * @param e the exception to wrap
     * @return a new instance of {@link SVNClientException}, or the original if already of that type
     */
    public static SVNClientException wrapException(Exception e) {
        Throwable t = e;
        if (e instanceof InvocationTargetException) {
            Throwable target = ((InvocationTargetException) e).getTargetException();
            if (target instanceof SVNClientException) {
                return (SVNClientException) target;
            }
            t = target;
        }
        return new SVNClientException(t);
    }

    /**
     * Returns the APR error code associated with this exception.
     *
     * @return APR error code, or {@link #NONE} if not set
     */
    public int getAprError() {
        return aprError;
    }

    /**
     * Sets the APR error code for this exception.
     *
     * @param aprError the APR error code to set
     */
    public void setAprError(int aprError) {
        this.aprError = aprError;
    }

    /**
     * Checks if this exception indicates that the operation was interrupted.
     *
     * @return {@code true} if the message contains the interruption marker; otherwise {@code false}
     */
    public boolean operationInterrupted() {
        return getMessage() != null && getMessage().contains(OPERATION_INTERRUPTED);
    }

}
