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

import static com.emc.caspian.ccs.common.policyengine.parser.Functions.POLICY_FUNCTION_ALL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.fabric.util.Validate;

public class PolicyTokenizer {

    private String expression;

    public PolicyTokenizer(String expression) {
        Validate.isNotNull(expression, "Expression");
        this.expression = StringUtils.isBlank(expression) ? POLICY_FUNCTION_ALL : expression;
    }

    public List<Token> parse() {
        List<Token> tokens = new ArrayList<Token>();
        try {
            String[] strings = insertSpace(expression).split(" +");
            for (String str : strings) {
                if (StringUtils.isBlank(str))
                    continue;
                tokens.add(fetchToken(str));
            }
        } catch (IllegalArgumentException exp) {
            // exception during parsing the token, default to enable all
            _log.error("Parsing of policy failed with the following exception", exp);
            tokens.clear();
            tokens.add(new FunctionToken(Functions.getFunction(POLICY_FUNCTION_ALL)));
        }
        return tokens;
    }

    private Token fetchToken(String str) {
        if (isOpenBrace(str)) {
            return new OpenBraceToken();
        } else if (isCloseBrace(str)) {
            return new CloseBraceToken();
        } else if (Operators.isAllowedOperator(str)) {
            return new OperatorToken(Operators.getOperator(str));
        } else if (Functions.isAllowedFunction(str)) {
            return new FunctionToken(Functions.getFunction(str));
        } else {
            _log.error("Invalid token: {} found. Disabling all the policy for this method", str);
            throw new IllegalArgumentException("Invalid token found, diabling all policy rules for this method");
        }
    }

    private String insertSpace(String expression) {
        StringBuilder builder = new StringBuilder(" ").append(expression).append(" ");
        // The following reqEx is used to insert spaces inside the rule.
        return (builder.toString().replaceAll("([^%])\\(", "$1 \\( ").replaceAll("\\)([^s])", " \\) $1"));

    }

    private boolean isOpenBrace(String str) {
        if (StringUtils.strip(str).equals("("))
            return true;
        return false;
    }

    private boolean isCloseBrace(String str) {
        if (StringUtils.strip(str).equals(")"))
            return true;
        return false;
    }

    private static final Logger _log = LoggerFactory.getLogger(PolicyTokenizer.class);
}
