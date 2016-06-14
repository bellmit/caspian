package com.emc.caspian.ccs.workflow.worker;

import com.emc.caspian.fabric.config.Configuration;

/**
 * Created by gulavb on 4/22/2015.
 */
public class WorkerProperties {

  private static final String defaultMaxRetries = "3";
  private static final String taskSection = "task";
  private static final String retriesSubSection = ".max_retries";
  private static final String workerSection = "worker";
  // default 10 seconds
  private static final String defaultRetryInterval = "10000";
  private static final String retryIntervalSubSection = ".retry_interval";
  private static final String log4jSubSection = ".log4jpropertiesfilepath";

  private static final Configuration.Value<Integer> MaxRetries =
      Configuration.make(Integer.class, taskSection + retriesSubSection, defaultMaxRetries);

  private static final Configuration.Value<String> Log4jPropertiesFilePath =
      Configuration.make(String.class, workerSection + log4jSubSection, "");

  private static final Configuration.Value<Integer> RetryInterval =
      Configuration.make(Integer.class, taskSection + retryIntervalSubSection, defaultRetryInterval);

  public static Integer getMaxRetries() {
    return MaxRetries.value();
  }

  public static Integer getRetryInterval() {
    return RetryInterval.value();
  }

  public static String getLog4jPropertiesFilePath() {
    return Log4jPropertiesFilePath.value();
  }

}
