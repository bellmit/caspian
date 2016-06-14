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

package com.emc.caspian.ccs.esrs.server.impl;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.http.HttpHeader;

import com.emc.caspian.ccs.esrs.server.controller.EsrsConstants;
import com.emc.caspian.ccs.esrs.server.controller.EsrsVeMockApi.EsrsVeMock;

public class EsrsVeMockImpl implements EsrsVeMock {
    
    private static final String VE_GATEWAY = EsrsConstants.VE_GATEWAY;
    private static final String VE_PORT    = EsrsConstants.VE_PORT;
    private static final String VE_SSL     = EsrsConstants.VE_SSL;
    private static final String VE_USER    = EsrsConstants.VE_USER;
    private static final String VE_PASS    = EsrsConstants.VE_PASS;

    @Override
    public Response register(HttpServletRequest request) throws Exception {
	
	String myString = "{\"message\":\"Request Approved\",\"serialNumber\":\"BETA2ENG11-1\",\"gatewaySerialNumber\":\"9SYZLYKNLD017N\","+
                "\"veType\":\"Connected\",\"model\":\"BETA2-GW\",\"deviceKey\":\"6404d47d37cc5e94692defae5661d4a510e53be1204921e2c1"+
                "7a6de2675c69bcb087c9967188f1d6ada3293965cb4bf40913ede169a7842b9c9b30b736853cc2\",\"responseCode\":201}";
	
	String receivedAuthString = request.getHeader(HttpHeader.AUTHORIZATION.asString());
	
	StringBuilder sb = new StringBuilder(VE_USER);
	sb.append(":");
	sb.append(VE_PASS);
	String expectedAuthString = sb.toString();
	
	if(receivedAuthString.equals(expectedAuthString))
	    return Response.status(201).entity(myString).build();
	else
	    return Response.status(403).build();
    }

    @Override
    public Response registerationDetails() throws Exception {
	
	String myString = "{\"message\":\"Request Approved\",\"serialNumber\":\"BETA2ENG11-1\",\"gatewaySerialNumber\":\"9SYZLYKNLD017N\","+
                 "\"veType\":\"Connected\",\"model\":\"BETA2-GW\",\"deviceKey\":\"6404d47d37cc5e94692defae5661d4a510e53be1204921e2c1"+
                 "7a6de2675c69bcb087c9967188f1d6ada3293965cb4bf40913ede169a7842b9c9b30b736853cc2\",\"responseCode\":201}";
	return Response.status(200).entity(myString).build();
    }

    @Override
    public Response healthStatus() {
	return Response.status(200).build();
    }

    @Override
    public Response callHome(HttpServletRequest callHomeProxy) {
	return Response.status(200).build();
    }

    @Override
    public Response deleteRegistration() {
	return Response.status(200).build();
    }
}
