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

import java.net.ConnectException;
import java.security.Principal;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.server.controller.Protocol.Request;
import com.emc.caspian.ccs.esrs.server.controller.Protocol.Status;
import com.emc.caspian.ccs.esrs.server.exception.PayLoadException;
import com.emc.caspian.ccs.esrs.server.util.Constants;

/**
 * Helper class that provides methods to handle API requests. Helper class with common methods used while processing
 * REST requests
 * <p/>
 * There are only three possible outcomes to any request: a valid response with a response body, an error or a failure.
 * <p/>
 * error: defines behavior when there is an expected error. We typically log the error message and return a valid HTTP
 * status code. All expected error conditions in the business logic should always return a Result with a valid status
 * code.
 * <p/>
 * fail: defines behavior in case of an unexpected condition. Fail generally implies that there is a bug in our code or
 * an irrecoverable and unexpected condition, like a RuntimeException. The status code is always 500/Internal Error.
 * 
 * @author raod4
 *
 */
public final class Controller {
    private static final Logger _log = LoggerFactory.getLogger(Controller.class);
    
    public static final ExceptionToStatus applicationExceptionMapper = new ExceptionHandler();
    /**
     * Process error in business logic. Log info and return an appropriate HTTP status code and response body.
     */
    public static final Response error(final Protocol.Request request,
                                       final Protocol.Response response) {
        final Response.StatusType httpStatus = convert(response.getStatus());
        _log.info("Unsuccessful request: status: {} http status code: {} ",
                response.getStatus(), httpStatus.getStatusCode());

        return Response.serverError().status(httpStatus).entity(httpStatus).build(); //convert to jax-rs error
    }

    /**
     * Process error given the status
     */
    public static Response error(Request request, Status fetchErrorStatus) {
        Protocol.Response response = new Protocol.Response();
        response.setStatus(fetchErrorStatus);
        return error(request, response);
    }


    /**
     * Process failures (exceptions, bug in business logic, etc.). Always returns Internal Error.
     */
    public static final Response fail(final Protocol.Request request, final Principal principal,
                                      final Throwable e) {
        _log.error("Failure in processing request: " + request + " for: " + principal + " with exception : " + e);

        return Response.serverError().build();
    }

    /**
     * Wrapper around handleRequest so that we never through an exception out of this method and always return a valid
     * response or error.
     */
    @SuppressWarnings("unchecked")
    public static final Response handleRequest(final Protocol.Request request, 
            final HandleRequestDelegate handleRequest,
            final TransformDelegate transformDelegate,
            final ExceptionToStatus... mappers ) {
        try {
            final Protocol.Response response = handleRequest.process();
            if (response.getStatus() != Protocol.Status.OK && response.getStatus() != Protocol.Status.CREATED
                    && response.getStatus() != Protocol.Status.BAD_REQUEST
                    && response.getStatus() != Protocol.Status.PRECONDITION_FAILED )
                return error(request, response);

            final ResponseBuilder transform = transformDelegate.transform(response);
            return transform.build();

        } catch (final EtcdAccessException e) {
            //if No Registration or Registration deleted cases shall return 200 and
            // return this message {"RegistrationStatus":"Not Registered"} for any Caspian Proxy APIs.
            String errMsg = e.getMessage();
            if (errMsg.contains(Constants.ESRS_LAST_KEEP_ALIVE_TIME)
                    || errMsg.contains(Constants.ETCD_PRODUCTION_KEY)
                    || errMsg.contains(Constants.ESRS_ENABLED) 
                    || errMsg.contains(Constants.ETCD_PLATFORM_NODE_COUNT)
                    || errMsg.contains(Constants.CASPIAN_PROXY_VE_CONNECTED)) {
                return Response.ok().entity("{\"connected\":\"No\"}").build();
            } else {
                return error(request, fetchStatus(mappers, e));
            }
        } catch (PayLoadException ple) {
            return Response.status(javax.ws.rs.core.Response.Status.BAD_REQUEST).entity(ple.getMessage()).build();
        }
        catch (final Exception e) {
            if (e.getMessage().contains(Constants.SOCKET_TIMEOUT_EXCEPTION) ||
                    e.getMessage().contains(Constants.CONNECT_EXCEPTION)
                    || e.getCause() instanceof ConnectException) {
                return Response.status(javax.ws.rs.core.Response.Status
                        .BAD_REQUEST).entity(Constants.NOT_REACHABLE_ESRS_VE).build();
            } else {
                return error(request, fetchStatus(mappers, e));
            }
        }
    }

    private static Status fetchStatus(ExceptionToStatus[] mappers, Exception e) {
        Status status = null;
        for (ExceptionToStatus mapper : mappers) {
            status = mapper.fetchErrorStatus(e);
            if (status != null)
                break;
        }
        if (status == null) {
            //If none of exceptions match throw a Runtime Exception. This exception will be handled by the Exception Mapper of Jax-rs {@link javax.ws.rs.ext.ExceptionMapper<E>}
            throw new RuntimeException(e);
        }
        return status;
    }
    
    private static final javax.ws.rs.core.Response.Status convert(final Protocol.Status status) {
        switch (status) {
        case OK:
            return Response.Status.OK;

        case ERROR_UNAUTHORIZED:
            return Response.Status.UNAUTHORIZED;

        case ERROR_INTERNAL:
            return Response.Status.INTERNAL_SERVER_ERROR;

        case ERROR_NOT_FOUND:
            return Response.Status.NOT_FOUND;

        case ERROR_BAD_REQUEST:
            return Response.Status.BAD_REQUEST;

        case NOT_IMPLEMENTED:
            return Response.Status.NOT_IMPLEMENTED;

        case PRECONDITION_FAILED:
            return Response.Status.PRECONDITION_FAILED;

        case SERVICE_UNAVAILABLE:
            return Response.Status.SERVICE_UNAVAILABLE;

        default: {
            _log.warn("Internal error: unexpected internal status: " + status);
            return Response.Status.INTERNAL_SERVER_ERROR;
        }
        }
    }
    
}
