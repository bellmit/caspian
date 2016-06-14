package com.emc.caspain.ccs.keystone.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.keystone.middleware.exceptions.InternalException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.InvalidTokenException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.MiddlewareException;
import com.emc.caspian.ccs.keystone.model.Token;

public final class Middleware {

  private static final Logger _log = LoggerFactory.getLogger(Middleware.class);


  private TokenCache tokenCache;
  private KeystoneCertificateManager keystoneCertManager;
  private RevocationEventsCache revocationEventsCache;
  private KeystoneConfiguration keystoneConfiguration;
  private KeystoneClientUtil keystoneClientUtil;
  boolean bInitialized;
  
  public Middleware(String authUri, String user, String password) {
    keystoneConfiguration = new KeystoneConfiguration(authUri, user, password);
    keystoneClientUtil = new KeystoneClientUtil(keystoneConfiguration);
    keystoneCertManager = null;
    revocationEventsCache = null;
    tokenCache = null; 
    bInitialized = false;
  }

  public void start() {
    // TODO: Add guards to prevent multiple starts
    keystoneCertManager = new KeystoneCertificateManager(keystoneClientUtil);
    tokenCache = new TokenCache();
    revocationEventsCache = new RevocationEventsCache(keystoneClientUtil);
    bInitialized = true;

    _log.info("Keystone Middlerware Initialized");
  }

  public void stop() {
    // TODO: Add guards to prevent multiple stops
    bInitialized = false;

    if (revocationEventsCache != null) {
      revocationEventsCache.destroy();
    }

    if (keystoneCertManager != null) {
      keystoneCertManager.destroy();
    }

    if (tokenCache != null) {
      tokenCache.clear();
    }
  }

  public Token getToken(String authToken) throws MiddlewareException {

    if (bInitialized == false) {
      _log.error("getToken() called without starting middleware");
      throw new InternalException("Middleware has not been started");
    }

    KeystoneToken ksToken = TokenFactory.getKeystoneToken(this.keystoneClientUtil, this.keystoneCertManager, authToken);
    if (ksToken == null) {
      _log.warn("Invalid token string received");
      throw new InvalidTokenException("Invalid token string received");
    }

    Token tokenInfo = null;

    // First check the token cache
    tokenInfo = tokenCache.get(ksToken.getTokenCacheKey());
    if (tokenInfo != null) {

      _log.debug("Successfully retrieved token from cache");
      // Next check the revocation cache
      int seqNum = revocationEventsCache.isRevoked(tokenInfo);
      if (seqNum == -1) {
        _log.warn("The token received is revoked");
        throw new InvalidTokenException("token is revoked");
      } else {
        _log.debug("The token received is not revoked");

        return tokenInfo;
      }
    } else {
      _log.debug("Could not retrieve token from cache");
      tokenInfo = ksToken.validate();
      if (tokenInfo != null) {
        _log.debug("Successfully validated auth token");
        // Check for revocation
        tokenInfo.setSequenceNumber(0);
        int seqNum = revocationEventsCache.isRevoked(tokenInfo);
        if (seqNum == -1) {
          _log.warn("The token received is revoked");
          throw new InvalidTokenException("token is revoked");
        } else {
          _log.debug("The token received is not revoked");
          // Store the token in the cache
          tokenCache.store(ksToken.getTokenCacheKey(), tokenInfo);
          return tokenInfo;
        }
      } else {
        _log.warn("Failed to validate token");
        throw new InvalidTokenException("Invalid token");
      }
    }
  }


}
