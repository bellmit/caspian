package com.emc.caspian.ccs.workflow.model;

/**
 * Pair of parameter name and value Created by gulavb on 4/13/2015.
 */
public class ParameterBinding {

  public ParameterBinding() {
  }

  public ParameterBinding(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  private String name;
  private String value;
}
