/**
 *  Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.esrs.server.controller;

import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * Created by shivesh on 2/20/15.
 */
public interface TransformDelegate<T extends Protocol.Response>
{
	ResponseBuilder transform(T response);
}
