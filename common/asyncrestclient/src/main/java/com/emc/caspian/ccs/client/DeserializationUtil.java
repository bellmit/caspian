package com.emc.caspian.ccs.client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class DeserializationUtil {

  static ObjectMapper MAPPER_DEFAULT;
  static ObjectMapper MAPPER_WRAP;

  private static final Logger _log = LoggerFactory.getLogger(DeserializationUtil.class);

  static {
    MAPPER_DEFAULT = new ObjectMapper();
    MAPPER_DEFAULT.setSerializationInclusion(Include.NON_NULL);
    MAPPER_DEFAULT.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    MAPPER_DEFAULT.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER_DEFAULT.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    MAPPER_DEFAULT.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

    MAPPER_WRAP = new ObjectMapper();
    MAPPER_WRAP.setSerializationInclusion(Include.NON_NULL);
    MAPPER_WRAP.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    MAPPER_WRAP.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    MAPPER_WRAP.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    MAPPER_WRAP.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
    MAPPER_WRAP.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

  }

  public static <T> T getResponseObject(String response, Class<T> responseType) {
    T responseObject = null;

    ObjectMapper mapper =
        responseType.getAnnotation(JsonRootName.class) == null ? MAPPER_DEFAULT : MAPPER_WRAP;
    if (null != response) {
      try {

        responseObject = mapper.readValue(response, responseType);

      } catch (JsonParseException e) {
        e.printStackTrace();
        _log.warn("JSON Parse exception occured during token deserialization {}", e);
      } catch (JsonMappingException e) {
        e.printStackTrace();
        _log.warn("JSON Mapping exception occured during token deserialization {}", e);
      } catch (IOException e) {
        e.printStackTrace();
        _log.warn("IO exception occured during token deserialization {}", e);
      }
    }
    return responseObject;
  }

  public static <T> ClientResponse<T> createClientResponse(HttpResponse response,
      Class<T> responseBodyType) throws ParseException, IOException {

    ClientResponse<T> clientResponse = null;
    HttpEntity entity = response.getEntity();
    if (isReadEntity(response.getStatusLine().getStatusCode())) {

      if (null != entity) {

        String responseStr = EntityUtils.toString(entity);
        T responseObject = null;

        if (responseStr == null) {
          _log.warn("Response body received is null after parse");
          clientResponse =
              getClientResponse(responseObject, response, ClientStatus.ERROR_MALFORMED_RESPONSE);
          return clientResponse;
        }
        
        _log.debug("Response {}", responseStr);

        if (String.class == responseBodyType) {
          responseObject = (T) responseStr;
          clientResponse = getClientResponse(responseObject, response, ClientStatus.SUCCESS);
          return clientResponse;
        }

        responseObject = DeserializationUtil.getResponseObject(responseStr, responseBodyType); 

        //checkAndAddTokenString(responseObject, response);
        if (responseObject == null) {
          _log.warn("Response body received is null after JSON parse");
          clientResponse =
              getClientResponse(responseObject, response, ClientStatus.ERROR_MALFORMED_RESPONSE);
          return clientResponse;
        }

        clientResponse = getClientResponse(responseObject, response, ClientStatus.SUCCESS);
        return clientResponse;
      }

      else {
        // some response arrived and has no body but the request is
        // success
        clientResponse = getClientResponse(null, response, ClientStatus.SUCCESS);
        return clientResponse;
      }
    } else if (204 == response.getStatusLine().getStatusCode()) {
      clientResponse = getClientResponse(null, response, ClientStatus.SUCCESS);
      return clientResponse;
    } else {
      String error = EntityUtils.toString(response.getEntity());
      _log.warn("Error {} occured during http request", error);
      clientResponse =
          getClientResponse(null, response, getClientStatus(response.getStatusLine()
              .getStatusCode()));
      clientResponse.setErrorMessage(response.getStatusLine().getReasonPhrase());

      return clientResponse;
    }
  }

  
  public static <T> ClientResponse<T> getClientResponse(T responseBody, HttpResponse response,
      ClientStatus clientStatus) {

    // TODO form the response completely
    ClientResponse<T> clientResponse = new ClientResponse<T>();
    com.emc.caspian.ccs.client.response.HttpResponse<T> httpResponse;
    httpResponse = new com.emc.caspian.ccs.client.response.HttpResponse<T>();

    if (null != responseBody) {
      httpResponse.setResponseBody(responseBody);
    }

    httpResponse.setHeaders(setHeaders(response.getAllHeaders()));

    httpResponse.setStatusCode(response.getStatusLine().getStatusCode());
    clientResponse.setHttpResponse(httpResponse);
    clientResponse.setStatus(clientStatus);
    return clientResponse;
  }

  public static Map<String, List<String>> setHeaders(Header[] headers) {

    Map<String, List<String>> headerMap = new HashMap<String, List<String>>();

    for (Header header : headers) {
      headerMap.put(header.getName(), Arrays.asList(header.getValue()));
    }
    return headerMap;
  }

  public static boolean isReadEntity(int httpStatusCode) {

    if (httpStatusCode >= 200 && httpStatusCode < 299 && httpStatusCode != 204) {
      return true;
    } else {
      return false;
    }
  }

  public static ClientStatus getClientStatus(int responseStatusCode) {

    if (responseStatusCode >= 300 && responseStatusCode <= 599) {
      return ClientStatus.ERROR_HTTP;
    }

    return ClientStatus.ERROR_UNKNOWN;
  }
}
