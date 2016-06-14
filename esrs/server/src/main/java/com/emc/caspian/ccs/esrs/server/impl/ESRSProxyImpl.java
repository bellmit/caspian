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
package com.emc.caspian.ccs.esrs.server.impl;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.Charsets;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.http.HttpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.client.response.HttpResponse;
import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.esrs.api.CaspianEsrsProxyApi;
import com.emc.caspian.ccs.esrs.model.EsrsCallHomeModel;
import com.emc.caspian.ccs.esrs.model.EsrsCallHomeModel.ConnectHome;
import com.emc.caspian.ccs.esrs.model.EsrsCallHomeModel.Event;
import com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyEventModel;
import com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel;
import com.emc.caspian.ccs.esrs.model.EsrsVeConnectedModel;
import com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel;
import com.emc.caspian.ccs.esrs.model.PropertyCollectorScheduleModel;
import com.emc.caspian.ccs.esrs.server.controller.Controller;
import com.emc.caspian.ccs.esrs.server.controller.EtcdAccessException;
import com.emc.caspian.ccs.esrs.server.controller.HandleRequestDelegate;
import com.emc.caspian.ccs.esrs.server.controller.Protocol;
import com.emc.caspian.ccs.esrs.server.controller.TransformDelegate;
import com.emc.caspian.ccs.esrs.server.exception.PayLoadException;
import com.emc.caspian.ccs.esrs.server.helper.PropCollectorUtil;
import com.emc.caspian.ccs.esrs.server.helper.TestESRSConnectionUtil;
import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.ESRSHmacUtil;
import com.emc.caspian.ccs.esrs.server.util.ESRSProxyRestClient;
import com.emc.caspian.ccs.esrs.server.util.ESRSUtil;
import com.emc.caspian.ccs.esrs.server.util.EtcdUtils;
import com.emc.caspian.ccs.esrs.server.util.RestClientUtil;
import com.emc.caspian.crs.model.ServiceRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public class ESRSProxyImpl implements CaspianEsrsProxyApi.EsrsProxy {

    private static final Logger _log = LoggerFactory.getLogger(ESRSProxyImpl.class);

    private static final String CASPIAN_NODE_SUFFIXES = "caspian-node-suffixes";

    private static final String HTTP_SECURED = "https://";
    private static final String HTTP_UNSECURED = "http://";
    private static final String SLASH = "/";
    private static final String NO = "No";
    private static final String YES = "Yes";
    private static final String TRUE = "true";
    private static final String NULL = "null";

    private static final String TEST_ESRS_MODEL = "TEST_ESRS_MODEL";
    private static final String TEST_ESRS_SERIAL_NUMBER = "TEST_ESRS_SERIAL_NUMBER";
    private static final String TEST_ECI_HOST = "TEST_ECI_HOST";
    private static final String KEEP_ALIVE = "keepalive";
    private static final String CONNECT_EMC = "connectemc";
    private static final String TOPOLOGY = "topology";
    private static final String CASPIAN_LICENSE = "license";
    private static final String LICENSE_URI = "/v1/licenses";
    private static final String LICENSES = "licenses";
    private static final String LIC_SWID = "SWID";
    private static final String PROPERTIES = "properties";
    private static final String FILE_KEY = "fileKey";
    private static final String TMP_UPL = "/tmp/upl-";
    private static final String FILE = "file";
    private static final String HMAC_DATE_FORMAT = "E, dd MMM yyyy HH:mm:ss z";
    private static final String DOMAIN_DEVICE = ",domain=Device";
    private static final String ZERO = "0";
    private static final List<String> callHomeMustHaveProps = Arrays.asList("EventList");

    @Override
    public Response register(final HttpServletRequest servRequest,
            final EsrsVeRegistrationModel registrationDetails) {
        //TODO Must validate if all mandatory i/ps are provided
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.GateWayResponse process() throws Exception {
                        _log.info("Registering ECI with ESRS VE");
                        if (!ESRSUtil.isValidPayload(registrationDetails, Constants.regMustHaveProps)) {
                            _log.error(Constants.INV_PAYLOAD);
                            throw new PayLoadException(Constants.INV_PAYLOAD);
                        }
                        persistToEtcd(Constants.ESRS_LAST_PROP_COLL_TIME, ZERO);
                        DeviceDetails device = fetchDeviceDetails(servRequest.getHeader(Constants.AUTH_TOKEN_KEY));
                        return registerWithESRSVE(registrationDetails, device);
                    }
                },
                new TransformDelegate<Protocol.GateWayResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.GateWayResponse response) {
                        ResponseBuilder rb = Response.status(response.getStatus().value());
                        if (response.getStatus().value() >= 400) {
                            rb.entity(response.getJsonObject().getMessage());
                        } else {
                            rb.entity(response.getJsonObject());
                        }
                        return rb;
                    }
                }, Controller.applicationExceptionMapper);

        return response;
    }

    @Override
    public Response registerationDetails() {

        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                //TODO uncomment once auth filter dependencies are added getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.GateWayResponse>()
                {
                    @Override
                    public Protocol.GateWayResponse process() throws Exception {
                        final Protocol.GateWayResponse response = new Protocol.GateWayResponse();
                        _log.info("Reading ESRS VE details ...");
                        response.setStatus(Protocol.Status.OK);
                        response.setJsonObject(buildRegModel());
                        return response;
                    }
                },
                new TransformDelegate<Protocol.GateWayResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.GateWayResponse response) {
                        return Response.ok().entity(response.getJsonObject());
                    }
                }, Controller.applicationExceptionMapper);

        return response;
    }

    private DeviceDetails fetchDeviceDetails(String authToken) throws Exception {
        //The following block is added for handling Unit Test environments with
        //Test Serial number, model and ECI Host.
        DeviceDetails device = new DeviceDetails();
        Map<String, String> eciHostNames = new LinkedHashMap<String, String>();
        int nodeIndex = 1;

        if(System.getenv().containsKey(TEST_ESRS_SERIAL_NUMBER)
                && System.getenv().containsKey(TEST_ESRS_MODEL)
                && System.getenv().containsKey(TEST_ECI_HOST)) {
            device.setModel(System.getenv(TEST_ESRS_MODEL));
            device.setSerialNumber(System.getenv(TEST_ESRS_SERIAL_NUMBER));

            eciHostNames.put(Integer.toString(nodeIndex), System.getenv(TEST_ECI_HOST));
            device.setEciHostNames(eciHostNames);
            return device;
        }

        //Reading VIP from CRS using NIS
        String vipNode = ESRSUtil.getVIPFromCRS(Constants.PLATFORM, Constants.NODE_INVENTORY);
        if (null != vipNode){
            //If any other suffix for VIP is implemented,  need to changes the
            //node index to other.
            eciHostNames.put(Integer.toString(nodeIndex), vipNode);
        }

        //Reading Max number of supported platform Node IPs from NIS,
        JSONArray nodesArray = ESRSUtil.getAllPlatformNodes();
        int maxPlatformNodesForReg = Math.min(nodesArray.length(), Constants.MAX_PLATFORM_NODES_SUPPORTED);

        for (int i = 0; i < maxPlatformNodesForReg; i++) {
            JSONObject node = (JSONObject) nodesArray.get(i);
            JSONObject topology = node.getJSONObject(TOPOLOGY);
            eciHostNames.put(Integer.toString(++nodeIndex), topology.getString(Constants.ECI_NODE_IP));
        }

        //Product/Model is fixed to CASPIAN
        device.setModel(Constants.PRODUCT_NAME);

        //Using SWID for SN
        StringBuilder licenseURI = new StringBuilder(ESRSUtil.getEndPointFromCRS(Constants.PLATFORM, CASPIAN_LICENSE));
        licenseURI.append(LICENSE_URI);

        Map<String,String> httpHeaders = new HashMap<String, String>();
        httpHeaders .put(Constants.AUTH_TOKEN_KEY, authToken);

        org.apache.http.HttpResponse getLicenseResp = RestClientUtil.httpGETRequest(licenseURI.toString(),httpHeaders).get();
        String licRespStr = EntityUtils.toString(getLicenseResp.getEntity(), Charsets.UTF_8);

        try {
            JSONObject licenses = new JSONObject(licRespStr);
            JSONArray licensesArray = licenses.getJSONArray(LICENSES);

            if ( null != licensesArray && licensesArray.length() >0 ) {
                JSONObject license = (JSONObject) licensesArray.get(0);
                JSONObject licenseProperties = license.getJSONObject(PROPERTIES);
                device.setSerialNumber(licenseProperties.getString(LIC_SWID));
            } else {
                _log.error("Unable to fetch SWID details from CRS/License");
            }
        } catch (JSONException e) {
            _log.error("Unable to fetch SWID details from CRS/License");
        }
        device.setEciHostNames(eciHostNames);

        return device;
    }

    public class DeviceDetails {
        private String model;
        private String serialNumber;
        private Map<String, String> eciHostNames;

        /**
         * @return the model
         */
        public String getModel() {
            return model;
        }

        /**
         * @param model
         *            the model to set
         */
        public void setModel(final String model) {
            this.model = model;
        }

        /**
         * @return the serialNumber
         */
        public String getSerialNumber() {
            return serialNumber;
        }

        /**
         * @param serialNumber
         *            the serialNumber to set
         */
        public void setSerialNumber(final String serialNumber) {
            this.serialNumber = serialNumber;
        }

        /**
         * @return the list of eciHostName
         */
        public Map<String, String> getEciHostNames() {
            return eciHostNames;
        }

        /**
         * @param eciHostName
         *            the list eciHostName to set
         */
        public void setEciHostNames(final Map<String, String> eciHostNames) {
            this.eciHostNames = eciHostNames;
        }
    }

    @Override
    public Response healthStatus() {
        //TODO Must validate if all mandatory i/ps are provided
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                //TODO uncomment once auth filter dependencies are added
                //                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.Response process() throws Exception {
                        final Protocol.GateWayConnectedResponse response = new Protocol.GateWayConnectedResponse();

                        //Setting connected No and will be overwritten when health status posting success.
                        //This is to handle exception case scenario's
                        persistToEtcd(Constants.CASPIAN_PROXY_VE_CONNECTED, NO);

                        Integer nodeCount = Integer.valueOf(fetchFromEtcd(Constants.ETCD_PLATFORM_NODE_COUNT));
                        String model = fetchFromEtcd(Constants.CASPIAN_MODEL);
                        String serialNumber = fetchFromEtcd(Constants.CASPIAN_SERIAL_NUMBER);

                        for (int index=1; index <= nodeCount; index++) {
                            String productRegkey = fetchFromEtcd(Constants.ETCD_PRODUCTION_KEY + Constants.HYPHEN + index);
                            StringBuilder  serNumIndex = new StringBuilder(serialNumber);
                            serNumIndex.append(Constants.HYPHEN);
                            serNumIndex.append(fetchFromEtcd(CASPIAN_NODE_SUFFIXES + Constants.HYPHEN + index));

                            StringBuilder gatewayURIEndPoint = getESRSURIEndpoint(model, serNumIndex.toString(), KEEP_ALIVE);

                            Map<String, Object> httpHeaders = new HashMap<String, Object>();
                            StringBuilder gatewayURI = getESRSBaseURI();

                            gatewayURI.append(gatewayURIEndPoint);

                            //Use the device key and hmacdate to generate an HMAC for this ECI
                            DateFormat dateFormat = new SimpleDateFormat(HMAC_DATE_FORMAT);
                            Date todayDate = new Date();
                            String esrsHmacDate = dateFormat.format(todayDate);

                            StringBuilder authString = new StringBuilder(model);
                            authString.append(Constants.COLON).append(
                                    serNumIndex).append(Constants.COLON).
                            append(ESRSHmacUtil.getESRSHmac(HttpPost.METHOD_NAME, MediaType.APPLICATION_JSON, gatewayURIEndPoint.toString(), productRegkey, esrsHmacDate))
                            .append(DOMAIN_DEVICE);

                            _log.debug("Using auth: {}" , authString);

                            httpHeaders.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                            httpHeaders.put(HTTP.DATE_HEADER, esrsHmacDate);
                            httpHeaders.put(HttpHeader.AUTHORIZATION.asString(), authString.toString());

                            _log.debug("Posting Health Status of ECI to ESRS VE on {}", gatewayURI.toString());

                            HttpResponse<String> postResponse = ESRSProxyRestClient.post(gatewayURI.toString()
                                    , httpHeaders, null);

                            int respStatus = postResponse.getStatusCode();

                            _log.debug("Response Status : {}" , respStatus);

                            if(200 == respStatus) {
                                _log.info("Health Status Posting for serial number {} to ESRS VE is successfully completed",
                                        serNumIndex);
                            } else {
                                _log.error("Health Status Posting to ESRS VE for Serial Number {} is failed with {} {}",
                                        new Object[]{serNumIndex , respStatus, postResponse.getErrorMessage()});
                                throw new Exception(postResponse.getErrorMessage());
                            }
                        }
                        response.setStatus(Protocol.Status.OK);
                        response.setJsonObject(new EsrsVeConnectedModel(YES));
                        //persisting the last successful keepAliveTime
                        persistToEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME, String.valueOf(System.currentTimeMillis()));
                        persistToEtcd(Constants.CASPIAN_PROXY_VE_CONNECTED, YES);

                        return response;
                    }
                },
                new TransformDelegate<Protocol.GateWayConnectedResponse>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.GateWayConnectedResponse response) {
                        //TODO do required transformation if any
                        return Response.ok().entity(response.getJsonObject());
                    }
                }, Controller.applicationExceptionMapper);

        return response;
    }
    @Override
    public Response callHome(final EsrsCallHomeProxyModel callHomeProxy) {
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.Response process() throws Exception {

                        _log.info("ECI callHome to ESRS VE. Will check if CallHome is enabled.");
                        
                        //First of all see if callHome is enabled.
                        String callHomeConfig = EtcdUtils.fetchFromEtcd(Constants.IS_ESRS_CALL_HOME_ENABLED);
                        if( callHomeConfig.matches(Constants.FALSE)){

                            final Protocol.JsonStringResponse response = new Protocol.JsonStringResponse();
                            _log.error("CallHome failed. CallHome is disabled.");
                            response.setJsonString(Constants.CALL_HOME_NOT_ENABLED);
                            response.setStatus(Protocol.Status.PRECONDITION_FAILED);
                            return response;
                        }

                        _log.debug("Call Home is enabled. Continuing");
                        return postCallHomeJSON(callHomeProxy, null);
                    }
                },
                new TransformDelegate<Protocol.Response>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.Response response) {
                        return Response.ok();
                    }
                }, Controller.applicationExceptionMapper);

        return response;
    }

    private EsrsCallHomeModel buildConnectEMCJSON(
            EsrsCallHomeProxyModel callHomeProxyModel, String model, String serialNumber, HashMap<File, String> fileList) throws Exception {
        EsrsCallHomeModel callHomeModel = new EsrsCallHomeModel();

        //Populate the connectHome Object based on incoming request.
        ConnectHome connectHome = callHomeModel.getConnectHome();

        connectHome.getNode().setId(serialNumber);

        connectHome.getNode().setIdentifier(serialNumber, Constants.PRODUCT_NAME
                , model, serialNumber);

        //Reading the emc_os_base for OS_VER
        JSONArray nodesArray = ESRSUtil.getAllPlatformNodes();
        if (nodesArray.length() > 0) {
            JSONObject node = (JSONObject) nodesArray.get(0);
            JSONObject topology = node.getJSONObject(TOPOLOGY);

            //For VMs, emc_os_base will be unknown
            String OS_Version = Constants.UNKNOWN;
            if (topology.has(Constants.ECI_BASE_OS_VER)) {
                OS_Version = topology.getString(Constants.ECI_BASE_OS_VER);
            }
            connectHome.getNode().getIdentifier().setOsVer(OS_Version);
        }

        //Reading the Package Version for UcodeVer
        ServiceRecord servRecord = ESRSUtil.getServiceFromCRS(Constants.PLATFORM);
        String UcodeVer = Constants.UNKNOWN;
        if (null != servRecord && null != servRecord.getVersion()) {
            UcodeVer = servRecord.getVersion();
        }
        connectHome.getNode().getIdentifier().setUcodeVer(UcodeVer);

        for(EsrsCallHomeProxyEventModel proxyEvent : callHomeProxyModel.getEventList()) {
            if (null == connectHome.getNode().getInternalData()) {
                connectHome.getNode().setInternalData(callHomeModel.new InternalData());
                connectHome.getNode().getInternalData().setEventList(callHomeModel.new EventList());
            }
            Event event = callHomeModel.new Event();
            event.setCategory(proxyEvent.getCategory());
            event.setSeverity(proxyEvent.getSeverity());
            event.setStatus(proxyEvent.getStatus());
            event.setSymptomCode(proxyEvent.getSymptomCode());
            event.setEventData(proxyEvent.getEventData());
            event.setDescription(proxyEvent.getEventDescription());
            event.setComponent(proxyEvent.getComponentID());
            event.setSubComponent(proxyEvent.getSubComponentID());
            event.setCallHome(YES);
            Date dt = new Date();
            String sdfDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(dt);
            event.setFirstTime(sdfDate);
            _log.debug("Posting Symtom Code : {} ", proxyEvent.getSymptomCode());
            connectHome.getNode().getInternalData().getEventList().addEvent(event);
        }

        if(null != fileList){
            for (Map.Entry<File, String> file : fileList.entrySet())
            {
                _log.debug(file.getKey().getName(), file.getValue());
                String fileName = file.getKey().getName(), fileKey = file.getValue();
                if (fileName.endsWith(".txt") || fileName.endsWith(".log") ||
                        fileName.endsWith(".xml")) {
                    if (null == connectHome.getNode().getInternalData()) {
                        connectHome.getNode().setInternalData(callHomeModel.new InternalData());
                    }
                    if (null == connectHome.getNode().getInternalData().getFileList()) {
                        connectHome.getNode().getInternalData().setFileList(callHomeModel.new FileList());
                    }
                    connectHome.getNode().getInternalData().getFileList().addFile(fileName, fileKey);
                } else {
                    if (null == connectHome.getNode().getExternalFiles()) {
                        connectHome.getNode().setExternalFiles(callHomeModel.new ExternalFiles());
                    }
                    if (null == connectHome.getNode().getExternalFiles().getFileList()) {
                        connectHome.getNode().getExternalFiles().setFileList(callHomeModel.new FileList());
                    }
                    connectHome.getNode().getExternalFiles().getFileList().addFile(fileName, fileKey);
                }
            }
        }
        return callHomeModel;
    }

    private static void persistToEtcd(String key, String value) {

        try {
            EtcdUtils.updateKeyValToEtcd(HttpPut.METHOD_NAME, key, value);
        } catch (ExecutionException e) {
            _log.error("Could not persist "+key+" to ETCD.", e);
        } catch (Exception e) {
            _log.error("Could not persist "+key+" to ETCD.", e);
        }
    }

    private static String fetchFromEtcd(String key) {

        String value = null;
        try {
            value = EtcdUtils.fetchValueFromEtcd(key);
        } catch (ExecutionException | IOException | InterruptedException e) {
            _log.error("Could not fetch key from ETCD.", e);
            throw new EtcdAccessException("Could not fetch key from ETCD.");
        }

        return value;
    }

    private void deleteKeyFromEtcd(String key) throws Exception {
        EtcdUtils.updateKeyValToEtcd(HttpDelete.METHOD_NAME, key, null);
    }

    @Override
    public Response deleteRegistration() {
        //TODO Must validate if all mandatory i/ps are provided
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(
                request,
                //TODO uncomment once auth filter dependencies are added
                //                getKeystonePrincipal(),
                new HandleRequestDelegate<Protocol.Response>()
                {
                    @Override
                    public Protocol.Response process() throws Exception {
                        final Protocol.Response response = new Protocol.Response();

                        Integer nodeCount = Integer.valueOf(fetchFromEtcd(Constants.ETCD_PLATFORM_NODE_COUNT));
                        String model = fetchFromEtcd(Constants.CASPIAN_MODEL);
                        String serialNumber = fetchFromEtcd(Constants.CASPIAN_SERIAL_NUMBER);


                        for (int index=1; index <= nodeCount; index++) {
                            String productRegkey = Constants.ETCD_PRODUCTION_KEY + Constants.HYPHEN + index;
                            String productRegValue = fetchFromEtcd(productRegkey);
                            StringBuilder  serNumIndex = new StringBuilder(serialNumber);
                            serNumIndex.append(Constants.HYPHEN);
                            serNumIndex.append(fetchFromEtcd(CASPIAN_NODE_SUFFIXES + Constants.HYPHEN + index));

                            _log.debug("Product registration key retrieved from etcd store :: {}", productRegkey);
                            StringBuilder gatewayURI = getESRSBaseURI();

                            HttpResponse<String> deleteResponse = deleteESRSVERegistration(model, serNumIndex.toString(), productRegValue, gatewayURI);

                            int statusCode = deleteResponse.getStatusCode();

                            _log.debug("Status : {}" , statusCode);

                            if(200 == statusCode) {
                                _log.info("UnRegistering ECI Node with Serial Number {} from the ESRS VE  is successfully completed", serNumIndex);
                                deleteKeyFromEtcd(productRegkey);
                            } else {
                                _log.error("Deleting(UnRegistering) ECI Node with Serial Number {} "
                                        + "from the ESRS VE  is failed with {}, {}" ,
                                        new Object[]{serNumIndex , statusCode, deleteResponse.getErrorMessage()});
                                throw new Exception(deleteResponse.getErrorMessage());
                            }
                        }
                        response.setStatus(Protocol.Status.OK);
                        deleteKeyFromEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME);
                        deleteKeyFromEtcd(Constants.CASPIAN_PROXY_VE_REGISTERED);
                        deleteKeyFromEtcd(Constants.CASPIAN_PROXY_VE_CONNECTED);
                        return response;
                    }
                },
                new TransformDelegate<Protocol.Response>()
                {
                    @Override
                    public ResponseBuilder transform(final Protocol.Response response) {
                        return Response.ok();
                    }
                }, Controller.applicationExceptionMapper);

        return response;
    }

    private EsrsVeRegistrationModel buildRegModel() {
        //We are assuming that MnR will be sending the keepAlive requests every predefined
        //interval. But incase MnR is down and connection with ESRS VE is no longer existing due to
        //some reason, that in that case UI may still show Connection status as "Connected"

        EsrsVeRegistrationModel veRegistrationDetails = new EsrsVeRegistrationModel();

        String veConnected = NO;
        try {
            veConnected = fetchFromEtcd(Constants.CASPIAN_PROXY_VE_CONNECTED);
        } catch (EtcdAccessException e){
            _log.debug(e.getMessage());
        }
        veRegistrationDetails.setVeConnected(veConnected);

        String veRegistered = NO;
        try {
            veRegistered = fetchFromEtcd(Constants.CASPIAN_PROXY_VE_REGISTERED);
        } catch (EtcdAccessException e){
            _log.debug(e.getMessage());
        }
        if (veRegistered.equals(NO)) {
            return veRegistrationDetails;
        }

        final String lastKeepAliveTime = fetchFromEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME);
        final String esrsEnabled = fetchFromEtcd(Constants.ESRS_ENABLED);
        final String caspianModel = fetchFromEtcd(Constants.CASPIAN_MODEL);
        final String caspianSerialNumber = fetchFromEtcd(Constants.CASPIAN_SERIAL_NUMBER);
        final String caspianNode = fetchFromEtcd(Constants.CASPIAN_HOST_NAME);//TODO convert hostname to IP

        final String veIp = fetchFromEtcd(Constants.ETCD_VE_IP);
        _log.debug("ESRS VE IP retrived from etcd store :: {}" , veIp);

        final String vePort = fetchFromEtcd(Constants.ETCD_VE_PORT);
        _log.debug("ESRS VE Port retrived from etcd store :: {}" , vePort);

        final String veSsl = fetchFromEtcd(Constants.ETCD_VE_SSL);
        _log.debug("ESRS VE Ssl retrived from etcd store :: {}" , veSsl);

        final String veUsername = fetchFromEtcd(Constants.ETCD_VE_USERNAME);
        _log.debug("ESRS VE IP retrived from etcd store :: {}" , veUsername);

        //  ccs-2121. Do not fetch password from etcd
        final String vePassword = "****"; 
        
        veRegistrationDetails.setEnabled(esrsEnabled).setLastKeepAliveTime(lastKeepAliveTime).
        setCaspianModel(caspianModel).setCaspianSerialNumber(caspianSerialNumber).setCaspianNode(caspianNode).
        setGateway(veIp).setPort(vePort).setSsl(Boolean.parseBoolean(veSsl))
        .setUsername(veUsername).setPassword(vePassword);

        return veRegistrationDetails;
    }

    private StringBuilder getESRSBaseURI(EsrsVeRegistrationModel registrationDetails) {
        StringBuilder gatewayBaseURI = new StringBuilder();
        //Added this check for UT to handle Mock ESRS VE as non ssl..
        if(System.getenv().containsKey(TEST_ESRS_SERIAL_NUMBER)) {
            gatewayBaseURI.append(HTTP_UNSECURED);
        } else {
            gatewayBaseURI.append(HTTP_SECURED);
        }
        gatewayBaseURI.append(registrationDetails.getGateway()).append(
                Constants.COLON).append(registrationDetails.getPort());
        return gatewayBaseURI;
    }

    private StringBuilder getESRSBaseURI() throws Exception {
        EsrsVeRegistrationModel veRegDetails = new EsrsVeRegistrationModel();
        veRegDetails.setGateway(fetchFromEtcd(Constants.ETCD_VE_IP));
        veRegDetails.setPort(fetchFromEtcd(Constants.ETCD_VE_PORT));
        veRegDetails.setSsl(Boolean.parseBoolean(fetchFromEtcd(Constants.ETCD_VE_SSL)));
        return getESRSBaseURI(veRegDetails);
    }

    private StringBuilder getESRSURIEndpoint(String model,
            String serialNumber, String esrsAction) {
        StringBuilder esrsURIEndpoint = new StringBuilder(
                Constants.ESRS_VE_V1_ENDPOINT_URI);
        esrsURIEndpoint.append(model).append(SLASH).append(serialNumber);
        if (null != esrsAction){
            esrsURIEndpoint.append(SLASH).append(esrsAction);
        }
        return esrsURIEndpoint;
    }

    @Override
    public Response callHomeFileUpload(HttpServletRequest servRequest) {
        final Protocol.RegistrationRequest request = new Protocol.RegistrationRequest();

        Response response = Controller.handleRequest(request,
                new HandleRequestDelegate<Protocol.Response>() {
            @Override
            public Protocol.Response process() throws Exception {

                //First of all see if callHome is enabled.
                String callHomeConfig = EtcdUtils.fetchFromEtcd(Constants.IS_ESRS_CALL_HOME_ENABLED);
                if( callHomeConfig.matches(Constants.FALSE)){

                    final Protocol.JsonStringResponse response = new Protocol.JsonStringResponse();
                    _log.error("CallHome with File upload failed. CallHome is disabled.");
                    response.setJsonString(Constants.CALL_HOME_NOT_ENABLED);
                    response.setStatus(Protocol.Status.PRECONDITION_FAILED);
                    return response;
                }

                final Protocol.Response response = new Protocol.Response();
                response.setStatus(Protocol.Status.BAD_REQUEST);

                final HashMap<File, String> fileList = new HashMap<File, String>();
                final String FILE_UPLOAD_PATH = TMP_UPL + UUID.randomUUID();
                final File savedDir = new File(FILE_UPLOAD_PATH);
                StringBuilder symJsonBody = null;
                if (ServletFileUpload.isMultipartContent(servRequest)) {
                    final FileItemFactory factory = new DiskFileItemFactory();
                    final ServletFileUpload fileUpload = new ServletFileUpload(
                            factory);
                    try {
                        final List<FileItem> items = fileUpload
                                .parseRequest(servRequest);

                        for (FileItem item : items) {
                            final String itemName = item.getName();
                            if (!item.isFormField()) {
                                _log.debug("I/P File = {}", itemName);

                                if (!savedDir.exists()) {
                                    savedDir.mkdirs();
                                }
                                final File savedFile = new File(
                                        FILE_UPLOAD_PATH
                                        + File.separator
                                        + itemName);

                                _log.debug("Saving the file: {}",
                                        savedFile.getAbsolutePath());
                                item.write(savedFile);

                                fileList.put(savedFile, null);
                            } else {
                                if (null == symJsonBody) {
                                    symJsonBody = new StringBuilder();
                                }
                                symJsonBody.append(item.getString(Charsets.UTF_8.displayName()));
                            }
                        }
                    } catch (Exception e) {
                        response.setStatus(Protocol.Status.ERROR_INTERNAL);
                    }

                    if (null == symJsonBody) {
                        _log.error("Symptom data is missing.");
                        response.setStatus(Protocol.Status.BAD_REQUEST);
                    } else if (symJsonBody != null && fileList.size() == 0) {
                        // if dataFilePath is null and jsonPath is not
                        // empty means user tries to post only symptom json.
                        _log.info("Redirecting to callHome Alert !! ");
                        EsrsCallHomeProxyModel callHomeProxy = JsonHelper
                                .deserializeFromJson(symJsonBody.toString(),
                                        EsrsCallHomeProxyModel.class);

                        return postCallHomeJSON(callHomeProxy, null);
                    } else if (symJsonBody != null
                            && fileList.size() > 0) {
                        _log.info("Processing with callHome FileUpload with Alert !! ");
                        final int eciIndex = getAnyECINodeIndex();

                        String productRegkey = new String(fetchFromEtcd(Constants.ETCD_PRODUCTION_KEY + Constants.HYPHEN + eciIndex));

                        _log.debug("Product registration key retrieved "
                                + "from etcd store :: {} ", productRegkey);

                        String model = fetchFromEtcd(Constants.CASPIAN_MODEL);

                        StringBuilder serialNumber = new StringBuilder(fetchFromEtcd(Constants.CASPIAN_SERIAL_NUMBER));
                        serialNumber.append(Constants.HYPHEN);
                        serialNumber.append(fetchFromEtcd(CASPIAN_NODE_SUFFIXES + Constants.HYPHEN + eciIndex));

                        StringBuilder gatewayURIEndPoint = getESRSURIEndpoint(model, serialNumber.toString(), CONNECT_EMC);

                        StringBuilder gatewayURI = getESRSBaseURI();
                        gatewayURI.append(gatewayURIEndPoint);

                        Map<String, String> httpHeaders = new HashMap<String, String>();

                        for (File file : fileList.keySet()) {
                            DateFormat dateFormat = new SimpleDateFormat(
                                    HMAC_DATE_FORMAT);
                            Date todayDate = new Date();
                            String esrsHmacDate = dateFormat.format(todayDate);

                            StringBuilder authString = new StringBuilder(
                                    model);
                            authString.append(Constants.COLON).append(
                                    serialNumber).append(Constants.COLON);
                            authString.append(ESRSHmacUtil.getESRSHmac(
                                    HttpPut.METHOD_NAME,
                                    MediaType.MULTIPART_FORM_DATA,
                                    gatewayURIEndPoint.toString(),
                                    productRegkey,
                                    esrsHmacDate))
                            .append(DOMAIN_DEVICE);

                            httpHeaders.clear();
                            httpHeaders.put(HTTP.DATE_HEADER,
                                    esrsHmacDate);
                            httpHeaders.put(HttpHeader.ACCEPT.asString()
                                    ,ContentType.APPLICATION_JSON.toString());
                            httpHeaders.put(
                                    HttpHeader.AUTHORIZATION.asString(),
                                    authString.toString());

                            FileBody bin = new FileBody(file);
                            MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
                            reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                            reqEntity.addPart(FILE, bin);

                            org.apache.http.HttpResponse mpPutResp = RestClientUtil.httpMultiPartPutRequest(gatewayURI.toString(),
                                    httpHeaders, reqEntity.build());

                            if (200 == mpPutResp.getStatusLine()
                                    .getStatusCode()) {
                                String filePUTResp = EntityUtils
                                        .toString(mpPutResp
                                                .getEntity());
                                JSONObject filePutDetails = new JSONObject(
                                        filePUTResp);
                                fileList.put(file, filePutDetails
                                        .getString(FILE_KEY));
                                if (file.exists()) {
                                    file.delete();
                                }
                            } else {
                                _log.error(
                                        "Processing with callHome FileUpload with Alert  is failed with {}",
                                        mpPutResp
                                        .getStatusLine()
                                        .getStatusCode());
                                throw new Exception(mpPutResp
                                        .getStatusLine()
                                        .getReasonPhrase());
                            }
                        }
                        _log.info("File Upload success, Posting file key(s) with status");
                        EsrsCallHomeProxyModel callHomeProxy = JsonHelper
                                .deserializeFromJson(symJsonBody.toString(),
                                        EsrsCallHomeProxyModel.class);

                        if (savedDir.exists()) {
                            savedDir.delete();
                        }
                        return postCallHomeJSON(callHomeProxy, fileList,
                                eciIndex);
                    }
                }
                return response;
            }
        }, new TransformDelegate<Protocol.Response>() {
            @Override
            public ResponseBuilder transform(
                    final Protocol.Response response) {
                ResponseBuilder rb = Response.status(response.getStatus().value());
                return rb;
            }
        }, Controller.applicationExceptionMapper);

        return response;
    }

    private Protocol.Response postCallHomeJSON(EsrsCallHomeProxyModel callHomeProxy,
            HashMap<File, String> fileList) throws Exception {
        return postCallHomeJSON(callHomeProxy, fileList, -1);
    }

    private Protocol.Response postCallHomeJSON(final EsrsCallHomeProxyModel callHomeProxy,
            final HashMap<File, String> fileList,final int SNSuffixIndex) throws Exception {

        final int eciIndex;

        Map<String, Object> httpHeaders = new HashMap<String, Object>();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        final Protocol.Response response = new Protocol.Response();

        String veConnected = fetchFromEtcd(Constants.CASPIAN_PROXY_VE_CONNECTED);
        if (veConnected.equals(NO)) {
            throw new EtcdAccessException(Constants.CASPIAN_PROXY_VE_CONNECTED);
        }

        if (!ESRSUtil.isValidPayload(callHomeProxy, callHomeMustHaveProps)) {
            _log.error(Constants.INV_PAYLOAD);
            throw new PayLoadException(Constants.INV_PAYLOAD);
        }

        if (SNSuffixIndex == -1) {
            eciIndex = getAnyECINodeIndex();
        } else {
            eciIndex = SNSuffixIndex;
        }

        String productRegkey = new String(fetchFromEtcd(Constants.ETCD_PRODUCTION_KEY + Constants.HYPHEN + eciIndex));
        String model = fetchFromEtcd(Constants.CASPIAN_MODEL);

        StringBuilder baseSN = new StringBuilder(fetchFromEtcd(Constants.CASPIAN_SERIAL_NUMBER));
        StringBuilder serialNumber = new StringBuilder(baseSN);
        serialNumber.append(Constants.HYPHEN);
        serialNumber.append(fetchFromEtcd(CASPIAN_NODE_SUFFIXES + Constants.HYPHEN + eciIndex));

        StringBuilder gatewayURIEndPoint = getESRSURIEndpoint(model, serialNumber.toString(), CONNECT_EMC);
        StringBuilder gatewayURI = getESRSBaseURI();
        gatewayURI.append(gatewayURIEndPoint);

        EsrsCallHomeModel connectHomeData = buildConnectEMCJSON(callHomeProxy,
                model, baseSN.toString(), fileList);
        String postMessageBody = ow.writeValueAsString(connectHomeData);

        _log.debug(postMessageBody);

        DateFormat dateFormat = new SimpleDateFormat(HMAC_DATE_FORMAT);
        Date todayDate = new Date();
        String esrsHmacDate = dateFormat.format(todayDate);

        StringBuilder authString = new StringBuilder(model);
        authString.append(Constants.COLON).append(serialNumber);
        authString.append(Constants.COLON).append(ESRSHmacUtil.getESRSHmac(HttpPost.METHOD_NAME,
                MediaType.APPLICATION_JSON, gatewayURIEndPoint.toString(),
                productRegkey, esrsHmacDate)).append(DOMAIN_DEVICE);
        _log.debug("Using auth: {}", authString);

        httpHeaders.put(HTTP.DATE_HEADER, esrsHmacDate);
        httpHeaders.put(HttpHeader.AUTHORIZATION.asString(), authString.toString());
        httpHeaders.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        HttpResponse<String> postResponse = ESRSProxyRestClient.post(gatewayURI.toString()
                , httpHeaders, postMessageBody);

        int respStatus = postResponse.getStatusCode();

        String body = postResponse.getResponseBody();
        _log.debug("Got response body: {}", body);

        if (200 == respStatus) {
            _log.info("connectEMC Posting to ESRS VE is successfully completed");
            response.setStatus(Protocol.Status.OK);
        } else {
            _log.info("connectEMC Posting to ESRS VE is failed with {}", respStatus);
            throw new Exception(postResponse.getErrorMessage());
        }
        return response;
    }

    private int getAnyECINodeIndex() throws NumberFormatException, Exception {
        //Picking any random ECI Node Serial Number for posting callHome
        Integer nodeCount = Integer.valueOf(fetchFromEtcd(Constants.ETCD_PLATFORM_NODE_COUNT));
        Random random = new Random();
        random.setSeed((new Date()).getTime());
        int randomECINode = random.nextInt(nodeCount)+1;
        return randomECINode;
    }

    public Protocol.GateWayResponse registerWithESRSVE(final EsrsVeRegistrationModel registrationDetails,
            final DeviceDetails device) throws URISyntaxException {
        final Protocol.GateWayResponse response = new Protocol.GateWayResponse();

        //Setting connected No and will be overwritten when registration posting success.
        //This is to handle exception case scenario's
        persistToEtcd(Constants.CASPIAN_PROXY_VE_CONNECTED, NO);
        persistToEtcd(Constants.CASPIAN_PROXY_VE_REGISTERED, NO);

        String model = device.getModel();
        String serialNumber = device.getSerialNumber();
        Map<String, String> eciHostNames = device.getEciHostNames();

        if (null == serialNumber) {
            EsrsVeRegistrationModel esrsVeRegModel = buildRegModel();
            esrsVeRegModel.setMessage("This product is unlicensed. Please add a license to continue.");
            response.setStatus(Protocol.Status.PRECONDITION_FAILED);
            response.setJsonObject(esrsVeRegModel);
            // To inform UI must show status as NotConnected
            persistToEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME, NULL);
            return response;
        }

        StringBuilder authString = new StringBuilder(
                registrationDetails.getUsername()).append(Constants.COLON)
                .append(registrationDetails.getPassword());

        Map<String, Object> httpHeaders = new HashMap<String, Object>();
        httpHeaders.put(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        httpHeaders.put(HttpHeader.ACCEPT.asString(), MediaType.APPLICATION_JSON);
        httpHeaders.put(HttpHeader.AUTHORIZATION.asString(), authString.toString());

        Map<String, String> prodKeyRegDetails = new LinkedHashMap<String, String>();
        int index = 1;
        for (Map.Entry<String,String> eciHosts : eciHostNames.entrySet())
        {
            //Key is suffix of SN
            //value is node IP.
            String eciHostName = eciHosts.getValue();
            String snSuffix = eciHosts.getKey();
            StringBuilder suffixSerialNumber = new StringBuilder(serialNumber);
            suffixSerialNumber.append(Constants.HYPHEN).append(snSuffix);

            StringBuilder postRequestBody = new StringBuilder("{\"ipAddress\":\"");
            postRequestBody.append(eciHostName);
            postRequestBody.append("\"}");

            StringBuilder gatewayURI = getESRSBaseURI(registrationDetails);
            gatewayURI.append(getESRSURIEndpoint(model,
                    suffixSerialNumber.toString(), null));
            _log.debug("Executing registration request on {} for ECI Node {}", gatewayURI, eciHostName);

            try {
                HttpResponse<String> postResponse = ESRSProxyRestClient.post(gatewayURI.toString()
                        , httpHeaders, postRequestBody.toString());

                int respStatus = postResponse.getStatusCode();

                _log.debug("Response Status : {}" , respStatus);

                if(201 == respStatus || 200 == respStatus ) {
                    String productRegkeyResp = postResponse.getResponseBody();
                    JSONObject veProdRegDetails = new JSONObject(productRegkeyResp);
                    if(veProdRegDetails.has(Constants.ESRS_VE_DEVICEKEY)) {
                        _log.info("Registering ECI Node {} with ESRS VE is successfully completed", eciHostName);
                        prodKeyRegDetails.put(eciHostName, veProdRegDetails.getString(Constants.ESRS_VE_DEVICEKEY));

                        persistToEtcd(Constants.CASPIAN_HOST_NAME + Constants.HYPHEN + index, eciHostName);
                        persistToEtcd(Constants.ETCD_PRODUCTION_KEY + Constants.HYPHEN + index, veProdRegDetails.getString(Constants.ESRS_VE_DEVICEKEY));
                        persistToEtcd(CASPIAN_NODE_SUFFIXES + Constants.HYPHEN + index, snSuffix);
                        index++;
                    }
                    else {
                        _log.error("Registering ECI Node {} with ESRS VE is failed due to {}",
                                eciHostName, veProdRegDetails.getString(Constants.MESSAGE));
                        revertFailedRegistration(model, serialNumber, getESRSBaseURI(registrationDetails) , index);
                        EsrsVeRegistrationModel veRegModel = buildRegModel();
                        veRegModel.setMessage(veProdRegDetails.getString(Constants.MESSAGE));
                        response.setJsonObject(veRegModel);
                        response.setStatus(Protocol.Status.BAD_REQUEST);
                        return response;
                    }
                } else {
                    
                    // To inform UI must show status as NotConnected
                    persistToEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME, NULL);
                    _log.error("Registering ECI with ESRS VE is failed with {}" , respStatus);
                    revertFailedRegistration(model, serialNumber, getESRSBaseURI(registrationDetails) , index);
                    EsrsVeRegistrationModel veRegModel = buildRegModel();
                    if(respStatus > 0){
                        response.setStatus(respStatus);
                        StringBuilder sb = new StringBuilder();
                        sb.append(postResponse.getErrorMessage());
                        String responseBody = postResponse.getResponseBody();
                        if(null != responseBody) {
                            JSONObject jsonbody = new JSONObject(responseBody);
                            if(null != jsonbody && jsonbody.has(Constants.MESSAGE)) {
                                sb.append(" | ");
                                String s = jsonbody.getString(Constants.MESSAGE);
                                if(null != s &&
                                        s.startsWith("Error occurred while communicating to DRM service")) {
                                    sb.append("ESRS VE could not establish a connection with EMC (DRM Service).");
                                    sb.append("Please check if your ESRS VE is connected.");
                                } else {
                                    sb.append(s);
                                }
                            }
                        }
                        veRegModel.setMessage(sb.toString());
                        _log.error("Registering ECI failed due to {}" , sb.toString());
                    } else if (postResponse.getErrorMessage().contains(Constants.SOCKET_TIMEOUT_EXCEPTION)
                            || postResponse.getErrorMessage().contains(Constants.CONNECT_EXCEPTION)) {
                        
                        _log.error("Registering ECI failed due to {}" , Constants.NOT_REACHABLE_ESRS_VE);
                        response.setStatus(Protocol.Status.BAD_REQUEST);
                        veRegModel.setMessage(Constants.NOT_REACHABLE_ESRS_VE);
                    } else {
                        
                        _log.error("Registering ECI failed due to {}" , Constants.INV_GATEWAY_PORT_SSL);
                        response.setStatus(Protocol.Status.BAD_REQUEST);
                        veRegModel.setMessage(Constants.INV_GATEWAY_PORT_SSL);
                    }
                    response.setJsonObject(veRegModel);
                    return response;
                }
            } catch (URISyntaxException | JSONException e) {
                revertFailedRegistration(model, serialNumber, getESRSBaseURI(registrationDetails) , index);
                response.setStatus(Protocol.Status.BAD_REQUEST);
                EsrsVeRegistrationModel veRegModel = buildRegModel();
                veRegModel.setMessage(Constants.INV_GATEWAY_PORT_SSL);
                response.setJsonObject(veRegModel);
                _log.error("Registering ECI Node {} with ESRS VE is failed with error {}", eciHostName, e.getMessage());
                return response;
            }
        }

        if(eciHostNames.size() == prodKeyRegDetails.size()) {
            response.setStatus(Protocol.Status.OK);

            persistToEtcd(Constants.CASPIAN_MODEL, model);
            persistToEtcd(Constants.CASPIAN_SERIAL_NUMBER, device.getSerialNumber());
            persistToEtcd(Constants.CASPIAN_HOST_NAME, eciHostNames.values().toString());
            persistToEtcd(CASPIAN_NODE_SUFFIXES, eciHostNames.keySet().toString());
            persistToEtcd(Constants.ETCD_VE_IP, registrationDetails.getGateway());
            persistToEtcd(Constants.ETCD_PLATFORM_NODE_COUNT, String.valueOf(prodKeyRegDetails.size()));
            persistToEtcd(Constants.ETCD_VE_PORT, registrationDetails.getPort());
            persistToEtcd(Constants.ETCD_VE_SSL, Boolean.TRUE.toString());
            persistToEtcd(Constants.ETCD_VE_USERNAME, registrationDetails.getUsername());
            persistToEtcd(Constants.ETCD_VE_PASSWORD, registrationDetails.getPassword());
            persistToEtcd(Constants.ESRS_ENABLED, TRUE);
            persistToEtcd(Constants.CASPIAN_PROXY_VE_CONNECTED, YES);
            persistToEtcd(Constants.CASPIAN_PROXY_VE_REGISTERED, YES);
            persistToEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME, String.valueOf(System.currentTimeMillis()));
        } else {
            // To inform UI must show status as NotConnected
            persistToEtcd(Constants.ESRS_LAST_KEEP_ALIVE_TIME, NULL);
            _log.error("Registering ECI with ESRS VE is failed as all the nodes are registered with ESRS VE");
        }
        response.setJsonObject(buildRegModel());
        _log.info("Registering ECI Node with ESRS VE is successfully completed");
        return response;
    }

    private void revertFailedRegistration (String model,
            String serialNumber, StringBuilder gatewayURI, int totalRegisteredNodes)
                    throws URISyntaxException {
        for (int nIndex=1;nIndex<totalRegisteredNodes;nIndex++) {
            StringBuilder snNumber = new StringBuilder(serialNumber);
            snNumber.append(Constants.HYPHEN);
            snNumber.append(fetchFromEtcd(CASPIAN_NODE_SUFFIXES + Constants.HYPHEN + nIndex));
            deleteESRSVERegistration(model, snNumber.toString(), fetchFromEtcd(Constants.ETCD_PRODUCTION_KEY + Constants.HYPHEN + nIndex), gatewayURI);
        }
    }

    private HttpResponse<String> deleteESRSVERegistration(final String model,
            final String serialNumber, final String productRegValue,
            StringBuilder gatewayURI) throws URISyntaxException {

        StringBuilder gatewayURIEndPoint = getESRSURIEndpoint(model, serialNumber , null);
        Map<String, Object> httpHeaders = new HashMap<String, Object>();

        gatewayURI.append(gatewayURIEndPoint);

        //Use the device key and hmacdate to generate an HMAC for this ECI
        DateFormat dateFormat = new SimpleDateFormat(HMAC_DATE_FORMAT);
        Date todayDate = new Date();
        String esrsHmacDate = dateFormat.format(todayDate);

        StringBuilder authString = new StringBuilder(model);
        authString.append(Constants.COLON).append(serialNumber).append(Constants.COLON).
        append(ESRSHmacUtil.getESRSHmac(HttpDelete.METHOD_NAME, null, gatewayURIEndPoint.toString(), productRegValue, esrsHmacDate))
        .append(DOMAIN_DEVICE);

        _log.debug("Using URI : {}" , gatewayURI);
        _log.debug("Using auth: {}" , authString);

        httpHeaders.put(HTTP.DATE_HEADER, esrsHmacDate);
        httpHeaders.put(HttpHeader.AUTHORIZATION.asString(), authString.toString());

        _log.info("UnRegistering ECI Node with Serial Number "
                + "{} from the ESRS VE ", serialNumber);

        return ESRSProxyRestClient.delete(gatewayURI.toString()
                , httpHeaders);
    }
    
    @Override
    public Response propConfigScheduleInfo() {
        return PropCollectorUtil.getConfigInfo();
    }

    @Override
    public Response updatePropConfigSchedule(UriInfo uriInfo, PropertyCollectorScheduleModel propCollectSchModel) {
        String queryString= uriInfo.getRequestUri().getQuery();

        if(queryString != null && queryString.equalsIgnoreCase(Constants.DEBUG)) //debug mode is enabled
            return PropCollectorUtil.setConfigInfo(propCollectSchModel, true );

        return PropCollectorUtil.setConfigInfo(propCollectSchModel, false );
    }

    @Override
    public Response createPropConfigSchedule(PropertyCollectorScheduleModel propCollectSchModel) {
        return PropCollectorUtil.setConfigInfo(propCollectSchModel, false);
    }

    @Override
    public Response changeCallHomeConfig(UriInfo uriInfo) {
        String queryString= uriInfo.getRequestUri().getQuery();
        return CallHomeUtil.setConfigInfo(queryString);
    }

    @Override
    public Response getCallHomeConfig() {
        return CallHomeUtil.getConfigInfo();
    }

    @Override
    public Response changeCallHomeConfigPut(UriInfo uriInfo) {        
        return changeCallHomeConfig(uriInfo);
    }

    @Override
    public Response testConnection(EsrsVeRegistrationModel registrationDetails) {
        return TestESRSConnectionUtil.TestESRSConnection(registrationDetails);
    }
}