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
package com.emc.caspian.ccs.esrs.api;

public class AuthorizationErrorMessage {

    /*These are the customized error messages that the ESRS Proxy will return.
    The format is maintained nin JSON format to facilitate reading of the response
    at the client which is expecting a JSON. */
    public static final String AUTH_MISSING_AUTH_INFO = "{\"Error\":\"No authenticated user info found\"}";
    public static final String AUTH_ACCESS_DENIED = "{\"Error\":\"Access Denied\"}";
    public static final String AUTH_INSUFFICIENT_PRIVILEGE =
	    "{\"Error\":\"User does not have sufficient privileges to access the resource\"}";
}
