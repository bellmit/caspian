/**
 * Copyright (c) 2016 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.esrs.server.helper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.model.PropertyCollectorScheduleModel;
import com.emc.caspian.ccs.esrs.server.controller.Controller;
import com.emc.caspian.ccs.esrs.server.controller.HandleRequestDelegate;
import com.emc.caspian.ccs.esrs.server.controller.Protocol;
import com.emc.caspian.ccs.esrs.server.controller.TransformDelegate;
import com.emc.caspian.ccs.esrs.server.impl.CaspianDetailsCollector;
import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.EsrsConfiguration;
import com.emc.caspian.ccs.esrs.server.util.EtcdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author kuppup
 *
 */
public class PropCollectorUtil {

    private static final Logger _log = LoggerFactory.getLogger(PropCollectorUtil.class);
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static ScheduledFuture<?> propTimeHandle = null;

    public static void startPropCollScheduler() {
        propTimeHandle = scheduler.scheduleAtFixedRate(new CaspianDetailsCollector(),
                EsrsConfiguration.getPropColStartTime(),
                EsrsConfiguration.getPropColInterval(),
                TimeUnit.MINUTES);
    }

    public static void restartPropCollScheduler() {

        propTimeHandle.cancel(true);
        propTimeHandle = scheduler.scheduleAtFixedRate(new CaspianDetailsCollector(),
                EsrsConfiguration.getPropColStartTime(),
                EsrsConfiguration.getPropColInterval(),
                TimeUnit.MINUTES);
    }

    public static Response getConfigInfo() {
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                new HandleRequestDelegate<Protocol.JsonStringResponse>()
                {
                    @Override
                    public Protocol.JsonStringResponse process() throws Exception {
                        _log.info("Reading Property Configuration Send Schedule");
                        final Protocol.JsonStringResponse response = new Protocol.JsonStringResponse();
                        PropertyCollectorScheduleModel propCollSchInt = new
                                PropertyCollectorScheduleModel();
                        propCollSchInt.setPropertyConfigSendInterval(EtcdUtils.
                                fetchFromEtcd(Constants.PROP_COLL_SCH_INTERVAL));
                        ObjectMapper mapper = new ObjectMapper();
                        String jsonString = mapper.writeValueAsString(propCollSchInt);
                        response.setJsonString(jsonString);
                        response.setStatus(Protocol.Status.OK);
                        return response;
                    }
                },
                new TransformDelegate<Protocol.JsonStringResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.JsonStringResponse response) {
                        ResponseBuilder rb = Response.status(response.getStatus().value());
                        rb.entity(response.getJsonString());
                        return rb;
                    }
                }, Controller.applicationExceptionMapper);

        return response;
    }

    public static Response setConfigInfo(PropertyCollectorScheduleModel newPropSchedSettings,
                                         boolean isDebug) {
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                new HandleRequestDelegate<Protocol.JsonStringResponse>()
                {
                    @Override
                    public Protocol.JsonStringResponse process() throws Exception {
                        _log.info("Updating Property Configuration Send Schedule");
                        
                        StringBuilder sb = new StringBuilder();
                        sb.append("Property config Send Interval was successfully changed- ");
                        
                        if(isDebug)
                            _log.info("ESRS Proxy Debug API called. Will run in debug mode.");
                        
                        final Protocol.JsonStringResponse response = new Protocol.JsonStringResponse();

                        if (null == newPropSchedSettings) {
                            _log.error("Updating Property Configuration Send Schedule failed");
                            response.setJsonString(Constants.INV_PAYLOAD);
                            response.setStatus(Protocol.Status.BAD_REQUEST);
                            return response;
                        }

                        String newStartTime = newPropSchedSettings.getPropertyConfigStartTime();
                        String newInterval = newPropSchedSettings.getPropertyConfigSendInterval();
                        
                        //see if the PUT request has a start time attribute. 
                        if( newStartTime != null && newStartTime != "" ) {
            
                            //start time attribute is sent in request
                            //we should set this in debug mode.
                            if(isDebug) {
                                
                                EtcdUtils.persistToEtcd(Constants.PROP_COLL_START_TIME,newStartTime);
                                sb.append("Start: ");
                                sb.append(newStartTime);
                                sb.append(" , ");
                                
                            } else { // in non debug mode, this should throw an error.
                                
                                _log.error("Updating Property Configuration Send Schedule failed");
                                _log.error("User is not allowed to change the property collector start time.");
                                response.setJsonString(Constants.INV_PAYLOAD);
                                response.setStatus(Protocol.Status.BAD_REQUEST);
                                return response;
                            }
            
                        }
                        
                        //if API is not in debug mode and interval is less than minimum allowed interval.
                        if(!isDebug && Integer.parseInt(newInterval) < Constants.MIN_PROP_COL_INT) {
                            _log.error("Updating Property Configuration Send Schedule failed");
                            _log.error("User attempting to set prop coll interval less than Minimum");
                            response.setJsonString(Constants.INV_PAYLOAD);
                            response.setStatus(Protocol.Status.BAD_REQUEST);
                            return response;
                        }
                        
                        EtcdUtils.persistToEtcd(Constants.PROP_COLL_SCH_INTERVAL, newInterval );
                        
                        //Now, restart the property scheduler.
                        restartPropCollScheduler();
                        response.setStatus(Protocol.Status.OK);
                        sb.append("Interval: ");
                        sb.append(newPropSchedSettings.getPropertyConfigSendInterval());
                        sb. append(" minutes");
                        response.setJsonString(sb.toString());
                        _log.info(sb.toString());

                        return response;
                    }
                },
                new TransformDelegate<Protocol.JsonStringResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.JsonStringResponse response) {
                        ResponseBuilder rb = Response.status(response.getStatus().value());
                        if(null != response.getJsonString()) {
                            rb.entity(response.getJsonString());
                        }
                        return rb;
                    }
                }, Controller.applicationExceptionMapper);

        return response;
    }

}
