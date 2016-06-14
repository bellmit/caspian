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

package com.emc.caspian.ccs.db;

import java.util.List;
import java.util.Map;

import com.emc.caspian.ccs.db.util.AppLogger;
import com.emc.caspian.crs.CRSClientBuilder;
import com.emc.caspian.crs.ComponentRegistryClient;
import com.emc.caspian.crs.ServiceLocationClient;
import com.emc.caspian.crs.model.BalancerRule;
import com.emc.caspian.crs.model.ComponentEndpoint;

public class CRSClient {
	private static String MySQLURL;

	public static String getMySQLURL() {

		ComponentRegistryClient CRSclient = CRSClientBuilder.newClient();
		List<BalancerRule> rule = CRSclient.getComponent("platform",
				"mysql-galera").getBalancerRules();
		if (!rule.isEmpty()) {
			Map<String, Object> map = rule.get(0).getParams();
			MySQLURL = "mysql://" + map.get("vip") + ":" + map.get("port");
		} else {
			ServiceLocationClient client = CRSClientBuilder
					.newServiceLocationClient();
			List<ComponentEndpoint> endpoint = client.getAllEndpoints(
					"platform", "mysql-galera");

			if (endpoint.size() > 0) {
				String url = endpoint.get(0).getUrl();
				MySQLURL = url.toString().replaceAll("http", "mysql")
						.replaceAll("tcp", "mysql");
			} else {
				AppLogger.error("No endpoint of MySQL found in CRS");
				throw new RuntimeException("No endpoint of MySQL found in CRS");
			}
		}
		
		return MySQLURL;
	}
}
