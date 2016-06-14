package com.emc.caspian.ccs.client;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.Args;

import com.emc.caspian.ccs.client.response.ClientResponse;

/**
 * The Class RestClientFuture implements the Future interface and returns the response of type
 * ClientResponse
 *
 * @author bhandp2
 * @param <T> the generic type
 */

public class RestClientFuture<T> implements Future<ClientResponse<T>> {

  private volatile boolean completed;
  private volatile ClientResponse<T> result;
  private volatile Exception ex;
  Future<HttpResponse> httpResponseFuture;
  Class<T> responseBodyType;

  public RestClientFuture(Future<HttpResponse> response, Class<T> responseBodyType) {
    super();
    this.httpResponseFuture = response;
    this.responseBodyType = responseBodyType;
  }

  /* 
   * @see java.util.concurrent.Future#isCancelled()
   */
  public boolean isCancelled() {
    return httpResponseFuture.isCancelled();
  }

  /* 
   * @see java.util.concurrent.Future#isDone()
   */
  public boolean isDone() {
    return httpResponseFuture.isDone();
  }

  private ClientResponse<T> getResult() throws ExecutionException {
    if (this.ex != null) {
      throw new ExecutionException(this.ex);
    }
    return result;
  }

  /*
   * 
   * @see also java.util.concurrent.Future#get() returns the Client Response and parses the response
   * received from apache http client and converts it into required template object.
   */
  public synchronized ClientResponse<T> get() throws InterruptedException, ExecutionException {

    /*In case the response is ready and get is called twice return the same response again*/
    if(isCompleted()){
      return getResult();
    }
    
    try {
      HttpResponse response = httpResponseFuture.get();
      ClientResponse<T> clientResponse =
          DeserializationUtil.createClientResponse(response, responseBodyType);

      completed(clientResponse);
    } catch (InterruptedException | ExecutionException exception) {
      failed(exception);
      throw exception;
    } catch (ParseException | IOException exc) {
      failed(exc);
    }
    return getResult();

  }

  /*
   * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit) returns the Client
   * Response and parses the response received from apache http client and converts it into
   * required template object.
   */
  public synchronized ClientResponse<T> get(final long timeout, final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    
    /*In case the response is ready and get is called twice return the same response again*/
    if(isCompleted()){
      return getResult();
    }
    
    
    try {

      Args.notNull(unit, "Time unit");
      final long msecs = unit.toMillis(timeout);
      HttpResponse response = httpResponseFuture.get(msecs, unit);
      
      ClientResponse<T> clientResponse =
          DeserializationUtil.createClientResponse(response, responseBodyType);

      completed(clientResponse);
    } catch (InterruptedException | ExecutionException | TimeoutException exception) {
      failed(exception);
      throw exception;
    } catch (ParseException | IOException exc) {
      failed(exc);
    }
    return getResult();
  }

  private boolean completed(final ClientResponse<T> result) {

    if (this.completed) {
      return false;
    }
    this.completed = true;
    this.result = result;

    return true;
  }
  
  private boolean isCompleted(){
    return this.completed;
  }
  
  private boolean failed(final Exception exception) {

    if (this.completed) {
        return false;
    }
    this.completed = true;
    this.ex = exception;
    return true;
    
  }

  /* 
   * @see java.util.concurrent.Future#cancel(boolean)
   */
  public synchronized boolean cancel(final boolean mayInterruptIfRunning) {
    if (this.completed) {
      return false;
    }
    this.completed = true;
    return httpResponseFuture.cancel(mayInterruptIfRunning);

  }
}
