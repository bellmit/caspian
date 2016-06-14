package com.emc.caspian.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Authentication.Identity;
import com.emc.caspian.ccs.keystone.model.Authentication.Scope;
import com.emc.caspian.ccs.keystone.model.Group;

public class KeyStoneTestUtil {

  /**
   * Gets the token creation request with authentocation method as password.
   *
   * @param domain the domain
   * @param userName the user name
   * @param password the password
   * @return the token creation request
   */
  public static Authentication getTokenCreationRequest(String domainName, String userName, String password) {

    Authentication authenticate = new Authentication();

    // set identity
    Authentication.Identity identity = new Identity();
    identity = Identity.password(domainName, userName, password);
    authenticate.setIdentity(identity);

    Authentication.Scope scope = new Scope();
    scope = Scope.domain(domainName);
    authenticate.setScope(scope);

    return authenticate;
  }

  public static Group getGroupCreateRequest(String name, String domainId, String description) {

    Group group = new Group();

    group.setDomainId(domainId);
    group.setName(name);
    group.setDescription(description);

    return group;
  }

  public static Properties loadProperties(String fileName) {

    FileInputStream fis;
    Properties properites = new Properties();
    try {
      fis = new FileInputStream(new File(fileName));
      properites.load(fis);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return properites;
  }

  public static Authentication getBadTokenCreationRequest(String domainName, String userName, String password) {

    Authentication authenticate = new Authentication();
    
    // set identity
    Authentication.Identity identity = new Identity();
    identity = Identity.password(domainName, userName, password);
    authenticate.setIdentity(identity);

    Authentication.Scope scope = new Scope();
    authenticate.setScope(scope);
    return authenticate;
  }

}
