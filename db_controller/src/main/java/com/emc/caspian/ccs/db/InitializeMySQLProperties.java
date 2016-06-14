/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.db;

import com.emc.caspian.fabric.config.Configuration;
import com.emc.caspian.encryption.AESUtil;
import com.emc.caspian.encryption.AESUtilException;
import com.emc.caspian.ccs.db.util.AppLogger;

public class InitializeMySQLProperties {
	private static final String SECTION = "database";
	private static String databaseHostName = null;
	private static String databasePort = null;
	private static String databaseUser = null;
	private static char[] databasePwd = null;

	public static void initializeMySQLPropertiesFromConfig() {
	
		databaseHostName = Configuration.make(String.class,
				SECTION + ".dbhostname").value();
		databasePort = Configuration.make(String.class, SECTION + ".dbport")
				.value();
		databaseUser = Configuration
				.make(String.class, SECTION + ".dbusername").value();
				
		setAccountsPassword();
	}

	public static String getHostname() {
		return databaseHostName;
	}

	public static String getPort() {
		return databasePort;
	}

	public static String getAccountsUser() {
		return databaseUser;
	}

	public static char[] getAccountsPassword() {
		return databasePwd;
	}
	
	private static void setAccountsPassword() {
        final String encryptedDbPwd;
		
        if (System.getenv().containsKey("ENCRYPTED_ROOT_PASS")){
                encryptedDbPwd = System.getenv("ENCRYPTED_ROOT_PASS");
        }else {
            AppLogger.error("DB password is missing from environment variables");
            throw new RuntimeException("DB password is missing from environment variables");
        }
        try(AESUtil au = AESUtil.getInstance()) {
            databasePwd = au.decrypt(encryptedDbPwd).toCharArray();
	    if(databasePwd == null){
		AppLogger.error("Critical Error. Could not set db password. ");
		throw new RuntimeException("Critical Error. Could not set db password. ");
            }
            return;
	} catch (AESUtilException e){
		AppLogger.error("AES Util failed to decrypt password", e);
		throw new RuntimeException("AES Util failed to decrypt password.");
	} catch (NullPointerException e){
		AppLogger.error("Critical Error. Could not set db password. ",e);
		throw new RuntimeException("Critical Error. Could not set db password.");
	} catch(Exception e){
            AppLogger.error("Error decrypting DB password", e);
            throw new RuntimeException(e);
        }
    }

}

