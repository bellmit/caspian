package com.emc.caspian.client;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.emc.caspian.ccs.client.ClientResponseCallback;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.common.KeystoneDeserializationUtils;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.client.util.KeyStoneTestUtil;
import com.emc.caspian.fabric.config.Configuration;

import org.apache.http.HttpResponse;
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
 
  }

  @Test
  public void getCertificateTest() throws InterruptedException {
    
	ClientResponseHandler<String> certCallback = new ClientResponseHandler<String>();
	  
    getCertificate(certCallback, ksClient);
    synchronized (certCallback) {
    	certCallback.wait(TestProperties.getWaitTimeout());
    }
    ClientResponse<String> certificateResp = certCallback.getResponse();
    Assert.assertEquals(ClientStatus.SUCCESS, certificateResp.getStatus());
    _log.info("Get certificate list passed");

  }

  @Test
  public void getCertificateFromFurtureTest() throws InterruptedException, ExecutionException {

    Future<ClientResponse<String>> future = getCertificate(null, ksClient);
    ClientResponse<String> response = future.get();

    String certificateResp = response.getHttpResponse().getResponseBody();
    Assert.assertEquals(ClientStatus.SUCCESS, response.getStatus());
    _log.info("Get certificate list passed");
  }

  @Test
  public void getRevocationListTest() throws InterruptedException {

	ClientResponseHandler<String> callback = new ClientResponseHandler<String>();
    getCertificate(callback, ksClient);
    synchronized (callback) {
    	callback.wait(TestProperties.getWaitTimeout());
    }
    ClientResponse<String> certResponse = callback.getResponse();

    ksClient.setCertificate(certResponse.getHttpResponse().getResponseBody());
    callback.clearAll();
    
    getRevocationList(callback, ksClient, token);
    synchronized (callback) {
      callback.wait(TestProperties.getWaitTimeout());
    }
    ClientResponse<String> revokeListResponse = callback.getResponse();

    Assert.assertEquals(ClientStatus.SUCCESS, revokeListResponse.getStatus());
    Assert.assertNotNull(revokeListResponse.getHttpResponse().getResponseBody());
    _log.info("Get revocation list passed");

    ClientResponse<String> revokeListVerifyResp =
        ksClient.verifyRevocationListResponse(revokeListResponse);
    Assert.assertEquals(ClientStatus.SUCCESS, revokeListVerifyResp.getStatus());
    callback.clearAll();
  }


  @Test
  public void getRevocationListNegativeTest() throws InterruptedException {
	ClientResponseHandler<String> callback = new ClientResponseHandler<String>();
    getRevocationList(callback, ksClient, "somerandomtoken");
    synchronized (callback) {
      callback.wait(TestProperties.getWaitTimeout());
    }
    ClientResponse<String> revokeResponse = callback.getResponse();

    Assert.assertEquals(ClientStatus.ERROR_HTTP, revokeResponse.getStatus());
    _log.info("Get revocation list for negative test is passed");
    callback.clearAll();
  }

  @Test
  public void getRevocationEventsTest() throws InterruptedException {
	
	ClientResponseHandler<String> callback = new ClientResponseHandler<String>();  
    getRevocationEvents(callback, ksClient, token);
    synchronized (callback) {
      callback.wait(TestProperties.getWaitTimeout());
    }
    ClientResponse<String> response = callback.getResponse();
    Assert.assertEquals(ClientStatus.SUCCESS, response.getStatus());
    Assert.assertNotNull(response.getHttpResponse().getResponseBody());
    _log.info("Get revocation events passed");
    callback.clearAll();
  }

  @Test
  public void getRevocationEventsNegativeTest() throws InterruptedException {
	  
	ClientResponseHandler<String> callback = new ClientResponseHandler<String>();  
    getRevocationEvents(callback, ksClient, "somerandomtoken");
    synchronized (callback) {
      callback.wait(TestProperties.getWaitTimeout());
    }
    ClientResponse<String> response = callback.getResponse();
    Assert.assertEquals(ClientStatus.ERROR_HTTP, response.getStatus());
    _log.info("Get revocation events for negative test is passed");
    callback.clearAll();
  }


  @Test
  public void validateTokenTest() throws InterruptedException {

	ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();
    ksClient.validateToken(tokenCallback, token, token, false);
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    
    Assert.assertEquals(ClientStatus.SUCCESS, tokenCallback.getResponse().getStatus());
    _log.info("Validate token passed");
    tokenCallback.clearAll();
  }

  @Test
  public void validateTokenFromFutureTest() throws InterruptedException, ExecutionException,
      TimeoutException {

    Future<ClientResponse<Token>> future = ksClient.validateToken(null, token, token, false);
    ClientResponse<Token> tokenResp = future.get(3000, TimeUnit.MILLISECONDS);

    Assert.assertEquals(ClientStatus.SUCCESS, tokenResp.getStatus());
    _log.info("Validate token passed");

  }
  
  @Test
  public void createTokenFromFutureTest() throws InterruptedException, ExecutionException,
      TimeoutException {

    Future<ClientResponse<Token>> future = ksClient.getToken(null, authentication, false);
    ClientResponse<Token> tokenResp = future.get(3000, TimeUnit.MILLISECONDS);

    Assert.assertEquals(ClientStatus.SUCCESS, tokenResp.getStatus());

    Assert.assertNotNull(tokenResp.getHttpResponse().getResponseBody());
    Assert.assertNotNull(tokenResp.getHttpResponse().getResponseBody().getTokenString());
    _log.info("Create token passed");

  }
  
  @Test
  public void checkTokenTest() throws InterruptedException {
	ClientResponseHandler<Token> tokenCallback = new ClientResponseHandler<Token>();  
    ksClient.getToken(tokenCallback, authentication, false);
    
    synchronized (tokenCallback) {
    	tokenCallback.wait(TestProperties.getWaitTimeout());
    }
    
    ClientResponse<Token> tokenResp = tokenCallback.getResponse();
    tokenObj = tokenResp.getHttpResponse().getResponseBody();    
    Assert.assertNotNull(tokenObj);
    String token1 = tokenObj.getTokenString();
    
	ClientResponseHandler<String> callback = new ClientResponseHandler<String>();  
    checkToken(callback, ksClient, token, token1);
    
    synchronized (callback) {
      callback.wait(TestProperties.getWaitTimeout());
    }
    
    Assert.assertEquals(ClientStatus.SUCCESS, callback.getResponse().getStatus());
    _log.info("Check token passed");

  }

  private Future<ClientResponse<String>> getCertificate( ClientResponseCallback<String> callback, KeystoneTokenClient ksClient) {

    return ksClient.getCertificate(callback);
  }

  private Future<ClientResponse<String>> getRevocationList(ClientResponseCallback<String> callback, KeystoneTokenClient ksClient, String authenticationToken) {
    return  ksClient.getRevocationList(callback, authenticationToken);
  }


  private Future<ClientResponse<String>> getRevocationEvents(ClientResponseCallback<String> callback, KeystoneTokenClient ksClient, String authenticationToken) {
    return ksClient.getRevocationEvents(callback, authenticationToken,null);
  }


  private Future<ClientResponse<String>> checkToken(ClientResponseCallback<String> callback, KeystoneTokenClient ksClient, String authenticationToken,
      String subjectToken) {
    return ksClient.checkToken(callback, authenticationToken, subjectToken);
  }
  
}


