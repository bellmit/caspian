package com.emc.caspian.ccs.workflow;

import com.emc.caspian.ccs.workflow.types.Type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method Annotation for marking methods as task functions for task execution engine. Created by gulavb on 3/30/2015.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {

  public String name();

  public Type returnType();

  public Type[] parametersTypes();
}
