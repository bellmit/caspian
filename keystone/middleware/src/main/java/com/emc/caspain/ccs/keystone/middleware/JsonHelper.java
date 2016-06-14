/*
 * Copyright (c) 2014 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspain.ccs.keystone.middleware;

/**
 * Created by shivesh on 2/17/15.
 */

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Helper class for serialization to and from json
 */
final class JsonHelper {
  /**
   * deserialize from json string into object of T
   *
   * @param string
   * @param tClass
   * @param <T>
   *
   * @return
   */
  public static <T> T deserializeFromJson(final String string, final Class<T> tClass) {
    T object;
    try {
      object = _mapper.readValue(string, tClass);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    return object;
  }

  /**
   * Helper method to serialize object as a json string
   *
   * @param object
   * @param <T>
   *
   * @return
   */
  public static <T> String serializeToJson(final T object) {
    final String json;
    try {
      json = _mapper.writeValueAsString(object);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
    return json;
  }

  private static final ObjectMapper _mapper = new ObjectMapper();
}
