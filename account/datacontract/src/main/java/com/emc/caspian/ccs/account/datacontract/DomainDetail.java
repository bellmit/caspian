/**
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

public class DomainDetail {

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  @JsonProperty("link")
  public Link getLink() {
    return link;
  }

  @JsonProperty("link")
  public void setLink(Link link) {
    this.link = link;
  }

  @JsonInclude(value = Include.NON_NULL)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonInclude(value = Include.NON_NULL)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  public Boolean getEnabled() {
    return enabled;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty("is_primary")
  public Boolean getPrimary() {
    return isPrimary;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty("is_primary")
  public void setPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty("is_present")
  public Boolean getIsPresent() {
    return isPresent;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty("is_present")
  public void setIsPresent(Boolean isPresent) {
    this.isPresent = isPresent;
  }

  @JsonProperty("account_id")
  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  @JsonProperty("account_id")
  public String getAccountId() {
    return accountId;
  }

  @JsonProperty("account_name")
  public void setAccountName(String accountName) {
    this.accountName = accountName;
  }

  @JsonProperty("account_name")
  public String getAccountName() {
    return accountName;
  }
  
  private String id;
  private String name;
  private String description;
  private Boolean enabled;
  private Boolean isPrimary;
  private Boolean isPresent;
  private String accountId;
  private String accountName;
  private Link link;
}
