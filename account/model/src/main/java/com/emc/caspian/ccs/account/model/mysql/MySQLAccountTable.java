/*
 * 
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.model.mysql;


import java.util.List;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;
import com.emc.caspian.ccs.account.model.AccountModel;
import com.emc.caspian.ccs.account.model.AccountTable;
import com.emc.caspian.ccs.account.model.DbResponse;
import com.emc.caspian.ccs.account.model.ErrorCode;
import com.emc.caspian.ccs.account.model.ErrorMessages;
import com.emc.caspian.ccs.account.model.JobModel;
import com.emc.caspian.ccs.common.utils.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements mysql operations on account table. Created by gulavb on 2/28/2015.
 */

public class MySQLAccountTable implements AccountTable {

  private static final Logger _log = LoggerFactory.getLogger(MySQLAccountTable.class);

  /**
   * Fetch a row from account table
   * 
   * @param id Identifier of the account row to be fetched
   * @return Row from account table
   */
  @Override
  public DbResponse<AccountModel> getAccount(String id) {

    Validator.validateNotEmpty(id, ErrorMessages.ACCOUNT_ID_EMPTY);

    DbResponse<AccountModel> resp = new DbResponse<AccountModel>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {

      List<AccountModel> accountModels =
          handle.createQuery(findAccountSQL).bind("id", id).map(AccountModel.class).list();

      if (accountModels != null) {
        if (accountModels.isEmpty()) {
          resp.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
          resp.setErrorMessage(String.format(ErrorMessages.ACCOUNT_NOT_FOUND, id));
          _log.warn("Account " + id + " not found");
        } else {
          _log.debug(String.format("Succesfully recieved account %s", id));
          resp.setResponseObj(accountModels.get(0));
        }
      } else {
        _log.warn(String.format("An internal error occured while getting account %s", id));
        resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
      }
    } catch (DBIException e) {
      _log.warn("An error encountered while fetching account " + id + ". Exception " + e.getMessage());
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
    }
    return resp;
  }

