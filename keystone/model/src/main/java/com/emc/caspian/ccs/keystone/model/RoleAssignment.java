package com.emc.caspian.ccs.keystone.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Created by gulavb on 4/24/2015.
 */
@JsonRootName("role_assignment")
public class RoleAssignment {
  
  public RoleAssignment() {
    role = new Role();
    scope = new Scope();
    user = new User();
    group = new Group();
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Role {
    private String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Domain {
    private String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Project {
    private String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Scope {

    @JsonProperty("domain")
    private Domain domain;

    @JsonProperty("project")
    private Project project;

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class User {
    private String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Group {
    private String id;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }
  }

  @JsonProperty("role")
  private Role role;

  @JsonProperty("scope")
  private Scope scope;

  @JsonProperty("user")
  private User user;

  @JsonProperty("group")
  private Group group;
}
