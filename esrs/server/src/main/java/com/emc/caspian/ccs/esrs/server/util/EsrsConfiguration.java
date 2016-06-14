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
package com.emc.caspian.ccs.esrs.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.fabric.config.Configuration;

/**
 * Provides accessor for server related application properties. It uses properties cached in ApplicationProperties
 * object. All methods of this class are static. Created by gulavb on 3/1/2015.
 */
public class EsrsConfiguration {

    private static int keepAliveStartTime;
    private static int keepAliveInterval;
    private static int propColStartTime;
    private static int propColInterval;
    private static int mnrTimeout;
    private static int nodeStatusStartTime;
    private static int nodeStatusInterval;

    public static final String KEEP_ALIVE = "keepalive";
    public static final String PROPERTIES = "properties";
    public static final String NODESTATUS = "nodestatus";
    public static final String INITIAL_WAIT = "initialwait";
    public static final String INTERVAL = "interval";
    public static final String MNR_TIMEOUT = "mnrtimeout";
    public static final String MNR_USER = "mnruser";
    public static final String MNR_PASS = "mnrpwd";

    public static final String DEFAULT_KEEP_ALIVE_START = "15";
    public static final String DEFAULT_KEEP_ALIVE_INTV  = "60";
    public static final String DEFAULT_PROP_COL_START = "1440";
    public static final String DEFAULT_PROP_COL_INTV = "10080";
    public static final String DEFAULT_NODE_COL_START = "240";
    public static final String DEFAULT_NODE_COL_INTV = "240";
    public static final String DEFAULT_MNR_TIMEOUT = "1";
    public static final String DEFAULT_MNR_USER = "admin";
    public static final String DEFAULT_MNR_PASS = "changeme";

    private static final Logger _log = LoggerFactory.getLogger(EsrsConfiguration.class);

    // method to initialize all server properties
    static  {

        _log.debug("Initializing ESRS configuration");

        keepAliveStartTime = Configuration.make(Integer.class, KEEP_ALIVE + "." + 
                INITIAL_WAIT, DEFAULT_KEEP_ALIVE_START).value();
        keepAliveInterval = Configuration.make(Integer.class, KEEP_ALIVE + "." + 
                INTERVAL, DEFAULT_KEEP_ALIVE_INTV).value();
        propColStartTime = Configuration.make(Integer.class, PROPERTIES + "." + 
                INITIAL_WAIT, DEFAULT_PROP_COL_START).value();
        propColInterval = Configuration.make(Integer.class, PROPERTIES + "." + 
                INTERVAL, DEFAULT_PROP_COL_INTV).value();
        mnrTimeout = Configuration.make(Integer.class, PROPERTIES + "." + 
                MNR_TIMEOUT, DEFAULT_MNR_TIMEOUT).value();
        nodeStatusStartTime = Configuration.make(Integer.class, NODESTATUS + "." +
                INITIAL_WAIT, DEFAULT_NODE_COL_START).value();
        nodeStatusInterval = Configuration.make(Integer.class, NODESTATUS + "." +
                INTERVAL, DEFAULT_NODE_COL_INTV).value();
        
        if (null == EtcdUtils.fetchFromEtcd(Constants.PROP_COLL_START_TIME)) {
            _log.debug("writing property collector start time");
            EtcdUtils.persistToEtcd(Constants.PROP_COLL_START_TIME,
                    String.valueOf(propColStartTime));
            
        }
        if (null == EtcdUtils.fetchFromEtcd(Constants.PROP_COLL_SCH_INTERVAL)) {
            _log.debug("writing property collector interval");
            EtcdUtils.persistToEtcd(Constants.PROP_COLL_SCH_INTERVAL,
                    String.valueOf(propColInterval));
        }
        if (null == EtcdUtils.fetchFromEtcd(Constants.IS_ESRS_CALL_HOME_ENABLED)) {
            _log.debug("writing CallHome Enabled");
            EtcdUtils.persistToEtcd(Constants.IS_ESRS_CALL_HOME_ENABLED, Constants.FALSE);
        }
        
        _log.info("If you see any ETCD errors from ESRS above- please ignore them");
    }


    public static int getKeepAliveStartTime() {
        return keepAliveStartTime;
    }

    public static int getKeepAliveInterval() {
        return keepAliveInterval;
    }

    public static int getPropColStartTime() {
        if(null == EtcdUtils.fetchFromEtcd(Constants.PROP_COLL_START_TIME))
            return Integer.parseInt(DEFAULT_PROP_COL_START);
        return Integer.parseInt(EtcdUtils.fetchFromEtcd(Constants.PROP_COLL_START_TIME));
    }

    public static int getPropColInterval() {
        if(null == EtcdUtils.fetchFromEtcd(Constants.PROP_COLL_SCH_INTERVAL))
            return Integer.parseInt(DEFAULT_PROP_COL_INTV);
        return Integer.parseInt(EtcdUtils.fetchFromEtcd(Constants.PROP_COLL_SCH_INTERVAL));
    }

    public static int getMnrTimeout() {
        return mnrTimeout;
    }

    /**
     * @return the nodeStatusStartTime
     */
    public static int getNodeStatusStartTime() {
        return nodeStatusStartTime;
    }

    /**
     * @return the nodeStatusInterval
     */
    public static int getNodeStatusInterval() {
        return nodeStatusInterval;
    }

}