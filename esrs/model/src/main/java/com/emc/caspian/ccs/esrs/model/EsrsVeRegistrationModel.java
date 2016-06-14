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
package com.emc.caspian.ccs.esrs.model;

/**
 * 
 * @author shivat
 *
 */
public class EsrsVeRegistrationModel extends EsrsVeConnectedModel{

    /***
     * {
    "ve-gateway" : "ESRS VE Gateway hostname",
    "ve-port" : "ESRS VE Gateway port",
    "ve-gateway-ssl" : "y", //Default
    "ve-gateway-username" : "ve-gateway-username",
    "ve-gateway-password" : "ve-gateway-password"
    }
     */

    private String gateway;
    private String port;
    private Boolean ssl;
    private String username;
    private String password;

    private String caspianModel;
    private String caspianSerialNumber;
    private String caspianNode;
    private String enabled;
    private String lastKeepAliveTime;

    /**
     * @return the gateway
     */
    public String getGateway() {
        return gateway;
    }
    /**
     * @param gateway the gateway to set
     */
    public EsrsVeRegistrationModel setGateway(String gateway) {
        this.gateway = gateway;
        return this;
    }
    /**
     * @return the port
     */
    public String getPort() {
        return port;
    }
    /**
     * @param port the port to set
     */
    public EsrsVeRegistrationModel setPort(String port) {
        this.port = port;
        return this;
    }
    /**
     * @return the ssl
     */
    public Boolean isSsl() {
        return ssl;
    }
    /**
     * @param ssl the ssl to set
     */
    public EsrsVeRegistrationModel setSsl(Boolean ssl) {
        this.ssl = ssl;
        return this;
    }
    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    /**
     * @param username the username to set
     */
    public EsrsVeRegistrationModel setUsername(String username) {
        this.username = username;
        return this;
    }
    /**
     * @return the password
     */
    public String getPassword() {
        //TODO: Do we need to return password in plain text?.
        return password;
    }
    /**
     * @param password the password to set
     */
    public EsrsVeRegistrationModel setPassword(String password) {
        this.password = password;
        return this;
    }
    /**
     * @return the caspianModel
     */
    public String getCaspianModel() {
        return caspianModel;
    }
    /**
     * @param caspianModel the caspianModel to set
     */
    public EsrsVeRegistrationModel setCaspianModel(String caspianModel) {
        this.caspianModel = caspianModel;
        return this;
    }
    /**
     * @return the caspianSerialNumber
     */
    public String getCaspianSerialNumber() {
        return caspianSerialNumber;
    }
    /**
     * @param caspianSerialNumber the caspianSerialNumber to set
     */
    public EsrsVeRegistrationModel setCaspianSerialNumber(String caspianSerialNumber) {
        this.caspianSerialNumber = caspianSerialNumber;
        return this;
    }
    /**
     * @return the caspianNode
     */
    public String getCaspianNode() {
        return caspianNode;
    }
    /**
     * @param caspianNode the caspianNode to set
     */
    public EsrsVeRegistrationModel setCaspianNode(String caspianNode) {
        this.caspianNode = caspianNode;
        return this;
    }
    /**
     * @return the enabled
     */
    public String getEnabled() {
        return enabled;
    }
    /**
     * @param enabled the enabled to set
     */
    public EsrsVeRegistrationModel setEnabled(String enabled) {
        this.enabled = enabled;
        return this;
    }
    public String getLastKeepAliveTime() {
        return lastKeepAliveTime;
    }
    public EsrsVeRegistrationModel setLastKeepAliveTime(String lastKeepAliveTime) {
        this.lastKeepAliveTime = lastKeepAliveTime;
        return this;
    }
    @Override
    public String toString() {
        return "{\"Connected\":\"" + getConnected() + "\",\"Message\":\"" + getMessage() + "\"}";
    }
}
