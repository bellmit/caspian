package com.emc.caspian.ccs.license.util;	

import java.io.IOException;
import java.util.List;

import com.emc.caspian.crs.CRSClientBuilder;
import com.emc.caspian.crs.ServiceLocationClient;
import com.emc.caspian.crs.model.ComponentEndpoint;
import com.emc.caspian.encryption.AESUtil;


/**
 * Method to get Keystone URL from CRS
 * @author mehroa3
 */


public class GetKeystoneDetails {

	private static final String KEYSTONE="keystone"; 
	private static final String PLATFORM="platform" ;
	private static final String COMPONENT_REGISTRY="COMPONENT_REGISTRY";

	public static String getKeystoneBaseUrl() throws Exception {

		if (System.getenv().containsKey(COMPONENT_REGISTRY)){
			AppLogger.info("Component registry ip:%s",System.getenv(COMPONENT_REGISTRY));
		}else {
			AppLogger.error("CRS IP is missing from environment variables");
			throw new IOException("CRS IP is missing from environment variables");
		}

		//Picking up private keystone IP
		StringBuilder keystoneBaseUri = null;
		try{
			ServiceLocationClient client = CRSClientBuilder.newServiceLocationClient();
			List<ComponentEndpoint> endpoints = client.getAllEndpoints(PLATFORM, KEYSTONE);
			if (endpoints.size() > 0) {
				for (ComponentEndpoint compEndPoint : endpoints) {
					if(compEndPoint.getType().equalsIgnoreCase("private")){
						String url = compEndPoint.getUrl();
						AppLogger.debug("Keystone URL %s",url);
						if(url!=null){
							keystoneBaseUri=new StringBuilder(url);
							break;
						}else{
							AppLogger.error("Keystone ip is null in CRS");
							throw new Exception("Keystone ip is null in CRS");
						}
					}
				}
			}

		} catch (Exception e) {
			AppLogger.error("Unable to get Keystone Base URI from CRS, exiting");
		}
		AppLogger.debug("Keystone service's base url :: " + keystoneBaseUri);
		return keystoneBaseUri.toString();

	}

	public static char[] getKeystonePassword()throws Exception{

		final String KsEncPwd,KsPwd; 

		if (System.getenv().containsKey("KS_CSA_PWD")){
			KsEncPwd = System.getenv("KS_CSA_PWD");
		}else {
			AppLogger.error("Keystone password is missing from environment variables");
			throw new Exception("Keystone password is missing from environment variables");
		}
		try{
			AESUtil au = AESUtil.getInstance();
			KsPwd = au.decrypt(KsEncPwd);
		}catch(Exception e){
			AppLogger.error("Error decrpting keystone password",e);
			throw e;
		}		

		return KsPwd.toCharArray();

	}


}