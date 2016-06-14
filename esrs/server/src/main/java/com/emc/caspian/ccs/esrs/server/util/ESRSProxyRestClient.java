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


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.emc.caspian.ccs.client.ClientConfig;
import com.emc.caspian.ccs.client.RestClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.HttpResponse;

/**
 * @author kuppup
 * 
 * Sync Rest client utility for making rest calls to any Rest Servers
 */
public class ESRSProxyRestClient {

    private RestClient restClient;

    public ESRSProxyRestClient(ClientConfig clientConfig) {
        this.restClient=new RestClient(clientConfig);
    }

    public ESRSProxyRestClient(URI uri, ClientConfig clientConfig) {
        this.restClient=new RestClient(uri, clientConfig);
    }

    public static RestClient getClient(String baseUri) throws URISyntaxException {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setIgnoreCertificates(true);

        URI uri = new URI(baseUri);
        final RestClient restClient = new ESRSProxyRestClient(uri, clientConfig).restClient;
        return restClient;
    }

    public static HttpResponse<String> post(String url, Map<String, Object> reqHeaders,
            String reqBody) throws URISyntaxException {
        ClientResponse<String> resp = getClient(url).post(String.class, reqBody, "", reqHeaders);
        return resp.getHttpResponse();
    }

    public static HttpResponse<String> delete(String url, Map<String, Object> reqHeaders) throws URISyntaxException {
        ClientResponse<String> resp = getClient(url).delete(String.class, "", reqHeaders);
        return resp.getHttpResponse();
    }

    public static HttpResponse<String> get(String url) throws URISyntaxException {
        ClientResponse<String> resp = getClient(url).get(String.class, "");
        return resp.getHttpResponse();
    }
}
