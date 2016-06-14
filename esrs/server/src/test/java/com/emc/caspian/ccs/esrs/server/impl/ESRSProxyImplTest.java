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

import static org.junit.Assume.assumeTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.api.ResponseErrorFilter;
import com.emc.caspian.ccs.esrs.server.controller.AbstractRestAPITest;
import com.emc.caspian.ccs.esrs.server.controller.EsrsConstants;
import com.emc.caspian.ccs.esrs.server.controller.EsrsTestServer;
import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.EtcdUtils;

/**
 * @author kuppup
 *
 */
public class ESRSProxyImplTest extends AbstractRestAPITest {
    
    private static final String VE_GATEWAY = EsrsConstants.VE_GATEWAY;
    private static final String VE_PORT    = EsrsConstants.VE_PORT;
    private static final String VE_SSL     = EsrsConstants.VE_SSL;
    private static final String VE_USER    = EsrsConstants.VE_USER;
    private static final String VE_PASS    = EsrsConstants.VE_PASS;
    
    private static final String TEST_ESRS_MODEL = "TEST_ESRS_MODEL";
    private static final String TEST_ESRS_SERIAL_NUMBER = "TEST_ESRS_SERIAL_NUMBER";
    private static final String TEST_ECI_HOST = "TEST_ECI_HOST";

    String registerDetails = new String("{\"gateway\" : \""+VE_GATEWAY+"\",\"port\" : \""+VE_PORT+"\",\"ssl\" : \""+VE_SSL+"\",\"username\" : \""+VE_USER+"\",\"password\":\""+VE_PASS+"\"}");
    Entity<String> REG_JSON_DATA = Entity.entity(registerDetails, MediaType.APPLICATION_JSON);

    String regDetInv1 = new String("{\"port\" : \""+VE_PORT+"\",\"ssl\" : \""+VE_SSL+"\",\"username\" : \""+VE_USER+"\",\"password\":\""+VE_PASS+"\"}");
    Entity<String> REG_JSON_DATA_INV1 = Entity.entity(regDetInv1, MediaType.APPLICATION_JSON);

    String regDetInv2 = new String("{\"gateway\" : \""+VE_GATEWAY+"\",\"ssl\" : \""+VE_SSL+"\",\"username\" : \""+VE_USER+"\",\"password\":\""+VE_PASS+"\"}");
    Entity<String> REG_JSON_DATA_INV2 = Entity.entity(regDetInv2, MediaType.APPLICATION_JSON);

    String regDetInv3 = new String("{\"gateway\" : \""+VE_GATEWAY+"\",\"port\" : \""+VE_PORT+"\",\"username\" : \""+VE_USER+"\",\"password\":\""+VE_PASS+"\"}");
    Entity<String> REG_JSON_DATA_INV3 = Entity.entity(regDetInv3, MediaType.APPLICATION_JSON);

    String regDetInv4 = new String("{\"gateway\" : \""+VE_GATEWAY+"\",\"port\" : \""+VE_PORT+"\",\"ssl\" : \""+VE_SSL+"\",\"password\":\""+VE_PASS+"\"}");
    Entity<String> REG_JSON_DATA_INV4 = Entity.entity(regDetInv4, MediaType.APPLICATION_JSON);

    String regDetInv5 = new String("{\"gateway\" : \""+VE_GATEWAY+"\",\"port\" : \""+VE_PORT+"\",\"ssl\" : \""+VE_SSL+"\",\"username\" : \""+VE_USER+"\"}");
    Entity<String> REG_JSON_DATA_INV5 = Entity.entity(regDetInv5, MediaType.APPLICATION_JSON);

    String regDetInv6 = new String("{}");
    Entity<String> REG_JSON_DATA_INV6 = Entity.entity(regDetInv6, MediaType.APPLICATION_JSON);

    String regDetInv7 = new String("");
    Entity<String> REG_JSON_DATA_INV7 = Entity.entity(regDetInv7, MediaType.APPLICATION_JSON);

    String regDetInv8 = new String("TestRegDummy");
    Entity<String> REG_JSON_DATA_INV8 = Entity.entity(regDetInv8, MediaType.APPLICATION_JSON);

