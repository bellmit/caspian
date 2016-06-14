package com.emc.caspian.client;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.asyncclient.KeyStoneGroupClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClientException;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneRoleAssignmentClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Group;
import com.emc.caspian.ccs.keystone.model.Role;
import com.emc.caspian.ccs.keystone.model.Roles;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.client.util.KeyStoneTestUtil;
import com.emc.caspian.fabric.config.Configuration;


public class KeyStoneDomainRoleAssignmentTest {

  private static final Logger _log = LoggerFactory.getLogger(KeyStoneDomainRoleAssignmentTest.class);

  private static KeystoneClient eciClient;
  private static KeystoneTokenClient ksClient;
  private static KeystoneRoleAssignmentClient roleAssignmentClient;
  private static KeyStoneGroupClient groupClient;
  private static Authentication authentication;

  private static String token;
  private static Token tokenObj;
  private static String groupId;
  private static String domainId;
  private static String adminRoleId = "d8fdd6537bfb49459b61faf65ec6dc27";

  @BeforeClass
  public static void setup() throws KeystoneClientException, InterruptedException {

    /**
     * Create a Keystone client and get a token for the usage create group which will be used for the role assignment
     * operations.
     */
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

    roleAssignmentClient = eciClient.getKeystoneRoleAssignmentClient();

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

    _log.info("Token: {}", token);
    
    ksClient.validateToken(tokenCallback, token, token, false);
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    
    Assert.assertEquals(ClientStatus.SUCCESS, tokenCallback.getResponse().getStatus());

    tokenObj = tokenCallback.getResponse().getHttpResponse().getResponseBody();  
  
    Assert.assertNotNull(tokenObj);

    // To check for the List roles for domain group
    // first create a group inside a domain and then ask for listRoles in domain by group id
    // second grant role to the group created
    groupClient = eciClient.getKeystoneGroupClient();
    Group groupCreateRequest =
        KeyStoneTestUtil.getGroupCreateRequest("default-group-" + System.currentTimeMillis(), tokenObj.getDomain()
            .getId(), "Test Default group");

    ClientResponseHandler<Group> groupCallback = new ClientResponseHandler<Group>();
    groupClient.addGroup(groupCallback, token, groupCreateRequest);
    synchronized (groupCallback) {
    	groupCallback.wait(TestProperties.getWaitTimeout());
    }
    
    
    Group groupCreateResponse = groupCallback.getResponse().getHttpResponse().getResponseBody();
    Assert.assertNotNull(groupCreateResponse);
    
    _log.info("Created a Group : {} under Domain : {}", groupCreateResponse.getId(), groupCreateResponse.getDomainId());
    groupId = groupCreateResponse.getId();
    domainId = groupCreateResponse.getDomainId();
    
    ClientResponseHandler<Roles> rolesListHandler = new ClientResponseHandler<Roles>();
    listRolesforDomainUser(rolesListHandler, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId());
    synchronized (rolesListHandler) {
    	rolesListHandler.wait(TestProperties.getWaitTimeout());
    }

    Roles roles = rolesListHandler.getResponse().getHttpResponse().getResponseBody();  
    
    Assert.assertNotNull(roles);
    _log.info("Roles {}", roles);

    List<Role> roleList = roles.getList();
    for (Role role : roleList) {
      _log.info("Role ID: {} Role Name : {}", role.getId(), role.getName());
      if (role.getName().equalsIgnoreCase("admin")) {
        adminRoleId = role.getId();
      }
    }

    _log.info("admin role", adminRoleId);
    Assert.assertNotNull(adminRoleId);


  }

  @AfterClass
  public static void teardown() throws InterruptedException {
    /* delete the group created */
	ClientResponseHandler<String> groupRemoveCallback = new ClientResponseHandler<String>();
    groupClient.removeGroup(groupRemoveCallback, token, groupId);
    synchronized (groupRemoveCallback) {
    	groupRemoveCallback.wait(TestProperties.getWaitTimeout());
    }
    
    ClientResponse<String> deleteGroupResp = groupRemoveCallback.getResponse();
    Assert.assertEquals(ClientStatus.SUCCESS, deleteGroupResp.getStatus());

  }

