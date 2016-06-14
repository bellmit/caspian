package com.emc.caspian.ccs.workflow.model;

import java.util.List;

/**
 * TaskTable stores meta-data of tasks registered in the system Created by gulavb on 4/5/2015.
 */
public abstract class TaskTable {

  public abstract DbResponse<Boolean> insert(TaskModel taskModel);

  public abstract DbResponse<TaskModel> get(String id);

  public abstract DbResponse<TaskModel> getByName(String name);

  public abstract DbResponse<List<TaskModel>> getAll();

  public abstract DbResponse<Boolean> update(TaskModel taskModel);

  public abstract DbResponse<Boolean> delete(String id);
}
