package com.emc.caspian.ccs.workflow.model;

import java.io.OutputStream;

/**
 * Simulates a blob store by providing methods to put a blob, get its details and copy it on local file system Created
 * by gulavb on 4/6/2015.
 */
public abstract class BlobStore {

  public abstract DbResponse<BlobMetadata> put();

  public abstract DbResponse<OutputStream> get(String blobId);

  public abstract DbResponse<Boolean> copyLocal(String blobId, String localFilePath);

}
