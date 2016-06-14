package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.workflow.model.DbResponse;
import com.emc.caspian.ccs.workflow.model.ErrorCode;
import com.emc.caspian.ccs.workflow.model.QueueMessage;
import com.emc.caspian.ccs.workflow.types.QueueType;
import com.emc.caspian.fabric.config.Configuration;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MySQLQueueTest {

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
  public void testQueue() {
    if (!setup) {
      return;
    }
    MySQLQueue queue = new MySQLQueue();
    DbResponse<Boolean> response = queue.put(QueueType.TaskQueue, "a3551dd1-d835-4204-90f1-39c65c34d404");
    Assert.assertNull(response.getErrorCode());

    DbResponse<QueueMessage> getResponse = queue.get(QueueType.TaskQueue, 600000);
    Assert.assertNull(getResponse.getErrorCode());

    response = queue.updateLease(
        QueueType.TaskQueue,
        getResponse.getResponseObj().getId(),
        getResponse.getResponseObj().getHandle(),
        60000);
    Assert.assertNull(response.getErrorCode());

    // negative test, use a corrupt handle
    response = queue.updateLease(
        QueueType.TaskQueue,
        getResponse.getResponseObj().getId(),
        UUID.randomUUID().toString(),
        60000);
    Assert.assertEquals(response.getErrorCode(), ErrorCode.DB_RECORD_NOT_FOUND);

    response = queue.delete(
        QueueType.TaskQueue,
        getResponse.getResponseObj().getId(),
        getResponse.getResponseObj().getHandle());
    Assert.assertNull(response.getErrorCode());
  }

  private static boolean setup = false;
  private static final String configPath = "conf/model.conf";
  private static final Logger logger = LoggerFactory.getLogger(MySQLJobTableTest.class);
}
