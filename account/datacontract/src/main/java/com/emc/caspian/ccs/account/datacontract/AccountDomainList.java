package com.emc.caspian.ccs.account.datacontract;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountDomainList {
  private List<AccountDomain> domains;

  public AccountDomainList(final List<AccountDomain> domains) {
    this.domains = domains;
  }

  @JsonProperty
  public List<AccountDomain> getDomains() {
    return domains;
  }

  @JsonProperty
  public void setDomains(final List<AccountDomain> domains) {
    this.domains = domains;
  }
}
