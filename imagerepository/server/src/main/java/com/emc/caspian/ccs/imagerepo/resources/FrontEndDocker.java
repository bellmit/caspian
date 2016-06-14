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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.common.utils.ImageStoreHelper;
import com.emc.caspian.ccs.imagerepo.FrontEndHelper;
import com.emc.caspian.ccs.imagerepo.HandleRequestDelegate;
import com.emc.caspian.ccs.imagerepo.TransformDelegate;
import com.emc.caspian.ccs.imagerepo.api.ApiV1;
import com.emc.caspian.ccs.imagerepo.api.datamodel.DockerImage;
import com.emc.caspian.ccs.imagerepo.api.datamodel.DockerRepoEntry;
import com.emc.caspian.ccs.imagerepo.api.datamodel.DockerRepository;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.Status;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreConfig;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.ccs.registry.Registry;
import com.emc.caspian.fabric.lang.Sequence;
import com.emc.caspian.fabric.lang.SequenceHelper;
import com.emc.caspian.fabric.util.Validate;
import com.google.common.io.ByteStreams;



/**
 * This class is used to implement APIs that are optimized for storing and
 * retrieving docker images for ECI Workloads.
 *
 * @author shivat
 *
 */
public final class FrontEndDocker extends BaseResource implements ApiV1.Docker {

    private static final Logger _log = LoggerFactory.getLogger(FrontEndDocker.class);
    private static final ImageStore imageStore = ImageStoreFactory.getImageStore();

    private static final String X_DOCKER_TOKEN_HEADER = "X-Docker-Token";
    private static final String X_DOCKER_ENDPOINTS_HEADER = "X-Docker-Endpoints";
    private static final String X_DOCKER_CHECKSUM = "X-Docker-Checksum";

