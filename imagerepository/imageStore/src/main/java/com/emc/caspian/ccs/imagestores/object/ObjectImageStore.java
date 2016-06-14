package com.emc.caspian.ccs.imagestores.object;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.emc.caspian.ccs.common.utils.ImageStoreHelper;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreConfig;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.fabric.util.Validate;

public class ObjectImageStore implements ImageStore 
{
    
    private static final Logger _log = LoggerFactory.getLogger(ObjectImageStore.class);
     
    private static ObjectImageStore mObjectImageStore;
    private static AmazonS3 s3;
    
    private static String dataNode       = ImageStoreConfig.ObjectConfig.dataNodeConf.value(); 
    private static String dataNodePort   = ImageStoreConfig.ObjectConfig.dataNodePortConf.value();
    private static String bucketName     = ImageStoreConfig.ObjectConfig.bucketNameConf.value();
    private static String accessKeyId    = ImageStoreConfig.ObjectConfig.accessKeyIdConf.value();
    private static String secretAccessKey= ImageStoreConfig.ObjectConfig.secretKeyConf.value();
    
    private static StringBuilder EndPoint;
    private static String objectKey;
    
    private static boolean alreadyInitialized;
    
    private ObjectImageStore() { }
    
    public static synchronized ImageStore getObjectImageStore() {
        
        if (mObjectImageStore == null) {
            
            mObjectImageStore = new ObjectImageStore();
            
            alreadyInitialized = false;
        
            BasicAWSCredentials credentials = null;
            
            Validate.isNotNullOrEmpty(accessKeyId, "accessKeyId");
            Validate.isNotNullOrEmpty(secretAccessKey, "secretAccessKey");
            
            credentials = new BasicAWSCredentials(accessKeyId, secretAccessKey);
            
            EndPoint = new StringBuilder("http://");
            EndPoint.append(dataNode);
            EndPoint.append(":");
            EndPoint.append(dataNodePort);
        
            setS3(new AmazonS3Client(credentials));
        }
        
        return mObjectImageStore;
    }
    
    private static void checkAndCreateBucket() {
	
	if(alreadyInitialized) {
	    return;
	}
	    
	alreadyInitialized = true;
    	
	//If we want to support regions, that code must go here
        //Set the endpoint
        s3.setEndpoint(EndPoint.toString());
        
        //Check if bucket exists. If not, create it
        if(checkBucketExists()) {
            _log.warn("Will not create a new bucket.");
        } else {
            s3.createBucket(bucketName);
        }
    }
        
