package com.emc.caspian.ccs.datastore.expressiontree;

/**
 * Created by shivesh on 2/25/15.
 */
public abstract class Expression
{
    /**
     * This has a list of operations supported by the filter.
     *
     */
    public enum Operation {
        NOP(""), AND(" AND "), OR(" OR "), NOT(" NOT "), LESSTHAN(" < "), GREATERTHAN(" > "), EQUALS(" = "),
        NOTEQUALS(" <> ");

        private Operation(String s) {
            this.value = s;
        }

        public String getValue() {
            return value;
        }

        private String value;
    }
}
