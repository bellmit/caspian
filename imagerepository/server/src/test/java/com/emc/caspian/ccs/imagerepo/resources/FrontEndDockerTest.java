package com.emc.caspian.ccs.imagerepo.resources;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.skife.jdbi.v2.Handle;

import com.emc.caspian.ccs.datastore.DataStoreFactory;
import com.emc.caspian.ccs.datastore.DataStoreType;
import com.emc.caspian.ccs.datastore.mysql.MySqlStore;
import com.emc.caspian.fabric.config.Configuration;

/**
 * Test Docker related APIs.
 * @author shivat
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FrontEndDockerTest extends AbstractRestAPITest {

    static {
        try {
            Configuration.load("src/test/resources/registry.conf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static MySqlStore mysqlStore = (MySqlStore) DataStoreFactory.getImageStore(DataStoreType.MYSQL);
    private static String repository = "opensuse";
    private static String docketLayerId1 = "058c78b4";
    private static String docketLayerId2 = "f6";
    private static String docketLayerId3 = "758c78b4";
    protected static String sampleRepoJsonData_1 = "{\"latest\":\"758c78b4\",\"2.0\":\"" + docketLayerId2 + "\"}";
    protected static final Entity<String> REPO_META_DATA_1 = Entity.entity(sampleRepoJsonData_1, MediaType.APPLICATION_JSON);

    protected static String Member_1 = "MADD00001";
    protected static String Member_2 = "MADD00002";
    protected static String Member_3 = "MADD00003";
    protected static String Member_4 = "MADD00004";

    protected static final Entity<String> MEMBER_META_DATA_1 = Entity.entity(Member_1, MediaType.TEXT_PLAIN);
    protected static final Entity<String> MEMBER_META_DATA_2 = Entity.entity(Member_2, MediaType.TEXT_PLAIN);
    protected static final Entity<String> MEMBER_META_DATA_3 = Entity.entity(Member_3, MediaType.TEXT_PLAIN);
    protected static final Entity<String> MEMBER_META_DATA_4 = Entity.entity(Member_4, MediaType.TEXT_PLAIN);

    private String getSampleRepoJsonDataFile( Map<String, String> tagsToImageIdMap) {
        String tag = null;
        String imageId = null;
        StringBuilder jsonMetaData = new StringBuilder();

        for (Entry<String, String> e: tagsToImageIdMap.entrySet()) {
            if(StringUtils.isNotEmpty(jsonMetaData)) {
                jsonMetaData.append(",");
            }
            tag = e.getKey();
            imageId = e.getValue();
            jsonMetaData.append("\"").append(tag).append("\"").append(":").append("\"").append(imageId).append("\"");
        }
        jsonMetaData.append("}");
        jsonMetaData.insert(0, "{");

        return jsonMetaData.toString();
    }

    private Entity<String> getRepoMetaData( Map<String, String> tagsToImageIdMap) {
        return Entity.entity(getSampleRepoJsonDataFile(tagsToImageIdMap), MediaType.APPLICATION_JSON);
    }

    private String getSampleImageMetaDataFile(String imageId) {
        return "{"
                + "\"id\":\"" + imageId +"\","
                + "\"parent\":\"2546999f5a0e7186737fecbf52852c5d593e75e4f442b2239df75f784d3d1cf0\","
                + "\"created\":\"2015-03-12T00:05:47.806830513Z\","
                + "\"Image\":\"9f5\","
                + "\"Size\":622706935229"
                + "}";
    }

    private Entity<String> getImageMetaData(String imageId) {
        return Entity.entity(getSampleImageMetaDataFile(imageId), MediaType.APPLICATION_JSON);
    }

    @BeforeClass
    public static void setup() {
        cleanup();
    }

    @AfterClass
    public static void cleanup() {
        cleanupDb();
    }

    private static void cleanupDb(){
        try (Handle h = mysqlStore.getDb().getConnection().open()) {
            h.execute("DELETE FROM glancemeta");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#createRepository(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testCreateRepository() {
        createRepository(repository, getTagsImageIdMap());
    }

    private Map<String, String> getTagsImageIdMap() {
        Map<String, String> tagsToImageIdMap = new TreeMap<String, String>();
        tagsToImageIdMap.put("1.0", docketLayerId3);
        tagsToImageIdMap.put("2.0", docketLayerId1);
        return tagsToImageIdMap;
    }

    private Response createRepository(String repository, Map<String, String> tagsToImageIdMap) {
        Response response = target("/docker/repositories/" + repository).request().put(getRepoMetaData(tagsToImageIdMap));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        return response;
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#createRepository(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testCreateRepositoryInvalidInput() {
        String errorneousRepoJsonData = "{\"latest\":\"758c78b4\",\"2.0\":\"" + docketLayerId1 + "\"";
        Entity<String> REPO_META_DATA = Entity.entity(errorneousRepoJsonData, MediaType.APPLICATION_JSON);
        Response response = target("/docker/repositories/" + repository).request().put(REPO_META_DATA);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#images(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testImages() {
        String repositoryName = "opensuseLatest";
        Map<String, String> tagsToImageIdMap = getTagsImageIdMap();
        createRepository(repositoryName,tagsToImageIdMap);
        Response response = target("/docker/repositories/" + repositoryName + "/images").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertEquals(getSampleRepoJsonDataFile(tagsToImageIdMap), resultantJson);
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#images(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testImagesInvalidInput() {
        Response response = target("/docker/repositories/" + repository + "_new" + "/images").request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#createImage(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testCreateImage() {
        Response response = createImage(docketLayerId2);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    private Response createImage(String imageId) {
        return target("/docker/images/" + imageId).request().put(getImageMetaData(imageId));
    }
    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#createImage(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testCreateImageInvalidInput() {
        String errorneousImageMetaDataFile = "{\"id\":\"f6\",\"";
        Entity<String> ERROR_IMAGE_META_DATA = Entity.entity(errorneousImageMetaDataFile, MediaType.APPLICATION_JSON);
        Response response = target("/docker/images/" + docketLayerId2).request().put(ERROR_IMAGE_META_DATA);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#createImage(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testCreateImageDuplicate() {
        String imgaeId = docketLayerId2 + "duplicate";
        createImage(imgaeId);
        Response response = createImage(imgaeId);
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#image(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testImage() {
        Response response = target("/docker/images/" + docketLayerId2).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertEquals(getSampleImageMetaDataFile(docketLayerId2), resultantJson);
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#image(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testImageInvalidInput() {
        Response response = target("/docker/images/" + docketLayerId2 +"_new").request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#uploadImage(byte[], java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testUploadImage() {
        Response response = target("/docker/images/"+ docketLayerId1 +"/layer").request().put(BIN_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#uploadImage(byte[], java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testUploadImageInvalidInput() {
        String binData = "";
        Entity<String> BIN_DATA_ERR = Entity.entity(binData, MediaType.APPLICATION_JSON);
        Response response = target("/docker/images/"+ docketLayerId1 +"/layer").request().put(BIN_DATA_ERR);
        assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());

        BIN_DATA_ERR = Entity.entity(binData, MediaType.APPLICATION_OCTET_STREAM);
        response = target("/docker/images/"+ docketLayerId1 +"/layer").request().put(BIN_DATA_ERR);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#downloadImage(String)
     * .
     */
    @Test
    public final void testDownloadImage() {
        createRepository("second"+repository, getTagsImageIdMap());

        Response response = target("/docker/repositories/third" + repository).request().put(REPO_META_DATA_1);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/repositories/" + repository + "/members").request().put(MEMBER_META_DATA_1);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/repositories/" + repository + "/members").request().put(MEMBER_META_DATA_3);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/repositories/" + repository + "/members").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/repositories/second" + repository + "/members").request().put(MEMBER_META_DATA_2);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/repositories/second" + repository + "/members").request().put(MEMBER_META_DATA_3);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/images/"+ docketLayerId2+"/layer").request().get();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        String imageId = docketLayerId1;
        createImage(imageId);
        response = target("/docker/images/"+ imageId +"/layer").request().put(BIN_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/images/"+ imageId +"/layer").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#downloadImage(String)
     * .
     */
    //    @Test
    //TODO will be enabled once membership apis negative cases are handled
    public final void testDownloadImageError() {
        Response response = target("/docker/images/"+ docketLayerId2+"_invalid/layer").request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#imageAncestry(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testImageAncestry() {
        createAndStoreAncestryGrpah();

        Response response = target("/docker/images/"+ 1 +"/ancestry").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String ancestryTree = response.readEntity(String.class);
        String expected = "[\"1\",\"2\",\"3\",\"4\",\"5\"]";
        assertEquals(expected, ancestryTree);
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#imageAncestry(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testImageAncestryInvalidInput() {
        Response response = target("/docker/images/"+ 7 +"/ancestry").request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    private void createAndStoreAncestryGrpah() {
        createImage("1","2");
        createImage("2","3");
        createImage("3","4");
        createImage("4","5");
    }

    private void createImage(String imageId, String parentId) {
        String imageMetaDataFile =
                "{"
                        + "\"id\":\""+ imageId +"\","
                        + "\"parent\":\""+ parentId+"\","
                        + "\"created\":\"2015-03-12T00:05:47.806830513Z\","
                        + "\"Image\":\"2546999f5\","
                        + "\"Size\":622706935222"
                        + "}";
        Entity<String> META_DATA = Entity.entity(imageMetaDataFile, MediaType.APPLICATION_JSON);
        Response response = target("/docker/images/"+imageId).request().put(META_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#membership(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testMembership() {
        Response response = target("/docker/repositories/" + repository + "/members").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#addMember(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testAddMember() {
        Response response = target("/docker/repositories/" + repository + "/members").request().put(MEMBER_META_DATA_1);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#deleteMember(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testDeleteMember() {
        //Adding a new member to perform delete operation
        Response response = target("/docker/repositories/" + repository + "/members").request().put(MEMBER_META_DATA_3);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/repositories/" + repository + "/members/"+Member_3).request().delete();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#deleteMember(java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testDeleteNonExistMember() {
        Response response = target("/docker/repositories/" + repository + "/members/"+Member_4).request().delete();
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#putMirror(String,String)}
     * .
     */
    @Test
    public final void testPutMirror() {
        putMirrors(docketLayerId2);
    }

    private void putMirrors(String imageId) {
        String location = "file://fmnt1/01/001";
        Entity<String> LOCATION_DATA = Entity.entity(location, MediaType.APPLICATION_JSON);
        Response response = target("/docker/images/"+ imageId +"/mirrors").request().put(LOCATION_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        location = "file://fmnt2/01/001";
        LOCATION_DATA = Entity.entity(location, MediaType.APPLICATION_JSON);
        response = target("/docker/images/"+ imageId +"/mirrors").request().put(LOCATION_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        location = "file://fmnt3/01/001";
        LOCATION_DATA = Entity.entity(location, MediaType.APPLICATION_JSON);
        response = target("/docker/images/"+ imageId +"/mirrors").request().put(LOCATION_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }
    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#putMirror(String,String)}
     * .
     */
    @Test
    public final void testPutMirrorError() {
        String location = "";
        Entity<String> LOCATION_DATA = Entity.entity(location, MediaType.APPLICATION_JSON);
        Response response = target("/docker/images/"+ docketLayerId2 +"/mirrors").request().put(LOCATION_DATA);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        location = "file://fmnt2/01/001";
        LOCATION_DATA = Entity.entity(location, MediaType.APPLICATION_SVG_XML_TYPE);
        response = target("/docker/images/"+ docketLayerId2 +"/mirrors").request().put(LOCATION_DATA);
        assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());

        location = "file://fmnt3/01/001";
        LOCATION_DATA = Entity.entity(location, MediaType.APPLICATION_JSON);
        response = target("/docker/images/"+ docketLayerId2 + "_wrong" +"/mirrors").request().put(LOCATION_DATA);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#mirrors(String)}
     * .
     */
    @Test
    public final void testMirrors() {
        String imageId = docketLayerId2;
        putMirrors(imageId);
        Response response = target("/docker/images/"+ imageId +"/mirrors").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String resultantJson = response.readEntity(String.class);
        String locations = "[\"file://fmnt1/01/001\",\"file://fmnt2/01/001\",\"file://fmnt3/01/001\"]";
        assertEquals(locations, resultantJson);
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#mirrors(String)}
     * .
     */
    @Test
    public final void testMirrorsError() {
        Response response = target("/docker/images/"+ docketLayerId2 + "_wrong" +"/mirrors").request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#tags(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testTags() {
        Response response = target("/docker/repositories/" + repository + "/tags").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        Map<String, String> tagsToImageIdMap = getTagsImageIdMap();
        assertEquals(getSampleRepoJsonDataFile(tagsToImageIdMap), resultantJson);
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#tags(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testTagsError() {
        Response response = target("/docker/repositories/" + repository + "_new" + "/tags").request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#getImageId(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testGetImageId() {
        createRepository("repoGetImageId", getTagsImageIdMap());
        Response response = target("/docker/repositories/"+ repository +"/tags/"+ "2.0").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String resultantJson = response.readEntity(String.class);
        assertEquals(docketLayerId1, resultantJson);
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#getImageId(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testGetImageIdError() {
        Response response = target("/docker/repositories/"+ repository + "_new" +"/tags/"+ "2.0").request().get();
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#addTag(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testAddTag() {
        String repositoryName = "openSuseAddTag";
        String imageId = docketLayerId1 + "AddTag";
        Map<String, String> tagsToImageIdMap = getTagsImageIdMap();
        createRepository(repositoryName, tagsToImageIdMap);
        createImage(imageId);

        String newTag = "testTag";
        Entity<String> TAG_DATA = Entity.entity(newTag, MediaType.APPLICATION_JSON);
        Response response = target("/docker/repositories/"+ repositoryName + "/images/" + imageId + "/tags").request().put(TAG_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/docker/repositories/" + repositoryName + "/tags").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String resultantJson = response.readEntity(String.class);

        tagsToImageIdMap.put(newTag, imageId);
        assertEquals(getSampleRepoJsonDataFile(tagsToImageIdMap), resultantJson);
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndDocker#addTag(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testAddTagError() {
        String newTag = "testTag";
        Entity<String> TAG_DATA = Entity.entity(newTag, MediaType.APPLICATION_JSON);
        //non-existing repository name
        Response response = target("/docker/repositories/"+ repository + "_new"  + "/images/" + docketLayerId1 + "/tags").request().put(TAG_DATA);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        //empty tag name
        TAG_DATA = Entity.entity("", MediaType.APPLICATION_JSON);
        response = target("/docker/repositories/"+ repository + "/images/" + docketLayerId1 + "/tags").request().put(TAG_DATA);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        //wrong content-type
        TAG_DATA = Entity.entity(newTag, MediaType.APPLICATION_ATOM_XML);
        response = target("/docker/repositories/"+ repository + "/images/" + docketLayerId1 + "/tags").request().put(TAG_DATA);
        assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
    }

    @Override
    protected Application configure() {
        ResourceConfig cfg = ResourceConfig.forApplication(createApplication(new Class[] { FrontEndDocker.class }));
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
