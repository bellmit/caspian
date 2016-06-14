/**
 *  Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.taskEngine.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.common.utils.FileHelper;
import com.emc.caspian.ccs.common.utils.ImageStoreHelper;
import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image.Status;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreConfig;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.ccs.registry.Registry;
import com.emc.caspian.ccs.taskEngine.CallableTask;
import com.emc.caspian.fabric.util.Validate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Import task is supported in Glance Juno release.
 *
 * @author shrids
 *
 */
public class ImportTask implements CallableTask {

    private final Task task;
    private final Input taskInput;

    // Input definition is specific to a given task. It is a JSON blob
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Input {
        @JsonProperty
        private String import_from;

        @JsonProperty
        private String import_from_format;

        @JsonProperty(required = false)
        private Map<String, Object> image_properties;

        //attributes of Input class
        public static final String IMPORT_FROM = "import_from";
        public static final String IMAGE_PROPERTIES = "image_properties";
        public static final String IMPORT_FROM_FORMAT = "import_from_format";
    }

    public ImportTask(final Task task) {
        Validate.isNotNull(task, "Task details");
        this.task = task;
        this.taskInput = getInput(task);
        if (!isValid()) {
            throw new IllegalArgumentException("Validation of the submitted task failed");
        }
    }

    @SuppressWarnings("unchecked")
    private Input getInput(final Task task) {
        Input input = null;
        if (task.getInput() == null) {
            input = null;
        } else {
            if(task.getInput() instanceof String) {
                input = JsonHelper.deserializeFromJson((String) task.getInput(), Input.class);
            } else if(task.getInput() instanceof Map ) {
                //Glance client does not send it as a json blob, but the glance model indicates that input should be a json blob.
                //this is to handle this scenario.
                Map<String, Object> map = (Map<String, Object>) task.getInput();
                input = new Input();
                input.import_from = (String) map.get(Input.IMPORT_FROM);
                input.import_from_format = (String) map.get(Input.IMPORT_FROM_FORMAT);
                input.image_properties = (Map<String, Object>) map.get(Input.IMAGE_PROPERTIES);
            }
        }
        return input;
    }

    private boolean isValid() {
        if (task.getInput() != null && isInputValid()) {
            return true;
        }
        return false;
    }

    /**
     * Validate if the input to import task is valid.
     *
     * @return
     */
    private boolean isInputValid() {

        if (taskInput != null) {
            if (StringUtils.isNotBlank(taskInput.import_from) && StringUtils.isNotBlank(taskInput.import_from_format)) {
                return true;
            } else {
                _log.error("Task Input should contain non-empty import_from and import_from_format fields");
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.taskEngine.CallableTask#onSubmit()
     */
    @Override
    public String onSubmit() throws Exception {
        String taskID = UUID.randomUUID().toString();

        task.setCreatedAt(Long.toString(System.currentTimeMillis()));
        task.setId(taskID);
        task.setStatus(Task.Status.PENDING); // change the state of the task to Pending

        Registry.insertTask(task).get();

        return taskID;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Task call() throws Exception {
        task.setStatus(Task.Status.PROCESSING);
        task.setUpdatedAt(Long.toString(System.currentTimeMillis()));

        Registry.updateTask(task).get();

        performTask();

        return task;
    }

    private void performTask() throws URISyntaxException, MalformedURLException, InterruptedException, ExecutionException,
            TimeoutException, IOException {

        // blocking call
        URI cloudImageURI = new URI(taskInput.import_from);
        String glanceImageID = UUID.randomUUID().toString();
        URL glanceURL = saveCloudImage(cloudImageURI, glanceImageID);

        // Validation of image- This is mentioned only in the blueprint of openstack, but this is
        // not present in the python implementation.

        Image image = constructImage(cloudImageURI, glanceImageID);
        image.setFile(glanceURL.toString()); // wait until the download is successful.
        Registry.createImage(image);

        HashMap<String,String> resultMap = new HashMap<String,String>(1);
        resultMap.put("image_uuid", glanceImageID);
        task.setResult(resultMap);
    }

    @SuppressWarnings("unchecked")
    private Image constructImage(URI directURL, String imageId) {
        Image image = new Image(imageId);
        image.setCreatedAt(Long.toString(System.currentTimeMillis()));
        image.setDiskFormat(taskInput.import_from_format);

        image.setDirectUrl(directURL.toString());
        image.setInstanceUuid(task.getId()); // id used to create this file.
        String imageName = (String) taskInput.image_properties.get("name");
        image.setName((imageName == null) ? imageId : imageName);
        image.setTags((List<String>) taskInput.image_properties.get("tags"));
        image.setStatus(Status.ACTIVE);// location is populated hence active
        return image;
    }

    /*
     * Method to import the image into glance.
     */
    private URL saveCloudImage(URI sourceImageURI, String glanceImageID) throws InterruptedException, ExecutionException,
            TimeoutException, IOException {

        _log.info("Fetching image from cloud. Source URL: {} TaskID: {} ", sourceImageURI, task.getId());

        URL glanceImageURL = null;

        try (InputStream stream = fetchCloudImage(sourceImageURI)) {

        	ImmutablePair<Long, InputStream> pairResponse= ImageStoreHelper.getStreamSize(stream, ImageStoreConfig.ObjectConfig.tempDir.value());
        	long streamLength = pairResponse.getLeft();
        	InputStream uploadStream = pairResponse.getRight();

        	ImageStore imageStore = ImageStoreFactory.getImageStore();
            glanceImageURL = imageStore.saveImageFile(glanceImageID, uploadStream, streamLength).get();
        } catch (InterruptedException | ExecutionException exception) {
            _log.error("Exception occured when importing Cloud image ", exception);
            throw exception;
        }
        return glanceImageURL; // return stored url
    }

    private InputStream fetchCloudImage(final URI sourceImageURI) {
        //only fileSchema is supported as of now. This needs to be extended to other protocols.
        if(!sourceImageURI.getScheme().equalsIgnoreCase("file")) {
            _log.error("Only File Scheme is supported for Import Task");
            throw new NotImplementedException();
        }

        final String imageFilePath = sourceImageURI.getPath();
        if(!FileHelper.checkFileExists(imageFilePath)) {
            _log.error("File does not exist. File:{}", imageFilePath);
            throw new IllegalArgumentException();
        }
        return FileHelper.readFileAsStream(imageFilePath);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.taskEngine.CallableTask#onSuccess()
     */
    @Override
    public void onSuccess() throws Exception {
        _log.info("Import Task ID: {} is successful", task.getId());
        task.setStatus(Task.Status.SUCCESS);
        long currentTimeMillis = System.currentTimeMillis();
        task.setUpdatedAt(Long.toString(currentTimeMillis));
        task.setExpiresAt(Long.toString(currentTimeMillis + TASK_TTL_MS));
        Registry.updateTask(task).get();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.taskEngine.CallableTask#onFailure(java.lang.Throwable)
     */
    @Override
    public void onFailure(Throwable exception) throws Exception {
        _log.error("Import Task ID: {} failed", task.getId());
        _log.error("exception details", exception);

        task.setMessage(exception.getMessage());
        task.setStatus(Task.Status.FAILURE.toString());
        long currentTimeMillis = System.currentTimeMillis();
        task.setUpdatedAt(Long.toString(currentTimeMillis));
        task.setExpiresAt(Long.toString(currentTimeMillis + TASK_TTL_MS));
        Registry.updateTask(task).get();
    }

    /**
     * @return the task
     */
    public final Task getTask() {
        return task;
    }

    private static final Logger _log = LoggerFactory.getLogger(ImportTask.class);
}
