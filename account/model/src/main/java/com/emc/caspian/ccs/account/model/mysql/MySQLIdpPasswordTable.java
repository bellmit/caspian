package com.emc.caspian.ccs.account.model.mysql;

import java.util.List;

import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.DBIException;

import com.emc.caspian.ccs.account.model.IdpPasswordModel;
import com.emc.caspian.ccs.account.model.DbResponse;
import com.emc.caspian.ccs.account.model.ErrorCode;
import com.emc.caspian.ccs.account.model.ErrorMessages;
import com.emc.caspian.ccs.common.utils.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.account.model.IdpPasswordTable;


public class MySQLIdpPasswordTable implements IdpPasswordTable {

  private static final Logger _log = LoggerFactory.getLogger(MySQLIdpPasswordTable.class);


  /**
   * Fetch a row from IdpPwd table
   * 
   * @param idpId Identifier of the idp row to be fetched
   * @return Row from IdpPwd table
   */
  @Override
  public DbResponse<IdpPasswordModel> getPassword(String idpId) {

    Validator.validateNotEmpty(idpId, ErrorMessages.IDP_ID_EMPTY);

    DbResponse<IdpPasswordModel> resp = new DbResponse<IdpPasswordModel>();

    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {

      List<IdpPasswordModel> idpPasswordModels =
          handle.createQuery(findPwdFromIdpSQL).bind("idpId", idpId).map(IdpPasswordModel.class).list();

      if (idpPasswordModels != null)
        if (idpPasswordModels.isEmpty()) {
          resp.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
          resp.setErrorMessage(String.format(ErrorMessages.IDP_NOT_FOUND_IN_DATABASE, idpId));
          _log.warn(String.format("idp %s not found in database", idpId));
        } else {
          if (idpPasswordModels.size() > 1) {
            _log.warn(String.format("Multiple entries found for idp %s", idpId));
            resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
            resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
          } else {
            _log.debug("Successfully retrieved idp {} from database ", idpId);
            resp.setResponseObj(idpPasswordModels.get(0));
          }
        }
      else {
        _log.warn(String.format("An internal error occured while getting password for idp %s", idpId));
        resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
      }
    } catch (DBIException e) {
      _log.warn(String.format("An error encountered while fetching password from idp %s . Exception %s ", idpId,
          e.getMessage()));
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
    }
    return resp;
  }



  /**
   * Add a row to IdpPwd table.
   * 
   * @param IdpPasswordModel Object having values for fields of the IdpPwd row
   * @return True/false
   */

  @Override
  public DbResponse<Boolean> addIdpPassword(IdpPasswordModel IdpPasswordModel) {

    Validator.validateNotNull(IdpPasswordModel);
    Validator.validateNotEmpty(IdpPasswordModel.getIdpId(), ErrorMessages.IDP_ID_EMPTY);
    Validator.validateNotEmpty(IdpPasswordModel.getIdpPwd(), ErrorMessages.IDP_PASSWORD_EMPTY);
    Validator.validateNotEmpty(IdpPasswordModel.getIdpUser(), ErrorMessages.IDP_USER_EMPTY);

    DbResponse<Boolean> resp = new DbResponse<Boolean>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {

      List<IdpPasswordModel> idpPasswordModels =
          handle.createQuery(findPwdFromIdpSQL).bind("idpId", IdpPasswordModel.getIdpId())
              .map(IdpPasswordModel.class).list();
      if (!idpPasswordModels.isEmpty()) {
        DbResponse<Boolean> response = removeIdpPassword(IdpPasswordModel.getIdpId());
        boolean result = response.getResponseObj();
        if (result != true) {
          _log.warn("Failed to remove duplicate idp entry {} in database", IdpPasswordModel.getIdpId());
          resp.setResponseObj(false);
          resp.setErrorMessage(response.getErrorMessage());
          resp.setErrorCode(response.getErrorCode());
          return resp;
        }
      }

      int rowsInserted =
          handle.insert(insertIdpPwdSQL, IdpPasswordModel.getIdpId(), IdpPasswordModel.getIdpUser(),
              IdpPasswordModel.getIdpPwd());
      if (rowsInserted != 1) {
        resp.setResponseObj(false);
        resp.setErrorCode(ErrorCode.DB_INTERNAL_ERROR);
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
        _log.warn(String.format(ErrorMessages.DB_INTERNAL_ERROR, IdpPasswordModel.getIdpId()));
      } else {
        _log.debug(String.format("Succesfully inserted idp %s in database", IdpPasswordModel.getIdpId()));
        resp.setResponseObj(true);
      }
    } catch (DBIException e) {
      _log.warn(String.format("An error encountered while inserting idp %s in database . Exception %s",
          IdpPasswordModel.getIdpId(), e.getMessage()));
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setResponseObj(false);
      resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
    }
    return resp;
  }

