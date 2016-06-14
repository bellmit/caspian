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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsrsCaspianServiceHealthModel {

//    {
//        "component-id" : "Caspian Component Id stored in Component Registry"
//    }

    private String componentID;
    
    public EsrsCaspianServiceHealthModel( ) {
	//Do nothing. Default constructor added to maintain seriable class. 
    }
    
    public EsrsCaspianServiceHealthModel( String compId) {
	componentID = compId;
    }

    /**
     * @return the componentID
     */
    public String getComponentID() {
        return componentID;
    }

    /**
     * @param componentID the componentID to set
     */
    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }
}
