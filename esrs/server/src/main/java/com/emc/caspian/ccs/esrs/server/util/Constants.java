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
package com.emc.caspian.ccs.esrs.server.util;

import java.util.Arrays;
import java.util.List;

/**
 * @author kuppup
 *
 */
public final class Constants {

    public static final String JETTY_CONFIG_FILE = "conf/esrsserver.xml";
    public static final String ESRS_CONFIG_FILE = "conf/esrs.conf";
    public static final String KEYSTONE = "keystone";
    public static final String CONFIG_KEYSTONE_USERNAME_DEFAULT = "csa";
    public static final String MESSAGE = "message";
    public static final String HYPHEN = "-";
    public static final String CASPIAN_MODEL = "caspian-model";
    
    //Service names
    public static final String SERVICES = "services";
    public static final String PLATFORM = "platform";
    
    //Component names
    public static final String ETCD_SERVICE = "etcd-service";
    public static final String NODE_INVENTORY = "node-inventory";
    public static final String ACCOUNT = "account";
    public static final String ESRS = "esrs";
    public static final String LICENSE = "license";
    public static final String CRS = "component-registry";
    public static final String MNR_FRONT = "mnr-frontend";
    public static final String MNR_ALL = "mnr-allinone";
    public static final String ELASTICSEARCH = "elasticsearch";
    public static final String CONTAINER_HOST_IP = System.getenv("CONTAINER_HOST_ADDRESS");

    public static final String HTTP_SECURED = "https";
    public static final String HTTP_UNSECURED = "http";
    public static final String PROP_FILENAME = "/tmp/caspianPropertyFile.txt";
    public static final String MNR_SOAP_XML_FILE = "conf/mnrReportSoap.xml";
    public static final String API_QUERY_FILE = "conf/apiQuery.json";
    public static final String NOTAPPLICABLE = "NA";

    //Component paths
    public static final String NODE_PATH = "v1/nodes";
    public static final String ACCOUNT_PATH = "v1/accounts";
    public static final String ACCOUNT_PATH_APPEND = "/domains?primary";
    public static final String CRS_PATH = "v1/services/platform/components";
    public static final String VERSION_PATH = "v1/services/platform";
    public static final String LICENSE_PATH = "v1/licenses";
    public static final String MNO_PATH = "v1/nodes/";
    public static final String ETCD_PATH = "v2/keys/caspian/ccs-esrs/caspian-proxy-esrs-connected";
    public static final String MNO_SERVICE_PORT_NUMBER = "5050";
    public static final String V1_ESRS_CALLHOME = "/v1/esrs/callhome";
    public static final String NIS_GET_PLATFORM_NODES = "/v1/nodes/allocation?service=platform";

    //Node Inv Json tags
    public static final String NODES = "nodes";
    public static final String NODE = "node";
    public static final String ID = "id";
    public static final String TOPOLOGY = "topology";
    public static final String RACK = "rack";
    public static final String BRICK = "brick";
    public static final String SERIAL_NUM = "serial_num";
    public static final String PHYSICAL_MEM_MB = "physical_mem_mb";
    public static final String CPUs = "processor_count";;
    public static final String OS_NAME = "os_name";
    public static final String OS_VERSION = "os_version";
    public static final String OS_KERNEL = "os_kernel";
    public static final String STORAGE_DEVICES = "storage_devices";
    public static final String ALLOCATION = "allocation";
    public static final String SERVICE = "service";
    public static final String MODEL = "model";
    public static final String ECI_NODE_IP = "external_ipv4";
    public static final String ECI_BASE_OS_VER = "emc_os_base";
    public static final String UNKNOWN = "Unknown";

    //Account Json tags
    public static final String ACCOUNTS = "accounts";
    public static final String DOMAINS = "domains";
    public static final String V3_ROLEASSIGNMENT_PER_PROJECT = "v3/role_assignments?scope.project.id=";
    public static final String EFFECTIVE = "&effective";
    public static final String ROLE_ASSIGNMENT = "role_assignments";
    public static final String USER = "user";

    //System Json tags
    public static final String VERSION = "version";
  
    //CRS Json tags
    public static final String COMPONENTS = "components";
    public static final String COMPONENT = "component";
    
    //License Json tags
    public static final String LICENSES = "licenses";
    public static final String NAME = "name";
    public static final String ENTITLEMENT = "ENTITLEMENT";
    public static final String PROPERTIES = "properties";
    public static final String STORAGE = "STORAGE";
    public static final String CORES = "CORES";
    public static final String LIMIT = "LIMIT";
    public static final String UNIT = "UNIT";
    public static final String MB = "MB";
    public static final String VALUE = "value";

