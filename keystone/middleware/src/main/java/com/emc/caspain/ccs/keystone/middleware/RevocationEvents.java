package com.emc.caspain.ccs.keystone.middleware;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;



@JsonIgnoreProperties(ignoreUnknown = true)
class RevocationEvents implements Cloneable {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static final class Event {

    @JsonProperty("issued_before")
    private String issuedBefore;

    @JsonProperty("domain_id")
    private String domainId;

    @JsonProperty("project_id")
    private String projectId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("role_id")
    private String roleId;

    @JsonProperty("audit_id")
    private String auditId;

    @JsonProperty("audit_chain_id")
    private String auditChainId;

    public String getIssuedBefore() {
      return issuedBefore;
    }

    public void setIssuedBefore(String IssuedBefore) {
      this.issuedBefore = IssuedBefore;
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }

    public String getProjectId() {
      return projectId;
    }

    public void setProjectId(String projectId) {
      this.projectId = projectId;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }

    public String getRoleId() {
      return roleId;
    }

    public void setRoleId(String roleId) {
      this.roleId = roleId;
    }

    public String getAuditId() {
      return auditId;
    }

    public void setAuditId(String auditId) {
      this.auditId = auditId;
    }

    public String getAuditChainId() {
      return auditChainId;
    }

    public void setAudtChainId(String auditChainId) {
      this.auditChainId = auditChainId;
    }

  }

  private List<Event> events;

  public List<Event> getEvents() {
    return events;
  }
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  private String dateTime;
  
  public String getDateTime()
  {
    return dateTime;
  }
  
  public void setDateTime(String date)
  {
    this.dateTime=date;
  }
}
