package com.emc.caspian.ccs.workflow.model;

import com.emc.caspian.ccs.workflow.types.QueueType;

/**
 * Queue provides store for messages and implements queue interface, put, get, delete. This interface simulates AWS SQS
 * and Azure queues. Created by gulavb on 4/5/2015.
 */
public abstract class Queue {

  /**
   * Put the message in the specified queue
   *
   * @param queueType name of the queue
   * @param message   message text
   * @return status of operation
   */
  public abstract DbResponse<Boolean> put(QueueType queueType, String message);

  /**
   * Put the message in the specified queue with retry interval
   *
   * @param queueType name of the queue
   * @param message   message text
   * @param retry interval
   * @return status of operation
   */
  public abstract DbResponse<Boolean> put(QueueType queueType, String message, long retryInterval);

  /**
   * This method picks the oldest message from the queue and locks it for 60 seconds for processing by the requester. If
   * the message is not deleted within 60 seconds, it shall be made available for any other requester.
   *
   * @param queueType name of the queue
   * @return leased queue message
   */
  public abstract DbResponse<QueueMessage> get(QueueType queueType);

  /**
   * This method picks the oldest message from the queue and locks it for specified time interval for processing by the
   * requester. If the message is not deleted within specified time interval 60 seconds, it shall be made available for
   * any other requester.
   *
   * @param queueType         name of the queue
   * @param visibilityTimeout lease time for which the message is sought
   * @return leased queue message
   */
  public abstract DbResponse<QueueMessage> get(QueueType queueType, long visibilityTimeout);

  /**
   * Increases the lease duration by visibilityTimeout time is the message identified by id is currently leased out to
   * the requester. The requester is identified by handle.
   *
   * @param queueType         name of the queue
   * @param id                message identifier
   * @param handle            unique lease identifier
   * @param visibilityTimeout additional time requested by requester
   * @return status of operation
   */
  public abstract DbResponse<Boolean> updateLease(QueueType queueType, String id, String handle,
                                                  long visibilityTimeout);

  /**
   * Deletes the message if it is currently leased out to a requester. The requester is identified by handle.
   *
   * @param queueType name of the queue
   * @param id        message identifier
   * @param handle    unique lease identifier
   * @return status of operation
   */
  public abstract DbResponse<Boolean> delete(QueueType queueType, String id, String handle);

}
