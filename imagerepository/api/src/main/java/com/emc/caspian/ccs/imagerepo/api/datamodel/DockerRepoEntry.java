package com.emc.caspian.ccs.imagerepo.api.datamodel;

public class DockerRepoEntry extends MetadataBase{

    public DockerRepoEntry() {
        super();
    }

    // id would be repositoryName_tag
    public DockerRepoEntry(String id) {
        super(id, EntityType.DOCKER_REPO_ENTRY);
    }
    
    /**
     * Unique name for the repository
     */
    private String repositoryName;
    
    /**
     * Tag associated with given image
     */
    private String tag;
    
    /**
     * Image GUUID
     */
    private String imageGUID;

    /**
     * @return the repositoryName
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * @param repositoryName the repositoryName to set
     */
    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag the tag to set
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * @return the imageGUID
     */
    public String getImageGUID() {
        return imageGUID;
    }

    /**
     * @param imageGUID the imageGUID to set
     */
    public void setImageGUID(String imageGUID) {
        this.imageGUID = imageGUID;
    }
}
