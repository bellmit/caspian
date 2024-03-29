package com.emc.caspian.ccs.workflow.types;

/**
 * Created by gulavb on 4/8/2015.
 */
public enum Status {
  Ready,         // task is ready to be picked for execution by a worker
  Running,       // task is being executed on some worker
  Blocked,       // task is blocked on an event, e.g., timer event for sleep task
  Successful,    // execution complete with success
  FleetingError, // temporary error, retrying may lead to success
  FatalError,    // temporary error, retrying may not lead to success
}
