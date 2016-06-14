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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyExpression {

    private static final int STACK_SIZE_INIT = 5;

    private final List<Token> tokens;

    public PolicyExpression(List<Token> tokenList) {
        this.tokens = tokenList;
    }

    public boolean evaluate(ContainerRequestContext context) {
        Deque<Boolean> valueStack = new ArrayDeque<Boolean>(STACK_SIZE_INIT);
        Deque<Token> opStack = new ArrayDeque<Token>(STACK_SIZE_INIT);

        for (Token token : tokens) {
            switch (token.getType()) {

            case Token.FUNCTION_TOKEN:
                valueStack.push(((FunctionToken) token).getFunction().invoke(context));
                break;

            case Token.OPEN_BRACE_TOKEN:
                opStack.push(token);
                break;

            case Token.CLOSE_BRACE_TOKEN:
                while (!opStack.isEmpty() && opStack.peek().getType() != Token.OPEN_BRACE_TOKEN) {
                    PolicyOperator operation = ((OperatorToken) opStack.pop()).getOperation();
                    boolean tempValue = operation.apply(fetchArguments(valueStack, valueStack.pop(), operation));
                    valueStack.push(tempValue);
                }
                ;
                if (opStack.pop().getType() != Token.OPEN_BRACE_TOKEN) {
                    _log.error("Mismatch in parenthesis");
                    throw new IllegalArgumentException("Malformed expression in the policy rule, check for parenthesis");
                }
                break;

            case Token.OPERATOR_TOKEN:
                OperatorToken tok = (OperatorToken) token;
                while (!opStack.isEmpty() && hasPrecedence(opStack.peek(), tok)) {
                    PolicyOperator operation = ((OperatorToken) opStack.pop()).getOperation();
                    boolean tempValue = operation.apply(fetchArguments(valueStack, valueStack.pop(), operation));
                    valueStack.push(tempValue);
                }
                opStack.push(token); // push it into the opStack
                break;

            default:
                // Invalid token type, we should not be here.
                throw new IllegalArgumentException("Invalid Token type: " + token.getType());
            }
        }

        boolean tempValue = valueStack.pop();
        while (!opStack.isEmpty()) {

            Token token = opStack.pop();
            if (token.getType() != Token.OPERATOR_TOKEN) {
                _log.error("Malformed expression in the policy rule, check for parenthesis");
                throw new IllegalArgumentException("Malformed expression in the policy rule, check for parenthesis");
            }
            PolicyOperator operation = ((OperatorToken) token).getOperation();
            tempValue = operation.apply(fetchArguments(valueStack, tempValue, operation));
        }

        return tempValue;

    }

    /**
     * Return true if Precedence of PeekToken is greater than token.
     *
     * @param peekToken
     * @param token
     * @return
     */
    private boolean hasPrecedence(Token peekToken, Token token) {
        if (peekToken.getType() != Token.OPERATOR_TOKEN) {
            return false;
        }
        return (((OperatorToken) peekToken).getOperation().getPrecedence() > ((OperatorToken) token).getOperation()
                .getPrecedence());
    }

    private Boolean[] fetchArguments(Deque<Boolean> valueStack, boolean tempValue, PolicyOperator operation) {
        List<Boolean> arguments = new ArrayList<Boolean>(5);
        arguments.add(tempValue);
        for (int i = 1; i < operation.getNumberOfArguments(); i++) {
            arguments.add(valueStack.pop());
        }
        Boolean[] result = arguments.toArray(new Boolean[0]);
        return result;
    }

    private static final Logger _log = LoggerFactory.getLogger(PolicyExpression.class);
}
