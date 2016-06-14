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
package com.emc.caspian.ccs.common.policyengine;

import com.emc.caspian.ccs.common.policyengine.parser.PolicyExpression;
import com.emc.caspian.ccs.common.policyengine.parser.PolicyTokenizer;
import com.emc.caspian.fabric.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * PolicyEngine is used to manage , evaluate the various policies defined for an open stack service.
 * This engine reads the configured policy configuration file and ensures that the Authfilter invokes the right rules
 * for a given policy definition.
 *
 * Note: No refresh policy is enabled in the current version.
 * @author shrids
 *
 */
public class PolicyEngine {

    private static final String SECTION = "service.policy";
    private static final String policyFileName = Configuration.make(String.class, SECTION + ".filename", "policy.json")
            .value();
    private static final String policyDirectory = Configuration.make(String.class, SECTION + ".directory",
            "conf").value();

    private final PolicyHolder policyHolder;

    public PolicyEngine() {
        //initialize the policy engine.
        Path policyFilePath = Paths.get(policyDirectory, policyFileName);
        policyHolder = new PolicyHolder(policyFilePath);
        _log.info("Policy Engine Initialized");
    }

    /**
     * Check if the given rule is valid for the given Servlet ContainerRequest.
     * @param policyName Name of the policy.
     * @param context The context against which the policy will be executed.
     * @return
     */
    public boolean isValid(final String policyName, final ContainerRequestContext context) {
        if (policyHolder == null) {
            _log.error("PolicyEngine is not initialized correctly. Check Error Logs");
            return false; // return invalid.
        }

        boolean result = false; //Invalid by default
        try {
            String rule = policyHolder.fetchExpandedRule(policyName);
            result = new PolicyExpression(new PolicyTokenizer(rule).parse()).evaluate(context);
        } catch (IllegalArgumentException exp) {
            _log.error("Error during evaluation of policyExpression", exp);
        }
        return result;
    }

    private static final Logger _log = LoggerFactory.getLogger(PolicyEngine.class);
}
