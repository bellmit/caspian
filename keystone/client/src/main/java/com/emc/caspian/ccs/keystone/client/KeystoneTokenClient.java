package com.emc.caspian.ccs.keystone.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.RestClient;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Token;

public class KeystoneTokenClient {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneTokenClient.class);

  private RestClient client;

  private static SignerInformationVerifier verifier = null;

  private static Object verifierMutex = new Object();

  public KeystoneTokenClient(RestClient client) {
    this.client = client;
  }

  /**
   * Create Authentication Token
   * <p>
   * API Call: <tt>POST https://keystone:6100/v3/auth/tokens</tt>.
   * 
   * @param authentication the authentication
   * @param nocatalog the nocatalog
   * @return the token
   */
  public ClientResponse<Token> getToken(Authentication authentication, boolean nocatalog) {
    QueryParams queryParam = null;

    if (nocatalog) {
      queryParam = new QueryParams();
      queryParam.addQueryParam(Constants.NO_CATALOG, Constants.EMPTY);

    }
    _log.debug("creating token with nocatalog option {}", nocatalog);

    ClientResponse<Token> tokenResp =
        this.client.post(Token.class, queryParam, authentication, PathConstants.KEY_AUTH_TOKEN_PATH_V3);

    if (tokenResp.getHttpResponse().getResponseBody() != null) {
      Token token = tokenResp.getHttpResponse().getResponseBody();
      _log.debug("Received response : {} for create token", tokenResp.getStatus());

      String tokenString =
          KeystoneClientUtil.getStringValueFromResponseHeader(Constants.SUBJ_TOKEN_KEY, tokenResp.getHttpResponse()
              .getHeaders());
      token.setTokenString(tokenString);
    }

    return tokenResp;

  }

  public ClientResponse<Token> validateToken(String authToken, String subjectToken, boolean nocatalog) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.SUBJ_TOKEN_KEY, subjectToken);
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);

    QueryParams queryParam = null;

    if (nocatalog) {
      queryParam = new QueryParams();
      queryParam.addQueryParam(Constants.NO_CATALOG, Constants.EMPTY);

    }
    ClientResponse<Token> tokenResp = null;

    tokenResp = this.client.get(Token.class, queryParam, PathConstants.KEY_AUTH_TOKEN_PATH_V3, requestHeader);
    if (tokenResp != null) {
      _log.debug("validated token with nocatlog option {}", nocatalog + " get response " + tokenResp.getStatus());
    } else {
      _log.warn("Got null response while validating token");
    }
    return tokenResp;
  }

  public ClientResponse<String> checkToken(String authToken, String subjectToken) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.SUBJ_TOKEN_KEY, subjectToken);
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);

    _log.debug("checking token  {}", subjectToken);

    ClientResponse<String> clientResponse =
        this.client.head(String.class, PathConstants.KEY_AUTH_TOKEN_PATH_V3, requestHeader);

    return clientResponse;
  }

  public ClientResponse<String> getCertificate() {
    String v3CertificatePath = PathConstants.KEY_AUTH_URL_V3 + PathConstants.CERTIFICATE;
    ClientResponse<String> clientResponse = this.client.get(String.class, v3CertificatePath);
    return clientResponse;
  }

  /**
   * Creates the SignerInformationVerifier from the String certificate. This is used to validate the cms messages.
   */
  private void setVerifier(String certificate) throws IOException, OperatorCreationException {
    // The cert is PEM encoded - need to translate those bytes into a PEM object
    Reader lCertBufferedReader =
        new BufferedReader(new InputStreamReader(new ByteArrayInputStream(certificate.getBytes())));

    @SuppressWarnings("resource")
    PemObject lPemObj = new PemReader(lCertBufferedReader).readPemObject();

    // Create our verify builder - basically we need to make an object that will verify the cert
    BcRSASignerInfoVerifierBuilder signerInfoBuilder =
        new BcRSASignerInfoVerifierBuilder(new DefaultCMSSignatureAlgorithmNameGenerator(),
            new DefaultSignatureAlgorithmIdentifierFinder(), new DefaultDigestAlgorithmIdentifierFinder(),
            new BcDigestCalculatorProvider());

    // Using the PEM object, create a cert holder and a verifier for the cert
    synchronized (verifierMutex) {
      verifier = signerInfoBuilder.build(new X509CertificateHolder(lPemObj.getContent()));
    }
  }

  /**
   * Initializes the keystone public certificate and the verifier.
   */
  public boolean setCertificate(String certificate) {
    if (certificate != null) {
      try {
        setVerifier(certificate);
      } catch (OperatorCreationException | IOException e) {
        _log.warn("Could not initialize verifier. Error: " + e.getMessage());
        return false;
      }
      _log.info("Initialized verifier");
      return true;
    } else {
      _log.warn("Received null for certificate");
      return false;
    }
  }


  /**
   * Verifies the signature of the given signer using the verifier.
   */
  private boolean verifySignature(SignerInformation signer) {

    if (verifier == null) {
      _log.warn("verifier not initialized at the client");
      return false;
    } else {
      try {
        synchronized (verifierMutex) {
          return signer.verify(verifier);
        }
      } catch (CMSException e) {
        _log.warn("Could not verify the signer from cms message. Error: " + e.getMessage());
        return false;
      }
    }
  }

  // Gets list of revoked tokens using V2 API
  public ClientResponse<String> getRevocationList(String authenticationToken) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);
    String v2TokenRevocationListPath = PathConstants.KEY_AUTH_URL_V2 + PathConstants.REVOCATION_LIST;

    ClientResponse<String> response = this.client.get(String.class, v2TokenRevocationListPath, requestHeader);

    if (response.getStatus() == ClientStatus.SUCCESS) {
      _log.debug("Received response : {} for get revocation list", response.getHttpResponse().getStatusCode());
      String revokedCms = response.getHttpResponse().getResponseBody();
      revokedCms = revokedCms.substring(33, (revokedCms.length() - 23));
      CMSSignedData signedData;
      // Initialize signed data object from cms message
      try {
        signedData = new CMSSignedData(Base64.decode(revokedCms.replaceAll("\\\\n", "")));
        _log.debug("Initialized signedData from revocation cms message");
      } catch (CMSException e) {
        _log.warn("Could not initialize signedData from revocation cms message. Error: " + e.getMessage());
        return null;
      }
      // Obtain actual message from signed data
      Object lObj = signedData.getSignedContent().getContent();
      String jsonString = null;
      if (lObj instanceof byte[]) {
        jsonString = new String((byte[]) lObj);
      }
      // Get signer information from signed data
      SignerInformationStore signerStore = signedData.getSignerInfos();
      Collection<SignerInformation> signers = signerStore.getSigners();
      Iterator<SignerInformation> it = signers.iterator();

      if (it.hasNext()) {
        SignerInformation signer = (SignerInformation) it.next();
        // Verify the signature of the signer
        if (verifySignature(signer)) {
          response.getHttpResponse().setResponseBody(jsonString);
        } else {
          _log.warn("Signer verification failed");
          response.getHttpResponse().setResponseBody(null);

        }
      } else {
        _log.warn("No signers could be retrieved from cms signedData");
        response.getHttpResponse().setResponseBody(null);

      }
    } else {
      _log.warn("Failed to get revocation list from keystone server: "
          + ((response != null) ? response.getErrorMessage() : ""));
    }


    return response;
  }


  // Gets list of revocation events using V3 API
  public ClientResponse<String> getRevocationEvents(String authenticationToken, String sinceDate) {
    Map<String, Object> requestHeader = new HashMap<String, Object>();
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authenticationToken);
    String v3TokenRevocationListPath = PathConstants.KEY_AUTH_URL_V3 + PathConstants.REVOCATION_EVENTS;
    QueryParams queryParam = null;
    if (StringUtils.isNotEmpty(sinceDate)) {
      queryParam = new QueryParams();
      queryParam.addQueryParam(Constants.SINCE, sinceDate);
      _log.debug("Using since query param with value {} in request", sinceDate);
    }
    ClientResponse<String> response =
        this.client.get(String.class, queryParam, v3TokenRevocationListPath, requestHeader);
    return response;
  }
}
