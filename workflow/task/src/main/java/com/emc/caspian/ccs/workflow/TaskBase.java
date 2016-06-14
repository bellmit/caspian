package com.emc.caspian.ccs.workflow;

import com.emc.caspian.ccs.workflow.types.EnvironmentKeys;

import java.io.PrintWriter;
import java.util.Map;

/**
 * TaskBase contains several environment variables for use by Task functions. Class containing task function may inherit
 * from TaskBase for accessing these environment variables. Created by gulavb on 4/15/2015.
 */
public class TaskBase {

  public String getToken() {
    if (environment != null) {
      return environment.get(EnvironmentKeys.Token.toString());
    } else {
      return null;
    }
  }

  public String getTrackingId() {
    if (environment != null) {
      return environment.get(EnvironmentKeys.TrackingId.toString());
    } else {
      return null;
    }
  }

  // output stream
  public PrintWriter out;

  // error stream
  public PrintWriter err;

  // environment
  public Map<String, String> environment;
}
