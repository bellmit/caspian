/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.imagerepo.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import java.util.Map;

import com.emc.caspian.ccs.common.policyengine.policy.Policy;
import com.emc.caspian.ccs.imagerepo.model.Image;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * REST APIs implemented by imageRepository service. This includes the Glance APIs
 *
 * @author shrids
 */
public final class ApiV1
{
    // TODO: Remove MediatType.TEXT_PLAIN
    // TODO: Glance Schema and MetaData APIs
    @Path("/docker")
    public static interface Docker
    {

        @GET
        @Path("/repositories/{repository}/images")
        @Produces({MediaType.APPLICATION_JSON})
        public Response images(@PathParam("repository") final String repository);

        @PUT
        @Path("/repositories/{repository}")
        @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
        public Response createRepository(final String repoMetadata, @PathParam("repository") final String repository);

        @PUT
        @Path("/images/{imageId}")
        @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
        public Response createImage(final String imageMetadata, @PathParam("imageId") final String imageId);

        @GET
        @Path("/images/{imageId}")
        @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
        public Response image(@PathParam("imageId") final String imageId);

        @PUT
        @Path("/images/{imageId}/layer")
        @Consumes({MediaType.APPLICATION_OCTET_STREAM})
        public Response uploadImage(final InputStream imageData, @PathParam("imageId") final String imageId, @HeaderParam("Content-Length") final long length);

        @GET
        @Path("/images/{imageId}/layer")
        @Produces({MediaType.APPLICATION_OCTET_STREAM})
        public Response downloadImage(@PathParam("imageId") final String imageId);

        @GET
        @Path("/images/{imageId}/ancestry")
        @Produces({MediaType.APPLICATION_JSON})
        public Response imageAncestry(@PathParam("imageId") final String imageId);

        @GET
        @Path("/repositories/{repository}/members")
        @Produces({MediaType.APPLICATION_JSON})
        public Response membership(@PathParam("repository") final String repository);

        @PUT
        @Path("/repositories/{repository}/members")
        @Consumes({MediaType.TEXT_PLAIN})
        public Response addMember(final String memberid, @PathParam("repository") final String repository);

        @DELETE
        @Path("/repositories/{repository}/members/{memberid}")
        @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
        public Response deleteMember(@PathParam("memberid") final String memberId, @PathParam("repository") final String repository);

        @GET
        @Path("/repositories/{repository}/tags")
        @Produces({MediaType.APPLICATION_JSON})
        public Response tags(@PathParam("repository") final String repository);

        @GET
        @Path("/repositories/{repository}/tags/{tagName}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response getImageId(@PathParam("repository") final String repository, @PathParam("tagName") final String tagValue);

        @PUT
        @Path("/repositories/{repository}/images/{imageId}/tags")
        @Consumes({MediaType.APPLICATION_JSON})
        public Response addTag(@PathParam("repository") final String repository, @PathParam("imageId") final String imageId, final String tagValue);

        @DELETE
        @Path("/repositories/{repository}/tags/{tagName}")
        public Response deleteTag(@PathParam("repository") final String repository, @PathParam("tagName") final String tagValue);

        @GET
        @Path("/images/{imageId}/mirrors")
        @Produces({MediaType.APPLICATION_JSON})
        public Response mirrors(@PathParam("imageId") final String imageId);

        @PUT
        @Path("/images/{imageId}/mirrors")
        @Consumes({MediaType.APPLICATION_JSON})
        public Response putMirror(@PathParam("imageId") final String imageId, final String mirror);
    }

    @Path("/v1/images")
    public static interface GlanceV1ImageService
    {

        @GET
        @Produces({MediaType.APPLICATION_JSON})
        public Response images();

        @GET
        @Path("/details")
        @Produces({MediaType.APPLICATION_JSON})
        public Response detailImages();

