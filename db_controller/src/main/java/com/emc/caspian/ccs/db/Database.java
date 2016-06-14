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

public class Database {
	private String database_name;

	private String user_name;

	private String password;

	/**
	 * @return the database_name
	 */
	public String getDatabase_name() {
		return database_name;
	}

	/**
	 * @param database_name
	 *            the database_name to set
	 */
	public void setDatabase_name(String database_name) {
		this.database_name = database_name;
	}

	/**
	 * @return the user_name
	 */
	public String getUser_name() {
		return user_name;
	}

	/**
	 * @param user_name
	 *            the user_name to set
	 */
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Database [database_name=" + database_name + ", user_name="
				+ user_name + ", password=" + password + "]";
	}
}
