package com.emc.caspian.ccs.workflow.worker;

import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.workflow.model.*;
import com.emc.caspian.ccs.workflow.model.mysql.MySQLJobTable;
import com.emc.caspian.ccs.workflow.model.mysql.MySQLProperties;
import com.emc.caspian.ccs.workflow.model.mysql.MySQLTaskFrameTable;
import com.emc.caspian.ccs.workflow.model.mysql.MySQLTaskTable;
import com.emc.caspian.ccs.workflow.types.EnvironmentKeys;
import com.emc.caspian.ccs.workflow.types.Status;
import com.emc.caspian.fabric.config.Configuration;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WorkerTest {

  @BeforeClass
  public static void setup() {
    // load configurations first
    try {
      Configuration.load(configPath);
      Validator.validateNotEmpty(MySQLProperties.getHostname());
      Validator.validatePortRange(Integer.parseInt(MySQLProperties.getPort()));
      Validator.validateNotEmpty(MySQLProperties.getDatabase());
      Validator.validateNotEmpty(MySQLProperties.getUser());
      Validator.validateNotEmpty(MySQLProperties.getPassword());
      setup = true;
    } catch (Exception e) {
      logger.error("Exception class={}, message={}", e.getClass().getName(), e.getMessage());
      setup = false;
      return;
    }

    jobTable = new MySQLJobTable();
    taskTable = new MySQLTaskTable();
    taskFrameTable = new MySQLTaskFrameTable();

    taskIds = new ArrayList<String>(3);
    jobIds = new ArrayList<String>(7);

    i = 5;
    a = 456745;
    b = 56;

    environment = new HashMap<>();
    environment.put(EnvironmentKeys.Token.toString(), UUID.randomUUID().toString());
    environment.put(EnvironmentKeys.TrackingId.toString(), UUID.randomUUID().toString());

    // Create 3 tasks
    String id = UUID.randomUUID().toString();
    taskIds.add(id);
    TaskModel
        increment =
        new TaskModel(id, "increment", "Integer", "{\"type_bindings\":[{\"name\":\"i\",\"type\":\"Integer\"}]}", null);
    addTask(increment);

    id = UUID.randomUUID().toString();
    taskIds.add(id);
    TaskModel
        decrement =
        new TaskModel(id, "decrement", "Integer", "{\"type_bindings\":[{\"name\":\"i\",\"type\":\"Integer\"}]}", null);
    addTask(decrement);

    id = UUID.randomUUID().toString();
    taskIds.add(id);
    TaskModel
        quotient =
        new TaskModel(id, "quotient", "Integer",
                      "{\"type_bindings\":[{\"name\":\"a\",\"type\":\"Integer\"}, {\"name\":\"b\",\"type\":\"Integer\"}]}",
                      null);
    addTask(quotient);

    id = UUID.randomUUID().toString();
    taskIds.add(id);
    TaskModel
        error =
        new TaskModel(id, "error", "Boolean",
                      "{\"type_bindings\":[{\"name\":\"a\",\"type\":\"String\"}, {\"name\":\"b\",\"type\":\"Integer\"}, {\"name\":\"c\",\"type\":\"Boolean\"}]}",
                      null);
    addTask(error);

    // Create one successful and one failure task frame case for each task
    addJob(true, "increment", Integer.toString(i));

    addJob(false, "increment");

    addJob(true, "decrement", Integer.toString(i));

    addJob(false, "decrement");

    addJob(true, "quotient", Integer.toString(a), Integer.toString(b));

    addJob(true, "quotient", Integer.toString(a), Integer.toString(0));

    addJob(false, "quotient", Integer.toString(a));

    addJob(true, "error", "a", "10", "true");

    addJob(true, "error", "a", "10", "wrong_boolean_value");

    Assert.assertEquals(jobIds.size(), 6);
  }

  @Test
  public void workerTest() throws Exception {
    if (!setup) {
      return;
    }
    // Start worker
    String[] args = new String[0];
    Worker.main(args);

    // sleep for 60 second per job and stop the workers
    Thread.sleep(jobIds.size() * 60 * 1000);
    Worker.stop();

    TaskFrameModel taskFrameModel = null;
    JobModel jobModel = null;

    // check whether the results are as expected by looking at task frames
    jobModel = getJob(jobIds.get(0));
    Assert.assertEquals(jobModel.getStatus(), Status.Successful);
    Assert.assertEquals(jobModel.getOutput(), Integer.toString(i + 1));
    taskFrameModel = getTaskFrame(jobModel.getTargetFrameId());
    Assert.assertEquals(taskFrameModel.getStatus(), Status.Successful);
    Assert.assertEquals(taskFrameModel.getOutput(), Integer.toString(i + 1));

    jobModel = getJob(jobIds.get(1));
    Assert.assertEquals(jobModel.getStatus(), Status.Successful);
    Assert.assertEquals(jobModel.getOutput(), Integer.toString(i - 1));
    taskFrameModel = getTaskFrame(jobModel.getTargetFrameId());
    Assert.assertEquals(taskFrameModel.getStatus(), Status.Successful);
    Assert.assertEquals(taskFrameModel.getOutput(), Integer.toString(i - 1));

    jobModel = getJob(jobIds.get(2));
    Assert.assertEquals(jobModel.getStatus(), Status.Successful);
    Assert.assertEquals(jobModel.getOutput(), Integer.toString(a / b));
    taskFrameModel = getTaskFrame(jobModel.getTargetFrameId());
    Assert.assertEquals(taskFrameModel.getStatus(), Status.Successful);
    Assert.assertEquals(taskFrameModel.getOutput(), Integer.toString(a / b));

    jobModel = getJob(jobIds.get(3));
    Assert.assertEquals(jobModel.getStatus(), Status.FatalError);
    taskFrameModel = getTaskFrame(jobModel.getTargetFrameId());
    Assert.assertEquals(taskFrameModel.getStatus(), Status.FatalError);

    jobModel = getJob(jobIds.get(4));
    Assert.assertEquals(jobModel.getStatus(), Status.FleetingError);
    taskFrameModel = getTaskFrame(jobModel.getTargetFrameId());
    Assert.assertEquals(taskFrameModel.getStatus(), Status.FleetingError);

    jobModel = getJob(jobIds.get(5));
    Assert.assertEquals(jobModel.getStatus(), Status.FatalError);
    taskFrameModel = getTaskFrame(jobModel.getTargetFrameId());
    Assert.assertEquals(taskFrameModel.getStatus(), Status.FatalError);
  }

  @AfterClass
  public static void cleanup() {
    if (!setup) {
      return;
    }
    // delete tasks
    for (int i = 0; i < taskIds.size(); i++) {
      taskTable.delete(taskIds.get(i));
    }

    // delete jobs
    // delete task frames
    for (int i = 0; i < jobIds.size(); i++) {
      JobModel jobModel = getJob(jobIds.get(i));
      String taskFrameId = jobModel.getTargetFrameId();
      jobTable.delete(jobIds.get(i));
      while (taskFrameId != null && !taskFrameId.isEmpty()) {
        TaskFrameModel taskFrameModel = getTaskFrame(taskFrameId);
        taskFrameTable.delete(taskFrameId);
        taskFrameId = taskFrameModel.getPreviousAttemptId();
      }
    }
  }

  private static void addTask(TaskModel taskModel) {
    DbResponse<Boolean> response = taskTable.insert(taskModel);
    Assert.assertNull(response.getErrorCode());
  }

  private static void addJob(boolean correct, String taskName, String... parameters) {
    DbResponse<JobModel> jobResponse = JobAPI.submitTask(environment, taskName, parameters);
    if (correct) {
      Assert.assertNull(jobResponse.getErrorCode());
      JobModel jobModel = jobResponse.getResponseObj();
      Assert.assertNotNull(jobModel);
      jobIds.add(jobModel.getId());
    } else {
      Assert.assertEquals(jobResponse.getErrorCode(), ErrorCode.DB_SYNTAX_ERROR);
    }
  }

  private static TaskFrameModel getTaskFrame(String id) {
    DbResponse<TaskFrameModel> response = taskFrameTable.get(id);
    Assert.assertNull(response.getErrorCode());
    Assert.assertNotNull(response.getResponseObj());
    return response.getResponseObj();
  }

  private static JobModel getJob(String id) {
    DbResponse<JobModel> response = JobAPI.getJob(id);
    Assert.assertNull(response.getErrorCode());
    Assert.assertNotNull(response.getResponseObj());
    return response.getResponseObj();
  }

  private static MySQLJobTable jobTable;
  private static MySQLTaskTable taskTable;
  private static MySQLTaskFrameTable taskFrameTable;

  private static List<String> taskIds;
  private static List<String> jobIds;

  private static int i;
  private static int a;
  private static int b;
  private static Map<String, String> environment;

  private static boolean setup = false;
  private static final String configPath = "conf/worker.conf";
  private static final Logger logger = LoggerFactory.getLogger(WorkerTest.class);
}