  @Test
  public void listRolesForDomainUserTest() throws InterruptedException {

	ClientResponseHandler<Roles> rolesListDomainsHandler = new ClientResponseHandler<Roles>();
    listRolesforDomainUser(rolesListDomainsHandler, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN,
        tokenObj.getUser().getId());
    
    synchronized (rolesListDomainsHandler) {
    	rolesListDomainsHandler.wait(TestProperties.getWaitTimeout());
    }
    Roles roles = rolesListDomainsHandler.getResponse().getHttpResponse().getResponseBody();

    _log.info("List Roles on user {}", roles);
    Assert.assertNotNull(roles);
  }

  @Test
  public void futureListRolesForDomainUserTest() throws InterruptedException, ExecutionException {

    Future<ClientResponse<Roles>> futureRoles =
        listRolesforDomainUser(null, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN,
            tokenObj.getUser().getId());

    ClientResponse<Roles> clientResponse = futureRoles.get();
    Roles roles = clientResponse.getHttpResponse().getResponseBody();

    _log.info("List Roles on user {}", roles);
    Assert.assertNotNull(roles);
  }
  
  @Test
  public void grantRoleUserTest() throws InterruptedException {

	ClientResponseHandler<String> roleGrantUserHandler = new ClientResponseHandler<String>();
    grantRole(roleGrantUserHandler, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId);
    synchronized (roleGrantUserHandler) {
    	roleGrantUserHandler.wait(TestProperties.getWaitTimeout());
    } 

    ClientResponse<String> grantRoleResp = roleGrantUserHandler.getResponse();    
    Assert.assertEquals(ClientStatus.SUCCESS, grantRoleResp.getStatus());
    roleGrantUserHandler.clearAll();

  }

  @Test
  public void futureGrantRoleUserTest() throws InterruptedException, ExecutionException,
      TimeoutException {

    Future<ClientResponse<String>> futureGrantRole =
        grantRole(null, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser()
            .getId(), adminRoleId);
    ClientResponse<String> clientResponse = futureGrantRole.get(5000, TimeUnit.MILLISECONDS);

    Assert.assertEquals(ClientStatus.SUCCESS, clientResponse.getStatus());

  }
  
