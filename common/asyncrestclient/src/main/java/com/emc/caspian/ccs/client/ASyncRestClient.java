package com.emc.caspian.ccs.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.Consts;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.response.ClientResponse;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author bhandp2
 * */
public class ASyncRestClient {

  private static final Logger _log = LoggerFactory.getLogger(ASyncRestClient.class);
  private ClientConfig config;
  private CloseableHttpAsyncClient client;
  private URI apiBaseUri;
  public static final String ACCEPT = "accept";
  ObjectMapper MAPPER_DEFAULT;


  public ASyncRestClient(URI baseUri, ClientConfig config) {

    this.config = config;
    this.apiBaseUri = baseUri;
    
    if (config.getSocketFactory() != null) {
      HttpsURLConnection.setDefaultSSLSocketFactory(config.getSocketFactory());
    }
    if (config.getHostnameVerifier() != null) {
      HttpsURLConnection.setDefaultHostnameVerifier(config.getHostnameVerifier());
    }

    MAPPER_DEFAULT = new ObjectMapper();
    MAPPER_DEFAULT.setSerializationInclusion(Include.NON_NULL);
    MAPPER_DEFAULT.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    MAPPER_DEFAULT.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER_DEFAULT.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    MAPPER_DEFAULT.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
  }
  
  public ASyncRestClient(ClientConfig config) {
    this(URI.create(String.format("%s://%s:%s", config.getProtocol(), config.getHost(), config.getPort())), config);
  }
  
  public ClientConfig getConfig() {
    return config;
  }


  public CloseableHttpAsyncClient getClient() {

    if (client == null) {

      CloseableHttpAsyncClient c = null;

      RequestConfig requestConfig =
          RequestConfig.custom().setRedirectsEnabled(false)
              .setConnectTimeout(config.getConnectionTimeout()).build();


      if (config.isIgnoreSSLCertificates()) {
        c =
            HttpAsyncClients.custom()
                .setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
                .setSSLContext(SSLUtil.getTrustAllContext()).setDefaultRequestConfig(requestConfig)
                .build();

      } else {
        c = HttpAsyncClients.custom().setDefaultRequestConfig(requestConfig).build();
      }

      // TODO work on defining the object mappers for json request and response
      
      client = c;
      client.start();
    }
    return client;

  }
  
  public UriBuilder uriBuilder() {
    return UriBuilder.fromUri(apiBaseUri);
  }

  public UriBuilder uriBuilder(String path) {
    return uriBuilder().path(path);
  }

  public URI getURI(String path, QueryParams queryParams, Object... args) {
    UriBuilder uriBuilder = uriBuilder(path);
    if (queryParams != null) {
      for (Entry<String, String> keyval : queryParams.getQueryParams().entrySet()) {
        uriBuilder.queryParam(keyval.getKey(), keyval.getValue());
      }
    }
    URI uri = uriBuilder.build(args);
    return uri;
  }

  public URI getURI(String path, Object... args) {
    URI uri = uriBuilder(path).build(args);
    return uri;
  }

  public <T> Future<ClientResponse<T>> get(Class<T> responseBodyType, String path, Map<String, String> requestHeader, QueryParams queryParams,
      ClientResponseCallback callback, Object... args) {
    return process(responseBodyType, MethodType.GET, requestHeader, null, queryParams, path, callback, args);

  }
  
  public <T> Future<ClientResponse<T>> get(Class<T> responseBodyType, String path, Map<String, String> requestHeader, ClientResponseCallback callback,
      Object... args) {
    return process(responseBodyType, MethodType.GET, requestHeader, null, null, path, callback, args);

  }

  public <T> Future<ClientResponse<T>> post(Class<T> responseBodyType, String path, Map<String, String> requestHeader, Object request,
      ClientResponseCallback<T> callback, QueryParams queryParams, Object... args)
      throws JsonProcessingException {
    String reqBody = null;
    if(null!= request){
      reqBody = MAPPER_DEFAULT.writeValueAsString(request);
    }
    return process(responseBodyType, MethodType.POST, requestHeader, reqBody, queryParams, path, callback, args);
  }

  public <T> Future<ClientResponse<T>> post(Class<T> responseBodyType, String path, Map<String, String> requestHeader, Object request,
      ClientResponseCallback callback, Object... args)
      throws JsonProcessingException {
    String reqBody = null;
    if(null!= request){
      reqBody = MAPPER_DEFAULT.writeValueAsString(request);
    }
    return process(responseBodyType, MethodType.POST, requestHeader, reqBody, null, path, callback, args);
  }
  
  public <T> Future<ClientResponse<T>> put(Class<T> responseBodyType, String path, Map<String, String> requestHeader, Object request,
      ClientResponseCallback callback, Object... args) throws JsonProcessingException {
    String reqBody = null;
    if(null!= request){
      reqBody = MAPPER_DEFAULT.writeValueAsString(request);
    }
    return process(responseBodyType, MethodType.PUT, requestHeader, reqBody, null, path, callback, args);
  }

  public <T> Future<ClientResponse<T>> put(Class<T> responseBodyType, String path, Map<String, String> requestHeader, ClientResponseCallback callback,
      Object... args) {
    return process(responseBodyType, MethodType.PUT, requestHeader, null, null, path, callback, args);
  }
  
