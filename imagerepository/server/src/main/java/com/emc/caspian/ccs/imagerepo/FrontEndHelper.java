package com.emc.caspian.ccs.imagerepo;

import java.security.Principal;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.Request;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.Status;
import com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus;
import com.emc.caspian.fabric.net.HttpHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;

/**
 * Helper class with common methods used while processing REST requests
 * <p/>
 * There are only three possible outcomes to any request: a valid response with a response body, an error or a failure.
 * <p/>
 * error: defines behavior when there is an expected error. We typically log the error message and return a valid HTTP
 * status code. All expected error conditions in the business logic should always return a Result with a valid status
 * code.
 * <p/>
 * fail: defines behavior in case of an unexpected condition. Fail generally implies that there is a bug in our code or
 * an irrecoverable and unexpected condition, like a RuntimeException. The status code is always 500/Internal Error.
 */
public final class FrontEndHelper
{
    private static final Logger _log = LoggerFactory.getLogger(FrontEndHelper.class);

    private static final ExceptionToStatus defaultMapper = new DefaultExceptionMapper();

    /**
     * Process error in business logic. Log info and return an appropriate HTTP status code and response body.
     */
    public static final Response error(final Protocol.Request request,
                                       final Principal principal,
                                       final Protocol.Response response) {
        final Response.StatusType httpStatus = convert(response.getStatus());
        _log.info("Unsuccessful request: " + request + " for: " + principal + " status: " + response.getStatus() + " " +
                          "http status code: " + httpStatus
        );

        return Response.serverError().status(httpStatus).build(); //convert to jax-rs error
    }

    /**
     * Process error given the status
     */
    public static Response error(Request request, KeystonePrincipal principal, Status fetchErrorStatus) {
        Protocol.Response response = new Protocol.Response();
        response.setStatus(fetchErrorStatus);
        return error(request, principal, response);
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
    public static final Response handleRequest(final Protocol.Request request, final KeystonePrincipal principal,
                                               final HandleRequestDelegate handleRequest,
                                               final TransformDelegate transformDelegate,
                                               final ExceptionToStatus... mappers ) {
//TODO: enable once Principal is enabled.
//        _log.debug("Principal: " + principal.getName() + ", request id: " + MDC.get(REQUEST_ID) +
//                           ", request: " + request.toString());
        try {
            final Protocol.Response response = handleRequest.process();
       	   //TODO: handle the following block in a better way
            if (response.getStatus() != Protocol.Status.OK && response.getStatus() != Protocol.Status.CREATED
            						&& response.getStatus() != Protocol.Status.NO_CONTENT && response.getStatus() != Protocol.Status.FORBIDDEN
            						&& response.getStatus() != Protocol.Status.BAD_REQUEST && response.getStatus() != Protocol.Status.CONFLICT)
                return FrontEndHelper.error(request, principal, response);
//TODO: enable once Principal is enabled.
//            _log.debug("Principal: " + principal.getName() + ", request id: " + MDC.get(REQUEST_ID) +
//                               ", request: " + request.toString() + ", response: " + response.toString());

            final ResponseBuilder transform = transformDelegate.transform(response);
            return transform.build();

        } catch (final Exception e) {
            //JAX-RS return internal error by default if the exception cannot be handled by exception mappers and JAX-RS
            //Exception mapper.
            return FrontEndHelper.error(request, principal, fetchStatus(mappers, e));
        }
    }

	private static Status fetchStatus(ExceptionToStatus[] mappers, Throwable e) {
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

    private static final Response.StatusType convert(final Protocol.Status status) {
        switch (status) {
            case OK:
                return Response.Status.OK;

            case ERROR_UNAUTHORIZED:
            case INCORRECT_VISIBILITY:
                return Response.Status.FORBIDDEN;

            case ERROR_INTERNAL:
                return Response.Status.INTERNAL_SERVER_ERROR;
                
            case MEMBER_EXISTS:
                return Response.Status.CONFLICT;

            case ERROR_NOT_FOUND:
            case ERROR_NO_IMAGE:
            case ERROR_NO_MEMBER:
                return Response.Status.NOT_FOUND;

            case ERROR_BAD_REQUEST:
                return Response.Status.BAD_REQUEST;

            case NOT_IMPLEMENTED:
                return HttpHelper.ExtendedStatus.NOT_IMPLEMENTED;

            case CONFLICT:
                return Response.Status.CONFLICT;

            default: {
                _log.warn("Internal error: unexpected internal status: " + status);
                return Response.Status.INTERNAL_SERVER_ERROR;
            }
        }
    }

    public static <T> T applyPatch(T bean, JsonPatch patch, Class<T> clazz) {
        // Convert this object to a an aray of bytes
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.valueToTree(bean);
            // Apply the patch
            JsonNode result = patch.apply(node);

            return mapper.readValue(result.toString(), clazz);
        } catch (Exception e) {
        	_log.warn("WebApplicationException");
            throw new WebApplicationException(
                    Response.status(500).type("text/plain").entity(e.getMessage()).build()
            );
        }
    }

    public static JsonPatch getPatchObject(final JsonNode patchObject) {
        try {
            return JsonPatch.fromJson(patchObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the default exception to status mapper.
     * @return
     */
    public static ExceptionToStatus getDefaultExceptionMapper() {
        return defaultMapper;
    }
}
