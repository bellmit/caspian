package com.emc.caspian.ccs.account.datacontract;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class IdpList implements Iterable<Idp>, Serializable{
  @JsonProperty("identity_providers")
  private List<Idp> identityProviders;

  public IdpList()
  {
	  
  }
  
  public IdpList(final List<Idp> identity_providers) {
    this.identityProviders = identity_providers;
  }

  public List<Idp> getIdentityProviders() {
    return identityProviders;
  }

  public void setIdentityProviders(final List<Idp> identity_providers) {
    this.identityProviders = identity_providers;
  }
  
  @Override
	public String toString() {
		return "Idps [list=" + identityProviders + "]";
	}
  @Override
	public Iterator<Idp> iterator() {
		return identityProviders.iterator();
	}
}
