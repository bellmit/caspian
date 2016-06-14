package com.emc.caspian.ccs.workflow.model;

/**
 * Created by gulavb on 4/9/2015.
 */
public class ErrorMessages {

  public final static String ID_EMPTY = "Identifier cannot be empty";
  public final static String JOB_ID_EMPTY = " Job id cannot be empty";
  public final static String NAME_EMPTY = "Name cannot be empty";
  public final static String RETURN_TYPE_EMPTY = "Return type cannot be empty";
  public final static String JAR_ID_EMPTY = "Jar id cannot be empty";
  public final static String MESSAGE_NOT_EMPTY = "Message cannot be empty";
  public final static String HANDLE_EMPTY = "Handle cannot be empty";
  public final static String TASK_ID_EMPTY = "Task id cannot be empty";
  public final static String STATUS_EMPTY = "Status cannot be empty";

  public final static String NOT_FOUND = "Record not found";
  public final static String INTERNAL_ERROR = "Internal error";
  public final static String CONSTRAINT_VIOLATION = "Constraint violation while deleting record";
  public final static String INSERT_ERROR = "Conflict, duplicate entry";

  public final static String INVALID_PARAMETERS = "Expected %s parameters, found %s parameters";
}
