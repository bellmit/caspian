package com.emc.caspian.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.ClientResponseCallback;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.asyncclient.KeyStoneGroupClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClientException;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.common.KeystoneDeserializationUtils;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Group;
import com.emc.caspian.ccs.keystone.model.Groups;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.client.util.KeyStoneTestUtil;
import com.emc.caspian.fabric.config.Configuration;
import com.rsa.cryptoj.o.fu;

public class KeyStoneGroupTest {

  private static final Logger _log = LoggerFactory.getLogger(KeyStoneGroupTest.class);

  private static KeystoneClient eciClient;
  private static KeystoneTokenClient ksClient;
  private static KeyStoneGroupClient groupClient;
  private static Authentication authentication;

  private static String token;
  private static Token tokenObj;
  private static String userId;
  

  @BeforeClass
  public static void setup() throws InterruptedException {

    final String testConfigPath = "src/test/resources/test.properties";

    try {
      Configuration.load(testConfigPath);
    } catch (Exception e) {
      throw (new RuntimeException("Test configuration file missing"));
    }


    eciClient =
        new KeystoneClient(TestProperties.getServerName(), TestProperties.getPort(),
            TestProperties.getKeystoneIgnoreCertificate());

    ksClient = eciClient.getKeystoneTokenClient();
    groupClient = eciClient.getKeystoneGroupClient();

    authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN, TestProperties.getUser(),
            TestProperties.getPassword());

    ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    
    ksClient.getToken(tokenCallback, authentication, true);
    
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    
    tokenObj = tokenCallback.getResponse().getHttpResponse().getResponseBody();  
    Assert.assertNotNull(tokenObj);
    token = tokenObj.getTokenString();
    Assert.assertNotNull(token);
    
