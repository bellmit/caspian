package com.emc.caspian.ccs.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Map of parameter names and their values Created by gulavb on 4/14/2015.
 */
public class ParameterBindings {

  public ParameterBindings() {
  }

  public ParameterBindings(List<ParameterBinding> list) {
    this.list = list;
  }

  public List<ParameterBinding> getList() {
    return list;
  }

  public void setList(List<ParameterBinding> list) {
    this.list = list;
  }

  @JsonProperty("value_bindings")
  private List<ParameterBinding> list;
}
