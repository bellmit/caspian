/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */


package com.emc.caspian.ccs.account.model;

public class IdpPasswordModel {

  public IdpPasswordModel() {
    idpId = idpUser = idpPwd = null;
  }

  public IdpPasswordModel(String idpId, String idpUser, String idpPwd) {
    this.idpId = idpId;
    this.idpUser = idpUser;
    this.idpPwd = idpPwd;
  }

  public String getIdpId() {
    return idpId;
  }

  public void setIdpId(String idpId) {
    this.idpId = idpId;
  }

  public String getIdpUser() {
    return idpUser;
  }

  public void setIdpUser(String idpUser) {
    this.idpUser = idpUser;
  }

  public String getIdpPwd() {
    return idpPwd;
  }

  public void setIdpPwd(String idpPwd) {
    this.idpPwd = idpPwd;
  }

  private String idpId;
  private String idpUser;
  private String idpPwd;

}
