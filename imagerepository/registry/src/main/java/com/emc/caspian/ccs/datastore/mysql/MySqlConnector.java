/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.datastore.mysql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.fabric.config.Configuration;
import com.emc.caspian.fabric.config.Configuration.Value;

/**
 * This class handles the connection configuration, DB initialization checks.
 *
 * @author shrids
 *
 */
public class MySqlConnector {

    private static final Logger _log = LoggerFactory.getLogger(MySqlConnector.class);

    private static final String SECTION = "metadata.store.sql";

    private static final Value<String> URL = Configuration.make(String.class, SECTION + ".url");
    private static final Value<String> USER = Configuration.make(String.class, SECTION + ".user");
    private static final Value<String> PASSWORD = Configuration.make(String.class, SECTION + ".password");
    private static final Value<String> TABLE_CREATE = Configuration.make(String.class, SECTION+ ".table");
    //The maximum number of active connections that can be allocated from this pool at the same time
    private static final Value<Integer> MAX_CONNECTION = Configuration.make(Integer.class, SECTION+ ".maxconnection");
    //The initial number of connections that are created when the pool is started.
    private static final Value<Integer> INITIAL_CONNECTION_SIZE = Configuration.make(Integer.class, SECTION+ ".initialConnection");
    //JDBC Driver to be used
    private static final Value<String> JDBC_DRIVER = Configuration.make(String.class, SECTION+ ".jdbcDriverClassName");

    public MySqlConnector() {

        BasicDataSource dataSource = new BasicDataSource(); //This ensures a pooled connection is used for DB.
        dataSource.setDriverClassName(JDBC_DRIVER.value());
        dataSource.setUrl(URL.value());
        dataSource.setUsername(USER.value());
        dataSource.setPassword(PASSWORD.value());
        dataSource.setMaxTotal(MAX_CONNECTION.value());
        dataSource.setInitialSize(INITIAL_CONNECTION_SIZE.value());

        _connection = new DBI(dataSource);
        initialize();
        _log.info("MySqlConnector has been initialized");
    }

    public DBI getConnection() {
        return _connection;
    }

    private void initialize() {
        try (Handle handle = _connection.open()) {
            handle.execute(TABLE_CREATE.value());
        }
    }

    private final DBI _connection;
}
