package com.emc.caspian.ccs.account.model;

public class PrimaryRoleAssignmentModel {

  public PrimaryRoleAssignmentModel() {
    this.accountId = this.roleId = this.groups = this.users = null;
  }

  public PrimaryRoleAssignmentModel(String accountId, String roleId, String users, String groups) {
    this.accountId = accountId;
    this.roleId = roleId;
    this.users = users;
    this.groups = groups;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  public String getUsers() {
    return users;
  }

  public void setUsers(String users) {
    this.users = users;
  }

  public String getGroups() {
    return groups;
  }

  public void setGroups(String groups) {
    this.groups = groups;
  }

  private String accountId;
  private String roleId;
  private String users;
  private String groups;

}