        @HEAD
        @Path("/{imageId}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response imageMetaData(@PathParam("imageId") final String imageId);

        @GET
        @Path("/{imageId}")
        @Produces({MediaType.APPLICATION_OCTET_STREAM})
        public Response downloadImage(@PathParam("imageId") final String imageId);

        @POST
        @Consumes({MediaType.APPLICATION_OCTET_STREAM})
        @Produces({MediaType.APPLICATION_JSON})
        public Response uploadImage();

        @PUT
        @Path("/{imageId}")
        @Consumes({MediaType.APPLICATION_JSON})
        public Response image(@PathParam("imageId") final String imageId);

        @DELETE
        @Path("/{imageId}")
        public Response deleteImage(@PathParam("imageId") final String imageId);

    }

    @Path("/v2/images")
    public static interface GlanceV2
    {

        @GET
        @Produces({MediaType.APPLICATION_JSON})
        @Policy("get_images")
        public Response getImages(
                @QueryParam("limit") String limit,
                @QueryParam("marker") String marker,
                // Specifies the ID of the last-seen image. The typical pattern of limit and
                // marker is to make an initial limited request and then to use the ID of
                // the last image from the response as the marker parameter in a subsequent
                // limited request.
                @QueryParam("name") String name,
                @QueryParam("visibility") String visibility, // public private shared
                @QueryParam("member_status") String memberStatus, //accepted, pending, rejected,
                // and all. Default is accepted
                @QueryParam("owner") String owner, // tenant id
                @QueryParam("status") String status, // The image status, such as queued, saving, active,
                // killed, deleted, and pending_delete.
                @QueryParam("size_min") String sizeMin,
                @QueryParam("size_max") String sizeMax,
                @QueryParam("sort_key") String sortKey, //Default is created_at.
                @QueryParam("sort_dir") String sortDir, //"asc", "desc" (default)
                @QueryParam("tag") String tag);

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces({MediaType.APPLICATION_JSON})
        @Policy("add_image")
        public Response createImage(Image image);

        @GET
        @Path("/{imageId}")
        @Produces({MediaType.APPLICATION_JSON})
        @Policy("get_image")
        public Response getImageDetails(@PathParam("imageId") final String imageId);

        @PATCH
        @Path("/{imageId}")
        @Consumes("application/openstack-images-v2.1-json-patch")
        @Produces({MediaType.APPLICATION_JSON})
        @Policy("modify_image")
        public Response patchImage(@PathParam("imageId") final String imageId, String image);

        @DELETE
        @Path("/{imageId}")
        @Policy("delete_image")
        public Response delete(@PathParam("imageId") final String imageId);

        @GET
        @Path("/{imageId}/file")
        @Policy("download_image")
        @Produces({MediaType.APPLICATION_OCTET_STREAM})
        public Response downloadImage(@PathParam("imageId") final String imageId);

        @PUT
        @Path("/{imageId}/file")
        @Consumes({MediaType.APPLICATION_OCTET_STREAM})
        @Policy("upload_image")
        public Response uploadImage(final InputStream imageData, @PathParam("imageId") final String imageId, @HeaderParam("Content-Length") final long length);

        @PUT
        @Path("/{imageId}/tags/{tagValue}")
        public Response addTag(@PathParam("imageId") final String imageId, @PathParam("tagValue") final String tagValue);

        @DELETE
        @Path("/{imageId}/tags/{tagValue}")
        public Response deleteTag(@PathParam("imageId") final String imageId, @PathParam("tagValue") final String tagValue);

        @GET
        @Path("/{imageId}/members")
        @Produces({MediaType.APPLICATION_JSON})
        @Policy("get_members")
        public Response membership(@PathParam("imageId") final String imageId);

        @POST
        @Path("/{imageId}/members")
        @Consumes({MediaType.APPLICATION_JSON})
        @Produces({MediaType.APPLICATION_JSON})
        @Policy("add_member")
        public Response addMember(@PathParam("imageId") final String imageId, final Map<String, String> memberId);

        @GET
        @Path("/{imageId}/members/{id}")
        @Produces({MediaType.APPLICATION_JSON})
        @Policy("get_member")
        public Response memberDetails(@PathParam("imageId") final String imageId, @PathParam("id") final String id);

