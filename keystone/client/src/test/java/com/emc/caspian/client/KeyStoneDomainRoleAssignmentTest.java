package com.emc.caspian.client;

import java.util.List;

import org.junit.AfterClass;
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
import com.emc.caspian.ccs.keystone.client.KeystoneRoleAssignmentClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
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
  private static String adminRoleId;

  @BeforeClass
  public static void setup() {

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

    tokenObj = ksClient.getToken(authentication, false).getHttpResponse().getResponseBody();
    
    Assert.assertNotNull(tokenObj);
    token = tokenObj.getTokenString();

    _log.info("Token: {}", token);
    // Validate is a extra step required because we don't return the token object when requested for
    // create token
    tokenObj = ksClient.validateToken(token, token, false).getHttpResponse().getResponseBody();
    Assert.assertNotNull(tokenObj);

    // To check for the List roles for domain group
    // first create a group inside a domain and then ask for listRoles in domain by group id
    // second grant role to the group created
    groupClient = eciClient.getKeystoneGroupClient();
    Group groupCreateRequest =
        KeyStoneTestUtil.getGroupCreateRequest("default-group-" + System.currentTimeMillis(), tokenObj.getDomain()
            .getId(), "Test Default group");

    Group groupCreateResponse = groupClient.addGroup(token, groupCreateRequest).getHttpResponse().getResponseBody();
    Assert.assertNotNull(groupCreateResponse);
    
    _log.info("Created a Group : {} under Domain : {}", groupCreateResponse.getId(), groupCreateResponse.getDomainId());
    groupId = groupCreateResponse.getId();
    domainId = groupCreateResponse.getDomainId();


    Roles roles =
        listRolesforDomainUser(roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId()).getHttpResponse().getResponseBody();    
    Assert.assertNotNull(roles);
    _log.info("Roles {}", roles);

    List<Role> roleList = roles.getList();
    for (Role role : roleList) {
      _log.info("Role ID: {} Role Name : {}", role.getId(), role.getName());
      if (role.getName().equalsIgnoreCase("admin")) {
        adminRoleId = role.getId();
      }
    }

    Assert.assertNotNull(adminRoleId);


  }

  @AfterClass
  public static void teardown() {
    /* delete the group created */
    ClientStatus isDeleted = groupClient.removeGroup(token, groupId).getStatus();
    Assert.assertEquals(ClientStatus.SUCCESS, isDeleted);
  }

  @Test
  public void listRolesForDomainUserTest() {
    Roles roles =
        listRolesforDomainUser(roleAssignmentClient, token, Constants.DEFAULT_DOMAIN,
            tokenObj.getUser().getId()).getHttpResponse().getResponseBody();
    
    _log.info("List Roles on user {}", roles);
    Assert.assertNotNull(roles);
  }

  @Test
  public void grantRoleUserTest() {

    ClientStatus grantRole =
        grantRole(roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId).getStatus();
    _log.info("Grant Role on user ? {}", grantRole);
    
    Assert.assertEquals(ClientStatus.SUCCESS, grantRole);

  }

  @Test
  public void checkRoleUserTest() {

    ClientStatus grantRole =
        grantRole(roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId).getStatus();
    _log.info("Grant Role on user ? {}", grantRole);
    Assert.assertEquals(ClientStatus.SUCCESS, grantRole);

    ClientStatus checkRole =
        checkRole(roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId).getStatus();
    _log.info("Check Role on user ? {}", checkRole);
    Assert.assertEquals(ClientStatus.SUCCESS, checkRole);

  }

  //@Test
  public void revokeRoleUserTest() {


    ClientStatus revokeRole =
        revokeRole(roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId).getStatus();
    _log.info("Revoke Role on user ? {}", revokeRole);
    Assert.assertEquals(ClientStatus.SUCCESS, revokeRole);

    // get a fresh token and use it for authentication as soon after the revokeRole the user will be
    // logged out
    tokenObj = ksClient.getToken(authentication, false).getHttpResponse().getResponseBody();
    
    Assert.assertNotNull(tokenObj);
    token = tokenObj.getTokenString();

    // grant back the same role to admin so that other operations can be done by admin
    ClientStatus grantRole =
        grantRole(roleAssignmentClient, token, Constants.DEFAULT_DOMAIN, tokenObj.getUser().getId(), adminRoleId).getStatus();
    _log.info("Grant Role on user ? {}", grantRole);
    Assert.assertEquals(ClientStatus.SUCCESS, grantRole);

  }

  @Test
  public void listRolesforDomainGroupTest() {

    Roles rolesForDomain = listRolesforDomainGroup(roleAssignmentClient, token, domainId, groupId).getHttpResponse().getResponseBody();
    Assert.assertNotNull(rolesForDomain);
    _log.info("Roles for Domain {}", rolesForDomain);

  }

  @Test
  public void grantRoleDomainGroupTest() {

    ClientStatus grantRole = grantRoleDomainGroup(roleAssignmentClient, token, domainId, groupId, adminRoleId).getStatus();
    _log.info("Grant Role on domain ? {}", grantRole);
    Assert.assertEquals(ClientStatus.SUCCESS, grantRole);

  }

  @Test
  public void checkRoleDomainGroupTest() {

    // grant the role and then invoke the check role
    ClientStatus grantRole = grantRoleDomainGroup(roleAssignmentClient, token, domainId, groupId, adminRoleId).getStatus();
    _log.info("Grant Role on domain succeded? {}", grantRole);
    Assert.assertEquals(ClientStatus.SUCCESS, grantRole);

    ClientStatus checkRole = checkRoleDomainGroup(roleAssignmentClient, token, domainId, groupId, adminRoleId).getStatus();
    _log.info("Check Role on domain succeded? {}", checkRole);
    Assert.assertEquals(ClientStatus.SUCCESS, checkRole);

  }

  public void revokeRoleDomainGroupTest() {
    ClientStatus revokeRole = revokeRoleDomainGroup(roleAssignmentClient, token, domainId, groupId, adminRoleId).getStatus();

    // get a fresh token and use it for authentication as soon after the revokeRole the user will be
    // logged out
    
    tokenObj = ksClient.getToken(authentication, false).getHttpResponse().getResponseBody();
    token = tokenObj.getTokenString();

    ClientStatus grantRole = grantRoleDomainGroup(roleAssignmentClient, token, domainId, groupId, adminRoleId).getStatus();
    _log.info("Grant Role on domain succeded? {}", grantRole);
    Assert.assertEquals(ClientStatus.SUCCESS, grantRole);
  }

  public static ClientResponse<Roles> listRolesforDomainUser(KeystoneRoleAssignmentClient roleAssignmentClient, String token,
      String domainId, String userId) {

    return roleAssignmentClient.listRolesForDomainUser(token, domainId, userId);

  }

  public ClientResponse<String> grantRole(KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String userId, String roleId) {

    return roleAssignmentClient.grantRoleToDomainUser(token, domainId, userId, roleId);

  }

  public ClientResponse<String> checkRole(KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String userId, String roleId) {

    return roleAssignmentClient.checkRoleOfDomainUser(token, domainId, userId, roleId);

  }

  private ClientResponse<String> revokeRole(KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String userId, String roleId) {
    return roleAssignmentClient.revokeRoleOfDomainUser(token, domainId, userId, roleId);
  }

  public ClientResponse<Roles> listRolesforDomainGroup(KeystoneRoleAssignmentClient roleAssignmentClient, String token,
      String domainId, String groupId) {

    return roleAssignmentClient.listRolesForDomainGroup(token, domainId, groupId);

  }

  public ClientResponse<String> grantRoleDomainGroup(KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String groupId, String roleId) {

    return roleAssignmentClient.grantRoleToDomainGroup(token, domainId, groupId, roleId);
  }

  public ClientResponse<String> checkRoleDomainGroup(KeystoneRoleAssignmentClient roleAssignmentClient, String token, String domainId,
      String groupId, String roleId) {
    return roleAssignmentClient.checkRoleOfDomainGroup(token, domainId, groupId, roleId);
  }

  private ClientResponse<String> revokeRoleDomainGroup(KeystoneRoleAssignmentClient roleAssignmentClient, String token,
      String domainId, String groupId, String roleId) {
    return roleAssignmentClient.revokeRoleToDomainGroup(token, domainId, groupId, roleId);
  }
}
