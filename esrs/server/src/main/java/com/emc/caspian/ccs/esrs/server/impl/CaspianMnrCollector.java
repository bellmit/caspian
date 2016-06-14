package com.emc.caspian.ccs.esrs.server.impl;

/* Call SOAP URL and send the Request XML and Get Response XML back */
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.esrs.server.util.Constants;
import com.emc.caspian.ccs.esrs.server.util.ESRSUtil;

public class CaspianMnrCollector {

	private static final Logger _log = LoggerFactory.getLogger(CaspianDetailsCollector.class);
	private static String mnrEndpoint;
	private static String mnrProxy;
	private static int mnrPort;
	private static String mnrWsdl;
	private static final String WSDL_EXTN = "/APG-WS/wsapi/report?wsdl";

	public CaspianMnrCollector() {

		mnrEndpoint = null;
		try {
			mnrEndpoint = ESRSUtil.getEndPointFromCRS(Constants.PLATFORM, Constants.MNR_FRONT);
		} catch (RuntimeException e) {

			_log.warn("Could not fetch the endpoint for MNR-Frontend, will attempt for allinone.");
			//failed to get MNR front end, will look for mnr-allinone
			try {
				mnrEndpoint = ESRSUtil.getEndPointFromCRS(Constants.PLATFORM, Constants.MNR_ALL);
			} catch (RuntimeException e2) {
				_log.error("Could not fetch the endpoint for MNR-Allinone either.",e2);
				mnrEndpoint = null;
			}
		}

		if(null == mnrEndpoint) {
			_log.error("Could not fetch MnR Endpoint. MnR details will be ignored");
			mnrEndpoint = null;
			return;
		}

		mnrWsdl = mnrEndpoint.concat(WSDL_EXTN);
		URL aURL;
		try {
			aURL = new URL(mnrEndpoint);
		} catch (MalformedURLException e) {
			_log.error("CRS Returned an incorrect URL {}. Could not parse. {}", mnrEndpoint, e);
			mnrPort = 0; 
			mnrProxy = null;
			return;
		}

		mnrProxy = aURL.getHost();
		mnrPort = aURL.getPort();
		_log.info("Got Mnr Location: {}:{}",mnrProxy, mnrPort);
	}

    public JSONObject completeSoapRequest(String xAuthToken) throws Exception {

        _log.info("Will try to fetch data from MNR now");
        // use this if you need proxy to connect
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(
                mnrProxy, mnrPort));
        String xmlFile2Send = Constants.MNR_SOAP_XML_FILE;

        // Create the connection with http
        URL url = new URL(mnrWsdl);
        URLConnection connection = url.openConnection(proxy);
        HttpURLConnection httpConn = (HttpURLConnection) connection;

        File xmlFile = new File(xmlFile2Send);
        String xmlString = FileUtils.readFileToString(xmlFile);
        byte[] xmlByteArray = null;
        xmlByteArray = xmlString.getBytes();

        // Set the appropriate HTTP parameters.
        httpConn.setRequestProperty("Content-Type", "text/xml");
        httpConn.setRequestProperty("X-Auth-Token", xAuthToken);
        httpConn.setRequestMethod("POST");
        httpConn.setDoOutput(true);

        OutputStream out = null;
        try {
            // send the XML that was read in to xmlByteArray.
            out = httpConn.getOutputStream();
            out.write(xmlByteArray);
            out.close();
        } catch (IOException e) {
            _log.error("Unable to read XML", e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                _log.error("Could not close file stream", ioe);
            }
        }
        // Read the response.
        httpConn.connect();
        _log.debug("Response status from Mnr:{}", httpConn.getResponseMessage());

        // now parse the response.
        String mnrResponse = IOUtils.toString(httpConn.getInputStream());

        int start = mnrResponse.indexOf("<?");
        int end = mnrResponse.indexOf("</S:Envelope>");
        String mnrXmlResponse = mnrResponse.substring(start, end + 13);
        _log.debug(mnrXmlResponse);
        JSONObject apiJsonData = XML.toJSONObject(mnrXmlResponse);

        if (apiJsonData == null) {
            _log.error("Could not collect data for Node Inventory Service");
            _log.error("Caspian properties to be collected from NIS will be ignored");
            return null;
        }

        _log.debug("Received data from MNR: {}", apiJsonData.toString());
        JSONObject finalJson = apiJsonData.getJSONObject(Constants.ENVELOPE)
                .getJSONObject(Constants.BODY)
                .getJSONObject(Constants.GET_REP_RESP)
                .getJSONObject(Constants.COMP_ELM);

        _log.debug("Will pass on JSON: {}", finalJson.toString());

        return finalJson;
    }
}