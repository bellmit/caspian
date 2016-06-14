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
package com.emc.caspian.ccs.datastore.mysql;

import java.sql.BatchUpdateException;

import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;
import org.skife.jdbi.v2.exceptions.UnableToObtainConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.Status;
import com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus;
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;

/**
 * @author shrids
 *
 */
public final class MysqlExceptionMapper implements ExceptionToStatus {

    private static final Logger _log = LoggerFactory.getLogger(MysqlExceptionMapper.class);

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus#fetchErrorStatus(java.lang.Throwable)
     */
    @Override
    public Status fetchErrorStatus(Throwable exception) {
        _log.error("Exception encountered. Details: ", exception);
        // The exception is wrapped as part of the java.util.concurrent.ExecutionException
        Throwable innerException = exception.getCause();

        Status status = null;

        if (innerException instanceof UnableToExecuteStatementException) {
            if (innerException.getCause() instanceof BatchUpdateException)
                status = Status.ERROR_BAD_REQUEST;
            if (innerException.getCause() instanceof MySQLSyntaxErrorException)
                status = Status.ERROR_INTERNAL;
        } else if (innerException instanceof UnableToObtainConnectionException) {
            status = Status.ERROR_INTERNAL;
        }
        return status;
    }

}
