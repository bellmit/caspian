package com.emc.caspian.ccs.account.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.workflow.model.DbResponse;
import com.emc.caspian.ccs.workflow.model.JobAPI;
import com.emc.caspian.ccs.workflow.model.JobModel;
import com.emc.caspian.ccs.workflow.types.EnvironmentKeys;

public class TaskSubmitter {
  
  private static final Logger _log = LoggerFactory.getLogger(TaskSubmitter.class);

  public static DbResponse<JobModel> submitTaskForWorkflow(Tasks task, String... parameters) {
    String taskName = task.toString();
    Map<String, String> env = new HashMap<String, String>();
    String token = KeystoneHelper.getInstance().getCSAToken();
    
    if (token == null) {
      _log.warn("Token is null while submitting the task for workflow");
      return null;
    }
    
    env.put(EnvironmentKeys.Token.toString(), token);

    return JobAPI.submitTask(env, taskName, parameters);

  }

  /**
   * class to hold the tasknames. New taskNames can be added here
   * 
   * @author raod4
   *
   */

  public enum Tasks {
    DELETE_ACCOUNT("deleteAccount"),
    ADD_DOMAIN("addDomain"),
    DELETE_DOMAIN("deleteDomain"),
    ELECT_PRIMARY_DOMAIN("electPrimaryDomain"),
    SYNC_ROLES("syncRoles");

    private String taskName;

    private Tasks(String taskName) {
      this.taskName = taskName;
    }

    @Override
    public String toString() {
      return this.taskName;
    }
  }
}
