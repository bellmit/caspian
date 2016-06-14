package com.emc.caspian.ccs.account.model;

public class AccountModel {

  public AccountModel() {
    this.id = this.state = null;
  }

  public AccountModel(String id, String state) {
    this.id = id;
    this.state = state;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getAccount_id() {
    return account_id;
  }

  public void setAccount_id(String account_id) {
    this.account_id = account_id;
  }

  private String id;
  private String state;
  private String account_id;
}
