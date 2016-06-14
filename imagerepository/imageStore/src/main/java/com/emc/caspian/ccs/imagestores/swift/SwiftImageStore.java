package com.emc.caspian.ccs.imagestores.swift;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.common.utils.ImageStoreHelper;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreConfig;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.fabric.util.Validate;

//Please note in all references below container <=> bucket and object <=> blob
public class SwiftImageStore implements ImageStore 
{
    
    private static final Logger _log = LoggerFactory.getLogger(SwiftImageStore.class);
    private static SwiftImageStore mSwiftImageStore;
    protected static CloseableHttpAsyncClient httpClient;
    
    private static String protocol     = ImageStoreConfig.SwiftConfig.protocolConf.value(); 
    private static String dataNode     = ImageStoreConfig.SwiftConfig.dataNodeConf.value(); 
    private static int    dataNodePort = ImageStoreConfig.SwiftConfig.dataPortConf.value();
    private static String tenant       = ImageStoreConfig.SwiftConfig.tenantConf.value();
    private static String user         = ImageStoreConfig.SwiftConfig.userConf.value();
    private static String password     = ImageStoreConfig.SwiftConfig.passwordConf.value();
    private static String version      = ImageStoreConfig.SwiftConfig.versionConf.value();
    private static String bucketName   = ImageStoreConfig.SwiftConfig.containerConf.value();
    private static String authToken;
    
    private static StringBuilder baseEndPoint;
    private static String objectKey;
    
    private static final String X_AUTH_TOKEN="X-Auth-Token";
    private static final String X_STORAGE_USER="X-Storage-User";
    private static final String X_STORAGE_PASS="X-Storage-Pass";
    
    private static boolean alreadyInitialized;
    
    private SwiftImageStore() { }
    
    public static synchronized ImageStore getSwiftStoreSingleton() {
        
        if (mSwiftImageStore == null) {
            
            alreadyInitialized  = false;
            
            mSwiftImageStore = new SwiftImageStore();
            
            Validate.isNotNullOrEmpty(protocol, "protocol");
            Validate.isNotNullOrEmpty(dataNode, "dataNode");
            Validate.isGreater(dataNodePort, 0);
            Validate.isNotNullOrEmpty(tenant, "tenant");
            Validate.isNotNullOrEmpty(user, "user");
            Validate.isNotNullOrEmpty(user, "password");
            
            //Set the endpoint
            baseEndPoint = new StringBuilder(protocol);
            baseEndPoint.append("://");
            baseEndPoint.append(dataNode);
            baseEndPoint.append(":");
            baseEndPoint.append(dataNodePort);
            baseEndPoint.append("/");
            
            setHttpClient(HttpAsyncClients.createDefault());
        } 
        
        return mSwiftImageStore;
    }
    
    private static void checkAndCreateBucket() {
	
	if(alreadyInitialized) {
	    return;
	}
	
	alreadyInitialized = true;
	
	// Create and Execute request
        StringBuilder endPoint = new StringBuilder(baseEndPoint);
        endPoint.append("auth/");
        endPoint.append(version);
        
        _log.debug("Executing GET request on "+endPoint.toString());
        final HttpGet request = new HttpGet(endPoint.toString());
        request.addHeader(X_STORAGE_USER, tenant+":"+user);
        request.addHeader(X_STORAGE_PASS, password);
        
        //The HTTP client must first be started in order to start executing rest calls
        httpClient.start();
        
        Future<HttpResponse> futureResponse = httpClient.execute(request, null);
        // and wait until a response is received
        HttpResponse response = null;
        try {
            
            response = futureResponse.get();
        
        } catch (InterruptedException | ExecutionException e) {
             _log.error("Error initializing Swift image store."
           	     + "Please check your swift configuration.", e);
             throw new RuntimeException(e);
        } 
        
        _log.info("Response for first verification:"+request.getRequestLine() + "->" + response.getStatusLine() );
        Header[] responseHeaders = response.getHeaders(X_AUTH_TOKEN);
        for (int i = 0; i < responseHeaders.length; i++) { 
            if(X_AUTH_TOKEN.equals(responseHeaders[i].getName())){
                //Storing the value of AuthToken
                // TODO secure the auth token
                authToken = responseHeaders[i].getValue();
            }
        }
        
        //Check if bucket exists. If not, create it
        if(checkBucketExists()) {
            _log.warn("Will not create a new bucket.");
        } else {
            _log.info("Bucket <"+bucketName+"> not found. Will create.");
            createBucket(bucketName);
        }
    }
        
