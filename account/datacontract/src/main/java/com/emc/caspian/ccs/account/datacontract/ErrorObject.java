package com.emc.caspian.ccs.account.datacontract;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorObject {

  private ErrorMessage error;


  public ErrorObject() {
    this.error = null;
  }

  public ErrorObject(ErrorMessage error) {
    this.error = error;
  }

  @JsonProperty
  public ErrorMessage getError() {
    return error;
  }

  @JsonProperty
  public void setError(ErrorMessage error) {
    this.error = error;
  }



  public class ErrorMessage {

    private String message;
    private int code;
    private String title;
    private String timestamp;
    private String requestId;

    @JsonProperty
    public String getMessage() {
      return message;
    }

    @JsonProperty
    public void setMessage(String message) {
      this.message = message;
    }

    @JsonProperty
    public int getCode() {
      return code;
    }

    @JsonProperty
    public void setCode(int code) {
      this.code = code;
    }

    @JsonProperty
    public String getTitle() {
      return title;
    }

    @JsonProperty
    public void setTitle(String title) {
      this.title = title;
    }

    @JsonProperty
    public String getTimestamp() {
      return timestamp;
    }

    @JsonProperty
    public void setTimestamp(String timestamp) {
      this.timestamp = timestamp;
    }

    @JsonProperty
    public String getRequestId() {
      return requestId;
    }

    @JsonProperty
    public void setRequestId(String requestId) {
      this.requestId = requestId;
    }


  }

}
