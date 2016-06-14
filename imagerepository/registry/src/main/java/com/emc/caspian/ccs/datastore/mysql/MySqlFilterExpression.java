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
package com.emc.caspian.ccs.datastore.mysql;

import com.emc.caspian.ccs.datastore.FilterExpression;
import com.emc.caspian.ccs.datastore.expressiontree.Expression;

/**
 * Mysql implementation of FilterExpression.
 * @author shrids
 *
 */
public class MySqlFilterExpression extends FilterExpression {

    public MySqlFilterExpression(Expression exp) {
        super(exp);
    }

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.datastore.FilterExpression#evaluate()
     */
    @Override
    public String evaluate() {
        return super.toString();
    }
}
