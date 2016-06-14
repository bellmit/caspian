package com.emc.caspian.ccs.license;

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

import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.slf4j.MDC;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.license.AuthorizationPolicy.Rule;
import com.emc.caspian.ccs.license.util.AppLogger;

public class AuthorizationDynamicFilter implements DynamicFeature {

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		AnnotatedMethod am = new AnnotatedMethod(
				resourceInfo.getResourceMethod());

		if (am.isAnnotationPresent(AuthorizationPolicy.class)) {
			AuthorizationPolicy rules = am
					.getAnnotation(AuthorizationPolicy.class);
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
		public void filter(ContainerRequestContext requestContext)
				throws IOException {
			
			AppLogger.info("AUDIT: RequestID:%s, Method called:%s, URI:%s ", MDC.get("REQUEST_ID"),
                    requestContext.getMethod(),requestContext.getUriInfo().getRequestUri() );
			
			if (requestContext.getSecurityContext() == null
					|| requestContext.getSecurityContext().getUserPrincipal() == null) {
				AppLogger
						.error("User principal not found in the request, auth filter probably missing");
				ErrorPayload error= new ErrorPayload(
						AuthorizationErrorMessage.AUTH_MISSING_AUTH_INFO_CODE,AuthorizationErrorMessage.AUTH_MISSING_AUTH_INFO_MESSAGE);
				requestContext
						.abortWith(Response
								.status(Response.Status.INTERNAL_SERVER_ERROR)
								.entity(error).build());
				return;
			}

			// check if any of the annotated rules match the request, if non
			// matches then return forbidden
			// trying out the least expensive authorization checks first

			if (rules.contains(Rule.DENY_ALL)) {
				 String message = "DENY_ALL rule matched, forbid request";
                 AppLogger.debug(message);
                 AppLogger.info("AUDIT: RequestID:%s, Authorization action:%s",MDC.get("REQUEST_ID"),message);

				ErrorPayload error= new ErrorPayload(
						AuthorizationErrorMessage.AUTH_ACCESS_DENIED_CODE,AuthorizationErrorMessage.AUTH_ACCESS_DENIED_MESSAGE);
				requestContext.abortWith(Response
						.status(Response.Status.FORBIDDEN)
						.entity(error).build());
				return;
			}

			if (rules.contains(Rule.ALLOW_ALL)) {
				String message = "ALLOW_ALL rule matched, granting access";
                AppLogger.debug(message);
                KeystonePrincipal p = (KeystonePrincipal) requestContext
                                .getSecurityContext().getUserPrincipal();
                AppLogger.info("AUDIT: RequestID:%s, UUID:%s, Authorization action:%s",
                                p.getUserId(),MDC.get("REQUEST_ID"),message);

				return;
			}

			KeystonePrincipal p = (KeystonePrincipal) requestContext
					.getSecurityContext().getUserPrincipal();
			if (rules.contains(Rule.ALLOW_CLOUD_ADMIN)) {
				if (p.getRoles() != null
						&& p.getRoles().contains(ADMIN_ROLE_NAME)
						&& p.getDomainId() != null
						&& p.getDomainId().equalsIgnoreCase(DEFAULT_DOMAIN_ID)
						&& p.getProjectId() == null
						&& p.getProjectName() == null) {
					 String message="User identified as cloud admin, granting access";
                     AppLogger.debug(message);
                     AppLogger.info("AUDIT: RequestID:%s, UUID:%s, Authorization action:%s",
                                     p.getUserId(),MDC.get("REQUEST_ID"),message);

					return;
				}
			}

			if (rules.contains(Rule.ALLOW_CLOUD_MONITOR)) {
				if (p.getRoles() != null
						&& p.getRoles().contains(MONITOR_ROLE_NAME)) {
					String message = "User identified as cloud monitor, granting access";
                    AppLogger.debug(message);
                    AppLogger.info("AUDIT: RequestID:%s, UUID:%s, Authorization action:%s",
                                    p.getUserId(),MDC.get("REQUEST_ID"),message);

					return;
				}
			}

			// No rules matched, deny access
			 String message = "Failed to authorize user. User principal: "+ p.toString();
             AppLogger.warn(message);
             AppLogger.info("AUDIT: RequestID:%s, UUID:%s, Authorization action:%s",
                             p.getUserId(),MDC.get("REQUEST_ID"),message);

			ErrorPayload error= new ErrorPayload(
					AuthorizationErrorMessage.AUTH_INSUFFICIENT_PRIVILEGE_CODE,AuthorizationErrorMessage.AUTH_INSUFFICIENT_PRIVILEGE_MESSAGE);
			requestContext
					.abortWith(Response
							.status(Response.Status.FORBIDDEN)
							.entity(error).build());
		}

		private static final String ADMIN_ROLE_NAME = "admin";
		private static final String MONITOR_ROLE_NAME = "monitor";
		private static final String DEFAULT_DOMAIN_ID = "default";

	}
}
