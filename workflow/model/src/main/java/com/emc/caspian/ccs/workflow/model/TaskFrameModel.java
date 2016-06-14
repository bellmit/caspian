package com.emc.caspian.ccs.workflow.model;

import com.emc.caspian.ccs.workflow.types.Status;
import com.emc.caspian.ccs.common.utils.JsonHelper;

import java.util.Map;

/**
 * TaskFrame stores state and meta-data of a task in execution within the system Created by gulavb on 4/5/2015.
 */
public class TaskFrameModel {

  public TaskFrameModel() {
  }

  public TaskFrameModel(String id, String jobId, String taskId, String parameters, int priority, Status status,
                        long creationTime) {
    this.id = id;
    this.jobId = jobId;
    this.taskId = taskId;
    this.parameters = parameters;
    this.priority = priority;
    this.status = status;
    this.creationTime = creationTime;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getJobId() {
    return jobId;
  }

  public void setJobId(String jobId) {
    this.jobId = jobId;
  }

  public String getWorkflowId() {
    return workflowId;
  }

  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public ParameterBindings getParameterBindings() {
    if (this.parameters == null || this.parameters.isEmpty()) {
      return null;
    } else {
      ParameterBindings bindings = JsonHelper.deserializeFromJson(this.parameters, ParameterBindings.class);
      return bindings;
    }
  }

  public void setParameters(ParameterBindings bindings) {
    if (bindings == null) {
      this.parameters = null;
    } else {
      String json = JsonHelper.serializeToJson(bindings);
      this.parameters = json;
    }
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public Map<String, String> getEnvironmentMap() {
    Map<String, String> map = JsonHelper.deserializeFromJson(this.environment, Map.class);
    return map;
  }

  public void setEnvironment(Map<String, String> environment) {
    String json = JsonHelper.serializeToJson(environment);
    this.environment = json;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public String getErrStream() {
    return errStream;
  }

  public void setErrStream(String errStream) {
    this.errStream = errStream;
  }

  public String getOutStream() {
    return outStream;
  }

  public void setOutStream(String outStream) {
    this.outStream = outStream;
  }

  public Long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(Long creationTime) {
    this.creationTime = creationTime;
  }

  public Long getStartTime() {
    return startTime;
  }

  public void setStartTime(Long startTime) {
    this.startTime = startTime;
  }

  public Long getEndTime() {
    return endTime;
  }

  public void setEndTime(Long endTime) {
    this.endTime = endTime;
  }

  public int getAttemptCounter() {
    return attemptCounter;
  }

  public void setAttemptCounter(int attemptCounter) {
    this.attemptCounter = attemptCounter;
  }

  public String getPreviousAttemptId() {
    return previousAttemptId;
  }

  public void setPreviousAttemptId(String previousAttemptId) {
    this.previousAttemptId = previousAttemptId;
  }

  private String id;
  private String jobId;
  private String workflowId;
  private String taskId;
  private String parameters;
  private String environment;
  private int priority;
  private Status status;
  private String output;
  private String errStream;
  private String outStream;
  private long creationTime;
  private long startTime;
  private long endTime;
  private int attemptCounter;
  private String previousAttemptId;
}
