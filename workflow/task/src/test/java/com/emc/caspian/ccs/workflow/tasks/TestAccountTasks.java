package com.emc.caspian.ccs.workflow.tasks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.emc.caspian.ccs.account.model.mysql.MySQLProperties;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Authentication.Identity;
import com.emc.caspian.ccs.keystone.model.Authentication.Scope;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.ccs.workflow.TaskException;
import com.emc.caspian.ccs.workflow.types.EnvironmentKeys;
import com.emc.caspian.fabric.config.Configuration;


/**
 * 
 * simple class to test the tasks in AccountTasks
 * 
 * @author raod4
 *
 */
@Ignore
public class TestAccountTasks {

  private static String KEYSTONE_URI = "https://10.63.13.180:6100";
  private static String ACCOUNT_SERVICE_URI_INFO = "http|10.63.13.125|35359";
  private static String DB_USER = "accountadmin";
  private static String DB_PASSWORD = "accountadmin";
  private static String KEYSTONE_USER = "admin";
  private static String KEYSTONE_PASSWORD = "admin123";
  private static String DB_HOSTNAME = "10.63.13.180";
  private static String DB_PORT = "3306";
  private static String DATABASE = "accounts";
  private static String ACCOUNT_ID="ab97fce7-82fc-43e4-bd2c-2b37f10c20a5";
  private static String LISTOFCONTROLLERHOSTS = "http://10.63.13.154:9997,http://10.63.13.152:9997";
  private static String DOMAIN_ID="541ec9ff0af84599af78f10287bdd123";
  private static String protocol;
  private static String hostName;
  private static int port;
  private static boolean ignoreCerts = true;
  private static KeystoneClient eciClient;
  private static Token token;
  private static AccountTasks testTasks;
  private static Map<String, String> env;
 
  private static final String accountConfigPath = "conf/account.conf";

  @BeforeClass
  public static void CorrectDbConfig() {
    try {
      Configuration.load(accountConfigPath);
      env = new HashMap<String, String>();
      initialiseKeystoneClients(KEYSTONE_URI);
      env.put(EnvironmentKeys.Token.toString(), token.getTokenString());
      testTasks = new AccountTasks();
      testTasks.environment = env;      
    } catch (Exception e) {
      throw (new RuntimeException("Account service configuration file missing"));
    }
  }
  
  /**
   * This method is just initialising the keystone client so that we can get token.
   * 
   * We cannot have dependency on keystoneHelper in workflow, hence this method.
   * @param keystoneUri
   */
  public static void initialiseKeystoneClients(String keystoneUri) {
    URL uri;
    try {
      uri = new URL(keystoneUri);
      protocol = uri.getProtocol();
      hostName = uri.getHost();
      port = uri.getPort();
      MySQLProperties.initializeMySQLPropertiesFromConfig();
    } catch (MalformedURLException e) {
      throw new TaskException("Invalid keystone auth uri configured", false);
    }
    eciClient = new KeystoneClient(protocol, hostName, port, ignoreCerts);
    Authentication authenticate = new Authentication();
    KeystoneTokenClient ksClient = eciClient.getKeystoneTokenClient();
    Authentication.Identity identity = new Identity();
    identity = Identity.password(Constants.DEFAULT_DOMAIN, KEYSTONE_USER, KEYSTONE_PASSWORD);
    authenticate.setIdentity(identity);
    Authentication.Scope scope = new Scope();
    scope = Scope.domain(Constants.DEFAULT_DOMAIN);
    authenticate.setScope(scope);

    // no catalogue is set to true below
    ClientResponse<Token> tokenObj = ksClient.getToken(authenticate, true);
    Assert.assertEquals(ClientStatus.SUCCESS, tokenObj.getStatus());
    token = tokenObj.getHttpResponse().getResponseBody();
    Assert.assertNotNull(token);
  }
  
  @Ignore
  @Test
  public void testAccountDeletionTask() {
   boolean response =
        testTasks.deleteAccount(ACCOUNT_ID, KEYSTONE_URI, DB_USER, DB_PASSWORD,DB_HOSTNAME,DB_PORT, DATABASE, LISTOFCONTROLLERHOSTS);
   Assert.assertEquals(true, response);
  }

}
