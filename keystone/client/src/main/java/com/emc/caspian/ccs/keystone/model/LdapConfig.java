package com.emc.caspian.ccs.keystone.model;

import com.emc.caspian.ccs.keystone.model.QueryScope;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class LdapConfig {
  private String url;
  private String user;
  private String password;
  private String userTreeDn;
  private String groupTreeDn;
  private String userFilter;
  private String groupFilter;
  private String userObjectClass;
  private String groupObjectClass;
  private String userNameAttribute;
  private String groupNameAttribute;
  private QueryScope queryScope;
  
  public LdapConfig() {
    
  }
  
  @JsonProperty("url")
  public String getUrl() {
    return url;
  }
  
  @JsonProperty("url")
  public void setUrl(String url) {
    this.url = url;
  }
  
  @JsonProperty("user")
  public String getUser() {
    return user;
  }
  
  @JsonProperty("user")
  public void setUser(String user) {
    this.user = user;
  }
  
  @JsonProperty("password")
  public String getPassword() {
    return password;
  }
  
  @JsonProperty("password")
  public void setPassword(String password) {
    this.password = password;
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
  
  @JsonProperty("user_objectclass")
  public String getUserObjectClass() {
    return userObjectClass;
  }
  
  @JsonProperty("user_objectclass")
  public void setUserObjectClass(String userObjectClass) {
    this.userObjectClass = userObjectClass;
  }
  
  @JsonProperty("group_objectclass")
  public String getGroupObjectClass() {
    return groupObjectClass;
  }
  
  @JsonProperty("group_objectclass")
  public void setGroupObjectClass(String groupObjectClass) {
    this.groupObjectClass = groupObjectClass;
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
}
