package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.workflow.model.*;
import com.emc.caspian.ccs.workflow.types.Type;
import com.emc.caspian.fabric.config.Configuration;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQLTaskTableTest {

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
  public void taskTableTest() {
    if (!setup) {
      return;
    }
    String id = UUID.randomUUID().toString();
    TaskModel incrementTaskModel = new TaskModel();
    incrementTaskModel.setId(id);
    incrementTaskModel.setName("increment");
    incrementTaskModel.setReturnType("Integer");
    List<ParameterTypeBinding> parameters = new ArrayList<ParameterTypeBinding>();
    parameters.add(new ParameterTypeBinding("i", Type.Integer));
    ParameterTypeBindings bindings = new ParameterTypeBindings();
    bindings.setList(parameters);
    incrementTaskModel.setParameters(bindings);

    MySQLTaskTable table = new MySQLTaskTable();
    DbResponse<Boolean> response = table.insert(incrementTaskModel);
    Assert.assertNull(response.getErrorCode());

    TaskModel decrementTaskModel = new TaskModel();
    decrementTaskModel.setId(UUID.randomUUID().toString());
    decrementTaskModel.setName("decrement");
    decrementTaskModel.setReturnType("Integer");
    parameters = new ArrayList<ParameterTypeBinding>();
    parameters.add(new ParameterTypeBinding("i", Type.Integer));
    bindings = new ParameterTypeBindings();
    bindings.setList(parameters);
    decrementTaskModel.setParameters(bindings);

    response = table.insert(decrementTaskModel);
    Assert.assertNull(response.getErrorCode());

    //negative test
    response = table.insert(incrementTaskModel);
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_RECORD_DUPLICATE);

    DbResponse<TaskModel> getResponse = table.get(id);
    Assert.assertNull(getResponse.getErrorCode());

    bindings = getResponse.getResponseObj().getParameterTypeBindings();
    ParameterTypeBinding binding = bindings.getList().get(0);
    Assert.assertEquals(binding.getType(), Type.Integer);

    incrementTaskModel.setReturnType("String");
    response = table.update(incrementTaskModel);
    Assert.assertNull(response.getErrorCode());

    DbResponse<List<TaskModel>> listResponse = table.getAll();
    Assert.assertNull(listResponse.getErrorCode());

    response = table.delete(incrementTaskModel.getId());
    Assert.assertNull(response.getErrorCode());

    response = table.delete(decrementTaskModel.getId());
    Assert.assertNull(response.getErrorCode());

    // negative test case
    getResponse = table.get(incrementTaskModel.getId());
    Assert.assertEquals(getResponse.getErrorCode(), ErrorCode.DB_RECORD_NOT_FOUND);

    // negative test case
    response = table.delete(incrementTaskModel.getId());
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_RECORD_NOT_FOUND);
  }

  private static boolean setup = false;
  private static final String configPath = "conf/model.conf";
  private static final Logger logger = LoggerFactory.getLogger(MySQLTaskTableTest.class);
}
