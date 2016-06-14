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
/***
 *
 * @author kuppup
 *
 * This class represents the model for alert data to be posted by ECI Clients for
 * ESRS Proxy to make callHome. The alert data should be in JSON format as below,
 *
 *      {
 *           "symptomCode": "<<SymtomCode>>",
 *           "category": "<<Status>>",
 *           "severity": "<<Error>>",
 *           "Status":"<<Warning>>",
 *           "componentID": "<<Matching with component Registry>>",
 *           "subComponentID": "<<Matching with component Registry>>",
 *           "eventData":"<<Event data>>",
 *           "description":"<<Event Description>>"
 *      }
 */
public class EsrsCallHomeProxyEventModel {

    private String symptomCode;
    private String category;
    private String severity;
    private String status;
    private String componentID;
    private String subComponentID;
    private String eventData;
    private String eventDescription;

    /**
     * @return the symptomCode
     */
    public String getSymptomCode() {
        return symptomCode;
    }

    /**
     * @param symptomCode
     *            the symptomCode to set
     */
    public void setSymptomCode(String symptomCode) {
        this.symptomCode = symptomCode;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category
     *            the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the severity
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * @param severity
     *            the severity to set
     */
    public void setSeverity(String severity) {
        this.severity = severity;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the componentID
     */
    public String getComponentID() {
        return componentID;
    }

    /**
     * @param componentID
     *            the componentID to set
     */
    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }

    /**
     * @return the eventData
     */
    public String getEventData() {
        return eventData;
    }

    /**
     * @param eventData
     *            the eventData to set
     */
    public void setEventData(String eventData) {
        this.eventData = eventData;
    }

    /**
     * @return the eventDescription
     */
    public String getEventDescription() {
        return eventDescription;
    }

    /**
     * @param eventDescription
     *            the eventDescription to set
     */
    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    /**
     * @return the subComponentID
     */
    public String getSubComponentID() {
        return subComponentID;
    }

    /**
     * @param subComponentID the subComponentID to set
     */
    public void setSubComponentID(String subComponentID) {
        this.subComponentID = subComponentID;
    }
}