    @Override
    public Future<InputStream> getImageFile(final URL imageLocation) {
        
        return ImageStoreFactory.pool.submit(new Callable<InputStream>(){
            
            @Override
            public InputStream call() throws Exception {
                
                _log.info("Will Attempt to get Image File from :"+ imageLocation);
                
                checkAndCreateBucket();
                
                ImageStoreHelper.parseS3Url(imageLocation);
                objectKey =  ImageStoreHelper.getRequestObjKey();
                
                _log.debug("Got reqNode: " + ImageStoreHelper.getRequestNode() + " reqPort: " + ImageStoreHelper.getRequestPort() + 
                           " reqBuck: "+ ImageStoreHelper.getRequestBucket() + " reqFile: "+ objectKey);
                
                
                if( ! ImageStoreHelper.getRequestNode().equals(dataNode) || 
                    ! Integer.valueOf(dataNodePort).equals(ImageStoreHelper.getRequestPort()) ||
                    ! ImageStoreHelper.getRequestBucket().equals(bucketName) ){
                    
                    _log.warn("Location mentioned in image request does not match "+
                               "details provided in configuration. Will not retrieve.");
                    
                    if( ! ImageStoreHelper.getRequestNode().equals(dataNode)){
                        _log.debug("dataNode mismatch");
                    }
                    if( ! Integer.valueOf(dataNodePort).equals(ImageStoreHelper.getRequestPort())){
                        _log.debug("dataNodePort mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestBucket().equals(bucketName)){
                        _log.debug("bucketName mismatch");
                    }
                    return null;
                    
                } else if(checkImageExists(objectKey)) {
                    
                    _log.debug("Will try to retrieve");
                    
                    S3Object object = s3.getObject(new GetObjectRequest(bucketName, objectKey));
                    _log.debug("Fetching content of image");
                    return object.getObjectContent();
            
                } else {
                    
                    _log.warn("Object <"+objectKey+"> not found in bucket <"+bucketName+">. Will not retrieve");
                    return null;
                }
            
             }
        });
    }

    @Override
    public Future<InputStream> getECIImageFile(final URL imageLocation) {
        
        return ImageStoreFactory.pool.submit(new Callable<InputStream>(){
            
            @Override
            public InputStream call() throws Exception {
                
                _log.info("Will Attempt to get Image File from :"+ imageLocation);
                
                checkAndCreateBucket();
                
                ImageStoreHelper.parseS3Url(imageLocation);
                objectKey =  ImageStoreHelper.getRequestObjKey();
                
                _log.debug("Got reqNode: " + ImageStoreHelper.getRequestNode()+
                	   " reqPort: " + ImageStoreHelper.getRequestPort() + 
                           " reqBuck: " + ImageStoreHelper.getRequestBucket() + 
                           " reqFile: " + objectKey);
                
                
                if( ! ImageStoreHelper.getRequestNode().equals(dataNode) || 
                    ! Integer.valueOf(dataNodePort).equals(ImageStoreHelper.getRequestPort()) ||
                    ! ImageStoreHelper.getRequestBucket().equals(bucketName) ){
                
                    
                    _log.warn("Location mentioned in image request does not match "+
                               "details provided in configuration. Will not retrieve.");
                    
                    if( ! ImageStoreHelper.getRequestNode().equals(dataNode)){
                        _log.debug("dataNode mismatch");
                    }
                    if( ! Integer.valueOf(dataNodePort).equals(ImageStoreHelper.getRequestPort())){
                        _log.debug("dataNodePort mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestBucket().equals(bucketName)){
                        _log.debug("bucketName mismatch");
                    }
                    return null;
                    
                } else if(checkImageExists(objectKey)) {
                    
                    _log.debug("Will try to retrieve");
                    
                    S3Object object = s3.getObject(new GetObjectRequest(bucketName, objectKey));
                    _log.debug("Fetching content of image");
                    //displayTextInputStream(object.getObjectContent());
                    return object.getObjectContent();
            
                } else {
                    
                    _log.warn("Object <"+objectKey+"> not found in bucket <"+
                              bucketName+">. Will not retrieve");
                    return null;
                }
            
             }
        });
    }

    @Override
    public Future<String> getECIImageMetadata(final URL imageMetadataLocation) {
        
        return ImageStoreFactory.pool.submit(new Callable<String>(){
            
            @Override
            public String call() throws Exception {
                
                _log.info("Will Attempt to get Image File from :"+ imageMetadataLocation);
                
                checkAndCreateBucket();
                
                ImageStoreHelper.parseS3Url(imageMetadataLocation);
                objectKey =  ImageStoreHelper.getRequestObjKey();
                
                _log.debug("Got reqNode: " + ImageStoreHelper.getRequestNode()+ 
                	   " reqPort: " + ImageStoreHelper.getRequestPort() + 
                           " reqBuck: "+ ImageStoreHelper.getRequestBucket() + 
                           " reqFile: "+ objectKey);
                
                
                if( ! ImageStoreHelper.getRequestNode().equals(dataNode) || 
                    ! Integer.valueOf(dataNodePort).equals(ImageStoreHelper.getRequestPort()) ||
                    ! ImageStoreHelper.getRequestBucket().equals(bucketName) ){
                
                    
                    _log.warn("Location mentioned in image request does not match "+
                               "details provided in configuration. Will not retrieve.");
                    
                    if( ! ImageStoreHelper.getRequestNode().equals(dataNode)){
                        _log.debug("dataNode mismatch");
                    }
                    if( ! Integer.valueOf(dataNodePort).equals(ImageStoreHelper.getRequestPort())){
                        _log.debug("dataNodePort mismatch");
                    }
                    if(    ! ImageStoreHelper.getRequestBucket().equals(bucketName)){
                        _log.debug("bucketName mismatch");
                    }
                    return null;
                    
                } else if(checkImageExists(objectKey)) {
                    
                    _log.debug("Will try to retrieve");
                    
                    S3Object object = s3.getObject(new GetObjectRequest(bucketName, objectKey));
                    _log.debug("Fetching MetaData");
                    
                    return ImageStoreHelper.InputStreamToText(object.getObjectContent());
            
                } else {
                    
                    _log.warn("Object <"+objectKey+"> not found in bucket <"+bucketName+">. Will not retrieve");
                    return null;
                }
            
             }
        });
    }
    
    /**
    * @param imageId
    * @param imageFile
    */
    @Override
    public Future<URL> saveImageFile(final String imageId, final InputStream imageFile, final long objectLength ) {
        
        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call()  {
        	
        	checkAndCreateBucket();
        	
        	URL url;
        	
        	try {
	            url = new URL(EndPoint+"/"+bucketName+"/"+objectKey);
        	} catch (MalformedURLException e) {
        	    _log.debug("Incorrect URL: <"+EndPoint+"/"+bucketName+"/"+objectKey+">. Please check configuration.", e);
		    throw new RuntimeException();
		}    
                
                final String objectKey = imageId;
                ObjectMetadata omd = new ObjectMetadata();
                omd.setContentLength(objectLength);
                
                _log.debug("Uploading a new object to S3 from a file\n");
                
                s3.putObject(new PutObjectRequest(bucketName, objectKey, imageFile, omd));

                return url;
            }
        });
    }
    
