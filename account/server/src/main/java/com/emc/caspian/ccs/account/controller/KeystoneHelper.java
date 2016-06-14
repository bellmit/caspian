package com.emc.caspian.ccs.account.controller;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import com.emc.caspian.ccs.account.server.KeystoneProperties;
import com.emc.caspian.ccs.account.util.AppLogger;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.client.KeyStoneGroupClient;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.common.KeystoneDateTimeUtils;
import com.emc.caspian.ccs.keystone.client.KeystoneDomainClient;
import com.emc.caspian.ccs.keystone.client.KeystoneProjectClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.client.KeystoneUserClient;
import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Domain;
import com.emc.caspian.ccs.keystone.model.Authentication.Identity;
import com.emc.caspian.ccs.keystone.model.Authentication.Scope;
import com.emc.caspian.ccs.keystone.model.DomainConfig;
import com.emc.caspian.ccs.keystone.model.Domains;
import com.emc.caspian.ccs.keystone.model.Groups;
import com.emc.caspian.ccs.keystone.model.Project;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.ccs.keystone.model.Users;

public class KeystoneHelper {

  private Token token = null;

  private KeystoneDomainClient domainClient = null;
  
  private KeystoneUserClient userClient = null;

  private KeyStoneGroupClient groupClient = null;
  
  private KeystoneTokenClient ksClient = null;

  private Authentication credentials = null;

  private KeystoneProjectClient projectClient = null;

  /* small correction due to clock synchronization problem */
  private final static int CORRECTION = 1000 * 60 * 5;
  private final static boolean DEFAULT_IGNORECERTS = true;
  private String hostname;

  private int port;

  private String protocol;

  private String adminuser;

  private String uri;

  private boolean ignoreCerts;

  private static volatile KeystoneHelper keystoneHelper = null;
 
  private KeystoneHelper() {

    if (keystoneHelper != null) {
      throw new IllegalStateException("Already instantiated keystoneHelper");
    }

    URL authUri;
    try {
      uri = KeystoneProperties.getkeystoneUri();
      authUri = new URL(uri);
    } catch (MalformedURLException e) {
      AppLogger.error("Invalid keystone auth uri configured: " + uri);
      throw new RuntimeException("Invalid keystone auth uri configured");
    }

    hostname = authUri.getHost();
    protocol = authUri.getProtocol();
    port = authUri.getPort();

    Validator.validateNotEmpty(hostname);
    Validator.validateProtocolType(protocol);
    Validator.validatePortRange(port);

    adminuser = KeystoneProperties.getKeystoneAdmin();
    Validator.validateNotEmpty(adminuser);
    
    KeystoneProperties keystoneProperties = new KeystoneProperties();
    // We dont want to store password in a String variable for security reasons
    Validator.validateNotEmpty(keystoneProperties.getKeystoneAdminPassword(), "Recieved null for Keystone admin password");

    ignoreCerts = DEFAULT_IGNORECERTS;

    KeystoneClient eciClient = new KeystoneClient(protocol, hostname, port, ignoreCerts);

    ksClient = eciClient.getKeystoneTokenClient();
    domainClient = eciClient.getKeystoneDomainClient();
    groupClient = eciClient.getKeystoneGroupClient();
    userClient = eciClient.getKeystoneUserClient();
    projectClient = eciClient.getKeystoneProjectClient();

    credentials = new Authentication();

    Authentication.Identity identity = new Identity();
    identity = Identity.password(Constants.DEFAULT_DOMAIN, adminuser, keystoneProperties.getKeystoneAdminPassword());
    credentials.setIdentity(identity);

    Authentication.Scope scope = new Scope();
    scope = Scope.domain(Constants.DEFAULT_DOMAIN);
    credentials.setScope(scope);

  }

  public static KeystoneHelper getInstance() {
    if (keystoneHelper == null) {
      synchronized (KeystoneHelper.class) {
        if (keystoneHelper == null)
          keystoneHelper = new KeystoneHelper();
      }
    }
    return keystoneHelper;
  }

  public synchronized String getCSAToken() {
	    
    long expiry = 0;
    long current = 0;
    
    if (token != null) {
      expiry = KeystoneDateTimeUtils.getTimeInMillis(token.getExpiresAt());
      current = System.currentTimeMillis() + CORRECTION;
    }

    if (token == null || expiry <= current) {
      token = null;
      ClientResponse<Token> tokenResp = ksClient.getToken(credentials, true);
      if (ClientStatus.SUCCESS != tokenResp.getStatus()) {
        AppLogger.warn("Couldn't get the token received error {}", tokenResp.getStatus());
        return null;
      }
      token = tokenResp.getHttpResponse().getResponseBody();
      AppLogger.debug("Created a new token which expires at: " + token.getExpiresAt());
    }
    return token.getTokenString();
  }
  
