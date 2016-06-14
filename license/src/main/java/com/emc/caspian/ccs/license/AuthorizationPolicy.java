package com.emc.caspian.ccs.license;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface AuthorizationPolicy {

	// Supported authorization rules
	public enum Rule {
		DENY_ALL, ALLOW_ALL, ALLOW_CLOUD_ADMIN, ALLOW_CLOUD_MONITOR
	};

	Rule[] value();
}
