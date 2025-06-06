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

package org.qubership.automation.pc.data;

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.pc.core.enums.DataContentType;

/**
 * Convert content Data.
 */
public class DataContentConverter {

    /**
     * Get Decoded content from Data type object.
     * 
     * @param data
     *            Data object which contains encodedContent
     * @return String decoded String
     */
    public static String toString(Data data) {
        byte[] decodedBytes = Base64.decodeBase64(data.getContent());
        return new String(decodedBytes,StandardCharsets.UTF_8);
    }

    /**
     * Decode encoded String.
     * 
     * @param content
     *            encoded string
     * @return String decoded String
     */
    public static String toString(String content) {
        byte[] decodedBytes = Base64.decodeBase64(content);
        return new String(decodedBytes,StandardCharsets.UTF_8);
    }

    /**
     * Encode String to base64.
     * 
     * @param content   string for encode
     * @return String Encoded string
     */
    public static String fromString(String content) {
        byte[] encodedBytes = Base64.encodeBase64(content.getBytes(StandardCharsets.UTF_8));
        return new String(encodedBytes);
    }

    public static void convertContent(Data data, DataContentType targetType) {
        //data.setContent(convertContent(data.getContent(),data.getContentType(),targetType));
        // Currently content of all datatypes is stored as encoded data.
        // There is no need to convert content between different datatypes
        data.setContentType(targetType);
    }
    
    public static String convertContent(String content, DataContentType sourceType, DataContentType targetType) {
        if (StringUtils.isBlank(content))  {
            return content;
        }
        String decodedContent = decodeContent(content, sourceType);
        return encodeContent(decodedContent, targetType);
    }

    public static String decodeContent(String content, DataContentType contentType) {
        return contentTransformation(content, contentType, "decode");
    }

    public static String encodeContent(String content, DataContentType contentType) {
        return contentTransformation(content, contentType, "encode");
    }

    public static String contentTransformation(String content, DataContentType contentType, String transformation) {
        switch (contentType) {
            case TASK_LIST:
            case PRIMITIVES:
                return content;
            default:
                return (transformation.equals("decode")) ? toString(content) : fromString(content);
        }
    }
}
