package com.emc.caspian.ccs.license;


public class DateParseException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6746011774416737025L;
	
	public DateParseException(){

	}
	public DateParseException(String message){
		super(message);
	}
	public DateParseException(Throwable cause){
		super(cause);
	}
	public DateParseException(String message, Throwable cause){
		super(message, cause);
	}
	public DateParseException(String message, Throwable cause,

			boolean enableSuppression, boolean writableStackTrace)

	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
