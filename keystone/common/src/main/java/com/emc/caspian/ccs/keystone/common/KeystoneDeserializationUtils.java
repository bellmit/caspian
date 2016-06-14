package com.emc.caspian.ccs.keystone.common;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.Constants;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.keystone.model.Token;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * The Class KeystoneDeserializationUtils for deserializing the ClientResponse received.
 * 
 * @author bhandp2
 * 
 */
public class KeystoneDeserializationUtils {
  
  static ObjectMapper MAPPER_DEFAULT;
  static ObjectMapper MAPPER_WRAP;
  
  private static final Logger _log = LoggerFactory.getLogger(KeystoneDeserializationUtils.class);

  
  static {
    MAPPER_DEFAULT = new ObjectMapper();
    MAPPER_DEFAULT.setSerializationInclusion(Include.NON_NULL);
    MAPPER_DEFAULT.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    MAPPER_DEFAULT.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    MAPPER_DEFAULT.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    MAPPER_DEFAULT.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
    
    
    MAPPER_WRAP = new ObjectMapper();
    MAPPER_WRAP.setSerializationInclusion(Include.NON_NULL);
    MAPPER_WRAP.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    MAPPER_WRAP.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    MAPPER_WRAP.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
    MAPPER_WRAP.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
    MAPPER_WRAP.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
    
  }

  public static Token getToken(ClientResponse<String> response) {
    
    String responseBody = response.getHttpResponse().getResponseBody();
    Token token = null;
    if (null != responseBody) {
        token = getResponseObject(responseBody, Token.class);
        String tokenString =
            KeystoneClientUtil.getStringValueFromResponseHeader(Constants.SUBJ_TOKEN_KEY, response
                .getHttpResponse().getHeaders());
        token.setTokenString(tokenString);

    }
    return token;
  }
  
  public static <T> T getResponseObject(String response, Class<T> responseType){
    T responseObject = null;
    
    ObjectMapper mapper = responseType.getAnnotation(JsonRootName.class) == null ? MAPPER_DEFAULT : MAPPER_WRAP;
    if (null != response) {
      try {
        
        responseObject = mapper.readValue(response, responseType);

      } catch (JsonParseException e) {
        e.printStackTrace();
        _log.warn("JSON Parse exception occured during token deserialization {}", e);
      } catch (JsonMappingException e) {
        e.printStackTrace();
        _log.warn("JSON Mapping exception occured during token deserialization {}", e);
      } catch (IOException e) {
        e.printStackTrace();
        _log.warn("IO exception occured during token deserialization {}", e);
      }
    }
    return responseObject;
  }
  
  public static ClientResponse<String> verifyRevocationList(ClientResponse<String> response,
      SignerInformationVerifier verifier) {
    
    String responseBody = response.getHttpResponse().getResponseBody();
    if (null != responseBody) {
      String revokedCms = responseBody.substring(33, (responseBody.length() - 23));
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
        if (verifySignature(signer,verifier)) {
          response.getHttpResponse().setResponseBody(jsonString);
        } else {
          _log.warn("Signer verification failed");
          response.getHttpResponse().setResponseBody(null);

        }
      } else {
        _log.warn("No signers could be retrieved from cms signedData");
        response.getHttpResponse().setResponseBody(null);

      }
    }
    return response;
  }
  
  /**
   * Verifies the signature of the given signer using the verifier.
   */
  private static boolean verifySignature(SignerInformation signer, SignerInformationVerifier verifier) {

    if (verifier == null) {
      _log.warn("verifier not initialized at the client");
      return false;
    } else {
      try {
          return signer.verify(verifier);
      } catch (CMSException e) {
        _log.warn("Could not verify the signer from cms message. Error: " + e.getMessage());
        return false;
      }
    }
  }

}