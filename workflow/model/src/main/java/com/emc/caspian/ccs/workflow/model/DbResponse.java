/*
 * Copyright (c) 2015 EMC Corporation
 *  All Rights Reserved
 *
 *  This software contains the intellectual property of EMC Corporation
 *  or is licensed to EMC Corporation from third parties.  Use of this
 *  software and the intellectual property contained therein is expressly
 *  limited to the terms and conditions of the License Agreement under which
 *  it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.workflow.model;

/**
 * Class that represents the response from a database has information on the type of object retrieved, http error codes
 * and messages
 *
 * @param <T> type of response data from the db on a transaction. Eg: List<AccountModel>, List<AccountDomainModel>
 *            boolean responses in case of updation, insertion etc
 * @author raod4
 */
public class DbResponse<T> {

  private String errorMessage;
  private ErrorCode errorCode;
  private T responseObj;


  public T getResponseObj() {
    return responseObj;
  }

  public void setResponseObj(T responseObj) {
    this.responseObj = responseObj;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public void setErrorCode(ErrorCode errorCode) {
    this.errorCode = errorCode;
  }

}
