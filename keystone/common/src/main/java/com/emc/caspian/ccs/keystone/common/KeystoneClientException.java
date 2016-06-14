package com.emc.caspian.ccs.keystone.common;

public class KeystoneClientException extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 7578620451621434654L;
  
  public KeystoneClientException() {
    super();
  }

  public KeystoneClientException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public KeystoneClientException(String message, Throwable cause) {
    super(message, cause);

  }

  public KeystoneClientException(String message) {
    super(message);
  }

  public KeystoneClientException(Throwable cause) {
    super(cause);
  }


}
