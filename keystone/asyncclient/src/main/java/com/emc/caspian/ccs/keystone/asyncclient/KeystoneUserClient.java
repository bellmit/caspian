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
import com.emc.caspian.ccs.keystone.model.User;
import com.emc.caspian.ccs.keystone.model.Users;
import com.fasterxml.jackson.core.JsonProcessingException;

public class KeystoneUserClient {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneUserClient.class);

  private ASyncRestClient client;

  public KeystoneUserClient(ASyncRestClient client) {
    this.client = client;
  }

  public Future<ClientResponse<User>> createUser(ClientResponseCallback<User> callback,
      String authenticationToken, User user) throws KeystoneClientException {
    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    try {
      return  this.client.post(User.class, PathConstants.KEYSTONE_USER_PATH_V3, requestHeader, user, callback);
    } catch (JsonProcessingException e) {
      _log.warn("Exception occured during CREATE user {}", e);
      throw new KeystoneClientException(e);
    }

  }

  public Future<ClientResponse<Users>> getAllUsersForDomain(ClientResponseCallback<Users> callback,
      String authenticationToken, String domainId) {
    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);
    QueryParams params = new QueryParams();
    params.addQueryParam(PathConstants.DOMAIN_ID_QUERY_PARAM, domainId);
    Future<ClientResponse<Users>> users =
        this.client.get(Users.class, PathConstants.USERS_DOMAIN_V3, requestHeader, params, callback, domainId);
    return users;
  }
}