    @Override
    public Future<InputStream> getImageFile(final URL imageLocation) {
        
        return ImageStoreFactory.pool.submit(new Callable<InputStream>(){
            
            @Override
            public InputStream call() throws Exception {

                _log.info("Will attempt to get Image File from :"+ imageLocation);
                
                checkAndCreateBucket();
                
                ImageStoreHelper.parseSwiftUrl(imageLocation);
                objectKey =  ImageStoreHelper.getRequestObjKey();
                
                _log.debug("Got reqNode: " + ImageStoreHelper.getRequestNode() + " reqPort: " + ImageStoreHelper.getRequestPort() + 
                           " reqVersion: "+ ImageStoreHelper.getRequestVersion() + " reqAccount: "+ ImageStoreHelper.getRequestAccount() +
                           " reqBuck: "+ ImageStoreHelper.getRequestBucket() + " reqFile: "+ objectKey);
                
                
                if( ! ImageStoreHelper.getRequestNode().equals(dataNode) || 
                    ! (dataNodePort == ImageStoreHelper.getRequestPort()) ||
                    ! ImageStoreHelper.getRequestVersion().equals(version) || 
                    ! ImageStoreHelper.getRequestBucket().equals(bucketName) ){
                    
                    _log.warn("Location mentioned in image request does not match "+
                               "details provided in configuration. Will not retrieve.");
                    
                    if( ! ImageStoreHelper.getRequestNode().equals(dataNode)){
                        _log.debug("dataNode mismatch");
                    }
                    if( ! (dataNodePort == ImageStoreHelper.getRequestPort())){
                        _log.debug("dataNodePort mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestVersion().equals(version)){
                        _log.debug("Version mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestBucket().equals(bucketName)){
                        _log.debug("bucketName mismatch");
                    }
                    return null;
                    
                } 
                
                //no need to check if image exists. If object is missing, HTTP will return 404 Not Found error.
                return retrieveImage(imageLocation);
            }
        });
    }

    @Override
    public Future<InputStream> getECIImageFile(final URL imageLocation) {
        
        return ImageStoreFactory.pool.submit(new Callable<InputStream>(){
            
            @Override
            public InputStream call() throws Exception {
                _log.info("Will attempt to get Image File from :"+ imageLocation);
                
                checkAndCreateBucket();
                
                ImageStoreHelper.parseSwiftUrl(imageLocation);
                objectKey =  ImageStoreHelper.getRequestObjKey();
                
                _log.debug("Got reqNode: " + ImageStoreHelper.getRequestNode() + " reqPort: " + ImageStoreHelper.getRequestPort() + 
                           " reqVersion: "+ ImageStoreHelper.getRequestVersion() + " reqAccount: "+ ImageStoreHelper.getRequestAccount() +
                           " reqBuck: "+ ImageStoreHelper.getRequestBucket() + " reqFile: "+ objectKey);
                
                
                if( ! ImageStoreHelper.getRequestNode().equals(dataNode) || 
                    ! (dataNodePort == ImageStoreHelper.getRequestPort()) ||
                    ! ImageStoreHelper.getRequestVersion().equals(version) || 
                    ! ImageStoreHelper.getRequestBucket().equals(bucketName) ){
                    
                    _log.warn("Location mentioned in image request does not match "+
                               "details provided in configuration. Will not retrieve.");
                    
                    if( ! ImageStoreHelper.getRequestNode().equals(dataNode)){
                        _log.debug("dataNode mismatch");
                    }
                    if( ! (dataNodePort == ImageStoreHelper.getRequestPort())){
                        _log.debug("dataNodePort mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestVersion().equals(version)){
                        _log.debug("Version mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestBucket().equals(bucketName)){
                        _log.debug("bucketName mismatch");
                    }
                    return null;
                    
                } 
                
                //no need to check if image exists. If object is missing, HTTP will return 404 Not Found error.
                return retrieveImage(imageLocation);
            }
        });
    }

    @Override
    public Future<String> getECIImageMetadata(final URL imageMetadataLocation) {
        
        return ImageStoreFactory.pool.submit(new Callable<String>(){
            
            @Override
            public String call() throws Exception {
                _log.info("Will attempt to get Image File from :"+ imageMetadataLocation);
                
                checkAndCreateBucket();
                
                ImageStoreHelper.parseSwiftUrl(imageMetadataLocation);
                objectKey =  ImageStoreHelper.getRequestObjKey();
                
                _log.debug("Got reqNode: " + ImageStoreHelper.getRequestNode()+
                	   " reqPort: " + ImageStoreHelper.getRequestPort() + 
                           " reqVersion: "+ ImageStoreHelper.getRequestVersion() + 
                           " reqAccount: "+ ImageStoreHelper.getRequestAccount() +
                           " reqBuck: "+ ImageStoreHelper.getRequestBucket() + 
                           " reqFile: "+ objectKey);
                
                
                if( ! ImageStoreHelper.getRequestNode().equals(dataNode) || 
                    ! (dataNodePort == ImageStoreHelper.getRequestPort()) ||
                    ! ImageStoreHelper.getRequestVersion().equals(version) || 
                    ! ImageStoreHelper.getRequestBucket().equals(bucketName) ){
                    
                    _log.warn("Location mentioned in image request does not match "+
                               "details provided in configuration. Will not retrieve.");
                    
                    if( ! ImageStoreHelper.getRequestNode().equals(dataNode)){
                        _log.debug("dataNode mismatch");
                    }
                    if( ! (dataNodePort == ImageStoreHelper.getRequestPort())){
                        _log.debug("dataNodePort mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestVersion().equals(version)){
                        _log.debug("Version mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestBucket().equals(bucketName)){
                        _log.debug("bucketName mismatch");
                    }
                    return null;
                    
                } 
                
                //no need to check if image exists. If object is missing, HTTP will return 404 Not Found error.
                InputStream stream = retrieveImage(imageMetadataLocation);
                return ImageStoreHelper.InputStreamToText(stream);
            }
        });
    }
    
    /**
    * @param imageId
    * @param imageFile
    */
    @Override
    public Future<URL> saveImageFile (final String imageId, final InputStream imageFile, final long objectLength ) 
    {
        
        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call() throws Exception {
                
        	checkAndCreateBucket();
        	
                //What do in case of overwrite? : not handled now. overwrite without prompt.
                //Shall we use versioning: not needed for now.
                
                //Create request
                StringBuilder endPoint = new StringBuilder(baseEndPoint);
                endPoint.append(version);
                endPoint.append("/");
                endPoint.append("AUTH_");
                endPoint.append(tenant);
                endPoint.append("/");
                endPoint.append(bucketName);
                endPoint.append("/");
                endPoint.append(imageId);
                
                _log.debug("Executing PUT request on "+endPoint.toString());
                InputStreamEntity entity = new InputStreamEntity(imageFile,objectLength);
                final HttpPut request2 = new HttpPut(endPoint.toString());
                request2.addHeader("X-Auth-Token", authToken);
                request2.setEntity(entity);

                Future<HttpResponse> futureResponse2 = httpClient.execute(request2, null);
                // and wait until a response is received
                HttpResponse response2 = null;
                try {

                    response2 = futureResponse2.get();

                    _log.info("Response for saveImageFile:"+request2.getRequestLine() + "->" + response2.getStatusLine() );
                    Header[] responseHeaders = response2.getAllHeaders();
                    for (int i = 0; i < responseHeaders.length; i++) { 
                        _log.debug("Got response header: "+ responseHeaders[i].getName() +". Its Val: "+responseHeaders[i].getValue());
                    }
                    
                    if(response2.getStatusLine().getStatusCode() == Response.Status.CREATED.getStatusCode()) {
                        _log.info("Successfully created file at <"+endPoint.toString()+">");
                        URL url = new URL(endPoint.toString());
                        
                        return url;
                    } else {
                	_log.error("Could not create file at <"+endPoint.toString()+">");
                	 throw new RuntimeException("Could not create file");
                    }
                    
                } catch (InterruptedException | ExecutionException e) {
                    _log.error("Error saving Swift image."
                      	     + "Please check your swift configuration.", e);
                        throw new RuntimeException(e);
                }                            
            }
        });
    }
    
    @Override
    public Future<URL> saveECIImageFile(final String imageId, final InputStream imageFile, final long objectLength )
                                        throws InterruptedException, ExecutionException 
    {
        
        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call() throws Exception {
        	
        	checkAndCreateBucket();
        	//What do in case of overwrite? : not handled now. overwrite without prompt.
                //Shall we use versioning: not needed for now.
                
                //Create request
                StringBuilder endPoint = new StringBuilder(baseEndPoint);
                endPoint.append(version);
                endPoint.append("/");
                endPoint.append("AUTH_");
                endPoint.append(tenant);
                endPoint.append("/");
                endPoint.append(bucketName);
                endPoint.append("/");
                endPoint.append(imageId);
                
                _log.debug("Executing PUT request on "+endPoint.toString());
                InputStreamEntity entity = new InputStreamEntity(imageFile,objectLength);
                final HttpPut request2 = new HttpPut(endPoint.toString());
                request2.addHeader("X-Auth-Token", authToken);
                request2.setEntity(entity);

                Future<HttpResponse> futureResponse2 = httpClient.execute(request2, null);
                // and wait until a response is received
                HttpResponse response2 = null;
                try {

                    response2 = futureResponse2.get();

                    _log.info("Response for saveImageFile:"+request2.getRequestLine() + "->" + response2.getStatusLine() );
                    Header[] responseHeaders = response2.getAllHeaders();
                    for (int i = 0; i < responseHeaders.length; i++) { 
                        _log.debug("Got response header: "+ responseHeaders[i].getName() +". Its Val: "+responseHeaders[i].getValue());
                    }
                    
                    if(response2.getStatusLine().equals(Response.Status.CREATED)){
                	_log.info("Successfully created file at <"+endPoint.toString()+">");
                	URL url = new URL(endPoint.toString());

                	return url;
                    } else {
                	_log.error("Could not create file at <"+endPoint.toString()+">");
                	throw new RuntimeException("Could not create file");
                    }

                } catch (InterruptedException | ExecutionException e) {
                    _log.error("Error saving Swift image."
                	    + "Please check your swift configuration.", e);
                    throw new RuntimeException(e);
                }     
            }
        });
    }

    @Override
    public Future<URL> saveECIImageMetadataFile(final String imageId, final String eciImageMetadata)
                                                throws InterruptedException, ExecutionException 
    {
        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call() throws Exception {
                
        	checkAndCreateBucket();
        	//What do in case of overwrite? : not handled now. overwrite without prompt.
                //Shall we use versioning: not needed for now.
                
                //Create request
                StringBuilder endPoint = new StringBuilder(baseEndPoint);
                endPoint.append(version);
                endPoint.append("/");
                endPoint.append("AUTH_");
                endPoint.append(tenant);
                endPoint.append("/");
                endPoint.append(bucketName);
                endPoint.append("/");
                endPoint.append(imageId);
                
                _log.debug("Executing PUT request on "+endPoint.toString());
                StringEntity entity = new StringEntity(eciImageMetadata);
                
                final HttpPut request2 = new HttpPut(endPoint.toString());
                request2.addHeader("X-Auth-Token", authToken);
                request2.setEntity(entity);

                Future<HttpResponse> futureResponse2 = httpClient.execute(request2, null);
                // and wait until a response is received
                HttpResponse response2 = null;
                try {

                    response2 = futureResponse2.get();

                    _log.info("Response for saveImageFile:"+request2.getRequestLine() + "->" + response2.getStatusLine() );
                    Header[] responseHeaders = response2.getAllHeaders();
                    for (int i = 0; i < responseHeaders.length; i++) { 
                        _log.debug("Got response header: "+ responseHeaders[i].getName() +". Its Val: "+responseHeaders[i].getValue());
                    }
                    
                    if(response2.getStatusLine().getStatusCode() == Response.Status.CREATED.getStatusCode()){
                        _log.info("Successfully created file at <"+endPoint.toString()+">");
                        URL url = new URL(endPoint.toString());
                        
                        return url;
                    } else {
                	_log.error("Could not create file at <"+endPoint.toString()+">");
                	throw new RuntimeException("Could not create file");
                    }

                } catch (InterruptedException | ExecutionException e) {
                    _log.error("Error saving Swift image."
                	    + "Please check your swift configuration.", e);
                    throw new RuntimeException(e);
                }     
            }
        });
    }

