package com.emc.caspian.ccs.keystone.client;

import java.util.HashMap;
import java.util.LinkedList;
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
  public static Map<String, Object> getAuthenticationHeader(String authenticationToken) {

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);
    return requestHeader;

  }
  
  public static String getStringValueFromResponseHeader(String key, Map<String, List<String>> map) {
    LinkedList<String> list = (LinkedList<String>) map.get(key);
    return (String) list.getFirst();
  }
}
