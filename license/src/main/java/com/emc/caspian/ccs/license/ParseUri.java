package com.emc.caspian.ccs.license;

import java.util.Hashtable;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

public class ParseUri {
	
	private int status;
	private UriInfo uriInfo;
	
	public int getStatus() {
		return status;
	}

	public ParseUri(UriInfo uriInfo) {
		super();
		this.uriInfo = uriInfo;
	}

	public Map<String, String> parse (){
		
		Map<String, String> values = new Hashtable<String, String>();
		String absoluteURI= uriInfo.getRequestUri().getQuery();
		
		if(absoluteURI==null){
			this.status=1;
			return null;		
		}
		
		String[] sepQuery = absoluteURI.split("=", 2);
		if(sepQuery.length==1){
			this.status=2;
			return null;
		}
		
		if(sepQuery[0].isEmpty()){
			this.status=3;
			return null;
		}
		
		values.put(sepQuery[0].trim(), sepQuery[1].trim());
		this.status=-1;
		return values;
				
	}

}