    String registerDetailsInvVEHost = new String("{\"gateway\" : \"INVALID"+VE_GATEWAY+"\",\"port\" : \""+VE_PORT+"\",\"ssl\" : \""+VE_SSL+"\",\"username\" : \""+VE_USER+"\",\"password\":\""+VE_PASS+"\"}");
    Entity<String> REG_JSON_DATA_INV_VE_HOST = Entity.entity(registerDetailsInvVEHost, MediaType.APPLICATION_JSON);

    String registerDetailsInvVECreds = new String("{\"gateway\" : \""+VE_GATEWAY+"\",\"port\" : \""+VE_PORT+"\",\"ssl\" : \""+VE_SSL+"\",\"username\" : \"INVALID"+VE_USER+"\",\"password\":\""+VE_PASS+"\"}");
    Entity<String> REG_JSON_DATA_INV_VE_CREDS = Entity.entity(registerDetailsInvVECreds, MediaType.APPLICATION_JSON);

    String healthStatus = new String("{\"componentID\" : \"ESRS Proxy\"}");
    Entity<String> REG_HEALTH_STATUS_DATA = Entity.entity(healthStatus, MediaType.APPLICATION_JSON);

    Entity<String> HEALTH_STATUS_DATA_INV1 = Entity.entity("Test", MediaType.APPLICATION_JSON);

    String invHealthStatus = new String("");
    Entity<String> REG_HEALTH_STATUS_EMPTY_DATA = Entity.entity(invHealthStatus, MediaType.APPLICATION_JSON);

    String callHomeAlert = new String("{\"eventList\": [{\"symptomCode\": \"SymptomCode-01\",\"category\": "
            + "\"Configuration\",\"severity\": \"Emergency\",\"status\": \"Unused\",\"componentID\": \"CompId-01\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-01\",\"eventDescription\": \"ED1Desc-01\"},{\"symptomCode\": \"SymptomCode-02\","
            + "\"category\": \"Topology\",\"severity\": \"Warning\",\"status\": \"Failed\",\"componentID\": \"CompId-02\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-02\",\"eventDescription\": \"ED1Desc-02\"}]}");

