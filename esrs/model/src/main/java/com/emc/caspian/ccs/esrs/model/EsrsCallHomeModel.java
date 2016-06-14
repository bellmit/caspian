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

import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.primitives.UnsignedLong;

/***
 *
 * @author kuppup
 *
 * This class is to have the callHome Model for ESRS VE. This Model will be used
 * to create the JSON data, which ESRS VE can understand and posted on callHome Request.
 *
 */
@JsonInclude(Include.NON_NULL)
public class EsrsCallHomeModel {
    @JsonProperty("connecthome")
    private ConnectHome connectHome = null;

    public EsrsCallHomeModel() {
        if (null == connectHome)
            connectHome = new ConnectHome();
    }

    public EsrsCallHomeModel(String transType, String transTypeDesc) {
        if (null == connectHome)
            connectHome = new ConnectHome(transType, transTypeDesc);
    }

    public ConnectHome getConnectHome() {
        return connectHome;
    }

    public void setConnectHome(ConnectHome connectHome) {
        this.connectHome = connectHome;
    }

    @JsonInclude(Include.NON_NULL)
    public class ConnectHome {
        @JsonProperty("schemaversion")
        private String schemaVersion;
        @JsonProperty("transtype")
        private String transType;
        @JsonProperty("transtypedesc")
        private String transTypeDesc;
        @JsonProperty("transid")
        private String transId;
        @JsonProperty("node")
        private Node node = null;

        ConnectHome() {
            if (null == node)
                node = new Node();
        }

        ConnectHome(String transType, String transTypeDesc) {
            this();
            this.transType = transType;
            this.transTypeDesc = transTypeDesc;
        }

        public String getTransType() {
            return transType;
        }

        public void setTransType(String transType) {
            this.transType = transType;
        }

        public String getTransTypeDesc() {
            return transTypeDesc;
        }

        public void setTransTypeDesc(String transTypeDesc) {
            this.transTypeDesc = transTypeDesc;
        }

        public Node getNode() {
            return node;
        }

        public void setNode(Node node) {
            this.node = node;
        }

        public void setNode(String id, String clarifyid, String deviceType,
                String model, String serialNumber) {
            this.node.setId(id);
            this.node.setIdentifier(clarifyid, deviceType, model, serialNumber);
        }

        public String getSchemaVersion() {
            return schemaVersion;
        }

        public void setSchemaVersion(String schemaVersion) {
            this.schemaVersion = schemaVersion;
        }

        public String getTransId() {
            return transId;
        }

        public void setTransId(String transId) {
            this.transId = transId;
        }
    }

    @JsonInclude(Include.NON_NULL)
    public class Node {
        @JsonProperty("status")
        private String status;
        @JsonProperty("state")
        private String state;
        @JsonProperty("id")
        private String id;
        @JsonProperty("identifier")
        private Identifier identifier = null;
        @JsonProperty("internalData")
        private InternalData internalData = null;
        @JsonProperty("externalFiles")
        private ExternalFiles externalFiles = null;

