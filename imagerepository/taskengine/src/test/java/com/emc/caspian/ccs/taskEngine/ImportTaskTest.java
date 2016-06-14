package com.emc.caspian.ccs.taskEngine;

import org.junit.Test;

import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task.Type;
import com.emc.caspian.ccs.taskEngine.tasks.ImportTask;
import com.emc.caspian.fabric.config.Configuration;

public class ImportTaskTest {

    ImportTask importTask;
    static {
        try {
            Configuration.load("src/test/resources/registry.conf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public final void testIsValid() {
        Task taskData = new Task("123");
        taskData.setInput("{   \"import_from\": \"swift://cloud.foo/myaccount/mycontainer/path\",  \"import_from_format\": \"qcow2\",  \"image_properties\": {         \"name\": \"GreatStack 1.22\",      \"tags\": [\"lamp\",        \"custom\"]     } }");
        taskData.setType(Type.IMPORT);
        importTask = new ImportTask(taskData); //no exception is thrown.
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testIsValidMissingImportFromTag() {
        Task taskData = new Task("123");
        taskData.setInput("{   \"import_from_format\": \"qcow2\",  \"image_properties\": {         \"name\": \"GreatStack 1.22\",      \"tags\": [\"lamp\",        \"custom\"]     } }");
        taskData.setType(Type.IMPORT);
        importTask = new ImportTask(taskData);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testIsValidMissingImportFromFromatTag() {
        Task taskData = new Task("123");
        taskData.setInput("{   \"import_from\": \"swift://cloud.foo/myaccount/mycontainer/path\",  \"image_properties\": {         \"name\": \"GreatStack 1.22\",      \"tags\": [\"lamp\",        \"custom\"]     } }");
        taskData.setType(Type.IMPORT);
        importTask = new ImportTask(taskData);
    }

    @Test
    public final void testIsValidMissingProperties() {
        Task taskData = new Task("123");
        taskData.setInput("{   \"import_from\": \"swift://cloud.foo/myaccount/mycontainer/path\",  \"import_from_format\": \"qcow2\" }");
        taskData.setType(Type.IMPORT);
        importTask = new ImportTask(taskData);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testIsValidError() {
        // null input
        Task taskData = new Task("123");
        taskData.setType(Type.IMPORT);
        importTask = new ImportTask(taskData);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testIsValidEmptyInput() {
        // null input
        Task taskData = new Task("123");
        taskData.setInput("");
        taskData.setType(Type.IMPORT);
        importTask = new ImportTask(taskData);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testIsValidError2() {
        // invalid Input
        Task taskData = new Task("123");
        taskData.setInput("{   \"testKey\": \"swift://cloud.foo/myaccount/mycontainer/path\",  \"testkey_2\": \"qcow2\" }");
        taskData.setType(Type.IMPORT);
        importTask = new ImportTask(taskData);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testIsValidError3() {
        // invalid json format
        Task taskData = new Task("123");
        taskData.setInput("{   \"testKey\": swift://cloud.foo/myaccount/mycontainer/path\",  \"testkey_2\": \"qcow2\" }");
        taskData.setType(Type.IMPORT);
        importTask = new ImportTask(taskData);
    }
}
