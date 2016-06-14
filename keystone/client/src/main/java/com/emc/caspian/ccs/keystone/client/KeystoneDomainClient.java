package com.emc.caspian.ccs.keystone.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.RestClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.Domain;
import com.emc.caspian.ccs.keystone.model.DomainConfig;
import com.emc.caspian.ccs.keystone.model.Domains;



public class KeystoneDomainClient {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneDomainClient.class);

  private RestClient client;

  public KeystoneDomainClient(RestClient client) {
    this.client = client;
  }

  // If domainName is null, this method will search for all domains.
  // If domainName is provided, this method will search for all domains(Ideally, should be only one)
  // with name domainName
  // If isEnabled is null, this method will search for all domains.
  // If isEnabled is true, it will search for all domains with enabled flag as true
  // If isEnabled is false, it will search for all domains with enabled flag as false
  public ClientResponse<Domains> getDomainsByFilter(String authenticationToken, String domainName, Boolean isEnabled) {

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    QueryParams params = new QueryParams();

    if (domainName != null) {
      params.addQueryParam(PathConstants.NAME_QUERY_PARAM, domainName);
    }
    if (isEnabled != null) {
      params.addQueryParam(PathConstants.ENABLED_QUERY_PARAM, isEnabled);
    }

    ClientResponse<Domains> response =
        this.client.get(Domains.class, params, PathConstants.KEYSTONE_DOMAIN_PATH_V3, requestHeader);
    _log.debug("Received response : {} for GET Domains with filter", response.getStatus());
    return response;
  }

  public ClientResponse<Domains> getAllDomains(String authenticationToken) {

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<Domains> response =
        this.client.get(Domains.class, PathConstants.KEYSTONE_DOMAIN_PATH_V3, requestHeader);
    _log.debug("Received response : {} for GET Domains", response.getStatus());
    return response;
  }

  public ClientResponse<Domain> getDomain(String authenticationToken, String id) {

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<Domain> response =
        this.client.get(Domain.class, PathConstants.KEYSTONE_DOMAIN_ID_PATH_V3, requestHeader, id);
    _log.debug("Received response : {} for GET Domain", response.getStatus());
    return response;
  }

  public ClientResponse<Domain> createDomain(String authenticationToken, Domain domain) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<Domain> response =
        this.client
            .post(Domain.class, domain, PathConstants.KEYSTONE_DOMAIN_PATH_V3, requestHeader);

    _log.debug("Received response : {} for CREATE Domain", response.getStatus());
    return response;

  }

  public ClientResponse<String> deleteDomain(String authenticationToken, String id) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<String> response =
        this.client.delete(String.class, PathConstants.KEYSTONE_DOMAIN_ID_PATH_V3, requestHeader,
            id);

    _log.debug("Received response : {} for DELETE Domain", response.getStatus());
    return response;
  }


  public ClientResponse<Domain> patchDomain(String authenticationToken, String id, Domain domain) {

    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<Domain> response =
        this.client.patch(Domain.class, domain, PathConstants.KEYSTONE_DOMAIN_ID_PATH_V3,
            requestHeader, id);

    _log.debug("Received response : {} for PATCH Domain", response.getStatus());
    return response;
  }

  
  public ClientResponse<DomainConfig> createDomainConfig(String authenticationToken, String domainId,
      String domainConfig) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);
    
    ClientResponse<DomainConfig> response =
        this.client.put(DomainConfig.class, domainConfig, PathConstants.KEYSTONE_DOMAIN_IDP_PATH_V3, requestHeader,
            domainId);
    _log.debug("Received response : {} for CREATE domain config", response.getStatus());
    return response;
  }
  
  public ClientResponse<DomainConfig> updateDomainConfig(String authenticationToken, String domainId,
      String domainConfig) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<DomainConfig> response =
        this.client.patch(DomainConfig.class, domainConfig, PathConstants.KEYSTONE_DOMAIN_IDP_PATH_V3, requestHeader,
            domainId);
    _log.debug("Received response : {} for UPDATE domain config", response.getStatus());
    return response;
  }
  
  public ClientResponse<DomainConfig> getDomainConfig(String authenticationToken, String domainId) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<DomainConfig> response =
        this.client.get(DomainConfig.class, PathConstants.KEYSTONE_DOMAIN_IDP_PATH_V3, requestHeader, domainId);
    _log.debug("Received response : {} for GET domain config", response.getStatus());
    return response;
  }

  public ClientResponse<DomainConfig> deleteDomainConfig(String authenticationToken, String domainId) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);

    ClientResponse<DomainConfig> response =
        this.client.delete(DomainConfig.class, PathConstants.KEYSTONE_DOMAIN_IDP_PATH_V3, requestHeader, domainId);
    _log.debug("Received response : {} for DELETE domain config", response.getStatus());
    return response;
  }
  
  
}
