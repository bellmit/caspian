package com.emc.caspian.ccs.workflow.tasks;

import com.emc.caspian.ccs.workflow.Task;
import com.emc.caspian.ccs.workflow.TaskBase;
import com.emc.caspian.ccs.workflow.TaskException;
import com.emc.caspian.ccs.workflow.types.Type;

/**
 * Created by gulavb on 4/15/2015.
 */
public class NumericOperations extends TaskBase {

  @Task(name = "increment", returnType = Type.Integer, parametersTypes = {Type.Integer})
  public int increment(int i) {
    this.out.println("Input parameter i = " + Integer.toString(i));
    return i + 1;
  }

  @Task(name = "decrement", returnType = Type.Integer, parametersTypes = {Type.Integer})
  public int decrement(int i) {
    this.out.println("Input parameter i = " + Integer.toString(i));
    return i - 1;
  }

  @Task(name = "quotient", returnType = Type.Integer, parametersTypes = {Type.Integer, Type.Integer})
  public int quotient(int a, int b) {
    if (b == 0) {
      String errorMessage = "Divisor cannot be 0";
      this.err.println(errorMessage);
      throw new TaskException(errorMessage, false);
    } else {
      return a / b;
    }
  }

  @Task(name = "error", returnType = Type.Boolean, parametersTypes = {Type.String, Type.Integer, Type.Boolean})
  public boolean error(String a, int b, boolean c) {
    this.out.println("Token value is " + this.getToken());
    this.out.println("Tacking Id is " + this.getTrackingId());
    throw new TaskException("Retriable error", true);
  }
}
