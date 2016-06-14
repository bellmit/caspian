package com.emc.caspian.ccs.keystone.middleware;

import org.junit.Assert;
import org.junit.Test;

import com.emc.caspain.ccs.keystone.middleware.Middleware;
import com.emc.caspain.ccs.keystone.middleware.exceptions.InternalException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.InvalidTokenException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.MiddlewareException;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.client.KeystoneDomainClient;
import com.emc.caspian.ccs.keystone.client.KeystoneRoleAssignmentClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Authentication.Identity;
import com.emc.caspian.ccs.keystone.model.Authentication.Scope;
import com.emc.caspian.ccs.keystone.model.Domain;
import com.emc.caspian.ccs.keystone.model.Token;

public class KeystoneMiddlewareTest {

  private static class KeystoneConfig {

    public KeystoneConfig(String proto, String host, int port, String user, String password) {
      this.proto = proto;
      this.host = host;
      this.port = port;
      this.user = user;
      this.password = password;
    }

    public String proto;
    public String host;
    public int port;
    public String user;
    public String password;
  }

  // Update the list of server configurations here before executing the tests
  // You can have keystone servers here with different configurations - UUID, PKI, https, http, etc
  // All the tests will be repeated for each server mentioned here
  // Ensure that the keystone servers are up and running before starting these tests
  private static final KeystoneConfig[] keystoneServersConfig = {new KeystoneConfig("https", "keystone", 6100, "admin",
      "admin123")};

  private static Middleware middleware = null;
  private static KeystoneClient ksClient = null;
  private static KeystoneConfig ksConfig = null;

  private void initialize(KeystoneConfig keystoneConfig) {
    ksConfig = keystoneConfig;
    String authUri = ksConfig.proto + "://" + ksConfig.host + ":" + String.valueOf(ksConfig.port);
    middleware = new Middleware(authUri, ksConfig.user, ksConfig.password);
    middleware.start();
    ksClient = new KeystoneClient(ksConfig.proto, ksConfig.host, ksConfig.port, true);
  }

  private void tearDown() {
    middleware.stop();
    middleware = null;
    ksClient = null;
    ksConfig = null;
  }

  private Token createAdminToken(boolean adminScoped) {
    KeystoneTokenClient ksTokenClient = ksClient.getKeystoneTokenClient();


    Authentication authenticate = new Authentication();

    // set identity
    Authentication.Identity identity = new Identity();
    identity = Identity.password(Constants.DEFAULT_DOMAIN, ksConfig.user, ksConfig.password);
    authenticate.setIdentity(identity);

    // set scope as default
    if (adminScoped) {
      Authentication.Scope scope = new Scope();
      scope = Scope.domain(Constants.DEFAULT_DOMAIN);
      authenticate.setScope(scope);
    }

    return ksTokenClient.getToken(authenticate, true).getHttpResponse().getResponseBody();
  }

  private Token createDomainToken(String domainId) {
    KeystoneTokenClient ksTokenClient = ksClient.getKeystoneTokenClient();


    Authentication authenticate = new Authentication();

    // set identity
    Authentication.Identity identity = new Identity();
    identity = Identity.password(Constants.DEFAULT_DOMAIN, ksConfig.user, ksConfig.password);
    authenticate.setIdentity(identity);

    // set scope as default

    Authentication.Scope scope = new Scope();
    scope = Scope.domain(domainId);
    authenticate.setScope(scope);



    return ksTokenClient.getToken(authenticate, true).getHttpResponse().getResponseBody();
  }

  //@Test
  public void TestAdminToken() {

    for (KeystoneConfig keystoneConfig : keystoneServersConfig) {
      initialize(keystoneConfig);

      Token adminToken = createAdminToken(true);
      Assert.assertNotNull(adminToken);
      Assert.assertNotNull(adminToken.getTokenString());

      Token token = null;
      try {
        token = middleware.getToken(adminToken.getTokenString());
      } catch (MiddlewareException e) {
        Assert.fail();
      }
      Assert.assertNotNull(token);
      Assert.assertNotNull(token.getDomain());
      Assert.assertNotNull(token.getDomain().getId());
      Assert.assertEquals(token.getDomain().getId(), Constants.DEFAULT_DOMAIN);

      tearDown();
    }
  }

  //@Test
  public void TestUnScopedToken() {
    for (KeystoneConfig keystoneConfig : keystoneServersConfig) {
      initialize(keystoneConfig);

      Token nonAdminToken = createAdminToken(false);
      Assert.assertNotNull(nonAdminToken);
      Assert.assertNotNull(nonAdminToken.getTokenString());

      Token token = null;
      try {
        token = middleware.getToken(nonAdminToken.getTokenString());
      } catch (MiddlewareException e) {
        Assert.fail();
      }
      Assert.assertNotNull(token);
      Assert.assertNull(token.getDomain());

      tearDown();
    }
  }

