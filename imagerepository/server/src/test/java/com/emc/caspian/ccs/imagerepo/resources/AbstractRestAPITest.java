package com.emc.caspian.ccs.imagerepo.resources;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.spi.ConnectorProvider;
import org.glassfish.jersey.test.JerseyTest;

import com.emc.caspian.ccs.imagerepo.FrontEndResources;

/**
 * This is a place holder for all the common methods used for testing the REST APIs.
 * @author shrids
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

    protected static final String binData = "Binary Data";
    protected static final String metaData = "MetaData";
    protected static final Entity<String> META_DATA = Entity.entity(metaData, MediaType.APPLICATION_JSON);
    protected static final Entity<String> BIN_DATA = Entity.entity(binData, MediaType.APPLICATION_OCTET_STREAM);

    @Override
    protected void configureClient(ClientConfig config) {
        ConnectorProvider connectorProvider = new ApacheConnectorProvider();
        config.connectorProvider(connectorProvider);
    }

}