        @PUT
        @Path("/{imageId}/members/{id}")
        @Consumes({MediaType.APPLICATION_JSON})
        @Produces({MediaType.APPLICATION_JSON})
        @Policy("modify_member")
        public Response updateMember(@PathParam("imageId") final String imageId, @PathParam("id") final String memberId, final Map<String, String> status);

        @DELETE
        @Path("/{imageId}/members/{id}")
        @Policy("delete_member")
        public Response deleteMember(@PathParam("imageId") final String imageId, @PathParam("id") final String id);
    }

    @Path("/v2/tasks")
    public static interface GlanceV2Tasks
    {

        @GET
        @Produces({MediaType.APPLICATION_JSON})
        public Response tasks(
                @QueryParam("type") String type,//type of task
                @QueryParam("status") String status, //status of task
                @QueryParam("sort_dir") String sortDir //"asc", "desc" (default)
                );

        @POST
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes({MediaType.APPLICATION_JSON})
        public Response createTask(final com.emc.caspian.ccs.imagerepo.model.Task task, @Context UriInfo uriInfo);

        @GET
        @Path("/{taskId}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response task(@PathParam("taskId") final String taskId);

        @DELETE
        @Path("/{taskId}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response deleteTask(@PathParam("taskId") final String taskId);
    }

    @Path("/v2/schemas")
    public static interface GlanceV2Schema
    {

        /**
         * Gets a json-schema document that represents an images entity.
         *
         * @return
         */
        @GET
        @Path("/images")
        @Produces({MediaType.APPLICATION_JSON})
        public Response images();

        /**
         * Gets a json-schema document that represents an image entity.
         *
         * @return
         */
        @GET
        @Path("/image")
        @Produces({MediaType.APPLICATION_JSON})
        public Response image();

        /**
         * Gets a json-schema document that represents an image members entity. (Since Images v2.1.)
         *
         * @return
         */
        @GET
        @Path("/members")
        @Produces({MediaType.APPLICATION_JSON})
        public Response members();

        /**
         * Gets a json-schema document that represents an image members entity. (Since Images v2.1.)
         *
         * @return
         */
        @GET
        @Path("/member")
        @Produces({MediaType.APPLICATION_JSON})
        public Response member();

        /**
         * Gets a json-schema document that represents an tasks entity.
         *
         * @return
         */
        @GET
        @Path("/tasks")
        @Produces({MediaType.APPLICATION_JSON})
        public Response tasks();

        /**
         * Gets a json-schema document that represents an task entity.
         *
         * @return
         */
        @GET
        @Path("/task")
        @Produces({MediaType.APPLICATION_JSON})
        public Response task();

        /**
         * Gets a json-schema document that represents an entity. The entity includes namespace, namespaces, resource_type, resource_types, property, properties, object, objects, tag, tags
         *
         * @return
         */
        @GET
        @Path("/metadefs/{entity}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response entity(@PathParam("entity") final String entity);
    }

    @Path("/v2/metadefs")
    public static interface GlanceV2Catalog
    {
        /**
         * Get list of available namespaces or all resource_types Supported URL: /v2/metadefs/resource_types and /v2/metadefs/namespaces
         *
         * @param entity
         *
         * @return
         */
        @GET
        @Path("/{entity}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response fetchResourceNamespaces(@PathParam("entity") final String entity);

        /**
         * Create a Namespace.
         *
         * @param namespace
         *
         * @return
         */
        @POST
        @Path("/namespaces")
        @Produces({MediaType.APPLICATION_JSON})
        public Response createNamespace(final String namespace);

        /**
         * Fetch details about a given namespace.
         *
         * @param namespaceId
         *
         * @return
         */
        @GET
        @Path("/namespaces/{namespace}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response namespace(@PathParam("namespace") final String namespaceId);

        /**
         * Update and existing Namespace
         *
         * @param namespace
         * @param namespaceId
         *
         * @return
         */
        @PUT
        @Path("/namespaces/{namespace}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response updateNamespace(final String namespace, @PathParam("namespace") final String namespaceId);

        @DELETE
        @Path("/namespaces/{namespace}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response deleteNamespace(@PathParam("namespace") final String namespaceId);

        /**
         * Fetch Details of Objects, properties, resource_types and tags for a given namespace. URLS supported: /v2/metadefs/namespaces/{namespace}/objects
         * /v2/metadefs/namespaces/{namespace}/properties /v2/metadefs/namespaces/{namespace}/resource_types /v2/metadefs/namespaces/{namespace}/tags
         *
         * @param namespaceId
         * @param entityType
         *
         * @return
         */
        @GET
        @Path("/namespaces/{namespace}/{entityType}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response entities(@PathParam("namespace") final String namespaceId,
                                 @PathParam("entityType") final String entityType);

        /**
         * Create an Object, property, resource_type or tag for a given namespace. URLS supported: /v2/metadefs/namespaces/{namespace}/objects /v2/metadefs/namespaces/{namespace}/properties
         * /v2/metadefs/namespaces/{namespace}/resource_types /v2/metadefs/namespaces/{namespace}/tags TODO: For a POST we might need to create separate methods to manage property, object
         * resource_type and tag creation.
         *
         * @param namespaceId
         * @param entity
         *
         * @return
         */
        @POST
        @Path("/namespaces/{namespace}/{entityType}")
        @Consumes({MediaType.APPLICATION_JSON})
        public Response createEntity(final String entity, @PathParam("namespace") final String namespaceId,
                                     @PathParam("entityType") final String entityType);

        /**
         * Fetch details of a given object, property or tag in a given namespace. URLs Supported: /v2/metadefs/namespaces/{namespace}/objects/{object_name}
         * /v2/metadefs/namespaces/{namespace}/properties/{property_name} /v2/metadefs/namespaces/{namespace}/tags/{tag_name}
         *
         * @param namespaceId
         * @param entityType
         * @param entityName
         *
         * @return
         */
        @GET
        @Path("/namespaces/{namespace}/{entityType}/{entityName}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response entity(@PathParam("namespace") final String namespaceId,
                               @PathParam("entityType") final String entityType, @PathParam("entityName") final String entityName);

        /**
         * Delete a given object, property, resource_type or tag in a given namespace. URLs Supported: /v2/metadefs/namespaces/{namespace}/objects/{object_name}
         * /v2/metadefs/namespaces/{namespace}/properties/{property_name} /v2/metadefs/namespaces/{namespace}/tags/{tag_name} /v2/metadefs/namespaces/{namespace}/resource_types/{resource_type}
         *
         * @param namespaceId
         * @param entityType
         * @param entityName
         *
         * @return
         */
        @DELETE
        @Path("/namespaces/{namespace}/{entityType}/{entityName}")
        @Produces({MediaType.APPLICATION_JSON})
        public Response deleteEntity(@PathParam("namespace") final String namespaceId,
                                     @PathParam("entityType") final String entityType, @PathParam("entityName") final String entityName);

        /**
         * Modify a given object, property or tag in a given namespace. URLs Supported: /v2/metadefs/namespaces/{namespace}/objects/{object_name}
         * /v2/metadefs/namespaces/{namespace}/properties/{property_name} /v2/metadefs/namespaces/{namespace}/tags/{tag_name}
         * <p/>
         * TODO: Based on the model defnition this might need to be split into three methods.
         *
         * @param entity
         * @param namespaceId
         * @param entityType
         * @param entityName
         *
         * @return
         */
        @PUT
        @Path("/namespaces/{namespace}/{entityType}/{entityName}")
        @Consumes({MediaType.APPLICATION_JSON})
        public Response modifyEntity(final String entity, @PathParam("namespace") final String namespaceId,
                                     @PathParam("entityType") final String entityType, @PathParam("entityName") final String entityName);
    }
}