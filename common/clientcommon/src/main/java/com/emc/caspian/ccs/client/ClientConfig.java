package com.emc.caspian.ccs.client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

public class ClientConfig {

  private static final int DEFAULT_LOGGING_ENTITY_LENGTH = 2048;
  private static final int DEFAULT_CONNECTION_TIMEOUT = 5 * 1000; // 5 seconds
  private static final int DEFAULT_READ_TIMEOUT = 5 * 60 * 1000; // 5 minutes
  private static final String DEFAULT_PROTOCOL = "https";
  private static final int DEFAULT_API_PORT = 6100;

  private String mediaType = "application/json"; // default media type
  private boolean requestLoggingEnabled = true;
  private int loggingEntityLength = DEFAULT_LOGGING_ENTITY_LENGTH;
  private int readTimeout = DEFAULT_READ_TIMEOUT;
  private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
  private String protocol = DEFAULT_PROTOCOL;
  private int port = DEFAULT_API_PORT;
  private String host;
  private SSLSocketFactory socketFactory;
  private HostnameVerifier hostnameVerifier;
  private boolean ignoreSSLCertificates = false;

  public boolean isRequestLoggingEnabled() {
    return requestLoggingEnabled;
  }

  /**
   * Sets if request logging should be enabled. This will log each called made to the API. Defaults to true.
   * 
   * @param requestLoggingEnabled True to enable logging each request, False to disable.
   */
  public void setRequestLoggingEnabled(boolean requestLoggingEnabled) {
    this.requestLoggingEnabled = requestLoggingEnabled;
  }

  public int getLoggingEntityLength() {
    return loggingEntityLength;
  }

  /**
   * Sets the maximum length of an HTTP request to be logged. If the size of the request exceeds the specified length,
   * it will be truncated. Defaults to logging 2048 characters.
   * 
   * @param loggingEntityLength Maximum length of HTTP request before truncation.
   */
  public void setLoggingEntityLength(int loggingEntityLength) {
    this.loggingEntityLength = loggingEntityLength;
  }

  public String getMediaType() {
    return mediaType;
  }

  /**
   * Sets the media type to be used for API requests. This can be either 'application/xml' or 'application/json'.
   * Defaults to 'application/xml'.
   * 
   * @param mediaType Media type to use.
   */
  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * Sets the connection timeout in milliseconds for API requests. Defaults to 5 minutes.
   * 
   * @param connectionTimeout Timeout in millliseconds.
   */
  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public int getReadTimeout() {
    return readTimeout;
  }

  /**
   * Sets the read timeout in milliseconds for API requests. Defaults to 5 minutes.
   * 
   * @param readTimeout Timeout in millliseconds.
   */
  public void setReadTimeout(int readTimeout) {
    this.readTimeout = readTimeout;
  }

  public String getHost() {
    return host;
  }

  /**
   * Sets the host of the target environment.
   * 
   * @param host Hostname or IP address for the Virtual IP of the target environment.
   */
  public void setHost(String host) {
    this.host = host;
  }

  public int getPort() {
    return port;
  }

  /**
   * Sets the target port for HTTP requests to the API. Defaults to 4443.
   * 
   * @param port Target port.
   */
  public void setPort(int port) {
    Validator.validatePortRange(port);
    this.port = port;
  }

  public String getProtocol() {
    return protocol;
  }

  /**
   * Sets the protocol for HTTP requests to the API. This should be either 'http' or 'https'. Defaults to 'https'.
   * 
   * @param protocol HTTP Protocol.
   */
  public void setProtocol(String protocol) {
    Validator.validateNotNull(protocol);
    Validator.validateProtocolType(protocol);
    this.protocol = protocol;
  }

  /**
   * Provide an alternate socket factory for the clients
   * 
   * @param socketFactory custom socket factory
   */
  public void setSocketFactory(SSLSocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  /**
   * Returns the provided SSLSocketFactory, or null
   * 
   * @return The custom SSLSocketFactory
   */
  public SSLSocketFactory getSocketFactory() {
    return socketFactory;
  }

  /**
   * Returns the provided HostnameVerifier, or null
   * 
   * @return The custom HostnameVerifier
   */
  public HostnameVerifier getHostnameVerifier() {
    return hostnameVerifier;
  }

  /**
   * Provide and alternate Hostname Verifier for the clients
   * 
   * @param hostnameVerifier custom hostname verifier
   */
  public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
    this.hostnameVerifier = hostnameVerifier;
  }

