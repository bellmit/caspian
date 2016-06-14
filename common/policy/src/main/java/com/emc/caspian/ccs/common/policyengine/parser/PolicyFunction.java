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

import javax.ws.rs.container.ContainerRequestContext;

/**
 * Represent a function defined in the policy.json. e.g: Rule in policy.json
 * "not role:accounting and not role:operator" Here role is a policyfunction.
 *
 * @author shrids
 *
 */
public abstract class PolicyFunction {
    private String symbol;

    private String[] arguments;

    public PolicyFunction(String symbol) {
        this.symbol = symbol;
    }

    public PolicyFunction(String symbol, String... args) {
        this(symbol);
        this.arguments = args;
    }

    public void setArguments(String... args) {
        this.arguments = args;
    }

    public String[] getArguments() {
        return this.arguments;
    }

    public String getSymbol() {
        return symbol;
    }

    public abstract boolean invoke(ContainerRequestContext context);
}
