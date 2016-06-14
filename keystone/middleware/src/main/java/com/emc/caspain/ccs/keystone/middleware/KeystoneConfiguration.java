package com.emc.caspain.ccs.keystone.middleware;

class KeystoneConfiguration {
  private String authURI;
  private String user;
  private String password;

  public KeystoneConfiguration(String uri, String username, String pwd) {
    authURI = uri;
    user = username;
    password = pwd;
  }

  public String getAuthURI() {
    return authURI;
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }

  public String getString() {
    return "KeystoneConfiguration: authURI - " + authURI + ". user - " + user;
  }
}
