package com.emc.caspian.ccs.license;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class License {
	@JsonProperty
	private String name;

	@JsonProperty
	private Long expirationDate;

	@JsonProperty
	private Map<String, String> properties = new HashMap<String, String>();

	@JsonProperty
	private String id;

	@JsonProperty
	private Long timeAdded;

	/**
	 * @return the timeAdded
	 */
	public Long getTimeAdded() {
		return timeAdded;
	}

	/**
	 * @param timeAdded the timeAdded to set
	 */
	public void setTimeAdded(Long timeAdded) {
		this.timeAdded = timeAdded;
	}

	/**
	 * Gets the name of the license.
	 * 
	 * @return String name of license.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the license.
	 * 
	 * @param name
	 *            String name of the license.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Gets the md5 hash of the license.
	 * 
	 * @return md5 hash of license.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the md5 hash of the license.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Gets the expiration date of the license.
	 * 
	 * @return expiration date of the license.
	 */
	public Long getExpirationDate() {
		return expirationDate;
	}

	/**
	 * Sets expiration date of the license.
	 * 
	 * @param expirationDate
	 *            expiration date of the license.
	 */
	public void setExpirationDate(final Long expirationDate) {
		this.expirationDate = expirationDate;
	}

	/**
	 * Adds a property to the license.
	 * 
	 * @param property
	 *            property name to be added.
	 * @param value
	 *            property value to be added.
	 */
	public void addProperty(final String property, final String value) {
		if (property != null) {
			properties.put(property, value);
		} else {
			// throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
			// "property cannot be null");
			// TODO: add log and handle it
		}
	}

	/**
	 * Gets a license property's value.
	 * 
	 * @param key
	 *            name of property to be retrieved.
	 * @return String value of property.
	 */
	public String getProperty(final String key) {
		return properties.get(key);
	}

}