  /**
   * Sets the SSLSocketFactory and HostnameVerifier to ignore all SSL certificates. This is suitable for a default
   * installation using self-signed certificates. This is <b>not</b> intended for production use as it bypasses
   * important SSL security.
   * 
   * @param ignoreCertificates True if SSL trust should be disabled
   * @see #setSocketFactory(javax.net.ssl.SSLSocketFactory)
   * @see #setHostnameVerifier(javax.net.ssl.HostnameVerifier)
   */
  public void setIgnoreCertificates(boolean ignoreCertificates) {
    if (ignoreCertificates) {
      this.ignoreSSLCertificates = true;
      setSocketFactory(SSLUtil.getTrustAllSslSocketFactory());
      setHostnameVerifier(SSLUtil.getNullHostnameVerifier());
    }
  }

  public boolean isIgnoreSSLCertificates() {
    return ignoreSSLCertificates;
  }

  /**
   * Sets the host and returns the updated configuration.
   * 
   * @see #setHost(String)
   * @param host Hostname or IP address for the Virtual IP of the target environment.
   * @return The updated ClientConfig object.
   */
  public ClientConfig withHost(String host) {
    setHost(host);
    return this;
  }

  /**
   * Sets the port and returns the updated configuration.
   * 
   * @see #setPort(int)
   * @param port Target port to set.
   * @return The updated ClientConfig object.
   */
  public ClientConfig withPort(int port) {
    setPort(port);
    return this;
  }

  /**
   * Sets the protocol and returns the updated configuration.
   * 
   * @see #setProtocol(String)
   * @param protocol HTTP Protocol to use.
   * @return The updated ClientConfig object.
   */
  public ClientConfig withProtocol(String protocol) {
    setProtocol(protocol);
    return this;
  }

  /**
   * Sets the connection timeout and returns the updated configuration.
   * 
   * @see #setConnectionTimeout(int)
   * @param connectionTimeout Connection timeout to set.
   * @return The updated ClientConfig object.
   */
  public ClientConfig withConnectionTimeout(int connectionTimeout) {
    setConnectionTimeout(connectionTimeout);
    return this;
  }

  /**
   * Sets the read timeout and returns the updated configuration.
   * 
   * @see #setReadTimeout(int)
   * @param readTimeout Read timeout to set.
   * @return The updated ClientConfig object.
   */
  public ClientConfig withReadTimeout(int readTimeout) {
    setReadTimeout(readTimeout);
    return this;
  }

  /**
   * Sets the SSLSocketFactory and returns the updated configuration.
   * 
   * @see #setSocketFactory(javax.net.ssl.SSLSocketFactory)
   * @param factory The SSLSocketFactory to use
   * @return the updated ClientConfig object
   */
  public ClientConfig withSocketFactory(SSLSocketFactory factory) {
    setSocketFactory(factory);
    return this;
  }

  /**
   * Sets the HostnameVerifier and returns the updated configuration.
   * 
   * @see #setHostnameVerifier(javax.net.ssl.HostnameVerifier)
   * @param verifier The HostnameVerifier to use
   * @return the updated ClientConfig object
   */
  public ClientConfig withHostnameVerifier(HostnameVerifier verifier) {
    setHostnameVerifier(verifier);
    return this;
  }

  /**
   * Sets the SSLSocketFactory and HostnameVerifier to ignore all SSL certificates and returns the updated
   * configuration. This is suitable for a default installation using self-signed certificates. This is <b>not</b>
   * intended for production use as it bypasses important SSL security.
   * 
   * @see #setIgnoreCertificates(boolean)
   * @return the updated ClientConfig object
   */
  public ClientConfig withIgnoringCertificates(boolean ignoringCertificates) {
    setIgnoreCertificates(ignoringCertificates);
    return this;
  }

}
