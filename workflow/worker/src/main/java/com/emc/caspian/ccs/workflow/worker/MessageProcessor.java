package com.emc.caspian.ccs.workflow.worker;

import com.emc.caspian.ccs.workflow.model.*;
import com.emc.caspian.ccs.workflow.types.QueueType;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessageProcessor is an abstract class that fetches a message from the configured queue and processes that message
 * Created by gulavb on 4/19/2015.
 */
public abstract class MessageProcessor implements Runnable {

  public MessageProcessor(QueueType queueType) {
    this.queueType = queueType;
    this.blobStore = TableFactory.getBlobStore();
    this.queue = TableFactory.getQueue();
    this.jobTable = TableFactory.getJobTable();
    this.taskFrameTable = TableFactory.getTaskFrameTable();
    this.taskTable = TableFactory.getTaskTable();
    pollPeriod = 2000;
    // default 5 minutes
    leasePeriod = 300000;
  }

  public MessageProcessor(
      QueueType queueType,
      BlobStore blobStore,
      Queue queue,
      JobTable jobTable,
      TaskFrameTable taskFrameTable,
      TaskTable taskTable,
      long pollPeriod,
      long leasePeriod) {
    this.queueType = queueType;
    this.blobStore = blobStore;
    this.queue = queue;
    this.jobTable = jobTable;
    this.taskFrameTable = taskFrameTable;
    this.taskTable = taskTable;
    this.pollPeriod = pollPeriod;
    this.leasePeriod = leasePeriod;
  }

  @Override
  public void run() {

    String threadId = String.valueOf(Thread.currentThread().getId());
    if (StringUtils.isNotEmpty(threadId)) {
      MDC.put(LoggerConstants.LOGGER_THREAD_ID, threadId);
    }

    do {

      // Reset the previous task ID
      MDC.put(LoggerConstants.LOGGER_TRACKING_ID, LoggerConstants.LOGGER_RESET);

      // fetch a message from configured queue
      QueueMessage message = queue.get(queueType).getResponseObj();

      if (message != null) {
        logger.info("Received message " + message.getId());
        processMessage(message);
        completeMessage(message);
        logger.info("Completed message " + message.getId());
      } else {
        try {
          Thread.sleep(pollPeriod);
        } catch (InterruptedException e) {
          logger.warn("Received interrupt signal, stopping");
          Thread.currentThread().interrupt();
        }
      }
      // clean up
      message = null;
    } while (!Thread.currentThread().isInterrupted());
  }

  public abstract void processMessage(QueueMessage message);

  public abstract void completeMessage(QueueMessage message);

  protected final BlobStore blobStore;
  protected final JobTable jobTable;
  protected final TaskFrameTable taskFrameTable;
  protected final TaskTable taskTable;
  protected final Queue queue;
  protected final QueueType queueType;
  protected final long pollPeriod;
  protected final long leasePeriod;
  
  private static final Logger logger = LoggerFactory.getLogger(MessageProcessor.class);
}
