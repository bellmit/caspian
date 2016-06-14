/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.account.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.emc.caspian.encryption.AESUtil;
import com.emc.caspian.fabric.config.Configuration;

/**
 * Provides accessor for keystone related application properties. All methods of this class are static.
 */

public class KeystoneProperties {
  private static final Logger _log = LoggerFactory.getLogger(KeystoneProperties.class);
  private final static String DEFAULT_AUTH_URI = "https://keystone:5000";
  private final static String SECTION_TAG = "keystone";
  private final static String AUTH_URI_TAG = ".auth_uri";
  private final static String ADMIN_USER_TAG = ".admin_user";
  private final static String keystoneUri = Configuration.make(String.class, SECTION_TAG + AUTH_URI_TAG,
      DEFAULT_AUTH_URI).value();
  private final static String userName = Configuration.make(String.class, SECTION_TAG + ADMIN_USER_TAG, "").value();
  private static final String keystoneAdminUserEnv="KS_CPSA_PWD";
  
  public static String getkeystoneUri() {
    return keystoneUri;
  }

  public static String getKeystoneAdmin() {
    return userName;
  }

 /**
  * Having a non static method will prevent decrypted password from staying in RAM at the time of class loading
  * @return 
  */
  public String getKeystoneAdminPassword() {
    return getKeystoneAdminPasswordFromEnv();
  }

  /**
   * This reads the env variable KS_CPSA_PWD to get the encrypted password and decrypts it.
   * 
   * @return
   */
  private String getKeystoneAdminPasswordFromEnv() {
    String userPassword = null;
    try (AESUtil au = AESUtil.getInstance()){
      String env = System.getenv(keystoneAdminUserEnv);
      userPassword = au.decrypt(env);
    }
    return userPassword;
  }

}
