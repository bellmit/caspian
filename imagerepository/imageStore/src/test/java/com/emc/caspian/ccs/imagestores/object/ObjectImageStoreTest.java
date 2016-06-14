package com.emc.caspian.ccs.imagestores.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.emc.caspian.ccs.common.utils.FileHelper;
import com.emc.caspian.ccs.common.utils.ImageStoreHelper;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreConfig;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.ccs.imagestores.ImageStoreType;
import com.emc.caspian.fabric.config.Configuration;

public class ObjectImageStoreTest {
	
    private static File tempFile;
    private static String fileName;
    
    private static AmazonS3 s3;
    private static S3Object s3Obj;
    private static ImageStore imageStore;
    public static final String SECTION = "image.store.object";
    private static final String OBJECT_KEY = "SampleFile.txt";
    private static final int OBJECT_SIZE = 135;
    private static final int META_DATA_SIZE = 130;
    private static final String TEST_META_DATA = "testing";
    
    
    static {
        try {
            Configuration.load("src/test/resources/objectResources.conf");

            tempFile = createSampleFile(OBJECT_KEY, ImageStoreConfig.ObjectConfig.tempDir.value());
            fileName = tempFile.getAbsolutePath();
            
            imageStore = ImageStoreFactory.getImageStore(ImageStoreType.fromString("ecs"));
            s3 = Mockito.mock(AmazonS3Client.class);
            ObjectImageStore.setS3(s3);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Before
    public final void setupMock() {
	
	MockitoAnnotations.initMocks(this);
	
	InputStream dummyStream =  FileHelper.readFileAsStream(fileName);
	s3Obj = new S3Object();
	s3Obj.setObjectContent(dummyStream);
	
	//Mock an object Summary
	S3ObjectSummary objectSummary = Mockito.mock(S3ObjectSummary.class);
	//Create list with mocked summary
	ArrayList<S3ObjectSummary> al = new ArrayList<S3ObjectSummary>();
	al.add(objectSummary);
	
	ObjectListing objectListing = Mockito.mock(ObjectListing.class);
	
        when(s3.getObject(any(GetObjectRequest.class))).thenReturn(s3Obj);
        when(s3.putObject(any(PutObjectRequest.class))).thenReturn(null);
        when(s3.listObjects(any(ListObjectsRequest.class))).thenReturn(objectListing);

        when(objectListing.getObjectSummaries()).thenReturn(al);
        when(objectSummary.getKey()).thenReturn(OBJECT_KEY);
        when(objectSummary.getSize()).thenReturn((long) 100);
    }
  
    @Test
    public void testSaveImageFile() throws InterruptedException, ExecutionException {
        
        if(FileHelper.checkFileExists(fileName)) {
            
            URL url = imageStore.saveImageFile(OBJECT_KEY, FileHelper.readFileAsStream(fileName), tempFile.length()).get();
            
            String expectedURL = "http://"+
                                 ImageStoreConfig.ObjectConfig.dataNodeConf.value() + ":" +
                                 ImageStoreConfig.ObjectConfig.dataNodePortConf.value()+ "/" +
                                 ImageStoreConfig.ObjectConfig.bucketNameConf.value()+ "/" + OBJECT_KEY;
            
            System.out.println("Returned URL: " + url.toString());
            System.out.println("Expected URL: " + expectedURL);
            assertEquals(url.toString(),expectedURL);
            
            System.out.println("File Length:" + tempFile.length());
            System.out.println("Object Length:" + OBJECT_SIZE);
            assertEquals(OBJECT_SIZE, tempFile.length());
        }
    }

    @Test
    public void testSaveECIImageFile() throws InterruptedException, ExecutionException {
       if(FileHelper.checkFileExists(fileName)) {
            
            URL url = imageStore.saveECIImageFile(OBJECT_KEY, FileHelper.readFileAsStream(fileName), tempFile.length()).get();
            
            String expectedURL = "http://"+
                                 ImageStoreConfig.ObjectConfig.dataNodeConf.value() + ":" +
                                 ImageStoreConfig.ObjectConfig.dataNodePortConf.value()+ "/" +
                                 ImageStoreConfig.ObjectConfig.bucketNameConf.value()+ "/" + OBJECT_KEY;
            
            System.out.println("Returned URL: " + url.toString());
            System.out.println("Expected URL: " + expectedURL);
            assertEquals(url.toString(),expectedURL);
            
            System.out.println("File Length:" + tempFile.length());
            System.out.println("Object Length:" + OBJECT_SIZE);
            assertEquals(OBJECT_SIZE, tempFile.length());
        }
    }

    @Test
    public void testSaveECIImageMetadataFile() throws InterruptedException, ExecutionException {
        if(FileHelper.checkFileExists(fileName)) {
            
            URL url = imageStore.saveECIImageMetadataFile(OBJECT_KEY, TEST_META_DATA).get();
            
            String expectedURL = "http://"+
                                 ImageStoreConfig.ObjectConfig.dataNodeConf.value() + ":" +
                                 ImageStoreConfig.ObjectConfig.dataNodePortConf.value()+ "/" +
                                 ImageStoreConfig.ObjectConfig.bucketNameConf.value()+ "/" + OBJECT_KEY;
            
            System.out.println("Returned URL: " + url.toString());
            System.out.println("Expected URL: " + expectedURL);
            assertEquals(url.toString(),expectedURL);
        }
    }

    @Test
    public void testGetImageFile() {
        
        try {
            
            URI uri = new URI("http://"+
                              ImageStoreConfig.ObjectConfig.dataNodeConf.value()+ ":" +
                              ImageStoreConfig.ObjectConfig.dataNodePortConf.value()+ "/" +
                              ImageStoreConfig.ObjectConfig.bucketNameConf.value()+ "/" + OBJECT_KEY);
            URL url = uri.toURL();
            
            InputStream stream = imageStore.getImageFile(url).get();
            
            long streamSize = ImageStoreHelper.getStreamSize(stream, ImageStoreConfig.ObjectConfig.tempDir.value()).getLeft();            
            System.out.println("File Length:" + streamSize);
            System.out.println("Object Length:" + OBJECT_SIZE);
            
            assertEquals(OBJECT_SIZE, streamSize);
            
        } catch (Exception e){
            e.printStackTrace();
            fail("Got an exception");
        }
        
    }

    @Test
    public void testGetECIImageFile() {
        try {
            
            URI uri = new URI("http://"+
                              ImageStoreConfig.ObjectConfig.dataNodeConf.value()+ ":" +
                              ImageStoreConfig.ObjectConfig.dataNodePortConf.value()+ "/" +
                              ImageStoreConfig.ObjectConfig.bucketNameConf.value()+ "/" + OBJECT_KEY);
            URL url = uri.toURL();
            
            InputStream stream = imageStore.getECIImageFile(url).get();
            
            long streamSize = ImageStoreHelper.getStreamSize(stream, ImageStoreConfig.ObjectConfig.tempDir.value()).getLeft();            
            System.out.println("File Length:" + streamSize);
            System.out.println("Object Length:" + OBJECT_SIZE);
            
            assertEquals(OBJECT_SIZE, streamSize);
            
        } catch (Exception e){
            e.printStackTrace();
            fail("Got an exception");
        }
    }

    @Test
    public void testGetECIImageMetadata() {
        try {
            
            URI uri = new URI("http://"+
                              ImageStoreConfig.ObjectConfig.dataNodeConf.value()+ ":" +
                              ImageStoreConfig.ObjectConfig.dataNodePortConf.value()+ "/" +
                              ImageStoreConfig.ObjectConfig.bucketNameConf.value()+ "/" + OBJECT_KEY);
            URL url = uri.toURL();
            
            String metadataResult = imageStore.getECIImageMetadata(url).get();
            
            System.out.println("File Length:" + metadataResult.length());
            System.out.println("Object Length:" + META_DATA_SIZE);
            
            assertEquals(META_DATA_SIZE, metadataResult.length());
            
        } catch (Exception e){
            e.printStackTrace();
            fail("Got an exception");
        }
    }

    //@Test
    public void testGetImageMetadataFilePath() {
        fail("Not yet implemented");
    }
    

    public static File createSampleFile(String filename, String tempDir) throws IOException {
            
        String filePath = tempDir+"/"+filename+ ".txt";
        new File(filePath).getParentFile().mkdirs();
        final File file = new File(filePath);
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.write("01234567890123456789012345\n");
        writer.write("!@#$%^&*()-=[]{};':',.<>/?\n");
        writer.write("01234567890123456789012345\n");
        writer.write("abcdefghijklmnopqrstuvwxyz\n");
        writer.close();

        return file;
    }
}
