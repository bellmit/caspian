package com.emc.caspian.ccs.keystone.client;

import java.util.HashMap;
import java.util.Map;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.RestClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.User;
import com.emc.caspian.ccs.keystone.model.Users;

public class KeystoneUserClient {

  private RestClient client;
  
  public KeystoneUserClient(RestClient client) {
    this.client = client;
  }

  public ClientResponse<User> createUser(String authenticationToken, User user) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<User> createResponse = this.client.post(User.class, user, PathConstants.KEYSTONE_USER_PATH_V3, requestHeader);
    return createResponse;

  }
  
  public ClientResponse<Users> getAllUsersForDomain(String authenticationToken, String domainId) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);
    QueryParams params = new QueryParams();
    params.addQueryParam(PathConstants.DOMAIN_ID_QUERY_PARAM, domainId);
    ClientResponse<Users> users = this.client.get(Users.class, params, PathConstants.USERS_DOMAIN_V3, requestHeader);
    return users;
  }
}
