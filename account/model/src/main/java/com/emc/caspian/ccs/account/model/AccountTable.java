/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.model;

import java.util.List;

import com.emc.caspian.ccs.account.model.DbResponse;


/**
 * Created by gulavb on 2/27/2015.
 */
public interface AccountTable {

  public DbResponse<AccountModel> getAccount(String id);

  public DbResponse<List<AccountModel>> getAccounts();

  default public DbResponse<Boolean> addAccount(AccountModel accountModel) {
    return null;
  }

  default public DbResponse<Boolean> removeAccount(String id) {
    return null;
  }

  default public DbResponse<AccountModel> changeAccountState(String id, String state) {
    return null;
  }

  default public DbResponse<JobModel> getTaskStatusForAccount(String taskId) {
    return null;
  }

  default public DbResponse<List<AccountModel>> getAccountDomainsWithEnhancedInfo() {
    return null;
  }

}
