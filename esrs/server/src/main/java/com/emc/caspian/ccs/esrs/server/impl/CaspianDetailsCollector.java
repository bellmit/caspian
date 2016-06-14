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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.esrs.internal.model.AccountHelper;
import com.emc.caspian.ccs.esrs.internal.model.ApiDetailsHelper;
import com.emc.caspian.ccs.esrs.internal.model.CaspianDetailsModel;
import com.emc.caspian.ccs.esrs.internal.model.ComponentHelper;
import com.emc.caspian.ccs.esrs.internal.model.DomainHelper;
import com.emc.caspian.ccs.esrs.internal.model.MnrHelper;
import com.emc.caspian.ccs.esrs.internal.model.NodeHelper;
import com.emc.caspian.ccs.esrs.internal.model.ProjectsHelper;
import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.ESRSProxyRestClient;
import com.emc.caspian.ccs.esrs.server.util.ESRSUtil;
import com.emc.caspian.ccs.esrs.server.util.EsrsConfiguration;
import com.emc.caspian.ccs.esrs.server.util.EtcdUtils;
import com.emc.caspian.ccs.esrs.server.util.RestClientUtil;
import com.emc.caspian.ccs.keystone.client.KeystoneClient;
import com.emc.caspian.ccs.keystone.client.KeystoneTokenClient;
import com.emc.caspian.ccs.keystone.model.Authentication;
import com.emc.caspian.ccs.keystone.model.Authentication.Identity;
import com.emc.caspian.ccs.keystone.model.Authentication.Scope;
import com.emc.caspian.ccs.keystone.model.Token;

public class CaspianDetailsCollector implements Runnable{

    private static final Logger _log = LoggerFactory.getLogger(CaspianDetailsCollector.class);
    private static final int CONFIGURED_PROP_INTERVAL = EsrsConfiguration.getPropColInterval(); //minutes

    private CaspianDetailsModel caspianData;
    private Map<String, List<String>> domainProjectMap = null;

    public CaspianDetailsCollector() {
	//do nothing.
    }

