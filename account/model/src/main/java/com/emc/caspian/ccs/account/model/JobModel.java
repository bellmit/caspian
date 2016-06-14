package com.emc.caspian.ccs.account.model;

import com.emc.caspian.ccs.account.types.JobType;
import com.emc.caspian.ccs.account.types.WorkflowTaskStatus;
import com.emc.caspian.ccs.common.utils.JsonHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JobModel wraps job meta-data Created by gulavb on 4/8/2015.
 */
public class JobModel {

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public JobType getTargetType() {
    return targetType;
  }

  public void setTargetType(JobType targetType) {
    this.targetType = targetType;
  }

  public String getTargetFrameId() {
    return targetFrameId;
  }

  public void setTargetFrameId(String targetFrameId) {
    this.targetFrameId = targetFrameId;
  }

  public String getTargetName() {
    return targetName;
  }

  public void setTargetName(String targetName) {
    this.targetName = targetName;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public List<String> getParametersList() {
    List<String> list = JsonHelper.deserializeFromJson(this.parameters, ArrayList.class);
    return list;
  }

  public void setParameters(List<String> parameters) {
    String json = JsonHelper.serializeToJson(parameters);
    this.parameters = json;
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

  public WorkflowTaskStatus getStatus() {
    return status;
  }

  public void setStatus(WorkflowTaskStatus status) {
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

  public long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  public long getCompletionTime() {
    return completionTime;
  }

  public void setCompletionTime(long completionTime) {
    this.completionTime = completionTime;
  }

  public String getExecutionState() {
    return executionState;
  }

  public void setExecutionState(String executionState) {
    this.executionState = executionState;
  }

  private String id;
  private JobType targetType;
  private String targetFrameId;
  private String targetName;
  private String parameters;
  private String environment;
  private int priority;
  private WorkflowTaskStatus status;
  private String output;
  private String errStream;
  private String outStream;
  private long creationTime;
  private long completionTime;
  // includes program counter and variable values
  private String executionState;
}
