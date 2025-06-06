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

package org.qubership.automation.pc.core.helpers.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Storage {

    public String dbConnString = "";
    public String dbUser = "";
    public String dbPassword = "";

    protected Connection connection;

    private static final String POSTGRESQL_PREFIX = "jdbc:postgresql:";
    private static final String ORACLE_PREFIX = "jdbc:oracle:";
    private static final String[] DRIVERS = {
            "org.postgresql.Driver",
            "oracle.jdbc.OracleDriver",
            "org.apache.hive.jdbc.HiveDriver"
    };

    public Storage() {
        registerDrivers();
    }

    public Storage(String connString, String dbUser, String dbPassword) {
        this();
        dbConnString = connString;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public void connect() throws SQLException {

        Properties props = new Properties();
        props.setProperty("user", dbUser);
        props.setProperty("password", dbPassword);
        connection = DriverManager.getConnection(dbConnString, props);

    }

    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }
    
    private void registerDrivers() {
        for (String className : DRIVERS) {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException ex) {
                log.error("Driver class {} not found.", className, ex);
            }
        }
    }
}