  public synchronized void invalidateCSAToken() {
    AppLogger.debug("Invalidating CSA token");
    token = null;
  }

  public ClientResponse<Domains> getAllDomains() {
    
    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is null while getting all domains");
      return null;
    }
    
    ClientResponse<Domains> domainList = domainClient.getAllDomains(tokenString);

    if (domainList.getStatus() != ClientStatus.SUCCESS) {
      if (domainList.getStatus() == ClientStatus.ERROR_HTTP && domainList.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      } else {
        AppLogger.warn("Request failed with status " + domainList.getStatus());
      }
    }

    return domainList;
  }

  public ClientResponse<Domain> getDomain(String id) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while getting domain");
      return null;
    }
    
    ClientResponse<Domain> domainResponse = domainClient.getDomain(tokenString, id);

    if (domainResponse.getStatus() != ClientStatus.SUCCESS) {
      if (domainResponse.getStatus() == ClientStatus.ERROR_HTTP && domainResponse.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      } else {
        AppLogger.warn("Request failed with status {} " + domainResponse.getStatus());
      }
    }
    
    return domainResponse;
  }

  public ClientResponse<Domains> getDomainByName(String domainName) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while getting domain by name");
      return null;
    }
    
    ClientResponse<Domains> domainsResponse = domainClient.getDomainsByFilter(tokenString, domainName, null);

    if (domainsResponse.getStatus() != ClientStatus.SUCCESS) {
      if (domainsResponse.getStatus() == ClientStatus.ERROR_HTTP && domainsResponse.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      } else {
        AppLogger.warn("Request failed with status " + domainsResponse.getStatus());
      }
    }

    return domainsResponse;
  }

  public ClientResponse<Domain> createDomain(Domain domain) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while creating a domain");
      return null;
    }
    
    ClientResponse<Domain> domainCreate = domainClient.createDomain(tokenString, domain);

    if (domainCreate.getStatus() != ClientStatus.SUCCESS) {
      if (domainCreate.getStatus() == ClientStatus.ERROR_HTTP && domainCreate.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      }
      // 409 error is handled in account service, which is required to check for duplicate domain
      else if (domainCreate.getStatus() == ClientStatus.ERROR_HTTP
          && domainCreate.getHttpResponse().getStatusCode() != 409) {
        AppLogger.warn("Request failed with http status " + domainCreate.getHttpResponse().getStatusCode());
      } else {
        AppLogger.warn("Request failed with status " + domainCreate.getStatus());
      }
    }
    
    return domainCreate;
  }

  public ClientResponse<Domain> updateDomain(Domain domain, String domainId) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while updating the domain");
      return null;
    }

    ClientResponse<Domain> domainUpdate = domainClient.patchDomain(tokenString, domainId, domain);

    if (domainUpdate.getStatus() != ClientStatus.SUCCESS) {
      if (domainUpdate.getStatus() == ClientStatus.ERROR_HTTP && domainUpdate.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      } else {
        AppLogger.warn("Request failed with status " + domainUpdate.getStatus());
      }
    }

    return domainUpdate;
  }

  public void deleteDomain(Domain domain) {
    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while creating a domain");
    }

    domain.setEnabled(false);
    updateDomain(domain, domain.getId());
    ClientResponse<String> domainDeleted = domainClient.deleteDomain(tokenString, domain.getId());

    if (domainDeleted.getStatus() != ClientStatus.SUCCESS) {
      if (domainDeleted.getStatus() == ClientStatus.ERROR_HTTP
          && domainDeleted.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Delete domain request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      }
      // 409 error is handled in account service, which is required to check for duplicate domain
      else if (domainDeleted.getStatus() == ClientStatus.ERROR_HTTP
          && domainDeleted.getHttpResponse().getStatusCode() != 409) {
        AppLogger.warn("Delete domain request failed with http status "
            + domainDeleted.getHttpResponse().getStatusCode());
      } else {
        AppLogger.warn("Delete domain request failed with status " + domainDeleted.getStatus());
      }
    }
  }
  
  public ClientResponse<DomainConfig> createDomainIdp(String domainConfig, String domainId) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while creating a domain configuration(idp)");
      return null;
    }

    ClientResponse<DomainConfig> domainIdpCreate = domainClient.createDomainConfig(tokenString, domainId, domainConfig);
    if (domainIdpCreate.getStatus() != ClientStatus.SUCCESS) {
      if (domainIdpCreate.getStatus() == ClientStatus.ERROR_HTTP
          && domainIdpCreate.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();
      }
       else {
        AppLogger.warn("Request failed with status " + domainIdpCreate.getStatus());
      }
    }
    return domainIdpCreate;
  }


  public ClientResponse<DomainConfig> updateDomainIdp(String domainConfig, String domainId) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while creating a domain configuration(idp)");
      return null;
    }

    ClientResponse<DomainConfig> domainIdpUpdate = domainClient.updateDomainConfig(tokenString, domainId, domainConfig);
    if (domainIdpUpdate.getStatus() != ClientStatus.SUCCESS) {
      if (domainIdpUpdate.getStatus() == ClientStatus.ERROR_HTTP
          && domainIdpUpdate.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();
      }
      else {
        AppLogger.warn("Request failed with status " + domainIdpUpdate.getStatus());
      }
    }
    return domainIdpUpdate;
  }

  public ClientResponse<DomainConfig> getDomainIdp(String domainId) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while creating a domain configuration(idp)");
      return null;
    }

    ClientResponse<DomainConfig> domainIdp = domainClient.getDomainConfig(tokenString, domainId);
    if (domainIdp.getStatus() != ClientStatus.SUCCESS) {
      if (domainIdp.getStatus() == ClientStatus.ERROR_HTTP
          && domainIdp.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();
      }
      else {
        AppLogger.warn("Request failed with status " + domainIdp.getStatus());
      }
    }
    return domainIdp;
  }
  
  public ClientResponse<DomainConfig> deleteDomainIdp(String domainId) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while creating a domain configuration(idp)");
      return null;
    }

    ClientResponse<DomainConfig> domainIdp = domainClient.deleteDomainConfig(tokenString, domainId);
    if (domainIdp.getStatus() != ClientStatus.SUCCESS) {
      if (domainIdp.getStatus() == ClientStatus.ERROR_HTTP
          && domainIdp.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();
      }
      else {
        AppLogger.warn("Request failed with status " + domainIdp.getStatus());
      }
    }
    return domainIdp;
  }
  
  
  public Users getUsers(String domainId) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while getting domain users");
      return null;
    }

    ClientResponse<Users> usersResponse = userClient.getAllUsersForDomain(tokenString, domainId);
    Users users = null;

    if (usersResponse.getStatus() != ClientStatus.SUCCESS) {
      if (usersResponse.getStatus() == ClientStatus.ERROR_HTTP
          && usersResponse.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      } else {
        AppLogger.warn("Request failed with status " + usersResponse.getStatus());
      }
    } else {
      users = usersResponse.getHttpResponse().getResponseBody();
    }

    return users;
  }

  public Groups getGroups(String domainId) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while getting domain users");
      return null;
    }

    ClientResponse<Groups> groupsResponse = groupClient.getAllGroupsForDomain(tokenString, domainId);
    Groups groups = null;

    if (groupsResponse.getStatus() != ClientStatus.SUCCESS) {
      if (groupsResponse.getStatus() == ClientStatus.ERROR_HTTP
          && groupsResponse.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      } else {
        AppLogger.warn("Request failed with status " + groupsResponse.getStatus());
      }
    } else {
      groups = groupsResponse.getHttpResponse().getResponseBody();
    }

    return groups;
  }

  public ClientResponse<Project> createProject(Project project) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while creating a project");
      return null;
    }

    ClientResponse<Project> projectCreate = projectClient.createProject(tokenString, project);

    if (projectCreate.getStatus() != ClientStatus.SUCCESS) {
      if (projectCreate.getStatus() == ClientStatus.ERROR_HTTP && projectCreate.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      } else {
        AppLogger.warn("Request failed with status " + projectCreate.getStatus());
      }
    }

    return projectCreate;
  }

  public Project getProjectDetails(String projectId) {

    String tokenString = this.getCSAToken();
    if (StringUtils.isEmpty(tokenString)) {
      AppLogger.warn("Token is empty while getting project details");
      return null;
    }

    ClientResponse<Project> projectResponse = projectClient.getProjectDetails(tokenString, projectId);
    Project project = null;

    if (projectResponse.getStatus() != ClientStatus.SUCCESS) {
      if (projectResponse.getStatus() == ClientStatus.ERROR_HTTP
          && projectResponse.getHttpResponse().getStatusCode() == 401) {
        AppLogger.warn("Request failed with 401 error, invalidating cached token");
        this.invalidateCSAToken();;
      } else {
        AppLogger.warn("Request failed with status " + projectResponse.getStatus());
      }
    } else {
      project = projectResponse.getHttpResponse().getResponseBody();
      AppLogger.debug("Successfully received details of project " + projectId);
    }
    return project;
  }
}
