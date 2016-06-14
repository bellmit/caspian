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

package com.emc.caspian.ccs.db.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * A common logger for all classes in the application. All methods of this class are static. This class encapsulates the
 * specific logger implementation and hides it from application classes.
 */
public class AppLogger {

	public static void initialize(String loggerName) {
		if (logger == null) {
			logger = LoggerFactory.getLogger(loggerName);
		}
	}

	public static void error(String message) {
		getLogger().error(message);
	}
	public static void error(String message, Throwable e) {
		getLogger().error(message, e);
	}
	public static void error(String format, Object... params) {
		String message = String.format(format, params);
		getLogger().error(message);
	}

	public static void info(String message) {
		logger.info(message);
	}

	public static void info(String format, Object... params) {
		String message = String.format(format, params);
		getLogger().info(message);
	}

	public static void debug(String message) {
		getLogger().debug(message);
	}

	public static void warn(String message) {
		getLogger().warn(message);
	}

	public static void debug(String format, Object... params) {
		String message = String.format(format, params);
		getLogger().debug(message);
	}

	public static void logException(Exception e) {
		if (e != null) {
			String error = String.format("Exception %s: %s", e.getClass().getCanonicalName(), e.getMessage());
			getLogger().error(error);
		}
	}

	private static Logger getLogger() {
		return logger;
	}

	private static Logger logger;
}
