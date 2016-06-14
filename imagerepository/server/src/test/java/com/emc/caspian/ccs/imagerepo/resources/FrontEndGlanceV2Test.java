package com.emc.caspian.ccs.imagerepo.resources;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.Handle;

import com.emc.caspian.fabric.config.Configuration;
import com.emc.caspian.ccs.datastore.DataStoreFactory;
import com.emc.caspian.ccs.datastore.DataStoreType;
import com.emc.caspian.ccs.datastore.PropertyBag;
import com.emc.caspian.ccs.datastore.mysql.MySqlStore;
import com.emc.caspian.ccs.imagerepo.api.AcceptPatchHeaderFilter;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image.Visibility;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Member;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image.ContainerFormat;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image.DiskFormat;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Member.Status;
import com.emc.caspian.ccs.imagerepo.model.Image.Container_format;
import com.emc.caspian.ccs.imagerepo.model.Image.Disk_format;
import com.emc.caspian.ccs.imagerepo.model.Images;

public class FrontEndGlanceV2Test extends AbstractRestAPITest {

    static {
        try {
            Configuration.load("src/test/resources/registry.conf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static MySqlStore mysqlStore = (MySqlStore) DataStoreFactory.getImageStore(DataStoreType.MYSQL);

    private com.emc.caspian.ccs.imagerepo.model.Image imageGlanceV2;

    @BeforeClass
    public static void setUpBefore() throws Exception {
        Image image_4444 = new Image("4444");
        image_4444.setEntityType(EntityType.IMAGE);
        image_4444.setArchitecture("X64");
        image_4444.setChecksum("checksume");
        image_4444.setCreatedAt("2/26/2015:9:20");
        image_4444.setDirectUrl("ftp://testFtpServer/image_4444");
        image_4444.setDiskFormat(DiskFormat.ISO);
        image_4444.setContainerFormat(ContainerFormat.BARE);
        image_4444.setSize(2048);
        List l = new ArrayList();
        l.add("hello");
        l.add("world");
        image_4444.setTags(l);
        image_4444.setProtected(false);
        
        Image image_5555 = new Image("5555");
        image_5555.setEntityType(EntityType.IMAGE);
        image_5555.setArchitecture("X64");
        image_5555.setChecksum("checksume");
        image_5555.setCreatedAt("2/26/2015:9:20");
        image_5555.setDirectUrl("ftp://testFtpServer/image_5555");
        image_5555.setDiskFormat(DiskFormat.ISO);
        image_5555.setContainerFormat(ContainerFormat.BARE);
        image_5555.setSize(2048);
        image_5555.setProtected(true);
        
    
        if(mysqlStore.get(EntityType.IMAGE, "4444") !=null) {
            mysqlStore.delete(EntityType.IMAGE, "4444");
        }
        
        mysqlStore.insert(new PropertyBag<Image>(image_4444));
        mysqlStore.insert(new PropertyBag<Image>(image_5555));
    }

    @Before
    public void setup() {
        imageGlanceV2 = new com.emc.caspian.ccs.imagerepo.model.Image();
        imageGlanceV2.setId("5555");
        imageGlanceV2.setArchitecture("X64");
        imageGlanceV2.setChecksum("checksume");
        imageGlanceV2.setCreated_at("2/26/2015:9:20");
        imageGlanceV2.setDirect_url("ftp://testFtpServer/image_5555");
        imageGlanceV2.setDisk_format(Disk_format.ISO);
        imageGlanceV2.setContainer_format(Container_format.BARE);
        imageGlanceV2.setSize(2048);
        imageGlanceV2.setProtected(true);
    }
    @Before
    public void memberTestSetup()
    {
        Image ubuntu = new Image("ubuntu");
        ubuntu.setEntityType(EntityType.IMAGE);
        ubuntu.setVisibility(Visibility.PUBLIC);;
        if(mysqlStore.get(EntityType.IMAGE, "ubuntu") != null) 
            mysqlStore.delete(EntityType.IMAGE, "ubuntu");
        mysqlStore.insert(new PropertyBag<Image>(ubuntu));        
        
        Image centos = new Image("centos");
        centos.setEntityType(EntityType.IMAGE);
        centos.setVisibility(Visibility.PRIVATE);;
        if(mysqlStore.get(EntityType.IMAGE, "centos") != null) 
            mysqlStore.delete(EntityType.IMAGE, "centos");
        mysqlStore.insert(new PropertyBag<Image>(centos));
        
        Member member1 = new Member("centos_poovanna");
        member1.setEntityType(EntityType.MEMBER);
        member1.setImageId("centos");
        member1.setMemberId("poovanna");
        member1.setStatus(Status.PENDING);
        if(mysqlStore.get(EntityType.MEMBER, "centos_poovanna") != null) 
            mysqlStore.delete(EntityType.MEMBER, "centos_poovanna");        
        mysqlStore.insert(new PropertyBag<Member>(member1));
        
        Member member2 = new Member("centos_rama");
        member2.setEntityType(EntityType.MEMBER);
        member2.setImageId("centos");
        member2.setMemberId("rama");
        member2.setStatus(Status.PENDING);
        if(mysqlStore.get(EntityType.MEMBER, "centos_rama") != null) 
            mysqlStore.delete(EntityType.MEMBER, "centos_rama");        
        mysqlStore.insert(new PropertyBag<Member>(member2));
        
       
        Member member3 = new Member("centos_leela");
        member3.setEntityType(EntityType.MEMBER);
        member3.setImageId("centos");
        member3.setMemberId("leela");
        member3.setStatus(Status.PENDING);
        if(mysqlStore.get(EntityType.MEMBER, "centos_leela") != null) 
            mysqlStore.delete(EntityType.MEMBER, "centos_leela");        
        mysqlStore.insert(new PropertyBag<Member>(member3));
        
        /**
                    Images                                Members
         --------------------------------    -------------------------------
        |ImageId      | Visibility       |  | ImageId        | MemberId     |
        |--------------------------------|  |-------------------------------|
        |1. ubuntu    | public           |  |1. centos       | poovanna     |
        |2. centos    | private          |  |2. centos       | rama         |
        |             |                  |  |3. centos       | leela        |
         --------------------------------    -------------------------------
        **/    	 
    }
    @AfterClass
    public static void memberTestCleanup() {
    	if(mysqlStore.get(EntityType.IMAGE, "ubuntu") != null) 
            mysqlStore.delete(EntityType.IMAGE, "ubuntu");
    	
    	if(mysqlStore.get(EntityType.IMAGE, "centos") != null) 
            mysqlStore.delete(EntityType.IMAGE, "centos");
    	
    	if(mysqlStore.get(EntityType.MEMBER, "centos_poovanna") != null) 
            mysqlStore.delete(EntityType.MEMBER, "centos_poovanna");    
    	
    	if(mysqlStore.get(EntityType.MEMBER, "centos_rama") != null) 
            mysqlStore.delete(EntityType.MEMBER, "centos_rama"); 
    	
    	if(mysqlStore.get(EntityType.MEMBER, "centos_leela") != null) 
            mysqlStore.delete(EntityType.MEMBER, "centos_leela");
    	
    	if(mysqlStore.get(EntityType.MEMBER, "centos_sita") != null) 
            mysqlStore.delete(EntityType.MEMBER, "centos_sita");
    }
    //@AfterClass
    public static void cleanup() {
        try (Handle h = mysqlStore.getDb().getConnection().open()) {
            h.execute("DELETE FROM glancemeta WHERE entityId = :id", 4444);
        }
    }

    ////@Test
    public final void testImages() {
        Response response = target("/v2/images").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        // todo: read entity as images and extract image list
        Images images = response.readEntity(Images.class);
        assert(images.getImages().get(0).getId().equals("4444"));
    }

    //@Test
    public final void testCreateImageId() {
        Response response = target("/v2/images").request().post(META_DATA);
//        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
    
    @Test
    public final void testCreateImage() {
    	com.emc.caspian.ccs.imagerepo.model.Image image=new com.emc.caspian.ccs.imagerepo.model.Image();
    	//image.setId("44544");
        image.setArchitecture("X64");
        image.setChecksum("checksume");
        image.setCreated_at("2/26/2015:9:20");
        image.setDirect_url("ftp://testFtpServer/image_4444");
        image.setDisk_format(Disk_format.ISO);
        image.setContainer_format(Container_format.BARE);
        image.setSize(2048);
        Response response = target("/v2/images").request().post(Entity.json(image));
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    }
    
    //@Test
    public final void testImage() {
        Response response = target("/v2/images/5555").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        com.emc.caspian.ccs.imagerepo.model.Image result = response
                .readEntity(com.emc.caspian.ccs.imagerepo.model.Image.class);
        assertEquals(result, imageGlanceV2);

    }

    //@Test
    public final void testPatchImage() {
        String patch = "[{  \"op\": \"replace\",    \"path\": \"/architecture\",    \"value\": \"X86\"}]";
        Response response = target("/v2/images/4444").request().method("PATCH",
                Entity.entity(patch, "application/json-patch+json"));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        //TODO: enable once the Registry is populated.
    }

    @Test
    public final void testDelete() {
    	Response response = target("/v2/images/4444").request().delete();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        response = target("/v2/images/5555").request().delete();
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus());
    }

    public final void testDownloadImage() {
        Response response = target("/v2/images/imageId/file").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    public final void testUploadImage() {
        Response response = target("/v2/images/imageId/file").request().put(BIN_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    //@Test
    public final void testAddTag() {
    	 Response response = target("/v2/images/5555/tags/world").request().put(BIN_DATA);
         assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    //@Test
    public final void testDeleteTag() {
    	Response response = target("/v2/images/5555/tags/hello").request().delete();
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
    }

    @Test
    public final void testMembership() {
    	// Getting members of a non-existent image.
        Response response1 = target("/v2/images/no_image/members").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());
        
       // Getting members of an image with visibility public.
        Response response2 = target("/v2/images/ubuntu/members").request().get();
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response2.getStatus());
        
       // Getting members of an image image.
        Response response3 = target("/v2/images/centos/members").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
    }

    @Test
    public final void testAddmember() {
        String payload = "{\"member\": \"sita\"}";
    	
    	/** Adding a member. The image doesn't exist. NOT FOUND error expected **/
    	Response response1 = target("/v2/images/no_image/members").request().post(Entity.entity(payload, MediaType.APPLICATION_JSON), Response.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());
        
        /** Adding a member. The image exists but visibility is public. FORBIDDEN error expected **/
        Response response2 = target("/v2/images/ubuntu/members").request().post(Entity.entity(payload, MediaType.APPLICATION_JSON), Response.class);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response2.getStatus());
        
        /** Adding a member to a valid image **/
        Response response3 = target("/v2/images/centos/members").request().post(Entity.entity(payload, MediaType.APPLICATION_JSON), Response.class);
        assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
        
        /** Re-adding a member to a valid image. CONFLICT error expected **/
        Response response4 = target("/v2/images/centos/members").request().post(Entity.entity(payload, MediaType.APPLICATION_JSON), Response.class);
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response4.getStatus());
    }

    @Test
    public final void testMemberDetails() {
    	/** Getting a invalid member. The image doesn't exist **/
    	Response response1 = target("/v2/images/no_image/members/poovanna").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());
        
        /** Getting a invalid member. The image exists but visibility is public **/
        Response response2 = target("/v2/images/ubuntu/members/poovanna").request().get();
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response2.getStatus());
              
        /** Getting a non-existent member **/
        Response response3 = target("/v2/images/centos/members/pradhan").request().get();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response3.getStatus());
        
        /** Getting a valid member **/
        Response response4 = target("/v2/images/centos/members/poovanna").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response4.getStatus());
    }

    @Test
    public final void testUpdateMember() {
        String payload = "{\"status\": \"accepted\"}";
    	
    	/** Updating a non-existent member. The image doesn't exist **/
    	Response response1 = target("/v2/images/no_image/members/ishaan").request().put(Entity.entity(payload, MediaType.APPLICATION_JSON), Response.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());
        
        /** Updating a invalid member of an image with visibility public **/
        Response response2 = target("/v2/images/ubuntu/members/poovanna").request().put(Entity.entity(payload, MediaType.APPLICATION_JSON), Response.class);
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response2.getStatus());
               
        /** Updating a valid member **/
        Response response3 = target("/v2/images/centos/members/poovanna").request().put(Entity.entity(payload, MediaType.APPLICATION_JSON), Response.class);
        assertEquals(Response.Status.OK.getStatusCode(), response3.getStatus());
        
        /** Updating a non-existing member. But the image exists **/
        Response response4 = target("/v2/images/centos/members/madhu").request().put(Entity.entity(payload, MediaType.APPLICATION_JSON), Response.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response4.getStatus());
    }

    @Test
    public final void testDeleteMember() {
    	/** No image exists. Deleting a non-existent member of a non-existent image **/
    	Response response1 = target("/v2/images/no_image/members/poovanna").request().delete();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response1.getStatus());    
        
        /** Image exists with visibility public **/
    	Response response2 = target("/v2/images/ubuntu/members/poovanna").request().delete();
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response2.getStatus());  
        
        /** The image exists.But no member of it exists.Deleting non-existent member of the image **/
        Response response3 = target("/v2/images/centos/members/madhu").request().delete();
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response3.getStatus());
        
        /** Both the image and its member exists. Deleting a valid member **/
        Response response4 = target("/v2/images/centos/members/leela").request().delete();
        assertEquals(Response.Status.OK.getStatusCode(), response4.getStatus());
    }

    @Override
    protected Application configure() {
        ResourceConfig cfg = ResourceConfig.forApplication(createApplication(new Class[] { FrontEndGlanceV2.class }));
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        cfg.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(request).to(HttpServletRequest.class);
            }
        });
        cfg.register(AcceptPatchHeaderFilter.class);
        return cfg;
    }
}
