package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.workflow.model.TaskFrameModel;
import com.emc.caspian.ccs.workflow.model.TaskFrameTable;
import com.emc.caspian.ccs.workflow.model.DbResponse;
import com.emc.caspian.ccs.workflow.model.ErrorMessages;
import com.emc.caspian.ccs.common.utils.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gulavb on 4/6/2015.
 */
public class MySQLTaskFrameTable extends TaskFrameTable {

  @Override
  public DbResponse<Boolean> insert(TaskFrameModel taskFrame) {
    validate(taskFrame);
    DbResponse<Boolean> response = MySQLTable.insert(
        insertTaskFrameSQL,
        taskFrame.getId(),
        taskFrame.getJobId(),
        taskFrame.getWorkflowId(),
        taskFrame.getTaskId(),
        taskFrame.getParameters(),
        taskFrame.getEnvironment(),
        taskFrame.getPriority(),
        taskFrame.getStatus(),
        taskFrame.getOutput(),
        taskFrame.getErrStream(),
        taskFrame.getOutStream(),
        taskFrame.getCreationTime(),
        taskFrame.getStartTime(),
        taskFrame.getEndTime(),
        taskFrame.getAttemptCounter(),
        taskFrame.getPreviousAttemptId()
    );
    return response;
  }

  @Override
  public DbResponse<TaskFrameModel> get(String id) {
    Validator.validateNotEmpty(id, ErrorMessages.ID_EMPTY);
    Map<String, String> map = new HashMap<String, String>();
    map.put("id", id);
    DbResponse<TaskFrameModel> response = MySQLTable.get(TaskFrameModel.class, findTaskFrameSQL, map);
    return response;
  }

  @Override
  public DbResponse<List<TaskFrameModel>> getAll() {
    DbResponse<List<TaskFrameModel>> response = MySQLTable.getAll(TaskFrameModel.class, findALLTaskFramesSQL);
    return response;
  }

  @Override
  public DbResponse<Boolean> update(TaskFrameModel taskFrame) {
    validate(taskFrame);
    DbResponse<Boolean> response = MySQLTable.update(
        updateTaskFrameSQL,
        taskFrame.getJobId(),
        taskFrame.getWorkflowId(),
        taskFrame.getTaskId(),
        taskFrame.getParameters(),
        taskFrame.getEnvironment(),
        taskFrame.getPriority(),
        taskFrame.getStatus(),
        taskFrame.getOutput(),
        taskFrame.getErrStream(),
        taskFrame.getOutStream(),
        taskFrame.getCreationTime(),
        taskFrame.getStartTime(),
        taskFrame.getEndTime(),
        taskFrame.getAttemptCounter(),
        taskFrame.getPreviousAttemptId(),
        taskFrame.getId()
    );
    return response;
  }

  @Override
  public DbResponse<Boolean> delete(String id) {
    Validator.validateNotEmpty(id, ErrorMessages.ID_EMPTY);
    DbResponse<Boolean> response = MySQLTable.delete(deleteTaskFrameSQL, id);
    return response;
  }

  private void validate(TaskFrameModel taskFrame) {
    Validator.validateNotNull(taskFrame);
    Validator.validateNotEmpty(taskFrame.getId(), ErrorMessages.ID_EMPTY);
    Validator.validateNotEmpty(taskFrame.getJobId(), ErrorMessages.JOB_ID_EMPTY);
    Validator.validateNotEmpty(taskFrame.getTaskId(), ErrorMessages.TASK_ID_EMPTY);
    Validator.validateNotNull(taskFrame.getStatus(), ErrorMessages.STATUS_EMPTY);
  }

  private final static String insertTaskFrameSQL =
      "insert into task_frame " +
      "(id, job_id, workflow_id, task_id, parameters, environment, priority, status, output, err_stream, out_stream, " +
      "creation_time, start_time, end_time, attempt_counter, previous_attempt_id) " +
      "values " +
      "(:id, :job_id, :workflow_id, :task_id, :parameters, :environment, :priority, :status, :output, :err_stream, :out_stream, "
      +
      ":creation_time, :start_time, :end_time, :attempt_counter, :previous_attempt_id)";

  private final static String findALLTaskFramesSQL =
      "select id, job_id as jobId, workflow_id as workflowId, task_id as taskId, parameters, environment, " +
      "priority, status, output, err_stream as errStream, " +
      "out_stream as outStream, creation_time as creationTime, start_time as startTime, " +
      "end_time as endTime, attempt_counter as attemptCounter, previous_attempt_id as previousAttemptId " +
      "from task_frame";

  private final static String findTaskFrameSQL = findALLTaskFramesSQL + " where id = :id";

  private final static String updateTaskFrameSQL =
      "update task_frame set job_id = :job_id, workflow_id = :workflow_id, task_id = :taskId, " +
      "parameters = :parameters, environment = :environment, priority = :priority, " +
      "status = :status, output = :output, err_stream = :errStream, out_stream = :outStream, " +
      "creation_time = :creationTime, start_time = :startTime, end_time = :endTime, " +
      "attempt_counter = :attempt_counter, previous_attempt_id = :previousAttemptId where id = :id";

  private final static String deleteTaskFrameSQL =
      "delete from task_frame where id = :id";
}
