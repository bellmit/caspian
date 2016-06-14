package com.emc.caspian.ccs.imagerepo.api.datamodel;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Docker Image model.
 * @author shivat
 *
 */
public class DockerImage extends MetadataBase{

    public DockerImage() {
        super();
        this.entityType = EntityType.DOCKER_IMAGE;
    }

    public DockerImage(String id) {
        super(id, EntityType.DOCKER_IMAGE);
    }

    /**
     * Image GUUID of the parent docker layer
     */
    private String parentId;

    /**
     * md5 hash of image contents
     *
     */
    private java.lang.Object checksum;
    /**
     * Date and time of image registration
     *
     */
    private String createdAt;

    /**
     * Cumulative space taken up by this layer and all its parent image layers
     * This is in bytes
     */
    private long virtualSize;

    /**
     * A set of all the locations the image resides within the cluster
     */
    private List<String> mirrors = new ArrayList<String>();

    /**
     * URL location of image file kept in external store
     */
    private URL location;

    /**
     * URL location of the image metadata file
     */
    private URL metaDataFilePath;
    
    /**
     * @return the parentId
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * @return the virtualSize
     */
    public long getVirtualSize() {
        return virtualSize;
    }

    /**
     * @param virtualSize the virtualSize to set
     */
    public void setVirtualSize(long virtualSize) {
        this.virtualSize = virtualSize;
    }

    /**
     * @return the dockerLayerLocations
     */
    public List<String> getMirrors() {
        return mirrors;
    }

    /**
     * @param dockerLayerLocations the dockerLayerLocations to set
     */
    public void setMirrors(List<String> mirrorLocations) {
        this.mirrors = mirrorLocations;
    }

    /**
     * @return the imageStoreLocation
     */
    public URL getLocation() {
        return location;
    }

    /**
     * @param imageStoreLocation the imageStoreLocation to set
     */
    public void setLocation(URL locationInExternalStore) {
        this.location = locationInExternalStore;
    }

    /**
     * @return the metaDataFileLocation
     */
    public URL getMetaDataFilePath() {
        return metaDataFilePath;
    }

    /**
     * @param metaDataFileLocation the metaDataFileLocation to set
     */
    public void setMetaDataFilePath(URL metaDataFileLocation) {
        this.metaDataFilePath = metaDataFileLocation;
    }


}
