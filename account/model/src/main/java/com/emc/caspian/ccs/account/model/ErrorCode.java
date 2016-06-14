/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.account.model;

public enum ErrorCode {

  DB_RECORD_NOT_FOUND,
  DB_RECORD_DUPLICATE,
  DB_RECORD_CONSTRAINT_VIOLATION,
  DB_SYNTAX_ERROR,
  DB_CONNECTION_ERROR,
  DB_INTERNAL_ERROR, 
  DB_REQUEST_ERROR,
  DB_REQUEST_PRE_CONDITION_FAILED
}
