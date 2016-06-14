package com.emc.caspian.ccs.imagerepo.resources;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HttpClient
{

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPatch patch = new HttpPatch("http://localhost:9999/v2/images/4444");
            String data = "[{\"op\":\"replace\",\"path\":\"/message\",\"value\":\"otherMessage\"},{\"op\":\"add\",\"path\":\"/list/-\",\"value\":\"three\"}]";
            patch.setEntity(new StringEntity(data, ContentType.create("application/json-patch+json")));

            CloseableHttpResponse response = httpclient.execute(patch);
            System.out.println(response.getStatusLine());
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}

