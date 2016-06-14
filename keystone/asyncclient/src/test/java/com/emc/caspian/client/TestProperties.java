package com.emc.caspian.client;

import com.emc.caspian.fabric.config.Configuration;
import com.emc.caspian.fabric.config.Configuration.Value;

public class TestProperties {

  private static final String SECTION = "keystone";
  private static final Value<String> KEYSTONE_SERVERNAME = Configuration.make(String.class, SECTION + ".servername");
  private static final Value<Integer> KEYSTONE_SERVERPORT = Configuration.make(Integer.class, SECTION + ".serverport");
  private static final Value<Boolean> KEYSTONE_IGNORECERT = Configuration.make(Boolean.class, SECTION
      + ".ignorecertificates");
  private static final Value<String> KEYSTONE_USER = Configuration.make(String.class, SECTION + ".serverusername");
  private static final Value<String> KEYSTONE_PASSWORD = Configuration.make(String.class, SECTION + ".serverpassword");
  private static final Value<Long> WAIT_TIMEOUT = Configuration.make(Long.class, SECTION + ".waittimeout");

  public static Long getWaitTimeout() {
    return WAIT_TIMEOUT.value();
  }

  public static String getServerName() {
    return KEYSTONE_SERVERNAME.value();
  }

  public static Integer getPort() {
    return KEYSTONE_SERVERPORT.value();
  }

  public static Boolean getKeystoneIgnoreCertificate() {
    return KEYSTONE_IGNORECERT.value();
  }

  public static String getUser() {
    return KEYSTONE_USER.value();
  }

  public static String getPassword() {
    return KEYSTONE_PASSWORD.value();
  }
}