    public static boolean checkLastPropCollLessThanInterval() {

	String lastPropCollTime = EtcdUtils.fetchFromEtcd(Constants.ESRS_LAST_PROP_COLL_TIME);
	_log.debug("Last kept alive from etcd: {}", lastPropCollTime);
	_log.debug("Current time: {}", String.valueOf(System.currentTimeMillis()));

	//If lastPropCollTime is null then there is no previous property collection happened.
	if (null == lastPropCollTime) {
	    return false;
	}
	Long lastCollect = Long.parseUnsignedLong(lastPropCollTime);
	Long currentTime = System.currentTimeMillis() ;
	Long timeDiff = currentTime - lastCollect;
	Long configuredIntervalMiliSecs = (long)CONFIGURED_PROP_INTERVAL * 60 * 1000; //miliseconds

	if (timeDiff < configuredIntervalMiliSecs ) {
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public void run() {

	_log.info("ESRS Proxy will check for properties' collection");

	caspianData = new CaspianDetailsModel();
	domainProjectMap = new HashMap<String, List<String>>();

	if(EtcdUtils.esrsIsRegistered() && EtcdUtils.esrsIsConnected()) {
	    _log.debug("ESRS is registered and connected.  Property collection started");
	} else {
	    _log.warn("ESRS is not Registered/Connected. Property collection skipped.");
	    return;
	}
	
	if(checkLastPropCollLessThanInterval()) {

	    _log.info("Last Property collection less than {}  minutes earlier. Will not collect."
		    , CONFIGURED_PROP_INTERVAL);

	    return;
	}
	
	boolean mnrResult = this.collectMnrReport();
	this.populateNodeInventoryDetails();
	this.getProjectDetails();
	this.populateAccountDetails();
	this.populateComponentDetails();
	this.populateVersionDetails();
	this.populateApiDetails();
	caspianData.populateDetailsNumbers();
	boolean writeResult = this.writeToFile();

	_log.debug("Shall I call callHome now?");
	if(mnrResult || writeResult) {
	    _log.debug("I will call callHome");
	    
	    boolean calledHome = false;
	    calledHome = this.callCallHome();
	    
	    if(calledHome) {
		_log.debug("persist {} in ETCD", Constants.ESRS_LAST_PROP_COLL_TIME);
		//once call is complete, persist time to ETCD.
		EtcdUtils.persistToEtcd(Constants.ESRS_LAST_PROP_COLL_TIME, String.valueOf(System.currentTimeMillis()));
		
		_log.info("Property collection and CallHome successfull");
	    }else {
		_log.error("Call Home failed for some reason. Will not update ETCD");
	    }
	}else {
	    _log.error("Property file collection failed for some reason will not call home.");
	}
	
    }

    private void populateNodeInventoryDetails(){

	//first find Node Inventory from CRS
	StringBuilder nodeUri = getComponentBaseUrl(Constants.NODE_INVENTORY);
	nodeUri.append(Constants.NODE_PATH);

	//Get the json data from NIS
	JSONObject nodeInventoryJsonData = fetchDetailsRestCall(nodeUri, false);
	if(nodeInventoryJsonData == null) {
	    _log.error("Could not collect data for Node Inventory Service");
	    _log.error("Caspian properties to be collected from NIS will be ignored");
	    return;
	}
	_log.debug("Received data from Node Inventory service: {}", nodeInventoryJsonData.toString());

	//Parse the data to collect required data
	try {
	    JSONArray listOfNodes = nodeInventoryJsonData.getJSONArray(Constants.NODES);
	    _log.debug("Found {} nodes from NIS", listOfNodes.length());
	    caspianData.setNumberOfNodes(listOfNodes.length());

	    for (int i=0; i<listOfNodes.length(); i++ ) {

                JSONObject node = listOfNodes.getJSONObject(i);
                NodeHelper nodeDetails = new NodeHelper();

                nodeDetails.setNodeId(node.getString(Constants.ID));

                JSONObject topology = node.getJSONObject(Constants.TOPOLOGY);
                nodeDetails.setRack(topology.getString(Constants.RACK));
                nodeDetails.setos_version(topology.getString(Constants.OS_VERSION));
                nodeDetails.setos_kernel(topology.getString(Constants.OS_KERNEL));
                nodeDetails.setos_name(topology.getString(Constants.OS_NAME));
                nodeDetails.setos_description(topology.getString(Constants.OS_DESCRIPTION));
                nodeDetails.setBrick(topology.getString(Constants.BRICK));

                nodeDetails.setserial_num(topology.getString(Constants.SERIAL_NUM));
                nodeDetails.setprocessor_count(topology.getInt(Constants.CPUs));
                nodeDetails.setrack_num(topology.getInt(Constants.RACK_NUM));
                nodeDetails.setnode_num(topology.getInt(Constants.NODE_NUM));
                nodeDetails.sethostname(topology.getString(Constants.HOST_NAME));
                nodeDetails.setemc_os_base(topology.getString(Constants.ECI_BASE_OS_VER));

                nodeDetails.setbrick_model(topology.getString(Constants.BRICK_MODEL));
                nodeDetails.setbrick_serial_num(topology.getString(Constants.BRICK_SN));
                nodeDetails.setbrick_part_num(topology.getString(Constants.BRICK_PN));

		StringBuilder memory = new StringBuilder();
		try {
		    memory.append(String.valueOf(topology.getInt(Constants.PHYSICAL_MEM_MB)));
		    memory.append(StringUtils.leftPad(Constants.MB, 3));
		    nodeDetails.setModel(topology.getString(Constants.MODEL));
		}catch(JSONException e1) {
		    
		    StringBuilder sb = new StringBuilder();
		    sb.append("Could not collect JSON prop ");
		    sb.append("[");
		    sb.append(Constants.PHYSICAL_MEM_MB);
		    sb.append(", ");
		    sb.append(Constants.MODEL);
		    sb.append("]. Since this ");;
		    sb.append("property is sometimes missing in NIS, we will ignore this ");
			    sb.append("proeprty and continue with NIS collection. Error: {}");
		    _log.warn( sb.toString(), e1 );
		}
		nodeDetails.setphysical_mem_mb(memory.toString());
		JSONArray storages = node.getJSONArray(Constants.STORAGE_DEVICES);
		nodeDetails.setNumOfStorageDevices(storages.length());

		JSONObject allocation = node.getJSONObject(Constants.ALLOCATION);
		nodeDetails.setAllocatedService(allocation.getString(Constants.SERVICE));

		caspianData.addNode(nodeDetails);

		_log.debug("Added node[{}] to caspianData", i);
	    }

	} catch (JSONException e) {
	    _log.error("Could not parse the nodes out of NIS data", e);
	    _log.error("Caspian properties to be collected from NIS will be ignored");
	    return;
	}
    }

    private void getProjectDetails() {
	//first find Account service from CRS
	StringBuilder keystoneBaseUri = getComponentBaseUrl(Constants.KEYSTONE);
	keystoneBaseUri.append(Constants.KEYSTONE_PROJECTS_PATH);

	//Get the json data from Account
	JSONObject projectsJsonData = fetchDetailsRestCall(keystoneBaseUri, true);
	if(projectsJsonData == null) {
	    _log.error("Could not collect project data for Keystone Service");
	    _log.error("Caspian properties to be collected for projects will be ignored");
	    domainProjectMap = null;
	    return;
	}
	_log.debug("Received project data from Keystone service: {}", projectsJsonData.toString());

	//Parse the data to collect required data
	try {
	    JSONArray listOfProjects = projectsJsonData.getJSONArray(Constants.PROJECTS);
	    _log.debug("Found {} projects from Keystone Service", listOfProjects.length());

	    for (int i=0; i<listOfProjects.length(); i++ ) {

		JSONObject project = listOfProjects.getJSONObject(i);
		String domainId = project.getString(Constants.DOMAIN_ID);
		String projectId = project.getString(Constants.ID);

		if(!domainProjectMap.containsKey(domainId)) {
		    domainProjectMap.putIfAbsent(domainId, new ArrayList<String>());
		}
		domainProjectMap.get(domainId).add(projectId);
		_log.debug("Added project[{}] to hashMap", i);
	    }

	} catch (JSONException e) {
	    _log.error("Could not parse the data out of response", e);
	    _log.error("Caspian project properties will be ignored");
	    return;
	}

	_log.debug("Finished collecting Projects data.");
    }

    private void populateAccountDetails() {

	//first find Account service from CRS
	StringBuilder accountBaseUri = getComponentBaseUrl(Constants.ACCOUNT);
	accountBaseUri.append(Constants.ACCOUNT_PATH);

	//Get the json data from Account
	JSONObject accountsJsonData = fetchDetailsRestCall(accountBaseUri, true);
	if(accountsJsonData == null) {
	    _log.error("Could not collect data for Account Service");
	    _log.error("Caspian properties to be collected from Account will be ignored");
	    return;
	}
	_log.debug("Received data from Account service: {}", accountsJsonData.toString());

	//Parse the data to collect required data
	try {
	    JSONArray listOfAccounts = accountsJsonData.getJSONArray(Constants.ACCOUNTS);
	    _log.debug("Found {} accounts from Account Service", listOfAccounts.length());
	    caspianData.setNumberOfAccounts(listOfAccounts.length());

	    for (int i=0; i<listOfAccounts.length(); i++ ) {

		JSONObject account = listOfAccounts.getJSONObject(i);
		AccountHelper accountDetails = new AccountHelper();

		String accountId = account.getString(Constants.ID);
		accountDetails.setAccountName(accountId);

		StringBuilder accountUrl = new StringBuilder(accountBaseUri); 
		accountUrl.append(Constants.SLASH);
		accountUrl.append(accountId);
		accountUrl.append(Constants.ACCOUNT_PATH_APPEND);

		JSONObject accountSpecificData = fetchDetailsRestCall(accountUrl, true);
		JSONArray listOfDomains = accountSpecificData.getJSONArray(Constants.DOMAINS);
		accountDetails.setNumOfDomains(listOfDomains.length());

		for(int j=0;j<listOfDomains.length();j++) {

		    JSONObject domain = listOfDomains.getJSONObject(j);
		    DomainHelper domainDetails = new DomainHelper();
		    String domainId = domain.getString(Constants.ID);
		    domainDetails.setDomainName(domainId);
		    if(domainProjectMap != null && domainProjectMap.containsKey(domainId)) {
			domainDetails.setNumberOfProjects(domainProjectMap.get(domainId).size());
			for (String projId : domainProjectMap.get(domainId)) {
			    ProjectsHelper projects = new ProjectsHelper(projId);
			    domainDetails.addProjects(projects);
			    Set<String> userSet = new HashSet<String>();

			    StringBuilder roleURL = getComponentBaseUrl(Constants.KEYSTONE);
			    roleURL.append(Constants.V3_ROLEASSIGNMENT_PER_PROJECT);
			    roleURL.append(projId);
			    roleURL.append(Constants.EFFECTIVE);

			    JSONObject projectRoleAssignment = fetchDetailsRestCall(roleURL, true);
			    if (projectRoleAssignment.has(Constants.ROLE_ASSIGNMENT)) {
				JSONArray listOfRoleAssignments = projectRoleAssignment.getJSONArray(Constants.ROLE_ASSIGNMENT);
				for (int index = 0; index < listOfRoleAssignments.length(); index++) {
				    JSONObject roles = listOfRoleAssignments.getJSONObject(index);
				    if (roles.has(Constants.USER)) {
					JSONObject user = roles.getJSONObject(Constants.USER);
					userSet.add(user.getString(Constants.ID));
					_log.debug("Account {}\tDomain {}\tProject {}\t User id {}",
						new Object[] {accountId,domainId,projId,user.getString(Constants.ID) });
				    }
				}
			    }
			    projects.setNumberOfUsers(userSet.size());
			    _log.debug("Account {}\tDomain {}\tProject {}\t User id {}",
				    new Object[] { accountId, domainId, projId, userSet.size() });
			}
		    }
		    accountDetails.addDomain(domainDetails);
		    _log.debug("Added domain[{}] to this account", j);
		}
		caspianData.addAccount(accountDetails);
		_log.debug("Added account[{}] to caspianData", i);
	    }
	    _log.debug("Finished collecting Account data.");
	} catch (JSONException e) {
	    _log.error("Could not parse the data out of response", e);
	    _log.error("Caspian properties to be collected from Account will be ignored");
	}
    }

    private void populateComponentDetails() {

	//first find CRS from CRS.. Funny.. done to get the URL actually registered
	StringBuilder crsUri = getComponentBaseUrl(Constants.CRS);
	crsUri.append(Constants.CRS_PATH);

	//Get the json data from CRS
	JSONObject crsJsonData = fetchDetailsRestCall(crsUri, false);
	if(crsJsonData == null) {
	    _log.error("Could not collect data for CRS");
	    _log.error("Caspian properties to be collected from CRS will be ignored");
	    return;
	}
	_log.debug("Received data from CRS:{} ", crsJsonData.toString());

	//Parse the data to collect required data
	try {
	    JSONArray listOfComponents = crsJsonData.getJSONArray(Constants.COMPONENTS);
	    _log.debug("Found {} comps from CRS", listOfComponents.length());
	    caspianData.setNumberOfComponents(listOfComponents.length());

	    for (int i=0; i<listOfComponents.length(); i++ ) {

		JSONObject component = listOfComponents.getJSONObject(i);
		ComponentHelper componentDetails = new ComponentHelper();

		componentDetails.setComponentName(component.getString(Constants.COMPONENT));

		caspianData.addComponent(componentDetails);

		_log.debug("Added component[{}] to caspianData", i);
	    }

	} catch (JSONException e) {
	    _log.error("Could not parse the nodes out of NIS data", e);
	    _log.error("Caspian properties to be collected from NIS will be ignored");
	    return;
	}
    }

    private void populateVersionDetails() {

    	//first find CRS from CRS.. Funny.. done to get the URL actually registered
    	StringBuilder crsUri = getComponentBaseUrl(Constants.CRS);
    	crsUri.append(Constants.VERSION_PATH);

    	//Get the json data from CRS
    	JSONObject crsJsonData = fetchDetailsRestCall(crsUri, false);
    	if(crsJsonData == null) {
    	    _log.error("Could not collect data for services/platform");
    	    _log.error("Caspian property - version will be ignored");
    	    return;
    	}
    	_log.debug("Received data from platform:{} ", crsJsonData.toString());

    	//Parse the data to collect required data
    	try {
    	    String version = crsJsonData.getString(Constants.VERSION);
    	    caspianData.setCaspianVersion(version);

    	} catch (JSONException e) {
    	    _log.error("Could not parse the version out of platform data", e);
    	    _log.error("Caspian version will be ignored");
    	    return;
    	}
	    

	_log.debug("Finished collecting version data.");

    }

    private void populateApiDetails() {

	//first find ElasticSearch service from CRS
	StringBuilder elasticsearchBaseUri = getComponentBaseUrl(Constants.ELASTICSEARCH);
	elasticsearchBaseUri.append("_search");
	String queryFilename = Constants.API_QUERY_FILE;
	File queryFile = new File(queryFilename);
	String query;
	try {
	    query = FileUtils.readFileToString(queryFile);
	} catch (IOException e2) {
	    _log.error("Could not read the API query json. Will ignore API Details", e2);
	    return;
	}

	com.emc.caspian.ccs.client.response.HttpResponse<String> getResponse =null;
	//Get the json data from ELK
	try {
	    getResponse = ESRSProxyRestClient.post(elasticsearchBaseUri.toString(),null, query);
	} catch (URISyntaxException e1) {
	    _log.error("Could not get end point for ELK. Ignoring ELK Details.");
	    return;
	}
	if(getResponse.getStatusCode() != 200) {
	    _log.error("Could not collect number of API by type");
	    _log.error("Caspian properties to be collected for ELK will be ignored");
	    domainProjectMap = null;
	    return;
	}
	_log.debug("Received  number of API by type from elasticsearch service: {}", getResponse.getResponseBody());
	//Parse the data to collect required data

	ObjectMapper objectMapper = new ObjectMapper();
	try {
	    JsonNode rootNode = objectMapper.readTree(getResponse.getResponseBody());
	    JsonNode aggregations = rootNode.path("aggregations");
	    JsonNode aggregationDetails = aggregations.path("2");
	    JsonNode buckets = aggregationDetails.get("buckets");
	    Iterator<JsonNode> elements = buckets.getElements();

	    JsonNode nodeDetails =null;
	    while(elements.hasNext()){

		ApiDetailsHelper apiDetails = new ApiDetailsHelper();
		nodeDetails = elements.next();
		apiDetails.setType(nodeDetails.get("key").getTextValue());
		apiDetails.setCount(nodeDetails.get("doc_count").asText());
		caspianData.addApi(apiDetails);
	    }

	} catch (JSONException e ) {
	    _log.error("Could not parse data out of json. Will ignore API Details",e);
	    return;
	} catch (IOException e) {
	    _log.error("Could not parse data out of json. Will ignore API Details",e);
	    return;
	}

	_log.debug("Finished collecting API data.");
    }

    private JSONObject fetchDetailsRestCall(StringBuilder uri, boolean authNeeded) {

	Future<HttpResponse> httpRegisterResponse;
	JSONObject jsonResponse = null;
	String xAuthToken = null;
	Map<String, String> headers = null;

	if(authNeeded) {
	    xAuthToken = getXAuthToken();

	    if(null == xAuthToken) {
		_log.error("Unable to fetch auth token from keystone");
		return null;
	    }
	    headers = new HashMap<String, String>();
	    headers.put(Constants.AUTH_TOKEN_KEY, xAuthToken);
	}

	try {
	    httpRegisterResponse = RestClientUtil.httpGETRequest(uri.toString(), headers);
	    HttpResponse responseFromCrs = httpRegisterResponse.get();

	    if(responseFromCrs.getStatusLine().getStatusCode() == 200) {
		String stringResponse = EntityUtils.toString(responseFromCrs.getEntity());
		jsonResponse = new JSONObject(stringResponse);

	    } else {
		_log.error("Response code {}", responseFromCrs.getStatusLine()
		        .getStatusCode());
		return null;
	    }

	} catch (UnsupportedEncodingException e) {
	    _log.error("Incorrect GET request.", e);
	} catch (InterruptedException |ExecutionException e) {
	    _log.error("Future task encountered Execution execption. ", e);
	} catch (ParseException | IOException | JSONException e) {
	    _log.error("Response received could not be parsed to as expected. Invalid response");
	} 

	return jsonResponse;
    }

    private StringBuilder getComponentBaseUrl(final String componentName) {

	StringBuilder compBaseUrl = new StringBuilder(ESRSUtil.getEndPointFromCRS(Constants.PLATFORM, componentName));
	return compBaseUrl.append(Constants.SLASH);
    }

    protected String getXAuthToken() {

	String keyStoneUri = getComponentBaseUrl(Constants.KEYSTONE).toString();
	URL url;
	String authToken = null;

	try {
	    url = new URL(keyStoneUri);
	} catch (MalformedURLException e) {
	    _log.error("Could notparse keystone URL. Will not fetch X Auth Token", e);
	    return null;
	}

	char[] keystonePassword = null;
	try {
	    keystonePassword = ESRSUtil.getKeystonePassword();
	} catch (Exception e) {
	    _log.error("Exception encountered while trying to fetch Keystone password",e);
	    throw new RuntimeException(e);
	}

	KeystoneClient ksClient = new KeystoneClient(url.getProtocol(), 
		url.getHost(), url.getPort(), true);
	KeystoneTokenClient ksTokenCli = ksClient.getKeystoneTokenClient();

	Authentication authenticate = new Authentication();
	// set identity
	Authentication.Identity identity = new Identity();
	identity = Identity.password(Constants.DEFAULT_DOMAIN, 
		Constants.CONFIG_KEYSTONE_USERNAME_DEFAULT, String.valueOf(keystonePassword));
	authenticate.setIdentity(identity);

	// set scope as default
	Authentication.Scope scope = new Scope();
	scope = Scope.domain(Constants.DEFAULT_DOMAIN);
	authenticate.setScope(scope);

	Token token =  ksTokenCli.getToken(authenticate, true).getHttpResponse().getResponseBody();
	authToken = token.getTokenString();

	return authToken;
    }

    private boolean writeToFile() {

        String json = JsonHelper.serializeToJson(caspianData);
        File file = new File(Constants.PROP_FILENAME);
        FileOutputStream fop = null;

        try {
            fop = new FileOutputStream(file, false);
        } catch (FileNotFoundException e) {
            try {
                file.createNewFile();
            } catch (IOException r) {
                _log.error("Unable to create file", e);
                return false;
            }
        }

        byte[] contentInBytes = json.toString().getBytes();
        try {
            fop.write(contentInBytes);
            fop.flush();
            _log.info("Successfully written properties' file to {}",
                    Constants.PROP_FILENAME);
        } catch (IOException e) {
            _log.error("Writing to file encoutered error", e);
            return false;
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException ioe) {
                _log.error("Could not close file stream", ioe);
            }
        }

        return true;
    }

    private boolean collectMnrReport() {

	JSONObject mnrJsonResult = null;
	CaspianMnrCollector collector = new CaspianMnrCollector();
	try {
	    _log.debug("Will attempt to collect properties now.");
	    mnrJsonResult = collector.completeSoapRequest(getXAuthToken());
	    _log.debug("MNR Properties collection complete.");

	    MnrHelper mnrDetail = new MnrHelper();
	    mnrDetail.setReportName(mnrJsonResult.getString(Constants.NAME));
	    mnrDetail.setReportString(net.sf.json.JSONObject.fromObject(mnrJsonResult.toString()));
	    caspianData.setMnrDetail(mnrDetail);

	} catch (Exception e) {	
	    _log.error("Could not collect MNR props:", e);
	    return false;
	}

	return true;
    }
    
    private boolean callCallHome() {
	//Send the files
	//      1. Constants.PROP_FILENAME
	//      2. COnstants.MNR_SCRIPT_OUTPUT

	StringBuilder sb = new StringBuilder();
	sb.append("{\"eventList\":[{\"symptomCode\":\"SFT-088000\",");
	sb.append("\"category\":\"Configuration\",\"severity\":\"Unknown\",\"status\":\"OK\",");
	sb.append("\"componentID\":\"CASPIAN\",\"subComponentID\":\"ESRS-Proxy\",");
	sb.append("\"eventData\":\"Caspian System Configuration\",");
	sb.append("\"eventDescription\":\"Details about present ECI system and configuration\"}]}");

	String strSymptom = sb.toString();
	_log.debug("Call Home Sympom Str: {}", strSymptom);
	
	MultipartEntityBuilder reqEntity = MultipartEntityBuilder.create();
	reqEntity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

	File caspPropFile = new File(Constants.PROP_FILENAME);
	if (caspPropFile.exists()) {
	    _log.info("Will Read file {}",Constants.PROP_FILENAME);
	    FileBody bin1 = new FileBody(caspPropFile);
	    reqEntity.addPart("File1", bin1);
	}else {
	    _log.error("Could not find the file: {}", Constants.PROP_FILENAME);
	    return false;
	}
	
	reqEntity.addTextBody("Symptom", strSymptom);
	StringBuilder callHomeURI = new StringBuilder(ESRSUtil.getEndPointFromCRS(Constants.PLATFORM,
	        Constants.ESRS));
	callHomeURI.append(Constants.V1_ESRS_CALLHOME);

	_log.info("sending to Callhome URI {}", callHomeURI);
	Map<String,String> httpHeaders = new HashMap<String, String>();
	httpHeaders.put(Constants.AUTH_TOKEN_KEY, getXAuthToken());
	
	HttpResponse httpResponse;
        try {
            httpResponse = RestClientUtil.httpMultiPartPostRequest(
                    callHomeURI.toString(), httpHeaders, reqEntity.build());

            _log.debug("Config file upload status {} ", httpResponse.toString());

            return true;

        } catch (IOException e) {
	    _log.error("Config file upload failed due to {} ", e);
	} finally {
	    
	    _log.debug("We will now delete the property file.");
	    //Removing the Caspian Prop file and Mnr Report irrespective of the upload status
	    if (caspPropFile.isFile() && caspPropFile.exists()) {
		caspPropFile.delete();
	    }
	    	    
	    _log.debug("CallHome: Deleted property file without errors");
	}
	
	//code will only reach here if it does not complete the try block.
	return false;
	
    }
    
}
