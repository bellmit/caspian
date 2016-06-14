package com.emc.caspian.ccs.client;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.*;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ContextResolver;

import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.client.response.HttpResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.filter.LoggingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestClient {

  private ClientConfig config;
  private URI apiBaseUri;


  private Client client;

  private static final Logger _log = LoggerFactory.getLogger(RestClient.class);

  public RestClient(URI baseUri, ClientConfig config) {
    this.apiBaseUri = baseUri;
    this.config = config;
    if (config.getSocketFactory() != null) {
      HttpsURLConnection.setDefaultSSLSocketFactory(config.getSocketFactory());
    }
    if (config.getHostnameVerifier() != null) {
      HttpsURLConnection.setDefaultHostnameVerifier(config.getHostnameVerifier());
    }
  }
  
  public RestClient(ClientConfig config) {
    this(URI.create(String.format("%s://%s:%s", config.getProtocol(), config.getHost(),
        config.getPort())), config);
  }


  public ClientConfig getConfig() {
    return config;
  }
  

  public Client getClient() {
    if (client == null) {

      Client c = null;

      if (config.isIgnoreSSLCertificates()) {
        c =
            ClientBuilder.newBuilder().sslContext(SSLUtil.getTrustAllContext())
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .hostnameVerifier(SSLUtil.getNullHostnameVerifier()).build();

      } else {
        c = ClientBuilder.newBuilder().build();
      }

      final ObjectMapper MAPPER_DEFAULT = new ObjectMapper();
      MAPPER_DEFAULT.setSerializationInclusion(Include.NON_NULL);
      MAPPER_DEFAULT.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
      MAPPER_DEFAULT.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      MAPPER_DEFAULT.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
      MAPPER_DEFAULT.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

      final ObjectMapper MAPPER_WRAP = new ObjectMapper();
      MAPPER_WRAP.setSerializationInclusion(Include.NON_NULL);
      MAPPER_WRAP.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
      MAPPER_WRAP.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
      MAPPER_WRAP.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
      MAPPER_WRAP.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
      MAPPER_WRAP.configure(SerializationFeature.WRAP_ROOT_VALUE, true);


      c.register(new ContextResolver<ObjectMapper>() {

        public ObjectMapper getContext(Class<?> type) {
          return type.getAnnotation(JsonRootName.class) == null ? MAPPER_DEFAULT : MAPPER_WRAP;
        }

      });

      // Set timeouts based on the configuration
      c.property(ClientProperties.CONNECT_TIMEOUT, config.getConnectionTimeout());
      c.property(ClientProperties.READ_TIMEOUT, config.getReadTimeout());
      c.property(ClientProperties.FOLLOW_REDIRECTS, false);
      c.property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);

      if(_log.isDebugEnabled()) {
        c.register(new LoggingFilter());
      }
      client = c;
    }

    return client;
  }

  public UriBuilder uriBuilder() {
    return UriBuilder.fromUri(apiBaseUri);
  }

  public UriBuilder uriBuilder(String path) {
    return uriBuilder().path(path);
  }


  public WebTarget resource(String path, QueryParams queryParams, Object... args) {
    UriBuilder uriBuilder = uriBuilder(path);
    if (queryParams != null) {
      for (Entry<String, String> keyval : queryParams.getQueryParams().entrySet()) {
        uriBuilder.queryParam(keyval.getKey(), keyval.getValue());
      }
    }
    URI uri = uriBuilder.build(args);
    return resource(uri);
  }

  public WebTarget resource(String path, Object... args) {
    URI uri = uriBuilder(path).build(args);
    return resource(uri);
  }

  public WebTarget resource(URI uri) {
    WebTarget resource = getClient().target(uri);
    return resource;
  }

  public <T> ClientResponse<T> put(Class<T> responseBodyType, Object request, String path,
      Map<String, Object> requestHeader, Object... args) {

    ClientResponse<T> clientResponse =
        process(MethodType.PUT, responseBodyType, requestHeader, request, null, path, args);
    _log.debug("Received response status : {} for PUT", clientResponse.getStatus());
      return clientResponse;   
  }

  public <T> ClientResponse<T> put(Class<T> responseBodyType, String path,
      Map<String, Object> requestHeader, Object... args) {
    
    ClientResponse<T> clientResponse =
        process(MethodType.PUT, responseBodyType, requestHeader, null, null, path, args);
    _log.debug("Received response status : {} for PUT", clientResponse.getStatus());
    return clientResponse;
  }

  public <T> ClientResponse<T> post(Class<T> responseBodyType, Object request, String path,
      Object... args) {
    ClientResponse<T> clientResponse =
        process(MethodType.POST, responseBodyType, null, request, null, path, args);
    _log.debug("Received response status : {} for POST", clientResponse.getStatus());
    return clientResponse;
  }

  public <T> ClientResponse<T> post(Class<T> responseBodyType, Object request, String path,
      Map<String, Object> requestHeader, Object... args) {

    ClientResponse<T> clientResponse =
        process(MethodType.POST, responseBodyType, requestHeader, request, null, path, args);
    _log.debug("Received response status : {} for POST", clientResponse.getStatus());
    return clientResponse;
    
  }
  public <T> ClientResponse<T> post(Class<T> responseBodyType, QueryParams queryParams, Object request,
      String path, Object... args) {

    ClientResponse<T> clientResponse =
        process(MethodType.POST, responseBodyType, null, request, queryParams, path, args);
    _log.debug("Received response status : {} for POST", clientResponse.getStatus());
    return clientResponse;

  }

  public <T> ClientResponse<T> delete(Class<T> responseBodyType, String path,
      Map<String, Object> requestHeader, Object... args) {

      ClientResponse<T> clientResponse =
          process(MethodType.DELETE, responseBodyType, requestHeader, null, null, path, args);
      _log.debug("Received response status : {} for DELETE", clientResponse.getStatus());
      return clientResponse;

  }


  public <T> ClientResponse<T> get(Class<T> responseBodyType, QueryParams queryParams, String path,
      Map<String, Object> requestHeader, Object... args) {
    
    ClientResponse<T> clientResponse =
        process(MethodType.GET, responseBodyType, requestHeader, null, queryParams, path, args);
    _log.debug("Received response status : {} for GET", clientResponse.getStatus());
    return clientResponse;

  }

  public <T> ClientResponse<T> get(Class<T> responseBodyType, String path,
      Map<String, Object> requestHeader, Object... args) {

    ClientResponse<T> clientResponse =
        process(MethodType.GET, responseBodyType, requestHeader, null, null, path, args);
    _log.debug("Received response status : {} for GET", clientResponse.getStatus());
    return clientResponse;

  }

  public <T> ClientResponse<T> get(Class<T> responseBodyType, String path, Object... args) {

    ClientResponse<T> clientResponse =
        process(MethodType.GET, responseBodyType, null, null, null, path, args);
    _log.debug("Received response status : {} for GET", clientResponse.getStatus());
    return clientResponse;

  }

  public <T> ClientResponse<T> head(Class<T> responseBodyType, String path,
      Map<String, Object> requestHeader, Object... args) {

    ClientResponse<T> clientResponse =
        process(MethodType.HEAD, responseBodyType, requestHeader, null, null, path, args);
    _log.debug("Received response status : {} for HEAD", clientResponse.getStatus());
    return clientResponse;

  }

  public <T> ClientResponse<T> patch(Class<T> responseBodyType, Object request, String path,
      Map<String, Object> requestHeader, Object... args) {

    ClientResponse<T> clientResponse =
        process(MethodType.PATCH, responseBodyType, requestHeader, request, null, path, args);
    _log.debug("Received response status : {} for PATCH", clientResponse.getStatus());
    return clientResponse;

  }

  public <T> ClientResponse<T> process(MethodType method, Class<T> responseBodyType,
      Map<String, Object> requestHeader, Object requestBody, QueryParams queryParams, String path,
      Object... args) {

    WebTarget resource = null;
    Response response = null;
    ClientResponse<T> clientResponse = null;
    if (queryParams != null) {
      resource = resource(path, queryParams, args);
    } else {
      resource = resource(path, args);
    }

    Invocation.Builder builder =
        resource.request(config.getMediaType());

    if (null != requestHeader) {
      for (Entry<String, Object> header : requestHeader.entrySet()) {
        builder.header(header.getKey(), header.getValue().toString());
      }
    }
    try {
      switch (method) {

        case GET:
          response = builder.get();
          break;

        case POST:
          response = builder.post(Entity.json(requestBody));
          break;

        case PUT:
          if (null == requestBody) {
            response = builder.put(Entity.json(null));
          } else {
            response = builder.put(Entity.json(requestBody));
          }
          break;

        case DELETE:
          response = builder.delete();
          break;

        case PATCH:
          response = builder.method("PATCH", Entity.json(requestBody), Response.class);
          break;

        case HEAD:
          response = builder.head();
          break;
      }
      clientResponse = createClientResponse(responseBodyType, response);
      return clientResponse;
    } catch (ProcessingException processingException) {
      _log.warn("Processing exception during {} method {}", method.toString(), processingException);

      clientResponse = getClientResponseFromProcessingCause(responseBodyType, processingException.getCause());

      clientResponse.getHttpResponse().setErrorMessage((processingException.getMessage()));
      clientResponse.setErrorMessage(processingException.toString());
      return clientResponse;

    } catch (WebApplicationException webApplicationException) {
      _log.warn("Web application exception during {} method {}", method.toString(), webApplicationException);
      response = webApplicationException.getResponse();
      clientResponse =
          getClientResponse(responseBodyType, null, response, getClientStatus(response.getStatus()));

      clientResponse.getHttpResponse().setErrorMessage((webApplicationException.getMessage()));
      clientResponse.setErrorMessage(webApplicationException.toString());
      return clientResponse;

    }
  }
  
  private <T> ClientResponse<T> createClientResponse(Class<T> responseBodyType, Response response) {

    ClientResponse<T> clientResponse = null;
    if (isReadEntity(response.getStatus())) {
      T responseObject = response.readEntity(responseBodyType);

      if (responseObject == null) {
        _log.warn("Response body received is null after JSON parse");
        clientResponse =
            getClientResponse(responseBodyType, responseObject, response, ClientStatus.ERROR_MALFORMED_RESPONSE);
        return clientResponse;
      }
      clientResponse = getClientResponse(responseBodyType, responseObject, response, ClientStatus.SUCCESS);
      return clientResponse;
    } else if (204 == response.getStatus()) {
      clientResponse = getClientResponse(responseBodyType, null, response, ClientStatus.SUCCESS);
      return clientResponse;
    } else {
      _log.warn("Error occured during http request. Failed with status " + response.getStatus());
      
      _log.debug("Will check if response has any data included in the error response' body");
      T responseObject = null;
      try {
          responseObject = response.readEntity(responseBodyType);
          _log.debug("Found a body in the error response. Will add to HttpResponse body");
      }catch(ProcessingException | IllegalStateException e ) {
          responseObject = null;
          _log.debug("No entity found in error response.");
          //no need to take further action
      }
      clientResponse = getClientResponse(responseBodyType, responseObject, response, getClientStatus(response.getStatus()));
      
      clientResponse.getHttpResponse().setErrorMessage(response.getStatusInfo().getReasonPhrase());
      clientResponse.setErrorMessage(response.getStatusInfo().getReasonPhrase());

      return clientResponse;
    }
  }

  public static boolean isReadEntity(int httpStatusCode) {

    if (httpStatusCode >= 200 && httpStatusCode < 299 && httpStatusCode != 204) {
      return true;
    } else {
      return false;
    }
  }

  public static <T> ClientResponse<T> getClientResponseFromProcessingCause(
      Class<T> responseBodyType, Throwable cause) {

    ClientResponse<T> clientResponse =
        getClientResponse(responseBodyType, null, null, ClientStatus.ERROR_UNKNOWN);

    if (cause instanceof ConnectException) {
      clientResponse.setStatus(ClientStatus.ERROR_SERVER_UNREACHABLE);
    } else if (cause instanceof SocketTimeoutException) {
      clientResponse.setStatus(ClientStatus.ERROR_HTTPTIMEOUT);
    }

    return clientResponse;
  }

  public static ClientStatus getClientStatus(int responseStatusCode) {

    if (responseStatusCode >= 300 && responseStatusCode <= 599) {
      return ClientStatus.ERROR_HTTP;
    }

    return ClientStatus.ERROR_UNKNOWN;
  }

  public static <T> ClientResponse<T> getClientResponse(Class<T> responseBodyType, T responseBody,
      Response response, ClientStatus clientStatus) {

    ClientResponse<T> clientResponse = new ClientResponse<T>();
    HttpResponse<T> httpResponse = new HttpResponse<T>();

    if (null != response) {
      httpResponse.setHeaders(response.getStringHeaders());
      httpResponse.setStatusCode(response.getStatus());
    }

    if (responseBody != null) {
      httpResponse.setResponseBody(responseBody);
    }

    clientResponse.setHttpResponse(httpResponse);
    clientResponse.setStatus(clientStatus);


    return clientResponse;
  }

	public static void logResponse(String request,
			ClientResponse<?> clientResponse) {
		ClientStatus clientStatus = clientResponse.getStatus();
		if (clientStatus.equals(ClientStatus.SUCCESS)) {
			_log.debug(String.format(
					"Request [%s] succeeded with status code: %s", request,
					clientResponse.getHttpResponse().getStatusCode()));
		} else if (clientStatus.equals(ClientStatus.ERROR_HTTP)) {
			_log.debug(String.format("Request [%s] failed with status code: %s",
					request, clientResponse.getHttpResponse().getStatusCode()));
		} else {
			_log.debug(String.format("Request [%s] failed with status code: %s",
					request, clientStatus.toString()));
		}
	}
}
