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

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains helper functions required to fetch/create new policyOperators.
 *
 * @author shrids
 *
 */
public abstract class Operators {

    public static final List<String> ALLOWED_OPERATORS = Arrays.asList("and", "not", "or");

    private static PolicyOperator[] operators = new PolicyOperator[4];

    static {
        operators[1] = new PolicyOperator("and", 2, OperatorPrecedence.PRECEDENCE_AND) {

            @Override
            public boolean apply(Boolean... args) {
                _log.debug("AND method invoked with args {} , {} ", args[0], args[1]);
                return args[0] && args[1];
            }
        };
        operators[2] = new PolicyOperator("not", 1, OperatorPrecedence.PRECEDENCE_NOT) {

            @Override
            public boolean apply(Boolean... args) {
                return !args[0];
            }

        };
        operators[3] = new PolicyOperator("or", 2, OperatorPrecedence.PRECEDENCE_OR) {

            @Override
            public boolean apply(Boolean... args) {
                _log.debug(" OR method invoked with args {}, {}", args[0], args[1]);
                return args[0] || args[1];
            }
        };
    }

    public static PolicyOperator getOperator(String value) {
        switch (value) {
        case "and":
            return operators[1];
        case "not":
            return operators[2];
        case "or":
            return operators[3];
        default:
            // this line will not be executed in the current flow throwing an exception if we are
            // here
            throw new IllegalArgumentException("Invalid Opertor: " + value);
        }
    }

    public static boolean isAllowedOperator(String operatorName) {
        return ALLOWED_OPERATORS.contains(operatorName);
    }

    private static final Logger _log = LoggerFactory.getLogger(Operators.class);
}
