package com.emc.caspian.ccs.account.server;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.account.controller.KeystoneHelper;
import com.emc.caspian.ccs.account.model.mysql.MySQLProperties;
import com.emc.caspian.ccs.account.types.QueryScope;
import com.emc.caspian.ccs.account.datacontract.IdpConfig;
import com.emc.caspian.ccs.account.controller.AccountProtocol;
import com.emc.caspian.ccs.account.controller.AccountService;
import com.emc.caspian.ccs.account.datacontract.IdpRequest;
import com.emc.caspian.ccs.account.types.IdpType;
import com.emc.caspian.ccs.account.util.AppLogger;
import com.emc.caspian.ccs.keystone.model.Users;
import com.emc.caspian.fabric.config.Configuration;

/**
 * 
 * simple class to test createIdp API
 *
 */
@Ignore
public class IdpTest {
  private static String url = "ldap://10.247.170.134";
  private static String user_bind_dn = "CN=Administrator,CN=Users,DC=eciqedom,DC=com";
  private static String user_bind_pwd = "Recover123";
  private static String user_tree_dn = "CN=Users,DC=eciqedom,DC=com";
  private static String group_tree_dn = "CN=Users,DC=eciqedom,DC=com";
  private static String user_filter = "memberof=cn=eci_group1,cn=users,dc=eciqedom,dc=com";
  private static String group_filter = "";
  private static String user_class_name = "person";
  private static String group_class_name = "group";
  private static String user_name_attribute = "samAccountName";
  private static String group_name_attribute = "name";
  private static String domain_id = "d8501a710b894b66bd6b062d06d74ae5";
  private static String account_id = "37b355dd-688b-4994-b9d5-6d4cfa14c7ee";
  private static IdpConfig idpConfig = new IdpConfig();
  private static IdpRequest idprequest = new IdpRequest();
  private static AccountProtocol.CreateIdpRequest createIdpFromDomainRequest = new AccountProtocol.CreateIdpRequest();
  private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
  private static final String accountConfigPath = "conf/account.conf";
  private static final String loggerName = "account";
  private static final String ACCOUNT_SERVICE_VERSION_V1 = "v1";

  @Before
  public void CorrectDbConfig() {
    try {
      Configuration.load(accountConfigPath);
      AppLogger.initialize(loggerName);
      MySQLProperties.initializeMySQLPropertiesFromConfig();
    } catch (Exception e) {
      logger.error("An exception occurred while setting the test setup " + e.getMessage());

    }
  }

  @Test
  public void testIdp() {
    testCreateIdp();
    testListUsersInDomain();
  }


  public void testCreateIdp() {

    idpConfig.setUrl(url);
    idpConfig.setUserBindDn(user_bind_dn);
    idpConfig.setUserBindPwd(user_bind_pwd);
    idpConfig.setUserTreeDn(user_tree_dn);
    idpConfig.setGroupTreeDn(group_tree_dn);
    idpConfig.setUserFilter(user_filter);
    idpConfig.setGroupFilter(group_filter);
    idpConfig.setUserClassName(user_class_name);
    idpConfig.setGroupClassName(group_class_name);
    idpConfig.setUserNameAttribute(user_name_attribute);
    idpConfig.setGroupNameAttribute(group_name_attribute);
    idpConfig.setQueryScope(QueryScope.one);

    idprequest.setId(domain_id);
    idprequest.setType(IdpType.LDAP);
    idprequest.setIdpConfig(idpConfig);

    createIdpFromDomainRequest.setDomainId(domain_id);
    createIdpFromDomainRequest.setIdp(idprequest);
    createIdpFromDomainRequest.setAccountId(account_id);

    AccountProtocol.CreateIdpResponse createIdpresponse =
        AccountService.createIdpFromDomain(ACCOUNT_SERVICE_VERSION_V1, createIdpFromDomainRequest,"no");
    assertEquals(createIdpresponse.getStatus(), AccountProtocol.Status.SUCCESS_CREATED);

  }

  public void testListUsersInDomain() {
    Users users = KeystoneHelper.getInstance().getUsers(domain_id);
    assertEquals(users.getList().isEmpty(), false);
  }

}