  public <T> Future<ClientResponse<T>> delete(Class<T> responseBodyType, String path, Map<String, String> requestHeader, ClientResponseCallback callback,
      Object... args) {
    return process(responseBodyType, MethodType.DELETE, requestHeader, null, null, path, callback, args);
  }
  
  public <T> Future<ClientResponse<T>> head(Class<T> responseBodyType, String path, Map<String, String> requestHeader, ClientResponseCallback callback,
      Object... args) {
    return process(responseBodyType, MethodType.HEAD, requestHeader, null, null, path, callback, args);
  }
  
  public <T> Future<ClientResponse<T>> patch(Class<T> responseBodyType, String path, Map<String, String> requestHeader, Object request, ClientResponseCallback callback,
      Object... args) throws JsonProcessingException {
    String reqBody = null;
    if(null!= request){
      reqBody = MAPPER_DEFAULT.writeValueAsString(request);
    }
    return process(responseBodyType, MethodType.PATCH, requestHeader, reqBody, null, path, callback, args);
  }
  
  private HttpMessage addHeader(Map<String, String> headers, HttpMessage httpRequest) {

    for (Map.Entry<String, String> header : headers.entrySet()) {
      httpRequest.addHeader(header.getKey(), header.getValue());
    }

    return httpRequest;
  }

  public <T> Future<ClientResponse<T>> process(Class<T> responseBodyType, MethodType method, Map<String, String> requestHeader, String requestBody,
      QueryParams queryParams, String path, final ClientResponseCallback callback, Object... args) {

    URI resourceURI = null;
    Future<HttpResponse> response = null;
    if (queryParams != null) {
      resourceURI = getURI(path, queryParams, args);
    } else {
      resourceURI = getURI(path, args);
    }
    
    HttpUriRequest httpRequest = null;
    try {
      switch (method) {

        case GET:
          httpRequest = new HttpGet(resourceURI);
          break;

        case POST:
          HttpPost httpPostRequest = new HttpPost(resourceURI);
          if (null != requestBody) {
            StringEntity entity = new StringEntity(requestBody, ContentType.create(config.getMediaType(), Consts.UTF_8));
            httpPostRequest.setEntity(entity);
          }
          httpRequest = httpPostRequest;
          break;

        case PUT:
          HttpPut httpPutRequest = new HttpPut(resourceURI);
          if (null != requestBody) {
            StringEntity entity = new StringEntity(requestBody);
            httpPutRequest.setEntity(entity);
          }
          httpRequest = httpPutRequest;
          break;

        case DELETE:
          httpRequest = new HttpDelete(resourceURI);
          break;

        case PATCH:
          HttpPatch httpPatchRequest = new HttpPatch(resourceURI);
          if (null != requestBody) {
            StringEntity entity = new StringEntity(requestBody, ContentType.create(config.getMediaType(), Consts.UTF_8));
            httpPatchRequest.setEntity(entity);
          }
         httpRequest = httpPatchRequest;
         break;

        case HEAD:
          httpRequest = new HttpHead(resourceURI);
          break;
      }

      if (null != requestHeader) {
        addHeader(requestHeader, httpRequest);
      }

      httpRequest.addHeader(ACCEPT, config.getMediaType());
      httpRequest.addHeader(HTTP.CONTENT_TYPE, config.getMediaType());
      ResponseHandler handler = new ResponseHandler(httpRequest, callback, responseBodyType);
      response = getClient().execute(httpRequest, null, handler);
      
      Future<ClientResponse<T>> restClientFuture = new RestClientFuture<T>(response, responseBodyType);
      
      _log.debug("Sent Request {} on {}", new Object[]{httpRequest.getMethod(), httpRequest.getURI()});
      return restClientFuture;
    } catch (UnsupportedEncodingException unspportedExec) {
      _log.warn("Unsupported Exception {}", unspportedExec);
      return null;
    }
  }


  class ResponseHandler<T> implements FutureCallback<HttpResponse> {

    HttpRequest httpRequest;
    ClientResponseCallback callback;
    Class responseBodyType;

    public <T> ResponseHandler(HttpRequest httpRequest, ClientResponseCallback callback,
        Class<T> responseBodyType) {
      this.httpRequest = httpRequest;
      this.callback = callback;
      this.responseBodyType = responseBodyType;
    }

    @Override
    public void completed(HttpResponse response) {
      _log.debug(httpRequest.getRequestLine() + "->" + response.getStatusLine());
      try {
        if (null != callback) {
          ClientResponse<T> clientResponse = DeserializationUtil.createClientResponse(response, responseBodyType);

          _log.debug("Callback completed for {} with status code", new Object[] {
              response.getStatusLine().getReasonPhrase(), response.getStatusLine().getStatusCode()});

          callback.completed(clientResponse);
        }
      } catch (ParseException | IOException e) {
        _log.warn("Creation of Client Response Failed {}", e);
        callback.failed(e);
      }
    }

    @Override
    public void failed(Exception ex) {
      _log.warn(httpRequest.getRequestLine() + "->" + ex);
      if (null != callback) {
        callback.failed(ex);
      }

    }

    @Override
    public void cancelled() {
      _log.warn(httpRequest.getRequestLine() + " cancelled");
      if (null != callback) {
        callback.cancelled();
      }

    }
  }

}