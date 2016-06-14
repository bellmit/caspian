package com.emc.caspian.ccs.imagerepo.resources;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test Glance Catalog APIs
 * @author shrids
 *
 */
public class FrontEndGlanceV2CatalogTest extends AbstractRestAPITest {

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#fetchResourceNamespaces(java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testFetchResourceNamespaces() {
        Response response = target("/v2/metadefs/entity").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#createNamespace(java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testCreateNamespace() {
        Response response = target("/v2/metadefs/namespaces").request().post(META_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#namespace(java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testNamespace() {
        Response response = target("/v2/metadefs/namespaces/namespace1").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#updateNamespace(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testUpdateNamespace() {
        Response response = target("/v2/metadefs/namespaces/namespace1").request().put(META_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#deleteNamespace(java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testDeleteNamespace() {
        Response response = target("/v2/metadefs/namespaces/namespace1").request().delete();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#entities(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testEntities() {
        Response response = target("/v2/metadefs/namespaces/namespace1/entitytype").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#createEntity(java.lang.String, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testCreateEntity() {
        Response response = target("/v2/metadefs/namespaces/namespace1/entitytype").request().post(META_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#entity(java.lang.String, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testEntity() {
        Response response = target("/v2/metadefs/namespaces/namespace1/entityType/entity1").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#deleteEntity(java.lang.String, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testDeleteEntity() {
        Response response = target("/v2/metadefs/namespaces/namespace1/entityType/entity1").request().delete();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Catalog#modifyEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}.
     */
    @Test
    public final void testModifyEntity() {
        Response response = target("/v2/metadefs/namespaces/namespace1/entityType/entity1").request().put(META_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Override
    protected Application configure() {
        ResourceConfig cfg = ResourceConfig.forApplication(createApplication(new Class[] { FrontEndGlanceV2Catalog.class }));
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        cfg.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(request).to(HttpServletRequest.class);
            }
        });
        return cfg;
    }
}
