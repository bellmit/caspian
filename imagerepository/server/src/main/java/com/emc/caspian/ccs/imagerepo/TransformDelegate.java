package com.emc.caspian.ccs.imagerepo;

import javax.ws.rs.core.Response.ResponseBuilder;

import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol;

/**
 * Created by shivesh on 2/20/15.
 */
public interface TransformDelegate<T extends Protocol.Response>
{
	ResponseBuilder transform(T response);
}
