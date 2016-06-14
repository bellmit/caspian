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
 * Token of type Operator.
 *
 */
public class OperatorToken extends Token {

    private PolicyOperator operator;

    OperatorToken(PolicyOperator op) {
        super(Token.OPERATOR_TOKEN);
        this.operator = op;
    }

    PolicyOperator getOperation() {
        return operator;
    }

}
