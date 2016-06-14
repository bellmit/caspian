package com.emc.caspian.ccs.client.response;


public class ClientResponse<T> {

  HttpResponse<T> httpResponse;
  ClientStatus status;
  String errorMessage;
  
  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMsg) {
    this.errorMessage = errorMsg;
  }

  public HttpResponse<T> getHttpResponse() {
    return httpResponse;
  }
  
  public void setHttpResponse(HttpResponse<T> httpResponse) {
    this.httpResponse = httpResponse;
  }
  
  public ClientStatus getStatus() {
    return status;
  }
  
  public void setStatus(ClientStatus status) {
    this.status = status;
  }
    
}
