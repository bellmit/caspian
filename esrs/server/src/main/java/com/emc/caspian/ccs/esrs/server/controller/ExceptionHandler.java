/**
 *  Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.esrs.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.server.controller.Protocol.Status;

/**
 * Application exception handler which is responsible to mapping all the exceptions
 * 
 * @author shivat
 *
 */
public class ExceptionHandler implements ExceptionToStatus {

    private static final Logger _log = LoggerFactory.getLogger(ExceptionHandler.class);

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus#fetchErrorStatus(java.lang.Throwable)
     */
    @Override
    public Status fetchErrorStatus(Exception exception) {
        _log.error("Exception encountered. Details: ", exception);
        // The exception is wrapped as part of the java.util.concurrent.ExecutionException
        Throwable innerException = exception.getCause();

        Status status = null;

        if ((exception instanceof IllegalArgumentException) || (innerException instanceof IllegalArgumentException)) {
            status = Status.ERROR_BAD_REQUEST;

        }  else if ((exception instanceof EtcdAccessException) || (innerException instanceof EtcdAccessException )){
            status = Status.ERROR_UNAUTHORIZED;
        } else {
            status = Status.ERROR_INTERNAL;
        }
        return status;
    }

}