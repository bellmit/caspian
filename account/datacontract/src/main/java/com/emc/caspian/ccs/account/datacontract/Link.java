/**
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.datacontract;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by gulavb on 2/26/2015.
 */
public class Link {
  private String rel;
  private String href;

  @JsonProperty("rel")
  public String getRel() {
    return rel;
  }

  @JsonProperty("rel")
  public void setRel(String rel) {
    this.rel = rel;
  }

  @JsonProperty("href")
  public String getHref() {
    return href;
  }

  @JsonProperty("href")
  public void setHref(String href) {
    this.href = href;
  }
}
