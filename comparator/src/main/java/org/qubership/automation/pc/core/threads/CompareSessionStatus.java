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

package org.qubership.automation.pc.core.threads;

/**
 * Represents the various lifecycle states of a comparison session.
 * <p>
 * These statuses are used to track and control the execution progress
 * of {@link CompareSession} instances during their lifecycle, such as:
 * <ul>
 *     <li>{@code NOT_STARTED} – the session has been created but not yet started</li>
 *     <li>{@code STARTED} – the session has been initiated</li>
 *     <li>{@code IN_PROGRESS} – the session is actively comparing data</li>
 *     <li>{@code AWAITING} – the session is waiting on external resources or dependencies</li>
 *     <li>{@code ON_HOLD} – the session has been paused or deferred</li>
 *     <li>{@code COMPLETED} – the session has finished processing</li>
 * </ul>
 */
public enum CompareSessionStatus {
    ON_HOLD, NOT_STARTED, STARTED, IN_PROGRESS, AWAITING, COMPLETED
}
