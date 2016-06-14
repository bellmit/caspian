package com.emc.caspian.ccs.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.StringTokenizer;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.omg.CosNaming.NamingContextExtPackage.InvalidAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.fabric.util.Validate;

public final class ImageStoreHelper {
    
    private static final Logger _log = LoggerFactory.getLogger(ImageStoreHelper.class);
    
    private static String requestNode ;
    private static int    requestPort ;

    private static String requestBucket; //used for containers in case of swift
    private static String requestObjKey;
    
    //Additional path parameters needed by swift
    private static String requestVersion;
    private static String requestAccount;
    
    
    
    public static String getRequestNode() {
        return requestNode;
    }

    public static void setRequestNode(String requestNode) {
        ImageStoreHelper.requestNode = requestNode;
    }

    public static int getRequestPort() {
        return requestPort;
    }

    public static void setRequestPort(int requestPort) {
        ImageStoreHelper.requestPort = requestPort;
    }

    public static String getRequestBucket() {
        return requestBucket;
    }

    public static void setRequestBucket(String requestBucket) {
        ImageStoreHelper.requestBucket = requestBucket;
    }

    public static String getRequestObjKey() {
        return requestObjKey;
    }

    public static void setRequestObjKey(String requestObjKey) {
        ImageStoreHelper.requestObjKey = requestObjKey;
    }

    public static String getRequestVersion() {
        return requestVersion;
    }

    public static void setRequestVersion(String requestVersion) {
        ImageStoreHelper.requestVersion = requestVersion;
    }

    public static String getRequestAccount() {
        return requestAccount;
    }

    public static void setRequestAccount(String requestAccount) {
        ImageStoreHelper.requestAccount = requestAccount;
    }

    public static void parseS3Url(final URL imageLocation) throws InvalidAddress {
        
        Validate.isNotNullOrEmpty(imageLocation.toString(), "imageLocation");
        _log.debug("Parsing URL: "+imageLocation.toString());
        String requestPath ;
        requestNode = imageLocation.getHost();
        requestPort = imageLocation.getPort();
        requestPath = imageLocation.getPath();
        
        StringTokenizer st = new StringTokenizer(requestPath,"/");
        if(st.countTokens() != 2) {
            _log.warn("Buckets with multiple namespaces is not supported at this time");
            
            throw new InvalidAddress();
        }

        requestBucket = st.nextToken();
        requestObjKey = st.nextToken();
    }
    
    public static void parseSwiftUrl(final URL imageLocation) throws InvalidAddress {
        
        Validate.isNotNullOrEmpty(imageLocation.toString(), "imageLocation");
        _log.debug("Parsing URL: "+imageLocation.toString());
        String requestPath ;
        requestNode = imageLocation.getHost();
        requestPort = imageLocation.getPort();
        requestPath = imageLocation.getPath();
        
        StringTokenizer st = new StringTokenizer(requestPath,"/");
        
        if(st.countTokens() < 3) {
        	
            _log.warn("The URL doesnt have enough path paramters to determine container name.");
            throw new InvalidAddress();
            
        } else if(st.countTokens() == 3) {
        	
        	requestVersion = st.nextToken();
        	requestAccount = st.nextToken();
        	requestBucket  = st.nextToken();
            requestObjKey  = null;
            
        } else if(st.countTokens() == 4) {
        	
        	requestVersion = st.nextToken();
        	requestAccount = st.nextToken();
        	requestBucket  = st.nextToken();
            requestObjKey  = st.nextToken();
            
        } else {
        	
            _log.warn("The URL has too many path paramters.");
            throw new InvalidAddress();
            
        }
    }
    
   public static ImmutablePair<Long, InputStream> getStreamSize(InputStream stream, String tempDir) throws IOException{

       //write stream to a file to get its length.

       StringBuilder tempFileName =  new StringBuilder(tempDir);
       tempFileName.append("/tmpStreamFile");

       File tempFile = new File(tempFileName.toString());
       tempFile.deleteOnExit(); //precautionary measure to delete on JVM exit. 

       final String tempFilePath = tempFile.getAbsolutePath();
       FileHelper.saveStreamToFile(tempFilePath, stream);

       ImmutablePair<Long, InputStream> pair = new ImmutablePair<Long, InputStream>((long)tempFile.length() , FileHelper.readFileAsStream(tempFilePath));
       //Now delete the temp file
       if(! tempFile.delete()){
           _log.warn("Could not delete file "+ tempFile.getName()+". Will delete on JVM exit."
                    +"You may choose to manually delete this file.");
       }
       
       return pair;
   }
    
    public static String InputStreamToText(InputStream stream) throws IOException{
    
        StringBuilder streamToText = new StringBuilder();
        _log.debug("Converting Stream to text ");
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            _log.debug("    " + line);
            streamToText.append(line);
        }
        
        return streamToText.toString();
    }
    
    public static boolean isEqual(InputStream i1, InputStream i2) throws IOException {

        try {
            // do the compare
            while (true) {
                int fr = i1.read();
                int tr = i2.read();

                if (fr != tr)
                    return false;

                if (fr == -1)
                    return true;
            }

        } finally {
            if (i1 != null)
                i1.close();
            if (i2 != null)
                i2.close();
        }
    }
}
