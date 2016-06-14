package com.emc.caspian.ccs.keystone.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.RestClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.Group;
import com.emc.caspian.ccs.keystone.model.Groups;

public class KeyStoneGroupClient {

  private static final Logger _log = LoggerFactory.getLogger(KeyStoneGroupClient.class);

  private RestClient client;

  public KeyStoneGroupClient(RestClient client) {
    this.client = client;
  }

  public ClientResponse<Groups> listGroupsForUser(String authenticationToken, String userId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<Groups> response =
        this.client.get(Groups.class, PathConstants.LIST_GROUPS_USER_DOMAIN_V3, requestHeader, userId);

    _log.debug("Received response : {} for List Groups", response.getStatus());
    return response;

  }

  public ClientResponse<Group> addGroup(String authenticationToken, Group group) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<Group> response =
        this.client.post(Group.class, group, PathConstants.KEYSTONE_GROUP_PATH_V3, requestHeader);

    _log.debug("Received response : {} for create Group", response.getStatus());
    return response;
  }

  public ClientResponse<String> addUserGroup(String authenticationToken, String groupId, String userId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<String> response =
        this.client.put(String.class, PathConstants.KEYSTONE_GROUP_USER_PATH_V3, requestHeader, groupId, userId);

    _log.debug("Received response : {} for add user to group", response.getStatus());
    return response;
  }

  public ClientResponse<String> removeGroup(String authenticationToken, String groupId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<String> response =
        this.client.delete(String.class, PathConstants.KEYSTONE_GROUP_ID_PATH_V3, requestHeader, groupId);

    _log.debug("Received response : {} for delete Group", response.getStatus());
    return response;
  }

  public ClientResponse<String> removeUserGroup(String authenticationToken, String groupId, String userId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<String> response =
        this.client.delete(String.class, PathConstants.KEYSTONE_GROUP_USER_PATH_V3, requestHeader, groupId, userId);

    _log.debug("Received response : {} for delete User from group", response.getStatus());
    return response;

  }

  public ClientResponse<Groups> getAllGroupsForDomain(String authenticationToken, String domainId) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);
    QueryParams params = new QueryParams();
    params.addQueryParam(PathConstants.DOMAIN_ID_QUERY_PARAM, domainId);
    ClientResponse<Groups> response =
        this.client.get(Groups.class, params, PathConstants.GROUPS_DOMAIN_V3, requestHeader);

    _log.debug("Received response : {} for get all groups for a domain", response.getStatus());
    return response;
  }
}
