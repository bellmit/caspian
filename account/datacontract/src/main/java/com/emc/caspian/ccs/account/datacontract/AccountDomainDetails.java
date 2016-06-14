/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.datacontract;

import java.util.List;

import com.emc.caspian.ccs.account.datacontract.DomainDetail;

/**
 * Created by harisa on 4/14/2015.
 */

public class AccountDomainDetails {

  public List<DomainDetail> getDomains() {
    return domains;
  }

  public void setDomains(List<DomainDetail> domain) {
    this.domains = domain;
  }

  private List<DomainDetail> domains;

}
