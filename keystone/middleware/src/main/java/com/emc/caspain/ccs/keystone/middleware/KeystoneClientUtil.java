package com.emc.caspain.ccs.keystone.middleware;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.keystone.middleware.exceptions.InternalException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.InvalidTokenException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.MiddlewareException;
import com.emc.caspian.ccs.client.ClientConfig;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.common.KeystoneDateTimeUtils;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.ccs.keystone.model.Authentication.Identity;
import com.emc.caspian.ccs.keystone.model.Authentication.Scope;


class KeystoneClientUtil {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneClientUtil.class);

  // To mark the csa token as expired one minute before its expiry time to handle time delay issues when sending a
  // request to the keystone server
  private static final Long CORRECTION = 60000L;

  private KeystoneTokenClient ksTokenClient;
  private Token cachedCSAToken;
  private Authentication authenticate;

  public KeystoneClientUtil(KeystoneConfiguration keystoneConfiguration) {
    ClientConfig clConfig = new ClientConfig();

    // authURI should be <protocol>://host:port
    URL authUri;
    try {
      authUri = new URL(keystoneConfiguration.getAuthURI());
    } catch (MalformedURLException e) {
      _log.error("Invalid keystone auth uri configured: " + keystoneConfiguration.getAuthURI());
      throw new RuntimeException("Invalid keystone auth uri configured");
    }

    clConfig.setHost(authUri.getHost());
    clConfig.setProtocol(authUri.getProtocol());
    if (authUri.getPort() > 0) {
      clConfig.setPort(authUri.getPort());
    }

    // TODO: set this to false when we have the fabric CA in place
    clConfig.setIgnoreCertificates(true);

    KeystoneClient eciClient = new KeystoneClient(clConfig);
    ksTokenClient = eciClient.getKeystoneTokenClient();
    if (ksTokenClient == null) {
      _log.error("Failed to get keystone token client object");
      throw new RuntimeException("Failed to create KeystoneTokenClient object");
    }

    authenticate = new Authentication();

    // set identity
    Authentication.Identity identity = new Identity();
    identity =
        Authentication.Identity.password(Constants.DEFAULT_DOMAIN, keystoneConfiguration.getUser(),
            keystoneConfiguration.getPassword());
    authenticate.setIdentity(identity);

    // set scope as default
    Authentication.Scope scope = new Scope();
    scope = Authentication.Scope.domain(Constants.DEFAULT_DOMAIN);
    authenticate.setScope(scope);

    cachedCSAToken = null;

    _log.info("Initialized keystone client with configuration: " + keystoneConfiguration.getString());
  }

  private synchronized String getCSAToken() {
    // Marking the token as expired one minute before its expiry time to handle time delay issues when sending a request
    // to the keystone server
    if (cachedCSAToken == null
        || KeystoneDateTimeUtils.getTimeInMillis(cachedCSAToken.getExpiresAt()) < (CORRECTION + System
            .currentTimeMillis())) {
      _log.debug("The cached csa token got expired getting new token from server");
      ClientResponse<Token> tokenResp = ksTokenClient.getToken(authenticate, true);
      if (ClientStatus.SUCCESS != tokenResp.getStatus()) {
        _log.warn("Failed to retrive admin auth token from Keystone Server received error {}", tokenResp.getStatus());
        cachedCSAToken = null;
        return null;
      }

      cachedCSAToken = tokenResp.getHttpResponse().getResponseBody();
    }
    return cachedCSAToken.getTokenString();
  }


  private synchronized void resetCSAToken() {
    cachedCSAToken = null;
  }

  // Validate this token from the configured Keystone server
  public Token validateAndGetDetailsFromServer(String authToken) throws MiddlewareException {
    String csaToken = getCSAToken();
    if (csaToken != null) {
      ClientResponse<Token> tokenResp = ksTokenClient.validateToken(csaToken, authToken, true);
      if (ClientStatus.SUCCESS != tokenResp.getStatus()) {
        if (tokenResp.getHttpResponse().getStatusCode() == 401) {
          _log.warn("The cached csa token got revoked clearing the token");
          resetCSAToken();
          throw new InternalException("Invalid csa token, need to retry");
        } else if (tokenResp.getStatus() == ClientStatus.ERROR_HTTP && tokenResp.getHttpResponse().getStatusCode() < 500) {
          _log.warn("Supplied auth token is invalid. received server error {}", tokenResp.getStatus());
          throw new InvalidTokenException(tokenResp.getErrorMessage());          
        } else {
          _log.warn("Keystone server error while validating token {}", tokenResp.getStatus());
          throw new InternalException("Keystone server error while validating token: " + tokenResp.getErrorMessage());
        }
      } else {
        Token token = tokenResp.getHttpResponse().getResponseBody();
        _log.debug("Auth token successfully validated from server");
        return token;
      }
    } else {
      _log.warn("Could not get the csa token");
      throw new InternalException("Could not get the csa token");
    }
  }

  // Get the Keystone server's public key required for PKI and PKIZ offline token verification
  public String getKeystonePublicKey() {
    ClientResponse<String> certResponse = ksTokenClient.getCertificate();
    if (certResponse.getStatus() == ClientStatus.SUCCESS) {
      _log.debug("Received response : {} for get certificate", certResponse.getHttpResponse().getStatusCode());
      return certResponse.getHttpResponse().getResponseBody();
    } else {
      _log.warn("Failed to get certificate from keystone server: "
          + ((certResponse != null) ? certResponse.getErrorMessage() : ""));
      return null;
    }
  }

  // Update the client with the keystone certificate
  public void updateClientWithCertificate(String certificate) {
    if (ksTokenClient.setCertificate(certificate))
      _log.debug("Client updated with the certificate");
    else
      _log.warn("Updating certificate to the client failed");
  }

  // Get the revocation list as Array list of strings
  // this is not being used now
  public ArrayList<String> getRevocationList() {
    String csaToken = getCSAToken();
    if (csaToken != null) {
      ClientResponse<String> revokeResp = ksTokenClient.getRevocationList(csaToken);
      if (revokeResp.getStatus() == ClientStatus.SUCCESS) {
        _log.debug("Received response : {} for get revocation list", revokeResp.getHttpResponse().getStatusCode());
        String jsonString = revokeResp.getHttpResponse().getResponseBody();
        ArrayList<String> revocationList = new ArrayList<String>();
        int index = 0;
        // Manually parsing the the json string to get the list of token ids
        // TODO Make sure the id doesn't appear anywhere else in the String
        index = jsonString.indexOf("id", index);
        while (index != -1) {

          revocationList.add(jsonString.substring(index + 6, index + 38));
          index = index + 39;
          index = jsonString.indexOf("id", index);
        }
        return revocationList;
      } else {
        if (revokeResp.getHttpResponse().getStatusCode() == 401) {
          _log.warn("The cached csa token got revoked clearing the token");
          resetCSAToken();

        } else {
          _log.warn("Failed to get revocation list from server with error {}", revokeResp.getStatus());
        }
        return null;
      }
    } else {
      _log.warn("Could not get the csa token");
      return null;
    }
  }

  private String convertToISO(String date) {
    DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss z");
    DateTime datetime = formatter.parseDateTime(date);
    DateTimeFormatter isoFormatter = ISODateTimeFormat.dateTime();
    return isoFormatter.print(datetime);
  }

  // Get list of revocation events
  public RevocationEvents getRevocationEvents(String sinceDate) {
    String csaToken = getCSAToken();
    if (csaToken != null) {
      ClientResponse<String> revokeResp = ksTokenClient.getRevocationEvents(csaToken, sinceDate);
      if (revokeResp.getStatus() == ClientStatus.SUCCESS) {
        _log.debug("Received response : {} for get revocation events", revokeResp.getHttpResponse().getStatusCode());
        // Converting header date from apache server format to ISO format
        String headerDate = convertToISO(revokeResp.getHttpResponse().getHeaders().get("Date").get(0));
        String jsonString = revokeResp.getHttpResponse().getResponseBody();
        // Parsing the json string and getting list of revocation events into an object
        RevocationEvents revokeEvents = JsonHelper.deserializeFromJson(jsonString, RevocationEvents.class);
        revokeEvents.setDateTime(headerDate);
        return revokeEvents;
      } else {
        if (revokeResp.getHttpResponse().getStatusCode() == 401) {
          _log.warn("The cached csa token got revoked clearing the token");
          resetCSAToken();

        } else {
          _log.warn("Failed to get revocation events  from server with error {}", revokeResp.getStatus());
        }
        return null;
      }
    } else {
      _log.warn("Could not get the csa token");
      return null;
    }
  }


}
