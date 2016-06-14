package com.emc.caspian.ccs.account.datacontract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonRootName("primaryDomain")
@JsonInclude(value = Include.NON_EMPTY)
public class PrimaryDomain {

  private String primaryDomainId;

  @JsonProperty("primary_domain_id")
  public String getPrimaryDomainId() {
    return primaryDomainId;
  }

  @JsonProperty("primary_domain_id")
  public void setPrimaryDomainId(String primaryDomainId) {
    this.primaryDomainId = primaryDomainId;
  }
  
  
}
