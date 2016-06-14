package com.emc.caspian.ccs.client;

import com.emc.caspian.ccs.client.response.ClientResponse;
/*
 * @author bhandp2
 * */
public interface ClientResponseCallback<T> {
  
  public void completed(ClientResponse<T> clientResponse);

  public void failed(Exception exception);

  public void cancelled();

  
}