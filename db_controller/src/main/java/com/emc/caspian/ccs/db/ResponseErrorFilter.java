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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response.StatusType;
import javax.ws.rs.ext.Provider;

import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.db.ErrorObject.ErrorMessage;

/**
 * Container response filter for handling the error objects for all the resource
 * class responses as well as for 404 (Not found) APIs/ API paths
 */
@Provider
public class ResponseErrorFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext,
			ContainerResponseContext responseContext) throws IOException {
		StatusType httpStatus = responseContext.getStatusInfo();
		int status = httpStatus.getStatusCode();

		final Date date = new Date();
		final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		final SimpleDateFormat format = new SimpleDateFormat(ISO_FORMAT);
		final TimeZone utc = TimeZone.getTimeZone("UTC");

		if (status >= 400) {
			ErrorMessage error = new ErrorObject().new ErrorMessage();
			ErrorObject err = new ErrorObject();

			if (((String) responseContext.getEntity()) == null)
				error.setMessage(httpStatus.getReasonPhrase());
			else
				error.setMessage((String) responseContext.getEntity());

			error.setCode(httpStatus.getStatusCode());
			format.setTimeZone(utc);
			error.setTimestamp(format.format(date));
			err.setError(error);
			
			String json = JsonHelper.serializeToJson(err);
			responseContext.setEntity(json);
		}
	}
}
