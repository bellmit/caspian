package com.emc.caspian.ccs.license;

import com.emc.caspian.fabric.config.Configuration;
import com.emc.caspian.fabric.config.Configuration.Value;

/**
 * Provides accessor for application properties. It uses properties cached in ApplicationProperties
 * object. All methods of this class are static. 
 */
public class ExtractProperties {

  public static int getPort() {
    final Value<Integer> port = Configuration.make(Integer.class, SERVER + "." + portKey, defaultPort);
    return port.value();
  }
  
  public static String getAccessLogPath() {
    final Value<String> accesslog =
        Configuration.make(String.class, SERVER + "." + accessLogKey, defaultAccessLogPath);
    return accesslog.value();
  }

  public static String getLog4jPropertiesFilePath() {
    final Value<String> logConfigPath =
        Configuration.make(String.class, SERVER + "." + log4jKey, "");
    return logConfigPath.value();
  }
  
  public static Boolean isHttpsEnabled() {
    return Configuration.make(Boolean.class, SERVER + "." + httpsEnabled, "false").value();
  }

  public static String getHttpsKeystorePath() {
    return Configuration.make(String.class, SERVER + "." + keystorePath).value();
  }

  public static String getHttpsKeystorePassword() {
    return Configuration.make(String.class, SERVER + "." + keystorePassword).value();
  }
  
  public static String getScaleioUsername(){
	  return Configuration.make(String.class, SERVER + "." + scaleioUserName).value();
  }
  
  public static String getScaleioPassword(){
	  return Configuration.make(String.class, SERVER + "." + scaleioPassword).value();
  }
  
  public static int getRequestHeaderSize() {
    final Value<Integer> requestHeaderSize = Configuration.make(Integer.class, SERVER + "." + maxHttpRequestHeaderSize, defaulMaxtHttpRequestHeaderSize);
    return requestHeaderSize.value();
  }
  
  private static final String SERVER = "server";
  private static final String defaultPort = "8765";
  private static final String portKey = "serverport";
  private static final String accessLogKey = "accesslogpath";
  private static final String defaultAccessLogPath = "accountaccess.log";  
  private static final String log4jKey = "log4jpropertiesfilepath";
  private static final String httpsEnabled = "httpsenabled";
  private static final String maxHttpRequestHeaderSize = "maxhttprequestheadersize";
  private static final String defaulMaxtHttpRequestHeaderSize = "65536";
  private static final String keystorePath = "httpskeystorepath";
  private static final String keystorePassword = "httpskeystorepassword";
  private static final String scaleioUserName= "scaleiousername";
  private static final String scaleioPassword = "scaleiopassword";

}

