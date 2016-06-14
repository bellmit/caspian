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
package com.emc.caspian.ccs.datastore.expressiontree;

/**
 * Class used to represent an Expression.
 *
 */
public class BinaryExpression extends Expression
{
    private Expression left;
    private Expression right;
    private Operation operation;

    private BinaryExpression(Expression lhs, Operation operation, Expression rhs) {
        this.left = lhs;
        this.operation = operation;
        this.right = rhs;
    }

    public static BinaryExpression and(BinaryExpression left, BinaryExpression right){
        return new BinaryExpression(left, Operation.AND, right);
    }

    public static BinaryExpression or(BinaryExpression left, BinaryExpression right){
        return new BinaryExpression(left, Operation.OR, right);
    }

    public static BinaryExpression greaterThan(Expression left, Expression right){
        return new BinaryExpression(left, Operation.GREATERTHAN, right);
    }

    public static BinaryExpression lessThan(Expression left, Expression right){
        return new BinaryExpression(left, Expression.Operation.LESSTHAN, right);
    }

    public static BinaryExpression equalTo(Expression left, Expression right){
        return new BinaryExpression(left, Expression.Operation.EQUALS, right);
    }

    public static BinaryExpression notEquals(Expression left, Expression right){
        return new BinaryExpression(left, Expression.Operation.NOTEQUALS, right);
    }

    @Override
    public String toString() {
        // walk the tree in order and print the string
        return new StringBuilder().
                append("( ").
                append(this.left.toString()).
                append(" ").
                append(this.operation.getValue()).
                append(" ").
                append(this.right.toString()).
                append(" )").toString();
    }
}