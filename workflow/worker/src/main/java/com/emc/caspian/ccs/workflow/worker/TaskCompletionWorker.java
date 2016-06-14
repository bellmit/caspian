package com.emc.caspian.ccs.workflow.worker;

import com.emc.caspian.ccs.workflow.model.*;
import com.emc.caspian.ccs.workflow.types.JobType;
import com.emc.caspian.ccs.workflow.types.QueueType;
import com.emc.caspian.ccs.workflow.types.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Task Completion worker takes a message from task completion queue and does one of the following (1) If the task has
 * failed, and the error is fleeting, it will schedule the task for re-execution, (2) Otherwise, if it is the last task
 * of the job the job would be marked complete else subsequent tasks shall be scheduled for execution Created by gulavb
 * on 4/19/2015.
 */
public class TaskCompletionWorker extends MessageProcessor {

  public TaskCompletionWorker(QueueType queueType) {
    super(queueType);
  }

  public TaskCompletionWorker(
      QueueType queueType,
      BlobStore blobStore,
      Queue queue,
      JobTable jobTable,
      TaskFrameTable taskFrameTable,
      TaskTable taskTable,
      long pollPeriod,
      long leasePeriod) {
    super(queueType, blobStore, queue, jobTable, taskFrameTable, taskTable, pollPeriod, leasePeriod);
  }

  @Override
  public void processMessage(QueueMessage message) {
    boolean success = fetchTaskFrame(message);
    if (success) {
      // mark the
      String jobId = taskFrame.getJobId();
      success = fetchJob(jobId);
      if (success) {
        if (taskFrame.getStatus() == Status.FleetingError
            && taskFrame.getAttemptCounter() < WorkerProperties.getMaxRetries()) {
          // reschedule the task
          rescheduleTaskFrame();
        } else if (job.getTargetType() == JobType.Task) {
          // mark the job complete
          job.setStatus(taskFrame.getStatus());
          job.setOutput(taskFrame.getOutput());
          job.setErrStream(taskFrame.getErrStream());
          job.setOutStream(taskFrame.getOutStream());
          job.setCompletionTime(System.currentTimeMillis());
          DbResponse<Boolean> response = jobTable.update(job);
          if (response.getErrorCode() != null) {
            logger.error("Failed to update job message id={}, job id={}", message.getId(), jobId);
          }
        }
      }
    }
  }

  private boolean fetchTaskFrame(QueueMessage message) {
    // fetch the task frame from queue message
    String taskFrameId = message.getMessage();
    if (taskFrameId == null || taskFrameId.isEmpty()) {
      logger.warn("Task Completion queue message is empty for id={}", message.getId());
      return false;
    }

    DbResponse<TaskFrameModel> response = taskFrameTable.get(taskFrameId);
    if (response.getErrorCode() != null) {
      logger.warn("Failed to find task frame for message id={}, message={}. Error code {}, error message {}",
                   new Object[]{
                       message.getId(),
                       message.getHandle(),
                       response.getErrorMessage(),
                       response.getErrorMessage()
                   });
      return false;
    } else {
      taskFrame = response.getResponseObj();
      if (taskFrame == null) {
        logger.warn("Failed to find task frame for message id={}, message={}. DB does not return error.",
                     message.getId(), message.getMessage());
        return false;
      } else {
        return true;
      }
    }
  }

  private boolean fetchJob(String id) {
    if (id == null || id.isEmpty()) {
      logger.warn("Failed to fetch job for empty id");
      return false;
    }

    DbResponse<JobModel> response = jobTable.get(id);
    if (response.getErrorCode() != null) {
      logger.warn("Failed to find job details for id={}. Error code={}, error message={}",
                   new Object[]{id, response.getErrorCode().toString(), response.getErrorMessage()});
      return false;
    } else {
      job = response.getResponseObj();
      if (job == null) {
        logger.warn("Failed to find job details for id={}. DB does not return error.");
        return false;
      } else {
        return true;
      }
    }
  }

  private void rescheduleTaskFrame() {
    // create a new task frame
    String id = UUID.randomUUID().toString();
    TaskFrameModel newTaskFrame = new TaskFrameModel();
    newTaskFrame.setId(id);
    newTaskFrame.setJobId(job.getId());
    newTaskFrame.setTaskId(taskFrame.getTaskId());
    newTaskFrame.setParameters(taskFrame.getParameters());
    newTaskFrame.setEnvironment(taskFrame.getEnvironmentMap());
    newTaskFrame.setStatus(Status.Ready);
    newTaskFrame.setCreationTime(System.currentTimeMillis());
    newTaskFrame.setPriority(taskFrame.getPriority());
    newTaskFrame.setAttemptCounter(taskFrame.getAttemptCounter() + 1);
    newTaskFrame.setPreviousAttemptId(taskFrame.getId());

    // insert it into table
    DbResponse<Boolean> response = taskFrameTable.insert(newTaskFrame);
    if (response.getErrorCode() != null) {
      String error = String.format(
          "Failed to schedule retriably failed task frame id=%s, error code=%s, error message=%s",
          taskFrame.getId(),
          response.getErrorCode().toString(),
          response.getErrorMessage());
      logger.warn(error);
    }

    // update job to point to this task frame
    job.setTargetFrameId(id);
    response = jobTable.update(job);
    if (response.getErrorCode() != null) {
      String error = String.format(
          "Failed to schedule retriably failed task frame id=%s, error code=%s, error message=%s",
          taskFrame.getId(),
          response.getErrorCode().toString(),
          response.getErrorMessage());
      logger.warn(error);
    }

    // put message into task_queue
    response = queue.put(QueueType.TaskQueue, id, WorkerProperties.getRetryInterval());
    if (response.getErrorCode() != null) {
      String error = String.format(
          "Failed to schedule retriably failed task frame id=%s, error code=%s, error message=%s",
          taskFrame.getId(),
          response.getErrorCode().toString(),
          response.getErrorMessage());
      logger.warn(error);
    }
  }

  @Override
  public void completeMessage(QueueMessage message) {
    DbResponse<Boolean> response =
        queue.delete(QueueType.TaskCompletionQueue, message.getId(), message.getHandle());
    if (response.getErrorCode() != null) {
      logger.warn("Failed to delete message from task completion queue, id={}, handle={}, error code={}, " +
                   "error message={}",
                   new Object[]{message.getId(), message.getHandle(), response.getErrorCode().toString(),
                                response.getErrorMessage()});
    }
    taskFrame = null;
    job = null;
  }

  private JobModel job;
  private TaskFrameModel taskFrame;

  private static final Logger logger = LoggerFactory.getLogger(TaskCompletionWorker.class);
}
