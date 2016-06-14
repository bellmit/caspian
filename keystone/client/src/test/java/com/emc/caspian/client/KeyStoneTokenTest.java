package com.emc.caspian.client;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.client.util.KeyStoneTestUtil;
import com.emc.caspian.fabric.config.Configuration;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoneTokenTest {

  private static final Logger _log = LoggerFactory.getLogger(KeyStoneTokenTest.class);

  private static KeystoneClient eciClient;
  private static KeystoneTokenClient ksClient;
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

    authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN, TestProperties.getUser(),
            TestProperties.getPassword());
    tokenObj = ksClient.getToken(authentication, false).getHttpResponse().getResponseBody();
    Assert.assertNotNull(tokenObj);
    token = tokenObj.getTokenString();
    Assert.assertNotNull(token);

  }

  @Test
  public void getCertificateTest() {
    ClientResponse<String> response = getCertificate(ksClient);
    Assert.assertEquals(ClientStatus.SUCCESS, response.getStatus());
    _log.info("Get certificate list passed");
  }

  @Test
  public void getRevocationListTest() {
    ClientResponse<String> certResponse = getCertificate(ksClient);
    ksClient.setCertificate(certResponse.getHttpResponse().getResponseBody());
    ClientResponse<String> response = getRevocationList(ksClient, token);
    Assert.assertEquals(ClientStatus.SUCCESS, response.getStatus());
    Assert.assertNotNull(response.getHttpResponse().getResponseBody());
    _log.info("Get revocation list passed");
  }


  @Test
  public void getRevocationListNegativeTest() {
    ClientResponse<String> response = getRevocationList(ksClient, "somerandomtoken");
    Assert.assertEquals(ClientStatus.ERROR_HTTP, response.getStatus());
    _log.info("Get revocation list for negative test is passed");
  }

  @Test
  public void getRevocationEventsTest() {
    ClientResponse<String> response = getRevocationEvents(ksClient, token);
    Assert.assertEquals(ClientStatus.SUCCESS, response.getStatus());
    Assert.assertNotNull(response.getHttpResponse().getResponseBody());
    _log.info("Get revocation events passed");
  }

  @Test
  public void getRevocationEventsNegativeTest() {
    ClientResponse<String> response = getRevocationEvents(ksClient, "somerandomtoken");
    Assert.assertEquals(ClientStatus.ERROR_HTTP, response.getStatus());
    _log.info("Get revocation events for negative test is passed");
  }


  @Test
  public void validateTokenTest() {
    ClientResponse<Token> tokenObj = ksClient.validateToken(token, token, false);
    Assert.assertEquals(ClientStatus.SUCCESS, tokenObj.getStatus());
    _log.info("Validate token passed");
  }

  @Test
  public void checkTokenTest() {
    tokenObj = ksClient.getToken(authentication, false).getHttpResponse().getResponseBody();
    String token1 = tokenObj.getTokenString();
    ClientResponse<String> response = checkToken(ksClient, token, token1);
    Assert.assertEquals(ClientStatus.SUCCESS, response.getStatus());
    _log.info("Check token passed");
  }

  private ClientResponse<String> getCertificate(KeystoneTokenClient ksClient) {

    return ksClient.getCertificate();
  }

  private ClientResponse<String> getRevocationList(KeystoneTokenClient ksClient, String authenticationToken) {
    return ksClient.getRevocationList(authenticationToken);
  }


  private ClientResponse<String> getRevocationEvents(KeystoneTokenClient ksClient, String authenticationToken) {
    return ksClient.getRevocationEvents(authenticationToken,null);
  }


  private ClientResponse<String> checkToken(KeystoneTokenClient ksClient, String authenticationToken,
      String subjectToken) {
    return ksClient.checkToken(authenticationToken, subjectToken);
  }
}
