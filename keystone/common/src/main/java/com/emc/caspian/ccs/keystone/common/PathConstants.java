package com.emc.caspian.ccs.keystone.common;

public class PathConstants {
  public static final String ID_PATH = "/{id}";
  public static final String KEY_AUTH_URL_V3 = "/v3";
  public static final String KEY_AUTH_URL_V2 = "/v2.0";
  public static final String KEY_AUTH_TOKEN_PATH = "/auth/tokens";
  public static final String CERTIFICATE = "/OS-SIMPLE-CERT/certificates";
  public static final String REVOCATION_LIST = "/tokens/revoked";
  public static final String REVOCATION_EVENTS = "/OS-REVOKE/events";
  public static final String DOMAINS = "/domains";
  public static final String USERS = "/users";
  public static final String ROLES = "/roles";
  public static final String GROUPS = "/groups";
  public static final String PROJECTS = "/projects";

  public static final String DOMAIN_ID = "/domains/{domain_id}";
  public static final String USER_ID = "/users/{user_id}";
  public static final String ROLE_ID = "/roles/{role_id}";
  public static final String ROLE_ASSIGNMENT_ID = "/role_assignments";
  public static final String GROUP_ID = "/groups/{group_id}";
  public static final String PROJECT_ID = "/projects/{project_id}";

  public static final String DOMAIN_USER_ROLES = "/domains/{domain_id}/users/{user_id}/roles";
  public static final String DOMAIN_USER_ROLE = "/domains/{domain_id}/users/{user_id}/roles/{role_id}";
  public static final String DOMAIN_GROUP_ROLES = "/domains/{domain_id}/groups/{group_id}/roles";
  public static final String DOMAIN_GROUP_ROLE = "/domains/{domain_id}/groups/{group_id}/roles/{role_id}";
  
  public static final String DOMAIN_IDP_CONFIG = "/domains/{domain_id}/config";
  
  public static final String USERS_GROUPS = "/users/{user_id}/groups";

  public static final String GROUPS_USERS = "/groups/{group_id}/users";
  public static final String GROUPS_USER = "/groups/{group_id}/users/{user_id}";

  public static final String KEY_AUTH_TOKEN_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.KEY_AUTH_TOKEN_PATH;

  public static final String KEYSTONE_DOMAIN_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.DOMAINS;

  public static final String KEYSTONE_DOMAIN_IDP_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.DOMAIN_IDP_CONFIG;
  
  
  public static final String KEYSTONE_USER_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.USERS;

  public static final String KEYSTONE_DOMAIN_ID_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.DOMAIN_ID;

  public static final String KEYSTONE_ROLES_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.ROLES;

  public static final String KEYSTONE_PROJECT_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.PROJECTS;

  public static final String KEYSTONE_PROJECT_ID_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.PROJECT_ID;

  public static final String LIST_ROLES_USER_DOMAIN_V3 = PathConstants.KEY_AUTH_URL_V3
      + PathConstants.DOMAIN_USER_ROLES;

  public static final String ROLE_ASSIGNMENT_ID_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.ROLE_ASSIGNMENT_ID;

  public static final String ROLES_USER_DOMAIN_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.DOMAIN_USER_ROLE;

  public static final String LIST_ROLES_GROUP_DOMAIN_V3 = PathConstants.KEY_AUTH_URL_V3
      + PathConstants.DOMAIN_GROUP_ROLES;

  public static final String LIST_GROUPS_USER_DOMAIN_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.USERS_GROUPS;

  public static final String KEYSTONE_GROUP_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.GROUPS;

  public static final String KEYSTONE_GROUP_ID_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.GROUP_ID;

  public static final String KEYSTONE_GROUP_USER_PATH_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.GROUPS_USER;

  public static final String ROLES_DOMAIN_GROUP_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.DOMAIN_GROUP_ROLE;



  
  public static final String USERS_DOMAIN_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.USERS;
  
  public static final String GROUPS_DOMAIN_V3 = PathConstants.KEY_AUTH_URL_V3 + PathConstants.GROUPS;
  

  public static final String GROUP_ID_QUERY_PARAM = "group.id";
  public static final String ROLE_ID_QUERY_PARAM = "role.id";
  public static final String SCOPE_DOMAIN_ID_QUERY_PARAM = "scope.domain.id";
  public static final String SCOPE_PROJECT_ID_QUERY_PARAM = "scope.project.id ";
  public static final String USER_ID_QUERY_PARAM = "user.id";
  public static final String DOMAIN_ID_QUERY_PARAM = "domain_id";
  public static final String NAME_QUERY_PARAM = "name";
  public static final String ENABLED_QUERY_PARAM = "enabled";

}
