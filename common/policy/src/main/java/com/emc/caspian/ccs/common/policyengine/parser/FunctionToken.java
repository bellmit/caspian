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

/**
 * Token of Type Function.
 *
 */
public class FunctionToken extends Token {

    private PolicyFunction func;

    FunctionToken(PolicyFunction f) {
        super(FUNCTION_TOKEN);
        this.func = f;
    }

    public PolicyFunction getFunction() {
        return func;
    }

}
