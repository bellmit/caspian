/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.esrs.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.esrs.api.AuthorizationPolicy.Rule;

public class AuthorizationDynamicFilter implements DynamicFeature {

    private static final Logger _log = LoggerFactory.getLogger(AuthorizationDynamicFilter.class);

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

	    if (requestContext.getSecurityContext() == null
		    || requestContext.getSecurityContext().getUserPrincipal() == null) {
		_log.error("User principal not found in the request.");
		requestContext	.abortWith(Response
			.status(Response.Status.INTERNAL_SERVER_ERROR)
			.entity(AuthorizationErrorMessage.AUTH_MISSING_AUTH_INFO)
			.type(MediaType.APPLICATION_JSON_TYPE)
			.build());
		return;
	    }

	    // check if any of the annotated rules match the request, if non
	    // matches then return forbidden
	    // trying out the least expensive authorization checks first
	    KeystonePrincipal p = (KeystonePrincipal) requestContext
		    .getSecurityContext().getUserPrincipal();

	    Iterator<Rule> iterator = rules.iterator();
	    while (iterator.hasNext()) {
		Rule ruleTemp = iterator.next();

		switch(ruleTemp) {

		case DENY_ALL:
		    _log.debug("DENY_ALL rule matched, forbid request");
		    requestContext.abortWith(Response
			    .status(Response.Status.FORBIDDEN)
			    .entity(AuthorizationErrorMessage.AUTH_ACCESS_DENIED)
			    .type(MediaType.APPLICATION_JSON_TYPE)
			    .build());
		    return;

		case ALLOW_ALL:
		    _log.debug("ALLOW_ALL rule matched, granting access");
		    return;

		case ALLOW_CLOUD_ADMIN:
		    if (p.getRoles() != null
		    && p.getRoles().contains(ADMIN_ROLE_NAME)
		    && p.getDomainId() != null
		    && p.getDomainId().equalsIgnoreCase(DEFAULT_DOMAIN_ID)
		    && p.getProjectId() == null
		    && p.getProjectName() == null) {
			_log.debug("User identified as cloud admin, granting access");
			return;
		    }	
		    break;

		case ALLOW_CLOUD_MONITOR:
		    if (p.getRoles() != null
		    && p.getRoles().contains(MONITOR_ROLE_NAME)) {
			_log.debug("User identified as cloud monitor, granting access");
			return;
		    }
		    break;

		case ALLOW_CLOUD_SERVICE:
		    if ( p.getRoles() != null 
		    && p.getRoles().contains(SERVICE_ROLE_NAME) 
		    && p.getDomainId() != null
		    && p.getDomainId().equalsIgnoreCase(DEFAULT_DOMAIN_ID) 
		    && p.getProjectId() == null
		    && p.getProjectName() == null) {
			_log.debug("User identified as cloud service, granting access");
			return;
		    }
		    break;
		    
		default:
		    //check for next rule. Final default case is included after while loop.
		    break;
		}
	    }
	    
	    // No rules matched, deny access
	    _log.warn("Failed to authorize user. User principal: {}",p.toString());
	    requestContext
	    .abortWith(Response
		    .status(Response.Status.FORBIDDEN)
		    .entity(AuthorizationErrorMessage.AUTH_INSUFFICIENT_PRIVILEGE)
		    .type(MediaType.APPLICATION_JSON_TYPE)
		    .build());
	}

	private static final String ADMIN_ROLE_NAME = "admin";
	private static final String MONITOR_ROLE_NAME = "monitor";
	private static final String DEFAULT_DOMAIN_ID = "default";
	private static final String SERVICE_ROLE_NAME = "service";

    }
}
