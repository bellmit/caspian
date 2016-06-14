package com.emc.caspain.ccs.keystone.middleware;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TokenFactory {
  private static final Logger _log = LoggerFactory.getLogger(TokenFactory.class);

  private static final String PKI_ASN1_PREFIX = "MII";

  public static KeystoneToken getKeystoneToken(KeystoneClientUtil keystoneClientUtil, KeystoneCertificateManager keystoneCertManager, String strToken) {
    if (strToken.startsWith(PKI_ASN1_PREFIX)) {
      _log.debug("PKI token detected");
      return new PKIToken(keystoneClientUtil, keystoneCertManager, strToken);
    } else {
      _log.error("Non-PKI based token is not supported");
      return null;
    }
  }
}
