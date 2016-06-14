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
package com.emc.caspian.ccs.datastore;

import com.emc.caspian.ccs.datastore.expressiontree.Expression;

/**
 * This is used to capture the simple filters that will be supported in glance.
 * E.g: filters include  (key1 < 10) AND (key2 == 'OS') OR (key3 = 'OVF')
 * Complex expressions are not supported in this version.
 *
 * @author shrids
 *
 */
public abstract class FilterExpression {

    private final Expression expression;

    /**
     * Constructor for
     * @param expression
     */
    public FilterExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * This function is used to return the FilterQuery for a given implementation
     * @return
     */
    public abstract String evaluate();

    @Override
    public String toString() {
        return expression.toString();
    }
}
