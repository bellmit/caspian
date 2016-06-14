/**
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.datacontract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Created by gulavb on 2/26/2015.
 */
public class Account {

  public Account() {
    this.id = this.name = this.description = null;
    this.active = null;
    this.link = null;
    this.state = null;
  }

  public Account(String id, String name, String description, Boolean active) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.active = active;
    this.state = null;
  }

  public Account(String id, String name, String description, String state, Boolean enabled) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.state = state;
    this.enabled = enabled;
  }

  @JsonProperty
  public String getId() {
    return id;
  }

  @JsonProperty
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
  
  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty
  public String getName() {
    return name;
  }

  @JsonProperty
  public void setName(String name) {
    this.name = name;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty
  public String getDescription() {
    return description;
  }

  @JsonProperty
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty
  public Boolean getActive() {
    return active;
  }

  @JsonProperty
  public void setActive(Boolean active) {
    this.active = active;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty
  public String getState() {
    return state;
  }

  @JsonProperty
  public void setState(String state) {
    this.state = state;
  }

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty
  public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  private String id;
  private Link link;
  private String name;
  private String description;
  private Boolean active;
  private String state;
  private Boolean enabled;
}
