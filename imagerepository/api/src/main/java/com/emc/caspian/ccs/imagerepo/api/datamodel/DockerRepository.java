/**
 * 
 */
package com.emc.caspian.ccs.imagerepo.api.datamodel;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author shivat
 *
 */
public class DockerRepository extends MetadataBase{

    public DockerRepository() {
        super();
    }

    public DockerRepository(String id) {
        super(id, EntityType.DOCKER_REPOSITORY);
        name = id;
    }

    /**
     * Unique name for the repository
     */
    //TODO id and name are same, can remove name attribute
    private String name;

    /**
     * Map of docker image GUID to tags associated with the image
     */
    Map<String, String> repositoryMap = new HashMap<String,String>();

    /**
     * URL location of the repository metadata file
     */
    private URL metaDataFilePath;

    /**
     * docker members GUID set associated with the repository
     */
    Set<String> members = new HashSet<String>();

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the repositoryMap
     */
    public Map<String, String> getRepositoryMap () {
        return repositoryMap;
    }

    /**
     * @param repositoryMap the repositoryMap to set
     */
    public void setRepositoryMap(Map<String, String> repositoryMap) {
        this.repositoryMap = repositoryMap;
    }

    /**
     * @return the metaDataFilePath
     */
    public URL getMetaDataFilePath() {
        return metaDataFilePath;
    }

    /**
     * @param metaDataFilePath the metaDataFilePath to set
     */
    public void setMetaDataFilePath(URL metaDataFilePath) {
        this.metaDataFilePath = metaDataFilePath;
    }

    /**
     * @return the members list
     */
    public Set<String> getMembers() {
        return members;
    }

    /**
     * @param members the members list to set
     */
    public void setMembers(Set<String> members) {
        this.members = members;
    }

}
