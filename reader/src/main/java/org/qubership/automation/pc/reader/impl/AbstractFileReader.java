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

package org.qubership.automation.pc.reader.impl;

import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.converters.Converter;
import org.qubership.automation.pc.core.enums.DataContentType;
import org.qubership.automation.pc.core.enums.DataType;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.exceptions.ValueConverterException;
import org.qubership.automation.pc.core.helpers.StringHelper;
import org.qubership.automation.pc.core.interfaces.IValueConverterValue;
import org.qubership.automation.pc.data.Data;
import org.qubership.automation.pc.data.DataContentConverter;

public abstract class AbstractFileReader {

    public static final String PATH_NAME_COLUMN = "path";
    public static final String MASK_NAME_COLUMN = "mask";
    public static final String STEP_NAME_COLUMN = "stepNameField";
    public static final String DEFAULT_STEP_NAME = "content";
    public static final String CONVERT_PROP = "convert";

    protected String childName;

    protected Data prepareContents(String contents,
                                   String convert,
                                   String filename,
                                   boolean isProcess,
                                   String mask) throws ReaderException {
        String ext = "";
        if (convert != null && !convert.isEmpty()) {
            try {
                IValueConverterValue convertedValue = Converter.exec(contents, convert);
                ext = convertedValue.getType().toString();
                contents = convertedValue.getValue();
            } catch (ValueConverterException ex) {
                throw new ReaderException(ex);
            }
        } else {
            ext = FilenameUtils.getExtension(filename).toUpperCase();
        }
        if (StringUtils.isEmpty(mask)) {
            return fillData(isProcess, FilenameUtils.getBaseName(filename), contents, ext);
        } else {
            return fillData(isProcess, FilenameUtils.getBaseName(StringHelper.maskName(filename, mask)), contents, ext);
        }
    }

    protected Data fillData(boolean isProcess, String itemName, String strResult, String ext) {
        Data data = new Data();
        data.setName(itemName);
        data.setDataType(DataType.SIMPLE);
        data.setExternalId("0");
        data.setTimeStamp(new Date());
        if (isProcess) {
            Data child = new Data();
            child.setName(childName);
            child.setDataType(DataType.SIMPLE);
            child.setExternalId("0");
            child.setTimeStamp(new Date());
            child.setContentType((EnumUtils.isValidEnum(DataContentType.class, ext))
                    ? DataContentType.valueOf(ext) : DataContentType.PRIMITIVES);
            child.setContent(DataContentConverter.fromString(strResult));
            data.setChilds(new ArrayList<Data>());
            data.getChilds().add(child);
        } else {
            data.setContentType((EnumUtils.isValidEnum(DataContentType.class, ext))
                    ? DataContentType.valueOf(ext) : DataContentType.PRIMITIVES);
            data.setContent(DataContentConverter.fromString(strResult));
        }
        return data;
    }
}
