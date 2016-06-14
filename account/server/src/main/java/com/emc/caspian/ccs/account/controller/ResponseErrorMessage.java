package com.emc.caspian.ccs.account.controller;

public class ResponseErrorMessage {

  public static final String ACCOUNT_DOMAIN_CREATION_FAILED = "Account creation failed, duplicate entry for account or domain";
  public static final String METHOD_NOT_IMPLEMENTED = "The method is not implemented";
  public static final String INTERNAL_ERROR = "An internal error occurred";
  public static final String INVALID_JSON = "Invalid JSON";
  
  public static final String ACCOUNT_NOT_FOUND = "Account does not exist";
  public static final String ACCOUNT_NOT_ACTIVE = "Account is not active";
  public static final String ACCOUNT_ALREADY_EXISTS = "Account with this name already exists";
  public static final String DOMAIN_ALREADY_EXISTS = "Domain with this name already exists";
  public static final String DOMAIN_ASSOCIATED_WITH_ANOTHER_ACCOUNT = "Domain is already associated with another account";
  public static final String DOMAIN_ALREADY_ASSOCIATED = "Domain is already associated with this account";
  public static final String CANT_DELETE_ENABLED_DOMAIN = "Cannot delete an enabled domain";

  public static final String DOMAIN_NOT_FOUND = "Domain %s not found";
  public static final String DOMAIN_IDP_NOT_FOUND = "No IDP found for domain %s";
  public static final String DOMAIN_IDP_EXISTS = "Domain %s already has an IDP associated with it";
  public static final String DOMAIN_NOT_ASSOCIATED = "Domain %s is not associated with this account %s";
  public static final String USERS_GROUPS_ALREADY_EXIST = "Users/Groups already exist for this domain";
  public static final String ACCOUNT_PRIMARY_DOMAIN_EXISTS = "Account %s already has a primary domain";
  public static final String ACCOUNT_PRIMARY_DOMAIN_NOT_FOUND = "Primary domain for account %s does not exist";

  public static final String VERSION_NOT_SUPPORTED = "Version %s not supported";
  public static final String RESOURCE_NOT_FOUND= "The resource could not be found.";
  public static final String METHOD_NOT_SUPPORTED_FOR_V1= "Mutable /v1 APIs not supported during upgrade.  Move to the /v2 api";
  public static final String AUTH_INSUFFICIENT_PRIVILEGE =
      "User does not have sufficient privileges to access the resource";
  
}
