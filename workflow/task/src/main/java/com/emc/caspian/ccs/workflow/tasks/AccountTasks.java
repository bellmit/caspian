package com.emc.caspian.ccs.workflow.tasks;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.account.model.DbResponse;
import com.emc.caspian.ccs.account.model.ErrorCode;
import com.emc.caspian.ccs.account.model.TableFactory;
import com.emc.caspian.ccs.account.model.mysql.MySQLProperties;
import com.emc.caspian.ccs.keystone.model.Domain;
import com.emc.caspian.ccs.workflow.Task;
import com.emc.caspian.ccs.workflow.TaskBase;
import com.emc.caspian.ccs.workflow.TaskException;
import com.emc.caspian.ccs.workflow.types.Type;

public class AccountTasks extends TaskBase {

  private static final Logger _log = LoggerFactory.getLogger(AccountTasks.class);

  /**
   * Task to delete an account as well as its domains
   * 
   * @param accountId
   * @param authToken
   * @param keystoneUri
   * @return boolean response indicating the status of account deletion
   */
  @Task(name = "deleteAccount", returnType = Type.Boolean, parametersTypes = {Type.String, Type.String, Type.String,
      Type.String, Type.String, Type.String, Type.String, Type.String})
  public boolean deleteAccount(String accountId, String keystoneUri, String dbUserName, String dbPassword,
      String dbHostName, String dbPort, String database, String listOfControllerHosts) {

    List<String> listOfControllers = Arrays.asList(listOfControllerHosts);

    // TODO Initialize only if not already initialized
    AccountTasksUtil.initialiseKeystoneClients(keystoneUri);
    _log.debug("Keystone initialization successful");
    MySQLProperties.initializeMySQLProperties(dbUserName, dbPassword, dbHostName, dbPort, database);
    
    DbResponse<Boolean> accountDeletedStatus = null;

    Domain domainToDelete = new Domain();
    domainToDelete.setId(accountId);
    domainToDelete.setEnabled(false);

    AccountTasksUtil.patchDomain(this.getToken(), accountId, domainToDelete);

    // After disabling of domains, notify the controllers of the delete operation
    // notification is made to controller
    AccountTasksUtil.notifyAccountDeletionAndGetTaskStatus(this.getToken(), accountId, listOfControllers);

    AccountTasksUtil.patchDomain(this.getToken(), accountId, domainToDelete);
    AccountTasksUtil.deleteDomain(this.getToken(), accountId);
   

    accountDeletedStatus = TableFactory.getAccountTable().removeAccount(accountId);

    // if multiple simultaneous requests for deletion of same account come to this step, one request will remove the
    // account but
    // for others there will be 404 not found, which is success for other threads
    if (!accountDeletedStatus.getResponseObj() && accountDeletedStatus.getErrorCode() != ErrorCode.DB_RECORD_NOT_FOUND) {
      _log.warn("Error encountered while deleting account {} from database, retrying", accountId);
      throw new TaskException("Deletion of account " + accountId + " failed", true);
    }
    return true;
  }

}
