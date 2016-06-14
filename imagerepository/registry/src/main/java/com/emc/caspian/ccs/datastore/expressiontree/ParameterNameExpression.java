package com.emc.caspian.ccs.datastore.expressiontree;

/**
 * Created by shivesh on 2/26/15.
 */
public class ParameterNameExpression extends Expression
{
    private final String paramName;

    public ParameterNameExpression(String paramName) {
        this.paramName = paramName;
    }

    @Override
    public String toString() {
        return this.paramName;
    }
}