        Node() {
            if (null == identifier)
                identifier = new Identifier();
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Identifier getIdentifier() {
            return identifier;
        }

        public void setIdentifier(Identifier identifier) {
            this.identifier = identifier;
        }

        public void setIdentifier(String clarifyid, String deviceType,
                String model, String serialNumber) {
            this.identifier.setClarifyid(clarifyid);
            this.identifier.setDeviceType(deviceType);
            this.identifier.setModel(model);
            this.identifier.setSerialNumber(serialNumber);
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        /**
         * @return the internalData
         */
        public InternalData getInternalData() {
            return internalData;
        }

        /**
         * @param internalData the internalData to set
         */
        public void setInternalData(InternalData internalData) {
            this.internalData = internalData;
        }

        /**
         * @return the externalFiles
         */
        public ExternalFiles getExternalFiles() {
            return externalFiles;
        }

        /**
         * @param externalFiles the externalFiles to set
         */
        public void setExternalFiles(ExternalFiles externalFiles) {
            this.externalFiles = externalFiles;
        }
    }

    @JsonInclude(Include.NON_NULL)
    public class Identifier {
        @JsonProperty("deviceState")
        private String deviceState;
        @JsonProperty("deviceStatus")
        private String deviceStatus;
        @JsonProperty("clarifyID")
        private String clarifyid;
        @JsonProperty("siteName")
        private String siteName;
        @JsonProperty("vendor")
        private String vendor="EMC";
        @JsonProperty("deviceType")
        private String deviceType;
        @JsonProperty("model")
        private String model;
        @JsonProperty("serialNumber")
        private String serialNumber;
        @JsonProperty("wwn")
        private String wwn;
        @JsonProperty("platform")
        private String platform;
        @JsonProperty("os")
        private String os;
        @JsonProperty("osver")
        private String osVer;
        @JsonProperty("ucodeVer")
        private String ucodeVer;
        @JsonProperty("embedLevel")
        private String EmbedLevel;
        @JsonProperty("internalMaxSize")
        private UnsignedLong InternalMaxSize;
        @JsonProperty("comment")
        private String comment;

        public String getClarifyid() {
            return clarifyid;
        }

        public void setClarifyid(String clarifyid) {
            this.clarifyid = clarifyid;
        }

        public String getDeviceType() {
            return deviceType;
        }

        public void setDeviceType(String deviceType) {
            this.deviceType = deviceType;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getSerialNumber() {
            return serialNumber;
        }

        public void setSerialNumber(String serialNumber) {
            this.serialNumber = serialNumber;
        }

        public String getDeviceState() {
            return deviceState;
        }

        public void setDeviceState(String deviceState) {
            this.deviceState = deviceState;
        }

        public String getDeviceStatus() {
            return deviceStatus;
        }

        public void setDeviceStatus(String deviceStatus) {
            this.deviceStatus = deviceStatus;
        }

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }

        public String getWwn() {
            return wwn;
        }

        public void setWwn(String wwn) {
            this.wwn = wwn;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public String getOs() {
            return os;
        }

        public void setOs(String os) {
            this.os = os;
        }

        public String getOsVer() {
            return osVer;
        }

        public void setOsVer(String osVer) {
            this.osVer = osVer;
        }

        public String getUcodeVer() {
            return ucodeVer;
        }

        public void setUcodeVer(String ucodeVer) {
            this.ucodeVer = ucodeVer;
        }

        public String getEmbedLevel() {
            return EmbedLevel;
        }

        public void setEmbedLevel(String embedLevel) {
            EmbedLevel = embedLevel;
        }

        public UnsignedLong getInternalMaxSize() {
            return InternalMaxSize;
        }

        public void setInternalMaxSize(UnsignedLong internalMaxSize) {
            InternalMaxSize = internalMaxSize;
        }

        public String getComment() {
            return comment;
        }

        public void setComment(String comment) {
            this.comment = comment;
        }
    }

    public class EventList {
        @JsonProperty("event")
        private ArrayList<Event> event = null;

        public EventList() {
            if (null == event)
                event = new ArrayList<Event>();
        }

        public ArrayList<Event> getEvent() {
            return event;
        }

        public void setEvent(ArrayList<Event> event) {
            this.event = event;
        }

        public void addEvent(Event event) {
            this.event.add(event);
        }
    }

    @JsonInclude(Include.NON_NULL)
    public class Event {
        @JsonProperty("symptomCode")
        private String symptomCode;
        @JsonProperty("category")
        private String category;
        @JsonProperty("severity")
        private String severity;
        @JsonProperty("status")
        private String status;
        @JsonProperty("component")
        private String component;
        @JsonProperty("componentID")
        private String componentID;
        @JsonProperty("subComponent")
        private String subComponent;
        @JsonProperty("subComponentID")
        private String subComponentId;
        @JsonProperty("callHome")
        private String callHome;
        @JsonProperty("firstTime")
        private String firstTime;
        @JsonProperty("lastTime")
        private String lastTime;
        @JsonProperty("count")
        private int count=1;
        @JsonProperty("eventData")
        private String eventData;
        @JsonProperty("description")
        private String description;

        public String getSymptomCode() {
            return symptomCode;
        }

        public void setSymptomCode(String symptomCode) {
            this.symptomCode = symptomCode;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getComponent() {
            return component;
        }

        public void setComponent(String component) {
            this.component = component;
        }

        public String getComponentId() {
            return componentID;
        }

        public void setComponentId(String componentId) {
            this.componentID = componentId;
        }

        public String getSubComponent() {
            return subComponent;
        }

        public void setSubComponent(String subComponent) {
            this.subComponent = subComponent;
        }

        public String getSubComponentId() {
            return subComponentId;
        }

        public void setSubComponentId(String subComponentId) {
            this.subComponentId = subComponentId;
        }

        public String getCallHome() {
            return callHome;
        }

        public void setCallHome(String callHome) {
            this.callHome = callHome;
        }

        public String getFirstTime() {
            return firstTime;
        }

        public void setFirstTime(String firstTime) {
            this.firstTime = firstTime;
        }

        public String getLastTime() {
            return lastTime;
        }

        public void setLastTime(String lastTime) {
            this.lastTime = lastTime;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getEventData() {
            return eventData;
        }

        public void setEventData(String eventData) {
            this.eventData = eventData;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    @JsonInclude(Include.NON_NULL)
    public class InternalData {
        @JsonProperty("fileList")
        private FileList fileList = null;
        @JsonProperty("eventList")
        private EventList eventList = null;

        /**
         * @return the fileList
         */
        public FileList getFileList() {
            return fileList;
        }

        /**
         * @param fileList the fileList to set
         */
        public void setFileList(FileList fileList) {
            this.fileList = fileList;
        }

        public EventList getEventList() {
            return eventList;
        }

        public void setEventList(EventList eventList) {
            this.eventList = eventList;
        }

    }

    public class ExternalFiles { //extends InternalData {
        @JsonProperty("fileList")
        private FileList fileList = new FileList();

        /**
         * @return the fileList
         */
        public FileList getFileList() {
            return fileList;
        }

        /**
         * @param fileList the fileList to set
         */
        public void setFileList(FileList fileList) {
            this.fileList = fileList;
        }
    }

    public class FileList {
        @JsonProperty("file")
        private ArrayList<File> file = new ArrayList<File>();

        /**
         * @return the file
         */
        public ArrayList<File> getFile() {
            return file;
        }

        /**
         * @param file the file to set
         */
        public void setFile(ArrayList<File> file) {
            this.file = file;
        }

        public void addFile(String fileName, String key) {
            file.add(new File(fileName, key));
        }
    }

    public class File {
        @JsonProperty("fileName")
        private String fileName;
        @JsonProperty("key")
        private String key;
        /**
         * @param fileName
         * @param key
         */
        public File(String fileName, String key) {
            this.fileName = fileName;
            this.key = key;
        }

        /**
         * @return the fileName
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * @param fileName the fileName to set
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        /**
         * @return the key
         */
        public String getKey() {
            return key;
        }

        /**
         * @param key the key to set
         */
        public void setKey(String key) {
            this.key = key;
        }
    }
}