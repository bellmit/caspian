package com.emc.caspian.ccs.account.controller;

import com.emc.caspian.ccs.account.controller.AccountProtocol.Status;
import com.emc.caspian.ccs.account.model.ErrorCode;

public class ErrorCodeToProtocolStatusMapper {

  public static Status convert(ErrorCode errorCode) {

    switch (errorCode) {

      case DB_RECORD_NOT_FOUND:
        return Status.ERROR_NOT_FOUND;

      case DB_RECORD_DUPLICATE:
        return Status.ERROR_CONFLICT;

      case DB_RECORD_CONSTRAINT_VIOLATION:
      case DB_REQUEST_PRE_CONDITION_FAILED:
        return Status.PRECONDITION_FAILED;

      case DB_SYNTAX_ERROR:
      case DB_CONNECTION_ERROR:
      case DB_INTERNAL_ERROR:
        return Status.ERROR_INTERNAL;

      case DB_REQUEST_ERROR:
        return Status.ERROR_BAD_REQUEST;

      default:
        return Status.ERROR_UNKNOWN;

    }
  }
 
  public static Status convertJobError(com.emc.caspian.ccs.workflow.model.ErrorCode errorCode) {

    switch (errorCode) {

      case DB_RECORD_NOT_FOUND:
        return Status.ERROR_NOT_FOUND;

      case DB_RECORD_DUPLICATE:
        return Status.ERROR_CONFLICT;

      case DB_RECORD_CONSTRAINT_VIOLATION:
        return Status.PRECONDITION_FAILED;

      case DB_SYNTAX_ERROR:
      case DB_CONNECTION_ERROR:
      case DB_INTERNAL_ERROR:
        return Status.ERROR_INTERNAL;

      default:
        return Status.ERROR_UNKNOWN;

    }
  }
}
