package com.emc.caspian.ccs.account.server;

import java.net.URI;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;

import com.emc.caspian.ccs.account.util.AppLogger;

@Provider
@PreMatching
public class RedirectFilter implements ContainerRequestFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    UriInfo uriInfo = requestContext.getUriInfo();
    String versionInfo = "^/v[\\d]*.*";
    String path = uriInfo.getRequestUri().getRawPath();
    String query = uriInfo.getRequestUri().getRawQuery();
    if (!path.matches(versionInfo)) {
      String newPath = AccountServiceVersions.BACKWARD_API_VERSION_PATH + path;
      AppLogger.debug("Redirecting the request from path " + path + " to path " + newPath);
      URI newRequestURI = uriInfo.getBaseUriBuilder().path(newPath).replaceQuery(query).build();
      requestContext.setRequestUri(newRequestURI);
    }
  }
}
