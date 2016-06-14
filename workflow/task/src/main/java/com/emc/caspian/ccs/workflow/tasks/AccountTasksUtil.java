package com.emc.caspian.ccs.workflow.tasks;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.account.client.CaspianControllerClient;
import com.emc.caspian.ccs.account.datacontract.C3StatusResponse;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.client.KeystoneDomainClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.keystone.model.Domain;
import com.emc.caspian.ccs.workflow.TaskException;

public class AccountTasksUtil {

  private static final Logger _log = LoggerFactory.getLogger(AccountTasksUtil.class);

  private static String protocol;
  private static String hostName;
  private static int port;
  private static URL uri;
  private static boolean ignoreCerts = true;
  private static KeystoneClient eciClient;
  private static KeystoneDomainClient domainClient;
  private static final char PATH_SEPARATOR = '/';
  private static final String LOCATION_HEADER = "Location";
  private static final int HTTP_ACCEPTED = 202;
  private static final int ERROR_HTTP_NOT_FOUND = 404;
  private static final int ERROR_HTTP_NOT_IMPLEMENTED = 405;
  private static final int ERROR_SERVER_UNREACHABLE = 503;
  private static final long SLEEP_TIME_IN_MILLISEC = 10000;

  /**
   * Method to initialize the keystone clients
   * 
   * @param keystoneUri
   */
  public static void initialiseKeystoneClients(String keystoneUri) {
    try {
      uri = new URL(keystoneUri);
      protocol = uri.getProtocol();
      hostName = uri.getHost();
      port = uri.getPort();
    } catch (MalformedURLException e) {
      // not retrying because of the URL is malformed then no need to retry
      _log.error("Keystone initialization failed");
      throw new TaskException("Invalid keystone auth uri configured", false);
    }
    eciClient = new KeystoneClient(protocol, hostName, port, ignoreCerts);
    domainClient = eciClient.getKeystoneDomainClient();
  }

  public static void patchDomain(String token, String id, Domain domain) {

    // If token is null, return failure
    if (StringUtils.isEmpty(token)) {
      _log.warn("Patching domain "+id+" failed because token was null");
      return;
    }

    ClientResponse<Domain> patchDomainResponse = domainClient.patchDomain(token, id, domain);

    if (patchDomainResponse.getStatus() == ClientStatus.SUCCESS) {
      _log.debug("Successfully patched domain with id " +id);
    } else {
      int statusCode = patchDomainResponse.getHttpResponse().getStatusCode();
      // Token Expired or revoked
      if (statusCode == 401) {
        _log.warn("Authentication failure while patching domain "+id);
        return;
      }
      // If domain is already deleted, return success else retry
      else if (statusCode != 404) {
        _log.warn("Unexpected error while patching domain " +id+ ", retrying. Status code "
            + patchDomainResponse.getStatus());
        return;
      }
    }
  }

  public static void deleteDomain(String token, String id) {

    // If token is null, return failure
    if (StringUtils.isEmpty(token)) {
      _log.warn("Deleting domain "+id+" failed because token is null");
      return;
    }

    ClientResponse<String> deleteDomainResponse = domainClient.deleteDomain(token, id);

    if (deleteDomainResponse.getStatus() != ClientStatus.SUCCESS) {
      int statusCode = deleteDomainResponse.getHttpResponse().getStatusCode();
      // Token Expired or revoked
      if (statusCode == 401) {
        _log.warn(String.format("Authentication failure while deleting domain %s", id));
        return;
      }
      // If domain is already deleted, return success else retry
      else if (statusCode != 404) {
        _log.warn("Unexpected error while deleting domain " +id+ ", retrying. Status code "
            +deleteDomainResponse.getStatus());
        return;
      }
    }
  }

