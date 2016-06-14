package com.emc.caspian.ccs.keystone.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IdentityDriver {
  @JsonProperty("driver")
  private String driver;

  public IdentityDriver() {
    
  }
  
  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }
}
