package com.emc.caspain.ccs.keystone.middleware;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Cache;
import com.emc.caspain.ccs.keystone.middleware.exceptions.InvalidTokenException;
import com.emc.caspian.ccs.keystone.common.KeystoneDateTimeUtils;
import com.emc.caspian.ccs.keystone.model.Token;

class TokenCache {
  private static final Logger _log = LoggerFactory.getLogger(TokenCache.class);

  private Cache<String, Token> cache;

  public TokenCache() {
    cache =
        CacheBuilder.newBuilder().maximumSize(CACHE_MAX_ELEMENTS)
            .expireAfterWrite(CACHE_EXPIRY_TIMEOUT, TimeUnit.SECONDS).build();
    _log.info("Initialized Keystone token cache");
  }

  public Token get(String key) throws InvalidTokenException {
    Token token = cache.getIfPresent(key);
    if (token != null) {
      // Check for expiry since the cache implementation doesn't support per object TTL
      if (KeystoneDateTimeUtils.getTimeInMillis(token.getExpiresAt()) < System.currentTimeMillis()) {
        _log.debug("Found an expired token in cache, invalidating");
        cache.invalidate(key);
        throw new InvalidTokenException("Auth token has expired");
      }
    }

    return token;
  }

  public void store(String key, Token token) {
    cache.put(key, token);
  }



  // This method is not being used now
  public synchronized void resetAllTokens() {
    Map<String, Token> tokenMap = cache.asMap();
    Collection<Token> allTokens = tokenMap.values();
    Iterator<Token> it = allTokens.iterator();
    while (it.hasNext()) {
      it.next().setSequenceNumber(0);
    }
  }

  // clear all cached entries
  public void clear() {
    cache.invalidateAll();

  }

  // TODO: allow method to override this via configuration
  private static final long CACHE_MAX_ELEMENTS = 1000;
  private static final long CACHE_EXPIRY_TIMEOUT = 3600;
}
