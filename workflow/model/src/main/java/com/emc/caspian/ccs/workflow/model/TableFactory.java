package com.emc.caspian.ccs.workflow.model;

import com.emc.caspian.ccs.workflow.model.mysql.*;

/**
 * TableFactory provides reference to table implementations Created by gulavb on 4/6/2015.
 */
public class TableFactory {

  public static BlobStore getBlobStore() {
    return new MySQLBlobStore();
  }

  public static Queue getQueue() {
    return new MySQLQueue();
  }

  public static JobTable getJobTable() {
    return new MySQLJobTable();
  }

  public static TaskFrameTable getTaskFrameTable() {
    return new MySQLTaskFrameTable();
  }

  public static TaskTable getTaskTable() {
    return new MySQLTaskTable();
  }

}
