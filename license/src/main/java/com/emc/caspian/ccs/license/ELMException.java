package com.emc.caspian.ccs.license;

public class ELMException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3156039640714443450L;

	public ELMException() {
	}

	public ELMException(String message) {
		super(message);
	}

	public ELMException(Throwable cause) {
		super(cause);
	}

	public ELMException(String message, Throwable cause) {
		super(message, cause);
	}

	public ELMException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
