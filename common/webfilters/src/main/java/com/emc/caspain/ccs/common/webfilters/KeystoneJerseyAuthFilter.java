package com.emc.caspain.ccs.common.webfilters;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.keystone.middleware.Middleware;
import com.emc.caspain.ccs.keystone.middleware.exceptions.InvalidTokenException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.MiddlewareException;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.ccs.keystone.model.Token.Role;
import com.google.common.base.Strings;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class KeystoneJerseyAuthFilter implements ContainerRequestFilter {
  private static final Logger _log = LoggerFactory.getLogger(KeystoneJerseyAuthFilter.class);

  private Middleware authMiddleware;

  public KeystoneJerseyAuthFilter(String authURI, String username, String password) {
    authMiddleware = new Middleware(authURI, username, password);
    authMiddleware.start();
  }

  public KeystoneJerseyAuthFilter() {
    authMiddleware = null;
  }
  
  public void resetConfig(String authURI, String username, String password) {
    
    // Access to authMiddleware should typically be serialized. But here we are assuming that this API call is made only 
    // if default constructor was initially used to create the auth filter and hence avoiding adding synchronized blocks for 
    // efficiency.
    // Due to this, this API should only be called in exceptional cases.
    if (authMiddleware != null) {
      authMiddleware.stop();
    }
    authMiddleware = new Middleware(authURI, username, password);
    authMiddleware.start();
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    final String authToken = requestContext.getHeaderString("X-Auth-Token");
    KeystonePrincipal ksPrincipal = null;

    if (Strings.isNullOrEmpty(authToken)) {
      _log.debug("No auth token supplied in request, using EMPTY principal");
      ksPrincipal = KeystonePrincipal.EMPTY;
    } else {
      if (authMiddleware == null) {
        _log.error("Keystone middleware not yet initialized");
        requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to validate token").build());
        return;          
      }
      Token tokenInfo = null;
      try {
        tokenInfo = authMiddleware.getToken(authToken);
      } catch (MiddlewareException e) {
        if (e instanceof InvalidTokenException) {
          _log.warn("Auth token invalid, returning 401 to client");
          requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid auth token").build());
          return;        
        } else {
          _log.warn("Failed to validate token due to internal error");
          requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to validate token").build());
          return;                  
        }
      }
      
      if (tokenInfo != null) {
        _log.debug("Valid auth token found for user");
        String domainId = null;
        String domainName = null;
        if (tokenInfo.getDomain() != null) {
          domainId = tokenInfo.getDomain().getId();
          domainName = tokenInfo.getDomain().getName();
        }

        String projectId = null;
        String projectName = null;
        String projectDomainId = null;
        String projectDomainName = null;
        if (tokenInfo.getProject() != null) {
          projectId = tokenInfo.getProject().getId();
          projectName = tokenInfo.getProject().getName();
          if (tokenInfo.getProject().getDomain() != null) {
            projectDomainId = tokenInfo.getProject().getDomain().getId();
            projectDomainName = tokenInfo.getProject().getDomain().getName();
          }
        }

        String userId = null;
        String userName = null;
        String userDomainId = null;
        String userDomainName = null;
        if (tokenInfo.getUser() != null) {
          userId = tokenInfo.getUser().getId();
          userName = tokenInfo.getUser().getName();
          if (tokenInfo.getUser().getDomain() != null) {
            userDomainId = tokenInfo.getUser().getDomain().getId();
            userDomainName = tokenInfo.getUser().getDomain().getName();
          }
        }

        List<String> roles = null;
        if (tokenInfo.getRoles() != null) {
          roles = new ArrayList<String>();
          for (Role role : tokenInfo.getRoles()) {
            roles.add(role.getName());
          }
        }

        ksPrincipal =
            new KeystonePrincipal(authToken, domainId, domainName, projectId, projectName, projectDomainId,
                projectDomainName, userId, userName, userDomainId, userDomainName, roles);
        _log.debug("Using KeystonePrincipal: " + ksPrincipal.toString());
      }
    }

    final KeystonePrincipal principal = ksPrincipal;
    if (principal != null) {
      _log.debug("Updating security context with info from auth token");
      requestContext.setSecurityContext(new SecurityContext() {
        @Override
        public Principal getUserPrincipal() {
          return principal;
        }

        @Override
        public boolean isUserInRole(String role) {
          if (principal.getRoles() != null)
            return principal.getRoles().contains(role);
          return false;
        }

        @Override
        public boolean isSecure() {
          return false;
        }

        @Override
        public String getAuthenticationScheme() {
          return "token";
        }
      });
    } else {
      _log.warn("Auth token invalid, returning 401 to client");
      requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid auth token").build());
      return;
    }
  }
}
