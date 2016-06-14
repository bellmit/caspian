package com.emc.caspian.ccs.imagestores.swift;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.ws.rs.core.Response;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.message.BasicHeader;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.emc.caspian.ccs.common.utils.FileHelper;
import com.emc.caspian.ccs.common.utils.ImageStoreHelper;
import com.emc.caspian.ccs.imagestores.ImageStore;
import com.emc.caspian.ccs.imagestores.ImageStoreConfig;
import com.emc.caspian.ccs.imagestores.ImageStoreFactory;
import com.emc.caspian.ccs.imagestores.ImageStoreType;
import com.emc.caspian.fabric.config.Configuration;

public class SwiftImageStoreTest {

    private static File tempFile;
    private static String fileName;
    
    private static String METADATA = "abcdefghijklmnopqrstuvwxyz01234567890123456789012345"+
                                     "!@#$%^&*()-=[]{};':',.<>/?01234567890123456789012345"+
	                             "abcdefghijklmnopqrstuvwxyz";
    private static String account;

    private static final String OBJECT_KEY = "SampleFile";
    private static final String OBJECT_KEY_ECI = "SampleFile_ECI";
    private static final String OBJECT_KEY_METADATA = "SampleFile_Metadata";

    private static ImageStore imageStore  = null;
        
    @Mock
    private static HttpResponse httpReponseMock;
    private static CloseableHttpAsyncClient httpClientMock;
    private static Future<HttpResponse> futureMock;
    private static StatusLine statusLineMock;
    private static HttpEntity httpEntityMock;

