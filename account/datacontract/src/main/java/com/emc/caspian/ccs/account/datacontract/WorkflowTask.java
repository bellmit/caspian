package com.emc.caspian.ccs.account.datacontract;

import com.emc.caspian.ccs.account.types.WorkflowTaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkflowTask {

  private String taskId;
  private Link link;
  private WorkflowTaskStatus status;
  private Resource resource;
 
  public WorkflowTask(String id, WorkflowTaskStatus status, Resource resource) {
    this.taskId = id;
    this.resource = resource;
    this.status = status;
  }

  public WorkflowTask() {

  }

  @JsonProperty("id")
  public String getId() {
    return taskId;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.taskId = id;
  }
  
  @JsonProperty("link")
  public Link getLink() {
    return link;
  }

  @JsonProperty("link")
  public void setLink(Link link) {
    this.link = link;
  }

  @JsonProperty("status")
  public WorkflowTaskStatus getStatus() {
    return status;
  }

  @JsonProperty("status")
  public void setStatus(WorkflowTaskStatus status) {
    this.status = status;
  }

  @JsonProperty("resource")
  public Resource getResource() {
    return resource;
  }

  @JsonProperty("resource")
  public void setResource(Resource resource) {
    this.resource = resource;
  }

  public class Resource {
    private String accountId;
    private Link link;

    @JsonProperty("id")
    public String getId() {
      return accountId;
    }

    @JsonProperty("id")
    public void setId(String id) {
      this.accountId = id;
    }

    @JsonProperty("link")
    public Link getLink() {
      return link;
    }

    @JsonProperty("link")
    public void setLink(Link link) {
      this.link = link;
    }

  }

}
