package com.emc.caspian.ccs.account.datacontract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class C3StatusResponse {

  @JsonInclude(value = Include.NON_EMPTY)
  private String status;

  @JsonProperty("status")
  public String getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(String status) {
    this.status = status;
  }
  
}
