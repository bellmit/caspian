/**
 *  Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.esrs.server.util;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kuppup
 *
 */
public class ESRSHmacUtil {

    private static final Logger _log = LoggerFactory.getLogger(ESRSHmacUtil.class);
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    public static String getESRSHmac(String method, String contentType, String path, String prodRegKey, String hmacDate) {

        StringBuilder sb = new StringBuilder(method);
        sb.append("\n\n");
        if (null != contentType){
            sb.append(contentType);
        }
        sb.append("\n");
        sb.append(hmacDate);
        sb.append("\n");
        sb.append(path);
        
        String signString = sb.toString();

        return calculateSha1Hmac(signString, prodRegKey);
    }

    /**
     * Computes RFC 2104-compliant HMAC signature.
     * @param data The data to be signed
     * @param key The signing key
     * @return The Base64-encoded RFC 2104-compliant HMAC signature.
     * @throws java.security.SignatureException when signature generation fails
     */
    public static String calculateSha1Hmac(String data, String key)
    {
        String result;
        try {
            _log.debug("Calculating Hmac from string :<"+data+">. Using Key:<"+key+">");
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

            // base64-encode the hmac
            result =  Base64.encodeBase64String(rawHmac);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException | UnsupportedEncodingException e) {
            _log.error("Error while trying to create HMAC for device",e);
            throw new RuntimeException(e);
        }
        return result;
    }
}
