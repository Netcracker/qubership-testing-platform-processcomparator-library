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

package org.qubership.automation.pc;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.core.VCSProviderFactory;
import org.qubership.automation.pc.core.exceptions.FactoryInstatiationException;
import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSContext;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSFile;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSProvider;
import org.qubership.automation.pc.core.interfaces.vcs.IVCSReadRequest;
import org.qubership.automation.pc.reader.impl.vcs.VCSReadRequest;

public class VCSFactoryTest {

    @Test
    @Disabled
    public void SVN_factoryTest() {
        try {
            IVCSProvider svnProvider = VCSProviderFactory.getProvider("SVNProvider");
            Properties props = new Properties();
            props.put(IVCSProvider.PROP_EXTENSION_FILTER, "xml|XML");
            svnProvider.init(props, "", "");
            IVCSReadRequest request = new VCSReadRequest("https://svn.some-url.com/", "HEAD");
            IVCSContext context = svnProvider.readRemote(request);
            for (IVCSFile file : context.getFiles()) {
                String content = inputStreamToString(file.getContent());
                System.out.println(content);
            }
        } catch (FactoryInstatiationException | ReaderException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    @Disabled
    public void GIT_factoryTest() {
        try {
            IVCSProvider gitProvider = VCSProviderFactory.getProvider("GITProvider");
            Properties props = new Properties();
            props.put(IVCSProvider.PROP_EXTENSION_FILTER, "java");
            gitProvider.init(props, "", "");
            IVCSReadRequest request = new VCSReadRequest("https://git.some-url.com/", "main");
            IVCSContext context = gitProvider.readRemote(request);
            for (IVCSFile file : context.getFiles()) {
                String content = inputStreamToString(file.getContent());
                System.out.println(content);
            }
        } catch (FactoryInstatiationException | ReaderException ex) {
            fail(ex.getMessage());
        }
    }

    private String inputStreamToString(InputStream is) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            StringBuilder sb = new StringBuilder();
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (counter != 0) {
                    sb.append(System.getProperty("line.separator"));
                }
                sb.append(line);
                counter++;
            }
            return sb.toString();
        } catch (IOException ex) {
            return null;
        }
    }
}
