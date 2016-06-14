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

import com.emc.caspian.fabric.config.Configuration;
import com.emc.caspian.fabric.config.Configuration.Value;


/**
 * Provides accessor for mysql application properties. It uses properties cached in ApplicationProperties object. All
 * methods of this class are static. Created by gulavb on 3/1/2015.
 */
public class MySQLProperties {

  private static final String SECTION = "database";
  private static final Value<String> DBHOSTNAME = Configuration.make(String.class, SECTION + ".dbhostname");
  private static final Value<String> DBPORT = Configuration.make(String.class, SECTION + ".dbport");
  private static final Value<String> DBNAME = Configuration.make(String.class, SECTION + ".workflowdb");
  private static final Value<String> DBUSER = Configuration.make(String.class, SECTION + ".workflowuser");
  private static final Value<String> DBPWD = Configuration.make(String.class, SECTION + ".workflowpassword");

  public static String getHostname() {
    return DBHOSTNAME.value();
  }

  public static String getPort() {
    return DBPORT.value();
  }

  public static String getDatabase() {
    return DBNAME.value();
  }

  public static String getUser() {
    return DBUSER.value();
  }

  public static String getPassword() {
    return DBPWD.value();
  }
}
