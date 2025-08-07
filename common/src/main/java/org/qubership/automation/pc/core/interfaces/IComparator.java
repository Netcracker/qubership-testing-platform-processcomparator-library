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

package org.qubership.automation.pc.core.interfaces;

import java.util.List;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.data.Data;

/**
 * Interface defining a contract for comparing two data inputs (expected vs actual).
 * <p>
 * Implementations of this interface perform comparisons using structured or raw data,
 * returning a list of {@link DiffMessage} objects that describe the differences.
 * Supports both base64-encoded and plain data formats.
 */
public interface IComparator {
    /* Content of er/ar is base64-encoded by default */
    List<DiffMessage> compare(Data er, Data ar, Parameters configuration) throws ComparatorException;

    List<DiffMessage> compare(Data er, Data ar, Parameters configuration, boolean encoded) throws ComparatorException;

    /* Strings er/ar are assumed as NOT encoded to base64 */
    List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException;

    /* Strings er/ar can be encoded to base64 OR not encoded.
    Extra boolean parameter 'encoded' contains information about it */
    List<DiffMessage> compare(String er, String ar, Parameters configuration,
                              boolean encoded) throws ComparatorException;
}
