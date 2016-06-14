package com.emc.caspian.ccs.license;

public class ErrorMessages {
	 public static final int NO_LICENSES_CODE = 3210;
	 public static final String NO_LICENSES_MESSAGE = "No licenses in payload";
	 
	 public static final int SOME_LICENSES_CORRUPTED_CODE = 3211;
	 public static final String SOME_LICENSES_CORRUPTED_MESSAGE = "License file rejected because licenses for these features are corrupted:";
	 
	 public static final int ALL_LICENSES_CORRUPTED_CODE = 3212;
	 public static final String ALL_LICENSES_CORRUPTED_MESSAGE = "All licenses are corrupted or error parsing license file";

	 public static final int GET_LICENSE_NOT_FOUND_CODE = 3220;
	 public static final String GET_LICENSE_NOT_FOUND_MESSAGE = "The requested licenses were not found";
	 
	 public static final int GET_LICENSE_INVALID_QUERY_CODE = 3221;
	 public static final String GET_LICENSE_INVALID_QUERY_MESSAGE = "The query parameters are not valid";
	 
	 public static final int EVAL_LICENSE_EXPIRED_CODE = 3222;
	 public static final String EVAL_LICENSE_EXPIRED_MESSAGE = "License file rejected because licenses for these features are expired:";

	 public static final int DELETE_LICENSE_NOT_FOUND_CODE = 3230;
	 public static final String DELETE_LICENSE_NOT_FOUND_MESSAGE = "The license marked for deletion was not found";
	 
	 public static final int SCALEIO_URL_CRS_ERROR_CODE = 3240;
	 public static final String SCALEIO_URL_CRS_ERROR_MESSAGE="Unable to get block storage gateway's base URI from CRS";
	 
	 public static final int SCALEIO_URL_CRS_EMPTY_CODE = 3241;
	 public static final String SCALEIO_URL_CRS_EMPTY_MESSAGE="Block storage gateway's base URI from CRS is empty";
	 
	 public static final int SCALEIO_SYSTEMID_CALL_ERROR_CODE = 3242;
	 public static final String SCALEIO_SYSTEMID_CALL_ERROR_MESSAGE ="Error making call to get System ID details from block storage";
	 
	 public static final int SCALEIO_SYSTEMID_EXTRACT_ERROR_CODE = 3243;
	 public static final String SCALEIO_SYSTEMID_EXTRACT_ERROR_MESSAGE ="Error parsing System ID details from block storage api call response";
	 
	 public static final int SCALEIO_LICENSE_ERROR_CODE = 3244;
	 public static final String SCALEIO_LICENSE_ERROR_MESSAGE = "Block storage license file is invalid or does not match this version";
	 
	 public static final int SCALEIO_POSTING_ERROR_CODE = 3245;
	 public static final String SCALEIO_POSTING_ERROR_MESSAGE = "Error while posting block storage license file";

}
