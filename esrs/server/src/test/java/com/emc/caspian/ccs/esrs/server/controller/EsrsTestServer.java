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

package com.emc.caspian.ccs.esrs.server.controller;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.server.controller.FrontEndResources;
import com.emc.caspian.ccs.esrs.server.impl.EsrsVeMockImpl;

public class EsrsTestServer {

    private static final Logger _log = LoggerFactory.getLogger(EsrsTestServer.class);
    private static final String JETTY_CONFIG_FILE = "conf/esrstestserver.xml";
    
    public static void main() {
	startEsrsMockServer();
    }

    public static void startEsrsMockServer() {
	
	_log.info("Initializing Test ESRS Ve Service ...");

	try {
	    // 1 create an application with the desired resources.
	    final Application application =
		    createApplication(new Class[] {EsrsVeMockImpl.class});

	    // 2 Create frontend
	    final Server server = createJettyServer(application);
	    server.start();
	    
    	    // 3 wait for ever.
	    //server.join();
		
	} catch (Exception e) {
	    _log.error("Test ESRS Ve service failed to start",e);
	    System.exit(1);
	}

	_log.info("Test ESRS Ve Service started.");
    }

    private static final Server createJettyServer(Application application) throws Exception {

	Resource serverConfig = Resource.newResource(JETTY_CONFIG_FILE);
	XmlConfiguration configuration = new XmlConfiguration(serverConfig.getInputStream());
	Server server = (Server) configuration.configure();

	ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
	context.setContextPath("/");

	final ResourceConfig resourceConfig = ResourceConfig.forApplication(application);

	ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));

	jerseyServlet.setInitOrder(0);
	jerseyServlet.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

	context.addServlet(jerseyServlet, "/*");

	server.setHandler(context);
	return server;
    }

    /**
     * Create javax.ws.rs.core.Application based on the input classes.
     * @param classes
     *            Class names of the REST resources.
     * @return
     */
    private static final Application createApplication(Class<?>[] classes) {
	// create resources.
	final Set<Object> resources = new HashSet<Object>();
	for (Class<?> resource : classes) {
	    try {
		resources.add(resource.newInstance());
	    } catch (InstantiationException | IllegalAccessException e) {
		_log.error("Error during instantiation of REST resource : {} ", resource.getName(), e);
	    }
	}
	return new FrontEndResources(resources);
    }
}
