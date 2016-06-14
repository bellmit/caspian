package com.emc.caspian.ccs.workflow.worker;

import com.emc.caspian.ccs.workflow.model.*;
import com.emc.caspian.ccs.workflow.types.QueueType;
import com.emc.caspian.fabric.config.Configuration;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by gulavb on 4/5/2015.
 */
public class Worker {

  public static void main(String[] args) throws Exception {
    logger.info("Initializing worker");

    // load the configuration file first
    try {
      Configuration.load(confFile);
    } catch (Exception e) {
      logger.error("Error loading configurations from {}", confFile);
      throw e;
    }
    
    // Initialize logger
    String loggerConfigPath = WorkerProperties.getLog4jPropertiesFilePath();
    if (loggerConfigPath != null && !loggerConfigPath.isEmpty()) {
      PropertyConfigurator.configure(loggerConfigPath);
    }

    BlobStore blobStore = TableFactory.getBlobStore();
    Queue queue = TableFactory.getQueue();
    JobTable jobTable = TableFactory.getJobTable();
    TaskFrameTable taskFrameTable = TableFactory.getTaskFrameTable();
    TaskTable taskTable = TableFactory.getTaskTable();

    TaskWorker taskWorker =
        new TaskWorker(QueueType.TaskQueue,
                       blobStore,
                       queue,
                       jobTable,
                       taskFrameTable,
                       taskTable,
                       pollPeriod,
                       leasePeriod);
    TaskCompletionWorker taskCompletionWorker =
        new TaskCompletionWorker(QueueType.TaskCompletionQueue,
                                 blobStore,
                                 queue,
                                 jobTable,
                                 taskFrameTable,
                                 taskTable,
                                 pollPeriod,
                                 leasePeriod);

    // create 2 threads
    logger.info("Creating task worker");
    taskThread = new Thread(taskWorker, taskThreadName);

    logger.info("Creating task completion worker");
    taskCompletionThread = new Thread(taskCompletionWorker, taskCompletionThreadName);

    // start them
    logger.info("Starting task worker");
    taskThread.start();

    logger.info("Starting task completion worker");
    taskCompletionThread.start();

    logger.info("Worker started");
  }

  public static void stop() {
    // interrupt the threads
    logger.info("Stopping task worker");
    taskThread.interrupt();

    logger.info("Stopping task completion worker");
    taskCompletionThread.interrupt();

    try {
      taskThread.join();
      taskCompletionThread.join();
      logger.info("Threads stopped");
    } catch (InterruptedException e) {
      logger.warn("Received interrupt signal, stopping");
      Thread.currentThread().interrupt();
    }
    logger.info("Worker stopped");
  }

  private static final long pollPeriod = 5000;
  private static final long leasePeriod = 300000;
  private static final String taskThreadName = "TaskThread";
  private static final String taskCompletionThreadName = "TaskCompletionThread";
  private static Thread taskThread;
  private static Thread taskCompletionThread;

  private static final String confFile = "conf/worker.conf";
  private static final Logger logger = LoggerFactory.getLogger(Worker.class);
}
