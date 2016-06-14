package com.emc.caspian.ccs.client;

import java.util.HashMap;
import java.util.Map;

public class QueryParams {

  private Map<String, String> queryParams;

  public Map<String, String> addQueryParam(Object query, Object value) {
    if (this.queryParams == null) {
      this.queryParams = new HashMap<>();
    }
    this.queryParams.put(query.toString(), value == null ? null : value.toString());
    return this.queryParams;
  }

  public Map<String, String> getQueryParams() {
    return queryParams;
  }

}
