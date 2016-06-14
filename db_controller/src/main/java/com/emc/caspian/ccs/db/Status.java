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

public class Status {
	public static enum Code {
		OK(200), BAD_REQUEST(400), UNAUTHENTICATED(401), FORBIDDEN(403), NOT_FOUND(
				404), CONFLICT(409), PRECONDITION_FAILED(412), INTERNAL_SERVER_ERROR(
				500);

		private final int _value;

		private Code(final int value) {
			_value = value;
		}

		public int value() {
			return _value;
		}
	}

	public final static String INTERNAL_ERROR = "Internal server error";
	public final static String DB_EXISTS = "Database already exists";
	public final static String USER_EXISTS = "User already exists";
	public final static String DB_NOT_FOUND = "Database not found";
	public final static String WRONG_CREDENTIALS = "Incorrect user name or password";
	public final static String UNACCEPTABLE_CREDENTIALS = "Unacceptable user name or password";
	public final static String UNACCEPTABLE_DB_NAME = "Unacceptable database name";
	public final static String SQL_EXCEPTION = "SQLException:";
}