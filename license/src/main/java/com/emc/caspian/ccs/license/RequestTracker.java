package com.emc.caspian.ccs.license;

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.MDC;

/**
 * Request filter to create a MDC object that will be used for generating random Request ID used for logging for all the
 * incoming requests
 * 
 * 
 * @author raod4
 *
 */
@Provider
@PreMatching
public class RequestTracker implements ContainerRequestFilter {

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// TODO: set a smaller length request ID
		String hex = UUID.randomUUID().toString().replaceAll("-", "");
		byte[] hexAsBytes = DatatypeConverter.parseHexBinary(hex);
		String request_id = DatatypeConverter.printHexBinary(hexAsBytes);
		MDC.put("REQUEST_ID", request_id);
	}
}

