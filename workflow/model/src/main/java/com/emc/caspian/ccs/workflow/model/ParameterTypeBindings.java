package com.emc.caspian.ccs.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Map of parameter names and their types Created by gulavb on 4/14/2015.
 */
public class ParameterTypeBindings {

  public ParameterTypeBindings() {
  }

  public ParameterTypeBindings(List<ParameterTypeBinding> list) {
    this.list = list;
  }

  public List<ParameterTypeBinding> getList() {
    return list;
  }

  public void setList(List<ParameterTypeBinding> list) {
    this.list = list;
  }

  @JsonProperty("type_bindings")
  private List<ParameterTypeBinding> list;
}
