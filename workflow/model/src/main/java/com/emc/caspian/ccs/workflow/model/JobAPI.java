package com.emc.caspian.ccs.workflow.model;

import com.emc.caspian.ccs.workflow.types.JobType;
import com.emc.caspian.ccs.workflow.types.QueueType;
import com.emc.caspian.ccs.workflow.types.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * JobAPI provides wrapper for submitting jobs to the system. Created by gulavb on 4/19/2015.
 */
public class JobAPI {

  /**
   * Submit a new job and return the meta-data of submitted job
   *
   * @param taskName   name of task to execute
   * @param parameters parameters for the task
   * @return job meta-data
   */
  public static DbResponse<JobModel> submitTask(Map<String, String> environment, String taskName,
                                                String... parameters) {
    DbResponse<JobModel> response = new DbResponse<JobModel>();

    // validate task name
    DbResponse<TaskModel> taskResponse = taskTable.getByName(taskName);
    TaskModel taskModel = taskResponse.getResponseObj();
    if (taskModel == null) {
      response.setErrorCode(taskResponse.getErrorCode());
      response.setErrorMessage(taskResponse.getErrorMessage());
      response.setResponseObj(null);
      return response;
    }

    // validate parameters
    ParameterTypeBindings typeBindings = taskModel.getParameterTypeBindings();
    List<ParameterTypeBinding> typeBindingsList = typeBindings.getList();
    int typeSize = typeBindingsList != null ? typeBindingsList.size() : 0;
    int valueSize = parameters != null ? parameters.length : 0;
    if (typeSize != valueSize) {
      response.setErrorCode(ErrorCode.DB_SYNTAX_ERROR);
      response.setErrorMessage(String.format(ErrorMessages.INVALID_PARAMETERS, typeSize, valueSize));
      return response;
    }

    // create parameter bindings
    List<ParameterBinding> list = new ArrayList<ParameterBinding>();
    if (valueSize > 0) {
      for (int i = 0; i < valueSize; i++) {
        ParameterBinding binding =
            new ParameterBinding(typeBindingsList.get(i).getName(), parameters[i]);
        list.add(binding);
      }
    }
    ParameterBindings bindings = new ParameterBindings(list);

    // create job model
    String jobId = UUID.randomUUID().toString();
    String taskFrameId = UUID.randomUUID().toString();

    JobModel jobModel = new JobModel();
    jobModel.setId(jobId);
    jobModel.setTargetType(JobType.Task);
    jobModel.setTargetFrameId(taskFrameId);
    jobModel.setTargetName(taskName);
    jobModel.setParameters(Arrays.asList(parameters));
    jobModel.setEnvironment(environment);
    jobModel.setStatus(Status.Running);
    jobModel.setCreationTime(System.currentTimeMillis());

    // create task frame model
    TaskFrameModel taskFrameModel = new TaskFrameModel();
    taskFrameModel.setId(taskFrameId);
    taskFrameModel.setJobId(jobId);
    taskFrameModel.setTaskId(taskModel.getId());
    taskFrameModel.setParameters(bindings);
    taskFrameModel.setEnvironment(environment);
    taskFrameModel.setStatus(Status.Ready);
    taskFrameModel.setCreationTime(System.currentTimeMillis());

    // create queue message
    String queueMessage = taskFrameId;
    QueueType queueType = QueueType.TaskQueue;

    // insert job, if failure, return error
    DbResponse<Boolean> jobResponse = jobTable.insert(jobModel);
    if (jobResponse.getErrorCode() != null) {
      response.setErrorCode(jobResponse.getErrorCode());
      response.setErrorMessage(jobResponse.getErrorMessage());
      response.setResponseObj(null);
      return response;
    }

    // insert taskFrame, if failure, return error
    DbResponse<Boolean> taskFrameResponse = taskFrameTable.insert(taskFrameModel);
    if (taskFrameResponse.getErrorCode() != null) {
      response.setErrorCode(jobResponse.getErrorCode());
      response.setErrorMessage(jobResponse.getErrorMessage());
      response.setResponseObj(null);
      // delete job
      jobTable.delete(jobId);
      // return
      return response;
    }

    // insert queue, if failure, return error
    DbResponse<Boolean> queueResponse = queue.put(queueType, queueMessage);
    if (queueResponse.getErrorCode() != null) {
      response.setErrorCode(jobResponse.getErrorCode());
      response.setErrorMessage(jobResponse.getErrorMessage());
      response.setResponseObj(null);
      // delete job
      jobTable.delete(jobId);
      // delete task frame
      taskFrameTable.delete(taskFrameId);
      // return
      return response;
    }

    // return jobModel;
    response.setResponseObj(jobModel);
    return response;
  }

  /**
   * Fetch the status of submitted job
   *
   * @param id job identifier
   * @return job meta-data
   */
  public static DbResponse<JobModel> getJob(String id) {
    return jobTable.get(id);
  }

  private final static Queue queue = TableFactory.getQueue();
  private final static JobTable jobTable = TableFactory.getJobTable();
  private final static TaskFrameTable taskFrameTable = TableFactory.getTaskFrameTable();
  private final static TaskTable taskTable = TableFactory.getTaskTable();
}
