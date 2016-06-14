package com.emc.caspian.ccs.account.datacontract;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AccountList {

  private List<Account> accounts;

  public AccountList(final List<Account> accounts) {
    this.accounts = accounts;
  }

  @JsonProperty
  public List<Account> getAccounts() {
    return accounts;
  }

  @JsonProperty
  public void setAccounts(final List<Account> accounts) {
    this.accounts = accounts;
  }

}
