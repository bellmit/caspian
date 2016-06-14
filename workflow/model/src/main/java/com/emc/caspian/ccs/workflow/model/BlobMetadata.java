package com.emc.caspian.ccs.workflow.model;

/**
 * BlobMetaData wraps blob meta-data Created by gulavb on 4/6/2015.
 */
public class BlobMetadata {

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  String id;
  String name;
  long creationTime;
  int size;
  String hash;
}
