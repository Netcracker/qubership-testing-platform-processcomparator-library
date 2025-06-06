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

package org.qubership.automation.pc.remoteapi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

/**
 * Abstract base class for interacting with remote APIs over HTTP.
 *
 * <p>
 * This class provides a generic mechanism to send requests and receive
 * responses from remote services using configurable HTTP methods.
 * It supports content encoding and response decoding with UTF-8 charset,
 * and is designed to be extended by concrete API client implementations.
 * </p>
 */
public abstract class RemoteApi {

    protected Gson gson = new Gson();

    protected RemoteMethod remoteMethod;

    public void setRemoteMethod(RemoteMethod remoteMethod) {
        this.remoteMethod = remoteMethod;
    }

    protected String readRemote(String url, String sendContent) throws RemoteApiException {
        try {
            if (this.remoteMethod == null) {
                this.remoteMethod = RemoteMethod.POST;
            }

            URL httpUrl = new URL(url);
            HttpURLConnection con = (HttpURLConnection) httpUrl.openConnection();
            con.setRequestMethod(this.remoteMethod.toString());
            con.setRequestProperty("User-Ager", "Mozilla/5.0");
            con.setRequestProperty("Connection", "keep-alive");
            con.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
            con.setRequestProperty("Accept-Charset", "UTF-8");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

            if (!sendContent.isEmpty()) {
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                if (remoteMethod == RemoteMethod.POST) {
                    wr.writeBytes("content=");
                    sendContent = sendContent.replaceAll("\\%", "%25").replaceAll("\\+", "%2B").replace("&", "%26");
                }
                // Other (bad for non-Latin1 charsets) variants:
                // wr.writeUTF(sendContent);
                // wr.writeBytes(sendContent);
                wr.write(sendContent.getBytes("UTF-8"));
                wr.flush();
                wr.close();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            String inLine;
            StringBuffer response = new StringBuffer();
            while ((inLine = in.readLine()) != null) {
                response.append(inLine);
            }
            in.close();

            return response.toString();
        } catch (Exception ex) {
            throw new RemoteApiException(ex);
        }
    }
}
