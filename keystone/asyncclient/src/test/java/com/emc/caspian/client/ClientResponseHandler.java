package com.emc.caspian.client;

import com.emc.caspian.ccs.client.ClientResponseCallback;
import com.emc.caspian.ccs.client.response.ClientResponse;

class ClientResponseHandler<T> implements ClientResponseCallback<T> {

  ClientResponse<T> response;
  Exception exception;
  boolean isCancelled;
  
	public ClientResponse<T> getResponse() {
		return response;
	}

	public Exception getException() {
		return exception;
	}

	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void completed(ClientResponse<T> clientResponse) {
		synchronized (this) {
			response = clientResponse;
			notifyAll();
		}

	}

	@Override
	public void failed(Exception exception) {
		synchronized (this) {
			this.exception = exception;
			notifyAll();
		}

	}

	@Override
	public void cancelled() {
		synchronized (this) {
			isCancelled = true;
			notifyAll();
		}

	}

	public void clearAll() {
		response = null;
		exception = null;
		isCancelled = false;
	}
}
