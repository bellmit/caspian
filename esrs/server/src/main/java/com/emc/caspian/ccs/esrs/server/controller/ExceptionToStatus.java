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
package com.emc.caspian.ccs.esrs.server.controller;

import com.emc.caspian.ccs.esrs.server.controller.Protocol.Status;

/**
 * @author shivat
 *
 */
public interface ExceptionToStatus {

    /**
     * This function is used to fetch the status for a given exception. It returns a null if there
     * is no mapping present.
     *
     * @param exception
     * @return
     */
    public Status fetchErrorStatus(Exception exception);
}