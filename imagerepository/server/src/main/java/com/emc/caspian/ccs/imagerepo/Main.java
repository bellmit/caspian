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
package com.emc.caspian.ccs.imagerepo;

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

import com.emc.caspain.ccs.common.webfilters.KeystoneAuthFilter;
import com.emc.caspian.ccs.common.policyengine.rest.PolicyDynamicFeature;
import com.emc.caspian.ccs.imagerepo.api.AcceptPatchHeaderFilter;
import com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker;
import com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV1Image;
import com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2;
import com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog;
import com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Schema;
import com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Tasks;
import com.emc.caspian.fabric.config.Configuration;

/**
 * This is a minimal rest server which is used to demo the REST APIs. This will be replaced by the
 * existing security / base libraries to start the JETTY server.
 *
 * @author shrids
 */
public final class Main {
	private static final Logger _log = LoggerFactory.getLogger(Main.class);
    static {
        try {
            Configuration.load("conf/registry.conf");
        } catch (Exception e) {
        	_log.error("unable to load conf file");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        _log.info("Initializing Image Repository Service");

        // 1 create an application with the desired resources.
        final Application application = createApplication(new Class[] { FrontEndDocker.class, FrontEndGlanceV1Image.class,
                FrontEndGlanceV2Tasks.class, FrontEndGlanceV2.class, FrontEndGlanceV2Catalog.class, FrontEndGlanceV2Schema.class });

        // 2 Create frontend
        final Server server = createJettyServer(application);
        server.start();

        _log.info("Image Repository Service has started");
        // 3 wait for ever.
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

        Resource serverConfig = Resource.newResource(JETTY_CONFIG_FILE);
        XmlConfiguration configuration = new XmlConfiguration(serverConfig.getInputStream());
        Server server = (Server) configuration.configure();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        final ResourceConfig resourceConfig = ResourceConfig.forApplication(application);
        resourceConfig.registerClasses(providerClasses);

        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));
        
		jerseyServlet.setInitOrder(0);
        context.addServlet(jerseyServlet, "/*");

        server.setHandler(context);
        return server;

    }

    /**
     * Create javax.ws.rs.core.Application based on the input classes.
     *
     * @param classes
     *            Class names of the REST resources.
     *
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

    private static final Class<?> providerClasses[] = { KeystoneAuthFilter.class, AcceptPatchHeaderFilter.class,
            PolicyDynamicFeature.class };
    
    private static final String SECTION = "image.server";
    private static final String JETTY_CONFIG_FILE = Configuration.make(String.class, SECTION + ".jettyConfigurationFile",
            "conf/imageserver.xml").value();
}
