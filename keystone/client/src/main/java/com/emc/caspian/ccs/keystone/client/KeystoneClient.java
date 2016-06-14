package com.emc.caspian.ccs.keystone.client;

import com.emc.caspian.ccs.client.ClientConfig;
import com.emc.caspian.ccs.client.RestClient;

public class KeystoneClient {

  protected RestClient client;

  /**
   * Convenience method for calling constructor with new ClientConfig().withHost(host)
   * 
   * @param host Hostname or IP address for the Virtual IP of the target environment.
   */
  public KeystoneClient(String host) {
    this(new ClientConfig().withHost(host));
  }


  public KeystoneClient(String host, int port) {
    this(new ClientConfig().withHost(host).withPort(port));
  }

  public KeystoneClient(String proto, String host, int port) {
    this(new ClientConfig().withProtocol(proto).withHost(host).withPort(port));
  }


  /**
   * Convenience method for calling constructor with new
   * ClientConfig().withHost(host).withIgnoringCertificates(ignoreCertificates)
   * 
   * @param host Hostname or IP address for the Virtual IP of the target environment.
   * @param ignoreCertificates True if SSL certificates should be ignored.
   */
  public KeystoneClient(String host, boolean ignoreCertificates) {
    this(new ClientConfig().withHost(host).withIgnoringCertificates(ignoreCertificates));
  }


  /**
   * Convenience method for calling constructor with new
   * ClientConfig().withHost(host).withIgnoringCertificates(ignoreCertificates)
   * 
   * @param host Hostname or IP address for the Virtual IP of the target environment.
   * @param ignoreCertificates True if SSL certificates should be ignored.
   */
  public KeystoneClient(String host, int port, boolean ignoreCertificates) {
    this(new ClientConfig().withHost(host).withPort(port).withIgnoringCertificates(ignoreCertificates));
  }


  public KeystoneClient(String proto, String host, boolean ignoreCertificates) {
    this(new ClientConfig().withProtocol(proto).withHost(host).withIgnoringCertificates(ignoreCertificates));
  }


  public KeystoneClient(String proto, String host, int port, boolean ignoreCertificates) {
    this(new ClientConfig().withProtocol(proto).withHost(host).withPort(port)
        .withIgnoringCertificates(ignoreCertificates));
  }
  
  public KeystoneClient(String proto, String host, int port, int connectionTimeout,
      boolean ignoreCertificates) {
    this(new ClientConfig().withProtocol(proto).withHost(host).withPort(port)
        .withIgnoringCertificates(ignoreCertificates).withConnectionTimeout(connectionTimeout));
  }

  public KeystoneClient(ClientConfig config) {
    this.client = new RestClient(config);
  }

  KeystoneClient(RestClient client) {
    this.client = client;
  }

  /**
   * Gets the keystone token client.
   * 
   * @return the keystone token client
   */
  public KeystoneTokenClient getKeystoneTokenClient() {
    return new KeystoneTokenClient(client);
  }

  /**
   * Gets the keystone domain client for operations on domain.
   *
   * @return the keystone domain client
   */
  public KeystoneDomainClient getKeystoneDomainClient() {
    return new KeystoneDomainClient(client);
  }

  /**
   * Gets the keystone group client for operations on group.
   *
   * @return the keystone group client
   */
  public KeyStoneGroupClient getKeystoneGroupClient() {
    return new KeyStoneGroupClient(client);
  }

  /**
   * Gets the keystone role assignment client.
   *
   * @return the keystone role assignment client
   */
  public KeystoneRoleAssignmentClient getKeystoneRoleAssignmentClient() {
    return new KeystoneRoleAssignmentClient(client);
  }
  
  public KeystoneUserClient getKeystoneUserClient() {
    return new KeystoneUserClient(client);
  }

  public KeystoneProjectClient getKeystoneProjectClient() {
    return new KeystoneProjectClient(client);
  }
}
