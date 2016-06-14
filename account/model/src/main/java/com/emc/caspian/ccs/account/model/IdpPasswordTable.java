/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.model;

public interface IdpPasswordTable {

  public DbResponse<Boolean> addIdpPassword(IdpPasswordModel IdpPasswordModel);

  public DbResponse<IdpPasswordModel> getPassword(String idpId);

  public DbResponse<Boolean> updateIdpPassword(IdpPasswordModel IdpPasswordModel);

  public DbResponse<Boolean> removeIdpPassword(String idpId);

}
