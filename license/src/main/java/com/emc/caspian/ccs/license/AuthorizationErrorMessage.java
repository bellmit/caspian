package com.emc.caspian.ccs.license;

public class AuthorizationErrorMessage {
	public static final int AUTH_INVALID_TOKEN_CODE =3200; 
	public static final String AUTH_INVALID_TOKEN_MESSAGE = "Invalid auth token";
	
	  public static final int AUTH_MISSING_AUTH_INFO_CODE = 3201;
	  public static final String AUTH_MISSING_AUTH_INFO_MESSAGE = "No authenticated user info found";
	  
	  public static final int AUTH_ACCESS_DENIED_CODE = 3202;
	  public static final String AUTH_ACCESS_DENIED_MESSAGE = "Access Denied";
	  
	  public static final int AUTH_ACCOUNTID_MISSING_CODE = 3203;
	  public static final String AUTH_ACCOUNTID_MISSING_MESSAGE = "Request missing account id";
	  
	  public static final int AUTH_INTERNAL_ERROR_CODE = 3204;
	  public static final String AUTH_INTERNAL_ERROR_MESSAGE = "Encountered an internal server error";
	  
	  public static final int AUTH_INSUFFICIENT_PRIVILEGE_CODE =3205;
	  public static final String AUTH_INSUFFICIENT_PRIVILEGE_MESSAGE =
		      "User does not have sufficient privileges to access the resource";
	}
