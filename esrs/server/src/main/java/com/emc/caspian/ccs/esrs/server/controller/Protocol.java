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
package com.emc.caspian.ccs.esrs.server.controller;

/**
 * Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */

import com.emc.caspian.ccs.esrs.model.EsrsVeConnectedModel;
import com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel;

/**
 */
public final class Protocol {

    public enum Status {
        OK(200),
        CREATED(201),
        BAD_REQUEST(400),
        NO_RESPONSE(1), 
        ERROR_INTERNAL(100),
        ERROR_BAD_REQUEST(101), 
        ERROR_UNAUTHORIZED(401),
        ERROR_NOT_FOUND(103), 
        NO_CONTENT(3),
        FORBIDDEN(403),
        PRECONDITION_FAILED(412),
        NOT_IMPLEMENTED(800),
        SERVICE_UNAVAILABLE(503);

        private final int _value;

        private Status(final int value) {
            _value = value;
        }

        public int value() {
            return _value;
        }
    }

    public interface Request{
    }

    public static class Response {
        
        private Status status = Status.OK;

        public Status getStatus() {
            return status;
        }

        public void setStatus(final Status status) {
            this.status = status;
        }

        public void setStatus(int statusCode) {
            status=Protocol.Status.ERROR_INTERNAL;
            for (Protocol.Status statusVal : Protocol.Status.values()) {
                if(statusVal.value() == statusCode){
                    status=statusVal;
                    break;
                }
              }
        }
    }

    public static class RegistrationRequest implements Request {
        private String esrsIp;
        private String esrsPort;
        private String esrsSsl;
        private String esrsUsername;
        private String esrsPassword;
        /**
         * @return the esrsIp
         */
        public String getEsrsIp() {
            return esrsIp;
        }
        /**
         * @param esrsIp the esrsIp to set
         */
        public void setEsrsIp(String esrsIp) {
            this.esrsIp = esrsIp;
        }
        /**
         * @return the esrsPort
         */
        public String getEsrsPort() {
            return esrsPort;
        }
        /**
         * @param esrsPort the esrsPort to set
         */
        public void setEsrsPort(String esrsPort) {
            this.esrsPort = esrsPort;
        }
        /**
         * @return the esrsSsl
         */
        public String getEsrsSsl() {
            return esrsSsl;
        }
        /**
         * @param esrsSsl the esrsSsl to set
         */
        public void setEsrsSsl(String esrsSsl) {
            this.esrsSsl = esrsSsl;
        }
        /**
         * @return the esrsUsername
         */
        public String getEsrsUsername() {
            return esrsUsername;
        }
        /**
         * @param esrsUsername the esrsUsername to set
         */
        public void setEsrsUsername(String esrsUsername) {
            this.esrsUsername = esrsUsername;
        }
        /**
         * @return the esrsPassword
         */
        public String getEsrsPassword() {
            return esrsPassword;
        }
        /**
         * @param esrsPassword the esrsPassword to set
         */
        public void setEsrsPassword(String esrsPassword) {
            this.esrsPassword = esrsPassword;
        }
    }

    public static class GateWayResponse extends Response {
        private EsrsVeRegistrationModel jsonObject;

        /**
         * @return the jsonObject
         */
        public EsrsVeRegistrationModel getJsonObject() {
            return jsonObject;
        }

        /**
         * @param jsonObject the jsonObject to set
         */
        public void setJsonObject(EsrsVeRegistrationModel jsonObject) {
            this.jsonObject = jsonObject;
        }
    }

    public static class GateWayConnectedResponse extends Response {
        private EsrsVeConnectedModel jsonObject;
        /**
         * @return the jsonObject
         */
        public EsrsVeConnectedModel getJsonObject() {
            return jsonObject;
        }

        /**
         * @param jsonObject the jsonObject to set
         */
        public void setJsonObject(EsrsVeConnectedModel jsonObject) {
            this.jsonObject = jsonObject;
        }
    }

    public static class JsonStringResponse extends Response {
        private String jsonString;

        /**
         * @return the jsonObject
         */
        public String getJsonString() {
            return jsonString;
        }

        /**
         * @param jsonObject the jsonObject to set
         */
        public void setJsonString(String jsonString) {
            this.jsonString = jsonString;
        }
    }

}