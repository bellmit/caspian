package com.emc.caspian.ccs.client.response;

import java.util.List;
import java.util.Map;

public class HttpResponse<T> {

  private int statusCode;

  private Map<String, List<String>> headers;

  private T responseBody;

  private String errorMessage;

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, List<String>> headers) {
    this.headers = (Map<String, List<String>>) headers;
  }

  public T getResponseBody() {
    return responseBody;
  }

  public void setResponseBody(T responseBody) {
    this.responseBody = responseBody;
  }
}
