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
 *
 * @author shrids
 *
 */
public abstract class MetadataBase {
    protected String id;
    protected EntityType entityType;

    public MetadataBase() {
    }

    public MetadataBase(final String id, final EntityType type) {
        this.id = id;
        this.entityType = type;
    }

    /**
     * Datetime when this resource was created
     *
     */
    protected String createdAt;
    /**
     * Datetime when this resource was updated
     *
     */
    protected String updatedAt;

    /**
     * Datetime when this resource would be subject to removal
     *
     */
    protected String deletedAt;

    /**
     * Datetime when this resource was created
     *
     * @return The created_at
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Datetime when this resource was created
     *
     * @param created_at
     *            The created_at
     */
    public void setCreatedAt(String created_at) {
        this.createdAt = created_at;
    }

    /**
     * Datetime when this resource was updated
     *
     * @return The updated_at
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Datetime when this resource was updated
     *
     * @param updated_at
     *            The updated_at
     */
    public void setUpdatedAt(String updated_at) {
        this.updatedAt = updated_at;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the entityType
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * @param entityType the entityType to set
     */
    public MetadataBase setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

}
