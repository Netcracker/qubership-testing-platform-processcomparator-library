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

package org.qubership.automation.pc.reader.impl.vcs.providers;

import lombok.var;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GITProviderTest {

    private GITProvider provider;
    private Properties props;

    @BeforeEach
    public void setUp() {
        provider = new GITProvider();
        props = new Properties();
        props.setProperty("extensionFilter", "java");
    }

    @Test
    public void testParseRepositoryUrl_blob() {
        String input = "https://git.company.com/PROD.TA/atp-selenium-kick/-/blob/main/src/Main.java";
        String expected = "https://git.company.com/PROD.TA/atp-selenium-kick.git";
        String actual = invokeParseRepositoryUrl(provider, input);
        assertEquals(expected, actual);
    }

    @Test
    public void testParseRepositoryUrl_tree() {
        String input = "https://git.company.com/PROD.TA/atp-selenium-kick/-/tree/main/src";
        String expected = "https://git.company.com/PROD.TA/atp-selenium-kick.git";
        String actual = invokeParseRepositoryUrl(provider, input);
        assertEquals(expected, actual);
    }

    @Test
    public void testParseRepositoryUrl_root() {
        String input = "https://git.company.com/PROD.TA/atp-selenium-kick";
        String expected = "https://git.company.com/PROD.TA/atp-selenium-kick.git";
        String actual = invokeParseRepositoryUrl(provider, input);
        assertEquals(expected, actual);
    }

    @Test
    public void testParseRepositoryUrl_root_withGitExtension() {
        String input = "https://git.company.com/PROD.TA/atp-selenium-kick.git";
        String expected = "https://git.company.com/PROD.TA/atp-selenium-kick.git";
        String actual = invokeParseRepositoryUrl(provider, input);
        assertEquals(expected, actual);
    }

    @Test
    public void testExtractBranch_blob() {
        String url = "https://git.company.com/PROD.TA/project/-/blob/develop/path/to/file.java";
        String branch = provider.extractBranch(url);
        assertEquals("develop", branch);
    }

    @Test
    public void testExtractBranch_tree() {
        String url = "https://git.company.com/PROD.TA/project/-/tree/release/src";
        String branch = provider.extractBranch(url);
        assertEquals("release", branch);
    }

    @Test
    public void testExtractFilePath() {
        String url = "https://git.company.com/PROD.TA/project/-/blob/main/path/to/file.java";
        String path = provider.extractFilePath(url);
        assertEquals("path/to/file.java", path);
    }

    @Test
    public void testExtractTreePath() {
        String url = "https://git.company.com/PROD.TA/project/-/tree/main/path/dir";
        String path = provider.extractTreePath(url);
        assertEquals("path/dir", path);
    }

    @Test
    public void testParseRepositoryUrlFromComplexTreePath() {
        String complexUrl = "https://git.company.com/Personal.TA/Custom_projects/project/custom_actions/-/tree/main/src/main/java";
        String expectedRepo = "https://git.company.com/Personal.TA/Custom_projects/project/custom_actions.git";
        String actualRepo = provider.parseRepositoryUrl(complexUrl);

        assertEquals(expectedRepo, actualRepo);
    }

    @Test
    public void testParseRepositoryUrlFromComplexBlobPath() {
        String complexUrl = "https://git.company.com/Personal.TA/Custom_projects/project/custom_actions/-/blob/main/src/main/java/File.java";
        String expectedRepo = "https://git.company.com/Personal.TA/Custom_projects/project/custom_actions.git";
        String actualRepo = provider.parseRepositoryUrl(complexUrl);

        assertEquals(expectedRepo, actualRepo);
    }

    private String invokeParseRepositoryUrl(GITProvider provider, String url) {
        try {
            var method = GITProvider.class.getDeclaredMethod("parseRepositoryUrl", String.class);
            method.setAccessible(true);
            return (String) method.invoke(provider, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
