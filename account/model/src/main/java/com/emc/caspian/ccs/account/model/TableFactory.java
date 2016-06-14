/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.model;

import com.emc.caspian.ccs.account.model.mysql.MySQLAccountTable;
import com.emc.caspian.ccs.account.model.mysql.MySQLAccountTableV1;
import com.emc.caspian.ccs.account.model.mysql.MySQLIdpPasswordTable;

/**
 * Class wrapping factory methods for various tables Created by gulavb on 2/28/2015.
 */
public class TableFactory {

  /**
   * Factory Method to get AccountTable object. Currently only mysql bridge is available. In future this method will be
   * made configurable to return one of many DB bridges.
   * 
   * @return AccountTable object
   */
  public static AccountTable getAccountTable() {
    return new MySQLAccountTable();
  }
  
  /**
   * Factory Method to get AccountTable object. Currently only mysql bridge is available. In future this method will be
   * made configurable to return one of many DB bridges.
   * 
   * @return AccountTable object
   */
  public static AccountTable getAccountTableForV1() {
    return new MySQLAccountTableV1();
  }

  /**
   * Factory Method to get IdpPasswordTable object. Currently only mysql bridge is available. In future this method will
   * be made configurable to return one of many DB bridges.
   * 
   * @return IdpPasswordTable object
   */

  public static IdpPasswordTable getIdpPasswordTable() {
    return new MySQLIdpPasswordTable();
  }

}
