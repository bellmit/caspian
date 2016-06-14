package com.emc.caspian.ccs.account.model.mysql;

import java.util.List;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.emc.caspian.ccs.account.model.AccountModel;
import com.emc.caspian.ccs.account.model.AccountTable;
import com.emc.caspian.ccs.account.model.DbResponse;
import com.emc.caspian.ccs.account.model.ErrorCode;
import com.emc.caspian.ccs.account.model.ErrorMessages;
import com.emc.caspian.ccs.account.model.JobModel;
import com.emc.caspian.ccs.common.utils.Validator;

public class MySQLAccountTableV1 implements AccountTable {

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
          handle.createQuery(findAccountSQLV1).bind("id", id).map(AccountModel.class).list();

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

  @Override
  public DbResponse<List<AccountModel>> getAccountDomainsWithEnhancedInfo() {
    DbResponse<List<AccountModel>> resp = new DbResponse<List<AccountModel>>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      List<AccountModel> accountModels = handle.createQuery(listAccountsAndDomainsSQLV1).map(AccountModel.class).list();
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

  @Override
  public DbResponse<List<AccountModel>> getAccounts() {
    DbResponse<List<AccountModel>> resp = new DbResponse<List<AccountModel>>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      List<AccountModel> accountModels = handle.createQuery(listAccountsSQLV1).map(AccountModel.class).list();
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

  
  private final String findAccountSQLV1 = "select account_id as id, state from account where id = :id";
  private final String listAccountsSQLV1 = "select account_id as id, state from account";
  private final String listAccountsAndDomainsSQLV1 = "select account_id, id from account";

}
