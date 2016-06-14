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
package com.emc.caspian.ccs.imagerepo.resources;

import java.util.concurrent.Future;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import jersey.repackaged.com.google.common.collect.Lists;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.imagerepo.FrontEndHelper;
import com.emc.caspian.ccs.imagerepo.HandleRequestDelegate;
import com.emc.caspian.ccs.imagerepo.TransformDelegate;
import com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2Tasks;
import com.emc.caspian.ccs.imagerepo.api.ModelHelper;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.CreateTaskRequest;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.TaskRequest;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.TaskResponse;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.registry.Registry;
import com.emc.caspian.ccs.taskEngine.TaskManager;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * Implemenation of GlanceV2Task APIs. Refer: http://developer.openstack.org/api-ref-image-v2.html
 *
 * @author shrids
 *
 */
public final class FrontEndGlanceV2Tasks extends BaseResource implements GlanceV2Tasks {

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2Tasks#tasks()
     */
    @Override
    public Response tasks(String type, String status, String sortDir) {

        final Protocol.TasksRequest request = new Protocol.TasksRequest().setSortDir(sortDir).setStatus(status).setType(type);

        Response response = FrontEndHelper.handleRequest(request, getKeystonePrincipal(), new HandleRequestDelegate<Protocol.TasksResponse>() {
            @Override
            public Protocol.TasksResponse process() throws Exception {
                // get registry
                Future<Iterable<Task>> futuresList = Registry.getTasks(request);

                final Protocol.TasksResponse tasksResponse = new Protocol.TasksResponse();
                tasksResponse.setTasks(futuresList.get());

                return tasksResponse;
            }
        }, new TransformDelegate<Protocol.TasksResponse>() {
            @Override
            public ResponseBuilder transform(final Protocol.TasksResponse response) {
                com.emc.caspian.ccs.imagerepo.model.Tasks tasks = new com.emc.caspian.ccs.imagerepo.model.Tasks();
                tasks.setTasks(Lists.newArrayList(Iterables.transform(response.getTasks(),
                        new Function<Task, com.emc.caspian.ccs.imagerepo.model.Task>() {
                            @Override
                            public com.emc.caspian.ccs.imagerepo.model.Task apply(final Task input) {
                                return ModelHelper.encode(input);
                            }
                        })));
                tasks.setSchema("/v2/schemas/images");
                return Response.ok().entity(tasks);
            }
        }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2Tasks#createTask(com.emc.caspian.ccs.imagerepo
     * .model.Task, javax.ws.rs.core.UriInfo)
     */
    @Override
    public Response createTask(final com.emc.caspian.ccs.imagerepo.model.Task taskData, final UriInfo uriInfo) {
        final Task decodedTaskData = ModelHelper.decode(taskData);
        final CreateTaskRequest request = new Protocol.CreateTaskRequest();
        request.setTask(decodedTaskData);

        final KeystonePrincipal principal = getKeystonePrincipal();
        if (principal != null) {
            decodedTaskData.setOwner(principal.getUserName());
        }

        final UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();
        Response response = FrontEndHelper.handleRequest(request, getKeystonePrincipal(), new HandleRequestDelegate<Protocol.TaskResponse>() {
            @Override
            public Protocol.TaskResponse process() throws Exception {
                TaskResponse taskResponse = TaskManager.submitTask(request);
                taskResponse.setLocation(uriBuilder.path(taskResponse.getTask().getId()).build());
                return taskResponse;
            }
        }, new TransformDelegate<Protocol.TaskResponse>() {
            @Override
            public ResponseBuilder transform(final Protocol.TaskResponse response) {

                Task resultTask = response.getTask();
                return Response.created(response.getLocation()).entity(ModelHelper.encode(resultTask));
            }
        }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2Tasks#task(java.lang.String)
     */
    @Override
    public Response task(final String taskId) {

        final TaskRequest request = new Protocol.TaskRequest();
        request.setTaskId(taskId);

        Response response = FrontEndHelper.handleRequest(request, getKeystonePrincipal(), new HandleRequestDelegate<Protocol.TaskResponse>() {
            @Override
            public Protocol.TaskResponse process() throws Exception {
                TaskResponse taskResponse = TaskManager.getTaskDetails(request).get();
                return taskResponse;
            }
        }, new TransformDelegate<Protocol.TaskResponse>() {
            @Override
            public ResponseBuilder transform(final Protocol.TaskResponse response) {

                Task resultTask = response.getTask();
                return Response.ok().entity(ModelHelper.encode(resultTask));
            }
        }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2Tasks#deleteTask(java.lang.String)
     * Method not allowed. Reference: https://bugs.launchpad.net/glance/+bug/1287951
     */
    @Override
    public Response deleteTask(final String taskId) {
        return Response.status(Status.METHOD_NOT_ALLOWED).build();
    }
}
