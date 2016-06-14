package com.emc.caspian.ccs.workflow;

import com.emc.caspian.ccs.workflow.types.EnvironmentKeys;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaskExecutorTest {

  @Test
  public void testExecute() throws Exception {
    int i = 5;
    Map<String, String> environment = new HashMap<>();
    environment.put(EnvironmentKeys.Token.toString(), UUID.randomUUID().toString());
    environment.put(EnvironmentKeys.TrackingId.toString(), UUID.randomUUID().toString());

    TaskExecutor executor = new TaskExecutor("");
    StringWriter out = new StringWriter();
    StringWriter err = new StringWriter();
    PrintWriter outStream = new PrintWriter(out);
    PrintWriter errStream = new PrintWriter(err);
    Integer o = (Integer) executor.execute(outStream, errStream, environment, "increment", i);
    Assert.assertEquals(o.intValue(), i + 1);
  }
}
