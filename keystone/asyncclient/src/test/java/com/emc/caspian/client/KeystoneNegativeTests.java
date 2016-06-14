package com.emc.caspian.client;

import java.net.ConnectException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneTokenClient;
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
  public void connectionTimeoutTest() throws InterruptedException {

    KeystoneClient eciClient =
        new KeystoneClient("https", TestProperties.getServerName(), TestProperties.getPort(),
            10 /* set less time out */, TestProperties.getKeystoneIgnoreCertificate());

    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();

    Authentication authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN,
            TestProperties.getUser(), TestProperties.getPassword());
    
    ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    
    ksClient.getToken(tokenCallback, authentication, true);    
    
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    ClientResponse<Token> tokenResp = tokenCallback.getResponse();

    _log.info("Client Response Status for connection Timeout {}", tokenResp.getStatus());
    
  }
  
  @Test
  public void connectionFailureTest() throws InterruptedException {
    KeystoneClient eciClient =
        new KeystoneClient(TestProperties.getServerName(), 6000 /*send wrong port*/,
            TestProperties.getKeystoneIgnoreCertificate());

    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();

    Authentication authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN,
            TestProperties.getUser(), TestProperties.getPassword());
    ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    
    ksClient.getToken(tokenCallback, authentication, false);
    
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    Assert.assertNotNull(tokenCallback.getException());
 
    if(tokenCallback.exception.getCause() instanceof ConnectException){
      _log.info("Connect Exception ", tokenCallback.getException());
    }

  }
  
  @Test
  public void authenticationFailureTest() throws InterruptedException {
    KeystoneClient eciClient =
        new KeystoneClient(TestProperties.getServerName(), TestProperties.getPort(),
            TestProperties.getKeystoneIgnoreCertificate());

    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();

    Authentication authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN,
            TestProperties.getUser(), TestProperties.getPassword() + "random" /*pass a wrong password */);
    ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    
    ksClient.getToken(tokenCallback, authentication, false);
    
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    
    ClientResponse<Token> clientResponse = tokenCallback.getResponse();
    _log.info("Client Response Status for connection failure {}", clientResponse.getStatus());
    
    Assert.assertEquals(ClientStatus.ERROR_HTTP, clientResponse.getStatus());

  }
  
  @Test
  public void clientBadRequestTest() throws InterruptedException {
    KeystoneClient eciClient =
        new KeystoneClient(TestProperties.getServerName(), TestProperties.getPort(),
            TestProperties.getKeystoneIgnoreCertificate());

    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();

    Authentication authentication =
        KeyStoneTestUtil.getBadTokenCreationRequest(Constants.DEFAULT_DOMAIN, TestProperties.getUser(), TestProperties.getPassword());
    ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    
    ksClient.getToken(tokenCallback, authentication, false);
    
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    
    ClientResponse<Token> clientResponse = tokenCallback.getResponse();
    _log.info("Client Response Status for connection failure {}", clientResponse.getStatus());
    
    Assert.assertEquals(ClientStatus.ERROR_HTTP, clientResponse.getStatus());

  }
}
