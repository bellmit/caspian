/**
 * 
 */
package com.emc.caspian.ccs.license;

import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.license.util.AppLogger;
import com.emc.caspian.crs.CRSClientBuilder;
import com.emc.caspian.crs.ServiceLocationClient;
import com.emc.caspian.crs.model.ComponentEndpoint;
import com.google.common.collect.Lists;


public class ETCDClient {
	private static final String ETCD_KEY_PREFIX = "v2/keys/caspian/ccs-license/";
	private static final String LOCAL_ETCD_URI="http://localhost:4001" ;
	private static final String ETCD_SERVICE="etcd-service"; 
	private static final String PLATFORM="platform" ;

	public static void persistToEtcd(String key, String value) throws Exception {

		StringBuilder etcdURI = getEtcdBaseUrl().append(ETCD_KEY_PREFIX)
				.append(key);
		List<BasicNameValuePair> data = Lists.newArrayList();
		data.add(new BasicNameValuePair("value", value));

		HttpResponse httpRegisterResponse = RestClientUtil.httpRequest(
				HttpPut.METHOD_NAME, etcdURI.toString(), null, data).get();

		String responseString = EntityUtils.toString(
				httpRegisterResponse.getEntity(), "UTF-8");
		EtcdResult etcdResp = JsonHelper.deserializeFromJson(responseString,
				EtcdResult.class);

		if (etcdResp.isError()) {
			if (etcdResp.errorCode == 100) {
				throw new RuntimeException(
						"Error reading production key from etcd store");
			}
		}
		AppLogger.debug("License VE " + key
				+ " has been successfully persisted in etcd kv store :: "
				+ etcdResp.message);

	}

	public static String fetchValueFromEtcd(String key) throws Exception {
		StringBuilder etcdURI = getEtcdBaseUrl().append(ETCD_KEY_PREFIX)
				.append(key);
		HttpResponse httpRegisterResponse = RestClientUtil.httpGETRequest(
				etcdURI.toString(), null).get();

		String responseString = EntityUtils.toString(
				httpRegisterResponse.getEntity(), "UTF-8");
		AppLogger.debug("License VE details got from etcd kv store :: "
				+ responseString);
		EtcdResult etcdResp = JsonHelper.deserializeFromJson(responseString,
				EtcdResult.class);

		if (etcdResp.isError()) {
			if (etcdResp.message.equals("Key not found"))
				return "[]";
			if (etcdResp.errorCode == 100) {
				throw new RuntimeException(
						"Error reading production key from etcd store.");
			}
		}
		return etcdResp.node.value;
	}

	private static StringBuilder getEtcdBaseUrl() throws IOException {

		String localNodeIP =null;
		if (System.getenv().containsKey("CONTAINER_HOST_ADDRESS")){
			localNodeIP = System.getenv("CONTAINER_HOST_ADDRESS");
		}else {
			AppLogger.error("Node IP is missing from environment variables");
			throw new IOException("Node IP is missing from environment variables");
		}

		//Setting localETCD as default URI else default to first node 
		boolean isEtcdRead=false;
		StringBuilder etcdBaseUri = new StringBuilder(LOCAL_ETCD_URI);
		try{
			ServiceLocationClient client = CRSClientBuilder.newServiceLocationClient();
			List<ComponentEndpoint> endpoints = client.getAllEndpoints(PLATFORM, ETCD_SERVICE);
			if (endpoints.size() > 0) {
				for (ComponentEndpoint compEndPoint : endpoints) {
					String url = compEndPoint.getUrl();
					AppLogger.debug("ETCD URL %s",url);
					if(localNodeIP!=null && url.contains(localNodeIP)){
						etcdBaseUri=new StringBuilder(url);
						isEtcdRead=true;
						break;
					}
				}
				if(!isEtcdRead){
					etcdBaseUri = new StringBuilder(endpoints.get(0).getUrl());
				}
			}

		} catch (Exception e) {
			AppLogger.error("Unable to get ETCD Base URI from CRS, Using the default URI");
		}
		etcdBaseUri=etcdBaseUri.append("/");
		AppLogger.debug("ETCD service's base url :: " + etcdBaseUri);
		return etcdBaseUri;
	}
}
