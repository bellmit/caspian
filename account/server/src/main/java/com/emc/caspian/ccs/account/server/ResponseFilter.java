/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.server;

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

import com.emc.caspian.ccs.account.datacontract.ErrorObject;
import com.emc.caspian.ccs.account.datacontract.ErrorObject.ErrorMessage;
import com.emc.caspian.ccs.common.utils.JsonHelper;

/**
 * Container response filter for handling the error objects for all the resource class responses as well as for 404 Not
 * found APIs/ API paths
 * 
 * 
 * @author raod4
 *
 */
@Provider
public class ResponseFilter implements ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    StatusType httpStatus = responseContext.getStatusInfo();
    int status = httpStatus.getStatusCode();
    responseContext.getHeaders().putSingle("Server", "account");
    
    //Validate idp returns an object in response for http-400 instead of default error case.
    final String ValidateIdpDetails = "com.emc.caspian.ccs.account.datacontract.ValidateIdpDetails";
    Object obj = responseContext.getEntity();
    if(obj != null){
      String str = obj.getClass().getName();
      if(str.equals(ValidateIdpDetails)){
        return;
      }
    }    
    if (status >= 400) {
      ErrorMessage error = new ErrorObject().new ErrorMessage();
      ErrorObject err = new ErrorObject();
      if (responseContext.getEntity() != null) {
        error.setMessage(responseContext.getEntity().toString());
      } else {
        error.setMessage("The request encountered an error");
      }
      error.setTitle(httpStatus.getReasonPhrase());
      error.setCode(httpStatus.getStatusCode());
      
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
