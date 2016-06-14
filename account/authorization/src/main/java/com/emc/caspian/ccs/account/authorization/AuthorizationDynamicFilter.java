package com.emc.caspian.ccs.account.authorization;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.account.authorization.AuthorizationPolicy.Rule;
import com.emc.caspian.ccs.account.model.AccountModel;
import com.emc.caspian.ccs.account.model.DbResponse;
import com.emc.caspian.ccs.account.model.ErrorCode;
import com.emc.caspian.ccs.account.model.TableFactory;
import com.emc.caspian.ccs.account.util.AppLogger;


public class AuthorizationDynamicFilter implements DynamicFeature {


  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

    if (am.isAnnotationPresent(AuthorizationPolicy.class)) {
      AuthorizationPolicy rules = am.getAnnotation(AuthorizationPolicy.class);
      context.register(new AuthorizationFilter(rules.value()));
    }
  }

  @Priority(Priorities.AUTHORIZATION)
  public class AuthorizationFilter implements ContainerRequestFilter {

    private final List<Rule> rules;

    public AuthorizationFilter(Rule[] rules) {
      this.rules = Arrays.asList(rules);
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
      if (requestContext.getSecurityContext() == null || requestContext.getSecurityContext().getUserPrincipal() == null) {
        AppLogger.error("User principal not found in the request, auth filter probably missing");
        requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(AuthorizationErrorMessage.AUTH_MISSING_AUTH_INFO).build());
        return;
      }

      // check if any of the annotated rules match the request, if non matches then return forbidden
      // trying out the least expensive authorization checks first

      if (rules.contains(Rule.DENY_ALL)) {
        AppLogger.debug("DENY_ALL rule matched, forbid request");
        requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
            .entity(AuthorizationErrorMessage.AUTH_ACCESS_DENIED).build());
        return;
      }

      if (rules.contains(Rule.ALLOW_ALL)) {
        AppLogger.debug("ALLOW_ALL rule matched, granting access");
        return;
      }

      if (rules.contains(Rule.DISALLOW_EDIT_DEFAULT_ACCOUNT)) {
        if(requestContext.getUriInfo().getPathParameters().getFirst("accountId").equals(DEFAULT_ACCOUNT_ID)) {
          requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
              .entity(AuthorizationErrorMessage.AUTH_ACCESS_DENIED).build());
          return;
        }
      }

      KeystonePrincipal p = (KeystonePrincipal) requestContext.getSecurityContext().getUserPrincipal();
      if (rules.contains(Rule.ALLOW_CLOUD_ADMIN)) {
        if (p.getRoles() != null && p.getRoles().contains(ADMIN_ROLE_NAME) && p.getDomainId() != null
            && p.getDomainId().equalsIgnoreCase(DEFAULT_DOMAIN_ID) && p.getProjectId() == null
            && p.getProjectName() == null) {
          // audit log
          AppLogger.info("User " + p.getUserId() + " identified as cloud admin, granting access to "
              + requestContext.getMethod() + " method on " + requestContext.getUriInfo().getRequestUri().getRawPath());
          return;
        }
      }

      if (rules.contains(Rule.ALLOW_CLOUD_MONITOR)) {
        if (p.getRoles() != null && p.getRoles().contains(MONITOR_ROLE_NAME) && p.getDomainId() != null
            && p.getDomainId().equalsIgnoreCase(DEFAULT_DOMAIN_ID) && p.getProjectId() == null
            && p.getProjectName() == null) {
          // audit log
          AppLogger.info("User " + p.getUserId() + " identified as cloud monitor, granting access to "
              + requestContext.getMethod() + " method on " + requestContext.getUriInfo().getRequestUri().getRawPath());
          return;
        }
      }

      if (rules.contains(Rule.ALLOW_CLOUD_SERVICE)) {
        if (p.getRoles() != null && p.getRoles().contains(SERVICE_ROLE_NAME) && p.getDomainId() != null
            && p.getDomainId().equalsIgnoreCase(DEFAULT_DOMAIN_ID) && p.getProjectId() == null
            && p.getProjectName() == null) {
          // audit log
          AppLogger.info("User " + p.getUserId() + " identified as cloud service, granting access to "
              + requestContext.getMethod() + " method on " + requestContext.getUriInfo().getRequestUri().getRawPath());
          return;
        }
      }

      // For domain scoped user, the domain id will be set and for project scoped token, project id will be set or
      // projectDomainId and project name will be set
      // This check validates both domain-scoped tokens as well as project-scoped tokens
      if (rules.contains(Rule.ALLOW_SCOPED_USER)) {
        if (p.getDomainId() != null || p.getProjectDomainId() != null || p.getProjectId() != null) {

          String domainId = requestContext.getUriInfo().getQueryParameters().getFirst(QUERY_PARAMS_DOMAIN_ID);
          if (StringUtils.isEmpty(domainId)) {
            // audit log
            AppLogger
                .info("User " + p.getUserId() + " identified as domain scoped, granting access to "
                    + requestContext.getMethod() + " method on "
                    + requestContext.getUriInfo().getRequestUri().getRawPath());
            return;
          }
        }
      }

      if (rules.contains(Rule.ALLOW_ACCOUNT_ADMIN)) {

        // Fetch account Id from the request path
        String targetAccountId = getTargetAccountId(requestContext, p);
        if (checkAccountRole(ADMIN_ROLE_NAME, targetAccountId, requestContext, p)) {
          return;
        }
      }

      if (rules.contains(Rule.ALLOW_ACCOUNT_MONITOR)) {

        // Fetch account Id from the request path
        String targetAccountId = requestContext.getUriInfo().getPathParameters().getFirst("accountId");
        if (checkAccountRole(MONITOR_ROLE_NAME, targetAccountId, requestContext, p)) {
          return;
        }
      }

      if (rules.contains(Rule.ALLOW_DOMAIN_ADMIN)) {
        String targetDomainId = requestContext.getUriInfo().getPathParameters().getFirst(PATH_PARAMS_DOMAIN_ID);
        if (p.getRoles() != null && p.getRoles().contains(ADMIN_ROLE_NAME) && p.getDomainId() != null
            && p.getDomainId().equalsIgnoreCase(targetDomainId) && p.getProjectId() == null
            && p.getProjectName() == null) {
          // audit log
          AppLogger.info("User " + p.getUserId() + " identified as domain admin, granting access to "
              + requestContext.getMethod() + " method on " + requestContext.getUriInfo().getRequestUri().getRawPath());
          return;
        }
      }

      if (rules.contains(Rule.ALLOW_DOMAIN_MONITOR)) {
        String targetDomainId = requestContext.getUriInfo().getPathParameters().getFirst(PATH_PARAMS_DOMAIN_ID);
        if (p.getRoles() != null && p.getRoles().contains(MONITOR_ROLE_NAME) && p.getDomainId() != null
            && p.getDomainId().equalsIgnoreCase(targetDomainId) && p.getProjectId() == null
            && p.getProjectName() == null) {
          // audit log
          AppLogger.info("User " + p.getUserId() + " identified as domain monitor, granting monitor access to "
              + requestContext.getMethod() + " method on " + requestContext.getUriInfo().getRequestUri().getRawPath());
          return;
        }
      }

      if (rules.contains(Rule.ALLOW_ACCOUNT_ADMIN_IDP)) {
        if (checkAccountIdpRole(ADMIN_ROLE_NAME, requestContext, p)) {
          return;
        }
      }

      if (rules.contains(Rule.ALLOW_ACCOUNT_MONITOR_IDP)) {
        if (checkAccountIdpRole(MONITOR_ROLE_NAME, requestContext, p)) {
          return;
        }
      }

      // No rules matched, deny access
      // audit log
      AppLogger.warn("User " + p.getUserId() + " tried to access " + requestContext.getMethod() + " method on "
          + requestContext.getUriInfo().getRequestUri().getRawPath() + ". Failed to authorize the user.");
      requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
          .entity(AuthorizationErrorMessage.AUTH_INSUFFICIENT_PRIVILEGE).build());
    }

    private String getTargetAccountId(ContainerRequestContext requestContext, KeystonePrincipal p) {
      String accountId = requestContext.getUriInfo().getPathParameters().getFirst("accountId");
      if (StringUtils.isEmpty(accountId)) {
        String domainId = requestContext.getUriInfo().getQueryParameters().getFirst(QUERY_PARAMS_DOMAIN_ID);
        if(StringUtils.isEmpty(domainId)){
          // try getting the account/domain ID from token
          domainId = p.getDomainId();
        }
        if (StringUtils.isNotEmpty(domainId)) {
          DbResponse<AccountModel> response = TableFactory.getAccountTable().getAccount(domainId);

          AccountModel accountModel = response.getResponseObj();
          if (accountModel != null) {
            AppLogger.debug("Account details from database, domainId: " + accountModel.getId() + ". accountState: "
                + accountModel.getState());

            accountId = accountModel.getId();
          } else {
            // If this is server error then return 500
            if (response.getErrorCode() == ErrorCode.DB_INTERNAL_ERROR) {
              AppLogger.warn("Encountered an internal error while fetching details from the db");
              requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                  .entity(AuthorizationErrorMessage.AUTH_INTERNAL_ERROR).build());
            }
          }
        }
      }
      return accountId;
    }

    private boolean checkAccountIdpRole(String roleName, ContainerRequestContext requestContext, KeystonePrincipal p) {

      if (p.getRoles() != null && p.getRoles().contains(roleName) && p.getDomainId() != null
          && !p.getDomainId().isEmpty() && p.getProjectId() == null && p.getProjectName() == null) {
        String targetAccountId = null;

        // If the IDP request contains domain-id
        if (requestContext.getUriInfo().getPathParameters().containsKey(PARAMS_DOMAIN_ID)) {
          // Get accountId using domain-id
          String domainId = requestContext.getUriInfo().getPathParameters().getFirst(PARAMS_DOMAIN_ID);
          DbResponse<AccountModel> response = TableFactory.getAccountTable().getAccount(domainId);

          if (response.getResponseObj() != null) {
            targetAccountId = response.getResponseObj().getId();
          }
          // If the domain is not mapped to any account
          else if (response.getErrorCode() == ErrorCode.DB_RECORD_NOT_FOUND) {
            AppLogger.warn("No associated account found for the domain");
            requestContext.abortWith(Response.status(Response.Status.NOT_FOUND)
                .entity(String.format(IdpErrorMessages.DOMAIN_NOT_MAPPED_TO_ANY_ACCOUNT, domainId)).build());
            return true;
          } else {
            AppLogger.warn("Encountered an internal error while fetching details from the db");
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(AuthorizationErrorMessage.AUTH_INTERNAL_ERROR).build());
            return true;
          }
        }
        if (checkAccountRole(roleName, targetAccountId, requestContext, p)) {
          return true;
        }
      }
      return false;
    }

    private boolean checkAccountRole(String roleName, String targetAccountId, ContainerRequestContext requestContext,
        KeystonePrincipal p) {
      // First check if we have the appropriate domain scoped role
      if (p.getRoles() != null && p.getRoles().contains(roleName) && p.getDomainId() != null
          && !p.getDomainId().isEmpty() && p.getProjectId() == null && p.getProjectName() == null) {

        // Check for account role
        if (targetAccountId == null || targetAccountId.isEmpty()) {
          // if the account ID is null then try to fetch it from token
          targetAccountId = p.getDomainId();
          if (targetAccountId == null || targetAccountId.isEmpty()) {
            // Cannot determine the account Id from the request, return client error
            AppLogger.warn("Account Id not supplied in the request");
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST)
                .entity(AuthorizationErrorMessage.AUTH_ACCOUNTID_MISSING).build());
            return true;
          }
        }

        AppLogger.debug("Fetched target Account Id: " + targetAccountId);

        DbResponse<AccountModel> resp = TableFactory.getAccountTable().getAccount(p.getDomainId());
        AccountModel accountModel = resp.getResponseObj();
        if (accountModel != null) {
          AppLogger.debug("Account details from database, domainId: " + accountModel.getId() + ". accountState: "
              + accountModel.getState());

          // Verify if the user has the role in the primary domain of the account
          if (accountModel.getId().equals(targetAccountId)) {
            // audit log
            AppLogger
                .info("User " + p.getUserId() + " identified as account " + roleName + ", granting access to "
                    + requestContext.getMethod() + " method on "
                    + requestContext.getUriInfo().getRequestUri().getRawPath());
            return true;
          }
        } else {
          // If this is server error then return 500
          if (resp.getErrorCode() == ErrorCode.DB_INTERNAL_ERROR) {
            AppLogger.warn("Encountered an internal error while fetching details from the db");
            requestContext.abortWith(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(AuthorizationErrorMessage.AUTH_INTERNAL_ERROR).build());
            return true;
          }
        }
      }
      return false;
    }

    private static final String ADMIN_ROLE_NAME = "admin";
    private static final String MONITOR_ROLE_NAME = "monitor";
    private static final String SERVICE_ROLE_NAME = "service";
    private static final String DEFAULT_DOMAIN_ID = "default";
    private static final String DEFAULT_ACCOUNT_ID = "default";
    private final static String PARAMS_DOMAIN_ID = "domain-id";
    private final static String QUERY_PARAMS_DOMAIN_ID = "domain_id";
    private final static String PATH_PARAMS_DOMAIN_ID = "domain-id";
  }
}
