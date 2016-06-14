package com.emc.caspian.client;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.client.util.KeyStoneTestUtil;
import com.emc.caspian.fabric.config.Configuration;

public class KeystoneNegativeTests {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneNegativeTests.class);

  @BeforeClass
  public static void setup() {

    final String testConfigPath = "src/test/resources/negativetest.properties";

    try {
      Configuration.load(testConfigPath);
    } catch (Exception e) {
      throw (new RuntimeException("Test configuration file missing"));
    }

  }
  
  @Test
  public void connectionTimeoutTest() {

    KeystoneClient eciClient =
        new KeystoneClient("https", TestProperties.getServerName(), TestProperties.getPort(),
            10 /* set less time out */, TestProperties.getKeystoneIgnoreCertificate());

    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();

    Authentication authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN,
            TestProperties.getUser(), TestProperties.getPassword());
    ClientResponse<Token> clientResponse = ksClient.getToken(authentication, false);
    _log.info("Client Response Status for connection Timeout {}", clientResponse.getStatus());
    
  }
  
  @Test
  public void connectionFailureTest() {
    KeystoneClient eciClient =
        new KeystoneClient(TestProperties.getServerName(), 6000 /*send wrong port*/,
            TestProperties.getKeystoneIgnoreCertificate());

    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();

    Authentication authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN,
            TestProperties.getUser(), TestProperties.getPassword());
    ClientResponse<Token> clientResponse = ksClient.getToken(authentication, false);
    _log.info("Client Response Status for connection failure {}", clientResponse.getStatus());
    
    Assert.assertEquals(ClientStatus.ERROR_SERVER_UNREACHABLE, clientResponse.getStatus());

  }
  
  @Test
  public void authenticationFailureTest() {
    KeystoneClient eciClient =
        new KeystoneClient(TestProperties.getServerName(), TestProperties.getPort(),
            TestProperties.getKeystoneIgnoreCertificate());

    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();

    Authentication authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN,
            TestProperties.getUser(), TestProperties.getPassword() + "random" /*pass a wrong password */);
    ClientResponse<Token> clientResponse = ksClient.getToken(authentication, false);
    _log.info("Client Response Status for connection failure {}", clientResponse.getStatus());
    
    Assert.assertEquals(ClientStatus.ERROR_HTTP, clientResponse.getStatus());

  }
  
  @Test
  public void clientBadRequestTest() {
    KeystoneClient eciClient =
        new KeystoneClient(TestProperties.getServerName(), TestProperties.getPort(),
            TestProperties.getKeystoneIgnoreCertificate());

    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();

    Authentication authentication =
        KeyStoneTestUtil.getBadTokenCreationRequest(Constants.DEFAULT_DOMAIN, TestProperties.getUser(), TestProperties.getPassword());
    ClientResponse<Token> clientResponse = ksClient.getToken(authentication, false);
    _log.info("Client Response Status for connection failure {}", clientResponse.getStatus());
    
    Assert.assertEquals(ClientStatus.ERROR_HTTP, clientResponse.getStatus());

  }
}
