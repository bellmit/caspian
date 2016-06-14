package com.emc.caspian.ccs.imagestores;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.imagestores.file.FileSystemImageStore;
import com.emc.caspian.ccs.imagestores.object.ObjectImageStore;
import com.emc.caspian.ccs.imagestores.swift.SwiftImageStore;
import com.emc.caspian.fabric.config.Configuration;


public class ImageStoreFactory
{
    private static final String SECTION = "image.store";
    private static final String imageStoreType = Configuration.make(String.class, 
	                     SECTION + ".imageStoreType", "FileSystem").value();
    private static final Logger _log = LoggerFactory.getLogger(ObjectImageStore.class);
    
    public static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static ImageStore getImageStore() {
        
        ImageStore imageStore;
        
        switch (ImageStoreType.valueOf(imageStoreType)) {
            case FileSystem:
                imageStore = FileSystemImageStore.getFileSystemStoreSingleton();
                break;
            case ObjectService:
                imageStore = ObjectImageStore.getObjectImageStore();
                break;
            case Swift:
    		imageStore = SwiftImageStore.getSwiftStoreSingleton();
                break;
            case Http:
            default:
                throw new NotImplementedException();
        }

        return imageStore;
    }
    
    public static ImageStore getImageStore(ImageStoreType imageStoreType) {
        
        ImageStore imageStore;
        
        switch (imageStoreType) {
            case FileSystem:
                imageStore = FileSystemImageStore.getFileSystemStoreSingleton();
                break;
            case ObjectService:
                imageStore = ObjectImageStore.getObjectImageStore();
                break;
            case Swift:
                imageStore = SwiftImageStore.getSwiftStoreSingleton();
                break;
            case Http:
            default:
                throw new NotImplementedException();
        }

        return imageStore;
    }
}