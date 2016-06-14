/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.model.mysql;

import com.emc.caspian.fabric.config.Configuration;


/**
 * Provides accessor for mysql application properties. It uses properties cached in ApplicationProperties object. All
 * methods of this class are static. Created by gulavb on 3/1/2015.
 */
public class MySQLProperties {
  private static final String SECTION = "database";
  private static String databaseHostName = null;
  private static String databasePort = null;
  private static String databaseName= null;
  private static String databaseUser = null;
  private static String databasePwd = null;
 
  private static String workflowdb =null;
  private static String workflowuser =null;
  private static String workflowpwd = null;

  public static void initializeMySQLProperties(String dbUser, String dbPassword, String dbHostName, String dbPort, String dbName){
    databaseHostName = dbHostName;
    databasePort = dbPort;
    databaseName = dbName;
    databaseUser = dbUser;
    databasePwd = dbPassword;
  }

  public static void initializeMySQLPropertiesFromConfig() {
    
    workflowdb = Configuration.make(String.class, SECTION + ".workflowdb").value();
    workflowuser = Configuration.make(String.class, SECTION + ".workflowuser").value();
    workflowpwd = Configuration.make(String.class, SECTION + ".workflowpassword").value();
    databaseHostName = Configuration.make(String.class, SECTION + ".dbhostname").value();
    databasePort = Configuration.make(String.class, SECTION + ".dbport").value();
    databaseName = Configuration.make(String.class, SECTION + ".dbname").value();
    databaseUser = Configuration.make(String.class, SECTION + ".dbusername").value();
    databasePwd = Configuration.make(String.class, SECTION + ".dbpassword").value();
  }
  
  public static String getHostname() {
      return databaseHostName;
  }

  public static String getPort() {
      return databasePort;
  }

  public static String getAccountsDatabase() {
      return databaseName;
  }

  public static String getAccountsUser() {
      return databaseUser;
  }

  public static String getAccountsPassword() {
      return databasePwd;
  }
  
  public static String getWorkflowDatabase() {
    return workflowdb;
  }

  public static String getWorkflowUser() {
    return workflowuser;
  }

  public static String getWorkflowPassword() {
    return workflowpwd;
  }
}
