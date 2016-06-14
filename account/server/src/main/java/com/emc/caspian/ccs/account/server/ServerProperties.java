/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.server;

import com.emc.caspian.fabric.config.Configuration;

/**
 * Provides accessor for server related application properties. It uses properties cached in ApplicationProperties
 * object. All methods of this class are static. Created by gulavb on 3/1/2015.
 */
public class ServerProperties {
  // method to initialize all server properties
  public static void initializeServerProperties() {
    requestHeaderSize =
        Configuration.make(Integer.class, SECTION + "." + maxHttpRequestHeaderSize, defaultMaxtHttpRequestHeaderSize)
            .value();
    httpsKeystorePassword = Configuration.make(String.class, SECTION + "." + keystorePassword).value();
    httpsKeystorePath = Configuration.make(String.class, SECTION + "." + keystorePath).value();
    isHttpsEnabled = Configuration.make(Boolean.class, SECTION + "." + httpsEnabled, "false").value();
    log4jPropertiesFilePath = Configuration.make(String.class, SECTION + "." + log4jKey, "").value();
    accessLogPath = Configuration.make(String.class, SECTION + "." + accessLogKey, defaultAccessLogPath).value();
    serverPort = Configuration.make(Integer.class, SECTION + "." + portKey, defaultPort).value();
  }
  
  public static int getPort() {
    return serverPort;
  }
  
  public static String getAccessLogPath() { 
    return accessLogPath;
  }

  public static String getLog4jPropertiesFilePath() {
   return log4jPropertiesFilePath;
  }
  
  public static Boolean isHttpsEnabled() {
    return isHttpsEnabled;
  }

  public static String getHttpsKeystorePath() {
    return httpsKeystorePath;
  }

  public static String getHttpsKeystorePassword() {
    return httpsKeystorePassword;
  }
  
  public static int getRequestHeaderSize() {
    return requestHeaderSize;
  }
 

  private static final String SECTION = "server";
  private static final String defaultPort = "35359";
  private static final String portKey = "serverport";
  private static final String accessLogKey = "accesslogpath";
  private static final String defaultAccessLogPath = "accountaccess.log";
  private static final String log4jKey = "log4jpropertiesfilepath";
  private static final String httpsEnabled = "httpsenabled";
  private static final String maxHttpRequestHeaderSize = "maxhttprequestheadersize";
  private static final String defaultMaxtHttpRequestHeaderSize = "65536";
  private static final String keystorePath = "httpskeystorepath";
  private static final String keystorePassword = "httpskeystorepassword";

  private static int requestHeaderSize;
  private static String httpsKeystorePassword;
  private static String httpsKeystorePath;
  private static boolean isHttpsEnabled;
  private static String log4jPropertiesFilePath;
  private static String accessLogPath;
  private static int serverPort;
}
