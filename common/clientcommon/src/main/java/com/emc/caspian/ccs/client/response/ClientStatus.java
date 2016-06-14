package com.emc.caspian.ccs.client.response;

public enum ClientStatus {
  
    /** The success returned when request is processed successfully. */
    SUCCESS,
    
    /** The error is returned when unable to deserialize the response body to specific type. */
    ERROR_MALFORMED_RESPONSE,
       
    /** The unknown error. */
    ERROR_UNKNOWN,
    
    /** The server is unreachable. */
    ERROR_SERVER_UNREACHABLE,
        
    /**The error for HTTP time out*/
    ERROR_HTTPTIMEOUT,
    
    /** The http error. */
    ERROR_HTTP 
    
    
}