  //@Test
  public void TestInValidUUIDToken() {
    for (KeystoneConfig keystoneConfig : keystoneServersConfig) {
      initialize(keystoneConfig);

      Token token = null;
      try {
        token = middleware.getToken("somerandomuuid");
      } catch (MiddlewareException e) {
        Assert.assertTrue(e instanceof InvalidTokenException);
      }
      Assert.assertNull(token);

      tearDown();
    }
  }

  //@Test
  public void TestInValidPKIToken() {
    for (KeystoneConfig keystoneConfig : keystoneServersConfig) {
      initialize(keystoneConfig);

      Token token = null;
      try {
        token = middleware.getToken("MIIsomerandomvalue");
      } catch (MiddlewareException e) {
        Assert.assertTrue(e instanceof InvalidTokenException);
      }
      Assert.assertNull(token);

      tearDown();
    }
  }

  //@Test
  public void TestRevokedToken() {

    for (KeystoneConfig keystoneConfig : keystoneServersConfig) {
      initialize(keystoneConfig);

      Token adminToken = createAdminToken(true);
      Assert.assertNotNull(adminToken);
      Assert.assertNotNull(adminToken.getTokenString());
      String authToken = adminToken.getTokenString();

      Token inittoken = null;
      try {
        inittoken = middleware.getToken(authToken);
      } catch (MiddlewareException e1) {
        Assert.fail();
      }
      Assert.assertNotNull(inittoken);

      // Revoke the token
      KeystoneDomainClient ksDomainClient = ksClient.getKeystoneDomainClient();
      Domain domain = new Domain();
      // Create a new domain
      domain.setName("Test4");
      domain.setEnabled(true);
      domain = ksDomainClient.createDomain(authToken, domain).getHttpResponse().getResponseBody();
      // Grant admin role in the new domain to the cloud admin user
      KeystoneRoleAssignmentClient ksRoleClient = ksClient.getKeystoneRoleAssignmentClient();
      ksRoleClient.grantRoleToDomainUser(authToken, domain.getId(), adminToken.getUser().getId(), adminToken.getRoles()
          .get(0).getId());
      // Get a domain scoped token for the new domain
      Token testToken = createDomainToken(domain.getId());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      // delete the new domain
      domain.setEnabled(false);
      ksDomainClient.patchDomain(authToken, domain.getId(), domain);
      ksDomainClient.deleteDomain(authToken, domain.getId());
      // Wait for 15 seconds as fetching the revocation list takes time
      try {
        Thread.sleep(15 * 1000);
      } catch (InterruptedException e) {
      }
      // validate the domain scoped token
      Token token = null;
      try {
        token = middleware.getToken(testToken.getTokenString());
      } catch (MiddlewareException e) {
        Assert.assertTrue(e instanceof InvalidTokenException);
      }
      Assert.assertNull(token);
      tearDown();
    }
  }

  //@Test
  public void TestInternalError() {
      initialize(new KeystoneConfig("https", "10.0.0.1", 6100, "admin", "admin123"));

      Token token = null;
      try {
        token = middleware.getToken("somerandomtoken");
        Assert.fail();
      } catch (MiddlewareException e) {
        Assert.assertTrue(e instanceof InternalException);
      }
      Assert.assertNull(token);

      tearDown();
  }
  
  //@Test
  public void TestStartStop() {
    for (KeystoneConfig keystoneConfig : keystoneServersConfig) {
      initialize(keystoneConfig);
      Token adminToken = createAdminToken(true);
      Assert.assertNotNull(adminToken);
      Assert.assertNotNull(adminToken.getTokenString());

      Token token = null;
      try {
        token = middleware.getToken(adminToken.getTokenString());
      } catch (MiddlewareException e1) {
        Assert.fail();
      }
      Assert.assertNotNull(token);
      Assert.assertNotNull(token.getDomain());
      Assert.assertNotNull(token.getDomain().getId());
      Assert.assertEquals(token.getDomain().getId(), Constants.DEFAULT_DOMAIN);

      middleware.stop();
      try {
        token = middleware.getToken(adminToken.getTokenString());
        Assert.fail();
      } catch (MiddlewareException e) {
        Assert.assertTrue(e instanceof InternalException);
      }

      middleware.start();
      try {
        token = middleware.getToken(adminToken.getTokenString());
      } catch (MiddlewareException e) {
        Assert.fail();
      }
      Assert.assertNotNull(token);
      Assert.assertNotNull(token.getDomain());
      Assert.assertNotNull(token.getDomain().getId());
      Assert.assertEquals(token.getDomain().getId(), Constants.DEFAULT_DOMAIN);

      tearDown();
    }
  }

}
