package com.emc.caspian.ccs.account.server;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.account.controller.ControllerClientHelper;
import com.emc.caspian.ccs.account.controller.KeystoneHelper;
import com.emc.caspian.ccs.account.util.AppLogger;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.fabric.config.Configuration;

// TODO: need to add a dummy server for test cases to run independent of the build environment

@SuppressWarnings("unused")
public class TestControllerClientHelper {

  @Before
  public void setUp() {
    domainId = "test-domain-id";
    accountId = "test-account-id";
    String test = System.getenv("COMPONENT_REGISTRY");
    try {
      Configuration.load(configPath);
      AppLogger.initialize(loggerName);

      if (System.getenv("COMPONENT_REGISTRY") != null) {
        setUp = true;
        authToken = KeystoneHelper.getInstance().getCSAToken();
      }
    } catch (Exception e) {
      logger.error("An exception occurred while setting the test setup " + e.getMessage());
      setUp = false;
    }
  }


  @Test
  public void testNotifyCreateAccountWithCRSEnvPresent() {
    // if the env variable is not set then return
    if (!setUp) {
      return;
    }
    List<String> listOfC3ControllerHosts = ControllerClientHelper.getInstance().getListOfControllerHosts();
    Map<String, ClientResponse<String>> response =
        ControllerClientHelper.getInstance().notifyCreateAccount(authToken, domainId, accountId,
            listOfC3ControllerHosts);
    assertEquals(response.isEmpty(), false);
  }

  @Test
  public void testNotifyDeleteAccountWithCRSEnvPresent() {

    // if the env variable is not set then return
    if (!setUp) {
      return;
    }
    List<String> listOfC3ControllerHosts = ControllerClientHelper.getInstance().getListOfControllerHosts();
    Map<String, ClientResponse<String>> response =
        ControllerClientHelper.getInstance().notifyDeleteAccount(authToken, accountId, listOfC3ControllerHosts);
    assertEquals(response.isEmpty(), false);
  }

  @Test
  public void testNotifyDeleteDomainWithCRSEnvPresent() {

    // if the env variable is not set then return
    if (!setUp) {
      return;
    }
    List<String> listOfC3ControllerHosts = ControllerClientHelper.getInstance().getListOfControllerHosts();
    Map<String, ClientResponse<String>> response =
        ControllerClientHelper.getInstance().notifyDeleteDomain(authToken, domainId, listOfC3ControllerHosts);
    assertEquals(response.isEmpty(), false);
  }

  @Test
  public void testGetASHostFromCRS() throws MalformedURLException {
    // if the env variable is not set then return
    if (!setUp) {
      return;
    }
    String ASHost = ControllerClientHelper.getInstance().getASHostFromCRS();
    URL url = new URL(ASHost);
    assertEquals(url.getPort(), 35359);
  }

  private String accountId;
  private String domainId;
  private boolean setUp = false;
  private String authToken;
  private static final Logger logger = LoggerFactory.getLogger(TestControllerClientHelper.class);
  private static final String configPath = "conf/account.conf";
  private static final String loggerName = "account";

}
