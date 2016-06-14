package com.emc.caspian.ccs.keystone.asyncclient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
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

import com.emc.caspian.ccs.client.ASyncRestClient;
import com.emc.caspian.ccs.client.ClientResponseCallback;
import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.QueryParams;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.common.KeystoneDeserializationUtils;
import com.emc.caspian.ccs.keystone.common.PathConstants;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Token;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author bhandp2
 * */
public class KeystoneTokenClient {

  private static final Logger _log = LoggerFactory.getLogger(KeystoneTokenClient.class);

  private ASyncRestClient client;
  
  private static SignerInformationVerifier verifier = null;

  private static Object verifierMutex = new Object();

  public KeystoneTokenClient(ASyncRestClient client) {
    this.client = client;
  }

  public Future<ClientResponse<String>> getCertificate(ClientResponseCallback<String> callback) {
    String v3CertificatePath = PathConstants.KEY_AUTH_URL_V3 + PathConstants.CERTIFICATE;
    return this.client.get(String.class, v3CertificatePath, null, callback);
  }


  public Future<ClientResponse<Token>>  getToken(ClientResponseCallback<Token> callback, Authentication authentication,
      boolean nocatalog) {
    QueryParams queryParam = null;

    if (nocatalog) {
      queryParam = new QueryParams();
      queryParam.addQueryParam(Constants.NO_CATALOG, Constants.EMPTY);
    }
    _log.debug("creating token with nocatalog option {}", nocatalog);
    KeystoneTokenResponseCallback tokenResponseCallback = null;
    
	if (null != callback) {
			tokenResponseCallback = new KeystoneTokenResponseCallback(callback);
	}
		
    try {
    	
    	Future<ClientResponse<Token>> restClientFuture =  this.client.post(Token.class, PathConstants.KEY_AUTH_TOKEN_PATH_V3, null, authentication, tokenResponseCallback, queryParam);
    	CreateTokenFuture<Token> createTokenFuture = new CreateTokenFuture<Token>(restClientFuture);
    	return createTokenFuture;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return null;
    }

  }
  
  public Future<ClientResponse<Token>> validateToken(ClientResponseCallback<Token> callback, String authToken,
      String subjectToken, boolean nocatalog) {
    Map<String, String> requestHeader = new HashMap<String, String>();
    requestHeader.put(Constants.SUBJ_TOKEN_KEY, subjectToken);
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);

    QueryParams queryParam = null;

    if (nocatalog) {
      queryParam = new QueryParams();
      queryParam.addQueryParam(Constants.NO_CATALOG, Constants.EMPTY);

    }

    _log.debug("validationg token with nocatlog option {}", nocatalog);

    return this.client.get(Token.class, PathConstants.KEY_AUTH_TOKEN_PATH_V3, requestHeader, queryParam,
        callback);
  }

  public Future<ClientResponse<String>> checkToken(ClientResponseCallback<String> callback, String authToken,
      String subjectToken) {
    Map<String, String> requestHeader = new HashMap<String, String>();
    requestHeader.put(Constants.SUBJ_TOKEN_KEY, subjectToken);
    requestHeader.put(Constants.AUTH_TOKEN_KEY, authToken);

    _log.debug("checking token  {}", subjectToken);


    return this.client.head(String.class, PathConstants.KEY_AUTH_TOKEN_PATH_V3, requestHeader, callback);

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
      _log.debug("Initialized verifier");
      return true;
    } else {
      _log.warn("Received null for certificate");
      return false;
    }
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

  
  // Gets list of revoked tokens using V2 API
  public Future<ClientResponse<String>> getRevocationList(ClientResponseCallback<String> callback, String authenticationToken) {
    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);
    String v2TokenRevocationListPath = PathConstants.KEY_AUTH_URL_V2 + PathConstants.REVOCATION_LIST;

    return this.client.get(String.class, v2TokenRevocationListPath, requestHeader, callback);
  }
  
  public ClientResponse<String> verifyRevocationListResponse(ClientResponse<String> response){
    return KeystoneDeserializationUtils.verifyRevocationList(response, verifier);
  }
  // Gets list of revocation events using V3 API
  public Future<ClientResponse<String>> getRevocationEvents(ClientResponseCallback<String> callback,
      String authenticationToken, String sinceDate) {
    Map<String, String> requestHeader =
        KeystoneClientUtil.getAuthenticationHeader(authenticationToken);

    String v3TokenRevocationListPath =
        PathConstants.KEY_AUTH_URL_V3 + PathConstants.REVOCATION_EVENTS;
    QueryParams queryParam = null;
    if (StringUtils.isNotEmpty(sinceDate)) {
      queryParam = new QueryParams();
      queryParam.addQueryParam(Constants.SINCE, sinceDate);
      _log.debug("Using since query param with value {} in request", sinceDate);
    }

    return this.client.get(String.class, v3TokenRevocationListPath, requestHeader, queryParam, callback);

  }
}

class KeystoneTokenResponseCallback implements ClientResponseCallback{

	ClientResponseCallback<Token> tokenRespCallback;
	public KeystoneTokenResponseCallback(ClientResponseCallback<Token> parentCallback){
		tokenRespCallback = parentCallback;
	}
	
	@Override
	public void completed(ClientResponse clientResponse) {
		//once response received get the token from header and add it to the Token body
		
        String tokenString =
                KeystoneClientUtil.getStringValueFromResponseHeader(Constants.SUBJ_TOKEN_KEY, clientResponse
                    .getHttpResponse().getHeaders());
        
        Token token = (Token) clientResponse.getHttpResponse().getResponseBody();
        token.setTokenString(tokenString);
		tokenRespCallback.completed(clientResponse);
	}

	@Override
	public void failed(Exception exception) {
		// No operation
		tokenRespCallback.failed(exception);
	}

	@Override
	public void cancelled() {
		// No Operation
		tokenRespCallback.cancelled();
	}
	
}