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

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.http.protocol.HTTP;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.response.HttpResponse;
import com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel;
import com.emc.caspian.ccs.esrs.server.controller.Controller;
import com.emc.caspian.ccs.esrs.server.controller.HandleRequestDelegate;
import com.emc.caspian.ccs.esrs.server.controller.Protocol;
import com.emc.caspian.ccs.esrs.server.controller.TransformDelegate;
import com.emc.caspian.ccs.esrs.server.exception.PayLoadException;
import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.ESRSProxyRestClient;
import com.emc.caspian.ccs.esrs.server.util.ESRSUtil;

/**
 * @author kuppup
 *
 * Utility Class to having the implementation of all Connection Test related APIs
 */
public class TestESRSConnectionUtil {

    private static final Logger _log = LoggerFactory.getLogger(TestESRSConnectionUtil.class);

    public static Response TestESRSConnection(
            EsrsVeRegistrationModel registrationDetails) {
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(request,
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.GateWayResponse process() throws Exception {
                        Protocol.GateWayResponse resp = new Protocol.GateWayResponse();

                        _log.info("Testing ESRS VE Connectivity");
                        if (!ESRSUtil.isValidPayload(registrationDetails, Constants.regMustHaveProps)) {
                            _log.error(Constants.INV_PAYLOAD);
                            throw new PayLoadException(Constants.INV_PAYLOAD);
                        }
                        String dummySerialNum = "TEST_ESRS_SN";
                        String dummyModel = "TEST_ESRS_MODEL";

                        StringBuilder authString = new StringBuilder(
                                registrationDetails.getUsername()).append(Constants.COLON)
                                .append(registrationDetails.getPassword());

                        Map<String, Object> httpHeaders = new HashMap<String, Object>();
                        httpHeaders.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                        httpHeaders.put(HttpHeader.ACCEPT.asString(), MediaType.APPLICATION_JSON);
                        httpHeaders.put(HttpHeader.AUTHORIZATION.asString(), authString.toString());

                        StringBuilder postRequestBody = new StringBuilder("{\"ipAddress\":\"");
                        postRequestBody.append("0.0.0.0");
                        postRequestBody.append("\"}");

                        StringBuilder gatewayBaseURI = new StringBuilder("https://");
                        gatewayBaseURI.append(registrationDetails.getGateway());
                        gatewayBaseURI.append(Constants.COLON);
                        gatewayBaseURI.append(registrationDetails.getPort());
                        gatewayBaseURI.append(Constants.ESRS_VE_V1_ENDPOINT_URI);
                        gatewayBaseURI.append(dummyModel).append(Constants.SLASH).append(dummySerialNum);

                        _log.debug("Executing Testing ESRS VE Connectivity using URI {}", gatewayBaseURI);

                        try {
                            HttpResponse<String> postResponse = ESRSProxyRestClient.post(gatewayBaseURI.toString()
                                    , httpHeaders, postRequestBody.toString());

                            int respStatus = postResponse.getStatusCode();

                            _log.debug("Response Status : {}" , respStatus);
                            resp.setStatus(Protocol.Status.OK);

                            if(201 == respStatus || 200 == respStatus ) {
                                _log.info("ESRS VE Connection is success");
                                EsrsVeRegistrationModel veRegModel = new EsrsVeRegistrationModel();
                                veRegModel.setMessage("ESRS VE Connection is success");
                                resp.setJsonObject(veRegModel);
                            } else if(401 == respStatus) {
                                _log.info("ESRS VE Connection is success, But username/password is invalid");
                                EsrsVeRegistrationModel veRegModel = new EsrsVeRegistrationModel();
                                veRegModel.setMessage("ESRS VE Connection is success, But username/password is invalid.");
                                resp.setJsonObject(veRegModel);
                            } else {
                                _log.error("Testing ESRS VE Connectivity is failed with {}" , respStatus);
                                EsrsVeRegistrationModel veRegModel = new EsrsVeRegistrationModel();
                                veRegModel.setVeConnected("No");
                                if(respStatus > 0) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(postResponse.getErrorMessage());
                                    String responseBody = postResponse.getResponseBody();
                                    if(null != responseBody) {
                                        JSONObject jsonbody = new JSONObject(responseBody);
                                        if(null != jsonbody && jsonbody.has(Constants.MESSAGE)) {
                                            sb.append(" | ");
                                            String s = jsonbody.getString(Constants.MESSAGE);
                                            if(null != s &&
                                                    s.startsWith("Error occurred while communicating to DRM service")) {
                                                sb.append("ESRS VE could not establish a connection with EMC (DRM Service).");
                                                sb.append("Please check if your ESRS VE is connected.");
                                            } else {
                                                sb.append(s);
                                            }
                                        }
                                    }
                                    veRegModel.setMessage(sb.toString());
                                    _log.error("Testing ESRS VE Connectivity is failed due to {}" , sb.toString());
                                } else if (postResponse.getErrorMessage().contains(Constants.SOCKET_TIMEOUT_EXCEPTION)
                                        || postResponse.getErrorMessage().contains(Constants.CONNECT_EXCEPTION)) {
                                    _log.error("Testing ESRS VE Connectivity is failed due to {}" , Constants.NOT_REACHABLE_ESRS_VE);
                                    veRegModel.setMessage(Constants.NOT_REACHABLE_ESRS_VE);
                                } else {
                                    _log.error("Testing ESRS VE Connectivity is failed due to {}" , Constants.INV_GATEWAY_PORT_SSL);
                                    veRegModel.setMessage(Constants.INV_GATEWAY_PORT_SSL);
                                }
                                resp.setJsonObject(veRegModel);
                            }
                        } catch (URISyntaxException | JSONException e) {
                            EsrsVeRegistrationModel veRegModel = new EsrsVeRegistrationModel();
                            veRegModel.setMessage(Constants.INV_GATEWAY_PORT_SSL);
                            resp.setJsonObject(veRegModel);
                            _log.error("Testing ESRS VE Connectivity is failed with error {}", e.getMessage());
                        }
                        return resp;
                    }
                },
                new TransformDelegate<Protocol.GateWayResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.GateWayResponse response) {
                        ResponseBuilder rb = Response.status(response.getStatus().value());
                        rb.entity(response.getJsonObject());
                        return rb;
                    }
                }, Controller.applicationExceptionMapper);
        return response;
    }
}
