package com.emc.caspian.ccs.workflow.model;

import java.util.List;

/**
 * JobTable stores meta-data of jobs submitted to the system Created by gulavb on 4/8/2015.
 */
public abstract class JobTable {

  public abstract DbResponse<Boolean> insert(JobModel jobModel);

  public abstract DbResponse<JobModel> get(String id);

  public abstract DbResponse<List<JobModel>> getAll();

  public abstract DbResponse<Boolean> update(JobModel jobModel);

  public abstract DbResponse<Boolean> delete(String id);
}
