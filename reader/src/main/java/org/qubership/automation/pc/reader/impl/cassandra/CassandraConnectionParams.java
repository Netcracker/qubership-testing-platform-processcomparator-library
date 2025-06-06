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

package org.qubership.automation.pc.reader.impl.cassandra;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qubership.automation.pc.core.exceptions.ReaderException;

public class CassandraConnectionParams {

    private List<String> addresses = new ArrayList<>(1);
    private Integer port;
    private String keyspace;
    private String user;
    private String password;

    private static final Pattern CONNECTION_STRING_PATTERN = Pattern.compile("^([^:]+)(:(\\d+))?\\/(.+)");

    public CassandraConnectionParams(String connectionString, String dbUser, String dbPass) throws ReaderException {
        Matcher m = CONNECTION_STRING_PATTERN.matcher(connectionString);
        if (m.find()) {
            if (m.group(3) != null) {
                port = Integer.valueOf(m.group(3));
            }
            keyspace = m.group(4);
            String tempAddresses = m.group(1);
            for (String address : tempAddresses.split(",")) {
                addresses.add(address);
            }
        } else {
            throw new ReaderException("Connection string is in wrong format!");
        }
        user = dbUser;
        password = dbPass;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKeyspace() {
        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
