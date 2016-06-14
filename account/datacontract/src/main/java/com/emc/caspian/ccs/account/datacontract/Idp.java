package com.emc.caspian.ccs.account.datacontract;

import com.emc.caspian.ccs.account.types.IdpType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Idp {

  public Idp() {
    this.id = null;
    this.type = null;
    this.idpConfig = null;
  }

  public Idp(String id, IdpType type, IdpConfig idpConfig) {
    this.id = id;
    this.type = type;
    this.idpConfig = idpConfig;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
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

  @JsonProperty("type")
  public IdpType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(IdpType type) {
    this.type = type;
  }

  @JsonProperty("idp_info")
  public IdpConfig getIdpConfig() {
    return idpConfig;
  }

  @JsonProperty("idp_info")
  public void setIdpConfig(IdpConfig idpConfig) {
    this.idpConfig = idpConfig;
  }

  private String id;
  private Link link;
  private IdpType type;
  private IdpConfig idpConfig;
}
