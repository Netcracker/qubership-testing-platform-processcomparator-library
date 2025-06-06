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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.qubership.automation.pc.core.exceptions.ReaderException;
import org.qubership.automation.pc.reader.impl.cassandra.CassandraConnectionParams;

public class CassandraConnectionParamsTest {

    private final String DEFAULT_USER = "cass";
    private final String DEFAULT_PASS = "cass";

    @Test
    public void testStringParsingSingleAddressWithPort() {
        String connectionString = "nc-prodgaps.openshift.sdntest.qubership.org:31382/uim_main_2_6_0_x_qa_env";
        try {
            CassandraConnectionParams params
                    = new CassandraConnectionParams(connectionString, DEFAULT_USER, DEFAULT_PASS);
            Assertions.assertEquals(DEFAULT_USER, params.getUser());
            Assertions.assertEquals(DEFAULT_PASS, params.getPassword());
            Assertions.assertEquals(Integer.valueOf(31382), params.getPort());
            Assertions.assertEquals("uim_main_2_6_0_x_qa_env", params.getKeyspace());
            Assertions.assertEquals(1, params.getAddresses().size());
            Assertions.assertEquals("nc-prodgaps.openshift.sdntest.qubership.org", params.getAddresses().get(0));
        } catch (ReaderException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    public void testStringParsingSingleAddressNoPort() {
        String connectionString = "nc-prodgaps.openshift.sdntest.qubership.org/uim_main_2_6_0_x_qa_env";
        try {
            CassandraConnectionParams params
                    = new CassandraConnectionParams(connectionString, DEFAULT_USER, DEFAULT_PASS);
            Assertions.assertNull(params.getPort());
        } catch (ReaderException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    public void testStringParsingMultipleAddressNoPort() {
        String connectionString = "nc-prodgaps,openshift.sdntest,qubership.org/uim_main_2_6_0_x_qa_env";
        try {
            CassandraConnectionParams params
                    = new CassandraConnectionParams(connectionString, DEFAULT_USER, DEFAULT_PASS);
            Assertions.assertEquals(3, params.getAddresses().size());
            Assertions.assertEquals("nc-prodgaps", params.getAddresses().get(0));
            Assertions.assertEquals("openshift.sdntest", params.getAddresses().get(1));
            Assertions.assertEquals("qubership.org", params.getAddresses().get(2));
        } catch (ReaderException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    public void testStringParsingMultipleAddressWithPort() {
        String connectionString = "nc-prodgaps,openshift.sdntest,qubership.org:31382/uim_main_2_6_0_x_qa_env";
        try {
            CassandraConnectionParams params
                    = new CassandraConnectionParams(connectionString, DEFAULT_USER, DEFAULT_PASS);
            Assertions.assertEquals(3, params.getAddresses().size());
            Assertions.assertEquals("nc-prodgaps", params.getAddresses().get(0));
            Assertions.assertEquals("openshift.sdntest", params.getAddresses().get(1));
            Assertions.assertEquals("qubership.org", params.getAddresses().get(2));
            Assertions.assertEquals(Integer.valueOf(31382), params.getPort());
        } catch (ReaderException ex) {
            Assertions.fail(ex.getMessage());
        }
    }

    @Test
    public void testStringParsingNoKeyspace() {
        String connectionString = "nc-prodgaps.openshift.sdntest.qubership.org:38001/";
        Assertions.assertThrows(ReaderException.class,
                () -> new CassandraConnectionParams(connectionString, DEFAULT_USER, DEFAULT_PASS));
    }

    @Test
    public void testStringParsingNonIntegerPort() {
        String connectionString = "nc-prodgaps.openshift.sdntest.qubership.org:38aa1/keyspace";
        Assertions.assertThrows(ReaderException.class,
                () -> new CassandraConnectionParams(connectionString, DEFAULT_USER, DEFAULT_PASS));
    }

    @Test
    public void testStringParsingNoAddresses() {
        String connectionString = ":38001/keyspace";
        Assertions.assertThrows(ReaderException.class,
                () -> new CassandraConnectionParams(connectionString, DEFAULT_USER, DEFAULT_PASS));
    }
}
