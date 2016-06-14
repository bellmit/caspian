package com.emc.caspian.ccs.account.datacontract;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.emc.caspian.ccs.account.types.QueryScope;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class IdpConfig {
  // Default values for LDAP/AD
  public IdpConfig() {
    this.url = null;
    this.userBindDn = null;
    this.userBindPwd = null;
    this.userTreeDn = null;
    this.groupTreeDn = null;
    this.userFilter = "";
    this.groupFilter = "";
    this.userClassName = "person";
    this.groupClassName = "group";
    this.userNameAttribute = "samAccountName";
    this.groupNameAttribute = "name";
    this.queryScope = QueryScope.valueOf("one");
  }

  @JsonProperty("url")
  public String getUrl() {
    return url;
  }

  @JsonProperty("url")
  public void setUrl(String url) {
    this.url = url;
  }

  @JsonProperty("user_bind_dn")
  public String getUserBindDn() {
    return userBindDn;
  }

  @JsonProperty("user_bind_dn")
  public void setUserBindDn(String userBindDn) {
    this.userBindDn = userBindDn;
  }

  @JsonProperty("user_bind_pwd")
  public String getUserBindPwd() {
    return userBindPwd;
  }

  @JsonProperty("user_bind_pwd")
  public void setUserBindPwd(String userBindPwd) {
    this.userBindPwd = userBindPwd;
  }

  @JsonProperty("user_tree_dn")
  public String getUserTreeDn() {
    return userTreeDn;
  }

  @JsonProperty("user_tree_dn")
  public void setUserTreeDn(String userTreeDn) {
    this.userTreeDn = userTreeDn;
  }

  @JsonProperty("group_tree_dn")
  public String getGroupTreeDn() {
    return groupTreeDn;
  }

  @JsonProperty("group_tree_dn")
  public void setGroupTreeDn(String groupTreeDn) {
    this.groupTreeDn = groupTreeDn;
  }

  @JsonProperty("user_filter")
  public String getUserFilter() {
    return userFilter;
  }

  @JsonProperty("user_filter")
  public void setUserFilter(String userFilter) {
    this.userFilter = userFilter;
  }

  @JsonProperty("group_filter")
  public String getGroupFilter() {
    return groupFilter;
  }

  @JsonProperty("group_filter")
  public void setGroupFilter(String groupFilter) {
    this.groupFilter = groupFilter;
  }

  @JsonProperty("user_class_name")
  public String getUserClassName() {
    return userClassName;
  }

  @JsonProperty("user_class_name")
  public void setUserClassName(String userClassName) {
    this.userClassName = userClassName;
  }

  @JsonProperty("group_class_name")
  public String getGroupClassName() {
    return groupClassName;
  }

  @JsonProperty("group_class_name")
  public void setGroupClassName(String groupClassName) {
    this.groupClassName = groupClassName;
  }

  @JsonProperty("user_name_attribute")
  public String getUserNameAttribute() {
    return userNameAttribute;
  }

  @JsonProperty("user_name_attribute")
  public void setUserNameAttribute(String userNameAttribute) {
    this.userNameAttribute = userNameAttribute;
  }

  @JsonProperty("group_name_attribute")
  public String getGroupNameAttribute() {
    return groupNameAttribute;
  }

  @JsonProperty("group_name_attribute")
  public void setGroupNameAttribute(String groupNameAttribute) {
    this.groupNameAttribute = groupNameAttribute;
  }

  @JsonProperty("query_scope")
  public QueryScope getQueryScope() {
    return queryScope;
  }

  @JsonProperty("query_scope")
  public void setQueryScope(QueryScope queryScope) {
    this.queryScope = queryScope;
  }

  public void validate() {
    boolean invalid = false;
    List<String> missingParams =  new ArrayList<String>();
    
    if (StringUtils.isEmpty(url)) {
      missingParams.add(PARAMS_URL);
      invalid = true;
    }
    if (StringUtils.isEmpty(userBindDn)) {
      missingParams.add(PARAMS_USER_BINDDN);
      invalid = true;
    }
    if (StringUtils.isEmpty(userBindPwd)) {
      missingParams.add(PARAMS_USER_PWD);
      invalid = true;
    }
    if (StringUtils.isEmpty(userTreeDn)) {
      missingParams.add(PARAMS_USER_TREE_DN);
      invalid = true;
    }
    if (StringUtils.isEmpty(groupTreeDn)) {
      missingParams.add(PARAMS_GROUP_TREE_DN);
      invalid = true;
    }
    if (StringUtils.isEmpty(userNameAttribute)) {
      missingParams.add(PARAMS_USER_NAME_ATTR);
      invalid = true;
    }
    if (StringUtils.isEmpty(groupNameAttribute)) {
      missingParams.add(PARAMS_GROUP_NAME_ATTR);
      invalid = true;
    }
    if (invalid) {
      throw new IllegalArgumentException(String.format(IDP_INVALID_CONFIG, StringUtils.join(missingParams, ",")));
    }
  }

  private String url;
  private String userBindDn;
  private String userBindPwd;
  private String userTreeDn;
  private String groupTreeDn;
  private String userFilter;
  private String groupFilter;
  private String userClassName;
  private String groupClassName;
  private String userNameAttribute;
  private String groupNameAttribute;
  private QueryScope queryScope;
  private static final String PARAMS_URL = "url";
  private static final String PARAMS_USER_BINDDN = "user_bind_dn";
  private static final String PARAMS_USER_PWD = "user_bind_pwd";
  private static final String PARAMS_USER_TREE_DN = "user_tree_dn";
  private static final String PARAMS_GROUP_TREE_DN = "group_tree_dn";
  private static final String PARAMS_USER_NAME_ATTR = "user_name_attribute";
  private static final String PARAMS_GROUP_NAME_ATTR = "group_name_attribute";
  public static final String IDP_INVALID_CONFIG = "Insufficient parameters. Mandatory attribute(s) [%s] are missing";
}
