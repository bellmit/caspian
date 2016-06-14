package com.emc.caspian.ccs.keystone.asyncclient;

import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;

import com.emc.caspian.ccs.client.ASyncRestClient;
import com.emc.caspian.ccs.client.ClientResponseCallback;
import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.RoleAssignments;
import com.emc.caspian.ccs.keystone.model.Roles;

public class KeystoneRoleAssignmentClient {

  private ASyncRestClient client;

  public KeystoneRoleAssignmentClient(ASyncRestClient client) {
    this.client = client;
  }

  public Future<ClientResponse<Roles>> listRolesForDomainUser(ClientResponseCallback<Roles> callback, String authenticationToken, String domainId, String userId) {

    return listRoles(callback, PathConstants.LIST_ROLES_USER_DOMAIN_V3, authenticationToken, domainId, userId);
  }

  public Future<ClientResponse<String>> grantRoleToDomainUser(ClientResponseCallback<String> callback,String authenticationToken, String domainId, String userId,
      String roleId) {

    return grantRole(callback, PathConstants.ROLES_USER_DOMAIN_V3, authenticationToken, domainId, userId, roleId);
  }

  public Future<ClientResponse<String>> checkRoleOfDomainUser(ClientResponseCallback<String> callback, String authenticationToken, String domainId, String userId,
      String roleId) {

    return checkRole(callback, PathConstants.ROLES_USER_DOMAIN_V3, authenticationToken, domainId, userId, roleId);
  }

  public Future<ClientResponse<String>> revokeRoleOfDomainUser(ClientResponseCallback<String> callback, String authenticationToken, String domainId, String userId,
      String roleId) {

    return revokeRole(callback, PathConstants.ROLES_USER_DOMAIN_V3, authenticationToken, domainId, userId, roleId);
  }

  public Future<ClientResponse<Roles>> listRolesForDomainGroup(ClientResponseCallback<Roles> callback, String authenticationToken, String domainId, String groupId) {

    return listRoles(callback, PathConstants.LIST_ROLES_GROUP_DOMAIN_V3, authenticationToken, domainId, groupId);
  }

  public Future<ClientResponse<String>> grantRoleToDomainGroup(ClientResponseCallback<String> callback, String authenticationToken, String domainId, String groupId,
      String roleId) {

    return grantRole(callback, PathConstants.ROLES_DOMAIN_GROUP_V3, authenticationToken, domainId, groupId, roleId);
  }

  public Future<ClientResponse<String>> checkRoleOfDomainGroup(ClientResponseCallback<String> callback, String authenticationToken, String domainId, String groupId,
      String roleId) {

    return checkRole(callback, PathConstants.ROLES_DOMAIN_GROUP_V3, authenticationToken, domainId, groupId, roleId);
  }

  public Future<ClientResponse<String>> revokeRoleToDomainGroup(ClientResponseCallback<String> callback, String authenticationToken, String domainId, String groupId,
      String roleId) {

    return revokeRole(callback, PathConstants.ROLES_DOMAIN_GROUP_V3, authenticationToken, domainId, groupId, roleId);
  }

  public Future<ClientResponse<Roles>> getRoles(ClientResponseCallback<Roles> callback,String authenticationToken, String roleName) {
    QueryParams params = new QueryParams();
    if (StringUtils.isNotEmpty(roleName)) {
      params.addQueryParam(PathConstants.NAME_QUERY_PARAM, roleName);
    }
    return listRoles(callback, PathConstants.KEYSTONE_ROLES_PATH_V3, authenticationToken, params);
  }

  private Future<ClientResponse<Roles>> listRoles(ClientResponseCallback<Roles> callback, String path, String authenticationToken, String domainId,
      String userIdOrGroupId) {

    Map<String, String> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    Future<ClientResponse<Roles>> response = this.client.get(Roles.class, path, requestHeader, callback, domainId, userIdOrGroupId);
    return response;
  }

  private Future<ClientResponse<String>> grantRole(ClientResponseCallback<String> callback, String path, String authenticationToken, String domainId,
      String userIdOrGroupId, String roleId) {

    Map<String, String> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    Future<ClientResponse<String>> response =
        this.client.put(String.class, path, requestHeader, callback, domainId, userIdOrGroupId, roleId);

    return response;
  }

  private Future<ClientResponse<String>> checkRole(ClientResponseCallback<String> callback, String path, String authenticationToken, String domainId,
      String userIdOrGroupId, String roleId) {

    Map<String, String> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    Future<ClientResponse<String>> response =
        this.client.head(String.class, path, requestHeader, callback, domainId, userIdOrGroupId, roleId);
    return response;
  }

  private Future<ClientResponse<String>> revokeRole(ClientResponseCallback<String> callback, String path, String authenticationToken, String domain,
      String userIdOrgroupId, String roleId) {

    Map<String, String> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    Future<ClientResponse<String>> response =
        this.client.delete(String.class, path, requestHeader, callback, domain, userIdOrgroupId, roleId);

    return response;
  }

  public Future<ClientResponse<RoleAssignments>> listRoleAssignments(ClientResponseCallback<RoleAssignments> callback, String authenticationToken, String domainId,
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

    return listRoleAssignments(callback, authenticationToken, params);
  }

  private Future<ClientResponse<Roles>> listRoles(ClientResponseCallback<Roles> callback, String path, String authenticationToken, QueryParams params) {
    Map<String, String> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);
    return this.client.get(Roles.class, path, requestHeader, params, callback);
  }

  private Future<ClientResponse<RoleAssignments>> listRoleAssignments(ClientResponseCallback<RoleAssignments> callback, String authenticationToken, QueryParams params) {
    return listRoleAssignments(callback,PathConstants.ROLE_ASSIGNMENT_ID_V3, authenticationToken, params);
  }

  private Future<ClientResponse<RoleAssignments>> listRoleAssignments(ClientResponseCallback<RoleAssignments> callback, String path, String authenticationToken,
      QueryParams params) {

    Map<String, String> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    Future<ClientResponse<RoleAssignments>> response = this.client.get(RoleAssignments.class, path, requestHeader, params, callback);

    return response;
  }

}
