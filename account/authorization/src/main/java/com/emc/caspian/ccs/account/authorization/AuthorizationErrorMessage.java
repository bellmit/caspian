package com.emc.caspian.ccs.account.authorization;

public class AuthorizationErrorMessage {
  public static final String AUTH_MISSING_AUTH_INFO = "No authenticated user info found";
  public static final String AUTH_ACCESS_DENIED = "Access Denied";
  public static final String AUTH_ACCOUNTID_MISSING = "Request missing account id";
  public static final String AUTH_INTERNAL_ERROR = "Encountered an internal server error";
  public static final String AUTH_INSUFFICIENT_PRIVILEGE =
      "User does not have sufficient privileges to access the resource";
}
