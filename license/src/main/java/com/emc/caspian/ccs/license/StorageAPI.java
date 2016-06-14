package com.emc.caspian.ccs.license;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.emc.caspian.ccs.license.util.AppLogger;
import com.emc.caspian.ccs.license.util.GetScaleIODetails;

public class StorageAPI {

	public static void writeToFile(InputStream uploadedInputStream,
			String uploadedFileLocation) throws IOException {

		try {
			String rawFileLocation = "./data/raw/"+ System.currentTimeMillis() + ".lic";
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			OutputStream outRaw = new FileOutputStream(new File(rawFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
				outRaw.write(bytes, 0, read);
			}
			out.flush();
			out.close();
			outRaw.flush();
			outRaw.close();
		} catch (IOException e) {
			AppLogger.error("Error copying the license file.", e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static void saveLicenses(Set<License> licenses) throws Exception {

		try {
			JSONParser parser = new JSONParser();
			Object dataObject = parser.parse("[]");
			JSONArray dataArray = (JSONArray) dataObject;

			for (License license : licenses) {

				ObjectMapper mapper = new ObjectMapper();
				JSONObject json = mapper.readValue(mapper.writeValueAsString(license), JSONObject.class);
				dataArray.add(json);				
			}
			ETCDClient.persistToEtcd("licenses", dataArray.toString());
			AppLogger.info("Persisting data to ETCD.");
		} catch (Exception e) {
			AppLogger.error("Error saving to ETCD.", e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONArray retrieveAll(boolean showScaleIO) throws Exception {
		JSONArray dataArray = new JSONArray();
		JSONArray resultArray = new JSONArray();
		JSONArray concatArray = new JSONArray();
		try {
			JSONParser parser = new JSONParser();
			Object dataObject = parser.parse(ETCDClient.fetchValueFromEtcd("licenses"));
			dataArray = (JSONArray) dataObject;
			AppLogger.debug("Read data from ETCD");

			if(dataArray.size()>0){
				JSONArray permanentDataArray = new JSONArray();
				JSONArray subscriptionDataArray = new JSONArray();

				for(int i=0; i<dataArray.size();i++){
					JSONObject licenseObject = (JSONObject) dataArray.get(i);
					JSONObject propertyObject= (JSONObject) licenseObject.get("properties");
					if(propertyObject.get(LicenseStrings.TYPE).equals(LicenseStrings.PERMANENT)){
						permanentDataArray.add(licenseObject);
					}else if(propertyObject.get(LicenseStrings.TYPE).equals(LicenseStrings.SUBSCRIPTION)){
						subscriptionDataArray.add(licenseObject);
					}else{
						concatArray.add(licenseObject);
					}
				}
				if(permanentDataArray.size()>1){
				concatArray.addAll(permanentConcatenate(permanentDataArray));
				}else if(permanentDataArray.size()==1){
						concatArray.add(permanentDataArray);
					}
				
				if(subscriptionDataArray.size()>1){
				concatArray.addAll(sunscriptionConcatenate(subscriptionDataArray));
				}else if(subscriptionDataArray.size()==1){
					concatArray.add(subscriptionDataArray);
				}
				dataArray=concatArray;
			}

			for (int i = 0; i < dataArray.size(); i++) {
				JSONObject licenseObject = (JSONObject) dataArray.get(i);
				licenseObject.remove("expirationDate");
				String name = licenseObject.get("name").toString();

				if(name.startsWith("SIO_") && !showScaleIO){
					continue;
				}else if(name.startsWith("CASPIAN_")){
					String truncName= name.substring(8);
					licenseObject.replace("name", truncName);
				}
				resultArray.add(licenseObject);
			}

		} catch (Exception e) {
			AppLogger.error("Error retrieving the licenses", e);
			throw e;
		}
		return resultArray;

	}

	@SuppressWarnings("unchecked")
	public static JSONArray retrieveLicenses(String queryBy,String queryValue)
			throws Exception {

		JSONArray dataArray = new JSONArray();
		JSONArray resultArray = new JSONArray();
		try {
			JSONParser parser = new JSONParser();
			Object dataObject = parser.parse(ETCDClient.fetchValueFromEtcd("licenses"));
			dataArray = (JSONArray) dataObject;
			AppLogger.debug("Read data from ETCD");

			for (int i = 0; i < dataArray.size(); i++) {
				JSONObject licenseObject = (JSONObject) dataArray.get(i);
				boolean isFound=false;

				if(licenseObject.containsKey(queryBy)){
					if(licenseObject.get(queryBy).equals(queryValue)){
						isFound=true;
					}
				}else{
					JSONObject propertyObject= (JSONObject) licenseObject.get("properties");
					if( propertyObject.containsKey(queryBy)){
						if(propertyObject.get(queryBy).equals(queryValue)){
							isFound=true;
						}
					}
				}
				if (isFound) {
					licenseObject.remove("expirationDate");
					resultArray.add(licenseObject);
				}
			}
		} catch (Exception e) {
			AppLogger.error("Error retrieving licenses:" + queryBy + "="+queryValue, e);
			throw e;
		}
		return resultArray;
	}

	public static boolean deleteLicense(String licenseHash) throws Exception{
		JSONArray dataArray = new JSONArray();
		JSONObject licenseObject= null;
		boolean isFound = false;
		try{
			JSONParser parser = new JSONParser();
			Object dataObject = parser.parse(ETCDClient.fetchValueFromEtcd("licenses"));
			dataArray = (JSONArray) dataObject;
			AppLogger.debug("Read data from ETCD");
			int i=0;
			while(i<dataArray.size() && isFound==false) {
				licenseObject = (JSONObject) dataArray.get(i);

				if(licenseObject.get("id").equals(licenseHash)){
					isFound=true;
					dataArray.remove(i);	
				}
				i++;
			}
			ETCDClient.persistToEtcd("licenses", dataArray.toString());
			AppLogger.info("Persisting data to ETCD.");
			AppLogger.info("This license was deleted--"+licenseObject);

		}catch (Exception e) {
			AppLogger.error("Error deleting the license:" + licenseHash, e);
			throw e;
		}
		return isFound;
	}

	public static void deleteAll() throws Exception{
		JSONArray dataArray = new JSONArray();

		try{
			dataArray.clear();
			ETCDClient.persistToEtcd("licenses", dataArray.toString());
			AppLogger.info("Persisting data to ETCD.");
			AppLogger.info("All licenses were deleted");

		}catch (Exception e) {
			AppLogger.error("Error deleting the licenses:" + e);
			throw e;
		}
	}
	@SuppressWarnings("unchecked")
	public static void listProperties() throws Exception{

		try{
			JSONParser parser = new JSONParser();
			Object dataObject = parser.parse(ETCDClient.fetchValueFromEtcd("licenses"));
			JSONArray dataArray = (JSONArray) dataObject;
			AppLogger.debug("Read data from ETCD");

			if(dataArray.isEmpty()){
				AppLogger.error("No licenses found in ETCD while listing licenses");
				throw new Exception("No licenses found in ETCD while listing licenses");
			}

			JSONObject licenseIterateObject = (JSONObject) dataArray.get(0);
			ArrayList<String> tagList = new ArrayList<String>(licenseIterateObject.keySet());
			JSONObject propertyiterateObject= (JSONObject) licenseIterateObject.get("properties");
			ArrayList<String> propertiesTagList = new ArrayList<String>(propertyiterateObject.keySet());
			tagList.addAll(propertiesTagList);

			for(int i = 1; i<dataArray.size(); i++){
				licenseIterateObject = (JSONObject) dataArray.get(i);
				ArrayList<String> tempTagList = new ArrayList<String>(licenseIterateObject.keySet());
				for(String temp:tempTagList){
					if(!tagList.contains(temp)){
						tagList.add(temp);
					}
				}
				JSONObject tempPropertyiterateObject= (JSONObject) licenseIterateObject.get("properties");
				ArrayList<String> tempPropertiesTagList = new ArrayList<String>(tempPropertyiterateObject.keySet());
				for(String temp:tempPropertiesTagList){
					if(!tagList.contains(temp)){
						tagList.add(temp);
					}
				}
			}

			JSONObject propertyJson = new JSONObject();
			propertyJson.put("properties", tagList);
			ETCDClient.persistToEtcd("licenseProperties", propertyJson.toString());
			AppLogger.info("Persisting data to ETCD.");

		}catch(Exception e){
			AppLogger.error("Error listing the tags of the licenses", e);
			throw e;
		}

	}
	public static boolean isPropertyPresent(String property)throws Exception{
		JSONParser parser = new JSONParser();
		try{
			Object propertiesObject = parser.parse(ETCDClient.fetchValueFromEtcd("licenseProperties"));
			JSONObject propertiesJSON = (JSONObject) propertiesObject;
			ArrayList<String> propertiesList = (ArrayList<String>)propertiesJSON.get("properties");
			AppLogger.debug("Read properties data from ETCD");
			return propertiesList.contains(property);
		}catch(Exception e){
			AppLogger.error("Error listing the tags of the licenses", e);
			throw e;
		}

	}

	public static String scaleioPost(String uploadFileLocation) throws ScaleioException{

		try{
			String scaleioBaseURL = GetScaleIODetails.getScaleioBaseURL();

			String encodedCredentials = GetScaleIODetails.getScaleioLogin(scaleioBaseURL); 

			Map<String,String> callHeaders = new Hashtable<String, String>();
			callHeaders.put("Authorization", "Basic "+encodedCredentials);

			FileInputStream fis = new FileInputStream(uploadFileLocation);
			String lic = "";
			char current;
			while (fis.available() > 0) {
				current = (char) fis.read();
				lic = lic+current;
			}
			lic=lic.replace("\r", "");
			lic=lic.replace("\n","");
			lic=lic.replace("\t", "");
			lic=lic.replace("\\", "\\\\ \\n");
			lic=lic.replace("\"","\\\"");
			lic=lic.replace("INCREMENT","\\n INCREMENT");
			lic=lic.replace("END_LICENSE", "\\n END_LICENSE");
			String pass= "{\"key\":\""+lic+"\"}";

			callHeaders.put("Content-Type", "application/json");

			String systemID = GetScaleIODetails.getScaleioSystemID(scaleioBaseURL, callHeaders);
			String postLicenseURL = new StringBuilder().append(scaleioBaseURL).append("api/instances/System::")
					.append(systemID).append("/action/setLicense").toString();
			HttpResponse postResponse = RestClientUtil.httpPostRequest(postLicenseURL, callHeaders, pass).get();
			String responsePost = EntityUtils.toString(postResponse.getEntity(),StandardCharsets.UTF_8);
			int responseCode =postResponse.getStatusLine().getStatusCode();

			AppLogger.info("Blockstorage license post response. Response code:%s, Response body:%s",
					responseCode,responsePost);
			if(responseCode!=200){
				AppLogger.error("Blockstorage license post was unsuccessful. Response code:%s, Response body:%s",
						responseCode,responsePost);
			}

			return new StringBuilder().append(responseCode).append(":").append(responsePost).toString();

		}
		catch(ScaleioException e){
			AppLogger.error(e.toString());
			throw e;
		}
		catch(Exception e){
			AppLogger.error("Error posting licenses to Scaleio", e);
			throw new ScaleioException("Error posting licenses to Scaleio");
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONArray sunscriptionConcatenate(JSONArray dataArray) throws Exception {
		try{
			ArrayList<String> diffNames = new ArrayList<String>();
			ArrayList<Integer> diffNumber = new ArrayList<Integer>();
			ArrayList<String> positions = new ArrayList<String>();

			int size=1;
			JSONObject licenseObject = (JSONObject) dataArray.get(0);
			diffNames.add((String) licenseObject.get("name"));
			diffNumber.add(1);
			positions.add("0");
			for(int i=1;i<dataArray.size();i++){
				JSONObject licenseObjectIterate = (JSONObject) dataArray.get(i);
				String name =(String) licenseObjectIterate.get("name");
				if(diffNames.contains(name)){
					for(int j=0;j<size;j++){
						if(name.equals(diffNames.get(j))){
							diffNumber.set(j, diffNumber.get(j)+1);
							String temp = positions.get(j);
							temp=temp+","+i;
							positions.set(j, temp);
							break;
						}
					}
				}
				else{
					size++;
					diffNames.add(name);
					diffNumber.add(1);
					positions.add(i+"");
				}
			}

			for(int i=0;i<size;i++){
				if(diffNumber.get(i)>1){
					String positionString = positions.get(i);
					String[] positionArray = positionString.split(",");

					long start = 0, end = 0,tempStart, tempEnd;
					for(int j=0;j<diffNumber.get(i);j++){
						JSONObject licenseObjectIterate = (JSONObject) dataArray.get(Integer.parseInt(positionArray[j]));
						JSONObject propertyObject= (JSONObject) licenseObjectIterate.get("properties");
						long currentTime=System.currentTimeMillis();

						tempStart=Long.valueOf( (String) (propertyObject.get(LicenseStrings.START_DATE)));
						tempEnd=Long.valueOf( (String) (propertyObject.get(LicenseStrings.END_DATE)));

						if(start==0 && end ==0){
							if(tempStart<currentTime && tempEnd>currentTime){
								start=tempStart;
								end=tempEnd;
							}
						}else{
							if(tempStart>currentTime && tempStart<=end){
								end=tempStart;
							}else if(tempEnd<currentTime && tempEnd<=start){
								start=tempEnd;
							}else{
								if(tempStart>start){
									start=tempStart;
								}
								if(tempEnd<end){
									end=tempEnd;
								}
							}
						}
					}
					if(start==0 && end==0){
						JSONObject licenseObjectIterate = (JSONObject) dataArray.get(Integer.parseInt(positionArray[0]));
						JSONObject propertyObject= (JSONObject) licenseObjectIterate.get("properties");
						start=Long.valueOf( (String) (propertyObject.get(LicenseStrings.START_DATE)));
						end=Long.valueOf( (String) (propertyObject.get(LicenseStrings.END_DATE)));
						for(int j=1;j<diffNumber.get(i);j++){
							licenseObjectIterate = (JSONObject) dataArray.get(Integer.parseInt(positionArray[j]));
							propertyObject= (JSONObject) licenseObjectIterate.get("properties");
							tempStart=Long.valueOf( (String) (propertyObject.get(LicenseStrings.START_DATE)));
							tempEnd=Long.valueOf( (String) (propertyObject.get(LicenseStrings.END_DATE)));

							if(tempStart<=start){
								if(tempEnd<=start){
									end=tempEnd;
								}else{
									end=start;
								}
								start=tempStart;
							}else{
								if(tempStart<end){
									end=tempStart;
								}
							}								
						}
					}
					long limit=0;
					for(int j=0;j<diffNumber.get(i);j++){
						JSONObject licenseObjectIterate = (JSONObject) dataArray.get(Integer.parseInt(positionArray[j]));
						JSONObject propertyObject= (JSONObject) licenseObjectIterate.get("properties");
						tempStart=Long.valueOf( (String) (propertyObject.get(LicenseStrings.START_DATE)));
						tempEnd=Long.valueOf( (String) (propertyObject.get(LicenseStrings.END_DATE)));
						if(tempStart>=start){
							if(tempEnd<=end||tempStart<end){
								long tempLimit=Long.valueOf( (String) (propertyObject.get(LicenseStrings.LIMIT)));
								limit=limit+tempLimit;
							}
						}else{
							if(tempEnd>start){
								long tempLimit=Long.valueOf( (String) (propertyObject.get(LicenseStrings.LIMIT)));
								limit=limit+tempLimit;
							}
						}
					}
					JSONObject licenseObjectIterate = (JSONObject) dataArray.get(Integer.parseInt(positionArray[0]));
					JSONObject propertyObject= (JSONObject) licenseObjectIterate.get("properties");
					propertyObject.replace(LicenseStrings.START_DATE, start+"");
					propertyObject.replace(LicenseStrings.END_DATE, end+"");
					propertyObject.replace(LicenseStrings.LIMIT, limit+"");
				}
			}
			JSONArray tempDataArray = new JSONArray();

			for(int i=0;i<size;i++){
				if(diffNumber.get(i)>1){
					String positionString = positions.get(i);
					String[] positionArray = positionString.split(",");
					tempDataArray.add((JSONObject) dataArray.get(Integer.parseInt(positionArray[0])));
				}
				else{
					String positionString = positions.get(i);
					tempDataArray.add((JSONObject) dataArray.get(Integer.parseInt(positionString)));
				}
			}
			return tempDataArray;

		}catch(Exception e){
			AppLogger.error("Error while concatenating the licenses", e);
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public static JSONArray permanentConcatenate(JSONArray dataArray) throws Exception {
		try{
			ArrayList<String> diffNames = new ArrayList<String>();
			ArrayList<Integer> diffNumber = new ArrayList<Integer>();
			ArrayList<String> positions = new ArrayList<String>();

			int size=1;
			JSONObject licenseObject = (JSONObject) dataArray.get(0);
			diffNames.add((String) licenseObject.get("name"));
			diffNumber.add(1);
			positions.add("0");
			for(int i=1;i<dataArray.size();i++){
				JSONObject licenseObjectIterate = (JSONObject) dataArray.get(i);
				String name =(String) licenseObjectIterate.get("name");
				if(diffNames.contains(name)){
					for(int j=0;j<size;j++){
						if(name.equals(diffNames.get(j))){
							diffNumber.set(j, diffNumber.get(j)+1);
							String temp=positions.get(j);
							temp=temp+","+i;
							positions.set(j, temp);
							break;
						}
					}
				}else{
					size++;
					diffNames.add(name);
					diffNumber.add(1);
					positions.add(i+"");
				}
			}

			long limit=0;
			for(int i=0;i<size;i++){
				if(diffNumber.get(i)>1){
					String positionString = positions.get(i);
					String[] positionArray = positionString.split(",");

					for(int j=0;j<diffNumber.get(i);j++){
						JSONObject licenseObjectIterate = (JSONObject) dataArray.get(Integer.parseInt(positionArray[j]));
						JSONObject propertyObject= (JSONObject) licenseObjectIterate.get("properties");
						long tempLimit=Long.valueOf( (String) (propertyObject.get(LicenseStrings.LIMIT)));
						limit=limit+tempLimit;
					}
					JSONObject licenseObjectIterate = (JSONObject) dataArray.get(Integer.parseInt(positionArray[0]));
					JSONObject propertyObject= (JSONObject) licenseObjectIterate.get("properties");
					propertyObject.replace(LicenseStrings.LIMIT, limit+"");


				}
			}
			JSONArray tempDataArray = new JSONArray();

			for(int i=0;i<size;i++){
				if(diffNumber.get(i)>1){
					String positionString = positions.get(i);
					String[] positionArray = positionString.split(",");
					tempDataArray.add((JSONObject) dataArray.get(Integer.parseInt(positionArray[0])));
				}
				else{
					String positionString = positions.get(i);
					tempDataArray.add((JSONObject) dataArray.get(Integer.parseInt(positionString)));
				}
			}
			return tempDataArray;
		}		catch(Exception e){
			AppLogger.error("Error concatenating permanent licenses", e);
			throw e;
		}

	}


}
