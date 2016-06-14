/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.account.server;

import com.emc.caspian.ccs.account.controller.AccountProtocol;
import com.emc.caspian.ccs.account.controller.AccountProtocol.Request;
import com.emc.caspian.ccs.account.controller.AccountProtocol.Status;
import com.emc.caspian.ccs.account.controller.ResponseErrorMessage;
import com.emc.caspian.ccs.account.util.AppLogger;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.security.Principal;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

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
public final class AccountServiceFrontEndHelper {
  
  /**
   * Process error in business logic. Log info and return an appropriate HTTP status code and response body.
   */
  public static final Response error(final AccountProtocol.Request request, final AccountProtocol.Response response) {
    final Response.Status httpStatus = convert(response.getStatus());
    AppLogger.warn("Unsuccessful request: " + request + " status: " + response.getStatus() + " " + "http status code: "
        + httpStatus);
    return Response.serverError().entity(response.getRespMsg()).status(httpStatus).build();

  }
  
  public static final Response errorWithResponseObject(final AccountProtocol.Request request, final AccountProtocol.Response response, final TransformResponse transformResponse) {
    final Response.Status httpStatus = convert(response.getStatus());
    AppLogger.warn("Unsuccessful request: " + request + " status: " + response.getStatus() + " " + "http status code: "
        + httpStatus);
    Object resp = null;
    try {
       resp = transformResponse.transform(response);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return Response.serverError().entity(resp).status(convert(response.getStatus())).build();
  }

  /**
   * Process error given the status
   */
  public static Response error(Request request, Status fetchErrorStatus) {
    AccountProtocol.Response response = new AccountProtocol.Response();
    response.setStatus(fetchErrorStatus);
    return error(request, response);
  }

  /**
   * Process failures (exceptions, bug in business logic, etc.). Always returns Internal Error.
   */
  public static final Response fail(final AccountProtocol.Request request, final Principal principal, final Throwable e) {
    AppLogger.warn("Failure in processing request: " + request + " for: " + principal + " with exception : " + e);

    return Response.serverError().build();
  }

  /**
   * Wrapper around handleRequest so that we never through an exception out of this method and always return a valid
   * response or error.
   */
  @SuppressWarnings("unchecked")
  public static final Response handleRequest(final AccountProtocol.Request request, final HandleRequest handleRequest,
      final TransformResponse transformResponse) {

    AccountProtocol.Response response = null;
    try {
      response = handleRequest.processRequest();
      // TODO: check for null responses

      if (response.getStatus() == AccountProtocol.Status.ERROR_BAD_REQUEST_WITH_RESPONSE) {
        return AccountServiceFrontEndHelper.errorWithResponseObject(request, response, transformResponse);
      }

      if (response.getStatus() != AccountProtocol.Status.SUCCESS_OK
          && response.getStatus() != AccountProtocol.Status.SUCCESS_CREATED
          && response.getStatus() != AccountProtocol.Status.SUCCESS_NO_CONTENT
          && response.getStatus() != AccountProtocol.Status.SUCCESS_ACCEPTED) {
        return AccountServiceFrontEndHelper.error(request, response);
      }
      if (response.getStatus() == AccountProtocol.Status.SUCCESS_NO_CONTENT) {
        return Response.noContent().build();

      } else if (response.getStatus() == AccountProtocol.Status.SUCCESS_ACCEPTED
          || response.getStatus() == AccountProtocol.Status.SUCCESS_CREATED
          || response.getStatus() == AccountProtocol.Status.SUCCESS_OK) {
        if (response.getResponseHeaders() != null) {

          Object entity = (transformResponse != null) ? transformResponse.transform(response) : null;

          ResponseBuilder interResp = Response.accepted(entity);
          for (Map.Entry<String, String> entry : response.getResponseHeaders().entrySet()) {
            interResp.header(entry.getKey(), entry.getValue());
          }
          return interResp.status(convert(response.getStatus())).build();

        } else {
          return Response.ok(transformResponse.transform(response)).status(convert(response.getStatus())).build();
        }
      } else {
        return Response.ok(transformResponse.transform(response)).status(convert(response.getStatus())).build();
      }
    } catch (IllegalArgumentException e) {
      response = new AccountProtocol.Response();
      // this block catches any JSON parsing exceptions
      if (e.getCause() instanceof JsonProcessingException) {
        response.setStatus(AccountProtocol.Status.ERROR_BAD_REQUEST);
        response.setRespMsg(ResponseErrorMessage.INVALID_JSON);
      } else {
        response.setStatus(AccountProtocol.Status.ERROR_BAD_REQUEST);
        response.setRespMsg(e.getMessage());
      }
      return AccountServiceFrontEndHelper.error(request, response);
    } catch (final Exception e) {
      response = new AccountProtocol.Response();
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.error(e.getMessage());
      return AccountServiceFrontEndHelper.error(request, response);
    }
  }

  private static final javax.ws.rs.core.Response.Status convert(final AccountProtocol.Status status) {
    switch (status) {
      case SUCCESS_OK:
        return Response.Status.OK;

      case SUCCESS_CREATED:
        return Response.Status.CREATED;

      case SUCCESS_NO_CONTENT:
        return Response.Status.NO_CONTENT;

      case ERROR_UNAUTHORIZED:
        return Response.Status.FORBIDDEN;

      case ERROR_INTERNAL:
        return Response.Status.INTERNAL_SERVER_ERROR;

      case ERROR_CONFLICT:
        return Response.Status.CONFLICT;

      case ERROR_NOT_FOUND:
        return Response.Status.NOT_FOUND;

      case ERROR_BAD_REQUEST:
        return Response.Status.BAD_REQUEST;
      
      case ERROR_BAD_REQUEST_WITH_RESPONSE:
    	return Response.Status.BAD_REQUEST;

      case PRECONDITION_FAILED:
        return Response.Status.PRECONDITION_FAILED;

      case NOT_IMPLEMENTED:
        return Response.Status.NOT_IMPLEMENTED;
      case SUCCESS_ACCEPTED:
        return Response.Status.ACCEPTED;

      default: {
        AppLogger.warn("Internal error: unexpected internal status: " + status);
        return Response.Status.INTERNAL_SERVER_ERROR;
      }
    }
  }
}
