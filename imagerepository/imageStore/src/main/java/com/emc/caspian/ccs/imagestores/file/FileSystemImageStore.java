package com.emc.caspian.ccs.imagestores.file;

import com.emc.caspian.fabric.config.Configuration;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.ccs.common.utils.FileHelper;
import com.emc.caspian.ccs.imagestores.ImageStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class FileSystemImageStore implements ImageStore
{
    public static final String SECTION = "image.store.file";

    private static final String DEFAULT_DIR = "/data/imageStore";
//    private static final Configuration.Value<String> STORAGE_DIR = Configuration.make(String.class,
//                                                                                      SECTION + ".path", DEFAULT_DIR
//    );

    private static final String mStorageDirectory = ImageStoreConfig.FileSystemConfig.STORAGE_DIR.value();
    private static final String BINARY_FILE = "file";
    private static final String METADATA_FILE = "json";

    private static FileSystemImageStore mFileSystemImageStore;

    private static final Logger _log = LoggerFactory.getLogger(FileSystemImageStore.class);

    private FileSystemImageStore() { }

    public static synchronized ImageStore getFileSystemStoreSingleton() {
        if (mFileSystemImageStore == null) {
            mFileSystemImageStore = new FileSystemImageStore();
        }
        
        _log.debug("Created fileImageStore object.");
        return mFileSystemImageStore;
    }

    /**
     *
     * @param imageId
     * @return
     */
    @Override
    public Future<InputStream> getImageFile(final URL imageId) {
        return ImageStoreFactory.pool.submit(new Callable<InputStream>()
          {
              @Override
              public InputStream call() throws Exception {
                  
                  final String imageFilePath = imageId.getPath();

                  if(FileHelper.checkFileExists(imageFilePath))
                      return FileHelper.readFileAsStream(imageFilePath);
                  
                  else {
                      // TODO: should we throw exception for file not found or return null?
                      return null;
                  }
              }
          });
    }

    /**
     *
     * @param imageId
     * @return
     */
    @Override
    public Future<InputStream> getECIImageFile(final URL imageId) {
        return ImageStoreFactory.pool.submit(new Callable<InputStream>()
        {
            @Override
            public InputStream call() throws Exception {
                final String imageFilePath = imageId.getPath();
               
                if(FileHelper.checkFileExists(imageFilePath))
                    return FileHelper.readFileAsStream(imageFilePath);
                
                else {
                    return null;
                }
            }
        });
    }

    /**
     *
     * @param imageMetadataLocation
     * @return
     */
    @Override
    public Future<String> getECIImageMetadata(final URL imageMetadataLocation) {
        return ImageStoreFactory.pool.submit(new Callable<String>()
         {
             @Override
             public String call() throws Exception {
                 final String imageFilePath = imageMetadataLocation.getPath();

                 if(FileHelper.checkFileExists(imageFilePath))
                     return FileHelper.readFileAsString(imageFilePath);

                 return null;
             }
         });
    }
   
    /**
     *
     * @param imageId
     * @param imageFile
     */
    @Override
    public Future<URL> saveImageFile(final String imageId, final InputStream imageFile, long size) {
        
        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call() throws Exception {
                final String imageLayerFilePath = getImageFilePath(imageId);
                FileHelper.saveStreamToFile(imageLayerFilePath, imageFile);
                return new URL("file://" + imageLayerFilePath);
            }
        });
    }

    /**
     *
     * @param imageId
     * @param imageFile
     */
    @Override
    public Future<URL> saveECIImageFile(final String imageId, final InputStream imageFile, final long objectLength) {

        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call() throws Exception {
                final String imageLayerFilePath = getImageFilePath(imageId);
                FileHelper.saveStreamToFile(imageLayerFilePath, imageFile);
                return new URL("file://" + imageLayerFilePath);
            }
        });
    }

    /**
     *
     * @param imageId
     * @param eciImageMetadata
     */
    @Override
    public Future<URL> saveECIImageMetadataFile(final String imageId, final String eciImageMetadata) {
        return ImageStoreFactory.pool.submit(new Callable<URL>()
        {
            @Override
            public URL call() throws Exception {
                final String imageJsonFilePath = getImageMetadataFilePath(imageId);
                FileHelper.saveStreamToFile(imageJsonFilePath, new ByteArrayInputStream(eciImageMetadata.getBytes(StandardCharsets.UTF_8)));

                return new URL("file://" + imageJsonFilePath);
            }
        });
    }

    /**
     *
     * @param imageId
     * @return
     */
    private static String getImageFilePath(final String imageId) {
        return mStorageDirectory + "/" + imageId + "/" + BINARY_FILE;
    }

    /**
     *
     * @param imageMetadataFilePath
     * @return
     */
    public String getImageMetadataFilePath(final String imageId) {
        return mStorageDirectory + "/" + imageId + "/" + METADATA_FILE;
    }
}
