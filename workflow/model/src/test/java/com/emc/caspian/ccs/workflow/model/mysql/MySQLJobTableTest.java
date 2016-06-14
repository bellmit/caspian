package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.workflow.model.DbResponse;
import com.emc.caspian.ccs.workflow.model.ErrorCode;
import com.emc.caspian.ccs.workflow.model.JobModel;
import com.emc.caspian.ccs.workflow.types.EnvironmentKeys;
import com.emc.caspian.ccs.workflow.types.JobType;
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

public class MySQLJobTableTest {

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
  public void testJobTable() {
    if (!setup) {
      return;
    }
    JobModel model = new JobModel();
    model.setId(UUID.randomUUID().toString());
    model.setTargetType(JobType.Task);
    model.setTargetName("deleteAccount");
    List<String> parameters = new ArrayList<String>();
    parameters.add("firstAccount");
    parameters.add("5");
    model.setParameters(parameters);
    Map<String, String> environment = new HashMap<String, String>();
    environment.put(EnvironmentKeys.Token.toString(), UUID.randomUUID().toString());
    environment.put(EnvironmentKeys.TrackingId.toString(), UUID.randomUUID().toString());
    model.setEnvironment(environment);
    model.setStatus(Status.Blocked);
    model.setOutput("done");

    MySQLJobTable table = new MySQLJobTable();
    DbResponse<Boolean> response = table.insert(model);
    Assert.assertNull(response.getErrorCode());

    DbResponse<JobModel> getResponse = table.get(model.getId());
    Assert.assertNull(getResponse.getErrorCode());

    //negative test case
    response = table.insert(model);
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_RECORD_DUPLICATE);

    model.setStatus(Status.FleetingError);
    model.setTargetFrameId(UUID.randomUUID().toString());
    response = table.update(model);
    Assert.assertNull(response.getErrorCode());

    DbResponse<List<JobModel>> listResponse = table.getAll();
    Assert.assertNull(listResponse.getErrorCode());
    Assert.assertEquals(listResponse.getResponseObj().size(), 1);

    response = table.delete(model.getId());
    Assert.assertNull(response.getErrorCode());

    // negative test case
    getResponse = table.get(model.getId());
    Assert.assertEquals(getResponse.getErrorCode(), ErrorCode.DB_RECORD_NOT_FOUND);

    // negative test case
    response = table.delete(model.getId());
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_RECORD_NOT_FOUND);
  }

  private static boolean setup = false;
  private static final String configPath = "conf/model.conf";
  private static final Logger logger = LoggerFactory.getLogger(MySQLJobTableTest.class);
}
