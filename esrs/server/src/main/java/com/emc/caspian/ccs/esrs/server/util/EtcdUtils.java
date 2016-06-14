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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.Charsets;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.esrs.internal.model.EtcdResult;
import com.emc.caspian.ccs.esrs.server.controller.EtcdAccessException;
import com.emc.caspian.crs.CRSClientBuilder;
import com.emc.caspian.crs.ServiceLocationClient;
import com.emc.caspian.crs.model.ComponentEndpoint;
import com.google.common.collect.Lists;

public class EtcdUtils {
    
    private static final Logger _log = LoggerFactory.getLogger(EtcdUtils.class);
    private static final String ETCD_KEY_PREFIX = "v2/keys/caspian/ccs-esrs/";
    private static final String ETCD_SERVICE = "etcd-service";
    private static final String LOCAL_ETCD_URI = "http://localhost:4001";
    private static final String CONTAINER_HOST_IP = System.getenv("CONTAINER_HOST_ADDRESS");
    
    private static final String SLASH = "/";
    
    public static void persistToEtcd(String key, String value) {
	
	try {
	    EtcdUtils.updateKeyValToEtcd(HttpPut.METHOD_NAME, key, value);
	} catch (ExecutionException e) {
	    _log.error("Could not persist {} to ETCD.", key, e);
	} catch (Exception e) {
	    _log.error("Could not persist {} to ETCD.", key, e);
	}
    }

    public static String fetchFromEtcd(String key) {
	
	String value = null;
	
	try {
	    value = EtcdUtils.fetchValueFromEtcd(key);
	} catch (ExecutionException e) {
	    _log.error("Could not fetch key from ETCD.", e);
	} catch (Exception e) {
	    _log.error("Could not fetch key from ETCD.", e);
	}
	
	return value;
    }
    
    public static boolean esrsIsRegistered() {

	String registered = fetchFromEtcd(Constants.CASPIAN_PROXY_VE_REGISTERED);

	if(Constants.YES.equalsIgnoreCase(registered)) {
	    _log.debug("ESRS is Registered.");
	    return true;
	}

	_log.debug("ESRS is NOT Registered.");
	return false;
    }

    public static boolean esrsIsConnected() {

        String connected = fetchFromEtcd(Constants.CASPIAN_PROXY_VE_CONNECTED);

        if(Constants.YES.equalsIgnoreCase(connected)) {
            _log.debug("ESRS is connected.");
            return true;
        }

        _log.debug("ESRS is NOT connected.");
        return false;
    }

    public static String fetchValueFromEtcd(String key) throws IOException, InterruptedException, ExecutionException {
        StringBuilder etcdURI = getEtcdBaseUrl().append(ETCD_KEY_PREFIX).append(key);
        HttpResponse httpRegisterResponse = RestClientUtil.httpGETRequest(etcdURI.toString(),null).get();

        String responseString = EntityUtils.toString(httpRegisterResponse.getEntity(), Charsets.UTF_8);
        _log.debug("ESRS VE details got from etcd kv store :: {}", responseString);

        EtcdResult etcdResp = JsonHelper.deserializeFromJson(responseString,EtcdResult.class);

        if(etcdResp.isError()) {
            if(etcdResp.errorCode == 100) {
                throw new EtcdAccessException("Error reading " + key + " from etcd store");
            }
        }
        return etcdResp.node.value;
    }
    
    private static StringBuilder getEtcdBaseUrl() throws IOException {
        //Setting localETCD as default URI
        StringBuilder etcdBaseUri = new StringBuilder(LOCAL_ETCD_URI);
        try{
            etcdBaseUri = new StringBuilder(getEndPointFromCRS(Constants.PLATFORM, ETCD_SERVICE));
        } catch (Exception e) {
            _log.debug("Unable to get ETCD Base URI from CRS, Using the default URI");
        }
        _log.debug("ETCD service's base url :: {}" , etcdBaseUri);
        return etcdBaseUri.append(SLASH);
    }
    
    public static void updateKeyValToEtcd(String method, String key, String value) throws Exception {

        StringBuilder etcdURI = getEtcdBaseUrl().append(ETCD_KEY_PREFIX).append(key);
        List<BasicNameValuePair> data = Lists.newArrayList();
        data.add(new BasicNameValuePair("value", value));

        UrlEncodedFormEntity entity = new  UrlEncodedFormEntity(data, Charsets.UTF_8);
        HttpResponse httpRegisterResponse = RestClientUtil.httpRequest(method, etcdURI.toString(), null, entity ).get();

        String responseString = EntityUtils.toString(httpRegisterResponse.getEntity(), Charsets.UTF_8);

        EtcdResult etcdResp = JsonHelper.deserializeFromJson(responseString,EtcdResult.class);

        if(etcdResp.isError()) {
            if(etcdResp.errorCode == 100) {
                throw new RuntimeException("Error reading production key from etcd store");
            }
        }
        _log.debug("Response from etcd: {}",httpRegisterResponse.getStatusLine());
        _log.debug("ESRS VE {} has been successfully updated in etcd kv store :: {}", key, etcdResp.message);
    }

    private static String getEndPointFromCRS(final String service, final String component) {
        ServiceLocationClient client = CRSClientBuilder.newServiceLocationClient();
        List<ComponentEndpoint> endpoints = client.getAllEndpoints(service, component);
        if (endpoints.size() > 0) {
            for (ComponentEndpoint compEndPoint : endpoints) {
                String url = compEndPoint.getUrl();
                _log.debug("URL {} " , url);
                if(null != CONTAINER_HOST_IP && url.contains(CONTAINER_HOST_IP)) {
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
}
