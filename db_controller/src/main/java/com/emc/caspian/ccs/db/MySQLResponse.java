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

import org.json.simple.JSONArray;

public class MySQLResponse {

	private int returnCode;

	private String errorMessage;

	private JSONArray databases;

	/**
	 * @return the databases
	 */
	public JSONArray getDatabases() {
		return databases;
	}

	/**
	 * @param databases
	 *            the databases to set
	 */
	public void setDatabases(JSONArray databases) {
		this.databases = databases;
	}

	/**
	 * @return the returnCode
	 */
	public int getReturnCode() {
		return returnCode;
	}

	/**
	 * @param returnCode
	 *            the returnCode to set
	 */
	public void setReturnCode(int returnCode) {
		this.returnCode = returnCode;
	}

	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 *            the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "MySQLResponse [databases=" + databases + ", returnCode="
				+ returnCode + ", errorMessage=" + errorMessage + "]";
	}

}