  @Test
  public void checkRoleUserTest() throws InterruptedException {

	ClientResponseHandler<String> roleGrantUserHandler = new ClientResponseHandler<String>();
    grantRole(roleGrantUserHandler, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId);
    synchronized (roleGrantUserHandler) {
    	roleGrantUserHandler.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<String> grantRoleResp = roleGrantUserHandler.getResponse();
    Assert.assertEquals(ClientStatus.SUCCESS, grantRoleResp.getStatus());

	ClientResponseHandler<String> checkRoleUserHandler = new ClientResponseHandler<String>();
    checkRole(checkRoleUserHandler, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId);    
    synchronized (checkRoleUserHandler) {
    	checkRoleUserHandler.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<String> checkRoleResp = checkRoleUserHandler.getResponse();
    Assert.assertEquals(ClientStatus.SUCCESS, checkRoleResp.getStatus());    
    _log.info("Check Role on user ? {}", checkRoleResp);

  }

  //@Test
  public void revokeRoleUserTest() throws InterruptedException {

	ClientResponseHandler<String> revokeRoleUserHandler = new ClientResponseHandler<String>();
    revokeRole(revokeRoleUserHandler, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId);
    synchronized (revokeRoleUserHandler) {
    	revokeRoleUserHandler.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<String> revokeRoleResp = revokeRoleUserHandler.getResponse();
    Assert.assertEquals(ClientStatus.SUCCESS, revokeRoleResp.getStatus());
    
    // get a fresh token and use it for authentication as soon after the revokeRole the user will be
    // logged out
    ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    ksClient.getToken(tokenCallback, authentication, false);
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    } 
    
    ClientResponse<Token> tokenResp = tokenCallback.getResponse();
    tokenObj = tokenResp.getHttpResponse().getResponseBody();  
    
    Assert.assertNotNull(tokenObj);
    token = tokenObj.getTokenString();

    ClientResponseHandler<String> grantRoleUserHandler = new ClientResponseHandler<String>();  
    // grant back the same role to admin so that other operations can be done by admin
    grantRole(grantRoleUserHandler, roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId);
    synchronized (grantRoleUserHandler) {
    	grantRoleUserHandler.wait(TestProperties.getWaitTimeout());
    } 
    
    ClientResponse<String> grantRoleResp = grantRoleUserHandler.getResponse();
    Assert.assertEquals(ClientStatus.SUCCESS, grantRoleResp.getStatus());
    _log.info("Grant Role on user ? {}", grantRoleResp);

  }

  @Test
  public void listRolesforDomainGroupTest() throws InterruptedException {

	ClientResponseHandler<Roles> rolesListDomainsHandler = new ClientResponseHandler<Roles>();
    listRolesforDomainGroup(rolesListDomainsHandler, roleAssignmentClient, token, domainId, groupId);
    synchronized (rolesListDomainsHandler) {
    	rolesListDomainsHandler.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<Roles> rolesForDomainResp = rolesListDomainsHandler.getResponse();
    
    Roles rolesForDomain = rolesForDomainResp.getHttpResponse().getResponseBody();
    Assert.assertNotNull(rolesForDomain);
    _log.info("Roles for Domain {}", rolesForDomain);

  }

  @Test
  public void grantRoleDomainGroupTest() throws InterruptedException {

	ClientResponseHandler<String> grantRoleDomainGroup = new ClientResponseHandler<String>();
    grantRoleDomainGroup(grantRoleDomainGroup, roleAssignmentClient, token, domainId, groupId, adminRoleId);
    synchronized (grantRoleDomainGroup) {
    	grantRoleDomainGroup.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<String> grantRoleDomainResp = grantRoleDomainGroup.getResponse();
    _log.info("Grant Role on domain ? {}", grantRoleDomainResp);
    Assert.assertEquals(ClientStatus.SUCCESS, grantRoleDomainResp.getStatus());

  }

  @Test
  public void checkRoleDomainGroupTest() throws InterruptedException {

    // grant the role and then invoke the check role
	ClientResponseHandler<String> grantRoleDomainGroup = new ClientResponseHandler<String>();
    grantRoleDomainGroup(grantRoleDomainGroup, roleAssignmentClient, token, domainId, groupId, adminRoleId);
    synchronized (grantRoleDomainGroup) {
    	grantRoleDomainGroup.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<String> grantRoleDomainResp = grantRoleDomainGroup.getResponse();
    _log.info("Grant Role on domain succeded? {}", grantRoleDomainResp.getStatus());
    Assert.assertEquals(ClientStatus.SUCCESS, grantRoleDomainResp.getStatus());
    
    ClientResponseHandler<String> checkRoleDomainGroup = new ClientResponseHandler<String>();
    checkRoleDomainGroup(checkRoleDomainGroup, roleAssignmentClient, token, domainId, groupId, adminRoleId);
    synchronized (checkRoleDomainGroup) {
    	checkRoleDomainGroup.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<String> checkRoleResp = checkRoleDomainGroup.getResponse();
    _log.info("Check Role on domain succeded? {}", checkRoleResp.getStatus());
    Assert.assertEquals(ClientStatus.SUCCESS, checkRoleResp.getStatus());
    
  }
  
  @Test
  public void futureCheckRoleDomainGroupTest() throws InterruptedException, ExecutionException,
      TimeoutException {

    // grant the role and then invoke the check role
    Future<ClientResponse<String>> futureGrantRole =
        grantRoleDomainGroup(null, roleAssignmentClient, token, domainId, groupId, adminRoleId);

    ClientResponse<String> grantRoleDomainResp = futureGrantRole.get();
    _log.info("Grant Role on domain succeded? {}", grantRoleDomainResp.getStatus());
    Assert.assertEquals(ClientStatus.SUCCESS, grantRoleDomainResp.getStatus());

    Future<ClientResponse<String>> futureCheckRole =
        checkRoleDomainGroup(null, roleAssignmentClient, token, domainId, groupId, adminRoleId);

    ClientResponse<String> checkRoleResp = futureCheckRole.get(5000, TimeUnit.MILLISECONDS);
    _log.info("Check Role on domain succeded? {}", checkRoleResp.getStatus());
    Assert.assertEquals(ClientStatus.SUCCESS, checkRoleResp.getStatus());

  }

  public void revokeRoleDomainGroupTest() throws InterruptedException {
	
	ClientResponseHandler<String> revokeRoleDomainGroup = new ClientResponseHandler<String>();  
    revokeRoleDomainGroup(revokeRoleDomainGroup, roleAssignmentClient, token, domainId, groupId, adminRoleId);
    synchronized (revokeRoleDomainGroup) {
    	revokeRoleDomainGroup.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<String> revokeRoleResp = revokeRoleDomainGroup.getResponse();
    // get a fresh token and use it for authentication as soon after the revokeRole the user will be
    // logged out
    Assert.assertEquals(ClientStatus.SUCCESS, revokeRoleResp.getStatus());

    
    ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    ksClient.getToken(tokenCallback, authentication, false);
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    } 
    
    tokenObj = tokenCallback.getResponse().getHttpResponse().getResponseBody();  
    
    Assert.assertNotNull(tokenObj);
    token = tokenObj.getTokenString();

	ClientResponseHandler<String> grantRoleDomainGroup = new ClientResponseHandler<String>();
    grantRoleDomainGroup(grantRoleDomainGroup, roleAssignmentClient, token, domainId, groupId, adminRoleId);
    synchronized (grantRoleDomainGroup) {
    	grantRoleDomainGroup.wait(TestProperties.getWaitTimeout());
    } 
    ClientResponse<String> grantRoleResp = grantRoleDomainGroup.getResponse();

    _log.info("Grant Role on domain succeded? {}", grantRoleResp.getStatus());
    Assert.assertEquals(ClientStatus.SUCCESS, grantRoleResp.getStatus());

  }

  public static Future<ClientResponse<Roles>> listRolesforDomainUser(ClientResponseHandler<Roles> callback, KeystoneRoleAssignmentClient roleAssignmentClient, String token,
      String domainId, String userId) {

    return roleAssignmentClient.listRolesForDomainUser(callback, token, domainId, userId);

  }

  public Future<ClientResponse<String>> grantRole(ClientResponseHandler<String> callback, KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String userId, String roleId) {

    return roleAssignmentClient.grantRoleToDomainUser(callback, token, domainId, userId, roleId);

  }

  public Future<ClientResponse<String>> checkRole(ClientResponseHandler<String> callback, KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String userId, String roleId) {

    return roleAssignmentClient.checkRoleOfDomainUser(callback, token, domainId, userId, roleId);

  }

  private Future<ClientResponse<String>> revokeRole(ClientResponseHandler<String> callback, KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String userId, String roleId) {
    return roleAssignmentClient.revokeRoleOfDomainUser(callback, token, domainId, userId, roleId);
  }

  public Future<ClientResponse<Roles>> listRolesforDomainGroup(ClientResponseHandler<Roles> callback, KeystoneRoleAssignmentClient roleAssignmentClient, String token,
      String domainId, String groupId) {

    return roleAssignmentClient.listRolesForDomainGroup(callback, token, domainId, groupId);

  }

  public Future<ClientResponse<String>> grantRoleDomainGroup(ClientResponseHandler<String> callback, KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String groupId, String roleId) {

    return roleAssignmentClient.grantRoleToDomainGroup(callback, token, domainId, groupId, roleId);
  }

  public Future<ClientResponse<String>> checkRoleDomainGroup(ClientResponseHandler<String> callback, KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String groupId, String roleId) {
    return roleAssignmentClient.checkRoleOfDomainGroup(callback, token, domainId, groupId, roleId);
  }

  private Future<ClientResponse<String>> revokeRoleDomainGroup(ClientResponseHandler<String> callback, KeystoneRoleAssignmentClient roleAssignmentClient, String token,
      String domainId, String groupId, String roleId) {
    return roleAssignmentClient.revokeRoleToDomainGroup(callback, token, domainId, groupId, roleId);
  }
}
