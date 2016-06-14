package com.emc.caspian.ccs.workflow;

/**
 * Enhanced exception that can be thrown by task function. Setting retriable to true indicates to the task execution
 * engine that the error is fleeting and the task can be retried. Created by gulavb on 4/15/2015.
 */
public class TaskException extends RuntimeException {

  public TaskException(String message, boolean retriable) {
    super(message);
    this.retriable = retriable;
  }

  public boolean isRetriable() {
    return retriable;
  }

  private boolean retriable;

}