  /**
   * Update IdpPwd table row. All properties from the input object shall be updated.
   * 
   * @param IdpPasswordModel Object having updated vales for fields of idpPwd row
   * @return True/false
   */
  @Override
  public DbResponse<Boolean> updateIdpPassword(final IdpPasswordModel IdpPasswordModel) {

    Validator.validateNotNull(IdpPasswordModel);
    Validator.validateNotEmpty(IdpPasswordModel.getIdpUser(), ErrorMessages.IDP_USER_EMPTY);
    Validator.validateNotEmpty(IdpPasswordModel.getIdpPwd(), ErrorMessages.IDP_PASSWORD_EMPTY);
    Validator.validateNotEmpty(IdpPasswordModel.getIdpId(), ErrorMessages.IDP_ID_EMPTY);

    DbResponse<Boolean> resp = new DbResponse<Boolean>();
    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      int rowsUpdated =
          handle.update(updateIdpPwdSQL, IdpPasswordModel.getIdpUser(), IdpPasswordModel.getIdpPwd(),
              IdpPasswordModel.getIdpId());
      if (rowsUpdated != 1) {
        resp.setResponseObj(false);
        resp.setErrorCode(ErrorCode.DB_REQUEST_ERROR);
        resp.setErrorMessage(String.format(ErrorMessages.IDP_NOT_FOUND_IN_DATABASE, IdpPasswordModel.getIdpId()));
        _log.warn(String.format(ErrorMessages.IDP_NOT_FOUND_IN_DATABASE, IdpPasswordModel.getIdpId()));
      } else {
        _log.debug(String.format("Successfully updated idp %s in database", IdpPasswordModel.getIdpId()));
        resp.setResponseObj(true);
      }
    } catch (DBIException e) {
      _log.warn(String.format("An error encountered while updating idp %s in database. Exception %s",
          IdpPasswordModel.getIdpId(), e.getMessage()));
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      resp.setResponseObj(false);
      resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
    }
    return resp;
  }

  @Override
  public DbResponse<Boolean> removeIdpPassword(String idpId) {
    DbResponse<Boolean> resp = new DbResponse<Boolean>();

    Validator.validateNotEmpty(idpId, ErrorMessages.IDP_ID_EMPTY);

    try (Handle handle = MySQLDataSource.getDataSourceAsDbiHandler().open()) {
      int rowsDeleted = handle.update(deleteIdpPwdSQL, idpId);

      if (rowsDeleted != 1) {
        resp.setResponseObj(false);
        resp.setErrorCode(ErrorCode.DB_RECORD_NOT_FOUND);
        _log.warn("Deletion of idp {} failed as idp is not found in database", idpId);
        resp.setErrorMessage(String.format(ErrorMessages.IDP_NOT_FOUND_IN_DATABASE, idpId));
      } else {
        _log.debug(String.format("Successfully deleted idp %s", idpId));
        resp.setResponseObj(true);
      }
    } catch (DBIException e) {
      _log.warn(String.format("An error encountered while deleting idp %s in database. Exception: %s", idpId,
          e.getMessage()));
      resp.setResponseObj(false);
      resp.setErrorCode(MySQLExceptionMapper.fetchErrorStatus(e));
      if (resp.getErrorCode().equals(ErrorCode.DB_RECORD_CONSTRAINT_VIOLATION)) {
        resp.setErrorMessage(String.format(ErrorMessages.DB_RECORD_CONSTRAINT_VIOLATION, idpId));
      } else {
        resp.setErrorMessage(ErrorMessages.DB_INTERNAL_ERROR);
      }
    }
    return resp;
  }


  private final String insertIdpPwdSQL =
      "insert into IdpPwd (idp_id, idp_user, idp_pwd) values (:idpId, :idpUser, :idpPwrd)";
  private final String findPwdFromIdpSQL =
      "select idp_id as idpId, idp_user as idpUser, idp_pwd as idpPwd from IdpPwd where idp_id = :idpId";
  private final String updateIdpPwdSQL =
      "update IdpPwd set idp_user = :idpUser, idp_pwd = :idpPwd where idp_id = :idpId ";
  private final String deleteIdpPwdSQL = "delete from IdpPwd where idp_id = :idpId";
}
