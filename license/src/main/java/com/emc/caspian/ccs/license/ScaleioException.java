package com.emc.caspian.ccs.license;

public class ScaleioException extends Exception {

	public ScaleioException() {
		super();
	}

	public ScaleioException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ScaleioException(String message, Throwable cause) {
		super(message, cause);
	}

	public ScaleioException(String message) {
		super(message);
	}

	public ScaleioException(Throwable cause) {
		super(cause);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4429402731746225287L;

}
