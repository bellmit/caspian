package com.emc.caspain.ccs.keystone.middleware;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.keystone.middleware.exceptions.InvalidTokenException;
import com.emc.caspain.ccs.keystone.middleware.exceptions.MiddlewareException;
import com.emc.caspian.ccs.keystone.common.KeystoneDateTimeUtils;
import com.emc.caspian.ccs.keystone.model.Token;

final class PKIToken extends KeystoneToken {

  private static final Logger _log = LoggerFactory.getLogger(PKIToken.class);

  private KeystoneCertificateManager keystoneCertManager;
  private String tokenHash;
  private Token token;
  private static long time = 0;
  private long currentTime;
  private long difference;
  private long SecToMinConverter = 60000;
 
  // Needed to parse the token JSON string
  private static class TokenInfo {

    private Token token;

    public Token getToken() {
      return token;
    }

    public void setToken(Token token) {
      this.token = token;
    }
  }

  public PKIToken(KeystoneClientUtil keystoneClientUtil, KeystoneCertificateManager keystoneCertManager, String strToken) {
    super(keystoneClientUtil, strToken);
    this.keystoneCertManager = keystoneCertManager;
    // initialize the hash, useful for cache look up while validating the token
    tokenHash = generateTokenHash();
  }

  private CMSSignedData getSignedDataFromRawToken(String lRawKeystoneToken) throws CMSException {
    // Keystone takes all '/' characters and replaced them by '-' in order to
    // encode the token into base 64. Let's reverse that..
    String lRealTokenData = lRawKeystoneToken.replace("-", "/");
    byte[] lData = Base64.decode(lRealTokenData.getBytes());

    // Now that we have the raw encoded token we can make a CMSSigned data out of it
    return new CMSSignedData(lData);
  }

  private String getTokenContentAsString(CMSSignedData aInSignedData) {
    Object lObj = aInSignedData.getSignedContent().getContent();
    if (lObj instanceof byte[]) {
      String lObjString = new String((byte[]) lObj);
      return lObjString;
    }
    _log.warn("Failed to get signed content from token");
    return null;
  }

  @Override
  public Token validate() throws MiddlewareException {


    CMSSignedData signedData;
    try {
      signedData = getSignedDataFromRawToken(authToken);
      String jsonToken = getTokenContentAsString(signedData);
      token = JsonHelper.deserializeFromJson(jsonToken, TokenInfo.class).getToken();
    } catch (Throwable e) {
      _log.warn("Couldn't extract token info from encoded PKI token string: " + e.getMessage());
      throw new InvalidTokenException("Couldn't extract token info from encoded PKI token string");
    }

    if (KeystoneDateTimeUtils.getTimeInMillis(token.getExpiresAt()) < System.currentTimeMillis()) {

      final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
      _log.warn("Auth token has expired at: " + token.getExpiresAt() + ". Current time is: "
          + dateFormat.format(Calendar.getInstance().getTime()));
      throw new InvalidTokenException("Auth token has expired");      
    }

    try {
      if (keystoneCertManager.isValidTokenSignature(signedData)) {
        _log.debug("Successfully verified token signature");
      } else {
    	currentTime = System.currentTimeMillis() / SecToMinConverter;
      	difference = currentTime - time;
      	if(time == 0 || difference > 5){
          if(keystoneCertManager.updateKeystoneCertificate() && keystoneCertManager.isValidTokenSignature(signedData)){
            time = System.currentTimeMillis() / SecToMinConverter;
            _log.debug("Successfully verified token signature");
            return token;
          }
        }
        _log.warn("Token signature validation failed");
        throw new InvalidTokenException("Token signature validation failed");
      }
    } catch (CMSException e) {
      _log.warn("Exception while validating token: " + e.getMessage());
      throw new InvalidTokenException("Token signature validation failed");
    } 
    return token;
  }

// Generate md5 hash of the PKI token string, used as a key in the token cache
  private String generateTokenHash() {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      _log.warn("Message digest cannot be initialized. Error: " + e.getMessage());
      return null;
    }
    byte[] byteData = md.digest(authToken.getBytes());
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < byteData.length; i++) {
      sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }


  @Override
  public String getTokenCacheKey() {
    return tokenHash;
  }
}
