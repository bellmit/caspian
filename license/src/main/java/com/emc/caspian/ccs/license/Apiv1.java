package com.emc.caspian.ccs.license;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.emc.caspian.ccs.license.AuthorizationPolicy.Rule;
import com.emc.caspian.ccs.license.LicenseFileReader.LicenseFileResults;
import com.emc.caspian.ccs.license.util.AppLogger;

@Path("/v1")
public class Apiv1 {

	final String uploadFileLocation = "./data/sample.lic";
	@Context
	public SecurityContext securityContext;

	@POST
	@Path("/licenses")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN})
	public Response uploadFile(InputStream io) throws Exception {

		try {
			StorageAPI.writeToFile(io, uploadFileLocation);

			File file = new File(uploadFileLocation);
			LicenseReader reader = new LicenseReader();
			LicenseFileResults newLicense = reader.readFile(file);

			if(newLicense.isEmpty()==true){
				ErrorPayload error= new ErrorPayload(ErrorMessages.NO_LICENSES_CODE,ErrorMessages.NO_LICENSES_MESSAGE);
				return Response.status(400).entity(error).build();


			}else if(newLicense.isAllCorrupted()==true){
				ErrorPayload error= new ErrorPayload(ErrorMessages.ALL_LICENSES_CORRUPTED_CODE,ErrorMessages.ALL_LICENSES_CORRUPTED_MESSAGE);
				return Response.status(400).entity(error).build();

			}else if(newLicense.getNumberCorrupted()!=0){
				Set<License> licenses = newLicense.getLicenses();
				ErrorPayload error= new ErrorPayload(ErrorMessages.SOME_LICENSES_CORRUPTED_CODE,
						ErrorMessages.SOME_LICENSES_CORRUPTED_MESSAGE+newLicense.getFeaturesCorrupted());
				return Response.status(400).entity(error).build();
			}else if (newLicense.getContainsExpired()){
				Set<License> licenses = newLicense.getLicenses();
				ErrorPayload error=new ErrorPayload(ErrorMessages.EVAL_LICENSE_EXPIRED_CODE,
						ErrorMessages.EVAL_LICENSE_EXPIRED_MESSAGE+newLicense.getExpiredNames());
				return Response.status(400).entity(error).build();
			}

			Set<License> licenses = newLicense.getLicenses();

			StorageAPI.saveLicenses(licenses);
			StorageAPI.listProperties();

			try {
				AppLogger.info("Request to retrieve all licenses to be shown");
				JSONObject responseJSON = new JSONObject();
				JSONArray licenseArray = StorageAPI.retrieveAll(false);
				responseJSON.put("licenses", licenseArray);
				return Response.status(200).entity(responseJSON).build();
			} catch (Exception e) {
				AppLogger.error("Error retrieving licenses.", e);
				return Response.status(500).build();
			}

		} catch (ScaleioException e){
			AppLogger.error("Error posting licenses to Block storage",e);
			ErrorPayload error = new ErrorPayload(ErrorMessages.SCALEIO_POSTING_ERROR_CODE,
					ErrorMessages.SCALEIO_POSTING_ERROR_MESSAGE);
			return Response.status(500).entity(error).build();
		} catch (ELMException e) {
			AppLogger.error("Error parsing the license file",e);
			ErrorPayload error = new ErrorPayload(ErrorMessages.ALL_LICENSES_CORRUPTED_CODE,
					ErrorMessages.ALL_LICENSES_CORRUPTED_MESSAGE);
			return Response.status(400).entity(error).build();
		}

		catch (Exception e) {
			AppLogger.error("Error uploading licenses.", e);
			return Response.status(500).build();
		}

	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/licenses")
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_MONITOR})
	public Response retrieveShow(@Context UriInfo uriInfo) {

		ParseUri parseObject = new ParseUri(uriInfo);
		Map values = parseObject.parse();
		if(parseObject.getStatus()!=-1){
			if(parseObject.getStatus()==1){
				try {
					AppLogger.info("Request to retrieve all licenses to be shown as no query operator found in request");
					JSONObject responseJSON = new JSONObject();
					JSONArray licenseArray = StorageAPI.retrieveAll(false);
											
					responseJSON.put("licenses", licenseArray);
					return Response.status(200).entity(responseJSON).build();
				} catch (Exception e) {
					AppLogger.error("Error retrieving licenses.", e);
					return Response.status(500).build();
				}
			}else if(parseObject.getStatus()==2){
				AppLogger.error(ErrorMessages.GET_LICENSE_INVALID_QUERY_CODE + ":" + ErrorMessages.GET_LICENSE_INVALID_QUERY_MESSAGE+ " because no = found in query string");
				ErrorPayload error= new ErrorPayload(ErrorMessages.GET_LICENSE_INVALID_QUERY_CODE,ErrorMessages.GET_LICENSE_INVALID_QUERY_MESSAGE);				
				return Response.status(400).entity(error).build();
			}else{
				AppLogger.error(ErrorMessages.GET_LICENSE_INVALID_QUERY_CODE + ":" + ErrorMessages.GET_LICENSE_INVALID_QUERY_MESSAGE+
						" QueryBy is empty");
				ErrorPayload error= new ErrorPayload(ErrorMessages.GET_LICENSE_INVALID_QUERY_CODE,ErrorMessages.GET_LICENSE_INVALID_QUERY_MESSAGE);				
				return Response.status(400).entity(error).build();

			}
		}else{	
			Object[] querySet=values.keySet().toArray();
			String queryBy = querySet[0].toString();

			String queryValue=values.get(queryBy).toString();
			try{
				AppLogger.info("Request to retrieve licenses of type " + queryBy+":"+ queryValue);
				JSONObject responseJSON = new JSONObject();
				if(!StorageAPI.isPropertyPresent(queryBy)){
					return Response.status(200).entity("{}").build();
				}
				JSONArray licenseArray = StorageAPI.retrieveLicenses(queryBy,queryValue);
				if (licenseArray.isEmpty()) {			
					return Response.status(200).entity("{}").build();
				} else {
					responseJSON.put("licenses", licenseArray);
					return Response.status(200).entity(responseJSON).build();
				}

			} catch (Exception e) {
				AppLogger.error("Error retrieving licenses of type:" + queryBy + "and value:"+ queryValue, e);
				return Response.status(500).build();
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/licenses/dump")
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_MONITOR})
	public Response retrieveAll(@Context UriInfo uriInfo) {

		ParseUri parseObject = new ParseUri(uriInfo);
		Map values = parseObject.parse();
		if(parseObject.getStatus()!=-1){
			if(parseObject.getStatus()==1){
				try {
					AppLogger.info("Request to retrieve all licenses as no query operator found in request");
					JSONObject responseJSON = new JSONObject();
					JSONArray licenseArray = StorageAPI.retrieveAll(true);
					responseJSON.put("licenses", licenseArray);
					return Response.status(200).entity(responseJSON).build();
				} catch (Exception e) {
					AppLogger.error("Error retrieving licenses.", e);
					return Response.status(500).build();
				}
			}else if(parseObject.getStatus()==2){
				AppLogger.error(ErrorMessages.GET_LICENSE_INVALID_QUERY_CODE + ":" + ErrorMessages.GET_LICENSE_INVALID_QUERY_MESSAGE+ " because no = found in query string");
				ErrorPayload error= new ErrorPayload(ErrorMessages.GET_LICENSE_INVALID_QUERY_CODE,ErrorMessages.GET_LICENSE_INVALID_QUERY_MESSAGE);				
				return Response.status(400).entity(error).build();
			}else{
				AppLogger.error(ErrorMessages.GET_LICENSE_INVALID_QUERY_CODE + ":" + ErrorMessages.GET_LICENSE_INVALID_QUERY_MESSAGE+
						" QueryBy is empty");
				ErrorPayload error= new ErrorPayload(ErrorMessages.GET_LICENSE_INVALID_QUERY_CODE,ErrorMessages.GET_LICENSE_INVALID_QUERY_MESSAGE);				
				return Response.status(400).entity(error).build();

			}
		}else{	
			Object[] querySet=values.keySet().toArray();
			String queryBy = querySet[0].toString();

			String queryValue=values.get(queryBy).toString();
			try{
				AppLogger.info("Request to retrieve licenses of type " + queryBy+":"+ queryValue);
				JSONObject responseJSON = new JSONObject();
				if(!StorageAPI.isPropertyPresent(queryBy)){
					return Response.status(200).entity("{}").build();
				}
				JSONArray licenseArray = StorageAPI.retrieveLicenses(queryBy,queryValue);
				if (licenseArray.isEmpty()) {			
					return Response.status(200).entity("{}").build();
				} else {
					responseJSON.put("licenses", licenseArray);
					return Response.status(200).entity(responseJSON).build();
				}

			} catch (Exception e) {
				AppLogger.error("Error retrieving licenses of type:" + queryBy + "and value:"+ queryValue, e);
				return Response.status(500).build();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/licenses/{component}/entitlements")
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_MONITOR})
	public Response retrieveLicenses(@PathParam("component") String featureName) {
		try {
			AppLogger.info("Request to retrieve licenses of name:" + featureName);
			JSONObject responseJSON = new JSONObject();
			JSONArray licenseArray = StorageAPI.retrieveLicenses("name",featureName);
			if (licenseArray.isEmpty()) {
				AppLogger.info(ErrorMessages.GET_LICENSE_NOT_FOUND_CODE+":"+ErrorMessages.GET_LICENSE_NOT_FOUND_MESSAGE + ":" + featureName);
				ErrorPayload error= new ErrorPayload(ErrorMessages.GET_LICENSE_NOT_FOUND_CODE,ErrorMessages.GET_LICENSE_NOT_FOUND_MESSAGE);				
				return Response.status(404).entity(error).build();
			} else {
				responseJSON.put("licenses", licenseArray);
				return Response.status(200).entity(responseJSON).build();
			}

		} catch (Exception e) {
			AppLogger.error("Error retrieving licenses of name:"+featureName, e);
			return Response.status(500).build();
		}
	}

	@DELETE
	@Path("/licenses/{hash}")
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN})
	public Response deleteLicenses(@PathParam("hash") String licenseHash){
		try{
			AppLogger.info("Request to delete license" + licenseHash);
			boolean responsedDelete = StorageAPI.deleteLicense(licenseHash);
			if(responsedDelete!=false){
				return Response.status(204).type(MediaType.APPLICATION_JSON_TYPE).build();
			} else{
				ErrorPayload error= new ErrorPayload(ErrorMessages.DELETE_LICENSE_NOT_FOUND_CODE,ErrorMessages.DELETE_LICENSE_NOT_FOUND_MESSAGE);				
				return Response.status(404).entity(error).build();
			}
		}catch(Exception e){
			AppLogger.error("Error deleting license:" + licenseHash,e);
			return Response.status(500).build();
		}
	}

	@DELETE
	@Path("/licenses")
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN})
	public Response deleteAllLicenses(){
		try{
			AppLogger.info("Request to delete all licenses");
			StorageAPI.deleteAll();

			return Response.status(204).type(MediaType.APPLICATION_JSON_TYPE).build();

		}catch(Exception e){
			AppLogger.error("Error deleting all licenses",e);
			return Response.status(500).build();
		}

	}

	@POST
	@Path("/licenses/debug")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	@AuthorizationPolicy({ Rule.ALLOW_CLOUD_ADMIN})
	public Response debugUploadFile(InputStream io) throws Exception {

		try {
			StorageAPI.writeToFile(io, uploadFileLocation);

			File file = new File(uploadFileLocation);
			LicenseReader reader = new LicenseReader();
			LicenseFileResults newLicense = reader.readFile(file);

			if(newLicense.isEmpty()==true){
				ErrorPayload error= new ErrorPayload(ErrorMessages.NO_LICENSES_CODE,ErrorMessages.NO_LICENSES_MESSAGE);
				return Response.status(400).entity(error).build();


			}else if(newLicense.isAllCorrupted()==true){
				ErrorPayload error= new ErrorPayload(ErrorMessages.ALL_LICENSES_CORRUPTED_CODE,ErrorMessages.ALL_LICENSES_CORRUPTED_MESSAGE);
				return Response.status(400).entity(error).build();

			}else if(newLicense.getNumberCorrupted()!=0){
				Set<License> licenses = newLicense.getLicenses();
				ErrorPayload error= new ErrorPayload(ErrorMessages.SOME_LICENSES_CORRUPTED_CODE,
						ErrorMessages.SOME_LICENSES_CORRUPTED_MESSAGE+newLicense.getFeaturesCorrupted());
				return Response.status(400).entity(error).build();
			}else if (newLicense.getContainsExpired()){
				Set<License> licenses = newLicense.getLicenses();
				ErrorPayload error=new ErrorPayload(ErrorMessages.EVAL_LICENSE_EXPIRED_CODE,
						ErrorMessages.EVAL_LICENSE_EXPIRED_MESSAGE+newLicense.getExpiredNames());
				return Response.status(400).entity(error).build();
			}

			Set<License> licenses = newLicense.getLicenses();

			StorageAPI.saveLicenses(licenses);
			StorageAPI.listProperties();

			try {
				AppLogger.info("Request to retrieve all licenses to be shown");
				JSONObject responseJSON = new JSONObject();
				JSONArray licenseArray = StorageAPI.retrieveAll(false);
				responseJSON.put("licenses", licenseArray);
				return Response.status(200).entity(responseJSON).build();
			} catch (Exception e) {
				AppLogger.error("Error retrieving licenses.", e);
				return Response.status(500).build();
			}

		}catch (ELMException e) {
			AppLogger.error("Error parsing the license file",e);
			ErrorPayload error = new ErrorPayload(ErrorMessages.ALL_LICENSES_CORRUPTED_CODE,
					ErrorMessages.ALL_LICENSES_CORRUPTED_MESSAGE);
			return Response.status(400).entity(error).build();
		}catch (Exception e) {
			AppLogger.error("Error uploading licenses.", e);
			return Response.status(500).build();
		}
	}
	

}
