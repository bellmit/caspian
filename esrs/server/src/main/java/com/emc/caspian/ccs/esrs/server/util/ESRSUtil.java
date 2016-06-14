/**
 *  Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.esrs.server.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyEventModel;
import com.emc.caspian.ccs.esrs.server.exception.NISException;
import com.emc.caspian.crs.CRSClientBuilder;
import com.emc.caspian.crs.ComponentRegistryClient;
import com.emc.caspian.crs.ServiceLocationClient;
import com.emc.caspian.crs.model.BalancerRule;
import com.emc.caspian.crs.model.ComponentEndpoint;
import com.emc.caspian.crs.model.ServiceRecord;
import com.emc.caspian.encryption.AESUtil;

/**
 * @author kuppup
 *
 * Utility class for all ESRS Common operations
 * 
 */
public class ESRSUtil {
    private static final Logger _log = LoggerFactory.getLogger(ESRSUtil.class);

    public static char[] getKeystonePassword() throws Exception {
        final String KsEncPwd,KsPwd;
        if (System.getenv().containsKey("KS_CSA_PWD")){
                KsEncPwd = System.getenv("KS_CSA_PWD");
        }else {
            _log.error("Keystone password is missing from environment variables");
            throw new Exception("Keystone password is missing from environment variables");
        }
        try{
                AESUtil au = AESUtil.getInstance();
                KsPwd = au.decrypt(KsEncPwd);
        }catch(Exception e){
            _log.error("Error decrpting keystone password", e);
            throw e;
        }
        return KsPwd.toCharArray();
    }

    public static String getEndPointFromCRS(final String service, final String component) {
        ServiceLocationClient client = CRSClientBuilder.newServiceLocationClient();
        List<ComponentEndpoint> endpoints = client.getAllEndpoints(service, component);
        if (endpoints.size() > 0) {
            for (ComponentEndpoint compEndPoint : endpoints) {
                String url = compEndPoint.getUrl();
                _log.debug("URL {} " , url);
                if(null != Constants.CONTAINER_HOST_IP && url.contains(Constants.CONTAINER_HOST_IP )) {
                    return url;
                }
            }
            return endpoints.get(0).getUrl();
        }
        else {
            _log.error("No Endpoint available for Service {}, Component {}",
                    service, component);
            throw new RuntimeException("No Endpoint available for the service/component");
        }
    }

    public static String getPublishedPvtAPIEndPt(final String service, final String component) {
        ServiceLocationClient client = CRSClientBuilder.newServiceLocationClient();
        List<ComponentEndpoint> endpoints = client.getAllEndpoints(service, component);
        if (endpoints.size() > 0) {
            for (ComponentEndpoint compEndPoint : endpoints) {
                if (compEndPoint.isPublished() &&
                        compEndPoint.getName().equals(Constants.API) &&
                        compEndPoint.getType().equals(Constants.PRIVATE)) {
                    _log.debug("URL {} " , compEndPoint.getUrl());
                    return compEndPoint.getUrl();
                }
            }
            return endpoints.get(0).getUrl();
        }
        else {
            _log.error("No Endpoint available for Service {}, Component {}",
                    service, component);
            throw new RuntimeException("No Endpoint available for the service/component");
        }
    }

    public static String getVIPFromCRS(final String service, final String component) {
        String vip=null;
        ComponentRegistryClient CRSclient = CRSClientBuilder.newClient();
        List<BalancerRule> rules = CRSclient.getComponent(service,
                component).getBalancerRules();
        if (!rules.isEmpty()) {
            for(BalancerRule rule:rules){
                if (rule.getParams().containsKey(Constants.VIP)) {
                    String ruleVIP = rule.getParams().get(Constants.VIP).toString();
                    if(ruleVIP.length() > 0 && !ruleVIP.isEmpty()) {
                        vip = ruleVIP;
                        break;
                    }
                }
            }
        }
        return vip;
    }

    public static JSONArray getAllPlatformNodes() throws NISException{
        try {
            StringBuilder nisURI = new StringBuilder(ESRSUtil.getPublishedPvtAPIEndPt(Constants.PLATFORM, Constants.NODE_INVENTORY));
            nisURI.append(Constants.NIS_GET_PLATFORM_NODES);
            org.apache.http.HttpResponse httpGetNISResponse = RestClientUtil.httpGETRequest(nisURI.toString(),null).get();

            String responseString = EntityUtils.toString(httpGetNISResponse.getEntity(), Charsets.UTF_8);
            _log.debug("Node URI got from NIS :: {}" , responseString);

            JSONObject nodesDetails = new JSONObject(responseString);

            JSONArray nodesArray = nodesDetails.getJSONArray(Constants.NODES);
            if (null == nodesArray || nodesArray.length() <= 0 ) {
                throw new NISException("No platform nodes available in NIS");
            }
            return nodesArray;
        } catch (Exception e) {
            _log.error("fetching ECI IP details from CRS/NIS failed due to {}", e);
            throw new NISException("Unable to fetch ECI IP details from CRS/NIS");
        }
    }

    public static boolean isValidPayload(final Object payloadObject,
            final List<String> mustHaveProps) {
            if (null == payloadObject) {
                _log.error("Invalid Payload");
                return false;
            }
            // MZ: Find the correct method
            Class<? extends Object> baseClass = payloadObject.getClass();
            for (Method method : baseClass.getMethods())
            {
                String methName = method.getName();
                _log.debug("Name {} ", methName);
                if ((methName.startsWith("get") && mustHaveProps.contains(methName.substring(3)))
                        || (methName.startsWith("is") && mustHaveProps.contains(methName.substring(2)))) {
                    Object value = null;
                    try {
                        value = method.invoke(payloadObject);
                        _log.debug("Property {} Value {} ", methName, value);
                        if (null == value) {
                            _log.debug("Missing value for property {} ", methName);
                            return false;
                        } else if (value instanceof List<?>) {
                            //To Validate for CallHome Symptom JSON
                            if (((List) value).size() == 0 ) {
                                _log.error("Invalid Events Payload");
                                return false;

                            }
                            for (Object event : (List<Object>)value) {
                                if (event instanceof EsrsCallHomeProxyEventModel){
                                    _log.debug("Ret type {}" , event.getClass());
                                    List<String> eventMustHaveProps = Arrays.asList("SymptomCode", "Category",
                                            "Severity", "Status", "ComponentID", "SubComponentID", "EventData", "EventDescription");
                                    if (!isValidPayload(event, eventMustHaveProps)){
                                        _log.error("Invalid Events Payload");
                                        return false;
                                    }
                                } else {
                                    _log.error("Invalid Payload");
                                    return false;
                                }
                            }
                        }
                    } catch (IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException e) {
                        _log.error("Invalid Payload");
                        return false;
                    }
                }
            }
            return true;
    }

    public static ServiceRecord getServiceFromCRS(final String service) {
        ComponentRegistryClient CRSclient = CRSClientBuilder.newClient();
        ServiceRecord serviceRecord = CRSclient.getService(service);
        return serviceRecord;
    }

}
