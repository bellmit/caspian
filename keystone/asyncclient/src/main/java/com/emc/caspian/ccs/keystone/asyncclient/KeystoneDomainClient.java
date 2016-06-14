package com.emc.caspian.ccs.keystone.asyncclient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.ASyncRestClient;
import com.emc.caspian.ccs.client.ClientResponseCallback;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.Domain;
import com.emc.caspian.ccs.keystone.model.Domains;
import com.fasterxml.jackson.core.JsonProcessingException;



public class KeystoneDomainClient {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneDomainClient.class);

  private ASyncRestClient client;

  public KeystoneDomainClient(ASyncRestClient client) {
    this.client = client;
  }

  // If domainName is null, this method will search for all domains.
  // If domainName is provided, this method will search for all domains(Ideally, should be only one)
  // with name domainName
  // If isEnabled is null, this method will search for all domains.
  // If isEnabled is true, it will search for all domains with enabled flag as true
  // If isEnabled is false, it will search for all domains with enabled flag as false
  public Future<ClientResponse<Domains>> getDomainsByFilter(ClientResponseCallback<Domains> callback,
      String authenticationToken, String domainName, Boolean isEnabled) {

    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    QueryParams params = new QueryParams();

    if (domainName != null) {
      params.addQueryParam(PathConstants.NAME_QUERY_PARAM, domainName);
    }
    if (isEnabled != null) {
      params.addQueryParam(PathConstants.ENABLED_QUERY_PARAM, isEnabled);
    }

    return this.client.get(Domains.class, PathConstants.KEYSTONE_DOMAIN_PATH_V3, requestHeader, params, callback);

  }

  public Future<ClientResponse<Domains>> getAllDomains(ClientResponseCallback<Domains> callback,
      String authenticationToken) {

    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    return this.client.get(Domains.class, PathConstants.KEYSTONE_DOMAIN_PATH_V3, requestHeader, callback);
  }

  public Future<ClientResponse<Domain>> getDomain(ClientResponseCallback<Domain> callback,
      String authenticationToken, String id) {

    Map<String, String> requestHeader = new HashMap<String, String>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    return this.client.get(Domain.class, PathConstants.KEYSTONE_DOMAIN_ID_PATH_V3, requestHeader, callback, id);

  }

  public Future<ClientResponse<Domain>> createDomain(ClientResponseCallback<Domain> callback,
      String authenticationToken, Domain domain) throws KeystoneClientException {
    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    try {
      return this.client.post(Domain.class, PathConstants.KEYSTONE_DOMAIN_PATH_V3, requestHeader, domain,
          callback);
    } catch (JsonProcessingException e) {
      _log.warn("Exception occured during CREATE domain {}", e);
      throw new KeystoneClientException(e);
    }

  }

  public Future<ClientResponse<String>> deleteDomain(ClientResponseCallback<String> callback,
      String authenticationToken, String id) {
    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    return this.client
        .delete(String.class, PathConstants.KEYSTONE_DOMAIN_ID_PATH_V3, requestHeader, callback, id);
  }


  public Future<ClientResponse<Domain>> patchDomain(ClientResponseCallback<Domain> callback,
      String authenticationToken, String id, Domain domain) throws KeystoneClientException {

    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);
    try {
      return this.client.patch(Domain.class, PathConstants.KEYSTONE_DOMAIN_ID_PATH_V3, requestHeader, domain,
          callback, id);
    } catch (JsonProcessingException e) {
      _log.warn("Exception occured during PATCH domain {}", e);
      throw new KeystoneClientException(e);
    }
  }


}
