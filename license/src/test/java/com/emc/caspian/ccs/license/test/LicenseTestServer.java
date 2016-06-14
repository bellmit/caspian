package com.emc.caspian.ccs.license.test;

import java.io.File;
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

import com.emc.caspian.ccs.license.Apiv1;
import com.emc.caspian.ccs.license.AuthorizationDynamicFilter;
import com.emc.caspian.ccs.license.ExtractProperties;
import com.emc.caspian.ccs.license.FrontEndResources;
import com.emc.caspian.ccs.license.RequestTracker;
import com.emc.caspian.ccs.license.ResponseErrorFilter;
import com.emc.caspian.ccs.license.util.AppLogger;
import com.emc.caspian.fabric.config.Configuration;

public class LicenseTestServer {
	private static final String loggerName = "license";

	private static final String accountConfigPath = "./conf/license.conf";

	static {
		AppLogger.initialize(loggerName);

		File folder = new File("./data/raw");
		try
		{
			folder.mkdirs();
		}catch(Exception e){
			AppLogger.logException(e);
			throw(new RuntimeException("Error in creating local storage folders"));
		}		

		try {
			Configuration.load(accountConfigPath);
		} catch (Exception e) {
			AppLogger.logException(e);
			throw (new RuntimeException(
					"License service configuration file missing"));
		}	

	}

	public static void main() throws Exception {

		AppLogger.info("Initializing license Service");

		final Application application = createApplication(new Class[] {
				Apiv1.class });

		final Server server = createJettyServer(application);
		try {
			server.start();
		} catch (Exception e) {
			AppLogger.logException(e);
			AppLogger.error("License service failed to start");
			System.exit(1);
		}

		AppLogger.info("License Service started");
//		server.join();
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
		int port = ExtractProperties.getPort();

		Server server = new Server();

		if (ExtractProperties.isHttpsEnabled()) {
			SslContextFactory sslContext = new SslContextFactory();
			sslContext
			.setKeyStorePath(ExtractProperties.getHttpsKeystorePath());
			sslContext.setKeyStorePassword(ExtractProperties
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
			config.setRequestHeaderSize(ExtractProperties
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
		resourceConfig.registerClasses(ResponseErrorFilter.class);

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
		return new FrontEndResources(resources);
	}
	
}