  /**
   * Method to get the controller notification task status
   * 
   * @param caspianControllerClient
   * @param clientResponse
   * @param accountId
   * @return
   */
  public static void notifyAccountDeletionAndGetTaskStatus(String authToken, String accountId,
      List<String> listOfControllerHosts) {
    // After disabling the domain notification is made to controller.
    CaspianControllerClient caspianControllerClient = new CaspianControllerClient();

    Map<String, ClientResponse<String>> controllerHostResponses =
        caspianControllerClient.notifyDeleteAccount(authToken, accountId, listOfControllerHosts);
    if (controllerHostResponses.isEmpty()) {
      _log.warn("No controller hosts found to notify account deletion for account " + accountId);
      return;
    }
    // for each controller host response the below steps will be executed
    for (Entry<String, ClientResponse<String>> controllerHost : controllerHostResponses.entrySet()) {
      String controllerIP = controllerHost.getKey();
      ClientResponse<String> controllerHostResponse = controllerHost.getValue();
      boolean checkForDeleteErrorResponse =
          checkForDeleteAccountErrorResponse(caspianControllerClient, controllerIP, controllerHostResponse, authToken,
              accountId);
      if (!checkForDeleteErrorResponse) {
        _log.error("Controller host " + controllerIP + " returned "
            + controllerHostResponse.getHttpResponse().getStatusCode() + " for " + accountId);
        // continue to fetch the next controller IP.
        continue;
      }
      getDeleteTaskStatus(authToken, controllerHostResponse, controllerIP, accountId, caspianControllerClient);
    }
  }

  /**
   * This method will retry 10 times (arbitrary) in case of an error while communicating with ControllerIp
   * for account deletion
   * 
   * @param client
   * @param controllerIP
   * @param controllerHostResponse
   * @param authToken
   * @param resourceId
   * @return
   */
  private static boolean checkForDeleteAccountErrorResponse(CaspianControllerClient client, String controllerIP,
      ClientResponse<String> controllerHostResponse, String authToken, String resourceId) {
    int count = 0;
    do {
      if (controllerHostResponse == null
          || controllerHostResponse.getHttpResponse().getStatusCode() == ERROR_SERVER_UNREACHABLE) {
        _log.error("C3 returned " + controllerHostResponse.getHttpResponse().getStatusCode() + " for " + resourceId
            + " Retrying");
        controllerHostResponse = client.notifyDeleteAccountToController(authToken, resourceId, controllerIP);
        try {
          Thread.sleep(SLEEP_TIME_IN_MILLISEC);
          count++;
          continue;
        } catch (InterruptedException e) {
          _log.error("Thread interrupted");
          return false;
        }
      } else {
        break;
      }
    } while ((controllerHostResponse == null
        || controllerHostResponse.getHttpResponse().getStatusCode() == ERROR_SERVER_UNREACHABLE) && count < 10);
    // apart from 503 or null response any other error no need to retry, just logging and returning
    if (controllerHostResponse.getStatus() != ClientStatus.SUCCESS) {
      _log.error("C3 returned " + controllerHostResponse.getHttpResponse().getStatusCode() + " for " + resourceId);
      return false;
    }
    return true;
  }
 
  /**
   * Method to notify delete service and call method to poll the delete of account/domain service notification task
   * status
   * 
   * @param controllerHostResponse
   * @param c3ControllerEndpoint
   * @param resourceId
   * @param caspianControllerClient
   */
  public static void getDeleteTaskStatus(String authToken, ClientResponse<String> controllerHostResponse,
      String c3ControllerEndpoint, String resourceId, CaspianControllerClient caspianControllerClient) {
    String taskUrl, taskId;
    _log.debug("Got response " + controllerHostResponse.getStatus() + " with status code "
        + controllerHostResponse.getHttpResponse().getStatusCode() + " for controller host " + c3ControllerEndpoint
        + " for resource " + resourceId);
    
      if (controllerHostResponse.getHttpResponse().getStatusCode() == 200) {
        // check for 200 and return success
        _log.info("Delete notification sent successfully for resource " + resourceId + ", nothing to delete");
        return;
      }
      // If response is 202 accepted then the controller should return the location information
      else if (controllerHostResponse.getHttpResponse().getStatusCode() == HTTP_ACCEPTED) {
        _log.info("Delete notification sent for resource " + resourceId + ". Received task URL "
            + controllerHostResponse.getHttpResponse().getHeaders().get(LOCATION_HEADER));

        taskUrl = controllerHostResponse.getHttpResponse().getHeaders().get(LOCATION_HEADER).get(0);
        taskId = taskUrl.substring(taskUrl.lastIndexOf(PATH_SEPARATOR) + 1);

        pollForDeleteTaskStatus(authToken, taskId, resourceId, c3ControllerEndpoint, caspianControllerClient);
      }
  }

