/**
 * 
 */
package com.emc.caspian.ccs.license;

import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * Application to register the REST resource classes
 *
 */
public class FrontEndResources extends Application {

	public FrontEndResources(final Set<Object> resources) {
		super();
		_resource = resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return _resource;
	}

	private final Set<Object> _resource;

}
