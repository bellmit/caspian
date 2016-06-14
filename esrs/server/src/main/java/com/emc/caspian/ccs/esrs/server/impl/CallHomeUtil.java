package com.emc.caspian.ccs.esrs.server.impl;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.server.controller.Controller;
import com.emc.caspian.ccs.esrs.server.controller.HandleRequestDelegate;
import com.emc.caspian.ccs.esrs.server.controller.Protocol;
import com.emc.caspian.ccs.esrs.server.controller.TransformDelegate;
import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.EtcdUtils;

public class CallHomeUtil {

    private static final Logger _log = LoggerFactory.getLogger(CallHomeUtil.class);
    
    CallHomeUtil(){
        
    }
    
    public static Response getConfigInfo() {
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                new HandleRequestDelegate<Protocol.JsonStringResponse>()
                {
                    @Override
                    public Protocol.JsonStringResponse process() throws Exception {
                        _log.info("Reading if CallHome is enabled");
                        final Protocol.JsonStringResponse response = new Protocol.JsonStringResponse();

                        String callHomeConfig = EtcdUtils.fetchFromEtcd(Constants.IS_ESRS_CALL_HOME_ENABLED);
                        StringBuilder sb = new StringBuilder("{\"callHomeEnabled\":\"");
                        sb.append(callHomeConfig);
                        sb.append("\"}");
                        response.setJsonString(sb.toString());
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

    public static Response setConfigInfo(String config) {
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                new HandleRequestDelegate<Protocol.JsonStringResponse>()
                {
                    @Override
                    public Protocol.JsonStringResponse process() throws Exception {
                        _log.info("Updating cofig to enable/disable callhome");
                        
                        final Protocol.JsonStringResponse response = new Protocol.JsonStringResponse();

                        if ( config.equalsIgnoreCase(Constants.ENABLE) || 
                             config.equalsIgnoreCase(Constants.DISABLE)) {
                            
                            String value = config.equalsIgnoreCase(Constants.ENABLE) ? Constants.TRUE : Constants.FALSE;
                            EtcdUtils.persistToEtcd(Constants.IS_ESRS_CALL_HOME_ENABLED,value);
                            StringBuilder sb = new StringBuilder();
                            sb.append("Call Home enabled config was successfully changed- ");
                            sb.append("is-callhome-enabled : ");
                            sb.append(value);
                            response.setJsonString(sb.toString());
                            response.setStatus(Protocol.Status.OK);
                            _log.info(sb.toString());

                            return response;
                            
                        }else
                            
                            _log.error("Updating CallHome-Enable configuration failed. Invalid query sring");
                            response.setJsonString(Constants.INV_PAYLOAD);
                            response.setStatus(Protocol.Status.BAD_REQUEST);
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
