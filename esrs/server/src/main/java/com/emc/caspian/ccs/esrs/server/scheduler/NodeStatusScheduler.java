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
package com.emc.caspian.ccs.esrs.server.scheduler;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel;
import com.emc.caspian.ccs.esrs.server.controller.Protocol;
import com.emc.caspian.ccs.esrs.server.controller.Protocol.GateWayResponse;
import com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl;
import com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl.DeviceDetails;
import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.ESRSUtil;
import com.emc.caspian.ccs.esrs.server.util.EsrsConfiguration;
import com.emc.caspian.ccs.esrs.server.util.EtcdUtils;

public class NodeStatusScheduler implements Runnable{

    private static final Logger _log = LoggerFactory.getLogger(NodeStatusScheduler.class);
    private static final int NODE_COL_INTV = EsrsConfiguration.getNodeStatusInterval();

    public NodeStatusScheduler() {
	//do nothing
    }

    @Override
    public void run() {
        _log.info("Validating {} Node status", Constants.PRODUCT_NAME);

        //Checking ESRS Proxy registration exists or not.
        if(EtcdUtils.esrsIsRegistered() && EtcdUtils.esrsIsConnected()) {
            _log.info("ESRS is registered and connected. Node status check started");
        } else {
            _log.info("ESRS is not Registered/Connected. Node status check will be skipped");
            return;
        }

        if(checkLastValidationSchedule()) {
            _log.info("Last Node status check was less than {} minutes earlier. Will not check now", NODE_COL_INTV);
            return;
        }

        JSONArray nodesArray;
        List<String> platformNodes = new ArrayList<String>();

        try {
            nodesArray = ESRSUtil.getAllPlatformNodes();
            for (int index = 0; index < nodesArray.length(); index++) {
                JSONObject node = (JSONObject) nodesArray.get(index);
                JSONObject topology = node.getJSONObject(Constants.TOPOLOGY);
                platformNodes.add(topology.getString(Constants.ECI_NODE_IP));
            }
        } catch (Exception e) {
            _log.error("Unable to get platform nodes, skipping Node status check");
            return;
        }

        Integer nodeCount = Integer.valueOf(EtcdUtils.fetchFromEtcd(Constants.ETCD_PLATFORM_NODE_COUNT));
        for (int regNodeIndex=2; regNodeIndex <= nodeCount; regNodeIndex++) {
            String regPFNode = EtcdUtils.fetchFromEtcd(Constants.CASPIAN_HOST_NAME + Constants.HYPHEN + regNodeIndex);
            if(!platformNodes.contains(regPFNode)) {
                //If a already registered platform node doesn't exist in current platform node list, do Re-Registration.
                ESRSProxyImpl esrsProxyServer = new ESRSProxyImpl(); 
                Map<String, String> eciHostNames = new LinkedHashMap<String, String>();

                DeviceDetails device = esrsProxyServer.new DeviceDetails();
                device.setModel(EtcdUtils.fetchFromEtcd(Constants.CASPIAN_MODEL));
                device.setSerialNumber(EtcdUtils.fetchFromEtcd(Constants.CASPIAN_SERIAL_NUMBER));

                //Reading VIP from CRS using NIS
                String vipNode = ESRSUtil.getVIPFromCRS(Constants.PLATFORM, Constants.NODE_INVENTORY);
                int nodeIndex = 1;
                if (null != vipNode){
                    //If any other suffix for VIP is implemented,  need to changes the
                    //node index to other.
                    eciHostNames.put(Integer.toString(nodeIndex ), vipNode);
                }

                //Reading Max number of supported platform Node IPs from NIS,
                int maxPlatformNodesForReg = Math.min(platformNodes.size(), Constants.MAX_PLATFORM_NODES_SUPPORTED);

                for (int i = 0; i < maxPlatformNodesForReg; i++) {
                    eciHostNames.put(Integer.toString(++nodeIndex), platformNodes.get(i));
                }

                device.setEciHostNames(eciHostNames);

                EsrsVeRegistrationModel registrationDetails = new EsrsVeRegistrationModel();
                registrationDetails.setGateway(EtcdUtils.fetchFromEtcd(Constants.ETCD_VE_IP));
                registrationDetails.setPort(EtcdUtils.fetchFromEtcd(Constants.ETCD_VE_PORT));
                registrationDetails.setSsl(Boolean.valueOf(EtcdUtils.fetchFromEtcd(Constants.ETCD_VE_SSL)));
                registrationDetails.setUsername(EtcdUtils.fetchFromEtcd(Constants.ETCD_VE_USERNAME));
                registrationDetails.setPassword(EtcdUtils.fetchFromEtcd(Constants.ETCD_VE_PASSWORD));

                try {
                    GateWayResponse response = esrsProxyServer.registerWithESRSVE(registrationDetails , device);

                    if(response.getStatus().equals(Protocol.Status.OK)) {
                        _log.info("Re-Registering ECI Nodes due to node transfer is completed successfully");
                        EtcdUtils.persistToEtcd(Constants.ESRS_NODE_STATUS_LAST_CHECK_TIME, String.valueOf(System.currentTimeMillis()));
                    } else {
                        _log.error("Re-Registering ECI Nodes due to node transfer failed with {} / {}", 
                                response.getStatus(), response.getJsonObject().getMessage());
                    }
                } catch (URISyntaxException e) {
                    _log.error("Re-Registering ECI Nodes due to node transfer failed with {}", e);
                }
                return;
            }
        }
        _log.info("No change in node status, Re-registration ignored");
    }

    private static boolean checkLastValidationSchedule() {

        String lastNodeStatusCheckTime = null;
        try {
            lastNodeStatusCheckTime = EtcdUtils.fetchFromEtcd(Constants.ESRS_NODE_STATUS_LAST_CHECK_TIME);
        } catch (Exception e) {
            //LastNodeStatusCheck doesn't exist in ETCD , means Very first validation run
        }
        Long currentTime = System.currentTimeMillis();
        _log.debug("Last node check status check from etcd: {}", lastNodeStatusCheckTime);
        _log.debug("Current time: {}", currentTime);

        //If lastPropCollTime is null then there is no previous last check happen.
        if (null == lastNodeStatusCheckTime) {
            return false;
        }
        Long lastCollect = Long.parseUnsignedLong(lastNodeStatusCheckTime);

        Long timeDiff = currentTime - lastCollect;
        Long configuredIntervalMiliSecs = (long)NODE_COL_INTV * 60 * 1000; //miliseconds

        if (timeDiff < configuredIntervalMiliSecs ) {
            return true;
        } else {
            return false;
        }
    }
}
