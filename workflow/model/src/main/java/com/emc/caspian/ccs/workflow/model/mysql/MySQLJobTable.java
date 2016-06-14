package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.workflow.model.DbResponse;
import com.emc.caspian.ccs.workflow.model.ErrorMessages;
import com.emc.caspian.ccs.workflow.model.JobModel;
import com.emc.caspian.ccs.workflow.model.JobTable;
import com.emc.caspian.ccs.common.utils.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gulavb on 4/8/2015.
 */
public class MySQLJobTable extends JobTable {

  @Override
  public DbResponse<Boolean> insert(JobModel jobModel) {
    validate(jobModel);
    DbResponse<Boolean> response = MySQLTable.insert(
        insertJobSQL,
        jobModel.getId(),
        jobModel.getTargetType(),
        jobModel.getTargetFrameId(),
        jobModel.getTargetName(),
        jobModel.getParameters(),
        jobModel.getEnvironment(),
        jobModel.getPriority(),
        jobModel.getStatus(),
        jobModel.getOutput(),
        jobModel.getErrStream(),
        jobModel.getOutStream(),
        jobModel.getCreationTime(),
        jobModel.getCompletionTime(),
        jobModel.getExecutionState()
    );
    return response;
  }

  @Override
  public DbResponse<JobModel> get(String id) {
    Validator.validateNotEmpty(id, ErrorMessages.ID_EMPTY);
    Map<String, String> map = new HashMap<String, String>();
    map.put("id", id);
    DbResponse<JobModel> response = MySQLTable.get(JobModel.class, findJobSQL, map);
    return response;
  }

  @Override
  public DbResponse<List<JobModel>> getAll() {
    DbResponse<List<JobModel>> response = MySQLTable.getAll(JobModel.class, findALLJobsSQL);
    return response;
  }

  @Override
  public DbResponse<Boolean> update(JobModel jobModel) {
    validate(jobModel);
    DbResponse<Boolean> response = MySQLTable.update(
        updateJobSQL,
        jobModel.getTargetType(),
        jobModel.getTargetFrameId(),
        jobModel.getTargetName(),
        jobModel.getParameters(),
        jobModel.getEnvironment(),
        jobModel.getPriority(),
        jobModel.getStatus(),
        jobModel.getOutput(),
        jobModel.getErrStream(),
        jobModel.getOutStream(),
        jobModel.getCreationTime(),
        jobModel.getCompletionTime(),
        jobModel.getExecutionState(),
        jobModel.getId()
    );
    return response;
  }

  @Override
  public DbResponse<Boolean> delete(String id) {
    Validator.validateNotEmpty(id, ErrorMessages.ID_EMPTY);
    DbResponse<Boolean> response = MySQLTable.delete(deleteJobSQL, id);
    return response;
  }

  private void validate(JobModel job) {
    Validator.validateNotNull(job);
    Validator.validateNotEmpty(job.getId());
    Validator.validateNotNull(job.getTargetType());
    Validator.validateNotEmpty(job.getTargetName());
    Validator.validateNotNull(job.getStatus());
  }

  private static final String insertJobSQL =
      "insert into job " +
      "(id, target_type, target_frame_id, target_name, parameters, environment, priority, status, output, " +
      "err_stream, out_stream, creation_time, completion_time, execution_state) " +
      "values " +
      "(:id, :target_type, :target_frame_id, :target_name, :parameters, :environment, :priority, :status, :output, " +
      ":err_stream, :out_stream, :creation_time, :completion_time, :execution_state)";

  private static final String findALLJobsSQL =
      "select id, target_type as targetType, target_frame_id as targetFrameId, target_name as targetName, " +
      "parameters, environment, priority, status, output, err_stream as errStream, out_stream as outStream, " +
      "creation_time as creationTime, completion_time as completionTime, " +
      "execution_state as executionState from job";

  private static final String findJobSQL =
      findALLJobsSQL + " where id = :id";

  private static final String
      updateJobSQL =
      "update job set target_type = :target_type, target_frame_id = :target_frame_id, target_name = :target_name, " +
      "parameters = :parameters, environment = :environment, priority = :priority, status = :status, output = :output, "
      +
      "err_stream = :err_stream, out_stream = :out_stream, creation_time = :creation_time, " +
      "completion_time = :completion_time, execution_state = :execution_state where id = :id";

  private static final String deleteJobSQL =
      "delete from job where id = :id";
}
