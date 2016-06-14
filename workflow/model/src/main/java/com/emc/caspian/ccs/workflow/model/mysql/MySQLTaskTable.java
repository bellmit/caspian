package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.workflow.model.TaskModel;
import com.emc.caspian.ccs.workflow.model.TaskTable;
import com.emc.caspian.ccs.workflow.model.DbResponse;
import com.emc.caspian.ccs.workflow.model.ErrorMessages;
import com.emc.caspian.ccs.common.utils.Validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gulavb on 4/6/2015.
 */
public class MySQLTaskTable extends TaskTable {

  @Override
  public DbResponse<Boolean> insert(TaskModel taskModel) {
    validate(taskModel);
    DbResponse<Boolean> resp = MySQLTable.insert(
        insertTaskSQL,
        taskModel.getId(),
        taskModel.getName(),
        taskModel.getReturnType(),
        taskModel.getJarId(),
        taskModel.getParameters());
    return resp;
  }

  @Override
  public DbResponse<TaskModel> get(String id) {
    Validator.validateNotEmpty(id, ErrorMessages.ID_EMPTY);
    Map<String, String> map = new HashMap<String, String>();
    map.put("id", id);
    DbResponse<TaskModel> resp = MySQLTable.get(TaskModel.class, findTaskSQL, map);
    return resp;
  }

  @Override
  public DbResponse<TaskModel> getByName(String name) {
    Validator.validateNotEmpty(name, ErrorMessages.NAME_EMPTY);
    Map<String, String> map = new HashMap<String, String>();
    map.put("name", name);
    DbResponse<TaskModel> resp = MySQLTable.get(TaskModel.class, findTaskByNameSQL, map);
    return resp;
  }

  @Override
  public DbResponse<List<TaskModel>> getAll() {
    DbResponse<List<TaskModel>> response = MySQLTable.getAll(TaskModel.class, listTasksSQL);
    return response;
  }

  @Override
  public DbResponse<Boolean> update(TaskModel taskModel) {
    validate(taskModel);
    DbResponse<Boolean> response = MySQLTable.update(
        updateTaskSQL,
        taskModel.getName(),
        taskModel.getReturnType(),
        taskModel.getJarId(),
        taskModel.getParameters(),
        taskModel.getId());
    return response;
  }

  @Override
  public DbResponse<Boolean> delete(String id) {
    Validator.validateNotEmpty(id, ErrorMessages.ID_EMPTY);
    DbResponse<Boolean> response = MySQLTable.delete(deleteTaskSQL, id);
    return response;
  }

  private void validate(TaskModel taskModel) {
    Validator.validateNotNull(taskModel);
    Validator.validateNotEmpty(taskModel.getId(), ErrorMessages.ID_EMPTY);
    Validator.validateNotEmpty(taskModel.getName(), ErrorMessages.NAME_EMPTY);
    Validator.validateNotEmpty(taskModel.getReturnType(), ErrorMessages.RETURN_TYPE_EMPTY);
  }

  private static final String listTasksSQL =
      "select id, name, return_type as returnType, jar_id as jarId, parameters from task";
  private static final String findTaskSQL =
      listTasksSQL + " where id = :id";
  private static final String findTaskByNameSQL =
      listTasksSQL + " where name = :name";
  private static final String
      insertTaskSQL =
      "insert into task (id, name, return_type, jar_id, parameters) values (:id, :name, :returnType, :jarId, :parameters)";
  private static final String
      updateTaskSQL =
      "update task set name = :name, return_type = :return_type, jar_id = :jar_id, parameters = :parameters where id = :id";
  private static final String deleteTaskSQL =
      "delete from task where id = :id";
}
