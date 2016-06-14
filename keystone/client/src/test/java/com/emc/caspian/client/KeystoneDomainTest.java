package com.emc.caspian.client;

import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.client.KeystoneDomainClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Authentication.Identity;
import com.emc.caspian.ccs.keystone.model.Authentication.Scope;
import com.emc.caspian.ccs.keystone.model.Domain;
import com.emc.caspian.ccs.keystone.model.Domains;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.client.util.KeyStoneTestUtil;
import com.emc.caspian.fabric.config.Configuration;

public class KeystoneDomainTest {
  
  private static KeystoneClient eciClient;
  private static KeystoneTokenClient ksClient;
  private static KeystoneDomainClient domainClient;
  private static Authentication authentication;

  private static String token;
  private static Token tokenObj;
  
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
    
    domainClient = eciClient.getKeystoneDomainClient();

    authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN, TestProperties.getUser(),
            TestProperties.getPassword());
    tokenObj = ksClient.getToken(authentication, false).getHttpResponse().getResponseBody();
    Assert.assertNotNull(tokenObj);

    token = tokenObj.getTokenString();
    Assert.assertNotNull(token);
  }
  
  @Test
  public void getDomainsTest(){
    ClientResponse<Domains> domains = getDomains(domainClient, token);
    Assert.assertEquals(ClientStatus.SUCCESS, domains.getStatus());
  }
  
  @Test
  public void getDomainTest(){
    ClientResponse<Domain> domainDetail = getDomain(domainClient, token, "Default");
    Assert.assertEquals(ClientStatus.SUCCESS, domainDetail.getStatus());
  }
  
  @Test
  public void patchDomainTest(){
    
    //create a domain
    Domain testDomain = new Domain();
    testDomain.setEnabled(true);
    testDomain.setName("domaintest_"+UUID.randomUUID());
    testDomain.setDescription("Test domain for deletion");
    ClientResponse<Domain> createResponse = createDomain(domainClient, token, testDomain);
    Assert.assertEquals(ClientStatus.SUCCESS, createResponse.getStatus());
    Domain domian = createResponse.getHttpResponse().getResponseBody();
    
    //patch the domain
    Domain patchDomain = new Domain();
    patchDomain.setEnabled(false);
    ClientResponse<Domain> patchDomainResponse = patchDomain(domainClient, token, domian.getId(), patchDomain);
    Assert.assertEquals(ClientStatus.SUCCESS, patchDomainResponse.getStatus());
    Domain patchedDomain = patchDomainResponse.getHttpResponse().getResponseBody();
    Assert.assertEquals(false, patchedDomain.getEnabled());
    
    //delete the domain
    ClientResponse<String> deleteReponse = deleteDomain(domainClient, token, domian.getId());
    Assert.assertEquals(ClientStatus.SUCCESS, deleteReponse.getStatus());
  }

  private String createToken(KeystoneTokenClient ksClient, boolean nocatalog) {

    Authentication authenticate = new Authentication();

    // set identity
    Authentication.Identity identity = new Identity();
    identity = Identity.password(Constants.DEFAULT_DOMAIN, "admin", "admin123");
    authenticate.setIdentity(identity);

    // set scope as default
    Authentication.Scope scope = new Scope();
    scope = Scope.domain(Constants.DEFAULT_DOMAIN);
    authenticate.setScope(scope);

    Token token = ksClient.getToken(authenticate, nocatalog).getHttpResponse().getResponseBody();

    if (token == null) {
      return null;
    } else {
      return token.getTokenString();
    }
  }

  private ClientResponse<Domains> getDomains(KeystoneDomainClient ksClient,
      String authenticationToken) {
    return ksClient.getAllDomains(authenticationToken);
  }

  private ClientResponse<Domain> getDomain(KeystoneDomainClient ksClient,
      String authenticationToken, String id) {
    return ksClient.getDomain(authenticationToken, id);
  }

  private ClientResponse<Domain> createDomain(KeystoneDomainClient ksClient,
      String authenticationToken, Domain testDomain) {
    return ksClient.createDomain(authenticationToken, testDomain);
  }

  private ClientResponse<String> deleteDomain(KeystoneDomainClient ksClient, String authenticationToken, String id) {
    return ksClient.deleteDomain(authenticationToken, id);
  }


  private ClientResponse<Domain> patchDomain(KeystoneDomainClient ksClient,
      String authenticationToken, String id, Domain domain) {
    return ksClient.patchDomain(authenticationToken, id, domain);
  }
}
