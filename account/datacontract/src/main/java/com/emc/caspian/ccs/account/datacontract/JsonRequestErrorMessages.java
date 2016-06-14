package com.emc.caspian.ccs.account.datacontract;

public class JsonRequestErrorMessages {
  
  public static final String REQUEST_DESCRIPTION_TOO_LONG= "Description too long";
  public static final String REQUEST_NAME_TOO_LONG= "Name too long";
  public static final String REQUEST_NAME_EMPTY= "Name cannot be empty";
  public static final String REQUEST_ACCOUNT_ID_EMPTY = "Account ID cannot be empty";
  public static final String REQUEST_DOMAIN_ID_EMPTY = "Domain ID cannot be empty";
  public static final String REQUEST_DOMAIN_NAME_EMPTY = "Domain name cannot be empty";
  public static final String REQUEST_IDP_TYPE_EMPTY = "Idp type cannot be empty";
  public static final String REQUEST_IDP_NAME_EMPTY = "Idp name cannot be empty";
  // the below String will be used when there is no minimum length for the field and can be empty
  public static final String REQUEST_MIN_LENGTH_VALIDATION = null;
}
