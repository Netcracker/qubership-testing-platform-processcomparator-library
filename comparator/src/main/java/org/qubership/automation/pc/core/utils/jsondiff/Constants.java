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

package org.qubership.automation.pc.core.utils.jsondiff;

/**
 * Defines constant string values used as JSON field keys in operations such as diff, patch, or JSON comparison.
 *
 * <p>
 * This utility class serves as a centralized location for common key names used throughout the JSON processing logic,
 * particularly in the context of generating or interpreting JSON Patch (RFC 6902) or similar structures.
 * </p>
 *
 * <p>
 * This class is final and has a private constructor to prevent instantiation or subclassing.
 * All members are static and should be accessed in a static context.
 * </p>
 */
final class Constants {
    public static String OP = "op";
    public static String VALUE = "value";
    public static String PATH = "path";
    public static String FROM = "from";
    public static String CONTROL_JSON_PATH = "controlJsonPath";
    public static String TEST_JSON_PATH = "testJsonPath";

    private Constants() {
    }
    
}
