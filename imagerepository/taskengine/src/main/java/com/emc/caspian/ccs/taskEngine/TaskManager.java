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
package com.emc.caspian.ccs.taskEngine;

import java.util.concurrent.Future;

import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task.Status;
import com.emc.caspian.ccs.registry.Registry;

/**
 * Helper methods to interact with TaskEngine.
 * @author shrids
 *
 */
public class TaskManager {

    private static final TaskEngine taskEngine = new TaskEngine();

    private TaskManager() {
        // private to prevent Instantiation
    }

    public static TaskEngine fetchTaskEngine() {
        return taskEngine;
    }

    public static final Protocol.TaskResponse submitTask(final Protocol.CreateTaskRequest taskRequest) {
        String taskID = taskEngine.submit(taskRequest.getTask());
        // construct a taskentity with the taskID
        Task taskResult = new Task(taskID);
        taskResult.setStatus(Status.PENDING);
        taskResult.setInput(taskRequest.getTask().getInput());
        taskResult.setOwner(taskRequest.getTask().getOwner());

        Protocol.TaskResponse response = new Protocol.TaskResponse().setTask(taskResult);
        response.setStatus(Protocol.Status.CREATED);
        return response;
    }

    public static Future<Protocol.TaskResponse> getTaskDetails(final Protocol.TaskRequest request) {
        return Registry.getTaskDetails(request);
    }

}
