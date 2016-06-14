package com.emc.caspian.client;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.client.KeyStoneGroupClient;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Group;
import com.emc.caspian.ccs.keystone.model.Groups;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.client.util.KeyStoneTestUtil;
import com.emc.caspian.fabric.config.Configuration;

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
  public static void setup() {

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

    token = ksClient.getToken(authentication, false).getHttpResponse().getResponseBody().getTokenString();
    tokenObj = ksClient.validateToken(token, token, false).getHttpResponse().getResponseBody();
    userId = tokenObj.getUser().getId();

  }

  @Test
  public void testGetUserGroups() {
    ClientResponse<Groups> groups = getUserGroups(groupClient, token, userId);
    Assert.assertEquals(ClientStatus.SUCCESS, groups.getStatus());
    _log.info("Groups {}", groups);
  }

  @Test
  public void testCreateGroup() {

    Group groupCreateRequest =
        KeyStoneTestUtil.getGroupCreateRequest("default-group-" + System.currentTimeMillis(), tokenObj.getDomain()
            .getId(), "Test Default group");

    ClientResponse<Group> groupCreateResp = createGroup(groupClient, token, groupCreateRequest);
    Assert.assertEquals(ClientStatus.SUCCESS, groupCreateResp.getStatus());
    Group group = groupCreateResp.getHttpResponse().getResponseBody();
    ClientStatus isDeleted = deleteGroup(groupClient, token, group.getId()).getStatus();
    Assert.assertEquals(ClientStatus.SUCCESS, isDeleted);

  }

  @Test
  public void testAddUserToGroup() {
    // create a user group
    Group groupCreateRequest =
        KeyStoneTestUtil.getGroupCreateRequest("default-group-" + System.currentTimeMillis(), tokenObj.getDomain()
            .getId(), "Test Default group");

    ClientResponse<Group> groupCreateResp = createGroup(groupClient, token, groupCreateRequest);
    Assert.assertEquals(ClientStatus.SUCCESS, groupCreateResp.getStatus());
    _log.info("Created Group");
    
    Group group = groupCreateResp.getHttpResponse().getResponseBody();
    // associate the user to group
    ClientStatus isAdded = addUserToGroup(groupClient, token, group.getId(), userId).getStatus();
    Assert.assertEquals(ClientStatus.SUCCESS, isAdded);

    ClientStatus isRemoved = removeUserFromGroup(groupClient, token, group.getId(), userId).getStatus();
    Assert.assertEquals(ClientStatus.SUCCESS, isRemoved);

    // get a new token before deleting the group
    Token tokenObject =  ksClient.getToken(authentication, false).getHttpResponse().getResponseBody();
    token = tokenObject.getTokenString();
    // delete the group
    deleteGroup(groupClient, token, group.getId());
  }


  public ClientResponse<Groups> getUserGroups(KeyStoneGroupClient groupClient, String authenticationToken, String userID) {

    return groupClient.listGroupsForUser(authenticationToken, userID);
  }

  public ClientResponse<Group> createGroup(KeyStoneGroupClient groupClient, String authenticationToken, Group groupCreateRequest) {

    return groupClient.addGroup(authenticationToken, groupCreateRequest);
  }

  public ClientResponse<String> addUserToGroup(KeyStoneGroupClient groupClient, String authenticationToken, String groupId,
      String userId) {

    return groupClient.addUserGroup(authenticationToken, groupId, userId);

  }

  public ClientResponse<String> deleteGroup(KeyStoneGroupClient groupClient, String authenticationToken, String groupId) {
    return groupClient.removeGroup(authenticationToken, groupId);
  }

  public ClientResponse<String> removeUserFromGroup(KeyStoneGroupClient groupClient, String authenticationToken, String groupId,
      String userId) {

    return groupClient.removeUserGroup(authenticationToken, groupId, userId);

  }
}
