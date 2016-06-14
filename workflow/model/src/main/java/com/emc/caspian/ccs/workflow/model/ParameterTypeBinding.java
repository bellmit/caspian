package com.emc.caspian.ccs.workflow.model;

import com.emc.caspian.ccs.workflow.types.Type;

/**
 * Pair of parameter name and type Created by gulavb on 4/13/2015.
 */
public class ParameterTypeBinding {

  public ParameterTypeBinding() {
  }

  public ParameterTypeBinding(String name, Type type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  private String name;
  private Type type;
}
