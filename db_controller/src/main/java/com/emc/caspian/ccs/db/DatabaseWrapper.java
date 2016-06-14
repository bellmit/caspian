/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.db;

import java.util.List;

public class DatabaseWrapper {
	private List<Database> databases;

	/**
	 * @return the databases
	 */
	public List<Database> getDatabase() {
		return databases;
	}

	/**
	 * @param databases
	 *            the databases to set
	 */
	public void setDatabases(List<Database> databases) {
		this.databases = databases;
	}
}
