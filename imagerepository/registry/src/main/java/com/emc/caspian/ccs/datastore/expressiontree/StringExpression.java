package com.emc.caspian.ccs.datastore.expressiontree;

public class StringExpression extends Expression
{
    private final String constant;

    public StringExpression(String constant) {
        this.constant = constant;
    }

    @Override
    public String toString() {
        return "'\"" + this.constant + "\"'";
    }
}