  /**
   * This method will poll for the task status in a loop for success, if not successful it returns failure
   * 
   * @param taskId
   * @param resourceId
   * @param controllerEndpoint
   */
  public static void pollForDeleteTaskStatus(String authToken, String taskId, String resourceId,
      String c3ControllerEndpoint, CaspianControllerClient caspianControllerClient) {
    ClientResponse<String> response = null;
    C3StatusResponse resp = null;
    int count = 0;
    do {
      response = caspianControllerClient.getNotifyTaskStatus(authToken, resourceId, taskId, c3ControllerEndpoint);
      try {
        // If response of 503 or null: log error and retry (ideally forever, but because of the single thread
        // limitation, we need to cap it. Say 10 times at 10 seconds interval). After retries exhausted, log error and
        // finish account deletion
        if (response == null || response.getHttpResponse().getStatusCode() == ERROR_SERVER_UNREACHABLE) {
          _log.error("Got null response while querying for task status for " + resourceId + " Retrying");
          
        } else if (response.getHttpResponse().getStatusCode() == ERROR_HTTP_NOT_IMPLEMENTED
            || response.getHttpResponse().getStatusCode() == ERROR_HTTP_NOT_FOUND) {
          _log.warn(",Got response " + response.getHttpResponse().getStatusCode() + " for task " + taskId + " for "
              + resourceId);
          return;
        }
        else {
          if (response.getStatus() == ClientStatus.SUCCESS) {
         // get the status from the response
            resp = JsonHelper.deserializeFromJson(response.getHttpResponse().getResponseBody(), C3StatusResponse.class);
            // null check for the status response
            if (resp != null && resp.getStatus() != null) {
              // if the status is "executing" then continue
              if (resp.getStatus().equalsIgnoreCase("executing")) {
                _log.info("Delete notification sent to " + c3ControllerEndpoint + " for " + resourceId
                    + " received task status as " + resp.getStatus() + " hence polling again");
                Thread.sleep(SLEEP_TIME_IN_MILLISEC);
                continue;
                // if the status is "completed" then return from the function as the delete notification is sent
                // successfully
              } else if (resp.getStatus().equalsIgnoreCase("completed")) {
                _log.info("Got delete task status as " + resp.getStatus() + " from " + c3ControllerEndpoint + " for "
                    + resourceId);
                return;
              }
              else if (resp.getStatus().equalsIgnoreCase("error")) {
                _log.error("Got error while querying delete task status, error " + resp.getStatus() + " from " + c3ControllerEndpoint + " for "
                    + resourceId);
                return;
              }
            }
          }
        }
        Thread.sleep(10000);
        count = count + 1;
      } catch (InterruptedException e) {
        _log.error("The deletion thread was interrupted, internal error");
        return;
      }
    } while ((response == null || response.getHttpResponse().getStatusCode() == ERROR_SERVER_UNREACHABLE)
        || (resp == null || resp.getStatus().equalsIgnoreCase("executing")) && count < 10);
    
    //Even after retrying as above if the response is still null or an error then log it and return
    if (response == null || response.getStatus() != ClientStatus.SUCCESS) {
      _log.error(" Got error response from " + c3ControllerEndpoint + " while querying the task " + taskId
          + " for resource " + resourceId);
      return;
    }
  }
}