    userId = tokenObj.getUser().getId();

  }

  @Test
  public void testGetUserGroups() throws InterruptedException {
	  
	ClientResponseHandler<Groups> groupsCallback = new ClientResponseHandler<Groups>();
	
    getUserGroups(groupsCallback, groupClient, token, userId);
    synchronized (groupsCallback) {
    	groupsCallback.wait(TestProperties.getWaitTimeout());
    }
    Assert.assertEquals(ClientStatus.SUCCESS, groupsCallback.getResponse().getStatus());
    Groups groups = groupsCallback.getResponse().getHttpResponse().getResponseBody();
    _log.info("Groups {}", groups);

  }
  
  @Test
  public void testFutureGetUserGroups() throws InterruptedException, ExecutionException {

    Future<ClientResponse<Groups>> future = getUserGroups(null, groupClient, token, userId);

    ClientResponse<Groups> clientResponse = future.get();

    Assert.assertEquals(ClientStatus.SUCCESS, clientResponse.getStatus());
    Groups groups = clientResponse.getHttpResponse().getResponseBody();
    _log.info("Groups {}", groups);
  }

  @Test
  public void testCreateGroup() throws InterruptedException, KeystoneClientException {

    Group groupCreateRequest =
        KeyStoneTestUtil.getGroupCreateRequest("default-group-" + System.currentTimeMillis(), tokenObj.getDomain()
            .getId(), "Test Default group");
    
	ClientResponseHandler<Group> groupCreateCallback = new ClientResponseHandler<Group>();
	
    createGroup(groupCreateCallback, groupClient, token, groupCreateRequest);
    synchronized (groupCreateCallback) {
    	groupCreateCallback.wait(TestProperties.getWaitTimeout());
    }
    Assert.assertEquals(ClientStatus.SUCCESS, groupCreateCallback.getResponse().getStatus());
    Group group = groupCreateCallback.getResponse().getHttpResponse().getResponseBody();
   
    ClientResponseHandler<String> groupDeleteCallback = new ClientResponseHandler<String>();
    deleteGroup(groupDeleteCallback, groupClient, token, group.getId());
    synchronized (groupDeleteCallback) {
    	groupDeleteCallback.wait(TestProperties.getWaitTimeout());
    }
    
    Assert.assertEquals(ClientStatus.SUCCESS, groupDeleteCallback.getResponse().getStatus());

  }
  
  @Test
  public void testFutureCreateGroup() throws KeystoneClientException, InterruptedException,
      ExecutionException, TimeoutException {

    Group groupCreateRequest =
        KeyStoneTestUtil.getGroupCreateRequest("default-group-" + System.currentTimeMillis(),
            tokenObj.getDomain().getId(), "Test Default group");

    Future<ClientResponse<Group>> future =
        createGroup(null, groupClient, token, groupCreateRequest);

    ClientResponse<Group> clientResponse = future.get(5000, TimeUnit.MILLISECONDS);
    Assert.assertEquals(ClientStatus.SUCCESS, clientResponse.getStatus());
    Group group = clientResponse.getHttpResponse().getResponseBody();

    Future<ClientResponse<String>> delFuture = deleteGroup(null, groupClient, token, group.getId());
    ClientResponse<String> response = delFuture.get();

    Assert.assertEquals(ClientStatus.SUCCESS, response.getStatus());

  }

  @Test
  public void testAddUserToGroup() throws InterruptedException, KeystoneClientException {
    // create a user group
    Group groupCreateRequest =
        KeyStoneTestUtil.getGroupCreateRequest("default-group-" + System.currentTimeMillis(), tokenObj.getDomain()
            .getId(), "Test Default group");

    ClientResponseHandler<Group> groupCreateCallback = new ClientResponseHandler<Group>();
    
    createGroup(groupCreateCallback, groupClient, token, groupCreateRequest);
    synchronized (groupCreateCallback) {
    	groupCreateCallback.wait(TestProperties.getWaitTimeout());
    }
    
    Assert.assertEquals(ClientStatus.SUCCESS, groupCreateCallback.getResponse().getStatus());
    _log.info("Created Group");
    
    Group group = groupCreateCallback.getResponse().getHttpResponse().getResponseBody();
    
    // associate the user to group
    ClientResponseHandler<String> groupAddUserCallback = new ClientResponseHandler<String>();
    addUserToGroup(groupAddUserCallback, groupClient, token, group.getId(), userId);
    synchronized (groupAddUserCallback) {
    	groupAddUserCallback.wait(TestProperties.getWaitTimeout());
    }
    
    ClientStatus isAdded = groupAddUserCallback.getResponse().getStatus();
    Assert.assertEquals(ClientStatus.SUCCESS, isAdded);
    
    ClientResponseHandler<String> groupRemoveUserCallback = new ClientResponseHandler<String>();
    removeUserFromGroup(groupRemoveUserCallback, groupClient, token, group.getId(), userId);
    synchronized (groupRemoveUserCallback) {
    	groupRemoveUserCallback.wait(TestProperties.getWaitTimeout());
    }
    
    ClientStatus isRemoved = groupRemoveUserCallback.getResponse().getStatus();
    Assert.assertEquals(ClientStatus.SUCCESS, isRemoved);

    ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    // get a new token before deleting the group
    ksClient.getToken(tokenCallback, authentication, false);
    
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    
    ClientResponse<Token> tokenResp = tokenCallback.getResponse();
    tokenObj = tokenResp.getHttpResponse().getResponseBody();  

    token = tokenObj.getTokenString();

    // delete the group
    ClientResponseHandler<String> groupDeleteCallback = new ClientResponseHandler<String>();
    deleteGroup(groupDeleteCallback, groupClient, token, group.getId());
  }


  public Future<ClientResponse<Groups>> getUserGroups(ClientResponseCallback<Groups> callback, KeyStoneGroupClient groupClient, String authenticationToken, String userID) {

    return groupClient.listGroupsForUser(callback, authenticationToken, userID);
  }

  public Future<ClientResponse<Group>> createGroup(ClientResponseCallback<Group> callback, KeyStoneGroupClient groupClient, String authenticationToken, Group groupCreateRequest) throws KeystoneClientException {

    return groupClient.addGroup(callback, authenticationToken, groupCreateRequest);
  }

  public Future<ClientResponse<String>>  addUserToGroup(ClientResponseCallback<String> callback, KeyStoneGroupClient groupClient, String authenticationToken, String groupId,
      String userId) throws KeystoneClientException {

    return groupClient.addUserGroup(callback, authenticationToken, groupId, userId);

  }

  public Future<ClientResponse<String>>  deleteGroup(ClientResponseCallback<String> callback, KeyStoneGroupClient groupClient, String authenticationToken, String groupId) {
    return groupClient.removeGroup(callback, authenticationToken, groupId);
  }

  public Future<ClientResponse<String>>  removeUserFromGroup(ClientResponseCallback<String> callback, KeyStoneGroupClient groupClient, String authenticationToken, String groupId,
      String userId) {

    return groupClient.removeUserGroup(callback, authenticationToken, groupId, userId);

  }
}
