package com.emc.caspain.ccs.keystone.middleware;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.bc.BcRSASignerInfoVerifierBuilder;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.keystone.middleware.exceptions.InternalException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.MiddlewareException;

class KeystoneCertificateManager implements Runnable {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneCertificateManager.class);

  private static final String THREAD_NAME = "fetch-keystone-certificate";

  private static final int SERVER_RETRY_INTERVAL = 3600 * 1000;
  private static final int SERVER_FAILURE_RETRY_INTERVAL = 60 * 1000;

  private SignerInformationVerifier signerInfo = null;
  private String localCachedCertificate = null;

  private Thread certificatePullThread = null;
  private KeystoneClientUtil keystoneClientUtil;

  public KeystoneCertificateManager(KeystoneClientUtil keystoneUtil) {
    this.keystoneClientUtil = keystoneUtil;
    final String keystoneCertificate = keystoneClientUtil.getKeystonePublicKey();
    if (StringUtils.isNotEmpty(keystoneCertificate)) {
      try {
        signerInfo = createTokenVerifier(keystoneCertificate);
        localCachedCertificate = keystoneCertificate;
      } catch (OperatorCreationException | IOException e) {
        _log.warn("Received invalid certificate from keystone server, continuing without certificate");
      }
    } else {
      _log.warn("Failed to fetch certificate from keystone, continuing without certificate");
    }
    certificatePullThread = new Thread(this, THREAD_NAME);
    certificatePullThread.start();
  }

  public void destroy() {
    certificatePullThread.interrupt();
    signerInfo = null;
    localCachedCertificate = null;
  }

  public boolean updateKeystoneCertificate() {

    final String keystoneCertificate = keystoneClientUtil.getKeystonePublicKey();

    // If we don't receive any certificate, return failure
    if (StringUtils.isEmpty(keystoneCertificate)) {
      _log.warn("No certificate fetched from server");
      return false;
    }

    // Check if we received the certificate for the first time, or if the certificate is different from the one which we
    // have stored locally
    if (signerInfo == null || !keystoneCertificate.equals(localCachedCertificate)) {
      synchronized (this) {
        try {
          signerInfo = createTokenVerifier(keystoneCertificate);
        } catch (OperatorCreationException | IOException e) {
          _log.warn("Could not create singerInfo from certificate. Error: " + e.getMessage());
          return false;
        }
      }
      localCachedCertificate = keystoneCertificate;
      keystoneClientUtil.updateClientWithCertificate(localCachedCertificate);
      _log.info("Updated keystone certificate from server");
    } else {
      _log.debug("No changes to keystone certificate detected");
    }

    return true;
  }

  private SignerInformationVerifier createTokenVerifier(String aInSigningCertificate) throws IOException,
      OperatorCreationException {
    // The cert is PEM encoded - need to translate those bytes into a PEM object
    Reader lCertBufferedReader =
        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(aInSigningCertificate.getBytes())));

    @SuppressWarnings("resource")
    PemObject lPemObj = new PemReader(lCertBufferedReader).readPemObject();

    // Create our verify builder - basically we need to make an object that will verify the cert
    BcRSASignerInfoVerifierBuilder signerInfoBuilder =
        new BcRSASignerInfoVerifierBuilder(new DefaultCMSSignatureAlgorithmNameGenerator(),
            new DefaultSignatureAlgorithmIdentifierFinder(), new DefaultDigestAlgorithmIdentifierFinder(),
            new BcDigestCalculatorProvider());

    // Using the PEM object, create a cert holder and a verifier for the cert
    SignerInformationVerifier lVerifier = signerInfoBuilder.build(new X509CertificateHolder(lPemObj.getContent()));

    return lVerifier;
  }

  // The main thread loop to fetch keystone certificate
  @Override
  public void run() {
    boolean bStatus = false;
    // We have already attempted to fetch certificate once before start of this thread, update its status
    if (localCachedCertificate != null) {
      bStatus = true;
    }
    while (true) {
      try {

        if (bStatus == true) {
          Thread.sleep(SERVER_RETRY_INTERVAL);
        } else {
          // We will be more aggressive on failures
          Thread.sleep(SERVER_FAILURE_RETRY_INTERVAL);
        }
      } catch (InterruptedException e) {
        // we can use interrupts to either quit or refresh immediately,
        // for now we will assume the former
        _log.error("Received thread interrupt signal, stop thread");
        return;
      }
      bStatus = updateKeystoneCertificate();
    }
  }

  public boolean isValidTokenSignature(CMSSignedData aInSignedData) throws MiddlewareException, CMSException {
    // The token contained the signer Info and has been parsed out
    // For each signer on the token, attempt to verify against the certificate

    if (signerInfo == null) {
      _log.warn("signerInfo not yet initialized");
      throw new InternalException("signerInfo not yet initialized");
    }

    SignerInformationStore lSignerInfo = aInSignedData.getSignerInfos();
    Collection<?> lSigners = lSignerInfo.getSigners();
    for (Object lObj : lSigners) {
      if (lObj instanceof SignerInformation) {
        _log.debug("Found signer info for token verification");
        SignerInformation lSigner = (SignerInformation) lObj;

        synchronized (this) {
          boolean lIsValid = lSigner.verify(signerInfo);
          if (lIsValid) {
            return true;
          }
        }
        _log.warn("Failed to verify signed content using signer");
      }
    }
    _log.warn("Failed to verify signed content");
    return false;
  }

}
