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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @author kuppup
 *
 * This model is to handle Neutrino Configuration collection intervals.
 *
 */
@JsonInclude(Include.NON_NULL)
public class PropertyCollectorScheduleModel {

    @JsonProperty("propertyConfigSendInterval")
    private String propertyConfigSendInterval;
    
    @JsonProperty("propertyConfigStartTime")
    private String propertyConfigStartTime;

    /**
     * @return the propertyConfigSendInterval
     */
    public String getPropertyConfigSendInterval() {
        return propertyConfigSendInterval;
    }

    /**
     * @param propertyConfigSendInterval the propertyConfigSendInterval to set
     */
    public void setPropertyConfigSendInterval(String propertyConfigSendInterval) {
        this.propertyConfigSendInterval = propertyConfigSendInterval;
    }
    
    /**
     * @return the propertyConfigStartTime
     */
    public String getPropertyConfigStartTime() {
        return propertyConfigStartTime;
    }

    /**
     * @param propertyConfigStartTime the propertyConfigStartTime to set
     */
    public void setPropertyConfigStartTime(String propertyConfigStartTime) {
        this.propertyConfigStartTime = propertyConfigStartTime;
    }

    public PropertyCollectorScheduleModel() {
    }

}
