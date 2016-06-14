package com.emc.caspian.ccs.keystone.client;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.RestClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.Project;

public class KeystoneProjectClient {

  public KeystoneProjectClient(RestClient client) {
    this.client = client;
  }

  public ClientResponse<Project> getProjectDetails(String authenticationToken, String projectId) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<Project> response =
        this.client.get(Project.class, PathConstants.KEYSTONE_PROJECT_ID_PATH_V3, requestHeader, projectId);

    _log.debug("Received response : {} for get Project Details", response.getStatus());
    return response;

  }

  public ClientResponse<Project> createProject(String authenticationToken, Project project) {

    Map<String, Object> requestHeader = KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    ClientResponse<Project> response =
        this.client.post(Project.class, project, PathConstants.KEYSTONE_PROJECT_PATH_V3, requestHeader);

    _log.debug("Received response : {} for create project", response.getStatus());
    return response;
  }

  private RestClient client;
  private static final Logger _log = LoggerFactory.getLogger(KeystoneProjectClient.class);
}
