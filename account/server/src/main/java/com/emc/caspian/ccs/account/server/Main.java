/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.server;

import com.emc.caspain.ccs.common.webfilters.KeystoneJerseyAuthFilter;
import com.emc.caspian.ccs.account.authorization.AuthorizationDynamicFilter;
import com.emc.caspian.ccs.account.model.mysql.MySQLProperties;
import com.emc.caspian.ccs.account.util.AppLogger;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.ws.rs.core.Application;

import com.emc.caspian.fabric.config.Configuration;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a minimal rest server which is used to demo the REST APIs. This will be replaced by the existing security /
 * base libraries to start the JETTY server.
 *
 * @author shrids
 *
 */
public final class Main {

  private static final String loggerName = "account";

  // TODO: review the location of this config file, should be read from a standard path
  private static final String accountConfigPath = "conf/account.conf";

  static {
    AppLogger.initialize(loggerName);
    try {
      Configuration.load(accountConfigPath);
    } catch (Exception e) {
      AppLogger.logException(e);
      throw (new RuntimeException("Account service configuration file missing"));
    }
  }

  public static void main(String[] args) throws Exception {

    //Initialize account service server properties
    ServerProperties.initializeServerProperties();
    
    // Set log4j properties file path
    AppLogger.setLoggerConfigPath(ServerProperties.getLog4jPropertiesFilePath());

    AppLogger.info("Initializing Account Service");

    // initialize MySQL configuration
    MySQLProperties.initializeMySQLPropertiesFromConfig();

    // 1 create an application with the desired resources.
    final Application application =
        createApplication(new Class[] {AccountAPI.class});

    // 2 Create frontend
    final Server server = createJettyServer(application);

    try {
      server.start();
    } catch (Exception e) {
      AppLogger.logException(e);
      AppLogger.error("Account service failed to start");
      System.exit(1);
    }

    AppLogger.info("Account Service started");
    // 3 wait for ever.
    server.join();
  }

  /**
   * Create a Jetty server with the associated REST resources.
   *
   * @param application javax.ws.rs.core.Application for the resource.
   * @param port Port Number on which the server will be started
   * @return Server
   * @throws Exception 
   */
  private static final Server createJettyServer(Application application) throws Exception {

    int port = ServerProperties.getPort();
    int requestHeaderSize = ServerProperties.getRequestHeaderSize();

    Server server = new Server();
    // setting the request header size to 64KB
    HttpConfiguration config = new HttpConfiguration();
    config.setRequestHeaderSize(requestHeaderSize);
    ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(config));
    http.setPort(port);
    server.addConnector(http);
    
    if (ServerProperties.isHttpsEnabled()) {
      SslContextFactory sslContext = new SslContextFactory();
      sslContext.setKeyStorePath(ServerProperties.getHttpsKeystorePath());
      sslContext.setKeyStorePassword(ServerProperties.getHttpsKeystorePassword());

      ServerConnector sslServerConnector = new ServerConnector(server, sslContext);
      sslServerConnector.setPort(port);

      server.addConnector(sslServerConnector);

      AppLogger.info("Listening on https port %s", port);
    } else {
      AppLogger.info("Listening on http port %s", port);
    }
    
    ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    
    
    KeystoneProperties keystoneProperties = new KeystoneProperties();
    
    final ResourceConfig resourceConfig = ResourceConfig.forApplication(application);
    resourceConfig.registerClasses(RedirectFilter.class, RequestTracker.class, AuthorizationDynamicFilter.class, ResponseFilter.class);
    resourceConfig.registerInstances(new KeystoneJerseyAuthFilter(KeystoneProperties.getkeystoneUri(), KeystoneProperties.getKeystoneAdmin(),
        keystoneProperties.getKeystoneAdminPassword()));

    ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));

    jerseyServlet.setInitOrder(0);
    context.addServlet(jerseyServlet, "/*");
    
    // Enable access logs
    NCSARequestLog requestLog = new NCSARequestLog();
    requestLog.setLogLatency(true);
    requestLog.setFilename(ServerProperties.getAccessLogPath());
    requestLog.setRetainDays(90);
    requestLog.setAppend(true);
    requestLog.setExtended(true);
    requestLog.setLogCookies(false);
    requestLog.setLogTimeZone("GMT");
    RequestLogHandler requestLogHandler = new RequestLogHandler();
    requestLogHandler.setRequestLog(requestLog);

    context.insertHandler(requestLogHandler);

    server.setHandler(context);
    return server;

  }

  /**
   * Create javax.ws.rs.core.Application based on the input classes.
   *
   * @param classes Class names of the REST resources.
   * @return
   * @throws Exception 
   */
  private static final Application createApplication(Class<?>[] classes) throws Exception {
    // create resources.
    final Set<Object> resources = new HashSet<Object>();
    for (Class<?> resource : classes) {
      try {
        resources.add(resource.newInstance());
      } catch (InstantiationException | IllegalAccessException e) {
        AppLogger.error("Error during instantiation of REST resource : %s", resource.getName());
        AppLogger.logException(e);
        throw e;
      }
    }
        
    return new FrontEndResources(resources);
  }
}

