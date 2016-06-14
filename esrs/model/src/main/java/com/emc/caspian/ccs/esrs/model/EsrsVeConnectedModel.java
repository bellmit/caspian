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
package com.emc.caspian.ccs.esrs.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author kuppup
 *
 */
@JsonInclude(Include.NON_NULL)
public class EsrsVeConnectedModel {

    private String connected;
    private String message;

    public EsrsVeConnectedModel(final String connected) {
        this.connected = connected;
    }

    public EsrsVeConnectedModel() {
    }

    /**
     * @return the connected
     */
    public String getConnected() {
        return connected;
    }

    /**
     * @param connected the connected to set
     */
    public void setVeConnected(final String connected) {
        this.connected = connected;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
