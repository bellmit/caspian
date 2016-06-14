package com.emc.caspian.ccs.imagerepo.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.Handle;

import com.emc.caspian.ccs.datastore.DataStoreFactory;
import com.emc.caspian.ccs.datastore.DataStoreType;
import com.emc.caspian.ccs.datastore.PropertyBag;
import com.emc.caspian.ccs.datastore.mysql.MySqlStore;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.imagerepo.model.Task.Type;
import com.emc.caspian.fabric.config.Configuration;

/**
 * Test Glance Task APIs.
 *
 * @author shrids
 *
 */
public class FrontEndGlanceV2TasksTest extends AbstractRestAPITest {

    static {
        try {
            Configuration.load("src/test/resources/registry.conf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static MySqlStore mysqlStore = (MySqlStore) DataStoreFactory.getImageStore(DataStoreType.MYSQL);

    @BeforeClass
    public static void setUpBefore() throws Exception {
        try (Handle h = mysqlStore.getDb().getConnection().open()) {
            h.execute("DELETE FROM glancemeta");
        }
        long currentTimeMS = System.currentTimeMillis();

        mysqlStore.insert(new PropertyBag<Task>(createTask("1", currentTimeMS, currentTimeMS + 10, Task.Status.FAILURE)));
        mysqlStore.insert(new PropertyBag<Task>(createTask("2", currentTimeMS, currentTimeMS + 20, Task.Status.SUCCESS)));
        mysqlStore.insert(new PropertyBag<Task>(createTask("3", currentTimeMS, currentTimeMS + 30, Task.Status.PENDING)));
        mysqlStore.insert(new PropertyBag<Task>(createTask("4", currentTimeMS, currentTimeMS + 40, Task.Status.PROCESSING)));
        mysqlStore.insert(new PropertyBag<Task>(createTask("5", currentTimeMS, currentTimeMS + 50, Task.Status.SUCCESS)));

    }

    private static Task createTask(String id, long createdAt, long updatedAt, Task.Status status) {
        // Create task.
        Task importTask = new Task();
        importTask.setType("import");
        importTask
                .setInput("{\"import_from\":\""
                        + "file://testlocation"
                        + "\",\"import_from_format\":\"qcow2\",\"image_properties\":{\"name\":\"GreatStack 1.22\",\"tags\":[\"lamp\",\"custom\"]}}");
        importTask.setId(id);
        importTask.setCreatedAt(Long.toString(createdAt));
        importTask.setUpdatedAt(Long.toString(updatedAt));
        importTask.setStatus(status);
        return importTask;
    }

    @Test
    public final void testTasksQueryParamType() {
        Response response = target("/v2/tasks").queryParam("type", "import").request().get();
        com.emc.caspian.ccs.imagerepo.model.Tasks tasks = response.readEntity(com.emc.caspian.ccs.imagerepo.model.Tasks.class);
        List<com.emc.caspian.ccs.imagerepo.model.Task> listofTasks = tasks.getTasks();
        assertTrue(listofTasks.size()>= 5); //the tasks listed should be greater than equal
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public final void testTasksQueryParamStatus() {
        Response response = target("/v2/tasks").queryParam("status", "success").queryParam("sort_dir", "asc").request().get();
        com.emc.caspian.ccs.imagerepo.model.Tasks tasks = response.readEntity(com.emc.caspian.ccs.imagerepo.model.Tasks.class);
        List<com.emc.caspian.ccs.imagerepo.model.Task> listofTasks = tasks.getTasks();

        // we know that task id 5 and taskid 2 are successful.
        com.emc.caspian.ccs.imagerepo.model.Task task_5 = null;
        com.emc.caspian.ccs.imagerepo.model.Task task_2 = null;
        //check if it is in the list returned.
        for (com.emc.caspian.ccs.imagerepo.model.Task task : listofTasks) {
            if (task.getId().equals("2"))
                task_2 = task;
            if (task.getId().equals("5"))
                task_5 = task;
        }
        assertTrue(task_2 != null);
        assertTrue(task_5 != null);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public final void testTasksQueryParamStatus_1() {
        Response response = target("/v2/tasks").queryParam("status", "failure").request().get();
        com.emc.caspian.ccs.imagerepo.model.Tasks tasks = response.readEntity(com.emc.caspian.ccs.imagerepo.model.Tasks.class);
        List<com.emc.caspian.ccs.imagerepo.model.Task> listofTasks = tasks.getTasks();

        assertEquals("1", listofTasks.get(0).getId());
        assertEquals(com.emc.caspian.ccs.imagerepo.model.Task.Status.FAILURE, listofTasks.get(0).getStatus());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public final void testTasksQueryParamStatus_2() {
        Response response = target("/v2/tasks").queryParam("status", "processing").request().get();
        com.emc.caspian.ccs.imagerepo.model.Tasks tasks = response.readEntity(com.emc.caspian.ccs.imagerepo.model.Tasks.class);
        List<com.emc.caspian.ccs.imagerepo.model.Task> listofTasks = tasks.getTasks();
        assertEquals(1, listofTasks.size());
        assertEquals("4", listofTasks.get(0).getId());
        assertEquals(com.emc.caspian.ccs.imagerepo.model.Task.Status.PROCESSING, listofTasks.get(0).getStatus());
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }


    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Tasks#createTask(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     *
     * @throws MalformedURLException
     */
    @Test
    public final void testCreateTask() throws MalformedURLException {

        Path path = Paths.get("src/test/resources/testImageFile.image");
        URI fileURI = path.toAbsolutePath().toUri();
        URL fileURL = fileURI.toURL();

        // Create task.
        com.emc.caspian.ccs.imagerepo.model.Task importTask = new com.emc.caspian.ccs.imagerepo.model.Task();
        importTask.setType(com.emc.caspian.ccs.imagerepo.model.Task.Type.IMPORT);
        importTask
                .setInput("{\"import_from\":\""
                        + fileURL.toString()
                        + "\",\"import_from_format\":\"qcow2\",\"image_properties\":{\"name\":\"GreatStack 1.22\",\"tags\":[\"lamp\",\"custom\"]}}");

        Response response = target("/v2/tasks").request().post(Entity.json(importTask));
        com.emc.caspian.ccs.imagerepo.model.Task taskResult = response.readEntity(com.emc.caspian.ccs.imagerepo.model.Task.class);
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertTrue(taskResult.getId() != null);
        assertTrue(response.getHeaderString("Location").contains(taskResult.getId()));

        Response getTaskResponse = target("/v2/tasks/" + taskResult.getId()).request().get();
        assertEquals(Response.Status.OK.getStatusCode(), getTaskResponse.getStatus());

    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Tasks#createTask(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     *
     * @throws MalformedURLException
     */
    @Test
    public final void testCreateTaskError() throws MalformedURLException {

        Path path = Paths.get("src/test/resources/testImageFile.image");
        URI fileURI = path.toAbsolutePath().toUri();
        URL fileURL = fileURI.toURL();

        // Create task.
        com.emc.caspian.ccs.imagerepo.model.Task importTask = new com.emc.caspian.ccs.imagerepo.model.Task();
        importTask.setType(Type.IMPORT);
        importTask.setInput("{\"import_from\":\"" + fileURL.toString()
                + "\",\"\"qcow2\",\"image_properties\":{\"name\":\"GreatStack 1.22\",\"tags\":[\"lamp\",\"custom\"]}}");

        Response response = target("/v2/tasks").request().post(Entity.json(importTask));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for
     * {@link com.emc.caspian.ccs.imagerepo.resources.FrontEndGlanceV2Tasks#deleteTask(java.lang.String, javax.servlet.http.HttpServletRequest)}
     * .
     */
    @Test
    public final void testDeleteTask() {
        Response response = target("/v2/tasks/taskID").request().delete();
        assertEquals(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), response.getStatus());
    }

    @Override
    protected Application configure() {
        ResourceConfig cfg = ResourceConfig.forApplication(createApplication(new Class[] { FrontEndGlanceV2Tasks.class }));
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
