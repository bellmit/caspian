package com.emc.caspian.ccs.account.client;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.ClientConfig;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.RestClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.crs.CRSClientBuilder;
import com.emc.caspian.crs.ServiceLocationClient;
import com.emc.caspian.crs.model.ApplicationException;
import com.emc.caspian.fabric.config.Configuration;

/**
 * This client can be used to get controller hosts endpoints like c3 and bdata and call their corresponding notification
 * APIs. Currently it has APIs for notifying only c3, this can be extended to notify bdata too
 * 
 * @author raod4
 *
 */
public class CaspianControllerClient {

  RestClient client;

  private static final Logger _log = LoggerFactory.getLogger(CaspianControllerClient.class);
  private static final String ACCOUNT_NOTIFICATION_PATH = "/v1/accounts/%s";
  private static final String DOMAIN_NOTIFICATION_PATH = "/v1/domains/%s";
  private static final String ACCOUNT_DELETION_NOTIFICATION_TASK_PATH = "/v1/accounts/%s/tasks/%s";
  private static final String SECTION = "server";
  private static final String DEFAULT = "true";
  private static final Boolean IGNORE_SERVER_CERTS = Configuration.make(Boolean.class, SECTION + ".ignorecerts",
      DEFAULT).value();


  public RestClient getControllerClient(String host) {
    RestClient client = null;
    ClientConfig config;
    URL clientURL = null;
    try {
      clientURL = new URL(host);
    } catch (MalformedURLException e) {
      _log.warn("Malformed controller URL encountered");
    }
    config =
        new ClientConfig().withProtocol(clientURL.getProtocol()).withHost(clientURL.getHost())
            .withPort(clientURL.getPort()).withIgnoringCertificates(IGNORE_SERVER_CERTS);
    client = new RestClient(config);
    return client;
  }

