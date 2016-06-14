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
package com.emc.caspian.ccs.common.policyengine.parser;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.fabric.util.Validate;

/**
 * This class contains helper functions required to fetch/create new policyFunctions.
 *
 * @author shrids
 *
 */
public abstract class Functions {

    public static final String POLICY_FUNCTION_ALL = "all";

    public static final List<String> ALLOWED_FUNCTIONS = Arrays.asList("role", POLICY_FUNCTION_ALL, "project_id");

    private static final PolicyFunction[] functions = new PolicyFunction[3];

    static {
        functions[1] = new PolicyFunction(POLICY_FUNCTION_ALL) {
            // function to allow all
            @Override
            public boolean invoke(ContainerRequestContext context) {
                return true;
            }

        };
    }

    /**
     * Returns a policy function based on the function Name.
     *
     * @param function
     * @return
     */
    public static PolicyFunction getFunction(String function) {
        String functionName = function.split(":")[0];
        String[] functionArgs = getArguments(function);

        switch (functionName) {
        case "role":
            return new PolicyFunction(functionName, functionArgs) {

                @Override
                public boolean invoke(ContainerRequestContext context) {
                    String[] args = getArguments();
                    if ((args == null) || args.length == 0) {
                        _log.error("Arguments to role: function is empty, returning false");
                        return false;
                    }

                    _log.debug("Role: Function invoked with arguments: {}", Arrays.asList(args));
                    KeystonePrincipal principal = (KeystonePrincipal) context.getSecurityContext().getUserPrincipal();
                    if ((principal != null)) {
                        _log.debug("Recieved the following keystonePrincipal {} ", principal.toString());
                        if ((principal.getRoles().contains(args[0])))
                            return true;
                    }
                    return false;
                }
            };

        case POLICY_FUNCTION_ALL:
            return functions[1];

        case "project_id":
            return new PolicyFunction(functionName, functionArgs) {

                @Override
                public boolean invoke(ContainerRequestContext context) {
                    _log.debug("project_id: function invoked with arguments: {}", Arrays.asList(getArguments()));
                    // TODO: add implementation of project_id:%(project_id)s function.
                    return true;
                }
            };
        default:
            throw new IllegalArgumentException("Invalid Function name, not supported. Name: " + function);

        }
    }

    public static boolean isAllowedFunction(String functionName) {
        Validate.isNotNullOrEmpty(functionName, "FunctionName");
        return ALLOWED_FUNCTIONS.contains(functionName.split(":")[0]);
    }

    private static String[] getArguments(String function) {
        String[] split = function.split(":");
        if (split.length == 1) {
            return new String[0]; // no arguments
        } else {
            return Arrays.copyOfRange(split, 1, split.length);
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(Functions.class);
}
