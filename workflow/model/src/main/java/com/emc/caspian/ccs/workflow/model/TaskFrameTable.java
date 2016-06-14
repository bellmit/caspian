package com.emc.caspian.ccs.workflow.model;

import java.util.List;

/**
 * TaskFrameTable stores task frames Created by gulavb on 4/5/2015.
 */
public abstract class TaskFrameTable {

  public abstract DbResponse<Boolean> insert(TaskFrameModel taskFrame);

  public abstract DbResponse<TaskFrameModel> get(String id);

  public abstract DbResponse<List<TaskFrameModel>> getAll();

  public abstract DbResponse<Boolean> update(TaskFrameModel taskFrame);

  public abstract DbResponse<Boolean> delete(String id);
}
