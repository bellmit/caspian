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

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 * Application to register the REST resource classes
 *
 */
public class FrontEndResources extends Application
{

    public FrontEndResources(final Set<Object> resources)
    {
        super();
        _resource = resources;
    }

    @Override
    public Set<Object> getSingletons() {
        return _resource;
    }

    private final Set<Object> _resource;

}
