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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

import com.emc.caspain.ccs.common.webfilters.KeystoneJerseyAuthFilter;
import com.emc.caspian.ccs.esrs.server.controller.RequestTracker;
import com.emc.caspian.ccs.esrs.server.helper.PropCollectorUtil;
import com.emc.caspian.ccs.esrs.api.AuthorizationDynamicFilter;
import com.emc.caspian.ccs.esrs.api.ResponseErrorFilter;
import com.emc.caspian.ccs.esrs.server.impl.CaspianDetailsCollector;
import com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl;
import com.emc.caspian.ccs.esrs.server.scheduler.NodeStatusScheduler;
import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.ESRSUtil;
import com.emc.caspian.ccs.esrs.server.util.EsrsKeepAliveEngine;
import com.emc.caspian.ccs.esrs.server.util.EsrsConfiguration;
import com.emc.caspian.ccs.esrs.server.util.EtcdUtils;
import com.emc.caspian.fabric.config.Configuration;


public class Main {

    private static final Logger _log = LoggerFactory.getLogger(Main.class);
    

    static {
	try {
	    Configuration.load(Constants.ESRS_CONFIG_FILE);
	} catch (Exception e) {
	    _log.error("ESRS configuration file missing. "
	    	+ "Will use default values for scheduling keepAlive and prop collect.");
	    
	    throw new RuntimeException("ESRS configuration file missing.");
	}
    }

    public static void main(String[] args) throws Exception {

        _log.info("Initializing ESRS Proxy Service ...");

        // 1 create an application with the desired resources.
        final Application application = createApplication(new Class[] { ESRSProxyImpl.class });

        // 2 Create frontend
        final Server server = createJettyServer(application);

        // 3 start the scheduler for the keep alive engine scheduler.
        // Get the scheduler
        ScheduledExecutorService scheduler = Executors
                .newSingleThreadScheduledExecutor();

        // Get a handle, starting now, with a time delay
        final ScheduledFuture<?> keepAliveTimeHandle = scheduler
                .scheduleAtFixedRate(new EsrsKeepAliveEngine(),
                        EsrsConfiguration.getKeepAliveStartTime(),
                        EsrsConfiguration.getKeepAliveInterval(),
                        TimeUnit.MINUTES);

        //start the property collector scheduler. 
        PropCollectorUtil.startPropCollScheduler();

        final ScheduledFuture<?> nodeStatusSchHandle = scheduler
                .scheduleAtFixedRate(new NodeStatusScheduler(),
                        EsrsConfiguration.getNodeStatusStartTime(),
                        EsrsConfiguration.getNodeStatusInterval(),
                        TimeUnit.MINUTES);

        try {
            server.start();
        } catch (Exception e) {
            _log.error("ESRS Proxy service failed to start", e);
            System.exit(1);
        }

        _log.info("ESRS Proxy Service started.");
        // 4 wait for ever.
        server.join();
    }

    /**
     * Create a Jetty server with the associated REST resources.
     *
     * @param application
     *            javax.ws.rs.core.Application for the resource.
     * @param port
     *            Port Number on which the server will be started
     *
     * @return Server
     */
    private static final Server createJettyServer(Application application) throws Exception {

        Resource serverConfig = Resource.newResource(Constants.JETTY_CONFIG_FILE);
        XmlConfiguration configuration = new XmlConfiguration(serverConfig.getInputStream());
        Server server = (Server) configuration.configure();
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        String authURI;

        try {
            authURI = ESRSUtil.getEndPointFromCRS(Constants.PLATFORM, Constants.KEYSTONE);
        } catch (Exception e) {
            _log.error("Unable to get keystone AUTHURI from CRS", e);
            throw new Exception("Unable to get keystone AUTHURI from CRS");
        }

        char[] ksPwd = null;
        try {
            ksPwd = ESRSUtil.getKeystonePassword();
        } catch (Exception e) {
            throw e;
        }

        final ResourceConfig resourceConfig = ResourceConfig.forApplication(application);
        resourceConfig.registerClasses(RequestTracker.class, AuthorizationDynamicFilter.class, ResponseErrorFilter.class);
        resourceConfig.registerInstances(new KeystoneJerseyAuthFilter(authURI,
                Constants.CONFIG_KEYSTONE_USERNAME_DEFAULT, String.valueOf(ksPwd)));

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
