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
 * A collection of built-in priority constants for operators used in the policy definition. The
 * operators include and, not, or
 *
 * @author shrids
 *
 */
public class OperatorPrecedence {

    private OperatorPrecedence() {
        // prevents construction
    }

    public static final int PRECEDENCE_AND = 500;

    public static final int PRECEDENCE_OR = PRECEDENCE_AND;

    public static final int PRECEDENCE_NOT = 1000; // Unary operator has higher precedence
}
