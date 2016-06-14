package com.emc.caspian.ccs.imagerepo.resources;

import static org.junit.Assert.assertEquals;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import com.emc.caspian.ccs.imagerepo.api.ApiV1;
import com.google.common.io.CharStreams;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Test V2 Glance Schema APIs
 * @author shrids
 *
 */
public class FrontEndGlanceV2SchemaTest extends AbstractRestAPITest {

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Schema#images(javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testImages() {
        Response response = target("/v2/schemas/images").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Schema#image(javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testImage() {
        Response response = target("/v2/schemas/image").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Schema#members(javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testMembers() {
        Response response = target("/v2/schemas/members").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Schema#member(javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testMember() {
        Response response = target("/v2/schemas/member").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Schema#tasks(javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testTasks() {
        Response response = target("/v2/schemas/tasks").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Schema#task(javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testTask() {
        Response response = target("/v2/schemas/task").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Schema#entity(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testEntity() {
        Response response = target("/v2/schemas/metadefs/entity").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Override
    protected Application configure() {
        ResourceConfig cfg = ResourceConfig.forApplication(createApplication(new Class[] { FrontEndGlanceV2Schema.class }));
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