    static {

	try {
	    Configuration.load("src/test/resources/objectResources.conf");

	    tempFile = createSampleFile(OBJECT_KEY, ImageStoreConfig.SwiftConfig.tempDir.value());
	    fileName = tempFile.getAbsolutePath();
	    account = "AUTH_"+ImageStoreConfig.SwiftConfig.tenantConf.value();

	    imageStore = ImageStoreFactory.getImageStore(ImageStoreType.fromString("swift"));
	    httpClientMock = Mockito.mock(CloseableHttpAsyncClient.class);
	    SwiftImageStore.setHttpClient(httpClientMock);

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public final void setupMock() {
	
	MockitoAnnotations.initMocks(this);
	
	httpReponseMock = Mockito.mock(HttpResponse.class);
	futureMock      = Mockito.mock(Future.class);	
	statusLineMock  = Mockito.mock(StatusLine.class);
	httpEntityMock  = Mockito.mock(HttpEntity.class);
		
	Header[] headerList = { 
	     new BasicHeader("Content-type", "application/x-www-form-urlencoded")
	    ,new BasicHeader("Connection", "keep-alive")
	    ,new BasicHeader("keep-alive", "115")};
	
	when(httpClientMock.execute(any(HttpUriRequest.class),any(FutureCallback.class))).thenReturn(futureMock);
	when(httpReponseMock.getAllHeaders()).thenReturn(headerList);
	when(httpReponseMock.getStatusLine()).thenReturn(statusLineMock);
	when(httpReponseMock.getEntity()).thenReturn(httpEntityMock);
	when(statusLineMock.getStatusCode()).thenReturn(Response.Status.CREATED.getStatusCode());
	

	try {
	    when(futureMock.get()).thenReturn(httpReponseMock);
	    when(httpEntityMock.getContent()).thenReturn(FileHelper.readFileAsStream(fileName));
        } catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (ExecutionException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    @Test
    public void testSaveImageFile() throws InterruptedException, ExecutionException{
	
	String objectKey = OBJECT_KEY;

	if(FileHelper.checkFileExists(fileName)) {
		
        	URL url = imageStore.saveImageFile(objectKey, FileHelper.readFileAsStream(fileName), tempFile.length()).get();
        
        	String expectedURL = "http://"+
        		ImageStoreConfig.SwiftConfig.dataNodeConf.value() + ":" +
        		ImageStoreConfig.SwiftConfig.dataPortConf.value()+ "/" + 
        		ImageStoreConfig.SwiftConfig.versionConf.value()+"/"+
        		account+"/"+
        		ImageStoreConfig.SwiftConfig.containerConf.value()+ "/" + objectKey;
        
        	System.out.println("Returned URL: " + url.toString());
        	System.out.println("Expected URL: " + expectedURL);
        	assertEquals(url.toString(),expectedURL);
	}    
    }

    @Test
    public void testSaveEciImageFile(){
	try{

	    if(FileHelper.checkFileExists(fileName)) {

		String objectKey = OBJECT_KEY_ECI;

		URL url = imageStore.saveImageFile(objectKey, FileHelper.readFileAsStream(fileName), tempFile.length()).get();

		String expectedURL = "http://"+
			ImageStoreConfig.SwiftConfig.dataNodeConf.value() + ":" +
			ImageStoreConfig.SwiftConfig.dataPortConf.value()+ "/" + 
			ImageStoreConfig.SwiftConfig.versionConf.value()+"/"+
			account+"/"+
			ImageStoreConfig.SwiftConfig.containerConf.value()+ "/" + objectKey;

		System.out.println("Returned URL: " + url.toString());
		System.out.println("Expected URL: " + expectedURL);
		assertEquals(url.toString(),expectedURL);
	    }
	}
	catch(Exception e){
	    //TODO Auto Generate exception block
	    e.printStackTrace();
	}
    }

    @Test
    public void testSaveEciImageMetadataFile(){
	try{

	    String objectKey = OBJECT_KEY_METADATA;

	    URL url = imageStore.saveECIImageMetadataFile(objectKey, METADATA).get();

	    String expectedURL = "http://"+
		    ImageStoreConfig.SwiftConfig.dataNodeConf.value()  +":"+
		    ImageStoreConfig.SwiftConfig.dataPortConf.value()  +"/"+ 
		    ImageStoreConfig.SwiftConfig.versionConf.value()   +"/"+
		    account                                            +"/"+
		    ImageStoreConfig.SwiftConfig.containerConf.value() +"/"+ 
		    objectKey;

	    System.out.println("Returned URL: " + url.toString());
	    System.out.println("Expected URL: " + expectedURL);
	    assertEquals(url.toString(),expectedURL);

	}
	catch(Exception e){
	    //TODO Auto Generate exception block
	    e.printStackTrace();
	}
    }
  
    @Test
    public void testGetImageFile() {

	try {

	    URI uri = new URI( ImageStoreConfig.SwiftConfig.protocolConf.value() +"://" +
		    ImageStoreConfig.SwiftConfig.dataNodeConf.value() + ":"  +
		    ImageStoreConfig.SwiftConfig.dataPortConf.value() + "/"  +
		    ImageStoreConfig.SwiftConfig.versionConf.value()  + "/"  +
		    account                                           + "/"  +
		    ImageStoreConfig.SwiftConfig.containerConf.value()+ "/"  + 
		    OBJECT_KEY
		    );

	    URL url = uri.toURL();
	    
	    when(statusLineMock.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
	    InputStream stream = imageStore.getImageFile(url).get();

	    assertTrue(ImageStoreHelper.isEqual(stream, FileHelper.readFileAsStream(fileName)));


	} catch (Exception e){
	    //do something
	}

    }

    @Test
    public void testGetECIImageFile() {

	try {

	    URI uri = new URI( ImageStoreConfig.SwiftConfig.protocolConf.value() +"://" +
		    ImageStoreConfig.SwiftConfig.dataNodeConf.value() + ":"  +
		    ImageStoreConfig.SwiftConfig.dataPortConf.value() + "/"  +
		    ImageStoreConfig.SwiftConfig.versionConf.value()  + "/"  +
		    account                                           + "/"  +
		    ImageStoreConfig.SwiftConfig.containerConf.value()+ "/"  + 
		    OBJECT_KEY_ECI
		    );

	    URL url = uri.toURL();

	    when(statusLineMock.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
	    InputStream stream = imageStore.getECIImageFile(url).get();

	    assertTrue(ImageStoreHelper.isEqual(stream, FileHelper.readFileAsStream(fileName)));

	} catch (Exception e){
	    //do something
	}

    }

    @Test
    public void testGetECIImageMetaData() {

	try {

	    URI uri = new URI( ImageStoreConfig.SwiftConfig.protocolConf.value() +"://" +
		    ImageStoreConfig.SwiftConfig.dataNodeConf.value() + ":"  +
		    ImageStoreConfig.SwiftConfig.dataPortConf.value() + "/"  +
		    ImageStoreConfig.SwiftConfig.versionConf.value()  + "/"  +
		    account                                           + "/"  +
		    ImageStoreConfig.SwiftConfig.containerConf.value()+ "/"  + 
		    OBJECT_KEY_METADATA
		    );

	    URL url = uri.toURL();

	    when(statusLineMock.getStatusCode()).thenReturn(Response.Status.OK.getStatusCode());
	    String result = imageStore.getECIImageMetadata(url).get();

	    System.out.println("Returned result: " + result);
            System.out.println("Expected result: " + METADATA);
	    assertEquals(result, METADATA);

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
