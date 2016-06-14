package com.emc.caspian.ccs.license.util;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.emc.caspian.ccs.license.ErrorMessages;
import com.emc.caspian.ccs.license.ErrorObject.ErrorMessage;
import com.emc.caspian.ccs.license.ExtractProperties;
import com.emc.caspian.ccs.license.RestClientUtil;
import com.emc.caspian.ccs.license.ScaleioException;
import com.emc.caspian.crs.CRSClientBuilder;
import com.emc.caspian.crs.ServiceLocationClient;

public class GetScaleIODetails {

	public static String getScaleioBaseURL() throws ScaleioException {
		final String SCALEIO_SERVICE="scaleio-gateway"; 
		final String PLATFORM="platform" ;
		StringBuilder scaleioURL=null;

		try{
			ServiceLocationClient client = CRSClientBuilder.newServiceLocationClient();
			Optional<URL> endpoint = client.getEndpoint(PLATFORM, SCALEIO_SERVICE);
			if (endpoint.isPresent()) {
				scaleioURL = new StringBuilder(endpoint.get().toExternalForm());
			}
		} catch (Exception e) {
			AppLogger.error(ErrorMessages.SCALEIO_URL_CRS_ERROR_MESSAGE,e);
			throw new ScaleioException(ErrorMessages.SCALEIO_URL_CRS_ERROR_MESSAGE);
		}
		if(scaleioURL.toString().isEmpty()){
			AppLogger.error(ErrorMessages.SCALEIO_URL_CRS_EMPTY_MESSAGE);
			throw new ScaleioException(ErrorMessages.SCALEIO_URL_CRS_EMPTY_MESSAGE);
		}  
		scaleioURL=scaleioURL.append("/");
		AppLogger.debug("ScaleIO gateway's service's base url :: %s" , scaleioURL);
		return scaleioURL.toString();
	}

	public static String getScaleioSystemID(String ScaleioGatewayURL, Map<String,String> headers) throws ScaleioException {

		String systemID=null;

		try{
			HttpResponse httpgetinstances = RestClientUtil.httpGETRequest(ScaleioGatewayURL.concat("api/instances"), headers).get();
			String responseScaleIO = EntityUtils.toString(httpgetinstances.getEntity(),StandardCharsets.UTF_8);

			AppLogger.debug("ScaleIO instances response got from gateway: ", responseScaleIO);
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(responseScaleIO);
			JsonNode detailsNode = rootNode.path("protectionDomainList");
			Iterator<JsonNode> elements = detailsNode.getElements();
			if(elements.hasNext()){
				JsonNode nodeDetails = elements.next();
				JsonNode urlNode = nodeDetails.path("systemId");
				systemID = urlNode.getTextValue();
			}
			else{
				AppLogger.error(ErrorMessages.SCALEIO_SYSTEMID_EXTRACT_ERROR_MESSAGE);
				throw new ScaleioException(ErrorMessages.SCALEIO_SYSTEMID_EXTRACT_ERROR_MESSAGE);
			}

			return systemID;
		}catch(Exception e){
			AppLogger.error(ErrorMessages.SCALEIO_SYSTEMID_CALL_ERROR_MESSAGE,e);
			throw new ScaleioException(ErrorMessages.SCALEIO_SYSTEMID_CALL_ERROR_MESSAGE);
		}

	}

	public static String getScaleioLogin(String scaleioBaseUrl) throws ScaleioException{

		final String loginUrlAppend ="api/login";
		String encodedCredentials=null;
		String scaleioCredentials=null;
		try {
			scaleioCredentials = new StringBuilder().append(ExtractProperties.getScaleioUsername())
					.append(":").append(ExtractProperties.getScaleioPassword()).toString(); 	
		}catch(Exception e){
			AppLogger.error("Error reading Scaleio credentials from conf file",e);
			throw new ScaleioException("Error reading Scaleio credentials from conf file");
		}
		try{
			String encoding = Base64.getEncoder().encodeToString(scaleioCredentials.getBytes(StandardCharsets.UTF_8));
			Map<String, String> loginHeaders = new Hashtable<String, String>();
			loginHeaders.put("Authorization", "Basic "+encoding);
			HttpResponse httpRegisterResponse = RestClientUtil.httpGETRequest(scaleioBaseUrl.concat(loginUrlAppend), loginHeaders).get();
			String responseString = EntityUtils.toString(httpRegisterResponse.getEntity(), StandardCharsets.UTF_8);

			String takeOutQuotes = responseString.substring(1, responseString.length()-1);
			String credentials = new StringBuilder().append( ExtractProperties.getScaleioUsername())
					.append(":").append(takeOutQuotes).toString();
			encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8) );

		}catch(Exception e){
			AppLogger.error("Error getting and encoding token from Scaleio gateway",e);
			throw new ScaleioException("Error getting and encoding token from Scaleio gateway");
		}
		return encodedCredentials;
	}

}