  /**
   * List all rows of account table
   * 
   * @return Rows from account table
   */
  @Override
  public DbResponse<List<AccountModel>> getAccounts() {
    DbResponse<List<AccountModel>> resp = new DbResponse<List<AccountModel>>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      List<AccountModel> accountModels = handle.createQuery(listAccountsSQL).map(AccountModel.class).list();
      resp.setResponseObj(accountModels);
      if (accountModels != null) {
        _log.debug("Successfully retrived list of accounts");
        resp.setResponseObj(accountModels);
      } else {
        // when the account-models is null we know an internal error has occurred
        resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
        _log.warn("An internal error occured while fetching list of accounts");
      }
    } catch (DBIException e) {
      _log.warn("An error encountered in fetching list of accounts. Exception " + e.getMessage());
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
    }
    return resp;
  }

  /**
   * Add a row to account table.
   * 
   * @param accountModel Object having values for fields of the account row
   * @return True/false
   */
  @Override
  public DbResponse<Boolean> addAccount(final AccountModel accountModel) {

    Validator.validateNotNull(accountModel);
    Validator.validateNotEmpty(accountModel.getId(), ErrorMessages.ACCOUNT_ID_EMPTY);

    DbResponse<Boolean> resp = new DbResponse<Boolean>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      int rowsInserted =
          handle.insert(insertAccountSQL, accountModel.getId(), accountModel.getState());
      if (rowsInserted != 1) {
        resp.setResponseObj(false);
        resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
        _log.warn(String.format("An internal error occured while inserting account %s", accountModel.getId()));
      } else {
        _log.debug(String.format("Successfully inserted account %s", accountModel.getId()));
        resp.setResponseObj(true);
      }
    } catch (DBIException e) {
      _log.warn("An error encountered while inserting account " + accountModel.getId() + ". Exception "
          + e.getMessage());
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setResponseObj(false);
      if (resp.getErrorCode().equals(ErrorCode.DB_RECORD_DUPLICATE)) {
        resp.setErrorMessage(ErrorMessages.ACCOUNT_INSERT_ERROR);
      } else {
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
      }
    }
    return resp;
  }

  /**
   * Delete the account identified by input id
   * 
   * @param id Identifier of the Account to be deleted
   * @return True/false
   */
  @Override
  public DbResponse<Boolean> removeAccount(final String id) {

    Validator.validateNotEmpty(id, ErrorMessages.ACCOUNT_ID_EMPTY);

    DbResponse<Boolean> resp = new DbResponse<Boolean>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {

      int rowsDeleted = handle.insert(deleteAccountSQL, id);
      if (rowsDeleted == 0) {
        resp.setResponseObj(false);
        resp.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
        resp.setErrorMessage(String.format(ErrorMessages.ACCOUNT_NOT_FOUND, id));
        _log.warn(String.format("Account %s not found while deleting it", id));
      } else {
        _log.debug(String.format("Successfully deleted account %s", id));
        resp.setResponseObj(true);
      }
    } catch (DBIException e) {
      _log.warn("An error encountered while deleting account " + id + ". Exception " + e.getMessage());
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setResponseObj(false);
      resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
    }
    return resp;
  }

  @Override
  public DbResponse<AccountModel> changeAccountState(final String id, String state) {

    Validator.validateNotEmpty(id, ErrorMessages.ACCOUNT_ID_EMPTY);
    DbResponse<AccountModel> resp = new DbResponse<AccountModel>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      int rowsDeleted = handle.update(deactivateAccountSQL, state, id);
      if (rowsDeleted != 1) {
        // This condition will be true for two cases,
        // 1. if the account is not found
        // 2. if the account is already de-activated
        // Hence setting the error status to Internal error and
        // the error situation is logged
        resp.setResponseObj(null);
        resp.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
        _log.warn("Account " + id + " not found while deactivating it");
        resp.setErrorMessage(String.format(ErrorMessages.ACCOUNT_NOT_FOUND, id));
      } else {
        _log.debug("Successfully deactivated account " + id);
        List<AccountModel> accountModels =
            handle.createQuery(findAccountSQL).bind("id", id).map(AccountModel.class).list();
        resp.setResponseObj(accountModels.get(0));
      }
    } catch (DBIException e) {
      _log.warn("An error encountered while deactivating account " + id + ". Exception " + e.getMessage());
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setResponseObj(null);
      if (resp.getErrorCode().equals(ErrorCode.DB_RECORD_CONSTRAINT_VIOLATION)) {
        resp.setErrorMessage(String.format(ErrorMessages.DB_RECORD_CONSTRAINT_VIOLATION, id));
      } else {
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
      }
    }
    return resp;
  }

  @Override
  public DbResponse<JobModel> getTaskStatusForAccount(String taskId) {
    Validator.validateNotEmpty(taskId, ErrorMessages.TASK_ID_EMPTY);

    DbResponse<JobModel> resp = new DbResponse<JobModel>();

    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandlerForWorkflowDb().open()) {
      List<JobModel> jobTaskModels = handle.createQuery(getTaskStatus).bind("id", taskId).map(JobModel.class).list();

      if (jobTaskModels != null) {
        if (jobTaskModels.isEmpty()) {
          resp.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
          resp.setErrorMessage(String.format(ErrorMessages.ACCOUNT_WORKFLOW_TASK_NOT_FOUND, taskId));
          _log.warn("Task " + taskId + " not found");
        } else {
          resp.setResponseObj(jobTaskModels.get(0));
        }
      } else {
        _log.warn(String.format("An internal error occured while getting status of task %s", taskId));
        resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
      }

    } catch (DBIException e) {
      _log.warn("An error encountered while fetching status of task " + taskId + ". Exception " + e.getMessage());
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
    }
    return resp;
  }


  private final String findAccountSQL = "select id, state from account where id = :id";
  private final String listAccountsSQL = "select id, state from account";
  private final String insertAccountSQL = "insert into account (id, state) values (:id, :state)";
  private final String deleteAccountSQL = "delete from account where id = :id";
  private final String deactivateAccountSQL = "update account set state = :state where id = :id";
  private final String getTaskStatus = "select * from job where id = :id";

}
