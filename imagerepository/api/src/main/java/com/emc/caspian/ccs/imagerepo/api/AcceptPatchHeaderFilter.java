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
package com.emc.caspian.ccs.imagerepo.api;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * A JAX-RS container response filter that applies {@value #ACCEPT_PATCH_HEADER} header
 * to any response to an {@value HttpMethod#OPTIONS} request.
 *
 */
public class AcceptPatchHeaderFilter implements ContainerResponseFilter {

    private static final String ACCEPT_PATCH_HEADER = "Accept-Patch";

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

        if (HttpMethod.OPTIONS.equals(requestContext.getMethod())) {
            final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
            if (!headers.containsKey(ACCEPT_PATCH_HEADER)) {
                headers.putSingle(ACCEPT_PATCH_HEADER, "application/json-patch+json");
            }
        }
    }
}
