package com.emc.caspian.ccs.taskEngine;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;

import com.emc.caspian.ccs.datastore.DataStoreFactory;
import com.emc.caspian.ccs.datastore.DataStoreType;
import com.emc.caspian.ccs.datastore.PropertyBag;
import com.emc.caspian.ccs.datastore.mysql.MySqlStore;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.MetadataBase;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task.Type;
import com.emc.caspian.fabric.config.Configuration;

public class TaskEngineTest {

    private TaskEngine engine;

    static {
        try {
            Configuration.load("src/test/resources/registry.conf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static MySqlStore mysqlStore = (MySqlStore) DataStoreFactory.getImageStore(DataStoreType.MYSQL);

    @Before
    public void setUp() throws Exception {
        try (Handle h = mysqlStore.getDb().getConnection().open()) {
            h.execute("DELETE FROM glancemeta");
        }
        engine = new TaskEngine();

    }

    @Test
    public final void testSubmitSwift() throws InterruptedException {
        Task taskData = new Task("123");
        // taskData.setCreatedAt(Long.toString(System.currentTimeMillis()));
        taskData.setInput("{\"import_from\":\"swift://cloud.foo/myaccount/mycontainer/path\",\"import_from_format\":\"qcow2\",\"image_properties\":{\"name\":\"GreatStack 1.22\",\"tags\":[\"lamp\",\"custom\"]}}");
        taskData.setType(Type.IMPORT);
        String taskID = engine.submit(taskData);

        Thread.sleep(10000); // wait for execution to complete. TODO:Avoid wait in unit tests.

        // Verification... fetch the taskID status
        PropertyBag<? extends MetadataBase> result1 = mysqlStore.get(EntityType.TASK, taskID);
        Task taskEntity1 = (Task) result1.getEntityObject();
        assertEquals(Task.Status.FAILURE, Task.Status.fromValue(taskEntity1.getStatus()));
        //assertEquals("Code is not implemented", taskEntity1.getMessage()); // swift is not
                                                                           // implemetented as of
                                                                           // now.

    }

    @Test
    public final void testSubmitFile() throws InterruptedException, MalformedURLException {
        Path path = Paths.get("src/test/resources/testImage.image");
        URI fileURI = path.toAbsolutePath().toUri();
        URL fileURL = fileURI.toURL();

        Task taskData = new Task("123");
        // taskData.setCreatedAt(Long.toString(System.currentTimeMillis()));
        taskData.setInput("{\"import_from\":\""
                + fileURL.toString()
                + "\",\"import_from_format\":\"qcow2\",\"image_properties\":{\"name\":\"GreatStack 1.22\",\"tags\":[\"lamp\",\"custom\"]}}");
        taskData.setType(Type.IMPORT);
        String taskID = engine.submit(taskData);

        Thread.sleep(10000); // wait for execution to complete. TODO:Avoid wait in unit tests.
        engine.shutdown();

        // Verification... fetch the taskID status
        PropertyBag<? extends MetadataBase> result1 = mysqlStore.get(EntityType.TASK, taskID);
        Task taskEntity1 = (Task) result1.getEntityObject();
        assertEquals(Task.Status.SUCCESS, Task.Status.fromValue(taskEntity1.getStatus()));

    }
}
