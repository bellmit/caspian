/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.model;

import java.util.List;

public interface PrimaryRoleAssignmentTable {
  
  public DbResponse<Boolean> upsertRoleAssignment(PrimaryRoleAssignmentModel primaryRoleAssignmentModel);
  
  public DbResponse<PrimaryRoleAssignmentModel> getRoleAssignment(String accountId, String roleId);
  
  public DbResponse<Boolean> removeRoleAssignment(String accountId, String roleId);
  
  public DbResponse<Boolean> removeRoleAssignmentsForAccount(String accountId);
  
  public DbResponse<List<PrimaryRoleAssignmentModel>> listRoleAssignmentsForAccount(String accountId);
  
}
