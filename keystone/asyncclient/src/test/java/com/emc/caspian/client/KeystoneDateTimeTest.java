package com.emc.caspian.client;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneClient;
import com.emc.caspian.ccs.keystone.asyncclient.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.common.KeystoneDateTimeUtils;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.client.util.KeyStoneTestUtil;
import com.emc.caspian.fabric.config.Configuration;


public class KeystoneDateTimeTest {

  private static KeystoneClient eciClient;
  private static KeystoneTokenClient ksClient;
  private static Authentication authentication;

  @BeforeClass
  public static void setup() {

    final String testConfigPath = "src/test/resources/test.properties";

    try {
      Configuration.load(testConfigPath);
    } catch (Exception e) {
      throw (new RuntimeException("Test configuration file missing"));
    }

    eciClient =
        new KeystoneClient(TestProperties.getServerName(), TestProperties.getPort(), true);

    ksClient = eciClient.getKeystoneTokenClient();

    authentication =
        KeyStoneTestUtil.getTokenCreationRequest(Constants.DEFAULT_DOMAIN, TestProperties.getUser(),
            TestProperties.getPassword());
  }  
  
  @Test
  public void compareTimeTests() {
    String time1 = "2015-06-02T08:43:59.746123Z";
    String time2 = "2015-06-02T08:43:59.746Z";
    String time3 = "2015-06-02T08:43:59.745999Z";
    String time4 = "2015-06-02T08:43:59.747123Z";
    String time5 = "2015-06-02T08:43:58.000000Z";
    String time6 = "2015-06-02T08:43:58.001000Z";
    String time7 = "2015-06-02T08:43:57.999000Z";
    String time8 = "2015-06-02T08:43:00.000000Z";
    
    Assert.assertEquals(KeystoneDateTimeUtils.compareTime(time1, time2), 0);
    Assert.assertEquals(KeystoneDateTimeUtils.compareTime(time1, time3), 1);
    Assert.assertEquals(KeystoneDateTimeUtils.compareTime(time1, time4), -1);
    
    Assert.assertEquals(KeystoneDateTimeUtils.compareTime(time5, time5), 0);
    Assert.assertEquals(KeystoneDateTimeUtils.compareTime(time5, time6), -1);
    Assert.assertEquals(KeystoneDateTimeUtils.compareTime(time5, time7), 1);
    
    Assert.assertEquals(KeystoneDateTimeUtils.compareTime(time5, time8), 1);
    Assert.assertEquals(KeystoneDateTimeUtils.compareTime(time8, time5), -1);
  }

  @Test
  public void testMillis() throws InterruptedException {
    long prevTimeInMillis = 0;
    ClientResponseHandler<Token> tokenCallback;
    for (int i = 0; i < 10; i++) {
      
    	tokenCallback = new ClientResponseHandler<Token>();
      
      ksClient.getToken(tokenCallback, authentication, true);
      
      synchronized (tokenCallback) {
    	  tokenCallback.wait(TestProperties.getWaitTimeout());
      }
      
      ClientResponse<Token> tokenResp = tokenCallback.getResponse();
      Token tokenObj = tokenResp.getHttpResponse().getResponseBody();   
      Assert.assertNotNull(tokenObj);
      Assert.assertTrue(KeystoneDateTimeUtils.getTimeInMillis(tokenObj.getIssuedAt()) > prevTimeInMillis);      
      prevTimeInMillis = KeystoneDateTimeUtils.getTimeInMillis(tokenObj.getIssuedAt());
    }
  }
}
