package com.emc.caspian.client;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

/**
 * @author bhandp2
 * */

public class KeystoneSendParallelRequestTest {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneSendParallelRequestTest.class);

  private static KeystoneClient eciClient;
  private static KeystoneTokenClient ksClient;
  private static Authentication authentication;


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
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN,
            TestProperties.getUser(), TestProperties.getPassword());

  }

  @Test
  public void sendParallelRequestTest() throws InterruptedException, ExecutionException {

    ArrayList<Future<ClientResponse<Token>>> list = new ArrayList<Future<ClientResponse<Token>>>();

    for (int i = 0; i < 20; i++) {
      Future<ClientResponse<Token>> future = ksClient.getToken(null, authentication, true);
      _log.info("Sent request {} for token creation", i + 1);
      list.add(future);
    }

    int j = 1;
    for (Future<ClientResponse<Token>> future : list) {
      ClientResponse<Token> clientResponse = future.get();
      Assert.assertEquals(ClientStatus.SUCCESS, clientResponse.getStatus());
      _log.info("Request {} for token creation passes", j);
      j++;
    }

  }

  @Test
  public void getRequestTwice() throws InterruptedException, ExecutionException {

    ArrayList<Future<ClientResponse<Token>>> list = new ArrayList<Future<ClientResponse<Token>>>();

    for (int i = 0; i < 4; i++) {
      Future<ClientResponse<Token>> future = ksClient.getToken(null, authentication, true);
      _log.info("Sent request {} for token creation", i + 1);
      list.add(future);
    }

    int j = 1;
    for (Future<ClientResponse<Token>> future : list) {
      ClientResponse<Token> clientResponse = future.get();
      ClientResponse<Token> clientResponse2 = future.get();// do get on request twice and response should be same
      Assert.assertEquals(ClientStatus.SUCCESS, clientResponse.getStatus());
      Assert.assertEquals(clientResponse2.getStatus(), clientResponse.getStatus());
      _log.info("Request {} for token creation passes", j);
      j++;
    }

  }
}