    //MnO Json tags
        
    //Keystone Json tags
    public static final String PROJECTS = "projects";
    public static final String DOMAIN_ID = "domain_id";
    //Keystone Auth Details
    public static final String AUTH_TOKEN_KEY = "X-Auth-Token";
    public static final String DEFAULT_DOMAIN = "default";
    public static final String KEYSTONE_PROJECTS_PATH = "v3/projects";
    public static final String KEYSTONE_PATH_APPEND = "/domains?primary";
    
    //MNR Json tags
    public static final String ENVELOPE = "S:Envelope";
    public static final String BODY = "S:Body";
    public static final String GET_REP_RESP = "tns:getReportResponse";    
    public static final String COMP_ELM = "compound-element";
    public static final String TABLE_ELM = "table-element";
    
    //ETCD DETAILS
    public static final String CASPIAN_PROXY_VE_CONNECTED = "caspian-proxy-esrs-connected";
    public static final String CASPIAN_PROXY_VE_REGISTERED = "caspian-proxy-esrs-registered";
    public static final String SLASH = "/";
    public static final String YES = "Yes";
    public static final String PRODUCT_NAME = "CASPIAN";
    public static final String ETCD_PLATFORM_NODE_COUNT = "platform-nodes-count";
    public static final String VIP = "vip";
    public static final String ETCD_PRODUCTION_KEY = "product-registration-key";
    public static final String ESRS_VE_DEVICEKEY = "deviceKey";
    public static final String ESRS_ENABLED = "esrs-enabled";
    public static final String ESRS_LAST_KEEP_ALIVE_TIME = "last-keep-alive-time";
    public static final String ESRS_LAST_PROP_COLL_TIME = "last-prop-coll-time";
    public static final String PROP_COLL_SCH_INTERVAL = "prop-coll-schedule-interval";
    public static final String PROP_COLL_START_TIME = "prop-coll-start-time";
    public static final String ESRS_NODE_STATUS_LAST_CHECK_TIME = "last-node-status-check-time";
    public static final String CASPIAN_HOST_NAME = "caspian-host-name";
    public static final String CASPIAN_SERIAL_NUMBER = "caspian-serial-number";
    public static final String IS_ESRS_CALL_HOME_ENABLED = "is-esrs-callhome-enabled";

    //Need to modify this whenever more number of platform nodes supported.
    public static final int MAX_PLATFORM_NODES_SUPPORTED = 3;

    //VE Related constants
    public static final String ETCD_VE_IP = "ve-ip";
    public static final String ETCD_VE_PORT = "ve-port";
    public static final String ETCD_VE_SSL = "ve-ssl";
    public static final String ETCD_VE_USERNAME = "ve-username";
    public static final String ETCD_VE_PASSWORD = "ve-password";

    public static final String INV_PAYLOAD = new String("Invalid Payload");
    public static final String INV_GATEWAY_PORT_SSL = new String("Bad Param. Please enter valid EMC Secure Remote Service details.");
    public static final String NOT_REACHABLE_ESRS_VE = new String("Unable to connect EMC Secure Remote Service");
    public static final String CALL_HOME_NOT_ENABLED = new String("CallHome is disabled. PLease contact your Neutrino Admin to enable it");
    public static final String OS_DESCRIPTION = "os_description";
    public static final String RACK_NUM = "rack_num";
    public static final String NODE_NUM = "node_num";
    public static final String HOST_NAME = "hostname";
    public static final String BRICK_MODEL = "brick_model";
    public static final String BRICK_SN = "brick_serial_num";
    public static final String BRICK_PN = "brick_part_num";
    public static final String CONNECT_EXCEPTION = "ConnectException";
    public static final String SOCKET_TIMEOUT_EXCEPTION = "SocketTimeoutException";
    public static final String API = "API";
    public static final String PRIVATE = "private";

    public static final String DEBUG= "debug";
    public static final String TRUE= "true";
    public static final String FALSE= "false";
    public static final String ENABLE= "enable";
    public static final String DISABLE= "disable";
    public static final int MIN_PROP_COL_INT = 10080;

    public static final List<String> regMustHaveProps = Arrays.asList("Gateway", "Port", "Username", "Password");
    public static final String COLON = ":";
    public static final String ESRS_VE_V1_ENDPOINT_URI = "/esrs/v1/devices/";

    /**
     * Having private constructor to prevent accidental instantiation 
     */
    private Constants() {
    }

}