    @Override
    public String getImageMetadataFilePath(String imageId) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private static boolean checkBucketExists(){
        
        // Create and Execute request
        StringBuilder endPoint = new StringBuilder(baseEndPoint);
        endPoint.append(version);
        endPoint.append("/");
        endPoint.append("AUTH_");
        endPoint.append(tenant);
        
        _log.debug("Executing GET request on "+endPoint.toString());
        final HttpGet request = new HttpGet(endPoint.toString());
        request.addHeader("X-Auth-Token", authToken);
        
        Future<HttpResponse> futureResponse = httpClient.execute(request, null);
        // and wait until a response is received
        HttpResponse response = null;
        try {
            
            response = futureResponse.get();
            
            _log.info("Response for bucket query:"+request.getRequestLine() + "->" + response.getStatusLine() );
            Header[] responseHeaders = response.getAllHeaders();
            for (int i = 0; i < responseHeaders.length; i++) { 
                _log.debug("Got response header: "+ responseHeaders[i].getName() +". Its Val: "+responseHeaders[i].getValue());
            }
        
            InputStream stream = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            _log.debug("Traversing list of existing cotainers.:");
            int num = 1;
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                _log.debug(num++ + ") " + line);
                
                if(line.equals(bucketName)){
                    return true;
                }
            }
            
            
        } catch (InterruptedException | ExecutionException | IOException e) {
            _log.error("Error chekcing if bucket exists."
              	     + "Please check your swift configuration.", e);
                throw new RuntimeException(e);
        } 
        
