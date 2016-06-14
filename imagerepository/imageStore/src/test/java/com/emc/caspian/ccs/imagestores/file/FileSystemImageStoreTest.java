package com.emc.caspian.ccs.imagestores.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import java.lang.Object;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;

import org.junit.Test;

import com.emc.caspian.ccs.common.utils.FileHelper;
import com.emc.caspian.ccs.common.utils.ImageStoreHelper;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreConfig;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.ccs.imagestores.ImageStoreType;
import com.emc.caspian.fabric.config.Configuration;

public class FileSystemImageStoreTest {
	private static File tempFile;
	private static String fileName;
	private static String metaData = "metaDataKey::metaDataValue";
	private static String account;
	
	private static final String OBJECT_KEY = "SampleFile";
	private static final String OBJECT_KEY_ECI = "SampleFile_ECI";
	private static final String OBJECT_KEY_METADATA = "SampleFile_Metadata";
	
	private static ImageStore imageStore  = null;
	private static boolean hasSaveImageFileTestRun;
	private static boolean hasSaveEciImageFileTestRun;
	private static boolean hasSaveEciImageMetaDataFileTestRun;
	
	private static final String BINARY_FILE = "file";
    private static final String METADATA_FILE = "json";
    
	static {
		
		try {
			Configuration.load("src/test/resources/objectResources.conf");
			
			tempFile = createSampleFile(OBJECT_KEY, ImageStoreConfig.FileSystemConfig.tempDir.value());
	    	fileName = tempFile.getAbsolutePath();
	    	
	    	hasSaveImageFileTestRun = false;
	    	hasSaveEciImageFileTestRun = false;
	    	hasSaveEciImageMetaDataFileTestRun = false;
	    	
	    	
	    	imageStore = ImageStoreFactory.getImageStore(ImageStoreType.fromString("file"));
	    	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void SaveImageFileTest(){
		try{
			
			String objectKey = OBJECT_KEY;
			
			if(FileHelper.checkFileExists(fileName)) {

				URL url = imageStore.saveImageFile(objectKey, FileHelper.readFileAsStream(fileName), tempFile.length()).get();
				
				String expectedURL = "file:"+ 
						ImageStoreConfig.FileSystemConfig.STORAGE_DIR.value()+ "/" + objectKey + "/" + BINARY_FILE;
				
				System.out.println("Returned URL: " + url.toString());
				System.out.println("Expected URL: " + expectedURL);
				assertEquals(url.toString(),expectedURL);
				
				if(url.toString().equals(expectedURL)){
					hasSaveImageFileTestRun = true;
				}
			}
		}
		catch(Exception e){
			//TODO Auto Generate exception block
			e.printStackTrace();
		}
	}

	@Test
	public void SaveECIImageFileTest(){
		try{
			
			String objectKey = OBJECT_KEY_ECI;
			
			if(FileHelper.checkFileExists(fileName)) {
	
				URL url = imageStore.saveECIImageFile(objectKey, FileHelper.readFileAsStream(fileName), tempFile.length()).get();
				
				String expectedURL = "file:"+ 
						ImageStoreConfig.FileSystemConfig.STORAGE_DIR.value()+ "/" +objectKey + "/"+BINARY_FILE;
				
				System.out.println("Returned URL: " + url.toString());
				System.out.println("Expected URL: " + expectedURL);
				assertEquals(url.toString(),expectedURL);
				
				if(url.toString().equals(expectedURL)){
					hasSaveEciImageFileTestRun = true;
				}
			}
		}
		catch(Exception e){
			//TODO Auto Generate exception block
			e.printStackTrace();
		}
}

	@Test
	public void SaveECIImageMetadataFileTest(){
		try{
			
			String objectKey = OBJECT_KEY_METADATA;
			
			if(FileHelper.checkFileExists(fileName)) {
	
				URL url = imageStore.saveECIImageMetadataFile(objectKey,metaData).get();
				
				String expectedURL = "file:"+ 
						ImageStoreConfig.FileSystemConfig.STORAGE_DIR.value()+ "/" +objectKey + "/" +METADATA_FILE;
				
				System.out.println("Returned URL: " + url.toString());
				System.out.println("Expected URL: " + expectedURL);
				assertEquals(url.toString(),expectedURL);
				
				if(url.toString().equals(expectedURL)){
					hasSaveEciImageMetaDataFileTestRun = true;
				}
			}
		}
		catch(Exception e){
			//TODO Auto Generate exception block
			e.printStackTrace();
		}
	}
	
	@Test
	public void GetImageFileTest() {
		
		try {
			
			if(! hasSaveImageFileTestRun ) { 
				SaveImageFileTest();
			}
			
			URI uri = new URI("file:" +
					ImageStoreConfig.FileSystemConfig.STORAGE_DIR.value()+ "/" +OBJECT_KEY + "/"+BINARY_FILE);
			
			URL url = uri.toURL();
			
			InputStream stream = imageStore.getImageFile(url).get();
			 
			assertTrue(ImageStoreHelper.isEqual(stream, FileHelper.readFileAsStream(fileName)));
			
			
		} catch (Exception e){
			//do something
		}
		
	}
        @Test
        public void GetImageFileInvalidTest() {

                try {

                        URI uri = new URI("file:" +
                                        "InvalidFile"+ "/" +OBJECT_KEY + "/"+BINARY_FILE);

                        URL url = uri.toURL();

                        InputStream stream = imageStore.getImageFile(url).get();

                        assertNull(stream);


                } catch (Exception e){
                        //do something
                }

        }

	@Test
	public void GetECIImageFileTest() {
		
		try {
			
			if(! hasSaveEciImageFileTestRun ) { 
				SaveECIImageFileTest();
			}
			
			URI uri = new URI("file:" +
					ImageStoreConfig.FileSystemConfig.STORAGE_DIR.value()+ "/" + OBJECT_KEY_ECI + "/"+BINARY_FILE);
			
			URL url = uri.toURL();
			
			InputStream stream = imageStore.getECIImageFile(url).get();
			 
			assertTrue(ImageStoreHelper.isEqual(stream, FileHelper.readFileAsStream(fileName)));
			
			
		} catch (Exception e){
			//do something
		}
		
	}

        @Test
        public void GetECIImageFileInvalidTest() {

                try {
                        URI uri = new URI("file:" +
                                        "InvalidFile"+ "/" + OBJECT_KEY_ECI + "/"+BINARY_FILE);

                        URL url = uri.toURL();

                        InputStream stream = imageStore.getECIImageFile(url).get();

                        assertNull(stream);


                } catch (Exception e){
                        //do something
                }

        }

	@Test
	public void GetECIImageMetadataTest() {
		
		try {
			
			if(! hasSaveEciImageMetaDataFileTestRun ) { 
				SaveECIImageMetadataFileTest();
			}
			
			URI uri = new URI("file:" +
					ImageStoreConfig.FileSystemConfig.STORAGE_DIR.value()+ "/" +
					OBJECT_KEY_METADATA + "/"+METADATA_FILE);
			
			URL url = uri.toURL();
			
			
			String result = imageStore.getECIImageMetadata(url).get();
			assertEquals(result, metaData);
			
		} catch (Exception e){
			//do something
		}
		
	}

        @Test
        public void GetECIImageMetadataInvalidTest() {

                try {
                        URI uri = new URI("file:" +
                                        "InvalidFile"+ "/" +
                                        OBJECT_KEY_METADATA + "/"+METADATA_FILE);

                        URL url = uri.toURL();


                        String result = imageStore.getECIImageMetadata(url).get();
                        assertNull(result);

                } catch (Exception e){
                        //do something
                }

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
