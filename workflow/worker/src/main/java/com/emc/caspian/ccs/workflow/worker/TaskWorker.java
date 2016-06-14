package com.emc.caspian.ccs.workflow.worker;

import com.emc.caspian.ccs.common.utils.ExceptionHelper;
import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.workflow.TaskException;
import com.emc.caspian.ccs.workflow.TaskExecutor;
import com.emc.caspian.ccs.workflow.model.*;
import com.emc.caspian.ccs.workflow.types.QueueType;
import com.emc.caspian.ccs.workflow.types.Status;
import com.emc.caspian.ccs.workflow.types.Type;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.List;

/**
 * TaskWorker processes a message from the task queue. It does the following. (1) Fetch the task frame and task details
 * from the queue message (2) Validate the parameter types (3) Execute the task (4) Update task frame (5) Delete message
 * from task queue (6) Put message in task completion queue for subsequent processing Created by gulavb on 4/19/2015.
 */
public class TaskWorker extends MessageProcessor {

  public TaskWorker(QueueType queueType) {
    super(queueType);
  }

  public TaskWorker(
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

  public void processMessage(QueueMessage message) {
    logger.debug("Processing message id={}, message={}", message.getId(), message.getMessage());
    StringWriter out = new StringWriter();
    StringWriter err = new StringWriter();
    PrintWriter outStream = new PrintWriter(out);
    PrintWriter errStream = new PrintWriter(err);
    try {
      boolean success = fetchTaskFrame(message);
      if (success) {

        String taskId = taskFrame.getJobId();
        if (StringUtils.isNotEmpty(taskId)) {
          MDC.put(LoggerConstants.LOGGER_TRACKING_ID, taskId);
        }

        taskFrame.setStartTime(System.currentTimeMillis());

        setupTask();
        Object returnValue = executeTask(outStream, errStream);

        // set the output and status
        taskFrame.setOutStream(out.toString());
        taskFrame.setStatus(Status.Successful);
        taskFrame.setErrStream(err.toString());
        taskFrame.setOutput(returnValue.toString());
        taskFrame.setEndTime(System.currentTimeMillis());
      }
    } catch (InvocationTargetException e) {
      String error = null;
      Throwable cause = e.getCause();
      if (cause instanceof TaskException) {
        error = err.toString() + ExceptionHelper.printException((TaskException) cause);
        logger.warn(error);
        if (((TaskException) cause).isRetriable()) {
          taskFrame.setStatus(Status.FleetingError);
        } else {
          taskFrame.setStatus(Status.FatalError);
        }
      } else {
        error = err.toString() + ExceptionHelper.printException(e);
        logger.warn(error);
        taskFrame.setStatus(Status.FatalError);
      }
      taskFrame.setErrStream(error);
      taskFrame.setOutStream(out.toString());
      taskFrame.setEndTime(System.currentTimeMillis());
    } catch (Exception e) {
      String error = err.toString() + ExceptionHelper.printException(e);
      logger.warn(error);
      taskFrame.setStatus(Status.FatalError);
      taskFrame.setErrStream(error);
      taskFrame.setOutStream(out.toString());
      taskFrame.setEndTime(System.currentTimeMillis());
    }

    outStream.flush();
    errStream.flush();
    outStream.close();
    errStream.close();
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

  private void setupTask() {
    // fetch task details from TaskTable
    task = taskTable.get(taskFrame.getTaskId()).getResponseObj();
    Validator.validateNotNull(task);

    // copy necessary jars locally
    copyTaskLocally(task);
  }

  private Object executeTask(PrintWriter out, PrintWriter err)
      throws InvocationTargetException,
             MalformedURLException,
             InstantiationException,
             IllegalAccessException {
    // find and execute the task from the local jar
    logger.debug("Executing task {}, parameters {}", task.getName(), taskFrame.getParameters());
    TaskExecutor taskExecutor = new TaskExecutor(localJarPath);
    Object returnValue = taskExecutor.execute(out, err, taskFrame.getEnvironmentMap(), task.getName(),
                                              bindParameters());
    return returnValue;
  }

  private Object[] bindParameters() {
    logger.debug("Binding parameters");
    ParameterTypeBindings typeBindings = task.getParameterTypeBindings();
    ParameterBindings valueBindings = taskFrame.getParameterBindings();
    if (typeBindings == null && valueBindings == null) {
      return null;
    }
    if ((typeBindings == null && valueBindings != null) || (typeBindings != null && valueBindings == null)) {
      throw new IllegalArgumentException(ErrorMessages.ParameterMismatch);
    }
    List<ParameterTypeBinding> typeBindingList = typeBindings.getList();
    List<ParameterBinding> valueBindingList = valueBindings.getList();
    if (typeBindingList.size() != valueBindingList.size()) {
      throw new IllegalArgumentException(ErrorMessages.ParameterMismatch);
    }
    int size = typeBindingList.size();
    Object[] parameters = new Object[size];
    for (int i = 0; i < size; i++) {
      Type type = typeBindingList.get(i).getType();
      String value = valueBindingList.get(i).getValue();
      Object parameter = null;
      if (type == Type.Boolean) {
        parameter = parseBoolean(value);
      } else if (type == Type.Integer) {
        parameter = Integer.parseInt(value);
      } else if (type == Type.String) {
        parameter = value;
      } else if (type != Type.String) {
        throw new IllegalArgumentException(ErrorMessages.UnsupportedTypeValue);
      }
      parameters[i] = parameter;
    }
    return parameters;
  }

  @Override
  public void completeMessage(QueueMessage message) {

    if (taskFrame != null) {
      // store the task frame back
      taskFrameTable.update(taskFrame);

      // indicate completion by sending message to completionQueue
      queue.put(QueueType.TaskCompletionQueue, message.getMessage());
    }

    //purge message from TaskQueue
    queue.delete(QueueType.TaskQueue, message.getId(), message.getHandle());

    localJarPath = null;
    task = null;
    taskFrame = null;
  }

  private void copyTaskLocally(TaskModel task) {
    String jarId = task.getJarId();
    if (jarId != null && !jarId.isEmpty()) {
      logger.debug("Initiating jar copy");
      localJarPath = task.getJarId() + jarSuffix;
      blobStore.copyLocal(task.getJarId(), localJarPath);
    }
  }

  private Boolean parseBoolean(String string) {
    if (string == null || string.isEmpty()) {
      throw new IllegalArgumentException("Invalid boolean");
    } else if (string.equalsIgnoreCase("true")) {
      return true;
    } else if (string.equalsIgnoreCase("false")) {
      return false;
    } else {
      throw new IllegalArgumentException("Invalid boolean");
    }
  }

  private String localJarPath;
  private TaskFrameModel taskFrame;
  private TaskModel task;
  private final static String jarSuffix = ".jar";

  private static final Logger logger = LoggerFactory.getLogger(TaskWorker.class);
}
