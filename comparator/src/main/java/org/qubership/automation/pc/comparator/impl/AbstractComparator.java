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

package org.qubership.automation.pc.comparator.impl;

import java.util.ArrayList;
import java.util.List;

import org.qubership.automation.pc.compareresult.DiffMessage;
import org.qubership.automation.pc.configuration.parameters.Parameters;
import org.qubership.automation.pc.core.exceptions.ComparatorException;
import org.qubership.automation.pc.core.interfaces.IComparator;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;

/**
 * Abstract base implementation of the {@link IComparator} interface.
 *
 * <p>
 * Provides default logic for handling {@link Data} inputs and encoded string values,
 * converting them to string representations before delegating to the appropriate comparator logic.
 * </p>
 *
 * <p>
 * Subclasses are expected to override the core {@code compare(String er, String ar, Parameters configuration)}
 * method to implement specific comparison behavior.
 * </p>
 */
public abstract class AbstractComparator implements IComparator {

    @Override
    public List<DiffMessage> compare(Data er, Data ar, Parameters configuration) throws ComparatorException {
        return compare(DataContentConverter.toString(er), DataContentConverter.toString(ar), configuration);
    }

    @Override
    public List<DiffMessage> compare(Data er, Data ar, Parameters configuration,
                                     boolean encoded) throws ComparatorException {
        if (encoded) {
            return compare(DataContentConverter.toString(er), DataContentConverter.toString(ar), configuration);
        } else {
            return compare(er.getContent(), ar.getContent(), configuration);
        }
    }

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration,
                                     boolean encoded) throws ComparatorException {
        if (encoded) {
            return compare(DataContentConverter.toString(er), DataContentConverter.toString(ar), configuration);
        } else {
            return compare(er, ar, configuration);
        }
    }

    @Override
    public List<DiffMessage> compare(String er, String ar, Parameters configuration) throws ComparatorException {
        return new ArrayList<>();
    }
}
