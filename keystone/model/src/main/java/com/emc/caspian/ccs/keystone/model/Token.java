package com.emc.caspian.ccs.keystone.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("token")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"tokenString"})
public class Token implements Serializable {

  private String id;

  private String tokenString;

  @JsonProperty("expires_at")
  private String expiresAt;

  @JsonProperty("issued_at")
  private String issuedAt;

  @JsonProperty("audit_ids")
  private List<String> auditIds;

  private List<String> methods;

  private Domain domain;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Domain {

    private String id;

    private String name;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

  }


  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Project {

    private Domain domain;

    private String id;

    private String name;

    public Domain getDomain() {
      return domain;
    }

    public void setDomain(Domain domain) {
      this.domain = domain;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

  }

  private Project project;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class User {

    private String id;

    private String name;

    private Domain domain;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public Domain getDomain() {
      return domain;
    }

    public void setDomain(Domain domain) {
      this.domain = domain;
    }
  }

  private User user;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Role {

    private String id;

    private String name;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

  private List<Role> roles;

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Service {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Endpoint {

      private String id;

      private String url;

      private String region;

      private Boolean enabled;

      @JsonProperty("legacy_endpoint_id")
      private String legacyEndpointId;

      @JsonProperty("interface")
      private String iface;

      public String getId() {
        return id;
      }

      public void setId(String id) {
        this.id = id;
      }

      public String getUrl() {
        return url;
      }

      public void setUrl(String url) {
        this.url = url;
      }

      public String getRegion() {
        return region;
      }

      public void setRegion(String region) {
        this.region = region;
      }

      public Boolean getEnabled() {
        return enabled;
      }

      public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
      }

      public String getLegacyEndpointId() {
        return legacyEndpointId;
      }

      public void setLegacyEndpointId(String legacyEndpointId) {
        this.legacyEndpointId = legacyEndpointId;
      }

      public String getInterface() {
        return iface;
      }

      public void setInterface(String iface) {
        this.iface = iface;
      }

    }

    private String id;

    private String type;

    private List<Endpoint> endpoints;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public List<Endpoint> getEndpoints() {
      return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
      this.endpoints = endpoints;
    }

  }

  private List<Service> catalog;


  public String getId() {
    return id;
  }



  public void setId(String id) {
    this.id = id;
  }

  public String getTokenString() {
    return this.tokenString;
  }

  public void setTokenString(String tokenString) {
    this.tokenString = tokenString;
  }


  public String getExpiresAt() {
    return expiresAt;
  }



  public void setExpiresAt(String expiresAt) {
    this.expiresAt = expiresAt;
  }



  public String getIssuedAt() {
    return issuedAt;
  }



  public void setIssuedAt(String issuedAt) {
    this.issuedAt = issuedAt;
  }

  public List<String> getAuditIds() {
    return auditIds;
  }

  public void setAuditIds(List<String> auditIds) {
    this.auditIds = auditIds;
  }

  public List<String> getMethods() {
    return methods;
  }



  public void setMethods(List<String> methods) {
    this.methods = methods;
  }



  public Domain getDomain() {
    return domain;
  }



  public void setDomain(Domain domain) {
    this.domain = domain;
  }



  public Project getProject() {
    return project;
  }



  public void setProject(Project project) {
    this.project = project;
  }



  public User getUser() {
    return user;
  }



  public void setUser(User user) {
    this.user = user;
  }



  public List<Role> getRoles() {
    return roles;
  }



  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }



  public List<Service> getCatalog() {
    return catalog;
  }



  public void setCatalog(List<Service> catalog) {
    this.catalog = catalog;
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private int sequenceNumber = 0;

  public int getSequenceNumber() {
    return sequenceNumber;
  }


  public void setSequenceNumber(int number) {
    this.sequenceNumber = number;
  }

  @Override
  public String toString() {
    return "Token [id=" + id + ", expiresAt=" + expiresAt + ", issuedAt=" + issuedAt + ", methods=" + methods
        + ", domain=" + domain + ", project=" + project + ", user=" + user + ", roles=" + roles + ", catalog="
        + catalog + "]";
  }


}
