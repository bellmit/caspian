package com.emc.caspian.ccs.keystone.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class DomainConfigInfo {
  private IdentityDriver identity;
  private LdapConfig ldap;
  
  public DomainConfigInfo() {
    
  }
  
  @JsonProperty("identity")
  public IdentityDriver getIdentityDriver() {
    return identity;
  }
  @JsonProperty("identity")
  public void setIdentityDriver(IdentityDriver driver) {
    this.identity = driver;
  }
  
  @JsonProperty("ldap")
  public LdapConfig getLdapConfig() {
    return ldap;
  }
  @JsonProperty("ldap")
  public void setLdapConfig(LdapConfig ldap) {
    this.ldap = ldap;
  }
}
