/**
 *  Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.common.policyengine.rest;

import com.emc.caspian.ccs.common.policyengine.policy.Policy;
import com.emc.caspian.ccs.common.policyengine.PolicyEngine;
import org.glassfish.jersey.server.model.AnnotatedMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import java.io.IOException;
import java.util.Arrays;

/**
 * This Dynamic Feature is used to handle Policy related management.
 * E.g usage: @Policy("add_image")
 * @Policy({"get_image_location", "get_image"})
 *
 * @author shrids
 *
 */
public class PolicyDynamicFeature implements DynamicFeature {

    private static final PolicyEngine policyEngine = new PolicyEngine();

    /*
     * (non-Javadoc)
     *
     * @see javax.ws.rs.container.DynamicFeature#configure(javax.ws.rs.container.ResourceInfo,
     * javax.ws.rs.core.FeatureContext)
     */
    @Override
    public void configure(ResourceInfo resourceInfo, FeatureContext context) {
        AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        if (am.isAnnotationPresent(Policy.class)) {
            // No @Policy annotation imples not policy filter is registered i.e. no Authorization.
            Policy policies = am.getAnnotation(Policy.class);
            context.register(new PolicyAllowedRequestFilter(policies.value()));
        }
    }

    @Priority(Priorities.AUTHORIZATION) //Ensure filter is invoked after Authentication
    private static class PolicyAllowedRequestFilter implements ContainerRequestFilter {

        private final String[] policies;

        PolicyAllowedRequestFilter(String[] policies) {
            this.policies = (policies != null) ? policies : new String[] {};
        }

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            _log.info(" Policy Filter invoked for the following polices: {} ", Arrays.toString(policies));
            for (final String policy : policies) {
                if (!policyEngine.isValid(policy, requestContext)) {
                    _log.error("Policy Validation Failed for Policy: {} ", policy);
                    throw new ForbiddenException("Policy validation failed for " + policy);
                }
            }
            return;
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(PolicyDynamicFeature.class);
}
