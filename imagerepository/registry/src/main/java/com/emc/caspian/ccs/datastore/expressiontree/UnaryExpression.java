package com.emc.caspian.ccs.datastore.expressiontree;

/**
 * Created by shivesh on 2/25/15.
 */
public class UnaryExpression extends Expression
{
    private final BinaryExpression expr;

    public UnaryExpression(final BinaryExpression expr) {
        this.expr = expr;
    }

    public static UnaryExpression Not(BinaryExpression expr){
        return new UnaryExpression(expr);
    }

    @Override
    public String toString() {
        return new StringBuilder().
                append(Operation.NOT.getValue()).
                append(" ( ").append(expr.toString()).
                append(" )").toString();
    }
}
