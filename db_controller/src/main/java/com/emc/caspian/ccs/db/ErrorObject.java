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

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorObject {

	private ErrorMessage error;

	public ErrorObject() {
		this.error = null;
	}

	public ErrorObject(ErrorMessage error) {
		this.error = error;
	}

	@JsonProperty
	public ErrorMessage getError() {
		return error;
	}

	@JsonProperty
	public void setError(ErrorMessage error) {
		this.error = error;
	}

	public class ErrorMessage {

		private String message;
		private int code;
		private String timestamp;

		@JsonProperty
		public String getMessage() {
			return message;
		}

		@JsonProperty
		public void setMessage(String message) {
			this.message = message;
		}

		@JsonProperty
		public int getCode() {
			return code;
		}

		@JsonProperty
		public void setCode(int errorCode) {
			this.code = errorCode;
		}

		@JsonProperty
		public String getTimestamp() {
			return timestamp;
		}

		@JsonProperty
		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}
	}
}
