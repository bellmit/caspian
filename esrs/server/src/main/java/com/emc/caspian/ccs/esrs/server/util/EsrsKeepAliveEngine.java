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

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl;

public class EsrsKeepAliveEngine implements Runnable{

    private static final Logger _log = LoggerFactory.getLogger(EsrsKeepAliveEngine.class);
    private static final int CONFIGURED_KEEP_ALIVE_INTERVAL = EsrsConfiguration.getKeepAliveInterval(); //minutes

    public static boolean checkLastKeepAliveLessThanInterval() {
	
	String lastKeepAliveTime = EtcdUtils.fetchFromEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME);
	_log.debug("Last kept alive from etcd: "+ lastKeepAliveTime);
	_log.debug("Current time: "+ String.valueOf(System.currentTimeMillis()));
	
	Long lastKeepAlive = Long.parseUnsignedLong(lastKeepAliveTime) ;
	Long currentTime = System.currentTimeMillis() ;
	Long timeDiff = currentTime - lastKeepAlive;
	Long configuredIntervalMiliSecs = (long)CONFIGURED_KEEP_ALIVE_INTERVAL * 60 * 1000; //miliseconds
	
	if (timeDiff < configuredIntervalMiliSecs )
	    return true;
	
	return false;
    }

    public static void sendKeepAlive() {

	ESRSProxyImpl esrsProxyServer = new ESRSProxyImpl(); 

	Response response = esrsProxyServer.healthStatus();

	if(Response.Status.OK.getStatusCode() == response.getStatus()) {
	    EtcdUtils.persistToEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME, String.valueOf(System.currentTimeMillis()));
	}
    }

    @Override
    public void run() {
	
	if(EtcdUtils.esrsIsRegistered()) {
	    _log.debug("ESRS is registered.");
	} else {
	    _log.warn("ESRS is not registered. keepAlive skipped.");
	    return;
	}

	if(checkLastKeepAliveLessThanInterval()) {
	
	    _log.debug("Last Keep alive ping less than " 
		    + CONFIGURED_KEEP_ALIVE_INTERVAL
		    +" minutes earlier. Do not send keep alive.");

	    return;
	}

	sendKeepAlive();
    }

    
}

