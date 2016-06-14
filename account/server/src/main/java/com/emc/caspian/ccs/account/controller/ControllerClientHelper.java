package com.emc.caspian.ccs.account.controller;

import java.util.List;
import java.util.Map;

import com.emc.caspian.ccs.account.client.CaspianControllerClient;
import com.emc.caspian.ccs.client.response.ClientResponse;


/**
 * Singleton class that helps in initializing controller clients 
 * 
 * @author raod4
 *
 */
public class ControllerClientHelper {
 
  private CaspianControllerClient controllerClient;
  private static volatile ControllerClientHelper controllerClientHelper = null;
  
  public static ControllerClientHelper getInstance() {
    if (controllerClientHelper == null) {
      synchronized (ControllerClientHelper.class) {
        if (controllerClientHelper == null)
          controllerClientHelper = new ControllerClientHelper();
      }
    }
    return controllerClientHelper;
  }
  
  private ControllerClientHelper(){
    this.controllerClient = new CaspianControllerClient();
  }
   
  /**
   * Method to send create account notification to caspian-controller
   * 
   * @param domain
   * @param accountId
   * @return ClientStatus
   */
  public Map<String,ClientResponse<String>> notifyCreateAccount(String authToken, String domain, String accountId, List<String> listOfControllerHosts) {
    return controllerClient.notifyCreateAccount(authToken, domain, accountId, listOfControllerHosts);
  }

  /**
   * Method to send delete domain notification to the caspian-controller
   * 
   * @param domainId
   * @return ClientStatus
   */
  public Map<String,ClientResponse<String>> notifyDeleteDomain(String authToken, String domainId, List<String> listOfControllerHosts) {
    return controllerClient.notifyDeleteDomain(authToken, domainId, listOfControllerHosts);
  }

  /**
   * Method to send delete domain notification to the caspian-controller
   * 
   * @param accountId
   * @return ClientStatus
   */
  public Map<String,ClientResponse<String>> notifyDeleteAccount(String authToken, String accountId, List<String> listOfControllerHosts) {
    return controllerClient.notifyDeleteAccount(authToken, accountId, listOfControllerHosts);
  }

  /**
   * Method to return the list of controllerHosts
   * 
   * @param controllerResponse
   * @return
   */
  public List<String> getListOfControllerHosts() {
    return controllerClient.getListOfControllerHosts();
  }

  /**
   * Method to return the list of controllerHosts
   * 
   * @param Account service endpoint in CRS
   * @return
   */
  public String getASHostFromCRS() {
    return controllerClient.getAccountServiceEndpointFromCRS();
  }
}
