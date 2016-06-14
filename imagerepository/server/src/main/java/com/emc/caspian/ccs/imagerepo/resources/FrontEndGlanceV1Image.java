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

import javax.ws.rs.core.Response;

import com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV1ImageService;

/**
 * Implementation of Glance V1 Image Service APIs.
 *
 * @author shrids
 *
 */
public final class FrontEndGlanceV1Image implements GlanceV1ImageService {


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV1ImageService#images()
     */
    @Override
        public Response images() {
        return Response.ok().build();
    }


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV1ImageService#detailImages()
     */
    @Override
    public Response detailImages() {
        return Response.ok().build();
    }


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV1ImageService#imageMetaData(java.lang.String)
     */
    @Override
    public Response imageMetaData(final String imageId) {
        return Response.ok().build();
    }


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV1ImageService#downloadImage(java.lang.String)
     */
    @Override
    public Response downloadImage(final String imageId) {
        return Response.ok().build();
    }


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV1ImageService#uploadImage()
     */
    @Override
    public Response uploadImage() {
        return Response.ok().build();
    }

    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV1ImageService#image(java.lang.String)
     */
    @Override
    public Response image(final String imageId) {
        return Response.ok().build();
    }


    /* (non-Javadoc)
     * @see com.emc.caspian.ccs.imagerepo.api.ApiV1.GlanceV1ImageService#deleteImage(java.lang.String)
     */
    @Override
    public Response deleteImage(final String imageId) {
        return Response.ok().build();
    }

}
