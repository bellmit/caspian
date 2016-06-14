package com.emc.caspian.ccs.license;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;

import com.emc.caspian.ccs.license.util.AppLogger;
import com.google.common.base.Charsets;

@SuppressWarnings("deprecation")
public class RestClientUtil {
	private static SSLContext trustAllContext;
	private static NullHostNameVerifier nullHostnameVerifier;

	private static CloseableHttpAsyncClient httpClient;

	static {
		SSLContext sslContext = getTrustAllContext();

		httpClient = HttpAsyncClients
				.custom()
				.setHostnameVerifier(
						SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
				.setSSLContext(sslContext).build();

		httpClient.start();
	}

	/**
	 * To Handle all the GET Requests for a restClient
	 * 
	 * @param uri
	 * @param headers
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Future<HttpResponse> httpGETRequest(String uri,
			Map<String, String> headers) throws UnsupportedEncodingException {
		HttpGet request;
		// Create the GET request and execute it.
		request = new HttpGet(uri);

		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}

		return httpClient.execute(request, null);
	}

	/**
	 * To Handle all the Post Requests for a restClient
	 * 
	 * @param uri
	 * @param body
	 * @param headers
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Future<HttpResponse> httpPostRequest(String uri,
			Map<String, String> headers, String body)
			throws UnsupportedEncodingException {
		HttpPost request;
		// Create the POST request and execute it.
		request = new HttpPost(uri);

		for (Map.Entry<String, String> header : headers.entrySet()) {
			request.addHeader(header.getKey(), header.getValue());
		}
		if (null != body) {
			StringEntity entity = new StringEntity(body);
			request.setEntity(entity);
		}
		return httpClient.execute(request, null);
	}

	/**
	 * To Handle all the Requests for a restClient, Supported as
	 * 
	 * @param method
	 * @param uri
	 * @param body
	 * @param headers
	 * @return
	 * @throws Exception
	 */
	public static Future<HttpResponse> httpRequest(String method, String uri,
			Map<String, String> headers, List<BasicNameValuePair> body)
			throws Exception {
		HttpEntityEnclosingRequestBase request;
		switch (method) {
		case HttpGet.METHOD_NAME:
			return httpGETRequest(uri, headers);
		case HttpPost.METHOD_NAME:
			request = new HttpPost(uri);
			break;
		case HttpPut.METHOD_NAME:
			request = new HttpPut(uri);
			break;
		default:
			throw new Exception("Not implemented");
		}

		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}

		if (null != body) {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(body,
					Charsets.UTF_8);
			request.setEntity(entity);
		}
		return httpClient.execute(request, null);
	}

	public static SSLContext getTrustAllContext() {
		if (trustAllContext == null) {
			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, newTrustManagers(), new SecureRandom());
				trustAllContext = sc;
			} catch (Exception e) {
				AppLogger
						.error("Unable to register SSL TrustManager to trust all SSL Certificates");
				AppLogger.logException(e);
			}
		}
		return trustAllContext;
	}

	private static TrustManager[] newTrustManagers() {
		return new TrustManager[] { new AllTrustManager() };
	}

	public static NullHostNameVerifier getNullHostnameVerifier() {
		if (nullHostnameVerifier == null) {
			nullHostnameVerifier = new NullHostNameVerifier();
		}
		return nullHostnameVerifier;
	}
}

class AllTrustManager implements X509TrustManager {

	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
			String authType) {
	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
			String authType) {
	}
}

class NullHostNameVerifier implements HostnameVerifier {
	public boolean verify(String arg0, SSLSession arg1) {
		return true;
	}
}
