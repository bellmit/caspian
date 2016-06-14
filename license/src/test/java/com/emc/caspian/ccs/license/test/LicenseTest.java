package com.emc.caspian.ccs.license.test;

import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.emc.caspian.ccs.license.Apiv1;
import com.emc.caspian.ccs.license.ETCDClient;
import com.emc.caspian.ccs.license.util.AppLogger;
import com.emc.caspian.ccs.license.util.ConvertDate;
import com.emc.caspian.ccs.license.util.HashGenerator;

@SuppressWarnings({ "deprecation"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LicenseTest{

	static {
		AppLogger.initialize("License service junit tests.");
	}

	private static String ETCD_INSTALL_PATH;
	private static String ETCD_FILENAME;
	private static String ETCD_FULLPATH="/tmp/etcd/etcd";
	private static String ETCD_PATH="/tmp/etcd";

	private static Process proc;

	private static boolean isSupportedPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		return (osName.contains("win") || osName.contains("nux"));
	}
	
	/*
         * NOTE: This would normally be handled in the @BeforeClass annotation to skip all tests
         *       when this assumption fails, however there is a bug in gradle that marks the 
         *       test as a failure if the assumption fails in the @BeforeClass annotation.
         */
	@Before
	public void checkSupportedPlatform() {
		assumeTrue(isSupportedPlatform());
	}
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		if (!isSupportedPlatform()) {
			return;
		}

		AppLogger.info(System.getProperty("os.name"));
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
			ETCD_FILENAME = "etcd.exe";
			ETCD_INSTALL_PATH="/tmp/etcd.zip";
			downloadETCD("https://github.com/coreos/etcd/releases/download/v2.1.0-rc.0/etcd-v2.1.0-rc.0-windows-amd64.zip");
		} else if(System.getProperty("os.name").toLowerCase().contains("nux")) {
			ETCD_FILENAME = "etcd";
			ETCD_INSTALL_PATH="/tmp/etcd.tar.gz";
			downloadETCD("https://github.com/coreos/etcd/releases/download/v2.1.0-rc.0/etcd-v2.1.0-rc.0-linux-amd64.tar.gz");
		} else {
			throw new Exception("Unsupported Operating System");
		}
		File folder = new File("./data/raw");
		folder.mkdirs();
		startLocalETCD();
		
		Map<String,String> testEnv = new HashMap<String, String>();
		testEnv.put("CONTAINER_HOST_ADDRESS", "http://localhost");
		setEnv(testEnv);
		
		FileInputStream fis = new FileInputStream("./src/test/resources/postEtcd");
		String post = "";
		char current;
		while (fis.available() > 0) {
			current = (char) fis.read();
			post = post+current;
		}
		ETCDClient.persistToEtcd("licenses", post);
		FileInputStream fisFeature = new FileInputStream("./src/test/resources/featuresEtcd");
		String postFeature = "";
		char currentFeature;
		while (fis.available() > 0) {
			currentFeature = (char) fisFeature.read();
			postFeature = postFeature+currentFeature;
		}
		ETCDClient.persistToEtcd("licenseProperties", postFeature);
		LicenseTestServer.main();
		

	}

	public class ServerThread extends Thread {

		public void run() {
			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * @throws java.lang.Exceptionl
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (!isSupportedPlatform()) {
			return;
		}
		proc.destroyForcibly();
		File folder = new File("./data");
		FileUtils.deleteDirectory(folder);
	}

	private static void startLocalETCD() throws IOException {
		proc = Runtime.getRuntime().exec(ETCD_FULLPATH);
	}

	private static void downloadETCD(String etcdDownloadURL) throws IOException, NoSuchAlgorithmException, KeyManagementException, InterruptedException {
		ETCD_PATH=ETCD_PATH + File.separator + ETCD_FILENAME;
		File etcdFile = new File(ETCD_PATH);
		if(!etcdFile.exists()){
			AppLogger.info("Local ETCD doesn't exists, downloading it !!");
			File file = new File(ETCD_INSTALL_PATH);
			URL url = new URL(etcdDownloadURL);

			/*****************************************/
			//To skip the certificate check while downloading etcd
			TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return null;
						}

						public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

						public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

					}
			};

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			/*****************************************/

			FileUtils.copyURLToFile(url, file);

			//create output directory is not exists
			File folder = new File("/tmp/etcd/");
			if(!folder.exists()){
				folder.mkdir();
			}

			if(ETCD_INSTALL_PATH.endsWith(".zip")) {
				unZipIt(ETCD_INSTALL_PATH,"/tmp/etcd/");
			} else {
				//As UnTar thru java requires additional library, untarring it using process.
				Process untarProc = Runtime.getRuntime().exec("tar -xvizf /tmp/etcd.tar.gz -C /tmp/etcd etcd-v2.1.0-rc.0-linux-amd64/etcd --strip=1");
				untarProc.waitFor();
			}
		} else {
			AppLogger.info("Local ETCD exists, No download required !!");
		}
	}

	private static void unZipIt(String file, String outputFolder) {
		byte[] buffer = new byte[1024];
		try{
			//get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(file));
			//get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while(ze!=null){
				String fileName = ze.getName();
				AppLogger.info("file unzip/tar : "+ fileName);
				if(!fileName.endsWith(ETCD_FILENAME)){
					ze = zis.getNextEntry();
					continue;
				}

				File newFile = new File(outputFolder + File.separator + ETCD_FILENAME);
				AppLogger.info("file unzip/tar : "+ newFile.getAbsoluteFile());

				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();
			AppLogger.info(" Unzip/tar done");
		}catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	 protected static void setEnv(Map<String, String> newenv)
	    {
		
		//this UT hack allows us to set env vars for the current jvm.
		//Note, system env is not affected
	      try
	        {
	            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	            theEnvironmentField.setAccessible(true);
	            Map<String, String> env = (Map<String, String>)theEnvironmentField.get(null);
	            env.putAll(newenv);
	            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	            theCaseInsensitiveEnvironmentField.setAccessible(true);
	            Map<String, String> cienv = (Map<String, String>)theCaseInsensitiveEnvironmentField.get(null);
	            cienv.putAll(newenv);
	        }
	        catch (NoSuchFieldException e)
	        {
	          try {
	            Class[] classes = Collections.class.getDeclaredClasses();
	            Map<String, String> env = System.getenv();
	            for(Class cl : classes) {
	                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
	                    Field field = cl.getDeclaredField("m");
	                    field.setAccessible(true);
	                    Object obj = field.get(env);
	                    Map<String, String> map = (Map<String, String>) obj;
	                    map.clear();
	                    map.putAll(newenv);
	                }
	            }
	          } catch (Exception e2) {
	            e2.printStackTrace();
	          }
	        } catch (Exception e1) {
	            e1.printStackTrace();
	        } 
	    }

	@Test
	public void test2RetrieveAll() throws Exception {

		Apiv1 apiTest = new Apiv1();
		HttpClient client = new DefaultHttpClient();
		
		HttpGet request1 = new HttpGet("http://localhost:8765/v1/licenses");
		HttpResponse response1= client.execute(request1);
		Assert.assertEquals(200, response1.getStatusLine().getStatusCode());
		request1.releaseConnection();
		
		HttpGet request2 = new HttpGet("http://localhost:8765/v1/licenses?=abc");
		HttpResponse response2= client.execute(request2);
		Assert.assertEquals(400, response2.getStatusLine().getStatusCode());
		request2.releaseConnection();
		
		HttpGet request3 = new HttpGet("http://localhost:8765/v1/licenses?abc");
		HttpResponse response3= client.execute(request3);
		Assert.assertEquals(400, response3.getStatusLine().getStatusCode());
		request3.releaseConnection();
			
	}

	@Test
	public void test3RetrieveLicenses() throws Exception {

		Apiv1 apiTest = new Apiv1();
		HttpClient client = new DefaultHttpClient();

		// Everything all right!
		HttpGet request1 = new HttpGet("http://localhost:8765/v1/licenses/CASPIAN_CLOUDCOMPUTE_CORES/entitlements");
		HttpResponse response1= client.execute(request1);
		Assert.assertEquals(200, response1.getStatusLine().getStatusCode());
		request1.releaseConnection();

		// Negative test case. The license doesn't exist.
		HttpGet request2 = new HttpGet("http://localhost:8765/v1/licenses/does_not_exist/entitlements");
		HttpResponse response2= client.execute(request2);
		Assert.assertEquals(404, response2.getStatusLine().getStatusCode());

	}

	@Test
	public void test4DeleteLicenses() throws Exception {

		Apiv1 apiTest = new Apiv1();
		HttpClient client = new DefaultHttpClient();

		// Everything all right!
		HttpDelete request1 = new HttpDelete("http://localhost:8765/v1/licenses/3cdb7288b410c5bf78c6c03ab6a8052d");
		HttpResponse response1= client.execute(request1);
		Assert.assertEquals(204, response1.getStatusLine().getStatusCode());
		request1.releaseConnection();

		// Negative test case. The license doesn't exist.
		HttpDelete request2 = new HttpDelete("http://localhost:8765/v1/licenses/does_not_exist");
		HttpResponse response2= client.execute(request2);
		Assert.assertEquals(404, response2.getStatusLine().getStatusCode());
		request2.releaseConnection();

		// Everything all right!
		HttpDelete request3 = new HttpDelete("http://localhost:8765/v1/licenses/");
		HttpResponse response3= client.execute(request3);
		Assert.assertEquals(204, response3.getStatusLine().getStatusCode());

	}

	@Test
	public void testConvertDate() throws Exception{
		
		long millisTime=1438819200000L;
		String UTCTime="2015-08-06T00:00:00.000Z";
		String rawTime="06-AUG-2015";

		// Everything all right!
		String response1 =ConvertDate.convertMillisToUTC(millisTime);
		Assert.assertEquals(UTCTime, response1);

		// Everything all right!
		long response2 =  ConvertDate.convertrawToMillis(rawTime);
		Assert.assertEquals(millisTime, response2);
	}

	@Test
	public void testHashGenerator() throws Exception{
		
		String hashString = "The quick brown fox jumps over the lazy dog";
		String hash="9e107d9d372bb6826bd81d3542a419d6";

		String response1 = HashGenerator.gen(hashString);
		Assert.assertEquals(hash, response1);
	}

}
