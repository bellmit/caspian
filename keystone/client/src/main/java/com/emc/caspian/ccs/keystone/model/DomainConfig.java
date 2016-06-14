package com.emc.caspian.ccs.keystone.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class DomainConfig {
  private DomainConfigInfo domainConfigInfo;

  public DomainConfig() {
    
  }
  
  @JsonProperty("config")
  public DomainConfigInfo getDomainConfigInfo() {
    return domainConfigInfo;
  }

  @JsonProperty("config")
  public void setDomainConfigInfo(DomainConfigInfo domainConfigInfo) {
    this.domainConfigInfo = domainConfigInfo;
  }
}
