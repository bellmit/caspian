package com.emc.caspian.ccs.keystone.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.emc.caspian.ccs.client.Constants;

class KeystoneClientUtil {

  /**
   * Gets the request header with the authentication token set for the header field X-Auth-Token
   * 
   * @param authenticationToken the authentication token
   * @return the request header
   */
  public static Map<String, String> getAuthenticationHeader(String authenticationToken) {

    Map<String, String> requestHeader = new HashMap<String, String>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);
    return requestHeader;

  }
  
  public static String getStringValueFromResponseHeader(String key, Map<String, List<String>> map) {
    List<String> list = map.get(key);
    return list.get(0);
  }
}
