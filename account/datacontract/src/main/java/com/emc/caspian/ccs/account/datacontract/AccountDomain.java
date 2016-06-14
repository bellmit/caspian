/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.datacontract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by gulavb on 3/2/2015.
 */
public class AccountDomain {

  public AccountDomain() {
    this.accountId = null;
    this.isPrimary = null;
  }

  public AccountDomain(String accountid, Boolean isPrimary) {
    this.accountId = accountid;
    this.isPrimary = isPrimary;
  }
  
  @JsonProperty("account_id")
  public String getAccountId() {
    return accountId;
  }

  @JsonProperty("account_id")
  public void setAccountId(String accountid) {
    this.accountId = accountid;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty("is_primary")
  public Boolean getIs_Primary() {
    return isPrimary;
  }

  @JsonProperty("is_primary")
  public void setIs_Primary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  private String accountId;
  private Boolean isPrimary;
}
