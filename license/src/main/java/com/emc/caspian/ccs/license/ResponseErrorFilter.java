package com.emc.caspian.ccs.license;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.Provider;

import org.slf4j.MDC;

import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.license.ErrorObject.ErrorMessage;

/**
 * Container response filter for handling the error objects for all the resource class responses as well as for 404 Not
 * found APIs/ API paths
 */
@Provider
public class ResponseErrorFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		StatusType httpStatus = responseContext.getStatusInfo();
		int status = httpStatus.getStatusCode();
		if (status >= 400) {
			ErrorMessage error = new ErrorObject().new ErrorMessage();
			ErrorObject err = new ErrorObject();
			if (responseContext.getEntity() != null) {
				if(responseContext.getEntity().toString()=="Invalid auth token"){
					error.setMessage(AuthorizationErrorMessage.AUTH_INVALID_TOKEN_MESSAGE);
					error.setCode(AuthorizationErrorMessage.AUTH_INVALID_TOKEN_CODE);
				}else{

					Class entityClass = responseContext.getEntityClass();
					ErrorPayload ep;
					if(entityClass.equals(String.class)){
						ep = new ErrorPayload();
						ep.setErrorMessage(responseContext.getEntity().toString());
						ep.setErrorCode(0);
					}else{
						ep = (ErrorPayload) responseContext.getEntity();
					}
					error.setMessage(ep.getErrorMessage());
					error.setCode(ep.getErrorCode());
				}
			} else {
				error.setMessage("The request encountered an error");
				error.setCode(httpStatus.getStatusCode());     
			}


			final Date date = new Date();
			final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
			final SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
			final TimeZone utc = TimeZone.getTimeZone("UTC");
			sdf.setTimeZone(utc);
			error.setTimestamp(sdf.format(date));
			error.setRequestId(MDC.get("REQUEST_ID"));
			err.setError(error);
			String json = JsonHelper.serializeToJson(err);
			responseContext.setEntity(json);
		}

	} 
}