  /**
   * Method to send create account notification to each of the caspian-controllers
   * 
   * @param authToken
   * @param domain
   * @param accountId
   * @param listOfControllerHosts
   * @return ClientStatus
   */
  public Map<String, ClientResponse<String>> notifyCreateAccount(String authToken, String domain, String accountId,
      List<String> listOfControllerHosts) {
    ClientResponse<String> response = null;
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    Map<String, ClientResponse<String>> responses = new HashMap<String, ClientResponse<String>>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);
    for (String host : listOfControllerHosts) {
      if (host.isEmpty()) {
        // if the entry is empty proceed to fetch the next entry
        continue;
      }
      String account_notification_path = String.format(ACCOUNT_NOTIFICATION_PATH, accountId);
      _log.debug("Sending create account notification to path " + account_notification_path);
      response = this.getControllerClient(host).post(String.class, domain, account_notification_path, requestHeader);
      responses.put(host, response);
    }
    return responses;
  }


  /**
   * Method to send delete domain notification to the caspian-controller
   * 
   * @param domainId
   * @return ClientStatus
   */
  @Deprecated
  public Map<String, ClientResponse<String>> notifyDeleteDomain(String authToken, String domainId,
      List<String> listOfControllerHosts) {
    ClientResponse<String> response = null;

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    Map<String, ClientResponse<String>> responses = new HashMap<String, ClientResponse<String>>();

    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);
    // Currently passing empty requestHeader as the RestClient delete method signature requires one. This can be later
    // used to populate any authorization related info
    for (String host : listOfControllerHosts) {
      if (host.isEmpty()) {
        // if the entry is empty proceed to fetch the next entry
        continue;
      }
      String domain_notification_path = String.format(DOMAIN_NOTIFICATION_PATH, domainId);
      _log.debug("Sending delete domain notification to " + domain_notification_path);
      response = this.getControllerClient(host).delete(String.class, domain_notification_path, requestHeader, domainId);
      responses.put(host, response);
    }
    return responses;
  }

  /**
   * Method to send delete account notification to the caspian-controller
   * 
   * @param accountId
   * @return ClientStatus
   */
  public Map<String, ClientResponse<String>> notifyDeleteAccount(String authToken, String accountId,
      List<String> listOfControllerHosts) {
    ClientResponse<String> response = null;

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    Map<String, ClientResponse<String>> responses = new HashMap<String, ClientResponse<String>>();

    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);
    // Currently passing empty requestHeader as the RestClient delete method signature requires one. This can be later
    // used to populate any authorization related info
    for (String host : listOfControllerHosts) {
      if (host.isEmpty()) {
        // if the entry is empty proceed to fetch the next entry
        continue;
      }
      String account_deletion_path = String.format(ACCOUNT_NOTIFICATION_PATH, accountId);
      _log.debug("Sending delete account notification to path" + account_deletion_path);
      response = this.getControllerClient(host).delete(String.class, account_deletion_path, requestHeader, accountId);
      responses.put(host, response);
    }

    return responses;
  }
  
  /**
   * Method to send delete account notification to the caspian-controller
   * 
   * @param accountId
   * @return ClientStatus
   */
  public ClientResponse<String> notifyDeleteAccountToController(String authToken, String accountId,
      String controllerHost) {
    ClientResponse<String> response = null;

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);
    String account_deletion_path = String.format(ACCOUNT_NOTIFICATION_PATH, accountId);
    _log.debug("Sending delete account notification to path" + account_deletion_path);
    response =
        this.getControllerClient(controllerHost).delete(String.class, account_deletion_path, requestHeader, accountId);
    return response;
  }
  
  /**
   * Method to send delete DOMAIN notification to the caspian-controller
   * 
   * @param accountId
   * @return ClientStatus
   */
  public ClientResponse<String> notifyDeleteDomainToController(String authToken, String domainId,
      String controllerHost) {
    ClientResponse<String> response = null;

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);
    String account_deletion_path = String.format(DOMAIN_NOTIFICATION_PATH, domainId);
    _log.debug("Sending delete account notification to path" + account_deletion_path);
    response =
        this.getControllerClient(controllerHost).delete(String.class, account_deletion_path, requestHeader, domainId);
    return response;
  }

  /**
   * Method to get delete notification task status from caspian-controller for an account or a domain
   * 
   * @param accountId
   * @return ClientStatus
   */
  public ClientResponse<String> getNotifyTaskStatus(String authToken, String accountId, String taskId,
      String controllerIP) {
    ClientResponse<String> response = null;
    if (controllerIP.isEmpty()) {
      return response;
    }
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);
    String account_deletion_task_status_path =
        String.format(ACCOUNT_DELETION_NOTIFICATION_TASK_PATH, accountId, taskId);
    _log.debug("Sending get task status request to path" + account_deletion_task_status_path);
    response =
        this.getControllerClient(controllerIP).get(String.class, account_deletion_task_status_path, requestHeader);
    return response;
  }

  /**
   * This method will return the list of controllerHosts from CRS as a string for compatibility with workflow task
   * framework
   * 
   * @return
   */
  public List<String> getListOfControllerHosts() {
    List<String> controllerHosts = new ArrayList<String>();
    String c3ControllerEndpoint = getC3ControllerEndpoint();
    if (c3ControllerEndpoint != null) {
      controllerHosts.add(c3ControllerEndpoint);
    }
    return controllerHosts;
  }

  /**
   * this method returns C3 endpoints
   * 
   * @return
   */
  public String getC3ControllerEndpoint() {
    String c3Host = null;
    Optional<URL> endpoint = null;
    // this step returns the public url endpoint of C3 which is a platform service
    try {
      // this returns the value of the COMPONENT_REGISTRY env variable
      ServiceLocationClient client = CRSClientBuilder.newServiceLocationClient();
      endpoint = client.getEndpoint(PLATFORM, C3, e -> e.getName().equals(PUBLIC_ENDPOINT));
      // this exception is thrown when c3 component is not installed
    } catch (ApplicationException e) {
      _log.warn(e.getMessage());
      return c3Host;
    } catch (IllegalStateException e) {
      _log.warn(e.getMessage());
      return c3Host;
    }
    if (endpoint.isPresent()) {
      c3Host = endpoint.get().toExternalForm();
    } else {
      _log.warn("C3 component not present");
    }
    return c3Host;
  }


  /**
   * this method returns Account service public endpoint
   * 
   * @return
   */
  public String getAccountServiceEndpointFromCRS() {
    String accountServiceHost = null;
    Optional<URL> endpoint = null;
    // this step returns the public url endpoint of AS which is a platform service
    try {
      // this returns the value of the COMPONENT_REGISTRY env variable
      ServiceLocationClient client = CRSClientBuilder.newServiceLocationClient();
      endpoint = client.getEndpoint(PLATFORM, ACCOUNT, e -> e.getName().equals(PUBLIC_ENDPOINT));
      _log.info("Got account service value from CRS " + endpoint);
      // this exception is thrown when AS component is not installed
    } catch (ApplicationException e) {
      _log.warn(e.getMessage());
      return accountServiceHost;
    }catch (IllegalStateException e) {
      _log.warn(e.getMessage());
      return accountServiceHost;
    }
    if (endpoint.isPresent()) {
      accountServiceHost = endpoint.get().toExternalForm();
    } else {
      _log.warn("Account Service component not present");
    }
    return accountServiceHost;
  }
  
  private static final String PLATFORM = "platform";
  private static final String ACCOUNT = "account";
  private static final String PUBLIC_ENDPOINT = "public";
  private static final String C3 = "c3";
}
