package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.workflow.model.BlobMetadata;
import com.emc.caspian.ccs.workflow.model.BlobStore;
import com.emc.caspian.ccs.workflow.model.DbResponse;

import java.io.OutputStream;

/**
 * Created by gulavb on 4/6/2015.
 */
public class MySQLBlobStore extends BlobStore {

  @Override
  public DbResponse<BlobMetadata> put() {
    return null;
  }

  @Override
  public DbResponse<OutputStream> get(String blobId) {
    return null;
  }

  @Override
  public DbResponse<Boolean> copyLocal(String blobId, String localFilePath) {
    return null;
  }
}
