/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.model.mysql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.skife.jdbi.v2.DBI;

/**
 * Wrapper for Singleton DataSource object. dbcp2 BasicDataSource object is
 * used. This DataSource does DB connection pooling Created by gulavb on
 * 2/28/2015.
 */

public class MySQLDataSource {

	 private static BasicDataSource accountDataSource;
	  private static BasicDataSource workflowDataSource;
	
	  /**
	   * Singleton Accounts DataSource accessor method. It creates accounts DataSource object using the DB properties from config file.
	   * 
	   * @return basic data source as DBI from dbcp2 package
	   */
	  public static DBI getDataSourceAsDbiHandler() {
	   return getDbiHandlerForDataSource (DataSource.AccountsDataSource);
	  }

	  /**
	   * Singleton workflow DataSource accessor method. It creates accounts DataSource object using the DB properties from config file.
	   * 
	   * @return basic data source as DBI from dbcp2 package
	   */
	  public static DBI getDataSourceAsDbiHandlerForWorkflowDb() {
		  return getDbiHandlerForDataSource (DataSource.WorkflowDataSource);
	  }
	  
	/**
	 * This method will take datasource as parameter and configure DBI for
	 * dataSource object accordingly
	 * 
	 * @param datasource
	 * @return
	 */
	private static DBI getDbiHandlerForDataSource(DataSource datasource) {
		DBI connection = null;
		switch (datasource) {
		case WorkflowDataSource:
			if (workflowDataSource == null) {
				workflowDataSource = new BasicDataSource();
				workflowDataSource.setDriverClassName(driver);
				String url = String.format(mysqlConnectionFormat,
						MySQLProperties.getHostname(),
						MySQLProperties.getPort(),
						MySQLProperties.getWorkflowDatabase());
				workflowDataSource.setUrl(url);
				workflowDataSource.setUsername(MySQLProperties
						.getWorkflowUser());
				workflowDataSource.setPassword(MySQLProperties
						.getWorkflowPassword());
			}
			connection = new DBI(workflowDataSource);
			break;

		case AccountsDataSource:
			if (accountDataSource == null) {
				accountDataSource = new BasicDataSource();
				accountDataSource.setDriverClassName(driver);
				String url = String.format(mysqlConnectionFormat,
						MySQLProperties.getHostname(),
						MySQLProperties.getPort(),
						MySQLProperties.getAccountsDatabase());
				accountDataSource.setUrl(url);
				accountDataSource
						.setUsername(MySQLProperties.getAccountsUser());
				accountDataSource.setPassword(MySQLProperties
						.getAccountsPassword());
			}
			connection = new DBI(accountDataSource);
			break;
		}
		return connection;
	}

	/**
	 * Enum representing the dataSources
	 * 
	 * @author raod4
	 *
	 */
	  private enum DataSource {	  
		  WorkflowDataSource,
		  AccountsDataSource
	  }
	  
	private final static String driver = "com.mysql.jdbc.Driver";
	private final static String mysqlConnectionFormat = "jdbc:mysql://%s:%s/%s";
}
