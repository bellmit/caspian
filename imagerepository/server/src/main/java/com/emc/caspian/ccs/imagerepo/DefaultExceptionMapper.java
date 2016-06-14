/**
 *  Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.imagerepo;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.Status;
import com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus;
import com.emc.caspian.ccs.registry.DuplicateEntityException;

/**
 * Default Exception mapper which is used to handle generic exceptions which indicate a
 * non-500("Internal Error") .
 *
 * @author shrids
 *
 */
public class DefaultExceptionMapper implements ExceptionToStatus {

    private static final Logger _log = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus#fetchErrorStatus(java.lang.Throwable)
     */
    @Override
    public Status fetchErrorStatus(Throwable exception) {
        _log.error("Exception encountered. Details: ", exception);
        // The exception is wrapped as part of the java.util.concurrent.ExecutionException
        Throwable innerException = exception.getCause();

        Status status = null;

        if ((exception instanceof IllegalArgumentException) || (innerException instanceof IllegalArgumentException)) {
            status = Status.ERROR_BAD_REQUEST;

        } else if ((exception instanceof FileNotFoundException)) {
            status = Status.ERROR_NOT_FOUND;
        } else if (innerException instanceof DuplicateEntityException) {
            status = Status.CONFLICT;
        }
        else {
            status = Status.ERROR_INTERNAL;
        }
        return status;
    }

}
