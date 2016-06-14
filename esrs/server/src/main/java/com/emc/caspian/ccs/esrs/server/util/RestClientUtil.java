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
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.server.controller.Protocol.Status;

/**
 * @author kuppup
 *
 */
public class RestClientUtil {
    private static SSLContext trustAllContext;
    private static NullHostNameVerifier nullHostnameVerifier;
    private static final String KEYSTORE_FILE = "conf/EsrsProxyKeyStore";

    private static final Logger _log = LoggerFactory.getLogger(RestClientUtil.class);

    private static CloseableHttpAsyncClient httpClient;
    private static HttpClient httpMultiPartClient;


    static {
        // Create the client first
        // TODO temporarily using ssl ignore. this should be properly authenticated.
        SSLContext sslContext = getTrustAllContext();

        httpClient = HttpAsyncClients.custom()
                .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .setSSLContext(sslContext).build();

        httpClient.start();

        httpMultiPartClient = HttpClientBuilder.create()
                .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .setSslcontext(sslContext).build();
    }

    /**
     * To Handle all the GET Requests for a restClient
     * @param uri
     * @param headers
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Future<HttpResponse> httpGETRequest(String uri,
            Map<String, String> headers) throws UnsupportedEncodingException {
        HttpGet request;
        // Creating the POST request and execute it.
        request = new HttpGet(uri);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }

        return httpClient.execute(request, null);
    }

    /**
     * To Handle all the Post Requests for a restClient
     * @param uri
     * @param body
     * @param headers
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Future<HttpResponse> httpPostRequest(String uri,
            Map<String, String> headers, String body)
            throws UnsupportedEncodingException {
        HttpPost request;
        // Creating the POST request and execute it.
        request = new HttpPost(uri);

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }
        if (null != body) {
            StringEntity entity = new StringEntity(body);
            request.setEntity(entity);
        }
        return httpClient.execute(request, null);
    }

    /**
     * To Handle all the Delete Requests for a restClient
     * @param uri
     * @param body
     * @param headers
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Future<HttpResponse> httpDeleteRequest(String uri,
            Map<String, String> headers) throws UnsupportedEncodingException {
        HttpDelete request;
        // Creating the Delete request and execute it.
        request = new HttpDelete(uri);
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }
        return httpClient.execute(request, null);
    }

    /***
     * To Handle all the Requests for a restClient, Supported as
     * @param method
     * @param uri
     * @param headers
     * @param body
     * @return
     * @throws Exception
     */
    public static Future<HttpResponse> httpRequest(String method, String uri,
            Map<String, String> headers, HttpEntity entity)
            throws Exception {
        HttpEntityEnclosingRequestBase request;
        switch (method) {
        case HttpGet.METHOD_NAME:
            return httpGETRequest(uri, headers);
        case HttpPost.METHOD_NAME:
            request = new HttpPost(uri);
            break;
        case HttpPut.METHOD_NAME:
            request = new HttpPut(uri);
            break;
        case HttpDelete.METHOD_NAME:
            return httpDeleteRequest(uri, headers);
        default:
            throw new Exception(Status.NOT_IMPLEMENTED.toString());
        }

        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }

        if (null != entity) {
            request.setEntity((HttpEntity) entity);
        }
        return httpClient.execute(request, null);
    }
        /**
         * To Handle all the MP Put Requests for a restClient
         * @param uri
         * @param body
         * @param headers
         * @return
         * @throws IOException
         * @throws ClientProtocolException
         */
        public static HttpResponse httpMultiPartPutRequest(String uri,
                Map<String, String> headers, HttpEntity entity)
                throws ClientProtocolException, IOException {
            HttpPut request;
            // Creating the PUT request and execute it.
            request = new HttpPut(uri);

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }
            if (null != entity) {
                request.setEntity((HttpEntity) entity);
            }
            return httpMultiPartClient.execute(request);
        }

        /**
         * To Handle all the MP Post Requests for a restClient
         * @param uri
         * @param body
         * @param headers
         * @return
         * @throws IOException
         * @throws ClientProtocolException
         */
        public static HttpResponse httpMultiPartPostRequest(String uri,
                Map<String, String> headers, HttpEntity entity)
                throws ClientProtocolException, IOException {
            HttpPost request;
            // Creating the PUT request and execute it.
            request = new HttpPost(uri);

            if (headers != null) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    request.addHeader(header.getKey(), header.getValue());
                }
            }
            if (null != entity) {
                request.setEntity((HttpEntity) entity);
            }
            return httpMultiPartClient.execute(request);
        }

    public static SSLContext getTrustAllContext() {
        if (trustAllContext == null) {
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, newTrustManagers(), new SecureRandom());
                trustAllContext = sc;
            } catch (Exception e) {
                _log.error(
                        "Unable to register SSL TrustManager to trust all SSL Certificates", e);
            }
        }
        return trustAllContext;
    }

    private static TrustManager[] newTrustManagers() {
        return new TrustManager[] { new AllTrustManager() };
    }

    public static NullHostNameVerifier getNullHostnameVerifier() {
        if (nullHostnameVerifier == null) {
            nullHostnameVerifier = new NullHostNameVerifier();
        }
        return nullHostnameVerifier;
    }
}

class AllTrustManager implements X509TrustManager {

    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return null;
    }

    public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
            String authType) {
    }

    public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
            String authType) {
    }
}

class NullHostNameVerifier implements HostnameVerifier {
    public boolean verify(String arg0, SSLSession arg1) {
        return true;
    }
}