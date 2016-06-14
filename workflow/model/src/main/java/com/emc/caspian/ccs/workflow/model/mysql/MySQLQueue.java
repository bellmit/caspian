package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.common.utils.ExceptionHelper;
import com.emc.caspian.ccs.workflow.model.*;
import com.emc.caspian.ccs.workflow.types.QueueType;
import com.emc.caspian.ccs.common.utils.Validator;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.OutParameters;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;
import java.util.UUID;

/**
 * Created by gulavb on 4/6/2015.
 */
public class MySQLQueue extends Queue {

  @Override
  public DbResponse<Boolean> put(QueueType queueType, String message) {
    Validator.validateNotEmpty(message, ErrorMessages.MESSAGE_NOT_EMPTY);
    DbResponse<Boolean> response = MySQLTable.insert(
        String.format(putSQL, getTableName(queueType)),
        message,
        System.currentTimeMillis()
    );
    return response;
  }

  @Override
  public DbResponse<Boolean> put(QueueType queueType, String message, long retryInterval) {
    Validator.validateNotEmpty(message, ErrorMessages.MESSAGE_NOT_EMPTY);
    DbResponse<Boolean> response = MySQLTable.insert(
        String.format(putSQLWithRetryInterval, getTableName(queueType)),
        message,
        System.currentTimeMillis(),
        retryInterval,
        UUID.randomUUID().toString()
    );
    return response;
  }

  @Override
  public DbResponse<QueueMessage> get(QueueType queueType) {
    return get(queueType, defaultVisibilityTimeout);
  }

  @Override
  public DbResponse<QueueMessage> get(QueueType queueType, long visibilityTimeout) {
    DbResponse<QueueMessage> response = new DbResponse<QueueMessage>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {

      long leaseTime = System.currentTimeMillis();
      long leasePeriod = visibilityTimeout;
      String messageHandle = UUID.randomUUID().toString();
      OutParameters outParameters =
          handle.createCall(getSP(queueType))
              .bind(0, leaseTime)
              .bind(1, leasePeriod)
              .bind(2, messageHandle)
              .registerOutParameter(3, Types.VARCHAR)
              .registerOutParameter(4, Types.VARCHAR)
              .registerOutParameter(5, Types.BIGINT)
              .invoke();

      if (outParameters == null) {
        response.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
        response.setErrorMessage(ErrorMessages.NOT_FOUND);
        //AppLogger.debug("Account " + id + " not found");
      } else {
        String id = outParameters.getString(4);
        if (id == null) {
          response.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
          response.setErrorMessage(ErrorMessages.NOT_FOUND);
          //AppLogger.debug("Account " + id + " not found");
        } else {
          QueueMessage queueMessage = new QueueMessage();
          queueMessage.setId(id);
          queueMessage.setMessage(outParameters.getString(5));
          queueMessage.setCreationTime(outParameters.getLong(6));
          queueMessage.setLeaseTime(leaseTime);
          queueMessage.setLeasePeriod(leasePeriod);
          queueMessage.setHandle(messageHandle);
          response.setResponseObj(queueMessage);
        }
      }
    } catch (DBIException e) {
      logger.error(
          "Error fetching queue message from " + queueType + " queue. " + ExceptionHelper.printExceptionCause(e));
      response.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      response.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
    }
    return response;
  }

  @Override
  public DbResponse<Boolean> updateLease(QueueType queueType, String id, String handle, long visibilityTimeout) {
    Validator.validateNotEmpty(id, ErrorMessages.ID_EMPTY);
    Validator.validateNotEmpty(handle, ErrorMessages.HANDLE_EMPTY);
    DbResponse<Boolean> response = MySQLTable.update(
        String.format(updateLeaseSQL, getTableName(queueType)),
        visibilityTimeout,
        id,
        handle,
        System.currentTimeMillis()
    );
    return response;
  }

  @Override
  public DbResponse<Boolean> delete(QueueType queueType, String id, String handle) {
    Validator.validateNotEmpty(id, ErrorMessages.ID_EMPTY);
    Validator.validateNotEmpty(handle, ErrorMessages.HANDLE_EMPTY);
    DbResponse<Boolean> response = MySQLTable.delete(
        String.format(deleteSQL, getTableName(queueType)),
        id,
        handle,
        System.currentTimeMillis()
    );
    return response;
  }

  private String getTableName(QueueType queueType) {
    if (queueType == QueueType.TaskQueue) {
      return taskQueueTableName;
    } else if (queueType == QueueType.TaskCompletionQueue) {
      return taskCompletionQueueTableName;
    } else {
      return null;
    }
  }

  private String getSP(QueueType queueType) {
    if (queueType == QueueType.TaskQueue) {
      return getTaskQueueSP;
    } else if (queueType == QueueType.TaskCompletionQueue) {
      return getTaskCompletionQueueSP;
    } else {
      return null;
    }
  }

  // TODO: instead of passing current time for setting creationTime and leaseTime in DB
  // TODO: manage them in the DB itself using CURRTIME(), or NOW(), or UNIX_TIMESTAMP()

  private final static String putSQL =
      "insert into %s (id, message, creation_time, lease_count) " +
      "values (null, :message, :creation_time, 0)";

  private final static String putSQLWithRetryInterval =
      "insert into %s (id, message, creation_time, lease_count, retry_interval, handle) " +
      "values (null, :message, :creation_time, 0, :retryInterval, :handle)";

  private final static String ownershipCondition =
      "id = :id and handle = :handle and (:current_time <= lease_time + lease_period)";

  private final static String updateLeaseSQL =
      "update %s set lease_period = lease_period + :increment where " + ownershipCondition;

  private final static String getTaskQueueSP =
      "call get_from_task_queue(:a, :b, :c, :d, :e, :f)";

  private final static String getTaskCompletionQueueSP =
      "call get_from_task_completion_queue(:a, :b, :c, :d, :e, :f)";

  private final static String deleteSQL =
      "delete from %s where " + ownershipCondition;

  private final static String taskQueueTableName = "task_queue";

  private final static String taskCompletionQueueTableName = "task_completion_queue";

  // default 10 minutes
  private final static long defaultVisibilityTimeout = 600000;

  private static final Logger logger = LoggerFactory.getLogger(MySQLQueue.class);
}
