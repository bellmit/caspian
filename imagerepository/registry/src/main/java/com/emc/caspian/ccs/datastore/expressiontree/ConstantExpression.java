package com.emc.caspian.ccs.datastore.expressiontree;

/**
 * Created by shivesh on 2/25/15.
 */
public class ConstantExpression extends Expression
{
    private final String constant;

    public ConstantExpression(String constant) {
        this.constant = constant;
    }

    @Override
    public String toString() {
        return "'" + this.constant + "'";
    }
}
