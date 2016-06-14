package com.emc.caspain.ccs.common.webfilters;

import java.security.Principal;
import java.util.List;

/**
 * Created by shivesh on 3/10/15.
 */
public class KeystonePrincipal implements Principal {
  public String getAuthToken() {
    return authToken;
  }

  public String getDomainId() {
    return domainId;
  }

  public String getDomainName() {
    return domainName;
  }

  public String getProjectId() {
    return projectId;
  }

  public String getProjectName() {
    return projectName;
  }

  public String getProjectDomainId() {
    return projectDomainId;
  }

  public String getProjectDomainName() {
    return projectDomainName;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserDomainId() {
    return userDomainId;
  }

  public String getUserDomainName() {
    return userDomainName;
  }

  public List<String> getRoles() {
    return roles;
  }

  private final String authToken;
  private final String domainId;
  private final String domainName;
  private final String projectId;
  private final String projectName;
  private final String projectDomainId;
  private final String projectDomainName;
  private final String userId;
  private final String userName;
  private final String userDomainId;
  private final String userDomainName;
  private final List<String> roles;

  private KeystonePrincipal() {

    authToken = null;
    domainId = null;
    domainName = null;
    projectId = null;
    projectName = null;
    projectDomainId = null;
    projectDomainName = null;
    userId = null;
    userName = null;
    userDomainId = null;
    userDomainName = null;
    roles = null;
  }

  public KeystonePrincipal(final String authToken, final String domainId, final String domainName,
      final String projectId, final String projectName, final String projectDomainId, final String projectDomainName,
      final String userId, final String userName, final String userDomainId, final String userDomainName,
      final List<String> roles) {

    this.authToken = authToken;
    this.domainId = domainId;
    this.domainName = domainName;
    this.projectId = projectId;
    this.projectName = projectName;
    this.projectDomainId = projectDomainId;
    this.projectDomainName = projectDomainName;
    this.userId = userId;
    this.userName = userName;
    this.userDomainId = userDomainId;
    this.userDomainName = userDomainName;
    this.roles = roles;
  }

  @Override
  public String getName() {
    return userName;
  }

  @Override
  public String toString() {
    return "KeystonePricipal [domainId=" + domainId + ", domainName=" + domainName + ", projectId=" + projectId
        + ", projectName=" + projectName + ", projectDomainId=" + projectDomainId + ", projectDomainName="
        + projectDomainName + ", userId=" + userId + ", userName=" + userName + ", userDomainId=" + userDomainId
        + ", roles=" + roles + "]";
  }

  public static final KeystonePrincipal EMPTY = new KeystonePrincipal();
}
