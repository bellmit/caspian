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

import java.util.ArrayList;
import java.util.List;
/***
 *
 * @author kuppup
 *
 * This class represents the model to hold the list of events passed from ECI Clients
 * for ESRS Proxy to make callHome. Refer this class EsrsCallHomeProxyEventModel for the event model details.
 *
 */
public class EsrsCallHomeProxyModel {

    private List<EsrsCallHomeProxyEventModel> eventList = new ArrayList<EsrsCallHomeProxyEventModel>();

    /**
     * @return the messages
     */
    public List<EsrsCallHomeProxyEventModel> getEventList() {
        return eventList;
    }

    /**
     * @param messages
     *            the messages to set
     */
    public void setEventList(List<EsrsCallHomeProxyEventModel> eventList) {
        this.eventList = eventList;
    }

}