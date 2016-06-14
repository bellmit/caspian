package com.emc.caspian.ccs.license.util;

import java.security.MessageDigest;

/**
 * This class is used to generate unique hash values for each of the licenses.
 */

public class HashGenerator {
	private static String md5Hash;

	public static String gen(String toHash) throws Exception {

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(toHash.getBytes());
			byte[] digest = md.digest();
			StringBuffer sb = new StringBuffer();
			for (byte b : digest)
				sb.append(String.format("%02x", b & 0xff));

			md5Hash = sb.toString();
		} catch (Exception e) {
			AppLogger.error("Error generating hash.", e);
			throw e;
		}
		return md5Hash;
	}

}
