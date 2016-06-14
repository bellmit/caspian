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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import jersey.repackaged.com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.datastore.DataStoreFactory;
import com.emc.caspian.ccs.datastore.DataStoreType;
import com.emc.caspian.ccs.datastore.mysql.MySqlStore;
import com.emc.caspian.ccs.imagerepo.FrontEndHelper;
import com.emc.caspian.ccs.imagerepo.HandleRequestDelegate;
import com.emc.caspian.ccs.imagerepo.TransformDelegate;
import com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2;
import com.emc.caspian.ccs.imagerepo.api.ModelHelper;
import com.emc.caspian.ccs.imagerepo.api.datamodel.EntityType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Member;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image.ContainerFormat;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image.DiskFormat;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Image.Visibility;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.CreateImageRequest;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.ImageRequestBase;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.MemberRequest;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.Request;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.RequestType;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.ImageResponse;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.Status;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol.UpdateImageRequest;
import com.emc.caspian.ccs.imagerepo.model.Images;
import com.emc.caspian.ccs.imagerepo.model.Members;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.ccs.imagestores.ImageStoreType;
import com.emc.caspian.ccs.registry.Registry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

/**
 * Implementation of Glance V2 service APIs.
 *
 * @author shrids, shivesh
 */
public final class FrontEndGlanceV2 extends BaseResource implements GlanceV2 {
    private static final Logger _log = LoggerFactory.getLogger(FrontEndGlanceV2.class);
    private static MySqlStore mysqlStore = (MySqlStore) DataStoreFactory.getImageStore(DataStoreType.MYSQL);
	private static final String member = "member";
	private static final String membersSchema = "/v2/schemas/members";
    private static ImageStore imageStore = ImageStoreFactory.getImageStore();
    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#getImages(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public Response getImages(final String limit, final String marker, final String name, final String visibility,
            final String memberStatus, final String owner, final String status, final String sizeMin, final String sizeMax,
            final String sortKey, final String sortDir, final String tag) {
        //TODO: check for limit --> its always 20 when passed through glance client

        final Protocol.ImagesRequest request = new Protocol.ImagesRequest().setMarker(marker).setMemberStatus(memberStatus)
                .setName(name).setVisibility(visibility).setOwner(owner).setSortKey(sortKey).setSortDir(sortDir).setTag(tag);
        if (!Strings.isNullOrEmpty(limit)) {
            request.setLimit(Integer.parseInt(limit));
        }

        if (!Strings.isNullOrEmpty(status)) {
            request.setStatus(Integer.parseInt(status));
        }

        if (!Strings.isNullOrEmpty(sizeMax)) {
            request.setSizeMax(Integer.parseInt(sizeMax));
        }

        if (!Strings.isNullOrEmpty(sizeMin)) {
            request.setSizeMin(Integer.parseInt(sizeMin));
        }

        Response response = FrontEndHelper.handleRequest(request, getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.ImagesResponse>() {
                    @Override
                    public Protocol.ImagesResponse process() {
                        // get registry
                        Future<Iterable<Image>> futuresList = Registry.getImages(request);

                        final Protocol.ImagesResponse imagesResponse = new Protocol.ImagesResponse();
                        try {
                            imagesResponse.setImages(futuresList.get());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return imagesResponse;
                    }
                }, new TransformDelegate<Protocol.ImagesResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.ImagesResponse response) {
                        Images images = new Images();
                        images.setImages(Lists.newArrayList(Iterables.transform(response.getImages(),
                                new Function<Image, com.emc.caspian.ccs.imagerepo.model.Image>() {
                                    @Override
                                    public com.emc.caspian.ccs.imagerepo.model.Image apply(final Image input) {
                                        return ModelHelper.encode(input);
                                    }
                                })));
                        images.setSchema("/v2/schemas/images");
                        images.setFirst("/v2/images");

                      //  images.setNext("/v2/images?limit=200&marker=");

                        return Response.ok().entity(images);
                    }
                });
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#createImage(java.lang.String, com.emc.caspian.ccs.imagerepo.model.Image)
     */
    @Override
    public Response createImage(com.emc.caspian.ccs.imagerepo.model.Image image) {
    	// we can also receive the store to upload the image to..

        // if not active then we need to set the status to queued , call addLocation(location, image)
        // else continue creating..
        // there could be a copy from info too.. to copy the image from the specified location
        // there could be a store option supplied as input too. need to see how that is presented..

    	//TODO add setDiskFormat(String) and setContainerFormat(String) to model image
    	//TODO initialize every string with empty string and not null. patch fix
        final Protocol.CreateImageRequest request = new Protocol.CreateImageRequest();
        Image decodedImage = ModelHelper.decode(image);
        request.setImage(decodedImage);

        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.ImageResponse>()
                {
                    @Override
                    public Protocol.ImageResponse process(){
                        // get registry
                    	populateDefaults(request);

                    	final Protocol.ImageResponse imageResponse = composeResponse(request);

                    	if (imageResponse.getStatus() == Status.OK){
                    		//TODO UPLOAD image if a local file is provided
                    		 Future<Image> futureImage = Registry.createImage(request);
	                        try {
	                        	Image image = futureImage.get();
	                            imageResponse.setStatus(Status.CREATED);
	                        } catch (Exception e) {
		                            throw new RuntimeException(e);
	                        }
                    	}
                    	return imageResponse;
                    }

                    private void populateDefaults(CreateImageRequest request) {
                    	// checks all the attributes and provides respective default values
                    	String ID= request.getImage().getId();
                    	String dateTime = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss").format(new Date());
                    	request.getImage().setCreatedAt(dateTime);

                    	if(ID == null){
                    		request.getImage().setId(UUID.randomUUID().toString());
                    	}

                    	String containerFormat = request.getImage().getContainerFormat();
                    	if(containerFormat == null){
                    		request.getImage().setContainerFormat(ContainerFormat.BARE);
                    	}

                    	String diskFormat = request.getImage().getDiskFormat();
                    	if(diskFormat == null){
                    		request.getImage().setDiskFormat(DiskFormat.RAW);
                    	}

                    	if(Float.floatToRawIntBits(request.getImage().getSize()) == 0 ){
                    		request.getImage().setSize(0);
                    	}

                		if(request.getImage().getMinDisk() == null){
                    		request.getImage().setMinDisk(0);
                		}

                		if(request.getImage().getMinRam() == null){
                    		request.getImage().setMinRam(0);
                    	}

                		if(request.getImage().getVisibility() == null){
                			request.getImage().setVisibility(Visibility.PRIVATE);
                		}

                		if(request.getImage().getProtected() == null){
                			request.getImage().setProtected(false);;
                		}


                		if(!request.getImage().getLocations().isEmpty()){
                			request.getImage().setDirectUrl(request.getImage().getLocations().get(0).toString());
                    	}
                    	//File attribute
                    	if(request.getImage().getFile() == null){
                    		request.getImage().setStatus(com.emc.caspian.ccs.imagerepo.model.Image.Status.QUEUED.toString());
                    	}
                    	else {
                    		//TODO set checksum , size
                    		request.getImage().setFile("/v2/images/"+request.getImage().getId()+"/file");
                    		request.getImage().setStatus(com.emc.caspian.ccs.imagerepo.model.Image.Status.ACTIVE.toString());
                    	}

                    	//TODO status check through schema
                	}

                },
                new TransformDelegate<Protocol.ImageResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.ImageResponse response) {
                    	com.emc.caspian.ccs.imagerepo.model.Image image = ModelHelper.encode(response.getImage());
                    	if(response.getStatus() == Status.CREATED){
                    		try {
                    			if(response.getImage().getLocations().isEmpty()){
	                        		return Response.created(null).entity(image);
	                        	}
	                        	else{
	                        		return Response.created(new java.net.URI(response.getImage().getLocations().get(0).toString())).entity(image);
	                        	}
	            			} catch (URISyntaxException e) {
	            				_log.error("URISyntaxException");	         
	            				e.printStackTrace();
	            			}
                    	}
                    	else {
                    		return Response.status(response.getStatus().value());
                    	}
						return null;
                    }
                },
                Registry.fetchExceptionMapper()
        );
       // _log.info("\n\nRESPONSE : \n" + response.getEntity().toString() + "\n");
        return response;
    }

    private ImageResponse composeResponse(ImageRequestBase request){
    	// checks all the attributes, its validity and provides respective response status
    	final Protocol.ImageResponse imageResponse = new Protocol.ImageResponse();
    	String dateTime = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss").format(new Date());
    	request.getImage().setUpdatedAt(dateTime);
    	imageResponse.setImage(request.getImage());
    	String ID= request.getImage().getId();
    	if(ID != null){
    		//ID is provided. Checking for the valid pattern of 8-4-4-4-8
    		final String ID_PATTERN=
    				"([a-z0-9]){8}-([a-z0-9]){4}-([a-z0-9]){4}-([a-z0-9]){4}-([a-z0-9]){12}";
    		Pattern pattern = Pattern.compile(ID_PATTERN);
    		Matcher matcher = pattern.matcher(ID);

    		if(!matcher.matches()){
    			//HTTP 400 error
    			imageResponse.setStatus(Status.BAD_REQUEST);
    			_log.info("Format of image ID is invalid (HTTP "+Status.BAD_REQUEST.value()+")");
    			return imageResponse;
    		}
    		else {
    			final Protocol.ImageRequest req = new Protocol.ImageRequest()
    												.setImageId(ID);
    			try {
					if (Registry.getImageDetails(req).get().getId() != null && request.getRequestType() == RequestType.CREATE_IMAGE_V2){
						//image already exists >> HTTP 409
						imageResponse.setStatus(Status.CONFLICT);
						_log.info("An image with identifier "+ID+" already exists (HTTP "+Status.CONFLICT.value()+")");
						return imageResponse;
					}
				} catch (Exception e) {
					_log.warn("Execution Exception");
					throw new RuntimeException(e);
				}
    		}
    	}

    	String containerFormat = request.getImage().getContainerFormat();
    	List supportedFormats = new ArrayList();
    	supportedFormats.add(ContainerFormat.BARE.toString());
    	supportedFormats.add(ContainerFormat.OVF.toString());
    	supportedFormats.add(ContainerFormat.AKI.toString());
    	supportedFormats.add(ContainerFormat.ARI.toString());
    	supportedFormats.add(ContainerFormat.AMI.toString());
    	supportedFormats.add(ContainerFormat.OVA.toString());

		if(!supportedFormats.contains(containerFormat)){
			imageResponse.setStatus(Status.BAD_REQUEST);
			_log.info("Invalid container format "+containerFormat+" for image. (HTTP "+Status.BAD_REQUEST.value()+")");
			return imageResponse;
    	}


    	String diskFormat = request.getImage().getDiskFormat();
    	List supportedDiskFormats = new ArrayList();
    	supportedDiskFormats.add(DiskFormat.RAW.toString());
    	supportedDiskFormats.add(DiskFormat.VMDK.toString());
    	supportedDiskFormats.add(DiskFormat.VDI.toString());
    	supportedDiskFormats.add(DiskFormat.ISO.toString());
    	supportedDiskFormats.add(DiskFormat.QCOW_2.toString());
    	supportedDiskFormats.add(DiskFormat.AKI.toString());
    	supportedDiskFormats.add(DiskFormat.ARI.toString());
    	supportedDiskFormats.add(DiskFormat.AMI.toString());

		if(!supportedDiskFormats.contains(diskFormat)){
			imageResponse.setStatus(Status.BAD_REQUEST);
			_log.info("Invalid disk format "+diskFormat+" for image. (HTTP "+Status.BAD_REQUEST.value()+")");
			return imageResponse;
    	}

		if(request.getImage().getSize() < 0){
			imageResponse.setStatus(Status.BAD_REQUEST);
			_log.info("Invalid value. Image size must be >= 0 ("+request.getImage().getSize()+" specified). (HTTP "+Status.BAD_REQUEST.value()+")");
			return imageResponse;
    	}

		if(request.getImage().getMinDisk() < 0){
			imageResponse.setStatus(Status.BAD_REQUEST);
			_log.info("Invalid value, must be >= 0 ("+request.getImage().getMinDisk()+" specified). (HTTP "+Status.BAD_REQUEST.value()+")");
			return imageResponse;
    	}

		if(request.getImage().getMinRam() < 0){
			imageResponse.setStatus(Status.BAD_REQUEST);
			_log.info("Invalid value, must be >= 0 ("+request.getImage().getMinRam()+" specified). (HTTP "+Status.BAD_REQUEST.value()+")");
			return imageResponse;
    	}

		if(!(request.getImage().getVisibility().equals(Visibility.PRIVATE.toString())
				|| request.getImage().getVisibility().equals(Visibility.PUBLIC.toString()))){
			imageResponse.setStatus(Status.BAD_REQUEST);
			_log.info("Invalid visibilty status. (HTTP "+Status.BAD_REQUEST.value()+")");
			return imageResponse;
		}
		return imageResponse;
	}

	@Override
    public Response patchImage(final String imageId, final String input) {
		JsonNode patchObject=null;
		try {
			patchObject = new ObjectMapper().readTree(input);
		} catch (Exception e) {
            throw new RuntimeException(e);
        }
        com.emc.caspian.ccs.imagerepo.model.Image image = (com.emc.caspian.ccs.imagerepo.model.Image) getImageDetails(imageId)
                .getEntity();
        com.emc.caspian.ccs.imagerepo.model.Image patchedImage = FrontEndHelper
                .<com.emc.caspian.ccs.imagerepo.model.Image> applyPatch(image, FrontEndHelper.getPatchObject(patchObject),
                        com.emc.caspian.ccs.imagerepo.model.Image.class);

        Image patchedProtocolImage = ModelHelper.decode(patchedImage);
        final Protocol.UpdateImageRequest request = (UpdateImageRequest) new Protocol.UpdateImageRequest().setImage(patchedProtocolImage);
        Response response = FrontEndHelper.handleRequest(request, getKeystonePrincipal(), new HandleRequestDelegate<Protocol.ImageResponse>() {
            @Override
            public Protocol.ImageResponse process() {

                // get registry
                
                final Protocol.ImageResponse imageResponse = composeResponse(request);

            	if (imageResponse.getStatus() == Status.OK){
            		//TODO UPLOAD image if a local file is provided
            		String updatedAt = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss").format(new Date());
            		request.getImage().setUpdatedAt(updatedAt);
            		Future<Image> futureImage = Registry.updateImageDetails(request);
            		try {
                        imageResponse.setImage(futureImage.get());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
            	}
                return imageResponse;
            }
        }, new TransformDelegate<Protocol.ImageResponse>() {
            @Override
            public ResponseBuilder transform(final Protocol.ImageResponse response) {
                if(response.getStatus() == Status.OK){
                	return Response.ok().entity(response.getImage());
                }
                else{
                	return Response.status(response.getStatus().value());
                }
            }
        });
        return response;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#image(java.lang.String ,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response getImageDetails(final String imageId) {

        final Protocol.ImageRequest request = new Protocol.ImageRequest().setImageId(imageId);
        Response response = FrontEndHelper.handleRequest(request, getKeystonePrincipal(), new HandleRequestDelegate<Protocol.ImageResponse>() {
            @Override
            public Protocol.ImageResponse process() {
                // get registry
            	Future<Image> futures;
            	boolean searchingName=false;
                futures = Registry.getImageDetails(request);
                try {
					if(futures.get().getId() == null){
						searchingName=true;
						futures = Registry.getImageDetailsByName(request);
					}
	                final Protocol.ImageResponse imageResponse = new Protocol.ImageResponse();
	                imageResponse.setImage(futures.get());
	                if(futures.get() != null){
		                if (futures.get().getId() != null)
		                {
		                	imageResponse.setStatus(Status.OK);
		                	if (searchingName && imageResponse.getImage().getVisibility() != null && imageResponse.getImage().getVisibility().equals("private")){
		                		imageResponse.setStatus(Status.FORBIDDEN);
		                    }
		                }
		                else {
		                	imageResponse.setStatus(Status.BAD_REQUEST);
		                }
	                }
	                else{
	                	_log.info("there are more  than one image with same name");
	                	imageResponse.setStatus(Status.BAD_REQUEST);
	                }
	                return imageResponse;
                } catch (Exception e) {
					throw new RuntimeException(e);
				}
            }
        }, new TransformDelegate<Protocol.ImageResponse>() {
            @Override
            public ResponseBuilder transform(final Protocol.ImageResponse response) {
            	if (response.getStatus() == Status.OK){
            		_log.info("CHECK "+response.getImage());
            		return Response.ok().entity(ModelHelper.encode(response.getImage()));
                }
            	else{
            		return Response.status(response.getStatus().value());
            	}

            }
        }, Registry.fetchExceptionMapper());
        return response;
    }

    /*
     * (non-Javadoc)
     *
     * You cannot delete images with the 'protected' attribute set to true (boolean); Preconditions
     * • You can delete an image in all status except deleted. • You must first set the
     * 'protected' attribute to false (boolean) and then perform the delete. Synchronous
     * Postconditions • The response is empty and returns the HTTP 204 status code. • The image
     * is deleted in images index. • The binary image data managed by OpenStack Image Service is
     * deleted from the storage node if the deleted image stores image data in the node.
     * TroubleShooting • The response returns the HTTP 403 status code when the 'protected'
     * attribute is set to true even if you have a correct permissions. Ensure you meet the
     * preconditions then investigate the attribute. Normal response codes: 204 Error response
     * codes: 403
     *
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#delete(java.lang. String,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response delete(final String imageId) {
    	//TODO : can even delete using image NAME >> if ID public or private both can be deleted , for name only accesible can be deleted
    	//TODO: check the conditions for 'deleted' status >> soft delete
    	//TODO: if image doesnt exists >> 404
        final Protocol.ImageRequest request = new Protocol.ImageRequest().setImageId(imageId);
        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.ImageResponse>()
                {
                    @Override
                    public Protocol.ImageResponse process(){
                        // get registry
                    	boolean searchingName=false;
                    	Future<Image> futures = Registry.getImageDetails(request);
                    	Image image;
						try {
							image = futures.get();
							if(image.getId() == null){
								_log.info("searching by name");
								searchingName=true;
								futures = Registry.getImageDetailsByName(request);
								image=futures.get();
								request.setImageId(image.getId());
							}
						} catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    	final Protocol.ImageResponse imageResponse = new Protocol.ImageResponse();
                    	if (image.getId() != null)
		                {
                    		imageResponse.setStatus(Status.FORBIDDEN);
                    		_log.info("image details for given delete command exists");
                    		if(!image.getProtected()){
	                    		if(!searchingName){
	                        		Registry.deleteImage(request);
	                        		imageResponse.setStatus(Status.NO_CONTENT);
	                        	}else{
	                        		if (searchingName && !image.getProtected() && image.getVisibility() != null && image.getVisibility().equals("public")){
	                        			_log.info("deleting non-protected public by name");
	                        			Registry.deleteImage(request);
	    		                		imageResponse.setStatus(Status.NO_CONTENT);
	    		                    }
	                        	}
                        	}
		                }
		                else {
		                	imageResponse.setStatus(Status.BAD_REQUEST);
		                }
                    	
                    	//imageResponse.setImage(image);
                        return imageResponse;
                    }
                },
                new TransformDelegate<Protocol.ImageResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.ImageResponse response) {
                    	if (response.getStatus() == Status.FORBIDDEN){
                    		return Response.status(403);
                    	} else {
                    		return Response.status(204);
                    	}

                    }
                },
                Registry.fetchExceptionMapper()
        );
        return response;
    }


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#downloadImage(java.lang.String)
     */
    @Override
    public Response downloadImage(final String imageId) {
        // verify that the image exists and its state is queued.

        final Protocol.ImageRequest request = new Protocol.ImageRequest().setImageId(imageId);
        final Response response = FrontEndHelper.handleRequest(request, getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.ImageFileResponse>() {
                    @Override
                    public Protocol.ImageFileResponse process(){
                        final Protocol.ImageFileResponse protocolResponse = new Protocol.ImageFileResponse();

                        Future<Image> futures = Registry.getImageDetails(request);

                        // TODO: add md5 checksum in the header!
                        /*
                         * • If image data exists, you receive the HTTP 200 status code. • If no
                         * image data exists, you receive the HTTP 204 status code.
                         */

                        try {
                            Image imageMetadata = futures.get();
                            if (!imageMetadata.getStatus().equals(Image.Status.ACTIVE)) {
                                // If no image data exists, you receive the HTTP 204 status code.
                                protocolResponse.setStatus(Protocol.Status.NO_RESPONSE);
                            } else {
                                List<URL> locations = imageMetadata.getLocations();
                                // todo: loop through locations and fetch the file from the
                                // appropriate backend store

                                InputStream imageFile = imageStore.getImageFile(locations.get(0)).get();
                                protocolResponse.setImageFile(imageFile);
                                protocolResponse.setStatus(Protocol.Status.OK);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return protocolResponse;
                    }

                }, new TransformDelegate<Protocol.ImageResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.ImageResponse response) {
                        com.emc.caspian.ccs.imagerepo.model.Image image = new com.emc.caspian.ccs.imagerepo.model.Image();
                        // todo: transform from data model to api model
                        Image dataModelImage = response.getImage();

                        return Response.ok().entity(image);
                    }
                });
        return response;
    }

    /*
     * (non-Javadoc)
     *
     * Preconditions • The specified image must exist before you store binary image data. • You
     * need to set disk_format and container_format in the image before you store the data. • You
     * can only store the data into a image which status is queued. • The user must have enough
     * image strage quota remaining to store the data. • Size of the data must be less than
     * OpenStack Image Service restricts. Synchronous Postconditions • With correct permissions,
     * you can see the image status as active via API calls. • With correct access, you can see
     * the stored data in the storage system that OpenStack Image Service manages.
     *
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#uploadImage(byte[],
     * java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response uploadImage(final InputStream imageData, final String imageId, final long length) {
        //check the image format
        // verify that the image exists and its state is queued.

        final Protocol.ImageRequest request = new Protocol.ImageRequest().setImageId(imageId);
        final Response response = FrontEndHelper.handleRequest(request, getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.ImageResponse>() {
                    @Override
                    public Protocol.ImageResponse process() {
                        final Protocol.ImageResponse protocolResponse = new Protocol.ImageResponse();

                        Future<Image> futures = Registry.getImageDetails(request);

                        try {
                            Image imageMetadata = futures.get();
                            if (!imageMetadata.getStatus().equals(Image.Status.QUEUED.toString())) {
                                // throw webexception with error code 403
                            	protocolResponse.setStatus(Status.FORBIDDEN);
                            	return protocolResponse;
                            }
                            // Validate.isNotNullOrEmpty(imageMetadata.getDisk_format(),
                            // "disk format");
                            // Validate.isNotNullOrEmpty(imageMetadata.getContainer_format(),
                            // "container format");
                            Registry.checkUserQuota(getKeystonePrincipal(), imageMetadata.getSize());

                            long imageSize;
                            if(length != 0){
                            	imageSize = length;
                            } else {
                            	imageSize = imageMetadata.getSize();
                            }


                            URL imageFileLocation = ImageStoreFactory.getImageStore(ImageStoreType.FileSystem)
                                    .saveImageFile(imageId, imageData, imageSize).get();
                            List<URL> locations= imageMetadata.getLocations();
                            locations.add(imageFileLocation);
                            imageMetadata.setLocations(locations);
                            imageMetadata.setStatus(com.emc.caspian.ccs.imagerepo.api.datamodel.Image.Status.ACTIVE);
                            final Protocol.UpdateImageRequest updatedRequest = (UpdateImageRequest) new Protocol.UpdateImageRequest().setImage(imageMetadata);
                    		Registry.updateImageDetails(updatedRequest);
                    		protocolResponse.setImage(updatedRequest.getImage());
                            // TODO: CHECK update the location for the file in the image metadata

                            // TODO: update the location for the file in the image metadata
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        protocolResponse.setStatus(Protocol.Status.OK);
                        return protocolResponse;
                    }

                }, new TransformDelegate<Protocol.ImageResponse>() {
                    @Override
                    public ResponseBuilder transform(final Protocol.ImageResponse response) {
                        com.emc.caspian.ccs.imagerepo.model.Image image = new com.emc.caspian.ccs.imagerepo.model.Image();
                        // todo: transform from data model to api model
                        Image dataModelImage = response.getImage();

                        return Response.ok().entity(ModelHelper.encode(dataModelImage));
                    }
                });
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#addTag(java.lang.String, java.lang.String)
     */
    @Override
    public Response addTag(final String imageId, final String tagValue) {
    	//TODO: add a function if Registry to take care of tag addition explicitly, if updateImage is used it will have a overhead. Or try defining a customized propertyBag
    	final Protocol.ImageRequest request = new Protocol.ImageRequest().setImageId(imageId);
        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.ImageResponse>()
                {
                    @Override
                    public Protocol.ImageResponse process(){
                        // get registry
                    	Future<Image> futures = Registry.getImageDetails(request);
                    	final Protocol.ImageResponse imageResponse = new Protocol.ImageResponse();
                    	Image image;
						try {
							image = futures.get();
						} catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    	if (image != null){
	                    	List tags = image.getTags();
	                    	tags.add(tagValue);
	                    	image.setTags(tags);
	                    	image.setEntityType(EntityType.IMAGE);
	                    	imageResponse.setImage(image);
	                    	final Protocol.UpdateImageRequest updatedRequest = (UpdateImageRequest) new Protocol.UpdateImageRequest().setImage(image);
	                    	Registry.updateImageDetails(updatedRequest);
                    		imageResponse.setStatus(Status.NO_CONTENT);
                    	} else {
                    		//TODO: what if the image doesnot exist
                    		imageResponse.setStatus(Status.FORBIDDEN);
                    	}
                        return imageResponse;
                    }
                },
                new TransformDelegate<Protocol.ImageResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.ImageResponse response) {
                    	if (response.getStatus() == Status.NO_CONTENT){
                    		return Response.status(204);
                    	} else {
                    		//TODO: check
                    		return Response.status(403);
                    	}

                    }
                },
                Registry.fetchExceptionMapper()
        );
        _log.info("added tag "+tagValue);
        return response;
    }

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#deleteTag(java.lang.String, java.lang.String)
     */
    @Override
    public Response deleteTag(final String imageId, final String tagValue) {
    	//TODO: add a function if Registry to take care of tag addition explicitly, if updateImage is used it will have a overhead. Or try defining a customized propertyBag
    	final Protocol.ImageRequest request = new Protocol.ImageRequest().setImageId(imageId);
        Response response = FrontEndHelper.handleRequest(
                request,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.ImageResponse>()
                {
                    @Override
                    public Protocol.ImageResponse process(){
                        // get registry
                    	Future<Image> futures = Registry.getImageDetails(request);
                    	final Protocol.ImageResponse imageResponse = new Protocol.ImageResponse();
                    	Image image;
						try {
							image = futures.get();
						} catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    	if (image != null){
	                    	List tags = image.getTags();
	                    	tags.remove(tagValue);
	                    	image.setTags(tags);
	                    	image.setEntityType(EntityType.IMAGE);
	                    	imageResponse.setImage(image);
	                    	final Protocol.UpdateImageRequest updatedRequest = (UpdateImageRequest) new Protocol.UpdateImageRequest().setImage(image);
                    		Registry.updateImageDetails(updatedRequest);
                    		imageResponse.setStatus(Status.NO_CONTENT);
                    	} else {
                    		//TODO: what if the image doesnot exist OR tag doesnt exist
                    		imageResponse.setStatus(Status.FORBIDDEN);
                    	}
                        return imageResponse;
                    }
                },
                new TransformDelegate<Protocol.ImageResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.ImageResponse response) {
                    	if (response.getStatus() == Status.NO_CONTENT){
                    		return Response.status(204);
                    	} else {
                    		//TODO: check
                    		return Response.status(403);
                    	}

                    }
                },
                Registry.fetchExceptionMapper()
        );
        return response;
    }


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#membership(java.lang.String)
     */
    @Override
    public Response membership(final String imageId) {
        final Protocol.MemberRequest memberRequest = new Protocol.MemberRequest(RequestType.GET_MEMBER_V2);     
        memberRequest.setImageId(imageId);
         
        Response response = FrontEndHelper.handleRequest(
                memberRequest,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.MemberResponse>()
                {
                    @Override
                    public Protocol.MemberResponse process() throws Exception {
                    	
                    	final Protocol.MemberResponse memberResponse = new Protocol.MemberResponse();
                    	Protocol.ImageRequest imageRequest = new Protocol.ImageRequest();
                    	imageRequest.setImageId(memberRequest.getImageId());
                    	Image image = Registry.getImageDetails(imageRequest).get();                 		
	                    	
	                    if (image.getId() == null)
	                    {
	                    	memberResponse.setStatus(Status.ERROR_NO_IMAGE);
	                    }
	                    else if (image.getVisibility() == "public")
	                    {
	                   		memberResponse.setStatus(Status.INCORRECT_VISIBILITY);
	                   	}
	                   	else
	                   	{ 
	                       	memberResponse.setMembers(Registry.getMembers(memberRequest).get());
	                       	memberResponse.setStatus(Status.OK);		
	                   	}
                    	return memberResponse;
                     } 
                },
                new TransformDelegate<Protocol.MemberResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.MemberResponse response) 
                    {
                        Members members = new Members();
                        members.setMembers(Lists.newArrayList(Iterables.transform(response.getMembers(),
                                new Function<Member, com.emc.caspian.ccs.imagerepo.model.Member>() {
                                    @Override
                                    public com.emc.caspian.ccs.imagerepo.model.Member apply(final Member input) {
                                        return ModelHelper.encodeMember(input);
                                    }
                                })));
                        members.setSchema(membersSchema);
                        return Response.ok().entity(members);
                    }
                },
                Registry.fetchExceptionMapper()
        );
        return response;             
    }

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#addmember(java.lang.String, com.emc.caspian.ccs.imagerepo.model.Member)
     */
    @Override
    public Response addMember(final String imageId, final Map<String, String> Json) { 	
    	String memberId = Json.get(member);
        final Protocol.MemberRequest memberRequest = new Protocol.MemberRequest(RequestType.ADD_MEMBER_V2, imageId+"_"+memberId, imageId, memberId);     
         
        Response response = FrontEndHelper.handleRequest(
                memberRequest,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.MemberResponse>()
                {
                    @Override
                    public Protocol.MemberResponse process() throws Exception {
                    	
                    	final Protocol.MemberResponse memberResponse = new Protocol.MemberResponse();
                    	Protocol.ImageRequest imageRequest = new Protocol.ImageRequest();
                    	imageRequest.setImageId(memberRequest.getImageId());
                    	Image image = Registry.getImageDetails(imageRequest).get(); 
                    	String entryId = Registry.getExistingMember(memberRequest).get();                 		
	                    	
	                    if (image.getId() == null)
	                    {
	                    	memberResponse.setStatus(Status.ERROR_NO_IMAGE);
	                    }
	                    else if (image.getVisibility() == "public")
	                    {
	                   		memberResponse.setStatus(Status.INCORRECT_VISIBILITY);
	                   	}
	                   	else if (entryId != null)
	                   	{
	                   		memberResponse.setStatus(Status.MEMBER_EXISTS);
	                   	}
	                   	else
	                   	{ 
	                   		populateDefaults(memberRequest);
	                       	Member member = Registry.addMember(memberRequest).get();
	                       	memberResponse.setMember(member);
	                       	memberResponse.setStatus(Status.OK);		
	                   	}
                    	return memberResponse;
                     } 
                    
                    private void populateDefaults(Protocol.MemberRequest request) {
                    	final Member member = new Member(request.getId());
                        String dateTime = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss").format(new Date());
                        String schema = "/v2/schemas/member";
                        
                        member.setImageId(request.getImageId());
                        member.setMemberId(request.getMemberId());
                        member.setCreatedAt(dateTime);
                        member.setUpdatedAt(dateTime);
                        member.setSchema(schema);
                        member.setStatus(Member.Status.PENDING);
                        
                        request.setMember(member);
                    }
                },
                new TransformDelegate<Protocol.MemberResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.MemberResponse response) 
                    { 
                    	if (response.getStatus() == Status.OK)
                    	{
                    		return Response.ok().entity(ModelHelper.encodeMember(response.getMember()));
                    	}                    	
                    	else
                    	{   
                    		return Response.status(response.getStatus().value()).entity(null);
                    	}                        
                    }
                },
                Registry.fetchExceptionMapper()
        );
        return response;               
    }


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#memberdetails(java.lang.String, java.lang.String)
     */
    @Override
    public Response memberDetails(final String imageId, final String memberId) {
        final Protocol.MemberRequest memberRequest = new Protocol.MemberRequest(RequestType.GET_MEMBER_V2, imageId+"_"+memberId, imageId, memberId);;

        Response response = FrontEndHelper.handleRequest(
                memberRequest,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.MemberResponse>()
                {
                    @Override
                    public Protocol.MemberResponse process() throws Exception {
                    	final Protocol.MemberResponse memberResponse = new Protocol.MemberResponse();
                    	Protocol.ImageRequest imageRequest = new Protocol.ImageRequest();
                    	imageRequest.setImageId(memberRequest.getImageId());                  	

                    	Image image = Registry.getImageDetails(imageRequest).get();                    		
	                    String entryId = Registry.getExistingMember(memberRequest).get();
	                    	
	                    if (image.getId() == null)
	                    {
	                   		  memberResponse.setStatus(Status.ERROR_NO_IMAGE);
	                   	}
	                    else if (image.getVisibility() == "public")
	                    {
	                   		memberResponse.setStatus(Status.INCORRECT_VISIBILITY);
	                   	}
	                   	else if (entryId == null)
	                   	{ 
	                   		 memberResponse.setStatus(Status.ERROR_NO_MEMBER);
	                   	}
	                   	else
	                   	{ 
	                       	Member member=Registry.getMember(memberRequest).get();
	                       	memberResponse.setMember(member);
	                       	memberResponse.setStatus(Status.OK);	                                    		
	                   	}
                    	return memberResponse;
                     } 
                },
                new TransformDelegate<Protocol.MemberResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.MemberResponse response) {
                    	if(response.getStatus() == Status.OK)
                    	{
                    		return Response.status(Response.Status.OK).entity(ModelHelper.encodeMember(response.getMember()));
                    	}
                    	else
                    	{
                    		return Response.status(response.getStatus().value()).entity(null);
                    	}                   
                    }
                },
                Registry.fetchExceptionMapper()
        );
        return response;
    }

/* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#updateMember(java.lang.String, java.lang.String, com.emc.caspian.ccs.imagerepo.model.Member)
     */
    @Override
    public Response updateMember( final String imageId, final String memberId, final java.util.Map<String, String> Json) {
    	final String status = Json.get("status");
        final Protocol.MemberRequest memberRequest = new Protocol.MemberRequest(RequestType.UPDATE_MEMBER_V2, imageId+"_"+memberId, imageId, memberId);
        
        Response response = FrontEndHelper.handleRequest(
                memberRequest,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.MemberResponse>() {
                    @Override
                    public Protocol.MemberResponse process() throws Exception {
                    	final Protocol.MemberResponse memberResponse = new Protocol.MemberResponse();
                    	Protocol.ImageRequest imageRequest = new Protocol.ImageRequest();
                    	imageRequest.setImageId(memberRequest.getImageId());
                   
                    	Image image = Registry.getImageDetails(imageRequest).get();                    		
	                    String entryId = Registry.getExistingMember(memberRequest).get();
	                    
	                    if (status == null )
	                    {
	                    	memberResponse.setStatus(Status.BAD_REQUEST);
	                    }
	                    else if (image.getId() == null)
	                    {
	                       	memberResponse.setStatus(Status.ERROR_NO_IMAGE);
	                    }
	                    else if (image.getVisibility() == "public")
	                    {
	                    	memberResponse.setStatus(Status.INCORRECT_VISIBILITY);
	                    }
	                    else if (entryId == null)
	                    {
	                    	memberResponse.setStatus(Status.ERROR_NO_MEMBER);
	                    }
	                    else
	                    {
	                       	Member member = Registry.getMember(memberRequest).get();
	                       	member.setStatus(status);
	                       	member.setEntityType(EntityType.MEMBER);
	                   		member.setUpdatedAt(new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss ").format(new Date()));
	                    	
                            Registry.updateMember(member).get();
	                        		
	                       	memberResponse.setMember(member);
	                       	memberResponse.setStatus(Status.OK);
	                   	}       	
                    	return memberResponse;
                     } 
                },
                new TransformDelegate<Protocol.MemberResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.MemberResponse response) 
                    {
                    	
                    	if (response.getStatus() == Status.OK)
                    	{
                    		return Response.ok().entity(ModelHelper.encodeMember(response.getMember()));
                    	}
                    	else 
                    	{
                    		return Response.status(response.getStatus().value()).entity(null);
                    	}
                    }
                },
                Registry.fetchExceptionMapper()
        );
        return response;	
    }

    
    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2#deleteMember(java.lang.String, java.lang.String)
     */
    @Override
    public Response deleteMember(final String imageId, final String memberId) {
        final Protocol.MemberRequest memberRequest = new Protocol.MemberRequest(RequestType.DELETE_MEMBER_V2, imageId+"_"+memberId, imageId, memberId);

        Response response = FrontEndHelper.handleRequest(
                memberRequest,
                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.MemberResponse>() {
                    @Override
                    public Protocol.MemberResponse process() throws Exception {
                    	final Protocol.MemberResponse memberResponse = new Protocol.MemberResponse();
                    	Protocol.ImageRequest imageRequest = new Protocol.ImageRequest();
                    	imageRequest.setImageId(memberRequest.getImageId());
                    	
                    	Image image = Registry.getImageDetails(imageRequest).get();                    		
	                    String entryId = Registry.getExistingMember(memberRequest).get();
	                    	
	                    if (image.getId() == null)
	                    {
	                    	memberResponse.setStatus(Status.ERROR_NO_IMAGE);
	                    } 
	                    else if (image.getVisibility() == "public")
	                    {
	                    	memberResponse.setStatus(Status.INCORRECT_VISIBILITY);
	                    }
	                    else if (entryId == null)
	                    {
	                   		memberResponse.setStatus(Status.ERROR_NO_MEMBER);	                    		
	                   	}
	                   	else
	                   	{ 
	                   		Registry.deleteMember(memberRequest).get();
	                       	memberResponse.setStatus(Status.OK);	
	                   	}
					    return memberResponse;
                     } 
                },                   
                new TransformDelegate<Protocol.MemberResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.MemberResponse response) {
                    	return Response.status(response.getStatus().value()).entity(null);             	                    
                    }
                },
                Registry.fetchExceptionMapper()
        );
        return response;    	
    }

}
