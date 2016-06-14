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
 * Define a Token.
 *
 */
public abstract class Token {
    public static final int OPERATOR_TOKEN = 1;
    public static final int FUNCTION_TOKEN = 2;
    public static final int OPEN_BRACE_TOKEN = 3;
    public static final int CLOSE_BRACE_TOKEN = 4;

    protected int type;

    public Token(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}
