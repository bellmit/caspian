package com.emc.caspian.ccs.workflow.model;

/**
 * QueueMessage wraps queue message meta-data Created by gulavb on 4/5/2015.
 */
public class QueueMessage {

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public long getCreationTime() {
    return creationTime;
  }

  public void setCreationTime(long creationTime) {
    this.creationTime = creationTime;
  }

  public long getLeaseTime() {
    return leaseTime;
  }

  public void setLeaseTime(long leaseTime) {
    this.leaseTime = leaseTime;
  }

  public long getLeasePeriod() {
    return leasePeriod;
  }

  public void setLeasePeriod(long leasePeriod) {
    this.leasePeriod = leasePeriod;
  }

  public int getLeaseCount() {
    return leaseCount;
  }

  public void setLeaseCount(int leaseCount) {
    this.leaseCount = leaseCount;
  }

  public String getHandle() {
    return handle;
  }

  public void setHandle(String handle) {
    this.handle = handle;
  }

  String id;
  String message;
  long creationTime;
  long leaseTime;
  long leasePeriod;
  int leaseCount;
  String handle;
}