    @Override
    public Future<URL> saveECIImageFile(final String imageId, final InputStream imageFile, final long objectLength ) {
        
        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call() throws Exception {
        	
        	checkAndCreateBucket();
                
                final String objectKey = imageId;
                ObjectMetadata omd = new ObjectMetadata();
                omd.setContentLength(objectLength);
                
                _log.debug("Uploading a new object to S3 from a file\n");
                
                s3.putObject(new PutObjectRequest(bucketName, objectKey, imageFile, omd));

                return new URL(EndPoint+"/"+bucketName+"/"+objectKey);
            }
        });
    }

    @Override
    public Future<URL> saveECIImageMetadataFile(final String imageId, final String eciImageMetadata) {
        
        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call() throws Exception {
        	
        	checkAndCreateBucket();
                
                final String objectKey = imageId;
                final InputStream imageFile = new ByteArrayInputStream(eciImageMetadata.getBytes(StandardCharsets.UTF_8));
                ObjectMetadata omd = new ObjectMetadata();
                omd.setContentLength(eciImageMetadata.length());
                
                _log.debug("Uploading a new object to S3 from a file\n");
                
                s3.putObject(new PutObjectRequest(bucketName, objectKey, imageFile, omd));

                return new URL(EndPoint+"/"+bucketName+"/"+objectKey);
            }
        });
    }

    @Override
    public String getImageMetadataFilePath(String imageId) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private static boolean checkBucketExists(){
        
        _log.debug("Listing buckets to check if it exists");
        
        for (Bucket bucket : s3.listBuckets()) {
            _log.debug(" - " + bucket.getName());
            if(bucket.getName().equals(bucketName)){
            
                _log.warn("Bucket <"+bucketName+"> exists.");
                return true;
            }
        }
        
        return false;
    }
    
    private boolean checkImageExists(String objectKey){
        
        _log.debug("Listing objects in Bucket <"+bucketName+"> to see if they exist.");
        ListObjectsRequest lor = new ListObjectsRequest().withBucketName(bucketName);
        ObjectListing objectListing = s3.listObjects(lor);
        
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            _log.debug(" - " + objectSummary.getKey() + " (size = " + objectSummary.getSize() + ")");
            
            if (objectSummary.getKey().equals(objectKey)){
            
                _log.debug("Found Object <"+objectKey+"> in bucket <"+bucketName+">");
                return(true);
            }
        }
        return false;
    }

    public static AmazonS3 getS3() {
        return s3;
    }
    
    public static void setS3(AmazonS3 s3) {
        
	ObjectImageStore.s3 = s3;
    }

    public static String getBucketName() {
        return bucketName;
    }

    public static String getObjectKey() {
        return objectKey;
    }

}
