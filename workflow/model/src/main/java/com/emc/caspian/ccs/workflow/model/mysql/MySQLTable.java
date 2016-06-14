package com.emc.caspian.ccs.workflow.model.mysql;

import com.emc.caspian.ccs.common.utils.ExceptionHelper;
import com.emc.caspian.ccs.workflow.model.DbResponse;
import com.emc.caspian.ccs.workflow.model.ErrorCode;
import com.emc.caspian.ccs.workflow.model.ErrorMessages;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * MySQLTable provides implementation for basic insert, read, list, update and delete queries on MySQL tables. Created
 * by gulavb on 4/9/2015.
 */
public final class MySQLTable {

  public static DbResponse<Boolean> insert(String insertSQL, Object... args) {
    DbResponse<Boolean> resp = new DbResponse<Boolean>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      int rowsInserted = handle.insert(insertSQL, args);

      if (rowsInserted != 1) {
        resp.setResponseObj(false);
        resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        resp.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
      } else {
        resp.setResponseObj(true);
      }

    } catch (DBIException e) {
      logger.error("Error inserting row. " + ExceptionHelper.printExceptionCause(e));
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setResponseObj(false);
      if (resp.getErrorCode().equals(ErrorCode.DB_RECORD_DUPLICATE)) {
        resp.setErrorMessage(ErrorMessages.INSERT_ERROR);
      } else {
        resp.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
      }
    }
    return resp;
  }

  public static <T> DbResponse<T> get(final Class<T> tClass, String getSQL, Map<String, String> args) {
    DbResponse<T> response = new DbResponse<T>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {

      List<T> rows = handle
          .createQuery(getSQL)
          .bindFromMap(args)
          .map(tClass).list();

      if (rows != null) {
        if (rows.isEmpty()) {
          response.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
          response.setErrorMessage(ErrorMessages.NOT_FOUND);
          logger.debug("{} row satisfying the desired constraint not found", tClass.getName());
        } else {
          response.setResponseObj(rows.get(0));
        }
      } else {
        response.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        response.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
      }
    } catch (DBIException e) {
      logger.error("Error fetching " + tClass.getName() + " row. " + ExceptionHelper.printExceptionCause(e));
      response.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      response.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
    }
    return response;
  }

  public static <T> DbResponse<List<T>> getAll(final Class<T> tClass, String getAllSQL) {
    DbResponse<List<T>> resp = new DbResponse<List<T>>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      List<T> rows = handle
          .createQuery(getAllSQL)
          .map(tClass)
          .list();
      if (rows != null) {
        resp.setResponseObj(rows);
      } else {
        // rows is null => an internal error
        resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        resp.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
      }
    } catch (DBIException e) {
      logger.error("Error fetching " + tClass.getName() + " rows. Exception " + e.getMessage());
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
    }
    return resp;
  }

  public static DbResponse<Boolean> update(String updateSQL, Object... args) {
    DbResponse<Boolean> resp = new DbResponse<Boolean>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      int rowsUpdated = handle.update(updateSQL, args);
      if (rowsUpdated != 1) {
        resp.setResponseObj(false);
        resp.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
        // the user will use account id in the REST path to update a
        // particular account, hence using ID for error message
        resp.setErrorMessage(ErrorMessages.NOT_FOUND);
      } else {
        resp.setResponseObj(true);
      }
    } catch (DBIException e) {
      logger.error("Error updating row. " + ExceptionHelper.printExceptionCause(e));
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setResponseObj(false);
      if (resp.getErrorCode().equals(ErrorCode.DB_RECORD_DUPLICATE)) {
        resp.setErrorMessage(ErrorMessages.INSERT_ERROR);
      } else {
        resp.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
      }
    }
    return resp;
  }

  public static DbResponse<Boolean> delete(String deleteSQL, Object... args) {
    DbResponse<Boolean> resp = new DbResponse<Boolean>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      int rowsDeleted = handle.update(deleteSQL, args);
      if (rowsDeleted != 1) {
        resp.setResponseObj(false);
        resp.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
        resp.setErrorMessage(ErrorMessages.NOT_FOUND);
      } else {
        resp.setResponseObj(true);
      }
    } catch (DBIException e) {
      logger.error("Error deleting row. " + ExceptionHelper.printExceptionCause(e));
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setResponseObj(false);
      if (resp.getErrorCode().equals(ErrorCode.DB_RECORD_CONSTRAINT_VIOLATION)) {
        resp.setErrorMessage(ErrorMessages.CONSTRAINT_VIOLATION);
      } else {
        resp.setErrorMessage(ErrorMessages.INTERNAL_ERROR);
      }
    }
    return resp;
  }


  private static final Logger logger = LoggerFactory.getLogger(MySQLTable.class);
}
