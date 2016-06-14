package com.emc.caspian.client;

import java.util.UUID;
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
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClientException;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneDomainClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
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
    
    domainClient = eciClient.getKeystoneDomainClient();

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
  public void getDomainsTest() throws InterruptedException {
    
	ClientResponseHandler<Domains> domainsCallback = new ClientResponseHandler<Domains>();
    getDomains(domainsCallback, domainClient, token);
    synchronized (domainsCallback) {
    	domainsCallback.wait(TestProperties.getWaitTimeout());
    }
    Assert.assertEquals(ClientStatus.SUCCESS, domainsCallback.getResponse().getStatus());
    Domains domains = domainsCallback.getResponse().getHttpResponse().getResponseBody();
  }
  
  @Test
  public void getFutureDomainsTest() throws InterruptedException, ExecutionException {

    Future<ClientResponse<Domains>> futureDomains = getDomains(null, domainClient, token);
    ClientResponse<Domains> clientResp = futureDomains.get();

    Assert.assertEquals(ClientStatus.SUCCESS, clientResp.getStatus());
  }
  
  @Test
  public void getDomainTest() throws InterruptedException{
	ClientResponseHandler<Domain> domainCallback = new ClientResponseHandler<Domain>();
    getDomain(domainCallback, domainClient, token, "Default");
    synchronized (domainCallback) {
    	domainCallback.wait(TestProperties.getWaitTimeout());
    }
    Assert.assertEquals(ClientStatus.SUCCESS, domainCallback.getResponse().getStatus());
  }
  
  
  @Test
  public void getFutureDomainTest() throws InterruptedException, ExecutionException,
      TimeoutException {

    Future<ClientResponse<Domain>> futureDomain = getDomain(null, domainClient, token, "Default");
    ClientResponse<Domain> clientResp = futureDomain.get(5000, TimeUnit.MILLISECONDS);

    Assert.assertEquals(ClientStatus.SUCCESS, clientResp.getStatus());
  }
  
  @Test
  public void patchDomainTest() throws KeystoneClientException, InterruptedException{
    
    //create a domain
    Domain testDomain = new Domain();
    testDomain.setEnabled(true);
    testDomain.setName("domaintest_"+UUID.randomUUID());
    testDomain.setDescription("Test domain for deletion");
    ClientResponseHandler<Domain> domainCreateCallback = new ClientResponseHandler<Domain>();
    createDomain(domainCreateCallback, domainClient, token, testDomain);
    synchronized (domainCreateCallback) {
    	domainCreateCallback.wait(TestProperties.getWaitTimeout());
    }
    
    Assert.assertEquals(ClientStatus.SUCCESS, domainCreateCallback.getResponse().getStatus());
    Domain domian = domainCreateCallback.getResponse().getHttpResponse().getResponseBody();
    
    //patch the domain
    Domain patchDomain = new Domain();
    patchDomain.setEnabled(false);
    
    ClientResponseHandler<Domain> domainPatchCallback = new ClientResponseHandler<Domain>();
    patchDomain(domainPatchCallback, domainClient, token, domian.getId(), patchDomain);
    synchronized (domainPatchCallback) {
    	domainPatchCallback.wait(TestProperties.getWaitTimeout());
    }
    
    Assert.assertEquals(ClientStatus.SUCCESS, domainPatchCallback.getResponse().getStatus());
    Domain patchedDomain = domainPatchCallback.getResponse().getHttpResponse().getResponseBody();
    Assert.assertEquals(false, patchedDomain.getEnabled());

    //delete the domain
    ClientResponseHandler<String> domainDeleteCallback = new ClientResponseHandler<String>();
    deleteDomain(domainDeleteCallback, domainClient, token, domian.getId());
    synchronized (domainDeleteCallback) {
    	domainDeleteCallback.wait(TestProperties.getWaitTimeout());
    }
    Assert.assertEquals(ClientStatus.SUCCESS,  domainDeleteCallback.getResponse().getStatus());

  }

  private Future<ClientResponse<Domains>> getDomains(ClientResponseCallback<Domains> callback, KeystoneDomainClient ksClient,
      String authenticationToken) {
    return ksClient.getAllDomains(callback, authenticationToken);
  }

  private Future<ClientResponse<Domain>> getDomain(ClientResponseCallback<Domain> callback, KeystoneDomainClient ksClient,
      String authenticationToken, String id) {
    return ksClient.getDomain(callback, authenticationToken, id);
  }

  private Future<ClientResponse<Domain>> createDomain(ClientResponseCallback<Domain> callback, KeystoneDomainClient ksClient,
      String authenticationToken, Domain testDomain) throws KeystoneClientException {
    return ksClient.createDomain(callback, authenticationToken, testDomain);
  }

  private Future<ClientResponse<String>> deleteDomain(ClientResponseCallback<String> callback, KeystoneDomainClient ksClient, String authenticationToken, String id) {
    return ksClient.deleteDomain(callback, authenticationToken, id);
  }


  private Future<ClientResponse<Domain>> patchDomain(ClientResponseCallback<Domain> callback, KeystoneDomainClient ksClient,
      String authenticationToken, String id, Domain domain) throws KeystoneClientException {
    return ksClient.patchDomain(callback, authenticationToken, id, domain);
  }
}
