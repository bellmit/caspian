package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.workflow.model.*;
import com.emc.caspian.ccs.workflow.types.EnvironmentKeys;
import com.emc.caspian.ccs.workflow.types.Status;
import com.emc.caspian.fabric.config.Configuration;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MySQLTaskFrameTableTest {

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
  public void taskFrameTableTest() {
    if (!setup) {
      return;
    }
    String token = UUID.randomUUID().toString();
    String trackingId = UUID.randomUUID().toString();
    TaskFrameModel model = new TaskFrameModel();
    model.setId(UUID.randomUUID().toString());
    model.setTaskId("8af8eba4-7a75-46c4-840a-3899ce9bf23a");
    model.setJobId("dummy_job");
    model.setStatus(Status.Ready);
    List<ParameterBinding> parameters = new ArrayList<ParameterBinding>();
    parameters.add(new ParameterBinding("i", "5"));
    ParameterBindings bindings = new ParameterBindings();
    bindings.setList(parameters);
    model.setParameters(bindings);
    Map<String, String> environment = new HashMap<String, String>();
    environment.put(EnvironmentKeys.Token.toString(), token);
    environment.put(EnvironmentKeys.TrackingId.toString(), trackingId);
    model.setEnvironment(environment);
    model.setOutput("output");
    model.setCreationTime(System.currentTimeMillis());
    model.setStartTime(System.currentTimeMillis() + 2000L);
    model.setEndTime(System.currentTimeMillis() + 5000L);
    model.setOutStream("out stream");
    model.setErrStream("err stream");

    MySQLTaskFrameTable table = new MySQLTaskFrameTable();
    DbResponse<Boolean> response = table.insert(model);
    Assert.assertNull(response.getErrorCode());

    DbResponse<TaskFrameModel> rowResponse = table.get(model.getId());
    Assert.assertNull(rowResponse.getErrorCode());
    Assert.assertNotNull(rowResponse.getResponseObj());
    bindings = rowResponse.getResponseObj().getParameterBindings();
    ParameterBinding binding = bindings.getList().get(0);
    Assert.assertTrue(binding.getValue().equals("5"));
    Map<String, String> map = rowResponse.getResponseObj().getEnvironmentMap();
    Assert.assertEquals(map.get(EnvironmentKeys.Token.toString()), token);
    Assert.assertEquals(map.get(EnvironmentKeys.TrackingId.toString()), trackingId);

    // negative test case
    response = table.insert(model);
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_RECORD_DUPLICATE);

    int attemptCounter = 1;
    String previousAttemptId = "previous_attempt_id";
    model.setTaskId("random id");
    model.setOutStream(null);
    model.setAttemptCounter(attemptCounter);
    model.setPreviousAttemptId(previousAttemptId);
    response = table.update(model);
    Assert.assertNull(response.getErrorCode());

    TaskFrameModel secondModel = new TaskFrameModel();
    secondModel.setId("second");
    secondModel.setTaskId("second random id");
    secondModel.setJobId("some job id");
    secondModel.setStatus(Status.FatalError);
    response = table.insert(secondModel);
    Assert.assertNull(response.getErrorCode());

    DbResponse<List<TaskFrameModel>> listResponse = table.getAll();
    Assert.assertNull(listResponse.getErrorCode());
    Assert.assertNotNull(listResponse.getResponseObj());
    Assert.assertEquals(listResponse.getResponseObj().get(0).getAttemptCounter(), attemptCounter);
    Assert.assertEquals(listResponse.getResponseObj().get(0).getPreviousAttemptId(), previousAttemptId);

    response = table.delete(model.getId());
    Assert.assertNull(response.getErrorCode());

    response = table.delete(secondModel.getId());
    Assert.assertNull(response.getErrorCode());

    // negative test case
    rowResponse = table.get(model.getId());
    Assert.assertEquals(rowResponse.getErrorCode(), ErrorCode.DB_RECORD_NOT_FOUND);
  }

  private static boolean setup = false;
  private static final String configPath = "conf/model.conf";
  private static final Logger logger = LoggerFactory.getLogger(MySQLTaskFrameTableTest.class);
}