    Entity<String> REG_CALL_HOME_DATA = Entity.entity(callHomeAlert, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV1 = new String("{\"eventList\": [{}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV1 = Entity.entity(CALL_HOME_DATA_INV1, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV2 = new String("{\"eventList\": []}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV2 = Entity.entity(CALL_HOME_DATA_INV2, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV3 = new String("{\"eventList\"}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV3 = Entity.entity(CALL_HOME_DATA_INV3, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV4 = new String("eventList");

    Entity<String> ENTITY_CALL_HOME_DATA_INV4 = Entity.entity(CALL_HOME_DATA_INV4, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV5 = new String("{\"eventList\": [{\"category\": "
            + "\"Configuration\",\"severity\": \"Emergency\",\"status\": \"Unused\",\"componentID\": \"CompId-01\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-01\",\"eventDescription\": \"ED1Desc-01\"},{\"symptomCode\": \"SymptomCode-02\","
            + "\"category\": \"Topology\",\"severity\": \"Warning\",\"status\": \"Failed\",\"componentID\": \"CompId-02\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-02\",\"eventDescription\": \"ED1Desc-02\"}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV5 = Entity.entity(CALL_HOME_DATA_INV5, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV6 = new String("{\"eventList\": [{\"symptomCode\": \"SymptomCode-01\","
            + "\"severity\": \"Emergency\",\"status\": \"Unused\",\"componentID\": \"CompId-01\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-01\",\"eventDescription\": \"ED1Desc-01\"},{\"symptomCode\": \"SymptomCode-02\","
            + "\"category\": \"Topology\",\"severity\": \"Warning\",\"status\": \"Failed\",\"componentID\": \"CompId-02\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-02\",\"eventDescription\": \"ED1Desc-02\"}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV6 = Entity.entity(CALL_HOME_DATA_INV6, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV7 = new String("{\"eventList\": [{\"symptomCode\": \"SymptomCode-01\",\"category\": "
            + "\"Configuration\",\"status\": \"Unused\",\"componentID\": \"CompId-01\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-01\",\"eventDescription\": \"ED1Desc-01\"},{\"symptomCode\": \"SymptomCode-02\","
            + "\"category\": \"Topology\",\"severity\": \"Warning\",\"status\": \"Failed\",\"componentID\": \"CompId-02\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-02\",\"eventDescription\": \"ED1Desc-02\"}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV7 = Entity.entity(CALL_HOME_DATA_INV7, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV8 = new String("{\"eventList\": [{\"symptomCode\": \"SymptomCode-01\",\"category\": "
            + "\"Configuration\",\"severity\": \"Emergency\",\"componentID\": \"CompId-01\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-01\",\"eventDescription\": \"ED1Desc-01\"},{\"symptomCode\": \"SymptomCode-02\","
            + "\"category\": \"Topology\",\"severity\": \"Warning\",\"status\": \"Failed\",\"componentID\": \"CompId-02\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-02\",\"eventDescription\": \"ED1Desc-02\"}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV8 = Entity.entity(CALL_HOME_DATA_INV8, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV9 = new String("{\"eventList\": [{\"symptomCode\": \"SymptomCode-01\",\"category\": "
            + "\"Configuration\",\"severity\": \"Emergency\",\"status\": \"Unused\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-01\",\"eventDescription\": \"ED1Desc-01\"},{\"symptomCode\": \"SymptomCode-02\","
            + "\"category\": \"Topology\",\"severity\": \"Warning\",\"status\": \"Failed\",\"componentID\": \"CompId-02\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-02\",\"eventDescription\": \"ED1Desc-02\"}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV9 = Entity.entity(CALL_HOME_DATA_INV9, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV10 = new String("{\"eventList\": [{\"symptomCode\": \"SymptomCode-01\",\"category\": "
            + "\"Configuration\",\"severity\": \"Emergency\",\"status\": \"Unused\",\"componentID\": \"CompId-01\","
            + "\"eventData\": \"ED-01\",\"eventDescription\": \"ED1Desc-01\"},{\"symptomCode\": \"SymptomCode-02\","
            + "\"category\": \"Topology\",\"severity\": \"Warning\",\"status\": \"Failed\",\"componentID\": \"CompId-02\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-02\",\"eventDescription\": \"ED1Desc-02\"}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV10 = Entity.entity(CALL_HOME_DATA_INV10, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV11 = new String("{\"eventList\": [{\"symptomCode\": \"SymptomCode-01\",\"category\": "
            + "\"Configuration\",\"severity\": \"Emergency\",\"status\": \"Unused\",\"componentID\": \"CompId-01\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventDescription\": \"ED1Desc-01\"},{\"symptomCode\": \"SymptomCode-02\","
            + "\"category\": \"Topology\",\"severity\": \"Warning\",\"status\": \"Failed\",\"componentID\": \"CompId-02\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-02\",\"eventDescription\": \"ED1Desc-02\"}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV11 = Entity.entity(CALL_HOME_DATA_INV11, MediaType.APPLICATION_JSON);

    String CALL_HOME_DATA_INV12 = new String("{\"eventList\": [{\"symptomCode\": \"SymptomCode-01\",\"category\": "
            + "\"Configuration\",\"severity\": \"Emergency\",\"status\": \"Unused\",\"componentID\": \"CompId-01\",\"subComponentID\":\"ESRS-Proxy\","
            + "\"eventData\": \"ED-01\"}]}");

    Entity<String> ENTITY_CALL_HOME_DATA_INV12 = Entity.entity(CALL_HOME_DATA_INV12, MediaType.APPLICATION_JSON);
    private static String ETCD_INSTALL_PATH;
    private static String ETCD_FILENAME;
    private static String ETCD_FULLPATH="/tmp/etcd/etcd";
    private static String ETCD_PATH="/tmp/etcd";
    private static Process proc;

    private static final Logger _log = LoggerFactory.getLogger(ESRSProxyImplTest.class);

    private static boolean isSupportedPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        return (osName.contains("win") || osName.contains("nux"));
    }

    /*
     * NOTE: This would normally be handled in the @BeforeClass annotation to skip all tests
     *       when this assumption fails, however there is a bug in gradle that marks the 
     *       test as a failure if the assumption fails in the @BeforeClass annotation.
     */
    @Before
    public void checkSupportedPlatform() {
        assumeTrue(isSupportedPlatform());
    }

    @BeforeClass
    public static void onlyOnce() throws Exception {
        if (!isSupportedPlatform()) {
            return;
        }

	_log.info("Running UT on "+System.getProperty("os.name"));
        if(System.getProperty("os.name").toLowerCase().contains("win")) {
            ETCD_FILENAME = "etcd.exe";
            ETCD_INSTALL_PATH="/tmp/etcd.zip";
            downloadETCD("https://github.com/coreos/etcd/releases/download/v2.1.0-rc.0/etcd-v2.1.0-rc.0-windows-amd64.zip");
        } else if(System.getProperty("os.name").toLowerCase().contains("nux")) {
            ETCD_FILENAME = "etcd";
            ETCD_INSTALL_PATH="/tmp/etcd.tar.gz";
            downloadETCD("https://github.com/coreos/etcd/releases/download/v2.1.0-rc.0/etcd-v2.1.0-rc.0-linux-amd64.tar.gz");
        } else {
            throw new Exception("Unsupported Operating System");
        }
        startLocalETCD();
        
        //UT hack to set environment variable.
        Map<String, String> testModel= new HashMap<String, String>();
        testModel.put(TEST_ESRS_MODEL, "BETA2-GW");
        testModel.put(TEST_ESRS_SERIAL_NUMBER, "BETA2ENG11");
        testModel.put(TEST_ECI_HOST, "10.63.13.199");
        
        //Enable call home.
        EtcdUtils.persistToEtcd(Constants.IS_ESRS_CALL_HOME_ENABLED, Constants.TRUE);
        
        setEnv(testModel);
        //Start the Esrs Mock server
        EsrsTestServer.startEsrsMockServer();
    }

    @AfterClass
    public static void shutDown() throws Exception {
        if (!isSupportedPlatform()) {
            return;
        }
        proc.destroy();
    }

    private static void startLocalETCD() throws IOException {
        proc = Runtime.getRuntime().exec(ETCD_FULLPATH);
    }

    private static void downloadETCD(String etcdDownloadURL) throws IOException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {
        ETCD_PATH=ETCD_PATH + File.separator + ETCD_FILENAME;
        File etcdFile = new File(ETCD_PATH);
        if(!etcdFile.exists()){
            _log.info("Local ETCD doesn't exists, downloading it !!");
            File file = new File(ETCD_INSTALL_PATH);
            URL url = new URL(etcdDownloadURL);

            /*****************************************/
            //To skip the certificate check while downloading etcd
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

                    }
            };

             SSLContext sc = SSLContext.getInstance("SSL");
             sc.init(null, trustAllCerts, new java.security.SecureRandom());
             HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
             // Create all-trusting host name verifier
             HostnameVerifier allHostsValid = new HostnameVerifier() {
                 public boolean verify(String hostname, SSLSession session) {
                   return true;
                 }
             };
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            /*****************************************/

            FileUtils.copyURLToFile(url, file);

            //create output directory is not exists
            File folder = new File("/tmp/etcd/");
            if(!folder.exists()){
                folder.mkdir();
            }

            if(ETCD_INSTALL_PATH.endsWith(".zip")) {
                unZipIt(ETCD_INSTALL_PATH,"/tmp/etcd/");
            } else {
                //As UnTar thru java requires additional library, untarring it using process.
                Process untarProc = Runtime.getRuntime().exec("tar -xvizf /tmp/etcd.tar.gz -C /tmp/etcd etcd-v2.1.0-rc.0-linux-amd64/etcd --strip=1");
                untarProc.waitFor();
            }
        } else {
            _log.info("Local ETCD exists, No download required !!");
        }
    }

    private static void unZipIt(String file, String outputFolder) {
        byte[] buffer = new byte[1024];
        try{
           //get the zip file content
           ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
           //get the zipped file list entry
           ZipEntry ze = zis.getNextEntry();

           while(ze!=null){
               String fileName = ze.getName();
               _log.info("file unzip/tar : "+ fileName);
               if(!fileName.endsWith(ETCD_FILENAME)){
                   ze = zis.getNextEntry();
                   continue;
               }

              File newFile = new File(outputFolder + File.separator + ETCD_FILENAME);
              _log.info("file unzip/tar : "+ newFile.getAbsoluteFile());

               new File(newFile.getParent()).mkdirs();

               FileOutputStream fos = new FileOutputStream(newFile);

               int len;
               while ((len = zis.read(buffer)) > 0) {
                   fos.write(buffer, 0, len);
               }
               fos.close();
               ze = zis.getNextEntry();
           }

           zis.closeEntry();
           zis.close();
           _log.info(" Unzip/tar done");
       }catch(IOException ex){
          ex.printStackTrace();
       }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegister() {
	try {
	    Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA);
	    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	    String resultantJson = response.readEntity(String.class);
	    _log.info(resultantJson);

	    String registerDetails = new String("{\"connected\":\"Yes\",\"gateway\":\""+VE_GATEWAY+"\",\"port\":\""+VE_PORT+"\",\"ssl\":true,\"username\":\""+VE_USER+"\",\"password\":\"****\",\"caspianModel\":\"BETA2-GW\",\"caspianSerialNumber\":\"BETA2ENG11\",\"caspianNode\":\"[10.63.13.199]\",\"enabled\":\"true\"");
	    //Since lastKeepAliveTime can never be hard-coded and compared, as a workaround
	    //comparing entire response except lastKeepAliveTime
	    assertTrue(resultantJson.startsWith(registerDetails));
	    
	} catch (Exception e){
	    _log.error(e.getMessage());
	}
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterWOSSLFlag() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV3);
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            String resultantJson = response.readEntity(String.class);
            _log.info(resultantJson);

            String registerDetails = new String("{\"connected\":\"Yes\",\"gateway\":\""+VE_GATEWAY+"\",\"port\":\""+VE_PORT+"\",\"ssl\":true,\"username\":\""+VE_USER+"\",\"password\":\"****\",\"caspianModel\":\"BETA2-GW\",\"caspianSerialNumber\":\"BETA2ENG11\",\"caspianNode\":\"[10.63.13.199]\",\"enabled\":\"true\"");
            //Since lastKeepAliveTime can never be hard-coded and compared, as a workaround
            //comparing entire response except lastKeepAliveTime
            assertTrue(resultantJson.startsWith(registerDetails));

        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidESRSVEHost() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV_VE_HOST);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

            String notRegisteredDetails = new String(Constants.INV_GATEWAY_PORT_SSL);
            String resultantJson = response.readEntity(String.class);
            //After deletion any API shall return "Not registered" as status msg with 200 Ok code.
            assertTrue(resultantJson.contains(notRegisteredDetails));
        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidESRSVECreds() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV_VE_CREDS);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#registerationDetails()}.
     */
    @Test
    public void testRegisterationDetails() {
        Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        response = target("/v1/esrs/ve-gateway").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String registerDetails = new String("{\"connected\":\"Yes\",\"gateway\":\""+VE_GATEWAY+"\",\"port\":\""+VE_PORT+"\",\"ssl\":true,\"username\":\""+VE_USER+"\",\"password\":\"****\",\"caspianModel\":\"BETA2-GW\",\"caspianSerialNumber\":\"BETA2ENG11\",\"caspianNode\":\"[10.63.13.199]\",\"enabled\":\"true\"");
        String resultantJson = response.readEntity(String.class);
        //Since lastKeepAliveTime can never be hard-coded and compared, as a workaround
        //comparing entire response except lastKeepAliveTime field
        assertTrue(resultantJson.startsWith(registerDetails));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidData1() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV1);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

            String resultantJson = response.readEntity(String.class);
            assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidData2() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV2);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

            String resultantJson = response.readEntity(String.class);
            assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidData4() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV4);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

            String resultantJson = response.readEntity(String.class);
            assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidData5() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV5);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

            String resultantJson = response.readEntity(String.class);
            assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidData6() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV6);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

            String resultantJson = response.readEntity(String.class);
            assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidData7() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV7);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

            String resultantJson = response.readEntity(String.class);
            assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#register(com.emc.caspian.ccs.esrs.model.EsrsVeRegistrationModel)}.
     */
    @Test
    public void testRegisterInvalidData8() {
        try {
            Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA_INV8);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

            String resultantJson = response.readEntity(String.class);
            assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
        } catch (Exception e){
            _log.error(e.getMessage());
        }
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#healthStatus(com.emc.caspian.ccs.esrs.model.EsrsCaspianServiceHealthModel)}.
     */
    @Test
    public void testHealthStatus() {
        //Registering before Posting health Status
        Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        response = target("/v1/esrs/health").request().post(REG_HEALTH_STATUS_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String registeredDetails = new String("{\"connected\":\"Yes\"}");
        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.equals(registeredDetails));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#healthStatus(com.emc.caspian.ccs.esrs.model.EsrsCaspianServiceHealthModel)}.
     */
    @Test
    public void testHealthStatusWithNoJSON() {
        Response response = target("/v1/esrs/health").request().post(REG_HEALTH_STATUS_EMPTY_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#healthStatus(com.emc.caspian.ccs.esrs.model.EsrsCaspianServiceHealthModel)}.
     */
    @Test
    public void testHealthStatusWithInvalidJson() {
        Response response = target("/v1/esrs/health").request().post(HEALTH_STATUS_DATA_INV1);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHome() {
        Response response = target("/v1/esrs/callhome").request().post(REG_CALL_HOME_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testInvalidCallHomeSymtom() {

        Response response = target("/v1/esrs/callhome").request().post(REG_HEALTH_STATUS_DATA);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv1() {
        Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        String resultantJson = response.readEntity(String.class);
        _log.info(resultantJson);

        response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV1);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv2() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV2);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv3() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV3);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv4() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV4);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv5() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV5);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv6() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV6);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv7() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV7);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv8() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV8);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv9() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV9);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv10() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV10);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv11() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV11);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }

    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#callHome(com.emc.caspian.ccs.esrs.model.EsrsCallHomeProxyModel)}.
     */
    @Test
    public void testCallHomeInv12() {
        Response response = target("/v1/esrs/callhome").request().post(ENTITY_CALL_HOME_DATA_INV12);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());

        String resultantJson = response.readEntity(String.class);
        assertTrue(resultantJson.contains(Constants.INV_PAYLOAD));
    }
    /**
     * Test method for {@link com.emc.caspian.ccs.esrs.server.impl.ESRSProxyImpl#deleteRegistration()
     */
    @Test
    public void testDeleteRegistration() {
        //Before performing test, registering ECI with ESRS VE.
        Response response = target("/v1/esrs/ve-gateway").request().post(REG_JSON_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        //Deleting the registration.
        response = target("/v1/esrs/ve-gateway").request().delete();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        //Confirming deletion was success by posting HealthStatus.
        response = target("/v1/esrs/health").request().post(REG_HEALTH_STATUS_DATA);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        String notRegisteredDetails = new String("{\"connected\":\"No\"}");
        String resultantJson = response.readEntity(String.class);
        //After deletion any API shall return "Not registered" as status msg with 200 Ok code.
        assertTrue(resultantJson.equals(notRegisteredDetails));
    }

    @Override
    protected Application configure() {
        ResourceConfig cfg = ResourceConfig.forApplication(
        	                                 createApplication(
        	                                     new Class[] {
        	                                	     //EsrsVeMockImpl.class, 
        	                                	     ESRSProxyImpl.class}));

        cfg.registerClasses(ResponseErrorFilter.class);

        //final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        cfg.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(request).to(HttpServletRequest.class);
            }
        });
        return cfg;
    }
    
    protected static void setEnv(Map<String, String> newenv)
    {
	
	//this UT hack allows us to set env vars for the current jvm.
	//Note, system env is not affected
      try
        {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        }
        catch (NoSuchFieldException e)
        {
          try {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newenv);
                }
            }
          } catch (Exception e2) {
            e2.printStackTrace();
          }
        } catch (Exception e1) {
            e1.printStackTrace();
        } 
    }
}
