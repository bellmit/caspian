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

import com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2Catalog;

import javax.ws.rs.core.Response;

/**
 * Implementation of Glance V2 Catalog APIs.
 *
 * @author shrids
 *
 */
public final class FrontEndGlanceV2Catalog implements GlanceV2Catalog {

    /*
     * (non-Javadoc)
     *
     * @see com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#
     * fetchResourceNamespaces(java.lang.String,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response fetchResourceNamespaces(final String entity) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#createNamespace
     * (java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response createNamespace(final String namespace) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#namespace(
     * java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response namespace(final String namespaceId) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#updateNamespace
     * (java.lang.String, java.lang.String,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response updateNamespace(final String namespace, final String namespaceId) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#deleteNamespace
     * (java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response deleteNamespace(final String namespaceId) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#entities(java
     * .lang.String, java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response entities(final String namespaceId, final String entityType) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#createEntity
     * (java.lang.String, java.lang.String, java.lang.String,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response createEntity(final String entity, final String namespaceId, final String entityType) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#entity(java
     * .lang.String, java.lang.String, java.lang.String,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response entity(final String namespaceId, final String entityType, final String entityName) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#deleteEntity
     * (java.lang.String, java.lang.String, java.lang.String,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response deleteEntity(final String namespaceId, final String entityType, final String entityName) {
        return Response.ok().build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Catalog#modifyEntity
     * (java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response modifyEntity(final String entity, final String namespaceId, final String entityType, final String entityName) {
        return Response.ok().build();
    }

}
