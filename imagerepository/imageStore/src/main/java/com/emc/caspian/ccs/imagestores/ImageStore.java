package com.emc.caspian.ccs.imagestores;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author shivesh.
 */
public interface ImageStore
{
    /**
     * Returns a stream to Virtual machine image file.
     * Returns null if file not found
     * @param imageLocation
     * @return
     */
    public Future<InputStream> getImageFile(URL imageLocation);

    /**
     * Images specific to ECI fabric -- Docker image
     * @param imageLocation
     * @return
     */
    public Future<InputStream> getECIImageFile(URL imageLocation);

    /**
     * Images specific to ECI fabric -- Docker image
     * @param imageMetadataLocation
     * @return
     */
    public Future<String> getECIImageMetadata(URL imageMetadataLocation);

    /**
     *
     * @param imageFile
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public Future<URL> saveImageFile(final String imageId, InputStream imageFile, long size) throws InterruptedException, ExecutionException;

    /**
     *
     * @param imageFile
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public Future<URL> saveECIImageFile(final String imageId, InputStream imageFile, long size) throws InterruptedException, ExecutionException;

    /**
     *
     * @param eciImageMetadata
     * @throws ExecutionException 
     * @throws InterruptedException 
     */
    public Future<URL> saveECIImageMetadataFile(final String imageId, String eciImageMetadata) throws InterruptedException, ExecutionException;
    
    /**
     * 
     * @param imageId
     */
    public String getImageMetadataFilePath(final String imageId);
    
}
