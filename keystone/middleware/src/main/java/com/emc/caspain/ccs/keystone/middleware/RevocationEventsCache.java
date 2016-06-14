package com.emc.caspain.ccs.keystone.middleware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.keystone.middleware.RevocationEvents.Event;
import com.emc.caspian.ccs.keystone.common.KeystoneDateTimeUtils;
import com.emc.caspian.ccs.keystone.model.Token;
import com.emc.caspian.ccs.keystone.model.Token.Role;

class RevocationEventsCache implements Runnable {
  private static final Logger _log = LoggerFactory.getLogger(RevocationEventsCache.class);

  private ArrayList<RevocationEvents> revocationCache;
  private String lastUpdateTime = null;
  private static final String THREAD_NAME = "revocation-events-cache";

  // TODO: allow method to override this via configuration
  private static final int FETCH_REVOCATION_INTERVAL = 10 * 1000;
  // This value represents the number of FETCH_REVOCATION_INTERVALs to wait for next initialization
  private static final int INITIALIZE_REVOCATION_INTERVAL = 360;

  private Thread revocationCacheUpdateThread = null;
  private KeystoneClientUtil keystoneClientUtil = null;

  public RevocationEventsCache(KeystoneClientUtil keystoneUtil) {
    this.keystoneClientUtil = keystoneUtil;
    revocationCache = new ArrayList<RevocationEvents>();
    _log.info("Initialized Revocation Events Cache");
    revocationCacheUpdateThread = new Thread(this, THREAD_NAME);
    revocationCacheUpdateThread.start();
  }


  public synchronized void destroy() {
    revocationCacheUpdateThread.interrupt();
    revocationCache.clear();
  }

  // Validate the token with single revocation event return true if revoked else false
  private boolean validateToken(Event eachEvent, Token token) {
    String domainId = token.getDomain() == null ? token.getUser().getDomain().getId() : token.getDomain().getId();
    if (eachEvent.getDomainId() != null && (token.getDomain() == null || !eachEvent.getDomainId().equals(domainId))) {
      return false;
    }

    if (eachEvent.getProjectId() != null
        && (token.getProject() == null || !eachEvent.getProjectId().equals(token.getProject().getId()))) {
      return false;
    }

    if (eachEvent.getUserId() != null && !eachEvent.getUserId().equals(token.getUser().getId())) {
      return false;
    }
    // if the role in the invalid token matches the revoked event role then proceed with further checks
    if (eachEvent.getRoleId() != null) {
      List<Role> roles = token.getRoles();
      int j;
      for (j = 0; j < roles.size(); j++) {
        if (eachEvent.getRoleId().equals(roles.get(j).getId())) {
          break;
        }
      }
      // if none of the roles in revoked event matches the token role Id then return the false ( means the token need
      // not be revoked )
      if (j == roles.size()) {
        _log.debug("No token role matched the event role");
        return false;
      }
    }

    // The first id in the token list is the auditId
    if (eachEvent.getAuditId() != null && !eachEvent.getAuditId().equals(token.getAuditIds().get(0))) {
      _log.debug("audit_id did not match");
      return false;
    }

    if (eachEvent.getAuditChainId() != null && !(token.getAuditIds().contains(eachEvent.getAuditChainId()))) {
      _log.debug("audit_chain_id did not match");
      return false;
    }

    /*
     * tokens issued just before the revocation event in the same second are being considered as not revoked because the
     * revocation events uses only seconds as the precision
     */
    if (eachEvent.getIssuedBefore() != null
        && (KeystoneDateTimeUtils.compareTime(token.getIssuedAt(), eachEvent.getIssuedBefore()) > 0)) {
      _log.debug("token issued after the event");
      _log.debug("token {}", token.getIssuedAt());
      _log.debug("event {}", eachEvent.getIssuedBefore());
      return false;
    }

    // TODO : Need to implement for "OS-TRUST" if required

    // TODO : Need to implement for "OS-OAUTH1" if required

    return true;


  }



  public int isRevoked(Token token) {
    int size;
    ArrayList<RevocationEvents> cacheCopy;
    synchronized (this) {
      size = revocationCache.size();
    }
    _log.debug("The revocationCache size {} ", size);
    // Check if the revocation list is empty
    if (size == 0) {
      _log.debug("The revocation events list is empty");
      return 0;
    } else {
      synchronized (this) {
        /*
         * Cloning the events list into separate object so that we can move the code to validate token against these
         * events out of synchronized block which is an expensive operation
         */
        cacheCopy = (ArrayList<RevocationEvents>) revocationCache.clone();
      }
      _log.debug("The token has to be validated against the latest revocation events");
      Iterator<RevocationEvents> listIt = cacheCopy.iterator();
      while (listIt.hasNext()) {
        Iterator<Event> eventIt = listIt.next().getEvents().iterator();
        while (eventIt.hasNext()) {
          Event eachEvent = eventIt.next();
          if (validateToken(eachEvent, token)) {
            return -1;
          }
        }
      }
      return cacheCopy.size();
    }
  }

  // Update the revocationCache with the recent events since last update
  private boolean updateRevocationCache() {
    RevocationEvents latestEvents = keystoneClientUtil.getRevocationEvents(lastUpdateTime);
    if (latestEvents != null) {
      lastUpdateTime = latestEvents.getDateTime();
      if (latestEvents.getEvents().isEmpty()) {
        _log.debug("No recent revocation events received");
        return true;
      }
      synchronized (this) {
        revocationCache.add(latestEvents);
      }
      _log.debug("Updated revocation events cache successfully with non empty list");
      return true;
    } else {
      _log.warn("Could not update revocation events cache");
      return false;
    }
  }

  // Intialize the revocation cache
  private boolean resetRevocationCache() {
    RevocationEvents totalEvents = keystoneClientUtil.getRevocationEvents(null);
    if (totalEvents != null) {
      lastUpdateTime = totalEvents.getDateTime();
      synchronized (this) {
        revocationCache.clear();
        if (totalEvents.getEvents().isEmpty()) {
          _log.debug("No revocation events received, initializing the cache to empty list");
          return true;
        }
        revocationCache.add(totalEvents);
      }
      _log.debug("Initialized revocation events cache successfully with non empty list");
      return true;
    } else {
      _log.warn("Could not initialize revocation events cache");
      return false;
    }
  }


  // The main thread loop
  @Override
  public void run() {
    int timer = 0;
    while (true) {
      if (timer <= 0) {
        boolean bInit = resetRevocationCache();
        if (bInit) {
          timer = INITIALIZE_REVOCATION_INTERVAL;
        }
      } else {
        boolean bRet = updateRevocationCache();
        if (bRet == true) {
          _log.debug("Successfully updated revocation events cache");
        } else {
          _log.warn("Could not update revocation events cache");
        }
      }
      try {
        Thread.sleep(FETCH_REVOCATION_INTERVAL);
      } catch (InterruptedException e) {
        _log.info("Received thread interrupt signal, stopping the thread");
        return;
      }
      timer--;
    }
  }
}
