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

import com.emc.caspian.ccs.imagerepo.api.ApiV1;
import com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV2Schema;
import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Implementation of Glance V2 Schema Apis
 *
 * @author shrids
 *
 */
public final class FrontEndGlanceV2Schema implements GlanceV2Schema {

    private static final Logger _log = LoggerFactory.getLogger(FrontEndGlanceV2Schema.class);

    private static class Types
    {
        public static final String IMAGES = "images";
        public static final String IMAGE = "image";
        public static final String MEMBERS = "members";
        public static final String MEMBER = "member";
        public static final String OBJECTS = "objects";
        public static final String OBJECT = "object";
        public static final String RESOURCE_TYPES = "resource_types";
        public static final String RESOURCE_TYPE = "resource_type";
        public static final String TAGS = "tags";
        public static final String TAG = "tag";
        public static final String TASKS = "tasks";
        public static final String TASK = "task";
    }
    /*
             * (non-Javadoc)
             *
             * @see
             * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Schema#images(javax
             * .servlet.http.HttpServletRequest)
             */
    @Override
    public Response images() {
        return Response.ok().entity(getSchemaFile(Types.IMAGES)).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Schema#image(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public Response image() {
        return Response.ok().entity(getSchemaFile(Types.IMAGE)).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Schema#members(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public Response members() {
        return Response.ok().entity(getSchemaFile(Types.MEMBERS)).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Schema#member(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public Response member() {
        return Response.ok().entity(getSchemaFile(Types.MEMBER)).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Schema#tasks(javax
     * .servlet.http.HttpServletRequest)
     */
    @Override
    public Response tasks() {
        return Response.ok().entity(getSchemaFile(Types.TASKS)).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Schema#task(javax.
     * servlet.http.HttpServletRequest)
     */
    @Override
    public Response task() {
        return Response.ok().entity(getSchemaFile(Types.TASK)).build();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.emc.caspian.shared.imagerepo.api.ApiV1.GlanceV2Schema#entity(java
     * .lang.String, javax.servlet.http.HttpServletRequest)
     */
    @Override
    public Response entity(String entity) {
        // TBD
        return Response.ok().build();
    }

    private String getSchemaFile(String objectType) {
        final String response;

        InputStream in = ApiV1.GlanceV2Schema.class.getClassLoader().getResourceAsStream("schema/" + objectType + ".json");
        try {
            response = CharStreams.toString(new InputStreamReader(in, "UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
