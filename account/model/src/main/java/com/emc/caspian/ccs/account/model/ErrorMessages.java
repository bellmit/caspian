/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.account.model;

public class ErrorMessages {

  public static final String ACCOUNT_NOT_FOUND = "Account %s not found";
  public static final String ACCOUNT_WORKFLOW_TASK_NOT_FOUND = "Task %s associated with account not found";
  public static final String DOMAIN_NOT_FOUND = "Domain %s not found ";
  public static final String PRIMARY_DOMAIN_NOT_FOUND = "Primary domain not found ";

  public static final String ACCOUNT_ID_EMPTY = "Account ID cannot be empty";
  public static final String ACCOUNT_NAME_EMPTY = "Account name cannot be empty";

  public static final String DOMAIN_ID_EMPTY = "Domain ID cannot be empty";
  public static final String DOMAIN_NAME_EMPTY = "Domain name cannot be empty";
  public static final String DOMAIN_NOT_MAPPED_TO_ANY_ACCOUNT = "Domain %s is not mapped to any account";
  public static final String PRIMARY_DOMAIN_ID = "Primary domain ID for account cannot be empty";

  public static final String DB_INTERNAL_ERROR = "Internal error";

  public static final String DB_RECORD_CONSTRAINT_VIOLATION =
      "An error occurred while deleting %s, record constraint violation";

  public static final String DB_RECORD_NOT_FOUND = "DB record not found";

  public static final String DOMAIN_INSERT_CONFLICT = "Conflict occurred, duplicate entry";
  public static final String ACCOUNT_INSERT_ERROR = "Conflict occurred, duplicate entry";

  public static final String IDP_TASK_NOT_FOUND = "IDP Task %s associated with IDP %s not found";
  public static final String IDP_NOT_FOUND = "IDP %s not found";

  public static final String IDP_INSERT_ERROR = "Conflict occurred, duplicate entry";
  public static final String IDP_ID_EMPTY = "Idp ID cannot be empty";
  public static final String IDP_USER_EMPTY = "Idp user cannot be empty";
  public static final String IDP_PASSWORD_EMPTY = "Idp password cannot be empty";
  public static final String IDP_NOT_FOUND_IN_DATABASE = "Idp %s not found in database";

  public static final String DOMAIN_IS_PRIMARY = "Domain is primary";
  public static final String TASK_ID_EMPTY = "Task ID cannot be empty";
  public static final String TASK_NOT_MAPPED = "Task ID does not map to the specified account ID";
  public static final String ACCOUNT_NOT_FOUND_OR_DEACTIVATED = "Account %s not found or already deactivated";

  public static final String ROLE_ID_EMPTY = "Role ID cannot be empty";

}
