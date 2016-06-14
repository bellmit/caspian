/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.common.utils;

import org.apache.commons.validator.routines.UrlValidator;

import java.io.UnsupportedEncodingException;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;



/**
 * Class to validate that certain function parameters are not null
 * 
 * @author raod4
 *
 */
public class Validator {

  public static void validateNotNull(Object object) {
    if (object == null) {
      throw new IllegalArgumentException("Empty Object");
    }
  }

  public static void validateNotNull(Object object, String message) {
    if (object == null) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void validateNotEmpty(String text, String message) {
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
  }

  public static void validateNotEmpty(String text) {
    if (text == null || text.isEmpty()) {
      throw new IllegalArgumentException("Empty String");
    }
  }

  public static void validatePortRange(int port) {
    if (port <= 0 || port > 65535) {
      throw new IllegalArgumentException("Port specified is not a valid port");
    }
  }

  public static void validateProtocolType(String protocol) {
    if (!(protocol.equals("https") || protocol.equals("http"))) {
      throw new IllegalArgumentException("Protocol must be 'http' or 'https'");
    }
  }

  public static void validateBulkSize(int bulkSize) {
    if (bulkSize < 1 || bulkSize > 4000) {
      throw new IllegalArgumentException("BulkSize must be between 1 and 4000 inclusive");
    }
  }
  /**
   * this method will validate the URL according to the scheme specified to it
   * @param url
   * @param scheme
   */
  public static void validateUrl(String url, String[] scheme) {
    String[] schemes = scheme;
    UrlValidator urlValidator = new UrlValidator(schemes);
    if (!(urlValidator.isValid(url))) {
      throw new IllegalArgumentException(String.format("URL %s is invalid", url));
    }
  }
  
   public static void validateLength(String characterEncoding, String fieldName, String minErrorMessage,
      String maxErrorMessage, int min, int max) {
    if (!(min <= max)) {
      throw new RuntimeException("Invalid bounds");
    }
    if (fieldName != null) {
      try {
        byte[] bytes = fieldName.getBytes(characterEncoding);
        if (!(bytes.length <= max)) {
          throw new IllegalArgumentException(maxErrorMessage);
        } else if (bytes.length < min) {
          throw new IllegalArgumentException(minErrorMessage);
        }
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("Unsupported encoding exception");
      }
    } else {
      if (min > 0) {
        throw new IllegalArgumentException(minErrorMessage);
      }
    }
  }
 
}
