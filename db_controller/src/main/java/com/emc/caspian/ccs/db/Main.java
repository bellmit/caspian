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

package com.emc.caspian.ccs.db;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import com.emc.caspian.ccs.db.util.AppLogger;
import com.emc.caspian.fabric.config.Configuration;

public class Main {

	private static final String loggerName = "db_controller";

	private static final String accountConfigPath = "/opt/caspian/db_controller/conf/controller.conf";

	static {
		AppLogger.initialize(loggerName);
		try {
			Configuration.load(accountConfigPath);
		} catch (Exception e) {
			AppLogger.logException(e);
			throw (new RuntimeException(
					"DB Controller configuration file missing"));
		}

	}

	public static void main(String[] args) throws Exception {

		// Set log4j properties file path
		// AppLogger.setLoggerConfigPath(ExtractProperties.getLog4jPropertiesFilePath());

		AppLogger.info("Initializing DB Controller");

		final Application application = createApplication(new Class[] {
				MySQLAPI.class, ResponseErrorFilter.class, });

		final Server server = createJettyServer(application);
		try {
			server.start();
		} catch (Exception e) {
			AppLogger.logException(e);
			AppLogger.error("DB Controller failed to start");
			System.exit(1);
		}

		AppLogger.info("DB Controller started");
		server.join();
	}

	/**
	 * Create a Jetty server with the associated REST resources.
	 *
	 * @param application
	 *            javax.ws.rs.core.Application for the resource.
	 * @param port
	 *            Port Number on which the server will be started
	 * @return Server
	 * @throws Exception
	 */
	private static Server createJettyServer(Application application) {
		int port = InitializeControllerServerProperties.getPort();

		Server server = new Server();

		if (InitializeControllerServerProperties.isHttpsEnabled()) {
			SslContextFactory sslContext = new SslContextFactory();
			sslContext.setKeyStorePath(InitializeControllerServerProperties
					.getHttpsKeystorePath());
			sslContext.setKeyStorePassword(InitializeControllerServerProperties
					.getHttpsKeystorePassword());
			sslContext.setRenegotiationAllowed(true);
			ServerConnector sslServerConnector = new ServerConnector(server,
					sslContext);

			sslServerConnector.setPort(port);
			AppLogger.info("Listening on http port %s", port);
			server.addConnector(sslServerConnector);
		} else {
			// setting the request header size to 64KB
			HttpConfiguration config = new HttpConfiguration();
			config.setRequestHeaderSize(InitializeControllerServerProperties
					.getRequestHeaderSize());
			ServerConnector http = new ServerConnector(server,
					new HttpConnectionFactory(config));
			http.setPort(port);
			AppLogger.info("Listening on https port %s", port);
			server.addConnector(http);
		}

		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		final ResourceConfig resourceConfig = ResourceConfig
				.forApplication(application);

		ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(
				resourceConfig));

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
	 * @return
	 */
	private static final Application createApplication(Class<?>[] classes) {
		// create resources.
		final Set<Object> resources = new HashSet<Object>();
		for (Class<?> resource : classes) {
			try {
				resources.add(resource.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				AppLogger.error(
						"Error during instantiation of REST resource : %s",
						resource.getName());
				AppLogger.logException(e);
			}
		}
		return new RegisterRestResource(resources);
	}
}
