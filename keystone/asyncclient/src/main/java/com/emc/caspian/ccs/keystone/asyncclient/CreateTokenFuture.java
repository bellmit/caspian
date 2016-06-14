package com.emc.caspian.ccs.keystone.asyncclient;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.keystone.model.Token;


/**
 * The Class CreateTokenFuture implements the Future interface and returns the response of type
 * ClientResponse used as the Token Object needs to be set the TokenString field from the response
 * header of create Token Response.
 *
 * @author bhandp2
 * @param <T> the generic type
 */
class CreateTokenFuture<Token> implements Future<ClientResponse<com.emc.caspian.ccs.keystone.model.Token>> {

  Future<ClientResponse<com.emc.caspian.ccs.keystone.model.Token>> restClientFuture;

  public CreateTokenFuture(Future<ClientResponse<com.emc.caspian.ccs.keystone.model.Token>> restClientFuture2) {
    super();
    this.restClientFuture = restClientFuture2;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return restClientFuture.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return restClientFuture.isCancelled();
  }

  @Override
  public boolean isDone() {
    return restClientFuture.isDone();
  }

  @Override
  public ClientResponse<com.emc.caspian.ccs.keystone.model.Token> get()
      throws InterruptedException, ExecutionException {
    ClientResponse<com.emc.caspian.ccs.keystone.model.Token> clientResponse = restClientFuture.get();
    checkAndAddToken(clientResponse);
    return clientResponse;
  }

  @Override
  public ClientResponse<com.emc.caspian.ccs.keystone.model.Token> get(long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    ClientResponse<com.emc.caspian.ccs.keystone.model.Token> clientResponse = restClientFuture.get(timeout, unit);
    checkAndAddToken(clientResponse);
    return clientResponse;
  }

  private void checkAndAddToken(ClientResponse<com.emc.caspian.ccs.keystone.model.Token> clientResponse) {
    
    if (ClientStatus.SUCCESS == clientResponse.getStatus()) {
      List<String> headers = clientResponse.getHttpResponse().getHeaders().get("X-Subject-Token");
      if (null != headers) {
        String tokenString = headers.get(0);
        if (null != tokenString) {
          com.emc.caspian.ccs.keystone.model.Token tokenObj = clientResponse.getHttpResponse().getResponseBody();
          tokenObj.setTokenString(tokenString);
        }
      }
    }
  }
  
}
