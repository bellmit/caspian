package com.emc.caspian.ccs.keystone.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonRootName("domain")
public class Domain {

  @JsonInclude(value = Include.NON_EMPTY)
  private String id;

  @JsonInclude(value = Include.NON_EMPTY)
  private String name;

  @JsonInclude(value = Include.NON_NULL)
  private String description;

  @JsonInclude(value = Include.NON_EMPTY)
  private Boolean enabled;

  @JsonInclude(value = Include.NON_EMPTY)
  @JsonProperty("neutrino_reserved")
  private Boolean neutrinoReserved;

  public Boolean isNeutrinoReserved() {
    return neutrinoReserved;
  }

  public void setNeutrinoReserved(Boolean neutrinoReserved) {
    this.neutrinoReserved = neutrinoReserved;
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

}
