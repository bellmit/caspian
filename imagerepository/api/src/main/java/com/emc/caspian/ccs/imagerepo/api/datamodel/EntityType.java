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
package com.emc.caspian.ccs.imagerepo.api.datamodel;

/**
 * List of EntityTypes supported.
 * @author shrids
 *
 */
public enum EntityType {
    IMAGE(Image.class.getCanonicalName()),
    DOCKER_IMAGE(DockerImage.class.getCanonicalName()),
    DOCKER_REPOSITORY(DockerRepository.class.getCanonicalName()),
    DOCKER_REPO_ENTRY(DockerRepoEntry.class.getCanonicalName()),
    IMAGE_LOCATION("com.emc.caspian.shared.datastore.expressiontree.ImageLocation"),
    NAMESPACE("com.emc.caspian.shared.datastore.expressiontree.Namespace"),
    METADEF("com.emc.caspian.shared.datastore.expressiontree.MetaDef"),
    ENTITY_A("com.emc.caspian.ccs.datastore.expressiontree.Entity_A"),

    //TODO: Enable in future
    //NAMESPACE("com.emc.caspian.shared.datamodel.Namespace"),
    //METADEF("com.emc.caspian.shared.datamodel.MetaDef"),
    MEMBER(Member.class.getCanonicalName()),
    TASK(Task.class.getCanonicalName());
    //TODO: Sandeep Remove ENTITY_A this is used for testing purposes.

    public String getClassName() {
        return modelClassName;
    }

    private EntityType(String className) {
        this.modelClassName = className;
    }

    private String modelClassName;
}
