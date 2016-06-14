
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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.test.JerseyTest;

import com.emc.caspian.ccs.esrs.server.controller.FrontEndResources;


/**
 * This is a place holder for all the common methods used for testing the REST APIs.
 * @author kuppup
 *
 */
public abstract class AbstractRestAPITest extends JerseyTest {

    /**
     * Create javax.ws.rs.core.Application based on the input classes.
     *
     * @param classes
     *            Class names of the REST resources.
     * @return
     */
    protected static final Application createApplication(Class<?>[] classes) {
	// create resources.
	final Set<Object> resources = new HashSet<Object>();
	for (Class<?> resource : classes) {
	    try {
		resources.add(resource.newInstance());
	    } catch (InstantiationException | IllegalAccessException e) {
		System.out.println("Error during instantiation of REST resource ");
	    }
	}
	return new FrontEndResources(resources);
    }

    @Override
    protected void configureClient(ClientConfig config) {
	ConnectorProvider connectorProvider = new ApacheConnectorProvider();
	config.connectorProvider(connectorProvider);
    }
}