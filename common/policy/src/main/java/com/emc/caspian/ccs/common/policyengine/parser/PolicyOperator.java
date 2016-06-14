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
 *
 * Represent various operators in a policy rule. e.g: and, not, or. Each Operator has following
 * attributes: - symbol, - numberOfArgs, (Number of args is >=1) -precedence
 *
 * @author shrids
 *
 */
public abstract class PolicyOperator {

    private String symbol;
    private int precedence;
    private int numberOfArguments;

    public PolicyOperator(String symbol, int numberOfArgs, int precedence) {
        this.symbol = symbol;
        this.numberOfArguments = numberOfArgs;
        this.precedence = precedence;
    }

    public abstract boolean apply(Boolean... args);

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @return the precedence
     */
    public int getPrecedence() {
        return precedence;
    }

    /**
     * @return the numberOfArguments
     */
    public int getNumberOfArguments() {
        return numberOfArguments;
    }

}
