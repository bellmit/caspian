/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.account.model.mysql;

import org.skife.jdbi.v2.exceptions.TransactionException;
import org.skife.jdbi.v2.exceptions.TransactionFailedException;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.skife.jdbi.v2.exceptions.UnableToObtainConnectionException;

import com.emc.caspian.ccs.account.model.ErrorCode;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

/**
 * Class to map specific MySQL exceptions to DB error codes
 * 
 * @author raod4
 *
 */
public class MySQLExceptionMapper {

  public static ErrorCode fetchErrorStatus(Throwable exception) {

    Throwable innerException = exception.getCause();

    if (exception instanceof UnableToExecuteStatementException) {
      if (innerException instanceof MySQLSyntaxErrorException) {
        return ErrorCode.DB_SYNTAX_ERROR;
      }
      if (innerException instanceof MySQLIntegrityConstraintViolationException) {
        if (innerException.getMessage().contains("Duplicate entry")) {
          return ErrorCode.DB_RECORD_DUPLICATE;
        } else {
          return ErrorCode.DB_RECORD_CONSTRAINT_VIOLATION;
        }
      }
    } else if (exception instanceof UnableToObtainConnectionException) {
      return ErrorCode.DB_CONNECTION_ERROR;
    } else if (exception instanceof TransactionException) {
      return ErrorCode.DB_INTERNAL_ERROR;
    } else if (exception instanceof TransactionFailedException) {
      return ErrorCode.DB_INTERNAL_ERROR;
    }

    return ErrorCode.DB_INTERNAL_ERROR;
  }
}
