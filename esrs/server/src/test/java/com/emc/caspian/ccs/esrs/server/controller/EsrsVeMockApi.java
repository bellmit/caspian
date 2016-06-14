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

package com.emc.caspian.ccs.esrs.server.controller;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class EsrsVeMockApi {
    
    private static final String DUMMY_MODEL = "BETA2-GW";
    private static final String DUMMY_SERIAL = "BETA2ENG11-1";

    @Path("esrs/v1/devices/"+DUMMY_MODEL+"/"+DUMMY_SERIAL)
    public static interface EsrsVeMock {
	//Use nouns and not verbs for all the method names

	/**
	 * API to post the ESRS VE details for registration  
	 * 
	 * @param request
	 * @return
	 */
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response register(@Context HttpServletRequest request) throws Exception;

	/**
	 * API to GET the ESRS VE details for UI  
	 * 
	 * @param request
	 * @return
	 */
	@GET
	@Produces({MediaType.APPLICATION_JSON})
	public Response registerationDetails() throws Exception;

	/**
	 * API to post the Health Status of ECI to ESRS VE
	 *
	 * @param request
	 * @return
	 */
	@POST
	@Path("/keepalive")
	@Produces({MediaType.APPLICATION_JSON})
	public Response healthStatus();

	/**
	 * API to post the call Home Alerts of ECI to ESRS VE
	 *
	 * @param request
	 * @return
	 */
	@POST
	@Path("/connectemc")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response callHome(@Context HttpServletRequest callHomeProxy);

	/**
	 * API to Delete the ESRS VE details from ESRS VE
	 *
	 * @param request
	 * @return
	 */
	@DELETE
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteRegistration();
    }
}