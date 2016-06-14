package com.emc.caspian.ccs.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("error")
class ServiceErrorRestRep {

  private int code;
  private String codeDescription;
  private String detailedMessage;
  private boolean retryable;

  /**
   * The numerical code associated with the error encountered when processing a service request
   * 
   * @valid none
   */
  @JsonProperty("code")
  public int getCode() {
    return code;
  }

  public void setCode(final int code) {
    this.code = code;
  }

  /**
   * The description of the error
   * 
   * @valid none
   */
  @JsonProperty("title")
  public String getCodeDescription() {
    return codeDescription;
  }

  public void setCodeDescription(final String codeDescription) {
    this.codeDescription = codeDescription;
  }

  /**
   * Detailed information concerning the error
   * 
   * @valid none
   */
  @JsonProperty("message")
  public String getDetailedMessage() {
    return detailedMessage;
  }

  public void setDetailedMessage(final String detailedMessage) {
    this.detailedMessage = detailedMessage;
  }

  /**
   * Indicates whether the error is retryable which means service is temporarily unavailable and the client could retry
   * after a while.
   * 
   * @valid true = it is retryable.
   * @valid false = it is not retryable.
   */
  @JsonProperty("retryable")
  public boolean isRetryable() {
    return retryable;
  }

  public void setRetryable(final boolean retryable) {
    this.retryable = retryable;
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    buffer.append("Service Code: ");
    buffer.append(this.code);
    buffer.append(", Title: ");
    buffer.append(this.codeDescription);
    buffer.append(", Details: ");
    buffer.append(this.detailedMessage);
    return buffer.toString();
  }
}
