package com.emc.caspian.ccs.keystone.client;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.RestClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.RoleAssignments;
import com.emc.caspian.ccs.keystone.model.Roles;

public class KeystoneRoleAssignmentClient {

  private RestClient client;

  public KeystoneRoleAssignmentClient(RestClient client) {
    this.client = client;
  }

  public ClientResponse<Roles> listRolesForDomainUser(String authenticationToken, String domainId, String userId) {

    return listRoles(PathConstants.LIST_ROLES_USER_DOMAIN_V3, authenticationToken, domainId, userId);
  }

  public ClientResponse<String> grantRoleToDomainUser(String authenticationToken, String domainId, String userId,
      String roleId) {

    return grantRole(PathConstants.ROLES_USER_DOMAIN_V3, authenticationToken, domainId, userId, roleId);
  }

  public ClientResponse<String> checkRoleOfDomainUser(String authenticationToken, String domainId, String userId,
      String roleId) {

    return checkRole(PathConstants.ROLES_USER_DOMAIN_V3, authenticationToken, domainId, userId, roleId);
  }

  public ClientResponse<String> revokeRoleOfDomainUser(String authenticationToken, String domainId, String userId,
      String roleId) {

    return revokeRole(PathConstants.ROLES_USER_DOMAIN_V3, authenticationToken, domainId, userId, roleId);
  }

  public ClientResponse<Roles> listRolesForDomainGroup(String authenticationToken, String domainId, String groupId) {

    return listRoles(PathConstants.LIST_ROLES_GROUP_DOMAIN_V3, authenticationToken, domainId, groupId);
  }

  public ClientResponse<String> grantRoleToDomainGroup(String authenticationToken, String domainId, String groupId,
      String roleId) {

    return grantRole(PathConstants.ROLES_DOMAIN_GROUP_V3, authenticationToken, domainId, groupId, roleId);
  }

  public ClientResponse<String> checkRoleOfDomainGroup(String authenticationToken, String domainId, String groupId,
      String roleId) {

    return checkRole(PathConstants.ROLES_DOMAIN_GROUP_V3, authenticationToken, domainId, groupId, roleId);
  }

  public ClientResponse<String> revokeRoleToDomainGroup(String authenticationToken, String domainId, String groupId,
      String roleId) {

    return revokeRole(PathConstants.ROLES_DOMAIN_GROUP_V3, authenticationToken, domainId, groupId, roleId);
  }

  public ClientResponse<Roles> getRoles(String authenticationToken, String roleName) {
    QueryParams params = new QueryParams();
    if (StringUtils.isNotEmpty(roleName)) {
      params.addQueryParam(PathConstants.NAME_QUERY_PARAM, roleName);
    }
    return listRoles(PathConstants.KEYSTONE_ROLES_PATH_V3, authenticationToken, params);
  }

  private ClientResponse<Roles> listRoles(String path, String authenticationToken, String domainId,
      String userIdOrGroupId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<Roles> response = this.client.get(Roles.class, path, requestHeader, domainId, userIdOrGroupId);

    return response;
  }

  private ClientResponse<String> grantRole(String path, String authenticationToken, String domainId,
      String userIdOrGroupId, String roleId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<String> response =
        this.client.put(String.class, path, requestHeader, domainId, userIdOrGroupId, roleId);

    return response;
  }

  private ClientResponse<String> checkRole(String path, String authenticationToken, String domainId,
      String userIdOrGroupId, String roleId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<String> response =
        this.client.head(String.class, path, requestHeader, domainId, userIdOrGroupId, roleId);
    return response;
  }

  private ClientResponse<String> revokeRole(String path, String authenticationToken, String domain,
      String userIdOrgroupId, String roleId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<String> response =
        this.client.delete(String.class, path, requestHeader, domain, userIdOrgroupId, roleId);

    return response;
  }

  public ClientResponse<RoleAssignments> listRoleAssignments(String authenticationToken, String domainId,
      String projectId, String roleId, String userId, String groupId) {
    QueryParams params = new QueryParams();

    if (domainId != null) {
      params.addQueryParam(PathConstants.SCOPE_DOMAIN_ID_QUERY_PARAM, domainId);
    }
    if (projectId != null) {
      params.addQueryParam(PathConstants.SCOPE_PROJECT_ID_QUERY_PARAM, projectId);
    }
    if (roleId != null) {
      params.addQueryParam(PathConstants.ROLE_ID_QUERY_PARAM, roleId);
    }
    if (userId != null) {
      params.addQueryParam(PathConstants.USER_ID_QUERY_PARAM, userId);
    }
    if (groupId != null) {
      params.addQueryParam(PathConstants.GROUP_ID_QUERY_PARAM, groupId);
    }

    return listRoleAssignments(authenticationToken, params);
  }

  private ClientResponse<Roles> listRoles(String path, String authenticationToken, QueryParams params) {
    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);
    ClientResponse<Roles> response = this.client.get(Roles.class, params, path, requestHeader);
    return response;
  }

  private ClientResponse<RoleAssignments> listRoleAssignments(String authenticationToken, QueryParams params) {
    return listRoleAssignments(PathConstants.ROLE_ASSIGNMENT_ID_V3, authenticationToken, params);
  }

  private ClientResponse<RoleAssignments> listRoleAssignments(String path, String authenticationToken,
      QueryParams params) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<RoleAssignments> response = this.client.get(RoleAssignments.class, params, path, requestHeader);

    return response;
  }

}
