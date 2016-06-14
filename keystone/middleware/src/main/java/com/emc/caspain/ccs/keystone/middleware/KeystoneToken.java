package com.emc.caspain.ccs.keystone.middleware;

import com.emc.caspain.ccs.keystone.middleware.exceptions.MiddlewareException;
import com.emc.caspian.ccs.keystone.model.Token;

abstract class KeystoneToken {

  protected String authToken;
  protected KeystoneClientUtil keystoneClientUtil;

  public KeystoneToken(KeystoneClientUtil keystoneUtil, String strToken) {
    this.keystoneClientUtil = keystoneUtil;
    this.authToken = strToken;
  }

  protected Token validateTokenWithServer() throws MiddlewareException {
    // Validate token using the keystone server
    return keystoneClientUtil.validateAndGetDetailsFromServer(authToken);
  }

  // validate the auth token and return detailed Token object containing scope and roles information
  public abstract Token validate() throws MiddlewareException;

  // Retrieve the key derived from the authtoken which can be used as the unique caching key
  public abstract String getTokenCacheKey();

}
