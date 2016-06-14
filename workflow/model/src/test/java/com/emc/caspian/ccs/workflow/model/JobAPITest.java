package com.emc.caspian.ccs.workflow.model;

import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.workflow.model.mysql.MySQLProperties;
import com.emc.caspian.ccs.workflow.types.EnvironmentKeys;
import com.emc.caspian.ccs.workflow.types.Type;
import com.emc.caspian.fabric.config.Configuration;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JobAPITest {

  @BeforeClass
  public static void loadConfigurations() throws Exception {
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
    }
  }

  @Test
  public void testJobAPI() {
    if (!setup) {
      return;
    }
    String taskName = "quotient";

    Map<String, String> environment = new HashMap<String, String>();
    environment.put(EnvironmentKeys.Token.toString(), UUID.randomUUID().toString());
    environment.put(EnvironmentKeys.TrackingId.toString(), UUID.randomUUID().toString());

    JobTable jobTable = TableFactory.getJobTable();
    TaskFrameTable taskFrameTable = TableFactory.getTaskFrameTable();
    TaskTable taskTable = TableFactory.getTaskTable();

    String taskId = UUID.randomUUID().toString();
    TaskModel quotientTaskModel = new TaskModel();
    quotientTaskModel.setId(taskId);
    quotientTaskModel.setName(taskName);
    quotientTaskModel.setReturnType("Integer");
    List<ParameterTypeBinding> parameters = new ArrayList<ParameterTypeBinding>();
    parameters.add(new ParameterTypeBinding("a", Type.Integer));
    parameters.add(new ParameterTypeBinding("b", Type.Integer));
    ParameterTypeBindings bindings = new ParameterTypeBindings();
    bindings.setList(parameters);
    quotientTaskModel.setParameters(bindings);
    DbResponse<Boolean> success = taskTable.insert(quotientTaskModel);
    Assert.assertNull(success.getErrorCode());

    DbResponse<JobModel> response = JobAPI.submitTask(environment, taskName, "23245", "34");
    Assert.assertNull(response.getErrorCode());
    JobModel model = response.getResponseObj();
    Assert.assertEquals(model.getParametersList().size(), 2);

    response = JobAPI.getJob(model.getId());
    Assert.assertNull(response.getErrorCode());
    Assert.assertEquals(response.getResponseObj().getTargetName(), taskName);

    success = taskFrameTable.delete(model.getTargetFrameId());
    Assert.assertNull(success.getErrorCode());

    success = jobTable.delete(model.getId());
    Assert.assertNull(success.getErrorCode());

    // negative test
    response = JobAPI.getJob(model.getId());
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_RECORD_NOT_FOUND);

    // negative test
    response = JobAPI.submitTask(environment, taskName, "1234");
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_SYNTAX_ERROR);

    // negative test
    response = JobAPI.submitTask(environment, "some random task name");
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_RECORD_NOT_FOUND);

    success = taskTable.delete(taskId);
    Assert.assertNull(success.getErrorCode());
  }

  private static boolean setup = false;
  private static final String configPath = "conf/model.conf";
  private static final Logger logger = LoggerFactory.getLogger(JobAPITest.class);
}