    @Override
    public Response images(final String repositoryName) {

        final Protocol.DockerRepositoryRequest request = new Protocol.DockerRepositoryRequest();
        request.setRepositoryName(repositoryName);

        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.DockerResponse>()
                {
                    @Override
                    public Protocol.DockerResponse process() throws Exception {

                        Validate.isNotNullOrEmpty(repositoryName, "repository name");
                        _log.info("Retrive map of images to tags in the repository: {}", repositoryName);

                        final Protocol.DockerResponse repoResponse = new Protocol.DockerResponse();
                        DockerRepository repository = Registry.getDockerRespositoryDetails(request).get();
                        //If the DB fetch fails it implies there is no repository with requested name
                        Validate.isNotNull(repository.getId(), "repository name");

                        //TODO don't save metadata file, instead retrieve image:tags mapping from DB
                        URL filePath = repository.getMetaDataFilePath();
                        final String imageJson = imageStore.getECIImageMetadata(filePath).get();

                        if(StringUtils.isEmpty(imageJson)) {
                            throw new FileNotFoundException();
                        }
                        repoResponse.setJsonString(imageJson);
                        repoResponse.setStatus(Protocol.Status.OK);
                        return repoResponse;
                    }
                },
                new TransformDelegate<Protocol.DockerResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.DockerResponse response) {
                        String jsonFile = response.getJsonString();
                      //TODO must return map of image:tags
                        return Response.ok().entity(jsonFile);
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response createRepository(final String repositoryMetaData, final String repositoryName) {

        final DockerRepository dockerRepository = new DockerRepository(repositoryName);

        //TODO Added dummy member here.It need to replace the member with the actual member id, who creates this repository.
        Set<String> members = new HashSet<String>();
        members.add("Default-Member");

        dockerRepository.setMembers(members);

        final Protocol.DockerRepositoryRequest request = new Protocol.DockerRepositoryRequest().setRepository(dockerRepository);

        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.Response process() throws Exception {

                        Validate.isNotNullOrEmpty(repositoryName, "repository name");
                        _log.info("Create Repository metadata file. Repository Name: {}", repositoryName);

                        dockerRepository.setRepositoryMap(JsonHelper.deserializeFromJson(repositoryMetaData, HashMap.class));

                        //update the repository metadata details in mysql store
                        Protocol.DockerRepositoryResponse protocolResponse = Registry.createRepository(request).get();
                        //TODO don't save metadata file, instead retrieve image:tags mapping from DB
                        URL repositoryFileLocation = imageStore.saveECIImageMetadataFile(repositoryName, repositoryMetaData).get();

                        DockerRepository repository = protocolResponse.getRepository();
                        repository.setMetaDataFilePath(repositoryFileLocation);
                        request.setRepository(repository);

                        protocolResponse = Registry.updateDockerRepository(request).get();
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.Response>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.Response response) {
                        //TODO check if docker public registry returns the repo metadata file's location url
                        return Response.ok();
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response createImage(final String imageMetadata, final String imageId) {

        final Protocol.DockerImageRequest request = new Protocol.DockerImageRequest();

        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.DockerImageResponse process() throws Exception {

                        Validate.isNotNullOrEmpty(imageId, "imageId");
                        _log.info("Add Image meta data for the image ID: {}", imageId);

                        request.setImage(contructDockerImageFromMetaDataFile(imageMetadata));
                        request.setImageId(imageId);

                        //update the image metadata details in mysql store
                        Protocol.DockerImageResponse protocolResponse = Registry.createDockerImage(request).get();

                        URL imageMetaDataFileLocation = imageStore.
                                saveECIImageMetadataFile(imageId, imageMetadata).get();

                        DockerImage dockerImage = protocolResponse.getImage();
                        dockerImage.setId(imageId);
                        dockerImage.setMetaDataFilePath(imageMetaDataFileLocation);
                        request.setImage(dockerImage);

                        protocolResponse = Registry.updateDockerImageDetails(request).get();
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.Response>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.Response response) {
                        return Response.ok();
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    public static DockerImage contructDockerImageFromMetaDataFile(String imageJsonMap) {
        HashMap<String, Object> imageMap = JsonHelper.deserializeFromJson(imageJsonMap, HashMap.class);
        DockerImage dockerImage = new DockerImage();

        for (Map.Entry<String,Object> e :imageMap.entrySet()) {
            switch(e.getKey()) {
            case "id" :
                dockerImage.setId(e.getValue().toString());
                break;
            case "parent" :
                dockerImage.setParentId(e.getValue().toString());
                break;
            case "created" :
                dockerImage.setCreatedAt(e.getValue().toString());
                break;
            case "Size" :
                e.getValue();
                Long virtualSize = (Long)e.getValue();
                dockerImage.setVirtualSize(virtualSize.longValue());
                break;
            }
        }
        return dockerImage;
    }

    @Override
    public Response image(final String imageId) {
        final Protocol.DockerImageRequest request = new Protocol.DockerImageRequest().setImageId(imageId);
        final Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.DockerResponse>()
                {
                    @Override
                    public Protocol.DockerResponse process() throws Exception {

                        Validate.isNotNullOrEmpty(imageId, "imageId");
                        _log.info("Retrive image information for imageID: {}", imageId);

                        final Protocol.DockerResponse protocolResponse = new Protocol.DockerResponse();
                        final String imageJson;
                        DockerImage dockerImage = Registry.getDockerImageDetails(request).get();
                        //If the DB fetch fails it implies there is no image with requested name
                        Validate.isNotNull(dockerImage.getId(), "image id");

                        URL filePath = dockerImage.getMetaDataFilePath();
                        imageJson = imageStore.getECIImageMetadata(filePath).get();

                        if(StringUtils.isEmpty(imageJson)) {
                            //TODO when saved in DB this exception needs to be changed
                            throw new FileNotFoundException();
                        }
                        protocolResponse.setJsonString(imageJson);
                        protocolResponse.setStatus(Status.OK);
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.DockerResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.DockerResponse response) {
                        return Response.ok().entity(response.getJsonString());
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response uploadImage(final InputStream imageData, final String imageId, final long length) {
        final KeystonePrincipal principal = getKeystonePrincipal();
        final Protocol.DockerImageRequest request = new Protocol.DockerImageRequest().setImageId(imageId);
        final Response response = FrontEndHelper.handleRequest(
                request,
                principal,
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.Response process() throws Exception {

                        Validate.isNotNull(imageData, "image input stream");
                        Validate.isNotNullOrEmpty(imageId, "imageId");
                        _log.info("Upload Image data for imageId: {}", imageId);

                        DockerImage dockerImage = Registry.getDockerImageDetails(request).get();
                        Registry.checkUserQuota(principal, dockerImage.getVirtualSize());

                        long imageSize;
                        InputStream dataStream;
                        if(length != 0){
                            imageSize = length;
                            dataStream = imageData;
                        } else {
                            ImmutablePair<Long, InputStream> pairResponse = ImageStoreHelper.getStreamSize(imageData, ImageStoreConfig.ObjectConfig.tempDir.value());
                            imageSize = pairResponse.getLeft();
                            dataStream = pairResponse.getRight();
                        }

                        Validate.isGreater(imageSize, 0);

                        //TODO check if the parent layer is already uploaded by checking for flag say "isUploaded"
                        //copy the value of isUploaded flag from parent to child
                        //Later once the parent layer is marked upload complete, propogate this change to all its children
                        // With this when one needs to download ancestry of images, they would traverse up the tree till isUploaded is 
                        // marked false.
                        URL imageFileLocation = imageStore.saveECIImageFile(imageId, dataStream, imageSize).get();
                        _log.debug("Uploaded Image data for imageId: {} at location: {}", imageId, imageFileLocation.toString());

                        //case when image json metadata is not yet uploaded
                        if(dockerImage.getId() == null) {
                            dockerImage.setId(imageId);
                        }

                        dockerImage.setLocation(imageFileLocation);
                        request.setImage(dockerImage);

                        Protocol.Response protocolResponse = Registry.updateDockerImageDetails(request).get();
                        protocolResponse.setStatus(Protocol.Status.OK);
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.Response>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.Response response) {
                        return Response.ok();
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response downloadImage(final String imageId) {

        //TODO Need to replace this member id with actual logged in member id.
        String memberid="Default-Member";
        if(!memberAccessCheck(memberid, imageId)){
            throw new RuntimeException("Logged in member does not have access to this image");
        }
        final Protocol.DockerImageRequest request = new Protocol.DockerImageRequest().setImageId(imageId);
        final Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.ImageFileResponse>()
                {
                    @Override
                    public Protocol.ImageFileResponse process() throws Exception {
                        Validate.isNotNullOrEmpty(imageId, "imageId");
                        _log.info("Download binary Image for imageID: {}", imageId);

                        final Protocol.ImageFileResponse protocolResponse = new Protocol.ImageFileResponse();

                        DockerImage dockerImage = Registry.getDockerImageDetails(request).get();
                        //If there is no image entity with specified imageId, then get on registry would return empty entity object
                        Validate.isNotNullOrEmpty(dockerImage.getId(), "imageId");
                        // TODO: add md5 checksum in the header!
                        /*
                            If image data exists, you receive the HTTP 200 status code.
                            If no image data exists, you receive the HTTP 204 status code.
                         */
                        URL location = dockerImage.getLocation();
                        _log.debug("Downloading Image data for imageId: {} from location: {}", imageId, location.toString());
                        //TODO need to test downloading image of large size and also test when the latency is high
                        InputStream imageFileStream = imageStore.getECIImageFile(location).get();
                        if(imageFileStream == null) {
                            throw new FileNotFoundException();
                        }
                        protocolResponse.setImageFile(imageFileStream);
                        protocolResponse.setStatus(Protocol.Status.OK);
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.ImageFileResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.ImageFileResponse response) {
                        final StreamingOutput streamOutput = new StreamingOutput()
                        {
                            @Override
                            public void write(OutputStream os) throws IOException, WebApplicationException {
                                try {
                                    ByteStreams.copy(response.getImageFile(), os);
                                } finally {
                                    response.getImageFile().close();
                                }
                            }
                        };
                        return Response.ok().entity(streamOutput);
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response imageAncestry(final String imageId) {

        final Protocol.DockerImageRequest request = new Protocol.DockerImageRequest().setImageId(imageId);
        final Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.DockerResponse>()
                {
                    @Override
                    public Protocol.DockerResponse process() {
                        Validate.isNotNullOrEmpty(imageId, "imageId");
                        _log.info("Return a list of image ids which are ancestors of a given imageId: {}", imageId);

                        final Protocol.DockerResponse protocolResponse = new Protocol.DockerResponse();

                        Sequence<String> ancestrySequence = Registry.getImageAncestry(request);
                        List<String> ancestryList = SequenceHelper.makeList(ancestrySequence);

                        protocolResponse.setJsonList(ancestryList);
                        protocolResponse.setStatus(Status.OK);
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.DockerResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.DockerResponse response) {
                        return Response.ok().entity(response.getJsonList());
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response membership(final String repositoryName) {

        final Protocol.DockerRepositoryRequest request = new Protocol.DockerRepositoryRequest().setRepositoryName(repositoryName);

        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.DockerRepositoryResponse>()
                {
                    @Override
                    public Protocol.DockerRepositoryResponse process() throws Exception {
                        Validate.isNotNullOrEmpty(repositoryName, "repository");
                        _log.info("List out members in a repository: {}", repositoryName);

                        final Protocol.DockerRepositoryResponse repoResponse = new Protocol.DockerRepositoryResponse();
                        DockerRepository dockerRepo = Registry.getDockerRespositoryDetails(request).get();

                        //TODO need to handle 401 and 404 error scenarios and add respective unit test cases
                        repoResponse.setMembers(dockerRepo.getMembers());
                        repoResponse.setStatus(Protocol.Status.OK);
                        return repoResponse;
                    }
                },
                new TransformDelegate<Protocol.DockerRepositoryResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.DockerRepositoryResponse response) {
                        Set<String> members = response.getMembers();
                        return Response.ok().entity(members);
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response addMember(final String memberId, final String repositoryName) {
        final Protocol.DockerRepositoryRequest request = new Protocol.DockerRepositoryRequest().setRepositoryName(repositoryName);
        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.Response process() throws Exception {
                        Validate.isNotNullOrEmpty(memberId, "memberId");
                        Validate.isNotNullOrEmpty(repositoryName, "repository");
                        _log.info("Add a new member to a repository: {}", repositoryName);

                        DockerRepository dockerRepo = Registry.getDockerRespositoryDetails(request).get();
                        dockerRepo.getMembers().add(memberId);
                        dockerRepo.setEntityType(EntityType.DOCKER_REPOSITORY);
                        dockerRepo.setName(repositoryName);
                        dockerRepo.setId(repositoryName);

                        Map<String,String> tagsToImageIdMap = dockerRepo.getRepositoryMap();
                        request.setRepository(dockerRepo);
                        Protocol.Response protocolResponse = Registry.updateDockerRepository(request).get();
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.Response>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.Response response) {
                        return Response.ok();
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response deleteMember(final String memberId, final String repositoryName) {

        final Protocol.DockerRepositoryRequest request = new Protocol.DockerRepositoryRequest().setRepositoryName(repositoryName);

        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.Response process() throws Exception {
                        Validate.isNotNullOrEmpty(memberId, "memberId");
                        Validate.isNotNullOrEmpty(repositoryName, "repository");
                        _log.info("Remove a member from the repository: {}", repositoryName);

                        DockerRepository dockerRepo = Registry.getDockerRespositoryDetails(request).get();
                        Set<String> members = dockerRepo.getMembers();

                        if(members.contains(memberId)) {
                            dockerRepo.getMembers().remove(memberId);
                        } else{
                            throw new RuntimeException("Member id does not exist " + memberId);
                        }

                        dockerRepo.setEntityType(EntityType.DOCKER_REPOSITORY);
                        request.setRepository(dockerRepo);
                        Protocol.Response protocolResponse = Registry.updateDockerRepository(request).get();
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.Response>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.Response response) {
                        return Response.ok();
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response tags(final String repositoryName) {

        final Protocol.DockerRepositoryRequest request = new Protocol.DockerRepositoryRequest().setRepositoryName(repositoryName);
        final Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.DockerResponse>()
                {
                    @Override
                    public Protocol.DockerResponse process() throws Exception {
                        Validate.isNotNullOrEmpty(repositoryName, "repository name");
                        _log.info("Retrive map of tags to image-id present in the repository: {}", repositoryName);

                        Protocol.DockerResponse protocolResponse = new Protocol.DockerResponse();
                        DockerRepository repository = Registry.getDockerRespositoryDetails(request).get();
                        //If the DB fetch fails it implies there is no repository with requested name
                        Validate.isNotNull(repository.getId(), "repository name");

                        protocolResponse.setJsonMap(repository.getRepositoryMap());
                        protocolResponse.setStatus(Status.OK);
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.DockerResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.DockerResponse response) {
                        // returns map of tag:imageId
                        return Response.ok().entity(response.getJsonMap());
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response getImageId(final String repositoryName, final String tagName) {

        final Protocol.DockerRepoEntryRequest request = new Protocol.DockerRepoEntryRequest(repositoryName, tagName);
        final Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.DockerResponse>()
                {
                    @Override
                    public Protocol.DockerResponse process() throws Exception {
                        Validate.isNotNullOrEmpty(repositoryName, "repository name");
                        Validate.isNotNullOrEmpty(tagName, "tag name");
                        _log.info("Retrive imageId having tag name {} in the repository: {}", tagName, repositoryName);

                        Protocol.DockerResponse protocolResponse = new Protocol.DockerResponse();
                        DockerRepoEntry repositoryEntry = Registry.getDockerRepoEntryDetails(request).get();
                        //If the DB fetch fails it implies there is no repository with requested name
                        Validate.isNotNull(repositoryEntry.getId(), "repository entry name");

                        protocolResponse.setJsonString(repositoryEntry.getImageGUID());

                        protocolResponse.setStatus(Status.OK);
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.DockerResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.DockerResponse response) {
                        return Response.ok().entity(response.getJsonString());
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response addTag(final String repositoryName, final String imageId, final String tagName) {

        final Protocol.DockerRepositoryRequest request = new Protocol.DockerRepositoryRequest().setRepositoryName(repositoryName);
        final Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.DockerRepositoryResponse>()
                {
                    @Override
                    public Protocol.DockerRepositoryResponse process() throws Exception {
                        Validate.isNotNullOrEmpty(repositoryName, "repository name");
                        Validate.isNotNullOrEmpty(imageId, "image Id");
                        Validate.isNotNullOrEmpty(tagName, "tag name");
                        _log.info("Adding tag {} for imageId {} in the repository: {}", new Object[]{tagName, imageId, repositoryName});

                        Protocol.DockerRepositoryResponse protocolResponse = new Protocol.DockerRepositoryResponse();

                        DockerRepository repository = Registry.getDockerRespositoryDetails(request).get();
                        //If the DB fetch fails it implies there is no repository with requested name
                        Validate.isNotNull(repository.getId(), "repository name");

                        //TODO check why EntityType is not being set in getDockerRespositoryDetails()
                        repository.setEntityType(EntityType.DOCKER_REPOSITORY);

                        Map<String,String> tagsToImageIdMap = repository.getRepositoryMap();
                        tagsToImageIdMap.put(tagName, imageId);
                        request.setRepository(repository);

                        //Adds new DOCKER_REPO_ENTRY and updates the DOCKER_REPOSITORY's repoMap with tag:image-id details
                        protocolResponse = Registry.updateDockerRepository(request).get();
                        protocolResponse.setStatus(Status.OK);
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.DockerRepositoryResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.DockerRepositoryResponse response) {
                        return Response.ok();
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response deleteTag(String repository, String tagValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Response mirrors(final String imageId) {

        final Protocol.DockerImageRequest request = new Protocol.DockerImageRequest().setImageId(imageId);
        final Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.DockerResponse>()
                {
                    @Override
                    public Protocol.DockerResponse process() throws Exception {
                        Validate.isNotNullOrEmpty(imageId, "imageId");
                        _log.info("Return a list of mirror locations in the cluster for a given imageId: {}", imageId);

                        Protocol.DockerResponse protocolResponse = new Protocol.DockerResponse();
                        DockerImage dockerImage = Registry.getDockerImageDetails(request).get();
                        //If the DB fetch fails it implies there is no image with requested name
                        Validate.isNotNull(dockerImage.getId(), "image id");
                        List<String> mirrors = dockerImage.getMirrors();
                        protocolResponse.setJsonList(mirrors);
                        protocolResponse.setStatus(Status.OK);
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.DockerResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.DockerResponse response) {
                        return Response.ok().entity(response.getJsonList());
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    @Override
    public Response putMirror(final String imageId, final String mirror) {

        final Protocol.DockerImageRequest request = new Protocol.DockerImageRequest().setImageId(imageId);

        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.Response process() throws InterruptedException, ExecutionException {
                        Protocol.Response protocolResponse;
                        Validate.isNotNullOrEmpty(imageId, "image id");
                        Validate.isNotNullOrEmpty(mirror, "mirror location");
                        _log.info("Put image mirror location {} for the image ID: {}", mirror, imageId);

                        DockerImage dockerImage = Registry.getDockerImageDetails(request).get();
                        List<String> mirrorLocations = dockerImage.getMirrors();
                        mirrorLocations.add(mirror);
                        request.setImage(dockerImage);
                        Future<Protocol.DockerImageResponse> response = Registry.updateDockerImageDetails(request);
                        protocolResponse = response.get();
                        return protocolResponse;
                    }
                },
                new TransformDelegate<Protocol.Response>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.Response response) {
                        return Response.ok();
                    }
                }, Registry.fetchExceptionMapper(), FrontEndHelper.getDefaultExceptionMapper());

        return response;
    }

    private boolean memberAccessCheck(final String memberid, String imageId) {

        final Protocol.DockerImageRequest request = new Protocol.DockerImageRequest().setImageId(imageId);

        try {
            Iterable<DockerRepoEntry> listDockerRepoEntry = Registry.getDockerRepoEntryDetailsForImageId(request).get();
            //Iterating all DockerRepo to verify member has access.
            for (DockerRepoEntry dockerRepo:listDockerRepoEntry){
                _log.debug("Repository : " + dockerRepo.getRepositoryName());
                Response memResponse = membership(dockerRepo.getRepositoryName());
                Set members = (Set) memResponse.getEntity();
                if(members.contains(memberid)){
                    return true;
                }
            }
        } catch (Exception e) {
            _log.error("Failed to get repository metadata map", e);
            throw new RuntimeException(e);
        }
        return false;
    }
}