        return false;
    }
    
    private static void createBucket(String bucketName){
        
        // Create and Execute request
        StringBuilder endPoint = new StringBuilder(baseEndPoint);
        endPoint.append(version);
        endPoint.append("/");
        endPoint.append("AUTH_");
        endPoint.append(tenant);
        endPoint.append("/");
        endPoint.append(bucketName);
        
        _log.debug("Executing PUT request on "+endPoint.toString());
        final HttpPut request = new HttpPut(endPoint.toString());
        request.addHeader("X-Auth-Token", authToken);
        
        Future<HttpResponse> futureResponse = httpClient.execute(request, null);
        // and wait until a response is received
        HttpResponse response = null;
            try {
                response = futureResponse.get();
                _log.info("Response for bucket create:"+request.getRequestLine() + "->" + response.getStatusLine() );
            } catch (InterruptedException | ExecutionException  e) {
                _log.error("Error creating bucket."
                 	     + "Please check your swift configuration.", e);
                   throw new RuntimeException(e);
           } 
    }
    
    private InputStream retrieveImage(final URL imageLocation){
        
        // Create and Execute request
        _log.debug("Executing GET request on "+imageLocation.toString());
        final HttpGet request = new HttpGet(imageLocation.toString());
        request.addHeader("X-Auth-Token", authToken);
        
        Future<HttpResponse> futureResponse = httpClient.execute(request, null);
        // and wait until a response is received
        HttpResponse response = null;
        try {
            
            response = futureResponse.get();
            
            _log.info("Response for object query:"+request.getRequestLine() + "->" + response.getStatusLine() );
            Header[] responseHeaders = response.getAllHeaders();
            for (int i = 0; i < responseHeaders.length; i++) { // TODO user response.headerIterator
                _log.debug("Got response header: "+ responseHeaders[i].getName() +". Its Val: "+responseHeaders[i].getValue());
            }
            
            if (response.getStatusLine().getStatusCode() == Response.Status.NOT_FOUND.getStatusCode()) {
                _log.warn("Object <"+objectKey+"> not found in container <"+bucketName+">. Will not retrieve");

            } else if (response.getStatusLine().getStatusCode() != Response.Status.OK.getStatusCode()) {
                   _log.error("Error retreiving object at URL" + imageLocation);

            } else {
                //received proper response. Now send it out.
                InputStream stream = response.getEntity().getContent();
                return stream;
            }
            
        } catch (InterruptedException | ExecutionException | IOException e) {
            _log.error("Error retrieving image."
             	     + "Please check your swift configuration.", e);
               throw new RuntimeException(e);
        } 
        
        return null;
    }

    public static CloseableHttpAsyncClient getHttpClient() {
        return httpClient;
    }

    public static void setHttpClient(CloseableHttpAsyncClient httpClient) {
        SwiftImageStore.httpClient = httpClient;
    }
    
}
