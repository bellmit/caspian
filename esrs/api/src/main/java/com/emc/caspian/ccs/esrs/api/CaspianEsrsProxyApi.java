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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.emc.caspian.ccs.esrs.api.AuthorizationPolicy.Rule;
import com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel;
import com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel;
import com.emc.caspian.ccs.esrs.model.PropertyCollectorScheduleModel;


/**
 * 
 * REST APIs implemented by caspian-esrs service.
 *
 * @author shivat
 *
 */
public class CaspianEsrsProxyApi {
    
    @Path("v1/esrs")
    public static interface EsrsProxy {

        /**
         * API to post the ESRS VE details for registration  
         * 
         * @param request
         * @return
         */
        @POST
        @Path("/ve-gateway")
        @Consumes({MediaType.APPLICATION_JSON})
        @Produces({MediaType.APPLICATION_JSON})
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN})
        public Response register(@Context HttpServletRequest servRequest,
                EsrsVeRegistrationModel registrationDetails);

        /**
         * API to GET the ESRS VE details for UI  
         * 
         * @param request
         * @return
         */
        @GET
        @Path("/ve-gateway")
        @Produces({MediaType.APPLICATION_JSON})
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_MONITOR, 
                               Rule.ALLOW_CLOUD_SERVICE})
        public Response registerationDetails();

        /**
         * API to post the Health Status of ECI to ESRS VE
         *
         * @param request
         * @return
         */
        @POST
        @Path("/health")
        @Produces({MediaType.APPLICATION_JSON})
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_SERVICE})
        public Response healthStatus();

        /**
         * API to post the call Home Alerts of ECI to ESRS VE
         *
         * @param request
         * @return
         */
        @POST
        @Path("/callhome")
        @Consumes({MediaType.APPLICATION_JSON})
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_SERVICE})
        public Response callHome(EsrsCallHomeProxyModel callHomeProxy);
        
        /**
         * API to post the call Home Alerts of ECI to ESRS VE
         *
         * @param request
         * @return
         */
        @POST
        @Path("/callhome")
        @Consumes({MediaType.MULTIPART_FORM_DATA})
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_SERVICE})
        public Response callHomeFileUpload(@Context HttpServletRequest servRequest);
        
        /**
         * API to GET the ESRS Proxy's Call Home Config
         * 
         * @param request
         * @return
         */
        @GET
        @Path("/config/callhome")
        @Produces({MediaType.APPLICATION_JSON})
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_MONITOR, 
                               Rule.ALLOW_CLOUD_SERVICE})
        public Response getCallHomeConfig();
        
        /**
         * API to enable or disable the call Home Alerts of ECI to ESRS VE
         *
         * @param request
         * @return
         */
        @POST
        @Path("/config/callhome")
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN })
        public Response changeCallHomeConfig(@Context UriInfo uriInfo);
        
        /**
         * API to enable or disable the call Home Alerts of ECI to ESRS VE
         *
         * @param request
         * @return
         */
        @PUT
        @Path("/config/callhome")
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN })
        public Response changeCallHomeConfigPut(@Context UriInfo uriInfo);

        /**
         * API to Delete the ESRS VE details from ESRS VE
         *
         * @param request
         * @return
         */
        @DELETE
        @Path("/ve-gateway")
        @Produces({MediaType.APPLICATION_JSON})
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN})
        public Response deleteRegistration();

        /**
         * API to GET the Property Collector Scheduler configuration details
         *
         * @return
         */
        @GET
        @Path("/config/schedule")
        @Produces({ MediaType.APPLICATION_JSON })
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN,
                Rule.ALLOW_CLOUD_MONITOR, Rule.ALLOW_CLOUD_SERVICE })
        public Response propConfigScheduleInfo();

        /**
         * API to Create the Property Collector Scheduler configuration details
         *
         * @param request
         * @return
         */
        @POST
        @Path("/config/schedule")
        @Consumes({ MediaType.APPLICATION_JSON })
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN,
                Rule.ALLOW_CLOUD_MONITOR, Rule.ALLOW_CLOUD_SERVICE })
        public Response createPropConfigSchedule(PropertyCollectorScheduleModel
                propCollectSchModel);

        /**
         * API to Update the Property Collector Scheduler configuration details
         *
         * @param request
         * @return
         */
        @PUT
        @Path("/config/schedule")
        @Consumes({ MediaType.APPLICATION_JSON })
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN,
                Rule.ALLOW_CLOUD_MONITOR, Rule.ALLOW_CLOUD_SERVICE })
        public Response updatePropConfigSchedule(@Context UriInfo uriInfo, PropertyCollectorScheduleModel
                propCollectSchModel);

        /**
         * API to Test the ESRS VE details for registration
         *
         * @param request
         * @return
         */
        @POST
        @Path("/test-connection")
        @Consumes({MediaType.APPLICATION_JSON})
        @Produces({MediaType.APPLICATION_JSON})
        @AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN})
        public Response testConnection(EsrsVeRegistrationModel registrationDetails);
    }
}