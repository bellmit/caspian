package com.emc.caspain.ccs.keystone.middleware;

import com.emc.caspain.ccs.keystone.middleware.exceptions.MiddlewareException;
import com.emc.caspian.ccs.keystone.model.Token;

final class UUIDToken extends KeystoneToken {

  public UUIDToken(KeystoneClientUtil keystoneClientUtil, String strToken) {
    super(keystoneClientUtil, strToken);
  }

  @Override
  public String getTokenCacheKey() {
    return authToken;
  }

  @Override
  public Token validate() throws MiddlewareException {
    return super.validateTokenWithServer();
  }
}
