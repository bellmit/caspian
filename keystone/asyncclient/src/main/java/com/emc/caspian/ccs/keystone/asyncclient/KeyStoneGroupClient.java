package com.emc.caspian.ccs.keystone.asyncclient;

import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.ASyncRestClient;
import com.emc.caspian.ccs.client.ClientResponseCallback;
import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.Group;
import com.emc.caspian.ccs.keystone.model.Groups;
import com.fasterxml.jackson.core.JsonProcessingException;

public class KeyStoneGroupClient {

  private static final Logger _log = LoggerFactory.getLogger(KeyStoneGroupClient.class);

  private ASyncRestClient client;

  public KeyStoneGroupClient(ASyncRestClient client) {
    this.client = client;
  }

  public Future<ClientResponse<Groups>> listGroupsForUser(ClientResponseCallback<Groups> callback, String authenticationToken, String userId) {

    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    return this.client.get(Groups.class, PathConstants.LIST_GROUPS_USER_DOMAIN_V3, requestHeader,callback,
            userId);

  }

  public Future<ClientResponse<Group>> addGroup(ClientResponseCallback<Group> callback, String authenticationToken,
      Group group) throws KeystoneClientException {

    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    try {
      return this.client.post(Group.class, PathConstants.KEYSTONE_GROUP_PATH_V3, requestHeader, group, callback);

    } catch (JsonProcessingException e) {
      _log.warn("Exception occured during ADD Group {}", e);
      throw new KeystoneClientException(e);
    }
  }

  public  Future<ClientResponse<String>> addUserGroup(ClientResponseCallback<String> callback, String authenticationToken, String groupId, String userId) throws KeystoneClientException {

    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

          return this.client.put(String.class, PathConstants.KEYSTONE_GROUP_USER_PATH_V3, requestHeader, callback, groupId, userId);

  }

  public  Future<ClientResponse<String>> removeGroup(ClientResponseCallback<String> callback,
      String authenticationToken, String groupId) {

    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    return this.client.delete(String.class, PathConstants.KEYSTONE_GROUP_ID_PATH_V3, requestHeader, callback,
        groupId);

  }

  public  Future<ClientResponse<String>> removeUserGroup(ClientResponseCallback<String> callback,
      String authenticationToken, String groupId, String userId) {

    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    return this.client.delete(String.class, PathConstants.KEYSTONE_GROUP_USER_PATH_V3, requestHeader, callback,
        groupId, userId);

  }
  
  public Future<ClientResponse<Groups>> getAllGroupsForDomain(ClientResponseCallback<Groups> callback,
      String authenticationToken, String domainId) {
    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    QueryParams params = new QueryParams();
    params.addQueryParam(PathConstants.DOMAIN_ID_QUERY_PARAM, domainId);

    return this.client.get(Groups.class, PathConstants.GROUPS_DOMAIN_V3, requestHeader, params, callback);
  }
}
