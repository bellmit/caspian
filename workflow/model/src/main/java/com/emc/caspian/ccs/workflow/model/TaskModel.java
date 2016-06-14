package com.emc.caspian.ccs.workflow.model;

import com.emc.caspian.ccs.common.utils.JsonHelper;

/**
 * TaskModel wraps task meta-data Created by gulavb on 4/5/2015.
 */
public class TaskModel {

  public TaskModel() {
  }

  public TaskModel(String id, String name, String returnType, String parameters, String jarId) {
    this.id = id;
    this.name = name;
    this.returnType = returnType;
    this.parameters = parameters;
    this.jarId = jarId;
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

  public String getReturnType() {
    return returnType;
  }

  public void setReturnType(String returnType) {
    this.returnType = returnType;
  }

  public String getParameters() {
    return parameters;
  }

  public ParameterTypeBindings getParameterTypeBindings() {
    if (this.parameters == null || this.parameters.isEmpty()) {
      return null;
    } else {
      ParameterTypeBindings bindings = JsonHelper.deserializeFromJson(this.parameters, ParameterTypeBindings.class);
      return bindings;
    }
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }

  public void setParameters(ParameterTypeBindings parameters) {
    if (parameters == null) {
      this.parameters = null;
    } else {
      String json = JsonHelper.serializeToJson(parameters);
      this.parameters = json;
    }
  }

  public String getJarId() {
    return jarId;
  }

  public void setJarId(String jarId) {
    this.jarId = jarId;
  }

  String id;
  String name;
  String returnType;
  String parameters;
  String jarId;
}
