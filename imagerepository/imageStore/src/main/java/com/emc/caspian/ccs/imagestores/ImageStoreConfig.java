package com.emc.caspian.ccs.imagestores;

import com.emc.caspian.fabric.config.Configuration;

public class ImageStoreConfig {
    
    private static final String DEFAULT_PROTOCOL     = "http";
    public static final Configuration.Value<Boolean> unitTestingConf  = 
    Configuration.make(Boolean.class,"image.store.unitTesting", "false");

    public static class FileSystemConfig {

        public static Configuration.Value<String> STORAGE_DIR;
        public static String DEFAULT_DIR = "/data/imageStore";
        public static Configuration.Value<String> tempDir;

        private static String SECTION = "image.store.file";

        static {
            try {
                STORAGE_DIR   = Configuration.make(String.class,SECTION  + ".path", DEFAULT_DIR);
                tempDir        = Configuration.make(String.class, SECTION + ".tempdir" , DEFAULT_DIR);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static class SwiftConfig {
        
        private static final String DEFAULT_DIR = "/data/imageStore";
        private static final String SECTION = "image.store.swift";        
        private static final String DEFAULT_TENANT       = "test";
        private static final String DEFAULT_USER         = "tester";
        private static final String DEFAULT_PASSWORD     = "testing";
        private static final String DEFAULT_VERSION      = "v1.0";
        private static final String DEFAULT_CONATINER    = "standalone-cont";

    	public static final Configuration.Value<String> protocolConf  = 
    	    Configuration.make(String.class,SECTION  + ".protocol", DEFAULT_PROTOCOL);
    	public static final Configuration.Value<String>dataNodeConf   = 
    	    Configuration.make(String.class,SECTION  + ".datanode");
    	public static final Configuration.Value<Integer>dataPortConf  = 
    	    Configuration.make(Integer.class,SECTION + ".datanodeport");
    	public static final Configuration.Value<String>tenantConf     = 
    	    Configuration.make(String.class,SECTION  + ".tenant", DEFAULT_TENANT);
    	public static final Configuration.Value<String>userConf       = 
    	    Configuration.make(String.class,SECTION  + ".user", DEFAULT_USER);
    	public static final Configuration.Value<String>passwordConf   = 
            Configuration.make(String.class,SECTION  + ".password", DEFAULT_PASSWORD);
    	public static final Configuration.Value<String>versionConf    = 
    	    Configuration.make(String.class,SECTION  + ".version", DEFAULT_VERSION);
    	public static final Configuration.Value<String>containerConf  = 
    	    Configuration.make(String.class,SECTION  + ".containerName", DEFAULT_CONATINER);
    	public static final Configuration.Value<String>tempDir        = 
    	    Configuration.make(String.class, SECTION + ".tempdir" , DEFAULT_DIR);
    }
    
    public static class ObjectConfig {
        
        private static String SECTION = "image.store.object";
        private static final String DEFAULT_USER_ID  = "tester";
        private static final String DEFAULT_SEC_KEY  = "testing";
        private static final String DEFAULT_BUCKET   = "standalone-bucket";
        private static final String DEFAULT_DIR = "/data/imageStore";
        
    	public static final Configuration.Value<String> protocolConf     = 
    	    Configuration.make(String.class,SECTION  + ".protocol", DEFAULT_PROTOCOL);
    	public static final Configuration.Value<String> dataNodeConf     = 
    	    Configuration.make(String.class, SECTION + ".datanode");
    	public static final Configuration.Value<String> dataNodePortConf = 
    	    Configuration.make(String.class, SECTION + ".datanodeport");
    	public static final Configuration.Value<String> accessKeyIdConf  = 
    	    Configuration.make(String.class, SECTION + ".accesskeyid", DEFAULT_USER_ID);
    	public static final Configuration.Value<String> secretKeyConf    = 
    	    Configuration.make(String.class, SECTION + ".secretaccesskey", DEFAULT_SEC_KEY);
    	public static final Configuration.Value<String> bucketNameConf   = 
    	    Configuration.make(String.class, SECTION + ".bucketname", DEFAULT_BUCKET);
    	public static final Configuration.Value<String> tempDir          = 
    	    Configuration.make(String.class, SECTION + ".tempdir", DEFAULT_DIR);
    }
}
