/*
 * Copyright (c) 2015 EMC Corporation
 *  All Rights Reserved
 *
 *  This software contains the intellectual property of EMC Corporation
 *  or is licensed to EMC Corporation from third parties.  Use of this
 *  software and the intellectual property contained therein is expressly
 *  limited to the terms and conditions of the License Agreement under which
 *  it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.workflow.model.mysql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.skife.jdbi.v2.DBI;

/**
 * Wrapper for Singleton DataSource object. dbcp2 BasicDataSource object is used. This DataSource does DB connection
 * pooling Created by gulavb on 2/28/2015.
 */

public class MySQLDataSource {

  private static BasicDataSource dataSource;
  private static DBI dbiConnection;

  /**
   * Singleton DataSource accessor method. It creates DataSource object using the DB properties from config file.
   *
   * @return basic data source from dbcp2 package
   */
  public static DBI getDataSourceAsDbiHandler() {
    if (dataSource == null) {
      dataSource = new BasicDataSource();
      dataSource.setDriverClassName(driver);
      String url = String.format(
          mysqlConnectionFormat,
          MySQLProperties.getHostname(),
          MySQLProperties.getPort(), MySQLProperties.getDatabase()
      );
      dataSource.setUrl(url);
      dataSource.setUsername(MySQLProperties.getUser());
      dataSource.setPassword(MySQLProperties.getPassword());
    }
    dbiConnection = new DBI(dataSource);
    return dbiConnection;
  }

  private final static String driver = "com.mysql.jdbc.Driver";
  private final static String mysqlConnectionFormat = "jdbc:mysql://%s:%s/%s";
}
