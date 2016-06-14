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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;

public abstract class BaseResource {

    @Context SecurityContext sc;

    public final KeystonePrincipal getKeystonePrincipal () {
        return (KeystonePrincipal) sc.getUserPrincipal();
    }
}
